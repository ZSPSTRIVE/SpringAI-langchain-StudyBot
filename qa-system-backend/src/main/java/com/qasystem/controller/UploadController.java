package com.qasystem.controller;

import com.qasystem.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 📤 文件上传控制器 - 处理文件上传相关请求
 * 
 * 📖 功能说明：
 * 本控制器提供文件上传的API接口，支持图片文件的上传、
 * 存储和访问。文件上传是师生答疑系统的重要功能，用于
 * 支持用户头像、文档附件、问题图片等文件的上传需求。
 * 
 * 🎯 主要功能：
 * 1. 图片上传 - 支持常见图片格式的上传
 * 2. 文件验证 - 验证文件类型、大小和安全性
 * 3. 文件存储 - 按日期分组存储文件
 * 4. 文件命名 - 使用UUID生成唯一文件名
 * 5. URL生成 - 生成可访问的文件URL
 * 6. 错误处理 - 提供详细的错误信息
 * 
 * 🔧 技术实现：
 * - 基于Spring MVC框架，提供RESTful API接口
 * - 使用MultipartFile处理文件上传
 * - 支持配置文件自定义上传路径和文件大小限制
 * - 使用UUID确保文件名唯一性，避免文件冲突
 * - 按日期分组存储，便于管理和备份
 * - 实现文件类型和大小验证，确保系统安全
 * 
 * 📋 API设计：
 * - 遵循RESTful设计原则，使用标准HTTP方法
 * - 统一返回格式，使用ApiResponse包装响应数据
 * - 支持CORS跨域访问，方便前端调用
 * - 提供详细的错误信息，便于前端处理异常
 * 
 * 🔄 工作流程：
 * 1. 前端选择文件 → 发送上传请求
 * 2. 后端接收文件 → 验证文件类型和大小
 * 3. 生成唯一文件名 → 创建存储目录
 * 4. 保存文件到磁盘 → 生成访问URL
 * 5. 返回上传结果 → 前端显示上传状态
 * 
 * ⚠️ 注意事项：
 * - 文件上传有大小限制，默认5MB
 * - 只支持特定格式的图片文件
 * - 上传路径可通过配置文件自定义
 * - 文件存储在本地文件系统，生产环境建议使用云存储
 * - 文件上传操作会被记录日志，用于审计追踪
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录操作日志
@RestController  // 标识为RESTful控制器，自动处理JSON序列化
@RequestMapping("/api/v1/upload")  // 设置基础路径为/api/v1/upload
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
@CrossOrigin(origins = "*")  // 允许所有源的跨域请求
public class UploadController {

    /**
     * 📁 上传路径配置 - 文件存储的根目录
     * 
     * 从配置文件中读取上传路径，默认值为"uploads"。
     * 可以是绝对路径或相对路径，相对路径将相对于
     * 项目根目录解析。
     */
    @Value("${upload.path:uploads}")
    private String uploadPath;

    /**
     * 📏 文件大小限制配置 - 单个文件的最大大小
     * 
     * 从配置文件中读取文件大小限制，默认值为5MB（5242880字节）。
     * 超过此大小的文件将被拒绝上传，以防止系统资源耗尽。
     */
    @Value("${upload.max-size:5242880}") // 5MB
    private long maxFileSize;

    /**
     * 📍 获取绝对上传路径
     * 
     * 📖 功能说明：
     * 将配置的上传路径转换为绝对路径，确保文件存储位置明确。
     * 如果配置的是相对路径，则将其转换为相对于项目根目录的绝对路径。
     * 
     * 🔧 技术实现：
     * - 使用Paths.get()创建Path对象
     * - 检查路径是否为绝对路径
     * - 如果是相对路径，使用System.getProperty("user.dir")获取项目根目录
     * - 返回绝对路径对象，用于后续文件操作
     * 
     * 🔄 返回结果：
     * @return 绝对路径对象，表示文件存储的根目录
     * 
     * ⚠️ 注意事项：
     * - 确保返回的路径有足够的磁盘空间
     * - 确保应用程序有该路径的读写权限
     * - 生产环境建议使用专用存储目录
     */
    private Path getAbsoluteUploadPath() {
        // 创建路径对象
        Path path = Paths.get(uploadPath);
        
        // 检查是否为绝对路径
        if (!path.isAbsolute()) {
            // 如果是相对路径，转换为项目根目录下的绝对路径
            path = Paths.get(System.getProperty("user.dir"), uploadPath);
        }
        
        return path;
    }

