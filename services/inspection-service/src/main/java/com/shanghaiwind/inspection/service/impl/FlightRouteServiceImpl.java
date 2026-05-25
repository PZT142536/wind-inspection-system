package com.shanghaiwind.inspection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.inspection.entity.FlightRoute;
import com.shanghaiwind.inspection.engine.FlightPlan;
import com.shanghaiwind.inspection.engine.FlightPlanEngine;
import com.shanghaiwind.inspection.engine.Waypoint;
import com.shanghaiwind.inspection.mapper.FlightRouteMapper;
import com.shanghaiwind.inspection.service.FlightRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞行航线服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlightRouteServiceImpl extends ServiceImpl<FlightRouteMapper, FlightRoute>
        implements FlightRouteService {

    private final FlightPlanEngine flightPlanEngine;
    private final ObjectMapper objectMapper;

    @Override
    public List<FlightRoute> getByTaskId(Long taskId) {
        return baseMapper.selectByTaskId(taskId);
    }

    @Override
    @Transactional
    public List<FlightRoute> generateBladeArrivalRoutes(Long taskId, double baseLat, double baseLng,
                                                          double hubHeight, double bladeLength) {
        List<FlightRoute> routes = new ArrayList<>();

        // 生成三条航线: 前缘、后缘、主梁面
        String[] surfaces = {"leading_edge", "trailing_edge", "spar_cap"};
        for (String surface : surfaces) {
            FlightPlan plan = flightPlanEngine.generateBladeArrivalPlan(
                    baseLat, baseLng, hubHeight, bladeLength, surface);
            FlightRoute route = saveFlightPlan(taskId, plan);
            routes.add(route);
        }

        return routes;
    }

    @Override
    @Transactional
    public FlightRoute generateOrbitalArrivalRoute(Long taskId, double baseLat, double baseLng,
                                                     double altitude, String component, double radius) {
        FlightPlan plan = flightPlanEngine.generateOrbitalArrivalPlan(
                baseLat, baseLng, altitude, component, radius);
        return saveFlightPlan(taskId, plan);
    }

    @Override
    @Transactional
    public FlightRoute generateBladeInstallRoute(Long taskId, double baseLat, double baseLng,
                                                   double hubHeight, double bladeLength, String bladeNo) {
        FlightPlan plan = flightPlanEngine.generateBladeInstallPlan(
                baseLat, baseLng, hubHeight, bladeLength, bladeNo);
        return saveFlightPlan(taskId, plan);
    }

    @Override
    @Transactional
    public FlightRoute generateAcceptanceRoute(Long taskId, double baseLat, double baseLng,
                                                double hubHeight, double towerHeight) {
        FlightPlan plan = flightPlanEngine.generateAcceptancePlan(
                baseLat, baseLng, hubHeight, towerHeight);
        return saveFlightPlan(taskId, plan);
    }

    @Override
    @Transactional
    public FlightRoute saveFlightPlan(Long taskId, FlightPlan plan) {
        FlightRoute route = new FlightRoute();
        route.setTaskId(taskId);
        route.setName(plan.getName());
        route.setSurface(plan.getSurface());
        route.setAltitude(plan.getDefaultAltitude());
        route.setSpeed(plan.getDefaultSpeed());

        try {
            // 保存航线配置
            Map<String, Object> config = new HashMap<>();
            config.put("inspectionType", plan.getInspectionType());
            config.put("component", plan.getComponent());
            route.setConfigJson(objectMapper.writeValueAsString(config));

            // 保存航点数据
            route.setWaypointsJson(objectMapper.writeValueAsString(plan.getWaypoints()));

            // 生成DJI格式任务数据
            Map<String, Object> djiMission = convertToDjiFormat(plan);
            route.setDjiMissionJson(objectMapper.writeValueAsString(djiMission));

        } catch (JsonProcessingException e) {
            log.error("序列化航线数据失败", e);
            throw new BusinessException("航线数据序列化失败");
        }

        save(route);
        return route;
    }

    /**
     * 转换为DJI WaypointMission格式
     */
    private Map<String, Object> convertToDjiFormat(FlightPlan plan) {
        Map<String, Object> mission = new HashMap<>();
        mission.put("name", plan.getName());
        mission.put("autoFlightSpeed", plan.getDefaultSpeed());
        mission.put("finishedAction", 0); // HOVER
        mission.put("headingMode", 0); // AUTO
        mission.put("flightPathMode", 0); // STRAIGHT_LINE

        List<Map<String, Object>> waypoints = new ArrayList<>();
        for (Waypoint wp : plan.getWaypoints()) {
            Map<String, Object> djiWp = new HashMap<>();
            djiWp.put("latitude", wp.getLatitude());
            djiWp.put("longitude", wp.getLongitude());
            djiWp.put("altitude", wp.getAltitude());
            djiWp.put("speed", wp.getSpeed());
            djiWp.put("heading", (int) wp.getHeading());
            djiWp.put("gimbalPitch", (int) wp.getGimbalPitch());
            djiWp.put("stayTime", wp.getStayTime());

            // 航点动作
            List<Map<String, Object>> actions = new ArrayList<>();
            if ("START_RECORDING".equals(wp.getAction())) {
                actions.add(createDjiAction(1, 1)); // START_RECORD
            } else if ("STOP_RECORDING".equals(wp.getAction())) {
                actions.add(createDjiAction(2, 1)); // STOP_RECORD
            } else if ("TAKE_PHOTO".equals(wp.getAction())) {
                actions.add(createDjiAction(0, 1)); // TAKE_PHOTO
            }
            djiWp.put("actions", actions);

            waypoints.add(djiWp);
        }
        mission.put("waypoints", waypoints);

        return mission;
    }

    private Map<String, Object> createDjiAction(int type, int param) {
        Map<String, Object> action = new HashMap<>();
        action.put("type", type);
        action.put("param", param);
        return action;
    }
}
