package com.shanghaiwind.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 飞行航线实体
 */
@Data
@TableName("biz_flight_route")
public class FlightRoute {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 航线名称
     */
    private String name;

    /**
     * 检查面: 前缘、后缘、主梁面
     */
    private String surface;

    /**
     * 飞行高度(米)
     */
    private Double altitude;

    /**
     * 飞行速度(米/秒)
     */
    private Double speed;

    /**
     * 航线配置JSON
     */
    private String configJson;

    /**
     * 航点数据JSON
     */
    private String waypointsJson;

    /**
     * DJI任务格式JSON
     */
    private String djiMissionJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
