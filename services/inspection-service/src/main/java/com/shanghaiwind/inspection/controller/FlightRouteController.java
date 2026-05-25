package com.shanghaiwind.inspection.controller;

import com.shanghaiwind.common.result.R;
import com.shanghaiwind.inspection.dto.GenerateRouteDTO;
import com.shanghaiwind.inspection.entity.FlightRoute;
import com.shanghaiwind.inspection.service.FlightRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 飞行航线控制器
 */
@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class FlightRouteController {

    private final FlightRouteService flightRouteService;

    /**
     * 根据任务ID查询航线列表
     */
    @GetMapping("/task/{taskId}")
    public R<List<FlightRoute>> listByTask(@PathVariable Long taskId) {
        return R.ok(flightRouteService.getByTaskId(taskId));
    }

    /**
     * 获取航线详情
     */
    @GetMapping("/{id}")
    public R<FlightRoute> detail(@PathVariable Long id) {
        return R.ok(flightRouteService.getById(id));
    }

    /**
     * 生成叶片到货检查航线
     */
    @PostMapping("/generate/blade-arrival")
    public R<List<FlightRoute>> generateBladeArrival(@Valid @RequestBody GenerateRouteDTO dto) {
        return R.ok(flightRouteService.generateBladeArrivalRoutes(
                dto.getTaskId(), dto.getBaseLat(), dto.getBaseLng(),
                dto.getHubHeight(), dto.getBladeLength()));
    }

    /**
     * 生成部套到货检查航线(360度环绕)
     */
    @PostMapping("/generate/orbital-arrival")
    public R<FlightRoute> generateOrbitalArrival(@Valid @RequestBody GenerateRouteDTO dto) {
        return R.ok(flightRouteService.generateOrbitalArrivalRoute(
                dto.getTaskId(), dto.getBaseLat(), dto.getBaseLng(),
                dto.getAltitude(), dto.getComponent(), dto.getRadius()));
    }

    /**
     * 生成叶片吊装监督航线
     */
    @PostMapping("/generate/blade-install")
    public R<FlightRoute> generateBladeInstall(@Valid @RequestBody GenerateRouteDTO dto) {
        return R.ok(flightRouteService.generateBladeInstallRoute(
                dto.getTaskId(), dto.getBaseLat(), dto.getBaseLng(),
                dto.getHubHeight(), dto.getBladeLength(), dto.getBladeNo()));
    }

    /**
     * 生成风机整体验收航线
     */
    @PostMapping("/generate/acceptance")
    public R<FlightRoute> generateAcceptance(@Valid @RequestBody GenerateRouteDTO dto) {
        return R.ok(flightRouteService.generateAcceptanceRoute(
                dto.getTaskId(), dto.getBaseLat(), dto.getBaseLng(),
                dto.getHubHeight(), dto.getTowerHeight()));
    }

    /**
     * 删除航线
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        flightRouteService.removeById(id);
        return R.ok();
    }
}
