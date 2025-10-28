# 师生答疑系统重构执行文档 - Phase 2-5

## Phase 2: 个人中心 + COS存储 (Sprint 3-4)

### 2.1 个人中心模块开发

#### 2.1.1 后端API设计

**个人中心相关API：**

| Method | Path | 描述 | 角色 |
|--------|------|------|------|
| GET | `/api/v1/profile/me` | 获取当前用户信息 | ALL |
| PUT | `/api/v1/profile/me` | 更新个人资料 | ALL |
| PUT | `/api/v1/profile/password` | 修改密码 | ALL |
| POST | `/api/v1/profile/avatar` | 上传头像 | ALL |
| GET | `/api/v1/profile/stats` | 获取个人统计数据 | ALL |
| GET | `/api/v1/profile/questions` | 我的提问列表 | STUDENT |
| GET | `/api/v1/profile/answers` | 我的回答列表 | TEACHER |
| GET | `/api/v1/profile/collections` | 我的收藏列表 | ALL |
| GET | `/api/v1/profile/follows` | 我关注的教师 | STUDENT |

#### 2.1.2 Redis缓存策略

```java
/**
 * 用户信息缓存
 * Key: user:info:{userId}
 * TTL: 30分钟
 * 更新策略: 更新资料时主动删除
 */
public UserInfoResponse getUserInfo(Long userId) {
    String cacheKey = RedisKeyConstant.USER_INFO + userId;
    
    // 1. 尝试从Redis获取
    UserInfoResponse cached = redisUtil.get(cacheKey, UserInfoResponse.class);
    if (cached != null) {
        return cached;
    }
    
    // 2. 从数据库查询
    UserInfoResponse userInfo = userMapper.selectUserInfoById(userId);
    
    // 3. 写入Redis缓存
    redisUtil.set(cacheKey, userInfo, 30, TimeUnit.MINUTES);
    
    return userInfo;
}
```

---

### 2.2 腾讯云COS集成

#### 2.2.1 COS配置类

**CosConfig.java:**

```java
package com.qasystem.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cos")
public class CosConfig {
    
    private String secretId;
    private String secretKey;
    private String region;
    private String bucketName;
    private String cdnDomain;
    
    @Bean
    public COSClient cosClient() {
        // 初始化用户身份信息
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        
        // 设置bucket的区域
        Region regionObj = new Region(region);
        ClientConfig clientConfig = new ClientConfig(regionObj);
        
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
}
```

#### 2.2.2 COS服务类

**CosService.java:**

```java
package com.qasystem.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qasystem.common.exception.BusinessException;
import com.qasystem.config.CosConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class CosService {

    private final COSClient cosClient;
    private final CosConfig cosConfig;

    /**
     * 上传文件到COS
     * 
     * @param file 文件
     * @param folder 文件夹路径 (如: avatar, question, answer)
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        // 1. 验证文件
        validateFile(file);
        
        // 2. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        String dateFolder = DateUtil.format(new Date(), "yyyy/MM/dd");
        String fileName = String.format("%s/%s/%s.%s", 
            folder, dateFolder, IdUtil.simpleUUID(), extension);
        
        try {
            // 3. 上传到COS
            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                cosConfig.getBucketName(), 
                fileName, 
                inputStream, 
                metadata
            );
            
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            log.info("文件上传成功: {}, ETag: {}", fileName, result.getETag());
            
            // 4. 返回CDN URL
            return cosConfig.getCdnDomain() + "/" + fileName;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 删除COS文件
     */
    public void deleteFile(String fileUrl) {
        try {
            // 从URL提取文件路径
            String filePath = fileUrl.replace(cosConfig.getCdnDomain() + "/", "");
            cosClient.deleteObject(cosConfig.getBucketName(), filePath);
            log.info("文件删除成功: {}", filePath);
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 验证文件大小（10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过10MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只支持上传图片文件");
        }
    }
}
```

#### 2.2.3 文件上传接口

**FileController.java:**

```java
package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.service.CosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {

    private final CosService cosService;

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadFile(file, "avatar");
        return Result.success(url);
    }

    @Operation(summary = "上传问题图片")
    @PostMapping("/question")
    public Result<String> uploadQuestionImage(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadFile(file, "question");
        return Result.success(url);
    }

    @Operation(summary = "上传回答图片")
    @PostMapping("/answer")
    public Result<String> uploadAnswerImage(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadFile(file, "answer");
        return Result.success(url);
    }

    @Operation(summary = "上传富文本图片")
    @PostMapping("/editor")
    public Result<String> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadFile(file, "editor");
        return Result.success(url);
    }
}
```

