package com.shanghaiwind.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户DTO
 */
@Data
public class UserDTO {

    @NotBlank(message = "工号不能为空")
    @Size(max = 32, message = "工号长度不能超过32")
    private String empNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过64")
    private String name;

    @Size(max = 128, message = "部门长度不能超过128")
    private String dept;

    @Size(min = 6, max = 32, message = "密码长度在6-32之间")
    private String password;
}
