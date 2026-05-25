package com.shanghaiwind.inspection.service;

import com.shanghaiwind.inspection.entity.AiFinding;

import java.util.List;

/**
 * AI推理服务调用接口
 */
public interface AiService {

    /**
     * 对图片进行AI检测
     *
     * @param imageUrl  图片URL
     * @param taskId    任务ID
     * @param mediaId   媒体文件ID
     * @param detectType 检测类型: helmet/sling/both
     * @return 检测结果列表
     */
    List<AiFinding> detectImage(String imageUrl, Long taskId, Long mediaId, String detectType);

    /**
     * 对视频进行AI检测（抽帧+逐帧检测）
     *
     * @param videoUrl  视频URL
     * @param taskId    任务ID
     * @param mediaId   媒体文件ID
     * @param detectType 检测类型
     * @return 检测结果列表
     */
    List<AiFinding> detectVideo(String videoUrl, Long taskId, Long mediaId, String detectType);

    /**
     * 健康检查
     */
    boolean isHealthy();
}
