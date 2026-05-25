package com.shanghaiwind.inspection.controller;

import com.shanghaiwind.common.result.R;
import com.shanghaiwind.inspection.dto.TurbineDTO;
import com.shanghaiwind.inspection.entity.Turbine;
import com.shanghaiwind.inspection.service.TurbineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机位管理控制器
 */
@RestController
@RequestMapping("/turbines")
@RequiredArgsConstructor
public class TurbineController {

    private final TurbineService turbineService;

    /**
     * 根据项目ID查询机位列表
     */
    @GetMapping("/project/{projectId}")
    public R<List<Turbine>> listByProject(@PathVariable Long projectId) {
        return R.ok(turbineService.getByProjectId(projectId));
    }

    /**
     * 获取机位详情
     */
    @GetMapping("/{id}")
    public R<Turbine> detail(@PathVariable Long id) {
        return R.ok(turbineService.getById(id));
    }

    /**
     * 创建机位
     */
    @PostMapping
    public R<Turbine> create(@Valid @RequestBody TurbineDTO dto) {
        return R.ok(turbineService.create(dto));
    }

    /**
     * 更新机位
     */
    @PutMapping("/{id}")
    public R<Turbine> update(@PathVariable Long id, @Valid @RequestBody TurbineDTO dto) {
        return R.ok(turbineService.update(id, dto));
    }

    /**
     * 删除机位
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        turbineService.delete(id);
        return R.ok();
    }
}
