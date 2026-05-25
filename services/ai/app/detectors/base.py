import time
import logging
import numpy as np
import cv2
from pathlib import Path
from typing import Optional

try:
    import onnxruntime as ort
except ImportError:
    ort = None

from app.models.schemas import DetectionResult, BoundingBox, DetectionType, Severity

logger = logging.getLogger(__name__)


class BaseDetector:
    """检测器基类"""

    def __init__(self, model_path: str, input_size: int = 640,
                 conf_threshold: float = 0.5, iou_threshold: float = 0.45):
        self.model_path = model_path
        self.input_size = input_size
        self.conf_threshold = conf_threshold
        self.iou_threshold = iou_threshold
        self.session: Optional[ort.InferenceSession] = None
        self.model_loaded = False

    def load_model(self) -> bool:
        """加载ONNX模型"""
        if not Path(self.model_path).exists():
            logger.warning(f"模型文件不存在: {self.model_path}")
            return False
        try:
            providers = ['CPUExecutionProvider']
            if ort and 'CUDAExecutionProvider' in ort.get_available_providers():
                providers.insert(0, 'CUDAExecutionProvider')

            self.session = ort.InferenceSession(self.model_path, providers=providers)
            self.model_loaded = True
            logger.info(f"模型加载成功: {self.model_path}, providers={self.session.get_providers()}")
            return True
        except Exception as e:
            logger.error(f"模型加载失败: {e}")
            return False

    def preprocess(self, image: np.ndarray) -> tuple[np.ndarray, tuple[float, float]]:
        """预处理图片：resize + normalize"""
        h, w = image.shape[:2]
        scale = self.input_size / max(h, w)
        new_h, new_w = int(h * scale), int(w * scale)

        resized = cv2.resize(image, (new_w, new_h))

        # 填充到正方形
        padded = np.full((self.input_size, self.input_size, 3), 114, dtype=np.uint8)
        padded[:new_h, :new_w, :] = resized

        # BGR -> RGB, HWC -> CHW, normalize
        blob = padded[:, :, ::-1].transpose(2, 0, 1).astype(np.float32) / 255.0
        blob = np.expand_dims(blob, axis=0)

        return blob, (scale, scale)

    def postprocess(self, output: np.ndarray, scale: tuple[float, float],
                    orig_shape: tuple[int, int]) -> list[dict]:
        """后处理：NMS + 坐标还原"""
        predictions = output[0]  # (N, classes+4)

        # 过滤低置信度
        scores = predictions[:, 4:]
        max_scores = np.max(scores, axis=1)
        mask = max_scores >= self.conf_threshold
        predictions = predictions[mask]
        max_scores = max_scores[mask]

        if len(predictions) == 0:
            return []

        # 提取框和类别
        boxes = predictions[:, :4]  # cx, cy, w, h
        class_ids = np.argmax(scores[mask], axis=1)

        # 转换为x1,y1,x2,y2
        x1 = boxes[:, 0] - boxes[:, 2] / 2
        y1 = boxes[:, 1] - boxes[:, 3] / 2
        x2 = boxes[:, 0] + boxes[:, 2] / 2
        y2 = boxes[:, 1] + boxes[:, 3] / 2

        # 缩放回原图尺寸
        x1 /= scale[0]
        y1 /= scale[1]
        x2 /= scale[0]
        y2 /= scale[1]

        # 裁剪
        orig_h, orig_w = orig_shape
        x1 = np.clip(x1, 0, orig_w)
        y1 = np.clip(y1, 0, orig_h)
        x2 = np.clip(x2, 0, orig_w)
        y2 = np.clip(y2, 0, orig_h)

        # NMS
        keep_indices = self._nms(
            np.column_stack([x1, y1, x2, y2]),
            max_scores,
            self.iou_threshold
        )

        results = []
        for idx in keep_indices:
            results.append({
                'bbox': [float(x1[idx]), float(y1[idx]), float(x2[idx]), float(y2[idx])],
                'confidence': float(max_scores[idx]),
                'class_id': int(class_ids[idx])
            })

        return results

    def _nms(self, boxes: np.ndarray, scores: np.ndarray, iou_threshold: float) -> list[int]:
        """非极大值抑制"""
        x1 = boxes[:, 0]
        y1 = boxes[:, 1]
        x2 = boxes[:, 2]
        y2 = boxes[:, 3]

        areas = (x2 - x1) * (y2 - y1)
        order = scores.argsort()[::-1]

        keep = []
        while order.size > 0:
            i = order[0]
            keep.append(i)

            xx1 = np.maximum(x1[i], x1[order[1:]])
            yy1 = np.maximum(y1[i], y1[order[1:]])
            xx2 = np.minimum(x2[i], x2[order[1:]])
            yy2 = np.minimum(y2[i], y2[order[1:]])

            w = np.maximum(0.0, xx2 - xx1)
            h = np.maximum(0.0, yy2 - yy1)
            inter = w * h
            iou = inter / (areas[i] + areas[order[1:]] - inter)

            inds = np.where(iou <= iou_threshold)[0]
            order = order[inds + 1]

        return keep

    def detect(self, image: np.ndarray) -> list[DetectionResult]:
        """检测图片，子类需实现此方法"""
        raise NotImplementedError
