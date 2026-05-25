package com.shanghaiwind.inspection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.common.result.R;
import com.shanghaiwind.inspection.entity.Alert;
import com.shanghaiwind.inspection.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 预警消息控制器
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * 发送预警（供内部调用）
     */
    @PostMapping
    public R<Void> sendAlert(@RequestBody Alert alert) {
        alertService.sendAlert(alert);
        return R.ok();
    }

    /**
     * 分页查询用户预警
     */
    @GetMapping
    public R<IPage<Alert>> pageQuery(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId,
                                      @RequestParam(required = false) Boolean unreadOnly) {
        return R.ok(alertService.pageQuery(pageNum, pageSize, userId, unreadOnly));
    }

    /**
     * 标记单条预警已读
     */
    @PutMapping("/{id}/read")
    public R<Void> markAsRead(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId) {
        alertService.markAsRead(id, userId);
        return R.ok();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public R<Void> markAllAsRead(@RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId) {
        alertService.markAllAsRead(userId);
        return R.ok();
    }

    /**
     * 获取未读预警数量
     */
    @GetMapping("/unread-count")
    public R<Integer> getUnreadCount(@RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId) {
        return R.ok(alertService.getUnreadCount(userId));
    }
}
