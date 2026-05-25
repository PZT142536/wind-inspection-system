package com.shanghaiwind.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanghaiwind.inspection.entity.Alert;
import com.shanghaiwind.inspection.mapper.AlertMapper;
import com.shanghaiwind.inspection.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 预警消息服务实现
 */
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertMapper alertMapper;

    @Override
    @Transactional
    public void sendAlert(Alert alert) {
        alert.setSentAt(LocalDateTime.now());
        alert.setCreatedAt(LocalDateTime.now());
        alertMapper.insert(alert);
    }

    @Override
    public IPage<Alert> pageQuery(Integer pageNum, Integer pageSize, Long userId, Boolean unreadOnly) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getTargetUserId, userId);
        if (Boolean.TRUE.equals(unreadOnly)) {
            wrapper.isNull(Alert::getReadAt);
        }
        wrapper.orderByDesc(Alert::getSentAt);
        return alertMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        Alert alert = alertMapper.selectById(id);
        if (alert != null && alert.getTargetUserId().equals(userId) && alert.getReadAt() == null) {
            alert.setReadAt(LocalDateTime.now());
            alertMapper.updateById(alert);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        Alert update = new Alert();
        update.setReadAt(LocalDateTime.now());

        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getTargetUserId, userId);
        wrapper.isNull(Alert::getReadAt);

        alertMapper.update(update, wrapper);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return alertMapper.countUnreadByUserId(userId);
    }
}
