package com.shanghaiwind.media.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.common.result.R;
import com.shanghaiwind.media.dto.InitUploadDTO;
import com.shanghaiwind.media.dto.InitUploadResult;
import com.shanghaiwind.media.dto.MergeChunksDTO;
import com.shanghaiwind.media.dto.UploadChunkDTO;
import com.shanghaiwind.media.entity.MediaFile;
import com.shanghaiwind.media.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * 初始化上传任务（秒传检测 + 断点续传）
     */
    @PostMapping("/upload/init")
    public R<InitUploadResult> initUpload(@Valid @RequestBody InitUploadDTO dto,
                                          @RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId) {
        return R.ok(uploadService.initUpload(dto, userId));
    }

    /**
     * 上传分片
     */
    @PostMapping("/upload/chunk")
    public R<Void> uploadChunk(@Valid @ModelAttribute UploadChunkDTO dto) {
        uploadService.uploadChunk(dto);
        return R.ok();
    }

    /**
     * 合并分片
     */
    @PostMapping("/upload/merge")
    public R<MediaFile> mergeChunks(@Valid @RequestBody MergeChunksDTO dto,
                                    @RequestHeader(value = "X-User-Id", defaultValue = "0") Long userId) {
        return R.ok(uploadService.mergeChunks(dto, userId));
    }

    /**
     * 获取已上传的分片列表（断点续传）
     */
    @GetMapping("/upload/chunks/{fileMd5}")
    public R<List<Integer>> getUploadedChunks(@PathVariable String fileMd5) {
        return R.ok(uploadService.getUploadedChunks(fileMd5));
    }

    /**
     * 分页查询媒体文件
     */
    @GetMapping("/files")
    public R<IPage<MediaFile>> pageQuery(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) Long taskId,
                                         @RequestParam(required = false) String type) {
        return R.ok(uploadService.pageQuery(pageNum, pageSize, taskId, type));
    }

    /**
     * 获取媒体文件详情
     */
    @GetMapping("/files/{id}")
    public R<MediaFile> getMediaFile(@PathVariable Long id) {
        return R.ok(uploadService.getMediaFile(id));
    }

    /**
     * 删除媒体文件
     */
    @DeleteMapping("/files/{id}")
    public R<Void> deleteMediaFile(@PathVariable Long id) {
        uploadService.deleteMediaFile(id);
        return R.ok();
    }

    /**
     * 获取文件下载URL
     */
    @GetMapping("/files/{id}/download")
    public R<String> getDownloadUrl(@PathVariable Long id) {
        return R.ok(uploadService.getDownloadUrl(id));
    }

    /**
     * 获取文件预览URL
     */
    @GetMapping("/files/{id}/preview")
    public R<String> getPreviewUrl(@PathVariable Long id) {
        return R.ok(uploadService.getPreviewUrl(id));
    }
}
