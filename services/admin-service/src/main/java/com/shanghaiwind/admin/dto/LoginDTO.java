package com.shanghaiwind.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录DTO
 */
@Data
public class LoginDTO {

    @NotBlank(message = "工号不能为空")
    private String empNo;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    private String captchaKey;
}
