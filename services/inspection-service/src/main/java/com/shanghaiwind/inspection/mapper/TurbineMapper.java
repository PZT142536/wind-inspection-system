package com.shanghaiwind.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.inspection.entity.Turbine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 机位Mapper
 */
@Mapper
public interface TurbineMapper extends BaseMapper<Turbine> {

    /**
     * 根据项目ID查询机位列表
     */
    @Select("SELECT * FROM biz_turbine WHERE project_id = #{projectId} ORDER BY code")
    List<Turbine> selectByProjectId(@Param("projectId") Long projectId);
}
