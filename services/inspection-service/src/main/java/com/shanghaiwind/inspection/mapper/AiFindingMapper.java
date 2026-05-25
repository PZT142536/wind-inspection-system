package com.shanghaiwind.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.inspection.entity.AiFinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI检测发现Mapper
 */
@Mapper
public interface AiFindingMapper extends BaseMapper<AiFinding> {

    @Select("SELECT COUNT(*) FROM biz_ai_finding WHERE task_id = #{taskId} AND severity = #{severity}")
    int countByTaskIdAndSeverity(@Param("taskId") Long taskId, @Param("severity") String severity);

    @Select("SELECT COUNT(*) FROM biz_ai_finding WHERE task_id = #{taskId} AND status = 'PENDING'")
    int countPendingByTaskId(@Param("taskId") Long taskId);
}
