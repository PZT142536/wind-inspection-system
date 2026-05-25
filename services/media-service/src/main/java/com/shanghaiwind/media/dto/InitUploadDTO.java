package com.shanghaiwind.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 初始化上传DTO
 */
@Data
public class InitUploadDTO {

    @NotBlank(message = "文件MD5不能为空")
    private String fileMd5;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    @NotNull(message = "分片总数不能为空")
    private Integer totalChunks;

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotBlank(message = "文件类型不能为空")
    private String fileType;
}
