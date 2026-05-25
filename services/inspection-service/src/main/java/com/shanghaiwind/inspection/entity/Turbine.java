package com.shanghaiwind.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机位实体
 */
@Data
@TableName("biz_turbine")
public class Turbine {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 机位编号
     */
    private String code;

    /**
     * 机型
     */
    private String model;

    /**
     * 纬度
     */
    private Double lat;

    /**
     * 经度
     */
    private Double lng;

    /**
     * 海拔高度
     */
    private Double altitude;

    /**
     * 轮毂高度
     */
    private Double hubHeight;

    /**
     * 状态: PENDING, INSTALLED, COMMISSIONED
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
