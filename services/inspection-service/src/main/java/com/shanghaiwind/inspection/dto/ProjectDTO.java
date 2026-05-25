package com.shanghaiwind.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 项目DTO
 */
@Data
public class ProjectDTO {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 128, message = "项目名称长度不能超过128")
    private String name;

    @Size(max = 64, message = "项目编码长度不能超过64")
    private String code;

    @Size(max = 128, message = "业主名称长度不能超过128")
    private String owner;

    @Size(max = 256, message = "位置长度不能超过256")
    private String location;

    private Double lat;

    private Double lng;

    private Long managerId;

    private String description;
}
