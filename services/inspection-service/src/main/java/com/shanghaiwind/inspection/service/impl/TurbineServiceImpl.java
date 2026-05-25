package com.shanghaiwind.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.ResultCode;
import com.shanghaiwind.inspection.dto.TurbineDTO;
import com.shanghaiwind.inspection.entity.Turbine;
import com.shanghaiwind.inspection.mapper.TurbineMapper;
import com.shanghaiwind.inspection.service.TurbineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 机位服务实现
 */
@Service
public class TurbineServiceImpl extends ServiceImpl<TurbineMapper, Turbine> implements TurbineService {

    @Override
    public List<Turbine> getByProjectId(Long projectId) {
        return baseMapper.selectByProjectId(projectId);
    }

    @Override
    @Transactional
    public Turbine create(TurbineDTO dto) {
        // 检查同一项目下机位编号是否重复
        LambdaQueryWrapper<Turbine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Turbine::getProjectId, dto.getProjectId())
                .eq(Turbine::getCode, dto.getCode());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "机位编号已存在");
        }

        Turbine turbine = new Turbine();
        turbine.setProjectId(dto.getProjectId());
        turbine.setCode(dto.getCode());
        turbine.setModel(dto.getModel());
        turbine.setLat(dto.getLat());
        turbine.setLng(dto.getLng());
        turbine.setAltitude(dto.getAltitude());
        turbine.setHubHeight(dto.getHubHeight());
        turbine.setStatus("PENDING");
        save(turbine);
        return turbine;
    }

    @Override
    @Transactional
    public Turbine update(Long id, TurbineDTO dto) {
        Turbine turbine = getById(id);
        if (turbine == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "机位不存在");
        }

        turbine.setModel(dto.getModel());
        turbine.setLat(dto.getLat());
        turbine.setLng(dto.getLng());
        turbine.setAltitude(dto.getAltitude());
        turbine.setHubHeight(dto.getHubHeight());
        updateById(turbine);
        return turbine;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Turbine turbine = getById(id);
        if (turbine == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "机位不存在");
        }
        removeById(id);
    }
}