---

### 2.3 前端Vue 3项目搭建

#### 2.3.1 初始化前端项目

**使用Vite创建项目：**

```bash
# 创建Vue 3 + TypeScript项目
npm create vite@latest qa-system-frontend -- --template vue-ts

cd qa-system-frontend

# 安装依赖
npm install

# 安装核心依赖
npm install vue-router@4 pinia axios element-plus @element-plus/icons-vue
npm install @vueuse/core dayjs lodash-es

# 安装开发依赖
npm install -D @types/lodash-es sass unplugin-vue-components unplugin-auto-import
```

#### 2.3.2 项目结构

```
qa-system-frontend/
├── public/
├── src/
│   ├── api/                    # API接口
│   │   ├── auth.ts            # 认证接口
│   │   ├── user.ts            # 用户接口
│   │   ├── question.ts        # 问题接口
│   │   └── request.ts         # Axios封装
│   ├── assets/                # 静态资源
│   │   ├── styles/            # 全局样式
│   │   │   ├── index.scss
│   │   │   ├── variables.scss
│   │   │   └── common.scss
│   │   └── images/
│   ├── components/            # 公共组件
│   │   ├── common/
│   │   │   ├── AppHeader.vue      # 顶部导航
│   │   │   ├── AppFooter.vue      # 底部
│   │   │   ├── BreadCrumb.vue     # 面包屑
│   │   │   └── Loading.vue        # 加载组件
│   │   ├── form/
│   │   │   └── CosUpload.vue      # COS上传组件
│   │   └── editor/
│   │       └── RichEditor.vue     # 富文本编辑器
│   ├── layouts/               # 布局组件
│   │   ├── AdminLayout.vue        # 管理端布局
│   │   ├── UserLayout.vue         # 用户端布局
│   │   └── BlankLayout.vue        # 空白布局
│   ├── router/                # 路由配置
│   │   ├── index.ts
│   │   ├── routes.ts
│   │   └── guards.ts              # 路由守卫
│   ├── stores/                # Pinia状态管理
│   │   ├── index.ts
│   │   ├── user.ts                # 用户状态
│   │   ├── app.ts                 # 应用状态
│   │   └── permission.ts          # 权限状态
│   ├── types/                 # TypeScript类型
│   │   ├── api.d.ts
│   │   ├── user.d.ts
│   │   └── question.d.ts
│   ├── utils/                 # 工具函数
│   │   ├── request.ts             # HTTP请求
│   │   ├── storage.ts             # 本地存储
│   │   ├── auth.ts                # 认证工具
│   │   └── validate.ts            # 表单验证
│   ├── views/                 # 页面视图
│   │   ├── auth/
│   │   │   ├── Login.vue          # 登录页
│   │   │   └── Register.vue       # 注册页
│   │   ├── admin/                 # 管理端
│   │   │   ├── Dashboard.vue
│   │   │   ├── UserManage.vue
│   │   │   └── QuestionManage.vue
│   │   ├── student/               # 学生端
│   │   │   ├── Home.vue
│   │   │   ├── QuestionList.vue
│   │   │   ├── QuestionDetail.vue
│   │   │   ├── AskQuestion.vue
│   │   │   └── Profile.vue
│   │   └── teacher/               # 教师端
│   │       ├── Home.vue
│   │       ├── AnswerCenter.vue
│   │       └── Profile.vue
│   ├── App.vue
│   └── main.ts
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

#### 2.3.3 Vite配置

**vite.config.ts:**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 自动导入
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/types/auto-imports.d.ts'
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/types/components.d.ts'
    })
  ],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia']
        }
      }
    }
  }
})
```

#### 2.3.4 Axios封装

**src/utils/request.ts:**

```typescript
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// Loading实例
let loadingInstance: any = null
let requestCount = 0

// 显示Loading
const showLoading = () => {
  if (requestCount === 0) {
    loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
  requestCount++
}

// 隐藏Loading
const hideLoading = () => {
  requestCount--
  if (requestCount === 0 && loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

// 请求拦截器
service.interceptors.request.use(
  (config: any) => {
    // 显示Loading
    if (!config.hideLoading) {
      showLoading()
    }
    
    // 添加Token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    hideLoading()
    ElMessage.error('请求错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    hideLoading()
    
    const res = response.data
    
    // 如果返回的状态码不是200，说明接口异常
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // Token过期或无效
      if (res.code === 401) {
        ElMessage.warning('登录已过期，请重新登录')
        removeToken()
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res.data
  },
  (error) => {
    hideLoading()
    
    console.error('请求错误：', error)
    
    let message = '网络错误'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          removeToken()
          router.push('/login')
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
```

