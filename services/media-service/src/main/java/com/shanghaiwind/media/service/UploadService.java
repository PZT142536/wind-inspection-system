package com.shanghaiwind.media.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.media.dto.*;
import com.shanghaiwind.media.entity.MediaFile;

import java.util.List;

/**
 * 上传服务接口
 */
public interface UploadService {

    /**
     * 初始化上传任务
     */
    InitUploadResult initUpload(InitUploadDTO dto, Long userId);

    /**
     * 上传分片
     */
    void uploadChunk(UploadChunkDTO dto);

    /**
     * 合并分片
     */
    MediaFile mergeChunks(MergeChunksDTO dto, Long userId);

    /**
     * 获取已上传的分片列表
     */
    List<Integer> getUploadedChunks(String fileMd5);

    /**
     * 查询媒体文件列表
     */
    IPage<MediaFile> pageQuery(Integer pageNum, Integer pageSize, Long taskId, String type);

    /**
     * 获取媒体文件详情
     */
    MediaFile getMediaFile(Long id);

    /**
     * 删除媒体文件
     */
    void deleteMediaFile(Long id);

    /**
     * 获取文件下载URL
     */
    String getDownloadUrl(Long id);

    /**
     * 获取文件预览URL
     */
    String getPreviewUrl(Long id);
}
