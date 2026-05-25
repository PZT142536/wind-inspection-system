import time
import logging
import tempfile
import os

import cv2
import httpx
import numpy as np
from fastapi import FastAPI, File, UploadFile, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from configs.settings import (
    HELMET_MODEL_PATH, SLING_MODEL_PATH, INPUT_SIZE,
    CONFIDENCE_THRESHOLD, IOU_THRESHOLD, ALERT_STREAM_KEY,
    REDIS_HOST, REDIS_PORT, REDIS_PASSWORD, FRAME_INTERVAL_SEC,
    MAX_FRAMES_PER_VIDEO, AI_SERVICE_PORT
)
from app.detectors.helmet_detector import HelmetDetector
from app.detectors.sling_detector import SlingDetector
from app.models.schemas import (
    ImageDetectRequest, ImageDetectResponse,
    VideoDetectRequest, VideoDetectResponse,
    DetectionResult, HealthResponse
)
from app.utils.frame_extractor import extract_frames

logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(name)s: %(message)s')
logger = logging.getLogger(__name__)

# FastAPI应用
app = FastAPI(
    title="风机巡检AI推理服务",
    description="安全帽检测 + 吊带/夹具位置检测",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化检测器
helmet_detector = HelmetDetector(
    model_path=HELMET_MODEL_PATH,
    input_size=INPUT_SIZE,
    conf_threshold=CONFIDENCE_THRESHOLD,
    iou_threshold=IOU_THRESHOLD
)

sling_detector = SlingDetector(
    model_path=SLING_MODEL_PATH,
    input_size=INPUT_SIZE,
    conf_threshold=CONFIDENCE_THRESHOLD,
    iou_threshold=IOU_THRESHOLD
)

# Redis连接（延迟初始化）
redis_client = None


def get_redis():
    global redis_client
    if redis_client is None:
        try:
            import redis
            redis_client = redis.Redis(
                host=REDIS_HOST, port=REDIS_PORT,
                password=REDIS_PASSWORD or None,
                decode_responses=True
            )
            redis_client.ping()
            logger.info("Redis连接成功")
        except Exception as e:
            logger.warning(f"Redis连接失败: {e}")
            redis_client = None
    return redis_client


def push_alert_to_redis(detection: DetectionResult, task_id: int = None, media_id: int = None):
    """将高危检测结果推送到Redis Stream"""
    if detection.severity.value not in ("HIGH", "MEDIUM"):
        return

    r = get_redis()
    if r is None:
        return

    try:
        import json
        alert_data = {
            "type": detection.type.value,
            "label": detection.label,
            "severity": detection.severity.value,
            "confidence": str(detection.confidence),
            "description": detection.description,
            "bbox": json.dumps({
                "x1": detection.bbox.x1, "y1": detection.bbox.y1,
                "x2": detection.bbox.x2, "y2": detection.bbox.y2
            }),
            "task_id": str(task_id or ""),
            "media_id": str(media_id or ""),
            "timestamp": str(time.time())
        }
        r.xadd(ALERT_STREAM_KEY, alert_data, maxlen=10000)
        logger.info(f"预警已推送: {detection.label} severity={detection.severity.value}")
    except Exception as e:
        logger.error(f"推送预警失败: {e}")


@app.on_event("startup")
async def startup():
    """启动时加载模型"""
    logger.info("正在加载AI模型...")
    helmet_detector.load_model()
    sling_detector.load_model()
    logger.info(f"安全帽模型: {'已加载' if helmet_detector.model_loaded else '未加载'}")
    logger.info(f"吊带模型: {'已加载' if sling_detector.model_loaded else '未加载'}")


@app.get("/health", response_model=HealthResponse)
async def health():
    """健康检查"""
    return HealthResponse(
        status="ok",
        helmet_model_loaded=helmet_detector.model_loaded,
        sling_model_loaded=sling_detector.model_loaded
    )


@app.post("/detect/image", response_model=ImageDetectResponse)
async def detect_image(
    file: UploadFile = File(...),
    detect_type: str = Form("both"),
    task_id: int = Form(None),
    media_id: int = Form(None),
    frame_time_sec: float = Form(None)
):
    """图片检测

    Args:
        file: 图片文件
        detect_type: 检测类型 (helmet/sling/both)
        task_id: 关联任务ID
        media_id: 关联媒体ID
        frame_time_sec: 帧时间点
    """
    start_time = time.time()

    # 读取图片
    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(status_code=400, detail="无法解析图片")

    detections = []

    # 安全帽检测
    if detect_type in ("helmet", "both"):
        helmet_results = helmet_detector.detect(image)
        detections.extend(helmet_results)

    # 吊带检测
    if detect_type in ("sling", "both"):
        sling_results = sling_detector.detect(image)
        detections.extend(sling_results)

    # 推送高危预警
    for det in detections:
        push_alert_to_redis(det, task_id, media_id)

    inference_time = (time.time() - start_time) * 1000

    return ImageDetectResponse(
        detections=detections,
        frame_time_sec=frame_time_sec,
        inference_time_ms=round(inference_time, 2)
    )


@app.post("/detect/video", response_model=VideoDetectResponse)
async def detect_video(
    file: UploadFile = File(...),
    detect_type: str = Form("both"),
    task_id: int = Form(None),
    media_id: int = Form(None),
    frame_interval: float = Form(None)
):
    """视频检测（抽帧 + 逐帧检测）

    Args:
        file: 视频文件
        detect_type: 检测类型
        task_id: 关联任务ID
        media_id: 关联媒体ID
        frame_interval: 抽帧间隔(秒)
    """
    start_time = time.time()

    # 保存临时文件
    suffix = os.path.splitext(file.filename or "video.mp4")[1]
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        contents = await file.read()
        tmp.write(contents)
        tmp_path = tmp.name

    try:
        interval = frame_interval or FRAME_INTERVAL_SEC
        all_detections = []
        processed = 0

        for frame, timestamp in extract_frames(tmp_path, interval, MAX_FRAMES_PER_VIDEO):
            detections = []

            if detect_type in ("helmet", "both"):
                detections.extend(helmet_detector.detect(frame))

            if detect_type in ("sling", "both"):
                detections.extend(sling_detector.detect(frame))

            # 设置帧时间
            for d in detections:
                d.frame_time_sec = timestamp

            all_detections.extend(detections)

            # 推送高危预警
            for det in detections:
                push_alert_to_redis(det, task_id, media_id)

            processed += 1

        total_time = (time.time() - start_time) * 1000

        return VideoDetectResponse(
            total_frames=processed,
            processed_frames=processed,
            detections=all_detections,
            total_inference_time_ms=round(total_time, 2)
        )
    finally:
        os.unlink(tmp_path)


@app.post("/detect/frame", response_model=ImageDetectResponse)
async def detect_frame_url(request: ImageDetectRequest):
    """通过URL检测单帧"""
    if not request.image_url:
        raise HTTPException(status_code=400, detail="image_url不能为空")

    start_time = time.time()

    # 下载图片
    try:
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.get(request.image_url)
            resp.raise_for_status()
            contents = resp.content
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"下载图片失败: {e}")

    nparr = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(status_code=400, detail="无法解析图片")

    detections = []
    detections.extend(helmet_detector.detect(image))
    detections.extend(sling_detector.detect(image))

    for det in detections:
        push_alert_to_redis(det, request.task_id, request.media_id)

    inference_time = (time.time() - start_time) * 1000

    return ImageDetectResponse(
        detections=detections,
        frame_time_sec=request.frame_time_sec,
        inference_time_ms=round(inference_time, 2)
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=AI_SERVICE_PORT, reload=True)
