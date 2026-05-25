package com.shanghaiwind.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检任务实体
 */
@Data
@TableName("biz_inspection_task")
public class InspectionTask {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 机位ID
     */
    private Long turbineId;

    /**
     * 任务类型: ARRIVAL(到货检查), INSTALL(施工监督), ACCEPTANCE(整体验收)
     */
    private String type;

    /**
     * 部套: BLADE, HUB, NACELLE, TOWER, BOX_TRANSFORMER
     */
    private String component;

    /**
     * 状态: PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
     */
    private String status;

    /**
     * 巡检员ID
     */
    private Long inspectorId;

    /**
     * 计划时间
     */
    private LocalDateTime plannedAt;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 进度百分比
     */
    private Integer progress;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
