package com.shanghaiwind.inspection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.inspection.dto.TaskDTO;
import com.shanghaiwind.inspection.entity.InspectionTask;

/**
 * 任务服务接口
 */
public interface TaskService extends IService<InspectionTask> {

    /**
     * 分页查询任务
     */
    IPage<InspectionTask> pageQuery(Integer pageNum, Integer pageSize, Long projectId, String type, String status);

    /**
     * 创建任务
     */
    InspectionTask create(TaskDTO dto);

    /**
     * 更新任务
     */
    InspectionTask update(Long id, TaskDTO dto);

    /**
     * 删除任务
     */
    void delete(Long id);

    /**
     * 开始任务
     */
    void start(Long id);

    /**
     * 完成任务
     */
    void complete(Long id);

    /**
     * 更新进度
     */
    void updateProgress(Long id, Integer progress);
}