    /**
     * 📸 上传图片
     * 
     * 📖 功能说明：
     * 处理图片文件的上传请求，支持常见图片格式（JPG、PNG、GIF、WebP等）。
     * 上传成功后返回文件的访问URL和其他相关信息。
     * 
     * 🔧 技术实现：
     * - 使用MultipartFile接收上传的文件
     * - 验证文件是否为空、大小是否超限、类型是否为图片
     * - 检查文件扩展名是否在允许列表中
     * - 使用UUID生成唯一文件名，避免文件冲突
     * - 按日期创建子目录，便于管理和备份
     * - 将文件保存到指定路径，并生成访问URL
     * - 使用try-catch处理IO异常，确保系统稳定性
     * 
     * 📋 请求参数：
     * @param file 上传的图片文件，通过multipart/form-data提交
     * 
     * 🔄 返回结果：
     * @return 统一响应格式，包含：
     *         - code: 状态码，200表示成功，其他值表示失败
     *         - message: 消息内容，描述操作结果
     *         - data: 文件信息，包含：
     *           - url: 文件访问URL
     *           - name: 保存的文件名
     *           - originalName: 原始文件名
     *           - size: 文件大小（字节）
     * 
     * 📝 使用示例：
     * 前端调用示例：
     * ```javascript
     * function uploadImage(file) {
     *   const formData = new FormData();
     *   formData.append('file', file);
     *   
     *   // 显示上传进度
     *   const progressBar = document.getElementById('upload-progress');
     *   progressBar.style.display = 'block';
     *   
     *   fetch('/api/v1/upload/image', {
     *     method: 'POST',
     *     body: formData
     *   })
     *   .then(response => response.json())
     *   .then(data => {
     *     progressBar.style.display = 'none';
     *     
     *     if (data.code === 200) {
     *       console.log('上传成功:', data.data);
     *       const imageUrl = data.data.url;
     *       
     *       // 显示上传的图片
     *       const imgElement = document.createElement('img');
     *       imgElement.src = imageUrl;
     *       imgElement.alt = data.data.originalName;
     *       document.getElementById('image-preview').appendChild(imgElement);
     *       
     *       // 保存图片URL到表单字段
     *       document.getElementById('image-url').value = imageUrl;
     *       
     *       alert('图片上传成功！');
     *     } else {
     *       console.error('上传失败:', data.message);
     *       alert('上传失败: ' + data.message);
     *     }
     *   })
     *   .catch(error => {
     *     progressBar.style.display = 'none';
     *     console.error('上传失败:', error);
     *     alert('上传失败，请重试');
     *   });
     * }
     * 
     * // 文件选择事件
     * document.getElementById('file-input').addEventListener('change', function(e) {
     *   const file = e.target.files[0];
     *   if (file) {
     *     // 验证文件大小（前端验证）
     *     if (file.size > 5 * 1024 * 1024) {
     *       alert('文件大小不能超过5MB');
     *       return;
     *     }
     *     
     *     // 验证文件类型（前端验证）
     *     if (!file.type.startsWith('image/')) {
     *       alert('只能上传图片文件');
     *       return;
     *     }
     *     
     *     // 显示预览
     *     const reader = new FileReader();
     *     reader.onload = function(e) {
     *       document.getElementById('preview').src = e.target.result;
     *     };
     *     reader.readAsDataURL(file);
     *     
     *     // 上传文件
     *     uploadImage(file);
     *   }
     * });
     * ```
     * 
     * ⚠️ 注意事项：
     * - 文件大小不能超过配置的最大值（默认5MB）
     * - 只支持图片格式的文件（JPG、JPEG、PNG、GIF、WebP）
     * - 文件名不能为空
     * - 上传路径必须有足够的磁盘空间和写入权限
     * - 生产环境建议使用云存储服务，如阿里云OSS、腾讯云COS等
     * - 文件上传操作会被记录日志，用于审计追踪
     */
    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // 记录上传请求日志
        log.info("📸 接收到图片上传请求: {}", file.getOriginalFilename());
        
        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.error("请选择要上传的文件");
            }

            // 验证文件大小
            if (file.getSize() > maxFileSize) {
                return ApiResponse.error("文件大小不能超过5MB");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("只能上传图片文件");
            }

            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ApiResponse.error("文件名不能为空");
            }
            
            // 提取文件扩展名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 定义允许的文件扩展名
            String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
            boolean isAllowed = false;
            for (String ext : allowedExtensions) {
                if (fileExtension.equalsIgnoreCase(ext)) {
                    isAllowed = true;
                    break;
                }
            }
            
            // 检查文件扩展名是否被允许
            if (!isAllowed) {
                return ApiResponse.error("不支持的图片格式");
            }

            // 生成新的文件名
            String newFileName = generateFileName(fileExtension);
            
            // 创建上传目录（按日期分组）
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            Path uploadDir = getAbsoluteUploadPath().resolve("images").resolve(datePath);
            
            // 确保目录存在
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("📁 创建上传目录: {}", uploadDir.toAbsolutePath());
            }

            // 保存文件
            Path filePath = uploadDir.resolve(newFileName);
            file.transferTo(filePath.toFile());

            // 生成访问URL（实际项目中应该配置域名）
            String fileUrl = "/uploads/images/" + datePath + "/" + newFileName;
            
            // 构建返回数据
            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("name", newFileName);
            data.put("originalName", originalFilename);
            data.put("size", String.valueOf(file.getSize()));
            
            // 记录上传成功日志
            log.info("✅ 图片上传成功: {}", fileUrl);
            return ApiResponse.success(data, "上传成功");
            
        } catch (IOException e) {
            // 记录上传失败日志
            log.error("❌ 图片上传失败:", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 🏷️ 生成唯一的文件名
     * 
     * 📖 功能说明：
     * 使用UUID生成唯一的文件名，避免文件名冲突。
     * 保留原始文件的扩展名，确保文件类型不变。
     * 
     * 🔧 技术实现：
     * - 使用UUID.randomUUID()生成随机UUID
     * - 移除UUID中的连字符，简化文件名
     * - 添加原始文件的扩展名
     * - 返回完整的唯一文件名
     * 
     * 📋 请求参数：
     * @param extension 文件扩展名，包含点号（如".jpg"）
     * 
     * 🔄 返回结果：
     * @return 唯一的文件名，格式为"UUID.扩展名"
     * 
     * 📝 使用示例：
     * ```java
     * // 生成唯一文件名
     * String uniqueFileName = generateFileName(".jpg");
     * // 结果可能如: "a1b2c3d4e5f6789012345678901234ab.jpg"
     * 
     * // 保存文件
     * Path filePath = uploadDir.resolve(uniqueFileName);
     * file.transferTo(filePath.toFile());
     * ```
     * 
     * ⚠️ 注意事项：
     * - 扩展名参数应包含点号（如".jpg"而非"jpg"）
     * - UUID生成是随机的，无法预测文件名
     * - 生成的文件名不包含路径信息，只有文件名本身
     * - 虽然UUID冲突概率极低，但在极高并发场景下仍需考虑
     */
    private String generateFileName(String extension) {
        // 生成UUID并移除连字符，然后添加扩展名
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}