---

## Phase 3: 核心问答模块 (Sprint 5-6)

### 3.1 数据库表设计

```sql
-- ==========================================
-- 3. 问答核心表
-- ==========================================

-- 3.1 科目类型表
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '科目名称',
  `description` TEXT COMMENT '科目描述',
  `icon` VARCHAR(200) COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目类型表';

-- 3.2 学生问题表
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
  `content` TEXT NOT NULL COMMENT '问题内容(富文本)',
  `cover_image` VARCHAR(500) COMMENT '封面图片',
  `subject_id` BIGINT NOT NULL COMMENT '科目ID',
  `subject_name` VARCHAR(100) COMMENT '科目名称(冗余)',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `student_name` VARCHAR(100) COMMENT '学生姓名(冗余)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待回答 1-已回答 2-已关闭',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `collect_count` INT DEFAULT 0 COMMENT '收藏数',
  `answer_count` INT DEFAULT 0 COMMENT '回答数',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
  `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门',
  `publish_time` DATETIME COMMENT '发布时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_status` (`status`),
  KEY `idx_hot` (`is_hot`, `view_count`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生问题表';

-- 3.3 教师回答表
DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_id` BIGINT NOT NULL COMMENT '问题ID',
  `content` TEXT NOT NULL COMMENT '回答内容(富文本)',
  `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
  `teacher_name` VARCHAR(100) COMMENT '教师姓名(冗余)',
  `is_accepted` TINYINT DEFAULT 0 COMMENT '是否被采纳',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_accepted` (`is_accepted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师回答表';

-- 3.4 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ref_type` VARCHAR(20) NOT NULL COMMENT '关联类型: question/answer',
  `ref_id` BIGINT NOT NULL COMMENT '关联ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(100) COMMENT '用户名',
  `user_role` VARCHAR(20) COMMENT '用户角色',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID',
  `reply_to_id` BIGINT COMMENT '回复的评论ID',
  `reply_to_name` VARCHAR(100) COMMENT '回复的用户名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_ref` (`ref_type`, `ref_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

### 3.2 Redis缓存设计

```
# 问题相关
question:detail:{id}           # 问题详情，TTL: 30分钟
question:list:page:{n}         # 问题列表分页，TTL: 5分钟
question:hot                   # 热门问题列表，TTL: 10分钟
question:view:{id}:{userId}    # 用户浏览记录，TTL: 1天

# 科目相关
subject:list                   # 科目列表，TTL: 1小时
subject:questions:{id}         # 科目下的问题，TTL: 10分钟

# 统计相关
stats:question:view:{id}       # 问题浏览计数器
stats:answer:like:{id}         # 回答点赞计数器
```

### 3.3 核心API接口

#### 问题模块API

| Method | Path | 描述 | 角色 |
|--------|------|------|------|
| GET | `/api/v1/subjects` | 获取科目列表 | ALL |
| POST | `/api/v1/admin/subjects` | 创建科目 | ADMIN |
| PUT | `/api/v1/admin/subjects/{id}` | 更新科目 | ADMIN |
| DELETE | `/api/v1/admin/subjects/{id}` | 删除科目 | ADMIN |
| POST | `/api/v1/questions` | 发布问题 | STUDENT |
| GET | `/api/v1/questions` | 获取问题列表 | ALL |
| GET | `/api/v1/questions/{id}` | 获取问题详情 | ALL |
| PUT | `/api/v1/questions/{id}` | 更新问题 | STUDENT |
| DELETE | `/api/v1/questions/{id}` | 删除问题 | STUDENT/ADMIN |
| POST | `/api/v1/questions/{id}/like` | 点赞问题 | ALL |
| POST | `/api/v1/questions/{id}/collect` | 收藏问题 | ALL |
| POST | `/api/v1/questions/{id}/answer` | 回答问题 | TEACHER |
| GET | `/api/v1/questions/{id}/answers` | 获取回答列表 | ALL |
| PUT | `/api/v1/answers/{id}` | 更新回答 | TEACHER |
| DELETE | `/api/v1/answers/{id}` | 删除回答 | TEACHER/ADMIN |
| POST | `/api/v1/answers/{id}/accept` | 采纳回答 | STUDENT |

---

## Phase 4: 社交互动功能 (Sprint 7-8)

### 4.1 数据库表设计

```sql
-- ==========================================
-- 4. 社交互动表
-- ==========================================

-- 4.1 关注表
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id` BIGINT NOT NULL COMMENT '关注者ID(学生)',
  `followee_id` BIGINT NOT NULL COMMENT '被关注者ID(教师)',
  `followee_name` VARCHAR(100) COMMENT '教师姓名',
  `followee_avatar` VARCHAR(500) COMMENT '教师头像',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`, `followee_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_followee` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 4.2 收藏表
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `ref_type` VARCHAR(20) NOT NULL COMMENT '收藏类型: question/answer',
  `ref_id` BIGINT NOT NULL COMMENT '收藏对象ID',
  `ref_title` VARCHAR(200) COMMENT '标题',
  `ref_cover` VARCHAR(500) COMMENT '封面',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection` (`user_id`, `ref_type`, `ref_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 4.3 交流区帖子表
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) COMMENT '帖子标题',
  `content` TEXT NOT NULL COMMENT '帖子内容',
  `user_id` BIGINT NOT NULL COMMENT '发帖人ID',
  `user_name` VARCHAR(100) COMMENT '发帖人姓名',
  `user_role` VARCHAR(20) COMMENT '发帖人角色',
  `view_count` INT DEFAULT 0 COMMENT '浏览数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
  `is_hot` TINYINT DEFAULT 0 COMMENT '是否热帖',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-关闭 1-开放',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_hot` (`is_hot`, `view_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交流区帖子表';
```

### 4.2 社交API接口

| Method | Path | 描述 | 角色 |
|--------|------|------|------|
| POST | `/api/v1/follows/teacher/{id}` | 关注教师 | STUDENT |
| DELETE | `/api/v1/follows/teacher/{id}` | 取消关注 | STUDENT |
| GET | `/api/v1/follows/teachers` | 我关注的教师 | STUDENT |
| GET | `/api/v1/follows/students` | 关注我的学生 | TEACHER |
| GET | `/api/v1/forum/posts` | 获取帖子列表 | ALL |
| POST | `/api/v1/forum/posts` | 发布帖子 | ALL |
| GET | `/api/v1/forum/posts/{id}` | 获取帖子详情 | ALL |
| PUT | `/api/v1/forum/posts/{id}` | 更新帖子 | OWNER |
| DELETE | `/api/v1/forum/posts/{id}` | 删除帖子 | OWNER/ADMIN |
| POST | `/api/v1/forum/posts/{id}/comment` | 评论帖子 | ALL |

---

## Phase 5: 容器化部署与监控 (Sprint 9)

### 5.1 Docker配置

**后端Dockerfile:**

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

**前端Dockerfile:**

```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 5.2 Docker Compose配置

**docker-compose.yml:**

```yaml
version: '3.8'

services:
  # MySQL
  mysql:
    image: mysql:8.0
    container_name: qa-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: qa_system_v2
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - qa-network

  # Redis
  redis:
    image: redis:7-alpine
    container_name: qa-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - qa-network

  # 后端
  backend:
    build: ./qa-system-backend
    container_name: qa-backend
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: qa_system_v2
      DB_USERNAME: root
      DB_PASSWORD: root123
      REDIS_HOST: redis
      REDIS_PORT: 6379
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    networks:
      - qa-network

  # 前端
  frontend:
    build: ./qa-system-frontend
    container_name: qa-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - qa-network

networks:
  qa-network:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
```

### 5.3 Nginx配置

```nginx
server {
    listen 80;
    server_name localhost;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 文件上传大小限制
    client_max_body_size 50M;
}
```

---

## ✅ 完整验收标准

### Phase 2 完成标准
- [ ] 个人中心页面完整展示用户信息
- [ ] 头像上传成功并显示COS URL
- [ ] Redis缓存用户信息生效
- [ ] 修改资料后缓存正确失效

### Phase 3 完成标准
- [ ] 学生可以成功发布问题（含图片）
- [ ] 教师可以查看并回答问题
- [ ] 问题详情页正确显示所有信息
- [ ] Redis缓存热点问题数据
- [ ] 点赞、收藏功能正常

### Phase 4 完成标准
- [ ] 学生可以关注/取关教师
- [ ] Redis Set正确存储关注关系
- [ ] 交流区可以发帖和评论
- [ ] 热帖列表正确排序

### Phase 5 完成标准
- [ ] docker-compose up成功启动所有服务
- [ ] Nginx正确代理前后端请求
- [ ] Prometheus端点可访问
- [ ] 日志JSON格式输出正确
- [ ] 生产环境配置文件完整

---

**完整实现代码请参考各Phase详细文档**

