package com.qasystem.service.impl;

import com.qasystem.config.CosConfig;
import com.qasystem.service.FileStorageService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件存储服务实现 - COS 优先，本地回退
 *
 * 策略：
 * 1. 如果 COS 客户端可用 → 尝试上传到 COS 存储桶
 * 2. COS 上传失败 → 回退到本地磁盘存储
 * 3. COS 客户端不可用（未配置） → 直接本地存储
 *
 * 所有操作均在控制台打印详细日志。
 */
@Slf4j
@Service
public class CosFileStorageService implements FileStorageService {

    @Autowired(required = false)
    private COSClient cosClient;

    @Autowired
    private CosConfig cosConfig;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public String uploadFile(MultipartFile file, String subDir, String fileName) throws IOException {
        // COS key: subDir/fileName, 例如 images/2026/02/24/abc123.jpg
        String cosKey = subDir + "/" + fileName;

        // ======== 1. 尝试 COS 上传 ========
        if (cosClient != null) {
            try {
                log.info("☁️ [COS上传] 开始上传到 COS | bucket={} | key={} | size={}bytes",
                        cosConfig.getBucket(), cosKey, file.getSize());

                InputStream inputStream = file.getInputStream();
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());

                PutObjectRequest putRequest = new PutObjectRequest(
                        cosConfig.getBucket(), cosKey, inputStream, metadata);

                PutObjectResult result = cosClient.putObject(putRequest);
                inputStream.close();

                // 构建 COS 访问 URL
                String cosUrl = cosConfig.getHost() + "/" + cosKey;

                log.info("  [COS上传] 上传成功 | url={} | etag={}", cosUrl, result.getETag());
                return cosUrl;

            } catch (Exception e) {
                log.warn("  [COS上传] COS 上传失败，回退到本地存储 | key={} | 原因: {}", cosKey, e.getMessage());
                // 继续执行本地存储逻辑
            }
        } else {
            log.info("  [本地存储] COS 客户端未配置，使用本地存储 | path={}", cosKey);
        }

        // ======== 2. 本地存储（回退方案） ========
        return saveToLocal(file, subDir, fileName);
    }

    /**
     * 保存文件到本地磁盘
     */
    private String saveToLocal(MultipartFile file, String subDir, String fileName) throws IOException {
        Path basePath = Paths.get(uploadPath);
        if (!basePath.isAbsolute()) {
            basePath = Paths.get(System.getProperty("user.dir"), uploadPath);
        }

        Path uploadDir = basePath.resolve(subDir);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("  [本地存储] 创建目录: {}", uploadDir.toAbsolutePath());
        }

        Path filePath = uploadDir.resolve(fileName);
        file.transferTo(filePath.toFile());

        String localUrl = "/uploads/" + subDir + "/" + fileName;
        log.info("  [本地存储] 文件已保存 | path={} | url={}", filePath.toAbsolutePath(), localUrl);
        return localUrl;
    }
}
