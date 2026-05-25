package com.shanghaiwind.inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.inspection.dto.TurbineDTO;
import com.shanghaiwind.inspection.entity.Turbine;

import java.util.List;

/**
 * 机位服务接口
 */
public interface TurbineService extends IService<Turbine> {

    /**
     * 根据项目ID查询机位列表
     */
    List<Turbine> getByProjectId(Long projectId);

    /**
     * 创建机位
     */
    Turbine create(TurbineDTO dto);

    /**
     * 更新机位
     */
    Turbine update(Long id, TurbineDTO dto);

    /**
     * 删除机位
     */
    void delete(Long id);
}
