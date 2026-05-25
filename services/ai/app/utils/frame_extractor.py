import logging
import cv2
import numpy as np
from typing import Generator

logger = logging.getLogger(__name__)


def extract_frames(video_path: str, interval_sec: float = 0.5,
                   max_frames: int = 1000) -> Generator[tuple[np.ndarray, float], None, None]:
    """从视频中按时间间隔抽取帧

    Args:
        video_path: 视频文件路径
        interval_sec: 抽帧间隔（秒）
        max_frames: 最大抽帧数

    Yields:
        (frame, timestamp_sec) 元组
    """
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        logger.error(f"无法打开视频: {video_path}")
        return

    fps = cap.get(cv2.CAP_PROP_FPS)
    if fps <= 0:
        fps = 30.0  # 默认30fps

    frame_interval = int(fps * interval_sec)
    frame_count = 0
    extracted = 0

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        if frame_count % frame_interval == 0:
            timestamp = frame_count / fps
            yield frame, timestamp
            extracted += 1

            if extracted >= max_frames:
                logger.info(f"达到最大抽帧数: {max_frames}")
                break

        frame_count += 1

    cap.release()
    logger.info(f"视频抽帧完成: {extracted}帧, 总帧数{frame_count}")


def extract_single_frame(video_path: str, timestamp_sec: float) -> np.ndarray | None:
    """提取视频指定时间点的帧

    Args:
        video_path: 视频文件路径
        timestamp_sec: 时间点（秒）

    Returns:
        帧图像，失败返回None
    """
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        return None

    cap.set(cv2.CAP_PROP_POS_MSEC, timestamp_sec * 1000)
    ret, frame = cap.read()
    cap.release()

    return frame if ret else None
