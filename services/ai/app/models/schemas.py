from pydantic import BaseModel, Field
from typing import Optional
from enum import Enum


class DetectionType(str, Enum):
    """检测类型"""
    SAFETY_HELMET = "SAFETY_HELMET"
    SLING_POSITION = "SLING_POSITION"


class Severity(str, Enum):
    """严重程度"""
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class BoundingBox(BaseModel):
    """检测框"""
    x1: float = Field(..., description="左上角X坐标")
    y1: float = Field(..., description="左上角Y坐标")
    x2: float = Field(..., description="右下角X坐标")
    y2: float = Field(..., description="右下角Y坐标")


class DetectionResult(BaseModel):
    """单个检测结果"""
    type: DetectionType
    label: str = Field(..., description="检测标签")
    confidence: float = Field(..., ge=0, le=1, description="置信度")
    bbox: BoundingBox
    severity: Severity
    description: str = Field("", description="检测描述")


class ImageDetectRequest(BaseModel):
    """图片检测请求"""
    image_url: Optional[str] = Field(None, description="图片URL")
    task_id: Optional[int] = Field(None, description="关联任务ID")
    media_id: Optional[int] = Field(None, description="关联媒体文件ID")
    frame_time_sec: Optional[float] = Field(None, description="帧时间点(秒)")


class ImageDetectResponse(BaseModel):
    """图片检测响应"""
    detections: list[DetectionResult]
    frame_time_sec: Optional[float] = None
    inference_time_ms: float = Field(..., description="推理耗时(ms)")


class VideoDetectRequest(BaseModel):
    """视频检测请求"""
    video_url: str = Field(..., description="视频URL或本地路径")
    task_id: Optional[int] = None
    media_id: Optional[int] = None
    frame_interval: Optional[float] = Field(0.5, description="抽帧间隔(秒)")


class VideoDetectResponse(BaseModel):
    """视频检测响应"""
    total_frames: int
    processed_frames: int
    detections: list[DetectionResult]
    total_inference_time_ms: float


class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str = "ok"
    helmet_model_loaded: bool
    sling_model_loaded: bool
    version: str = "1.0.0"
