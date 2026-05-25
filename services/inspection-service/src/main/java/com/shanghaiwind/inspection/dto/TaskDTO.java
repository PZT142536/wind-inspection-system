package com.shanghaiwind.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务DTO
 */
@Data
public class TaskDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private Long turbineId;

    @NotBlank(message = "任务类型不能为空")
    private String type;

    private String component;

    private Long inspectorId;

    private LocalDateTime plannedAt;

    private String remark;
}
