package com.shanghaiwind.inspection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.common.result.R;
import com.shanghaiwind.inspection.dto.ReviewFindingDTO;
import com.shanghaiwind.inspection.entity.AiFinding;
import com.shanghaiwind.inspection.service.AiFindingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI检测发现控制器
 */
@RestController
@RequestMapping("/ai/findings")
@RequiredArgsConstructor
public class AiFindingController {

    private final AiFindingService aiFindingService;

    /**
     * 创建AI发现（供AI服务回调）
     */
    @PostMapping
    public R<Void> createFinding(@RequestBody AiFinding finding) {
        aiFindingService.createFinding(finding);
        return R.ok();
    }

    /**
     * 分页查询AI发现
     */
    @GetMapping
    public R<IPage<AiFinding>> pageQuery(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) Long taskId,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String severity,
                                          @RequestParam(required = false) String status) {
        return R.ok(aiFindingService.pageQuery(pageNum, pageSize, taskId, type, severity, status));
    }

    /**
     * 获取AI发现详情
     */
    @GetMapping("/{id}")
    public R<AiFinding> getFinding(@PathVariable Long id) {
        return R.ok(aiFindingService.getFinding(id));
    }

    /**
     * 审核AI发现
     */
    @PutMapping("/{id}/review")
    public R<Void> reviewFinding(@PathVariable Long id,
                                  @Valid @RequestBody ReviewFindingDTO dto,
                                  @RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId) {
        aiFindingService.reviewFinding(id, dto.getStatus(), userId);
        return R.ok();
    }

    /**
     * 获取任务的发现统计
     */
    @GetMapping("/stats/{taskId}")
    public R<Object> getTaskStats(@PathVariable Long taskId) {
        return R.ok(aiFindingService.getTaskStats(taskId));
    }
}
