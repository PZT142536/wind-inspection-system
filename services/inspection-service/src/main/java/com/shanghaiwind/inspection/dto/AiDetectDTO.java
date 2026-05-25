package com.shanghaiwind.inspection.dto;

import lombok.Data;

/**
 * AI检测请求DTO
 */
@Data
public class AiDetectDTO {

    /** 媒体文件ID */
    private Long mediaId;

    /** 任务ID */
    private Long taskId;

    /** 检测类型: helmet/sling/both */
    private String detectType = "both";
}
