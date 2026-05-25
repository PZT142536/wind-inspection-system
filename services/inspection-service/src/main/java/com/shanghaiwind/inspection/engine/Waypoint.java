package com.shanghaiwind.inspection.engine;

import lombok.Builder;
import lombok.Data;

/**
 * 航点数据
 */
@Data
@Builder
public class Waypoint {

    /**
     * 纬度
     */
    private double latitude;

    /**
     * 经度
     */
    private double longitude;

    /**
     * 高度(米)
     */
    private double altitude;

    /**
     * 飞行速度(米/秒)
     */
    private double speed;

    /**
     * 停留时间(秒)
     */
    private int stayTime;

    /**
     * 偏航角(度)
     */
    private double heading;

    /**
     * 俯仰角(度)
     */
    private double gimbalPitch;

    /**
     * 动作: START_RECORDING, STOP_RECORDING, TAKE_PHOTO, NONE
     */
    private String action;
}
