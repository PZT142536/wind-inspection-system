import time
import logging
import numpy as np

from app.detectors.base import BaseDetector
from app.models.schemas import DetectionResult, BoundingBox, DetectionType, Severity

logger = logging.getLogger(__name__)

# 安全帽检测类别
# 0: helmet (佩戴安全帽)
# 1: no-helmet (未佩戴安全帽)
# 2: person (人员)
CLASS_NAMES = {0: "helmet", 1: "no-helmet", 2: "person"}


class HelmetDetector(BaseDetector):
    """安全帽检测器

    检测逻辑：
    - 检测到 "no-helmet" 人员 -> HIGH 严重违规
    - 检测到 "person" 但无对应 "helmet" -> MEDIUM 潜在风险
    - 检测到 "helmet" -> LOW 正常（佩戴安全帽）
    """

    def __init__(self, model_path: str, **kwargs):
        super().__init__(model_path, **kwargs)

    def detect(self, image: np.ndarray) -> list[DetectionResult]:
        if not self.model_loaded or self.session is None:
            logger.warning("安全帽模型未加载")
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
            if label == "no-helmet":
                severity = Severity.HIGH
                desc = "检测到未佩戴安全帽的人员，存在安全隐患"
            elif label == "person":
                severity = Severity.MEDIUM
                desc = "检测到人员，但未检测到安全帽佩戴"
            else:  # helmet
                severity = Severity.LOW
                desc = "检测到安全帽佩戴正常"

            detections.append(DetectionResult(
                type=DetectionType.SAFETY_HELMET,
                label=label,
                confidence=confidence,
                bbox=BoundingBox(x1=bbox[0], y1=bbox[1], x2=bbox[2], y2=bbox[3]),
                severity=severity,
                description=desc
            ))

        inference_time = (time.time() - start_time) * 1000
        logger.info(f"安全帽检测完成: {len(detections)}个结果, 耗时{inference_time:.1f}ms")

        return detections
