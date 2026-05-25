package com.shanghaiwind.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.ResultCode;
import com.shanghaiwind.inspection.dto.ProjectDTO;
import com.shanghaiwind.inspection.entity.Project;
import com.shanghaiwind.inspection.mapper.ProjectMapper;
import com.shanghaiwind.inspection.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 项目服务实现
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public IPage<Project> pageQuery(Integer pageNum, Integer pageSize, String keyword, String status) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null, Project::getName, keyword)
                .or()
                .like(keyword != null, Project::getCode, keyword);
        wrapper.eq(status != null, Project::getStatus, status);
        wrapper.orderByDesc(Project::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Project create(ProjectDTO dto) {
        // 检查编码是否重复
        if (dto.getCode() != null) {
            LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Project::getCode, dto.getCode());
            if (count(wrapper) > 0) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "项目编码已存在");
            }
        }

        Project project = new Project();
        project.setName(dto.getName());
        project.setCode(dto.getCode());
        project.setOwner(dto.getOwner());
        project.setLocation(dto.getLocation());
        project.setLat(dto.getLat());
        project.setLng(dto.getLng());
        project.setManagerId(dto.getManagerId());
        project.setDescription(dto.getDescription());
        project.setStatus("ACTIVE");
        save(project);
        return project;
    }

    @Override
    @Transactional
    public Project update(Long id, ProjectDTO dto) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }

        project.setName(dto.getName());
        project.setOwner(dto.getOwner());
        project.setLocation(dto.getLocation());
        project.setLat(dto.getLat());
        project.setLng(dto.getLng());
        project.setManagerId(dto.getManagerId());
        project.setDescription(dto.getDescription());
        updateById(project);
        return project;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        removeById(id);
    }
}
