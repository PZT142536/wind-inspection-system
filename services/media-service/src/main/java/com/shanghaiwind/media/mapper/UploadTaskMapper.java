package com.shanghaiwind.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.media.entity.UploadTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 上传任务Mapper
 */
@Mapper
public interface UploadTaskMapper extends BaseMapper<UploadTask> {

    /**
     * 根据文件MD5查询上传任务
     */
    @Select("SELECT * FROM biz_upload_task WHERE file_md5 = #{fileMd5}")
    UploadTask selectByFileMd5(@Param("fileMd5") String fileMd5);
}
