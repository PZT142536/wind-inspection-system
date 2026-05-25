package com.shanghaiwind.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 工号
     */
    private String empNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 部门
     */
    private String dept;

    /**
     * 用户来源: LDAP, LOCAL
     */
    private String source;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 盐值
     */
    private String salt;

    /**
     * 状态: 1启用, 0禁用
     */
    private Integer status;

    /**
     * 锁定截止时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
