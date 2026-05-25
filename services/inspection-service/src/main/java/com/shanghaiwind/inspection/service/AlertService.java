package com.shanghaiwind.inspection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.inspection.entity.Alert;

/**
 * 预警消息服务
 */
public interface AlertService {

    /**
     * 发送预警
     */
    void sendAlert(Alert alert);

    /**
     * 分页查询用户预警
     */
    IPage<Alert> pageQuery(Integer pageNum, Integer pageSize, Long userId, Boolean unreadOnly);

    /**
     * 标记已读
     */
    void markAsRead(Long id, Long userId);

    /**
     * 全部标记已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取未读数量
     */
    int getUnreadCount(Long userId);
}
