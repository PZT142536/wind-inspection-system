package com.shanghaiwind.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传分片DTO
 */
@Data
public class UploadChunkDTO {

    @NotBlank(message = "文件MD5不能为空")
    private String fileMd5;

    @NotNull(message = "分片序号不能为空")
    private Integer chunkNumber;

    @NotNull(message = "分片文件不能为空")
    private MultipartFile chunkFile;
}
