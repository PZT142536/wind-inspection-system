package com.shanghaiwind.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目实体
 */
@Data
@TableName("biz_project")
public class Project {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目编码
     */
    private String code;

    /**
     * 业主
     */
    private String owner;

    /**
     * 项目位置
     */
    private String location;

    /**
     * 纬度
     */
    private Double lat;

    /**
     * 经度
     */
    private Double lng;

    /**
     * 状态: ACTIVE, COMPLETED, ARCHIVED
     */
    private String status;

    /**
     * 项目经理ID
     */
    private Long managerId;

    /**
     * 描述
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
