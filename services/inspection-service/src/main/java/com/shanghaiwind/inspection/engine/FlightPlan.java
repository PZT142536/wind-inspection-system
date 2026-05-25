package com.shanghaiwind.inspection.engine;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 飞行计划
 */
@Data
@Builder
public class FlightPlan {

    /**
     * 航线名称
     */
    private String name;

    /**
     * 检查类型: ARRIVAL, INSTALL, ACCEPTANCE
     */
    private String inspectionType;

    /**
     * 部套: BLADE, HUB, NACELLE, TOWER, BOX_TRANSFORMER
     */
    private String component;

    /**
     * 检查面
     */
    private String surface;

    /**
     * 默认飞行高度(米)
     */
    private double defaultAltitude;

    /**
     * 默认飞行速度(米/秒)
     */
    private double defaultSpeed;

    /**
     * 航点列表
     */
    private List<Waypoint> waypoints;
}
