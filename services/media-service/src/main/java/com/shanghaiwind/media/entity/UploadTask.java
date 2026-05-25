package com.shanghaiwind.media.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上传任务实体
 */
@Data
@TableName("biz_upload_task")
public class UploadTask {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件唯一标识(MD5)
     */
    private String fileMd5;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 分片总数
     */
    private Integer totalChunks;

    /**
     * 已上传分片数
     */
    private Integer uploadedChunks;

    /**
     * 任务ID(关联巡检任务)
     */
    private Long taskId;

    /**
     * 文件类型: VIDEO, PHOTO
     */
    private String fileType;

    /**
     * 上传状态: UPLOADING, MERGING, COMPLETED, FAILED
     */
    private String status;

    /**
     * 最终文件路径
     */
    private String filePath;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
