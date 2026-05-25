package com.shanghaiwind.inspection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成航线DTO
 */
@Data
public class GenerateRouteDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "基准纬度不能为空")
    private Double baseLat;

    @NotNull(message = "基准经度不能为空")
    private Double baseLng;

    /**
     * 轮毂高度(米)
     */
    private Double hubHeight;

    /**
     * 叶片长度(米)
     */
    private Double bladeLength;

    /**
     * 飞行高度(米)
     */
    private Double altitude;

    /**
     * 部套: BLADE, HUB, NACELLE, TOWER, BOX_TRANSFORMER
     */
    private String component;

    /**
     * 环绕半径(米)
     */
    private Double radius;

    /**
     * 塔筒高度(米)
     */
    private Double towerHeight;

    /**
     * 叶片编号
     */
    private String bladeNo;
}
