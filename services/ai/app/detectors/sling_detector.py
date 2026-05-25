import time
import logging
import numpy as np

from app.detectors.base import BaseDetector
from app.models.schemas import DetectionResult, BoundingBox, DetectionType, Severity

logger = logging.getLogger(__name__)

# 吊带/夹具检测类别
# 0: sling-hook (吊钩)
# 1: sling-strap (吊带)
# 2: clamp (夹具)
# 3: connected (正确连接)
# 4: disconnected (未连接/脱落)
CLASS_NAMES = {0: "sling-hook", 1: "sling-strap", 2: "clamp", 3: "connected", 4: "disconnected"}


class SlingDetector(BaseDetector):
    """吊带/夹具位置检测器

    检测逻辑：
    - 检测到 "disconnected" -> HIGH 吊带脱落，严重安全隐患
    - 检测到 "sling-strap" 或 "clamp" 但无 "connected" -> MEDIUM 可能未正确连接
    - 检测到 "connected" -> LOW 连接正常
    """

    def __init__(self, model_path: str, **kwargs):
        super().__init__(model_path, **kwargs)

    def detect(self, image: np.ndarray) -> list[DetectionResult]:
        if not self.model_loaded or self.session is None:
            logger.warning("吊带检测模型未加载")
            return []

        start_time = time.time()

        # 预处理
        blob, scale = self.preprocess(image)
        orig_shape = image.shape[:2]

        # 推理
        input_name = self.session.get_inputs()[0].name
        output = self.session.run(None, {input_name: blob})[0]

        # 后处理
        raw_results = self.postprocess(output, scale, orig_shape)

        # 转换为检测结果
        detections = []
        for r in raw_results:
            class_id = r['class_id']
            label = CLASS_NAMES.get(class_id, "unknown")
            confidence = r['confidence']
            bbox = r['bbox']

            # 判断严重程度
            if label == "disconnected":
                severity = Severity.HIGH
                desc = "检测到吊带/夹具脱落，存在严重安全隐患"
            elif label in ("sling-strap", "clamp"):
                severity = Severity.MEDIUM
                desc = f"检测到{label}，请确认连接状态"
            elif label == "connected":
                severity = Severity.LOW
                desc = "吊带/夹具连接正常"
            else:
                severity = Severity.LOW
                desc = f"检测到{label}"

            detections.append(DetectionResult(
                type=DetectionType.SLING_POSITION,
                label=label,
                confidence=confidence,
                bbox=BoundingBox(x1=bbox[0], y1=bbox[1], x2=bbox[2], y2=bbox[3]),
                severity=severity,
                description=desc
            ))

        inference_time = (time.time() - start_time) * 1000
        logger.info(f"吊带检测完成: {len(detections)}个结果, 耗时{inference_time:.1f}ms")

        return detections
