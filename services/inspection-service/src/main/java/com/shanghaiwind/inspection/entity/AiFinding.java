package com.shanghaiwind.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI检测发现实体
 */
@Data
@TableName("biz_ai_finding")
public class AiFinding {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long mediaId;

    /** 检测类型: SAFETY_HELMET, SLING_POSITION */
    private String type;

    /** 严重程度: HIGH, MEDIUM, LOW */
    private String severity;

    /** 置信度 */
    private Double confidence;

    /** 帧时间点(秒) */
    private Double frameTimeSec;

    /** 帧图片路径 */
    private String frameImagePath;

    /** 检测框坐标JSON */
    private String bboxJson;

    /** 描述 */
    private String description;

    /** 状态: PENDING, CONFIRMED, REJECTED */
    @TableField(fill = FieldFill.INSERT)
    private String status;

    /** 审核人 */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
