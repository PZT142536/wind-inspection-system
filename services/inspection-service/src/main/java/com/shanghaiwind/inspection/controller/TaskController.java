package com.shanghaiwind.inspection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.common.result.R;
import com.shanghaiwind.inspection.dto.TaskDTO;
import com.shanghaiwind.inspection.entity.InspectionTask;
import com.shanghaiwind.inspection.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理控制器
 */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 分页查询任务
     */
    @GetMapping
    public R<IPage<InspectionTask>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return R.ok(taskService.pageQuery(pageNum, pageSize, projectId, type, status));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public R<InspectionTask> detail(@PathVariable Long id) {
        return R.ok(taskService.getById(id));
    }

    /**
     * 创建任务
     */
    @PostMapping
    public R<InspectionTask> create(@Valid @RequestBody TaskDTO dto) {
        return R.ok(taskService.create(dto));
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public R<InspectionTask> update(@PathVariable Long id, @Valid @RequestBody TaskDTO dto) {
        return R.ok(taskService.update(id, dto));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return R.ok();
    }

    /**
     * 开始任务
     */
    @PutMapping("/{id}/start")
    public R<Void> start(@PathVariable Long id) {
        taskService.start(id);
        return R.ok();
    }

    /**
     * 完成任务
     */
    @PutMapping("/{id}/complete")
    public R<Void> complete(@PathVariable Long id) {
        taskService.complete(id);
        return R.ok();
    }

    /**
     * 更新进度
     */
    @PutMapping("/{id}/progress")
    public R<Void> updateProgress(@PathVariable Long id, @RequestParam Integer progress) {
        taskService.updateProgress(id, progress);
        return R.ok();
    }
}
