package com.shanghaiwind.media.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 媒体文件实体
 */
@Data
@TableName("biz_media_file")
public class MediaFile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 文件类型: VIDEO, PHOTO
     */
    private String type;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * MinIO存储路径
     */
    private String path;

    /**
     * 存储桶
     */
    private String bucket;

    /**
     * 文件大小(字节)
     */
    private Long sizeBytes;

    /**
     * 检查面
     */
    private String surface;

    /**
     * 关键点
     */
    private String keyPoint;

    /**
     * 视频时长(秒)
     */
    private Integer durationSec;

    /**
     * 文件校验码(MD5或SHA-256)
     */
    private String checksum;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 上传者ID
     */
    private Long uploadedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadedAt;
}
