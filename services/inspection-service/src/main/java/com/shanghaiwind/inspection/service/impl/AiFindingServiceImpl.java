package com.shanghaiwind.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.ResultCode;
import com.shanghaiwind.inspection.entity.AiFinding;
import com.shanghaiwind.inspection.mapper.AiFindingMapper;
import com.shanghaiwind.inspection.service.AiFindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI检测发现服务实现
 */
@Service
@RequiredArgsConstructor
public class AiFindingServiceImpl implements AiFindingService {

    private final AiFindingMapper aiFindingMapper;

    @Override
    @Transactional
    public void createFinding(AiFinding finding) {
        if (finding.getStatus() == null) {
            finding.setStatus("PENDING");
        }
        finding.setCreatedAt(LocalDateTime.now());
        aiFindingMapper.insert(finding);
    }

    @Override
    public IPage<AiFinding> pageQuery(Integer pageNum, Integer pageSize, Long taskId,
                                       String type, String severity, String status) {
        LambdaQueryWrapper<AiFinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(taskId != null, AiFinding::getTaskId, taskId);
        wrapper.eq(type != null, AiFinding::getType, type);
        wrapper.eq(severity != null, AiFinding::getSeverity, severity);
        wrapper.eq(status != null, AiFinding::getStatus, status);
        wrapper.orderByDesc(AiFinding::getCreatedAt);
        return aiFindingMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public AiFinding getFinding(Long id) {
        AiFinding finding = aiFindingMapper.selectById(id);
        if (finding == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "AI发现不存在");
        }
        return finding;
    }

    @Override
    @Transactional
    public void reviewFinding(Long id, String status, Long reviewerId) {
        AiFinding finding = getFinding(id);
        if (!"PENDING".equals(finding.getStatus())) {
            throw new BusinessException("该发现已被审核");
        }
        finding.setStatus(status);
        finding.setReviewerId(reviewerId);
        finding.setReviewedAt(LocalDateTime.now());
        aiFindingMapper.updateById(finding);
    }

    @Override
    public Object getTaskStats(Long taskId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", aiFindingMapper.selectCount(
                new LambdaQueryWrapper<AiFinding>().eq(AiFinding::getTaskId, taskId)));
        stats.put("pending", aiFindingMapper.countPendingByTaskId(taskId));
        stats.put("highSeverity", aiFindingMapper.countByTaskIdAndSeverity(taskId, "HIGH"));
        stats.put("mediumSeverity", aiFindingMapper.countByTaskIdAndSeverity(taskId, "MEDIUM"));
        stats.put("lowSeverity", aiFindingMapper.countByTaskIdAndSeverity(taskId, "LOW"));
        return stats;
    }
}
