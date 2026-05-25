package com.shanghaiwind.media.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上传分片实体
 */
@Data
@TableName("biz_upload_chunk")
public class UploadChunk {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件唯一标识(MD5)
     */
    private String fileMd5;

    /**
     * 分片序号
     */
    private Integer chunkNumber;

    /**
     * 分片大小(字节)
     */
    private Long chunkSize;

    /**
     * 分片存储路径
     */
    private String chunkPath;

    /**
     * 上传状态: UPLOADING, UPLOADED
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
