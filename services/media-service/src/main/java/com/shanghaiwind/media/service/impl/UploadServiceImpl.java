package com.shanghaiwind.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.ResultCode;
import com.shanghaiwind.media.config.MinioConfig;
import com.shanghaiwind.media.dto.*;
import com.shanghaiwind.media.entity.MediaFile;
import com.shanghaiwind.media.entity.UploadChunk;
import com.shanghaiwind.media.entity.UploadTask;
import com.shanghaiwind.media.mapper.MediaFileMapper;
import com.shanghaiwind.media.mapper.UploadChunkMapper;
import com.shanghaiwind.media.mapper.UploadTaskMapper;
import com.shanghaiwind.media.service.MinioService;
import com.shanghaiwind.media.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 上传服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final MinioService minioService;
    private final MinioConfig minioConfig;
    private final UploadTaskMapper uploadTaskMapper;
    private final UploadChunkMapper uploadChunkMapper;
    private final MediaFileMapper mediaFileMapper;

    private static final String CHUNK_TEMP_DIR = "temp/chunks/";

    @Override
    @Transactional
    public InitUploadResult initUpload(InitUploadDTO dto, Long userId) {
        // 检查文件是否已存在(秒传)
        LambdaQueryWrapper<MediaFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaFile::getChecksum, dto.getFileMd5());
        MediaFile existingFile = mediaFileMapper.selectOne(wrapper);
        if (existingFile != null) {
            return InitUploadResult.builder()
                    .newFile(false)
                    .uploadTaskId(null)
                    .uploadedChunks(null)
                    .build();
        }

        // 查询是否已有上传任务
        UploadTask existingTask = uploadTaskMapper.selectByFileMd5(dto.getFileMd5());
        if (existingTask != null) {
            // 断点续传：返回已上传的分片列表
            List<Integer> uploadedChunks = uploadChunkMapper.selectUploadedChunkNumbers(dto.getFileMd5());
            return InitUploadResult.builder()
                    .newFile(true)
                    .uploadTaskId(existingTask.getId())
                    .uploadedChunks(uploadedChunks)
                    .build();
        }

        // 创建新的上传任务
        UploadTask task = new UploadTask();
        task.setFileMd5(dto.getFileMd5());
        task.setFileName(dto.getFileName());
        task.setFileSize(dto.getFileSize());
        task.setTotalChunks(dto.getTotalChunks());
        task.setUploadedChunks(0);
        task.setTaskId(dto.getTaskId());
        task.setFileType(dto.getFileType());
        task.setStatus("UPLOADING");
        uploadTaskMapper.insert(task);

        return InitUploadResult.builder()
                .newFile(true)
                .uploadTaskId(task.getId())
                .uploadedChunks(new ArrayList<>())
                .build();
    }

    @Override
    @Transactional
    public void uploadChunk(UploadChunkDTO dto) {
        try {
            // 保存分片到临时目录
            String chunkPath = CHUNK_TEMP_DIR + dto.getFileMd5() + "/" + dto.getChunkNumber();
            File chunkFile = new File(chunkPath);
            chunkFile.getParentFile().mkdirs();
            dto.getChunkFile().transferTo(chunkFile);

            // 记录分片信息
            UploadChunk chunk = new UploadChunk();
            chunk.setFileMd5(dto.getFileMd5());
            chunk.setChunkNumber(dto.getChunkNumber());
            chunk.setChunkSize(dto.getChunkFile().getSize());
            chunk.setChunkPath(chunkPath);
            chunk.setStatus("UPLOADED");
            uploadChunkMapper.insert(chunk);

            // 更新已上传分片数
            UploadTask task = uploadTaskMapper.selectByFileMd5(dto.getFileMd5());
            if (task != null) {
                task.setUploadedChunks(task.getUploadedChunks() + 1);
                uploadTaskMapper.updateById(task);
            }

            log.info("分片上传成功: {} chunk {}", dto.getFileMd5(), dto.getChunkNumber());
        } catch (IOException e) {
            log.error("保存分片失败", e);
            throw new BusinessException("分片上传失败");
        }
    }

    @Override
    @Transactional
    public MediaFile mergeChunks(MergeChunksDTO dto, Long userId) {
        UploadTask task = uploadTaskMapper.selectByFileMd5(dto.getFileMd5());
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "上传任务不存在");
        }

        // 更新任务状态为合并中
        task.setStatus("MERGING");
        uploadTaskMapper.updateById(task);

        try {
            // 获取所有分片并合并
            String objectName = buildObjectName(task);
            File mergedFile = mergeChunkFiles(dto.getFileMd5(), task.getTotalChunks());

            // 上传合并后的文件到MinIO
            try (FileInputStream fis = new FileInputStream(mergedFile)) {
                String contentType = getContentType(task.getFileName());
                minioService.uploadStream(minioConfig.getBucketName(), objectName, fis, mergedFile.length(), contentType);
            }

            // 创建媒体文件记录
            MediaFile mediaFile = new MediaFile();
            mediaFile.setTaskId(task.getTaskId());
            mediaFile.setType(task.getFileType());
            mediaFile.setName(task.getFileName());
            mediaFile.setOriginalName(task.getFileName());
            mediaFile.setPath(objectName);
            mediaFile.setBucket(minioConfig.getBucketName());
            mediaFile.setSizeBytes(task.getFileSize());
            mediaFile.setSurface(dto.getSurface());
            mediaFile.setKeyPoint(dto.getKeyPoint());
            mediaFile.setChecksum(dto.getFileMd5());
            mediaFile.setUploadedBy(userId);
            mediaFileMapper.insert(mediaFile);

            // 更新上传任务状态
            task.setStatus("COMPLETED");
            task.setFilePath(objectName);
            uploadTaskMapper.updateById(task);

            // 清理临时文件
            cleanTempFiles(dto.getFileMd5(), task.getTotalChunks());

            log.info("文件合并完成: {} -> {}", dto.getFileMd5(), objectName);
            return mediaFile;

        } catch (Exception e) {
            task.setStatus("FAILED");
            uploadTaskMapper.updateById(task);
            log.error("文件合并失败", e);
            throw new BusinessException("文件合并失败");
        }
    }

    @Override
    public List<Integer> getUploadedChunks(String fileMd5) {
        return uploadChunkMapper.selectUploadedChunkNumbers(fileMd5);
    }

    @Override
    public IPage<MediaFile> pageQuery(Integer pageNum, Integer pageSize, Long taskId, String type) {
        LambdaQueryWrapper<MediaFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(taskId != null, MediaFile::getTaskId, taskId);
        wrapper.eq(type != null, MediaFile::getType, type);
        wrapper.orderByDesc(MediaFile::getUploadedAt);
        return mediaFileMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public MediaFile getMediaFile(Long id) {
        MediaFile file = mediaFileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "媒体文件不存在");
        }
        return file;
    }

    @Override
    @Transactional
    public void deleteMediaFile(Long id) {
        MediaFile file = getMediaFile(id);
        // 从MinIO删除文件
        minioService.deleteFile(file.getBucket(), file.getPath());
        // 删除数据库记录
        mediaFileMapper.deleteById(id);
    }

    @Override
    public String getDownloadUrl(Long id) {
        MediaFile file = getMediaFile(id);
        return minioService.getPresignedUrl(file.getBucket(), file.getPath(), 60);
    }

    @Override
    public String getPreviewUrl(Long id) {
        MediaFile file = getMediaFile(id);
        return minioService.getPresignedUrl(file.getBucket(), file.getPath(), 30);
    }

    /**
     * 构建对象存储路径
     */
    private String buildObjectName(UploadTask task) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = getFileExtension(task.getFileName());
        return String.format("media/%s/%s/%s%s",
                dateStr,
                task.getTaskId(),
                task.getFileMd5(),
                ext);
    }

    /**
     * 合并分片文件
     */
    private File mergeChunkFiles(String fileMd5, int totalChunks) throws IOException {
        String mergedPath = CHUNK_TEMP_DIR + fileMd5 + "/merged";
        File mergedFile = new File(mergedPath);

        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            for (int i = 1; i <= totalChunks; i++) {
                String chunkPath = CHUNK_TEMP_DIR + fileMd5 + "/" + i;
                File chunkFile = new File(chunkPath);
                if (!chunkFile.exists()) {
                    throw new BusinessException("分片文件不存在: " + i);
                }
                try (FileInputStream fis = new FileInputStream(chunkFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }

        return mergedFile;
    }

    /**
     * 清理临时文件
     */
    private void cleanTempFiles(String fileMd5, int totalChunks) {
        for (int i = 1; i <= totalChunks; i++) {
            File chunkFile = new File(CHUNK_TEMP_DIR + fileMd5 + "/" + i);
            if (chunkFile.exists()) {
                chunkFile.delete();
            }
        }
        File mergedFile = new File(CHUNK_TEMP_DIR + fileMd5 + "/merged");
        if (mergedFile.exists()) {
            mergedFile.delete();
        }
        File dir = new File(CHUNK_TEMP_DIR + fileMd5);
        if (dir.exists()) {
            dir.delete();
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot) : "";
    }

    private String getContentType(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        switch (ext) {
            case ".mp4": return "video/mp4";
            case ".avi": return "video/x-msvideo";
            case ".mov": return "video/quicktime";
            case ".jpg":
            case ".jpeg": return "image/jpeg";
            case ".png": return "image/png";
            default: return "application/octet-stream";
        }
    }
}
