package com.shanghaiwind.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 机位DTO
 */
@Data
public class TurbineDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "机位编号不能为空")
    @Size(max = 64, message = "机位编号长度不能超过64")
    private String code;

    @Size(max = 128, message = "机型长度不能超过128")
    private String model;

    @NotNull(message = "纬度不能为空")
    private Double lat;

    @NotNull(message = "经度不能为空")
    private Double lng;

    private Double altitude;

    private Double hubHeight;
}
