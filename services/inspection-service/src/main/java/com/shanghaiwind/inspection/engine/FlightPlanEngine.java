package com.shanghaiwind.inspection.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 飞行计划引擎
 * 负责根据检查类型和部套自动生成飞行航线
 */
@Component
public class FlightPlanEngine {

    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double EARTH_RADIUS = 6371000.0; // 地球半径(米)

    /**
     * 生成叶片到货检查航线
     * @param baseLat 基准纬度(机位点)
     * @param baseLng 基准经度(机位点)
     * @param hubHeight 轮毂高度(米)
     * @param bladeLength 叶片长度(米)
     * @param surface 检查面: leading_edge(前缘), trailing_edge(后缘), spar_cap(主梁面)
     * @return 飞行计划
     */
    public FlightPlan generateBladeArrivalPlan(double baseLat, double baseLng, double hubHeight,
                                                double bladeLength, String surface) {
        List<Waypoint> waypoints = new ArrayList<>();

        // 根据检查面确定无人机方位角
        double azimuth;
        double gimbalPitch;
        switch (surface) {
            case "leading_edge":
                azimuth = 35; // 左上方30-45度
                gimbalPitch = -45;
                break;
            case "trailing_edge":
                azimuth = -35; // 右上方-30到-45度
                gimbalPitch = -45;
                break;
            case "spar_cap":
            default:
                azimuth = 0; // 正上方
                gimbalPitch = -90;
                break;
        }

        // 飞行参数
        double altitude = hubHeight + 5; // 轮毂高度+5米
        double distance = 5; // 距离叶片5米
        double cruiseSpeed = 3.0; // 巡航速度3米/秒
        double tipSpeed = 2.0; // 叶尖速度2米/秒
        double tipZone = 20; // 叶尖20米范围

        // 生成从叶根到叶尖的航点
        int numWaypoints = (int) (bladeLength / 5) + 1; // 每5米一个航点
        for (int i = 0; i <= numWaypoints; i++) {
            double fraction = (double) i / numWaypoints;
            double distanceFromRoot = fraction * bladeLength;

            // 计算航点位置(相对于基准点的偏移)
            double offsetAngle = (90 + azimuth) * DEG_TO_RAD;
            double offsetLat = (distance * Math.cos(offsetAngle)) / EARTH_RADIUS * (180 / Math.PI);
            double offsetLng = (distance * Math.sin(offsetAngle)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

            // 沿叶片方向的偏移
            double bladeAngle = 0; // 假设叶片朝向正北
            double bladeLat = (distanceFromRoot * Math.cos(bladeAngle * DEG_TO_RAD)) / EARTH_RADIUS * (180 / Math.PI);
            double bladeLng = (distanceFromRoot * Math.sin(bladeAngle * DEG_TO_RAD)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

            double wpLat = baseLat + offsetLat + bladeLat;
            double wpLng = baseLng + offsetLng + bladeLng;

            // 叶尖区域减速
            double speed = (distanceFromRoot > bladeLength - tipZone) ? tipSpeed : cruiseSpeed;

            // 第一个航点开始录制
            String action = (i == 0) ? "START_RECORDING" : "NONE";

            Waypoint wp = Waypoint.builder()
                    .latitude(wpLat)
                    .longitude(wpLng)
                    .altitude(altitude)
                    .speed(speed)
                    .heading(azimuth)
                    .gimbalPitch(gimbalPitch)
                    .stayTime(0)
                    .action(action)
                    .build();
            waypoints.add(wp);
        }

        // 最后一个航点停止录制
        if (!waypoints.isEmpty()) {
            Waypoint lastWp = waypoints.get(waypoints.size() - 1);
            waypoints.set(waypoints.size() - 1, Waypoint.builder()
                    .latitude(lastWp.getLatitude())
                    .longitude(lastWp.getLongitude())
                    .altitude(lastWp.getAltitude())
                    .speed(lastWp.getSpeed())
                    .heading(lastWp.getHeading())
                    .gimbalPitch(lastWp.getGimbalPitch())
                    .stayTime(0)
                    .action("STOP_RECORDING")
                    .build());
        }

        String surfaceName = getSurfaceName(surface);

        return FlightPlan.builder()
                .name("叶片到货检查-" + surfaceName)
                .inspectionType("ARRIVAL")
                .component("BLADE")
                .surface(surface)
                .defaultAltitude(altitude)
                .defaultSpeed(cruiseSpeed)
                .waypoints(waypoints)
                .build();
    }

    /**
     * 生成轮毂/机舱/箱变到货检查航线(360度环绕)
     */
    public FlightPlan generateOrbitalArrivalPlan(double baseLat, double baseLng, double altitude,
                                                  String component, double radius) {
        List<Waypoint> waypoints = new ArrayList<>();

        double cruiseSpeed = 3.0;
        int numWaypoints = 36; // 每10度一个航点
        double gimbalPitch = -30; // 斜上方30-45度

        for (int i = 0; i <= numWaypoints; i++) {
            double angle = (360.0 * i / numWaypoints) * DEG_TO_RAD;

            double offsetLat = (radius * Math.cos(angle)) / EARTH_RADIUS * (180 / Math.PI);
            double offsetLng = (radius * Math.sin(angle)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

            double wpLat = baseLat + offsetLat;
            double wpLng = baseLng + offsetLng;

            String action = (i == 0) ? "START_RECORDING" : ((i == numWaypoints) ? "STOP_RECORDING" : "NONE");

            Waypoint wp = Waypoint.builder()
                    .latitude(wpLat)
                    .longitude(wpLng)
                    .altitude(altitude)
                    .speed(cruiseSpeed)
                    .heading(Math.toDegrees(angle) + 90) // 朝向中心
                    .gimbalPitch(gimbalPitch)
                    .stayTime(0)
                    .action(action)
                    .build();
            waypoints.add(wp);
        }

        String componentName = getComponentName(component);

        return FlightPlan.builder()
                .name(componentName + "到货检查-360度环绕")
                .inspectionType("ARRIVAL")
                .component(component)
                .surface("orbital")
                .defaultAltitude(altitude)
                .defaultSpeed(cruiseSpeed)
                .waypoints(waypoints)
                .build();
    }

    /**
     * 生成叶片吊装施工监督航线
     */
    public FlightPlan generateBladeInstallPlan(double baseLat, double baseLng, double hubHeight,
                                                double bladeLength, String bladeNo) {
        List<Waypoint> waypoints = new ArrayList<>();

        double altitude = hubHeight + 10;
        double distance = 8;
        double cruiseSpeed = 2.0;

        // 关键点位置(相对于叶片长度的比例)
        double[] keyPointRatios = {0.0, 0.15, 0.3, 0.5, 0.7, 0.85, 1.0};

        for (int i = 0; i < keyPointRatios.length; i++) {
            double distanceFromRoot = keyPointRatios[i] * bladeLength;

            // 在每个关键点前后各生成一个航点
            for (int j = (i == 0 ? 0 : -1); j <= (i == keyPointRatios.length - 1 ? 0 : 1); j++) {
                double d = distanceFromRoot + j * 3; // 前后3米
                if (d < 0 || d > bladeLength) continue;

                double bladeAngle = 0;
                double bladeLat = (d * Math.cos(bladeAngle * DEG_TO_RAD)) / EARTH_RADIUS * (180 / Math.PI);
                double bladeLng = (d * Math.sin(bladeAngle * DEG_TO_RAD)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

                // 无人机在侧方
                double sideOffset = distance;
                double sideLat = (sideOffset * Math.cos((bladeAngle + 90) * DEG_TO_RAD)) / EARTH_RADIUS * (180 / Math.PI);
                double sideLng = (sideOffset * Math.sin((bladeAngle + 90) * DEG_TO_RAD)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

                double wpLat = baseLat + bladeLat + sideLat;
                double wpLng = baseLng + bladeLng + sideLng;

                // 关键点停留2秒拍照
                int stayTime = (j == 0) ? 2 : 0;
                String action = (j == 0) ? "TAKE_PHOTO" : "NONE";

                Waypoint wp = Waypoint.builder()
                        .latitude(wpLat)
                        .longitude(wpLng)
                        .altitude(altitude)
                        .speed(cruiseSpeed)
                        .heading(-90) // 朝向叶片
                        .gimbalPitch(-30)
                        .stayTime(stayTime)
                        .action(action)
                        .build();
                waypoints.add(wp);
            }
        }

        return FlightPlan.builder()
                .name("叶片吊装监督-" + bladeNo)
                .inspectionType("INSTALL")
                .component("BLADE")
                .surface("all")
                .defaultAltitude(altitude)
                .defaultSpeed(cruiseSpeed)
                .waypoints(waypoints)
                .build();
    }

    /**
     * 生成风机整体验收检查航线
     */
    public FlightPlan generateAcceptancePlan(double baseLat, double baseLng, double hubHeight,
                                              double towerHeight) {
        List<Waypoint> waypoints = new ArrayList<>();

        double cruiseSpeed = 3.0;
        double radius = 30; // 环绕半径30米

        // 1. 塔底环绕
        for (int i = 0; i <= 12; i++) {
            double angle = (360.0 * i / 12) * DEG_TO_RAD;
            double offsetLat = (radius * Math.cos(angle)) / EARTH_RADIUS * (180 / Math.PI);
            double offsetLng = (radius * Math.sin(angle)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

            Waypoint wp = Waypoint.builder()
                    .latitude(baseLat + offsetLat)
                    .longitude(baseLng + offsetLng)
                    .altitude(10)
                    .speed(cruiseSpeed)
                    .heading(Math.toDegrees(angle) + 90)
                    .gimbalPitch(-20)
                    .stayTime(0)
                    .action(i == 0 ? "START_RECORDING" : "NONE")
                    .build();
            waypoints.add(wp);
        }

        // 2. 基础外观环绕
        for (int i = 0; i <= 12; i++) {
            double angle = (360.0 * i / 12) * DEG_TO_RAD;
            double offsetLat = (radius * Math.cos(angle)) / EARTH_RADIUS * (180 / Math.PI);
            double offsetLng = (radius * Math.sin(angle)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

            Waypoint wp = Waypoint.builder()
                    .latitude(baseLat + offsetLat)
                    .longitude(baseLng + offsetLng)
                    .altitude(5)
                    .speed(cruiseSpeed)
                    .heading(Math.toDegrees(angle) + 90)
                    .gimbalPitch(-45)
                    .stayTime(0)
                    .action("NONE")
                    .build();
            waypoints.add(wp);
        }

        // 3. 水平环绕机舱
        double nacelleAlt = hubHeight + 5;
        for (int i = 0; i <= 24; i++) {
            double angle = (360.0 * i / 24) * DEG_TO_RAD;
            double offsetLat = (radius * Math.cos(angle)) / EARTH_RADIUS * (180 / Math.PI);
            double offsetLng = (radius * Math.sin(angle)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

            Waypoint wp = Waypoint.builder()
                    .latitude(baseLat + offsetLat)
                    .longitude(baseLng + offsetLng)
                    .altitude(nacelleAlt)
                    .speed(cruiseSpeed)
                    .heading(Math.toDegrees(angle) + 90)
                    .gimbalPitch(0)
                    .stayTime(0)
                    .action("NONE")
                    .build();
            waypoints.add(wp);
        }

        // 4. 俯视环绕机舱和轮毂
        for (int i = 0; i <= 24; i++) {
            double angle = (360.0 * i / 24) * DEG_TO_RAD;
            double offsetLat = (radius * Math.cos(angle)) / EARTH_RADIUS * (180 / Math.PI);
            double offsetLng = (radius * Math.sin(angle)) / (EARTH_RADIUS * Math.cos(baseLat * DEG_TO_RAD)) * (180 / Math.PI);

            Waypoint wp = Waypoint.builder()
                    .latitude(baseLat + offsetLat)
                    .longitude(baseLng + offsetLng)
                    .altitude(nacelleAlt + 15)
                    .speed(cruiseSpeed)
                    .heading(Math.toDegrees(angle) + 90)
                    .gimbalPitch(-90)
                    .stayTime(0)
                    .action(i == 48 ? "STOP_RECORDING" : "NONE")
                    .build();
            waypoints.add(wp);
        }

        return FlightPlan.builder()
                .name("风机整体验收检查")
                .inspectionType("ACCEPTANCE")
                .component("ALL")
                .surface("all")
                .defaultAltitude(10)
                .defaultSpeed(cruiseSpeed)
                .waypoints(waypoints)
                .build();
    }

    private String getSurfaceName(String surface) {
        switch (surface) {
            case "leading_edge": return "前缘";
            case "trailing_edge": return "后缘";
            case "spar_cap": return "主梁面";
            default: return surface;
        }
    }

    private String getComponentName(String component) {
        switch (component) {
            case "BLADE": return "叶片";
            case "HUB": return "轮毂";
            case "NACELLE": return "机舱";
            case "TOWER": return "塔筒";
            case "BOX_TRANSFORMER": return "箱变";
            default: return component;
        }
    }
}
