package com.shanghaiwind.inspection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.inspection.dto.ProjectDTO;
import com.shanghaiwind.inspection.entity.Project;

/**
 * 项目服务接口
 */
public interface ProjectService extends IService<Project> {

    /**
     * 分页查询项目
     */
    IPage<Project> pageQuery(Integer pageNum, Integer pageSize, String keyword, String status);

    /**
     * 创建项目
     */
    Project create(ProjectDTO dto);

    /**
     * 更新项目
     */
    Project update(Long id, ProjectDTO dto);

    /**
     * 删除项目
     */
    void delete(Long id);
}
