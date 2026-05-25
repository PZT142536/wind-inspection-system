package com.shanghaiwind.media.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    /**
     * 检查存储桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("检查存储桶失败: {}", bucketName, e);
            return false;
        }
    }

    /**
     * 创建存储桶
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("创建存储桶成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", bucketName, e);
        }
    }

    /**
     * 上传文件
     */
    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            log.error("上传文件失败: {}", objectName, e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 上传文件流
     */
    public String uploadStream(String bucketName, String objectName, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            log.error("上传文件流失败: {}", objectName, e);
            throw new RuntimeException("上传文件流失败", e);
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件失败: {}", objectName, e);
            throw new RuntimeException("下载文件失败", e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
        }
    }

    /**
     * 获取文件预签名URL
     */
    public String getPresignedUrl(String bucketName, String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}", objectName, e);
            throw new RuntimeException("获取预签名URL失败", e);
        }
    }

    /**
     * 初始化分片上传
     */
    public String initiateMultipartUpload(String bucketName, String objectName) {
        try {
            return minioClient.createMultipartUpload(
                    CreateMultipartUploadArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            ).uploadId();
        } catch (Exception e) {
            log.error("初始化分片上传失败: {}", objectName, e);
            throw new RuntimeException("初始化分片上传失败", e);
        }
    }

    /**
     * 上传分片
     */
    public String uploadPart(String bucketName, String objectName, String uploadId, int partNumber, InputStream data, long size) {
        try {
            return minioClient.uploadPart(
                    UploadPartArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .stream(data, size, -1)
                            .build()
            ).etag();
        } catch (Exception e) {
            log.error("上传分片失败: {} part {}", objectName, partNumber, e);
            throw new RuntimeException("上传分片失败", e);
        }
    }

    /**
     * 合并分片
     */
    public void completeMultipartUpload(String bucketName, String objectName, String uploadId, String[] etags) {
        try {
            Part[] parts = new Part[etags.length];
            for (int i = 0; i < etags.length; i++) {
                parts[i] = new Part(i + 1, etags[i]);
            }
            minioClient.completeMultipartUpload(
                    CompleteMultipartUploadArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .uploadId(uploadId)
                            .parts(parts)
                            .build()
            );
        } catch (Exception e) {
            log.error("合并分片失败: {}", objectName, e);
            throw new RuntimeException("合并分片失败", e);
        }
    }

    /**
     * 取消分片上传
     */
    public void abortMultipartUpload(String bucketName, String objectName, String uploadId) {
        try {
            minioClient.abortMultipartUpload(
                    AbortMultipartUploadArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .uploadId(uploadId)
                            .build()
            );
        } catch (Exception e) {
            log.error("取消分片上传失败: {}", objectName, e);
        }
    }
}
