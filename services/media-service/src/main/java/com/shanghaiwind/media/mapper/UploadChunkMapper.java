package com.shanghaiwind.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.media.entity.UploadChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 上传分片Mapper
 */
@Mapper
public interface UploadChunkMapper extends BaseMapper<UploadChunk> {

    /**
     * 根据文件MD5查询已上传的分片列表
     */
    @Select("SELECT chunk_number FROM biz_upload_chunk WHERE file_md5 = #{fileMd5} AND status = 'UPLOADED'")
    List<Integer> selectUploadedChunkNumbers(@Param("fileMd5") String fileMd5);
}
