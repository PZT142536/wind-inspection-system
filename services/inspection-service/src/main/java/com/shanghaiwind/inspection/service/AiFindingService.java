package com.shanghaiwind.inspection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.inspection.entity.AiFinding;

/**
 * AI检测发现服务
 */
public interface AiFindingService {

    /**
     * 创建检测发现
     */
    void createFinding(AiFinding finding);

    /**
     * 分页查询
     */
    IPage<AiFinding> pageQuery(Integer pageNum, Integer pageSize, Long taskId, String type, String severity, String status);

    /**
     * 获取详情
     */
    AiFinding getFinding(Long id);

    /**
     * 审核发现
     */
    void reviewFinding(Long id, String status, Long reviewerId);

    /**
     * 获取任务的发现统计
     */
    Object getTaskStats(Long taskId);
}
