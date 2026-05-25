package com.shanghaiwind.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警消息实体
 */
@Data
@TableName("biz_alert")
public class Alert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long findingId;

    private Long taskId;

    /** 目标用户ID */
    private Long targetUserId;

    /** 预警类型 */
    private String type;

    /** 预警消息 */
    private String message;

    /** 发送时间 */
    private LocalDateTime sentAt;

    /** 已读时间 */
    private LocalDateTime readAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
