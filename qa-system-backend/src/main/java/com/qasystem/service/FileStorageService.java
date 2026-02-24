package com.qasystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口 - 统一文件上传入口
 *
 * 支持多种存储后端（COS 云存储、本地磁盘），
 * 使用策略模式实现 COS 优先、本地回退。
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file     上传的文件对象
     * @param subDir   存储子目录（如 images/2026/02/24）
     * @param fileName 目标文件名
     * @return 文件访问 URL（COS 完整 URL 或本地相对路径）
     * @throws IOException 上传失败时抛出
     */
    String uploadFile(MultipartFile file, String subDir, String fileName) throws IOException;
}
