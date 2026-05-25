package com.shanghaiwind.inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.inspection.engine.FlightPlan;
import com.shanghaiwind.inspection.entity.FlightRoute;

import java.util.List;

/**
 * 飞行航线服务接口
 */
public interface FlightRouteService extends IService<FlightRoute> {

    /**
     * 根据任务ID查询航线列表
     */
    List<FlightRoute> getByTaskId(Long taskId);

    /**
     * 生成叶片到货检查航线
     */
    List<FlightRoute> generateBladeArrivalRoutes(Long taskId, double baseLat, double baseLng,
                                                   double hubHeight, double bladeLength);

    /**
     * 生成部套到货检查航线(360度环绕)
     */
    FlightRoute generateOrbitalArrivalRoute(Long taskId, double baseLat, double baseLng,
                                             double altitude, String component, double radius);

    /**
     * 生成叶片吊装监督航线
     */
    FlightRoute generateBladeInstallRoute(Long taskId, double baseLat, double baseLng,
                                            double hubHeight, double bladeLength, String bladeNo);

    /**
     * 生成风机整体验收航线
     */
    FlightRoute generateAcceptanceRoute(Long taskId, double baseLat, double baseLng,
                                         double hubHeight, double towerHeight);

    /**
     * 保存飞行计划为航线记录
     */
    FlightRoute saveFlightPlan(Long taskId, FlightPlan plan);
}
