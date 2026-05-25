package com.shanghaiwind.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.inspection.entity.InspectionTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 巡检任务Mapper
 */
@Mapper
public interface InspectionTaskMapper extends BaseMapper<InspectionTask> {
}
