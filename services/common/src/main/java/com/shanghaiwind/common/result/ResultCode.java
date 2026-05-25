package com.shanghaiwind.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),

    // 认证相关 1xxx
    UNAUTHORIZED(1001, "未登录或登录已过期"),
    FORBIDDEN(1002, "无权限访问"),
    TOKEN_INVALID(1003, "Token无效"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    ACCOUNT_LOCKED(1005, "账户已锁定"),
    ACCOUNT_DISABLED(1006, "账户已禁用"),
    LOGIN_FAILED(1007, "登录失败"),
    CAPTCHA_ERROR(1008, "验证码错误"),
    CAPTCHA_EXPIRED(1009, "验证码已过期"),

    // 参数校验 2xxx
    PARAM_ERROR(2001, "参数错误"),
    PARAM_MISSING(2002, "参数缺失"),
    PARAM_TYPE_ERROR(2003, "参数类型错误"),

    // 业务逻辑 3xxx
    DATA_NOT_FOUND(3001, "数据不存在"),
    DATA_ALREADY_EXISTS(3002, "数据已存在"),
    DATA_SAVE_FAILED(3003, "数据保存失败"),
    DATA_UPDATE_FAILED(3004, "数据更新失败"),
    DATA_DELETE_FAILED(3005, "数据删除失败"),

    // 文件相关 4xxx
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_NOT_FOUND(4002, "文件不存在"),
    FILE_TYPE_ERROR(4003, "文件类型错误"),
    FILE_SIZE_EXCEEDED(4004, "文件大小超出限制"),

    // 系统错误 5xxx
    SYSTEM_ERROR(5001, "系统错误"),
    SERVICE_UNAVAILABLE(5002, "服务不可用"),
    DATABASE_ERROR(5003, "数据库错误"),
    REDIS_ERROR(5004, "Redis错误");

    private final int code;
    private final String message;
}
