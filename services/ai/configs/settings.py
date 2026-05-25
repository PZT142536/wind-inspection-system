import os
from pathlib import Path

# 基础路径
BASE_DIR = Path(__file__).resolve().parent.parent

# 模型权重路径
HELMET_MODEL_PATH = os.getenv("HELMET_MODEL_PATH", str(BASE_DIR / "weights" / "helmet_best.onnx"))
SLING_MODEL_PATH = os.getenv("SLING_MODEL_PATH", str(BASE_DIR / "weights" / "sling_best.onnx"))

# 推理参数
CONFIDENCE_THRESHOLD = float(os.getenv("CONF_THRESHOLD", "0.5"))
IOU_THRESHOLD = float(os.getenv("IOU_THRESHOLD", "0.45"))
INPUT_SIZE = int(os.getenv("INPUT_SIZE", "640"))

# Redis配置
REDIS_HOST = os.getenv("REDIS_HOST", "127.0.0.1")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", "")
REDIS_DB = int(os.getenv("REDIS_DB", "0"))

# 预警流
ALERT_STREAM_KEY = "ai:alerts"

# 视频抽帧参数
FRAME_INTERVAL_SEC = float(os.getenv("FRAME_INTERVAL_SEC", "0.5"))  # 每0.5秒抽一帧
MAX_FRAMES_PER_VIDEO = int(os.getenv("MAX_FRAMES_PER_VIDEO", "1000"))

# 服务端口
AI_SERVICE_PORT = int(os.getenv("AI_SERVICE_PORT", "8090"))
