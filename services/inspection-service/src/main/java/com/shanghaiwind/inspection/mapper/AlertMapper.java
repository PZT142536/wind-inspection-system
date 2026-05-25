package com.shanghaiwind.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.inspection.entity.Alert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 预警消息Mapper
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

    @Select("SELECT COUNT(*) FROM biz_alert WHERE target_user_id = #{userId} AND read_at IS NULL")
    int countUnreadByUserId(@Param("userId") Long userId);
}
