package com.shanghaiwind.media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 合并分片DTO
 */
@Data
public class MergeChunksDTO {

    @NotBlank(message = "文件MD5不能为空")
    private String fileMd5;

    private String surface;

    private String keyPoint;
}
