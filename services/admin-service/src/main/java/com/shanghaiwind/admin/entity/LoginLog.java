package com.shanghaiwind.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 */
@Data
@TableName("sys_login_log")
public class LoginLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String empNo;

    private String name;

    private String ip;

    /**
     * 来源: WEB, APP
     */
    private String source;

    private String userAgent;

    private LocalDateTime loginTime;

    /**
     * 状态: 1成功, 0失败
     */
    private Integer status;

    private String failReason;
}
