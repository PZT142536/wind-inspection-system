package com.shanghaiwind.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.media.entity.MediaFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 媒体文件Mapper
 */
@Mapper
public interface MediaFileMapper extends BaseMapper<MediaFile> {

    /**
     * 根据任务ID查询媒体文件列表
     */
    @Select("SELECT * FROM biz_media_file WHERE task_id = #{taskId} ORDER BY uploaded_at DESC")
    List<MediaFile> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据任务ID和类型查询媒体文件数量
     */
    @Select("SELECT COUNT(*) FROM biz_media_file WHERE task_id = #{taskId} AND type = #{type}")
    int countByTaskIdAndType(@Param("taskId") Long taskId, @Param("type") String type);
}
