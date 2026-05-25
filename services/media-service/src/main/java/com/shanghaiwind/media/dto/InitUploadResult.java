package com.shanghaiwind.media.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 初始化上传结果
 */
@Data
@Builder
public class InitUploadResult {

    /**
     * 是否为新文件(秒传)
     */
    private boolean newFile;

    /**
     * 上传任务ID
     */
    private Long uploadTaskId;

    /**
     * 已上传的分片序号列表(断点续传)
     */
    private List<Integer> uploadedChunks;
}
