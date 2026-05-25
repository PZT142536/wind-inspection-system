package com.shanghaiwind.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.ResultCode;
import com.shanghaiwind.inspection.dto.TaskDTO;
import com.shanghaiwind.inspection.entity.InspectionTask;
import com.shanghaiwind.inspection.mapper.InspectionTaskMapper;
import com.shanghaiwind.inspection.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务服务实现
 */
@Service
public class TaskServiceImpl extends ServiceImpl<InspectionTaskMapper, InspectionTask> implements TaskService {

    @Override
    public IPage<InspectionTask> pageQuery(Integer pageNum, Integer pageSize, Long projectId, String type, String status) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(projectId != null, InspectionTask::getProjectId, projectId);
        wrapper.eq(type != null, InspectionTask::getType, type);
        wrapper.eq(status != null, InspectionTask::getStatus, status);
        wrapper.orderByDesc(InspectionTask::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public InspectionTask create(TaskDTO dto) {
        InspectionTask task = new InspectionTask();
        task.setProjectId(dto.getProjectId());
        task.setTurbineId(dto.getTurbineId());
        task.setType(dto.getType());
        task.setComponent(dto.getComponent());
        task.setInspectorId(dto.getInspectorId());
        task.setPlannedAt(dto.getPlannedAt());
        task.setRemark(dto.getRemark());
        task.setStatus("PENDING");
        task.setProgress(0);
        save(task);
        return task;
    }

    @Override
    @Transactional
    public InspectionTask update(Long id, TaskDTO dto) {
        InspectionTask task = getById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException("只能修改待执行状态的任务");
        }

        task.setTurbineId(dto.getTurbineId());
        task.setComponent(dto.getComponent());
        task.setInspectorId(dto.getInspectorId());
        task.setPlannedAt(dto.getPlannedAt());
        task.setRemark(dto.getRemark());
        updateById(task);
        return task;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        InspectionTask task = getById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        if ("IN_PROGRESS".equals(task.getStatus())) {
            throw new BusinessException("不能删除执行中的任务");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void start(Long id) {
        InspectionTask task = getById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException("只能开始待执行状态的任务");
        }

        task.setStatus("IN_PROGRESS");
        task.setStartedAt(LocalDateTime.now());
        updateById(task);
    }

    @Override
    @Transactional
    public void complete(Long id) {
        InspectionTask task = getById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new BusinessException("只能完成执行中的任务");
        }

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setProgress(100);
        updateById(task);
    }

    @Override
    @Transactional
    public void updateProgress(Long id, Integer progress) {
        InspectionTask task = getById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new BusinessException("只能更新执行中任务的进度");
        }

        task.setProgress(progress);
        updateById(task);
    }
}
