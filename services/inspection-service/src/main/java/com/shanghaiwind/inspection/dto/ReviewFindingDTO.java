package com.shanghaiwind.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核发现DTO
 */
@Data
public class ReviewFindingDTO {

    @NotBlank(message = "审核状态不能为空")
    private String status; // CONFIRMED, REJECTED
}
