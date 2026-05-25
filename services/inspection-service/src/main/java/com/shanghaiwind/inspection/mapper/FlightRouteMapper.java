package com.shanghaiwind.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.inspection.entity.FlightRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 飞行航线Mapper
 */
@Mapper
public interface FlightRouteMapper extends BaseMapper<FlightRoute> {

    /**
     * 根据任务ID查询航线列表
     */
    @Select("SELECT * FROM biz_flight_route WHERE task_id = #{taskId}")
    List<FlightRoute> selectByTaskId(@Param("taskId") Long taskId);
}
