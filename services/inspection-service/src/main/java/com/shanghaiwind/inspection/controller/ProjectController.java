package com.shanghaiwind.inspection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.common.result.R;
import com.shanghaiwind.inspection.dto.ProjectDTO;
import com.shanghaiwind.inspection.entity.Project;
import com.shanghaiwind.inspection.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 分页查询项目
     */
    @GetMapping
    public R<IPage<Project>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(projectService.pageQuery(pageNum, pageSize, keyword, status));
    }

    /**
     * 获取所有项目（下拉选择用）
     */
    @GetMapping("/all")
    public R<List<Project>> listAll() {
        return R.ok(projectService.list());
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    public R<Project> detail(@PathVariable Long id) {
        return R.ok(projectService.getById(id));
    }

    /**
     * 创建项目
     */
    @PostMapping
    public R<Project> create(@Valid @RequestBody ProjectDTO dto) {
        return R.ok(projectService.create(dto));
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public R<Project> update(@PathVariable Long id, @Valid @RequestBody ProjectDTO dto) {
        return R.ok(projectService.update(id, dto));
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return R.ok();
    }
}
