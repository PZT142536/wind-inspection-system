package com.shanghaiwind.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class OperationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String empNo;

    private String name;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 操作详情
     */
    private String detail;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 操作时间
     */
    private LocalDateTime time;

    /**
     * 操作结果: SUCCESS, FAIL
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMsg;
}
