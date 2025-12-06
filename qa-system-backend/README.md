# 🎓 师生答疑系统 v2.0 - 后端服务

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-6DB33F.svg?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00.svg?logo=openjdk&logoColor=white)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-blue.svg)
![Redis](https://img.shields.io/badge/Redis-6.0+-DC382D.svg?logo=redis&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql&logoColor=white)
![LangChain4j](https://img.shields.io/badge/LangChain4j-0.35.0-FF6B35.svg)
![WebSocket](https://img.shields.io/badge/WebSocket-Enabled-010101.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker&logoColor=white)

> 🚀 一个现代化的师生互动答疑平台后端服务，集成AI智能助手、文档查重降重、实时问答和交流论坛功能。

---

## 📑 目录

- [功能特性](#-功能特性)
- [技术架构](#-技术架构)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [API文档](#-api文档)
- [配置说明](#-配置说明)
- [部署指南](#-部署指南)
- [数据库设计](#-数据库设计)

---

## ✨ 功能特性

### 🎯 核心功能模块

| 模块 | 功能描述 | 状态 |
|------|----------|------|
| **用户认证** | JWT Token认证、角色权限管理、安全登录注册 | ✅ |
| **问答系统** | 学生提问、教师解答、问题分类、采纳回答 | ✅ |
| **交流论坛** | 帖子发布、评论回复、点赞收藏、关注用户 | ✅ |
| **AI助手** | 智能对话、多轮会话、流式响应、对话历史 | ✅ |
| **文档工作台** | 文档上传、查重检测、AI降重改写、敏感词过滤 | ✅ |
| **后台管理** | 用户管理、内容审核、数据统计、系统配置 | ✅ |

### 🤖 AI智能功能

- **多模型支持** - 支持硅基流动(SiliconFlow)、通义千问等多种AI模型
- **流式对话** - 基于SSE实时推送，打字机效果输出
- **上下文记忆** - 多轮对话保持上下文连贯性
- **智能降重** - AI辅助文档内容改写降重
- **敏感词检测** - 自动检测并过滤敏感内容

### 📄 文档处理功能

- **文档解析** - 支持 `.docx` Word文档解析
- **相似度检测** - 基于段落级别的内容查重
- **AI降重改写** - WebSocket流式输出改写结果
- **版本管理** - 保存改写历史，支持回溯
- **操作日志** - 完整记录文档操作轨迹

---

## � 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 3.1.5 | 核心框架 |
| **Spring Security** | 6.1.5 | 安全认证框架 |
| **Spring WebSocket** | - | 实时通信支持 |
| **MyBatis-Plus** | 3.5.7 | ORM增强框架 |
| **JWT (jjwt)** | 0.12.3 | Token认证 |
| **Redis** | 6.0+ | 缓存、会话管理 |
| **MySQL** | 8.0+ | 关系型数据库 |
| **LangChain4j** | 0.35.0 | AI模型集成框架 |
| **Apache POI** | 5.2.5 | Word文档解析 |
| **Hutool** | 5.8.25 | Java工具库 |
| **Lombok** | - | 代码简化 |
| **Micrometer** | - | 应用监控指标 |

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端应用 (Vue 3)                          │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP / WebSocket
┌─────────────────────────────▼───────────────────────────────────┐
│                     Spring Boot 应用层                           │
│  ┌──────────────┬──────────────┬──────────────┬──────────────┐  │
│  │ AuthController│ QuestionCtrl │  ForumCtrl   │  DocController│  │
│  │ AdminController│ AnswerCtrl  │  AiAssistant │  UploadCtrl  │  │
│  └──────────────┴──────────────┴──────────────┴──────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐│
│  │                      Service 业务层                          ││
│  │  AuthService | QuestionService | ForumService | DocService  ││
│  │  AdminService | AnswerService | AiAssistantService          ││
│  └──────────────────────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────────────────────┐│
│  │                      Mapper 数据层                           ││
│  │              MyBatis-Plus + MySQL 8.0                        ││
│  └──────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
         │                    │                    │
    ┌────▼────┐         ┌─────▼─────┐        ┌────▼────┐
    │  MySQL  │         │   Redis   │        │ AI API  │
    │ 数据存储 │         │ 缓存/会话  │        │ 硅基流动 │
    └─────────┘         └───────────┘        └─────────┘
```

---

## 📁 项目结构

```
qa-system-backend/
├── 📂 src/main/java/com/qasystem/
│   ├── 📄 QaSystemApplication.java      # 应用启动类
│   │
│   ├── 📂 controller/                   # 控制器层 (16个)
│   │   ├── AuthController.java          # 认证：登录/注册/登出
│   │   ├── AdminController.java         # 管理：用户/数据统计
│   │   ├── QuestionController.java      # 问题：发布/查询/管理
│   │   ├── AnswerController.java        # 回答：提交/采纳/评价
│   │   ├── ForumController.java         # 论坛：帖子/评论/互动
│   │   ├── AiAssistantController.java   # AI：对话/历史/反馈
│   │   ├── DocController.java           # 文档：上传/查重/降重
│   │   ├── DocAdminController.java      # 文档管理：配置/审核
│   │   ├── DocSensitiveWordAdminController.java  # 敏感词管理
│   │   ├── DocOperationLogAdminController.java   # 操作日志
│   │   ├── CollectionController.java    # 收藏：问题/帖子收藏
│   │   ├── FollowController.java        # 关注：用户关注/粉丝
│   │   ├── ProfileController.java       # 个人中心：资料/设置
│   │   ├── SubjectController.java       # 科目：分类管理
│   │   └── UploadController.java        # 上传：图片/文件
│   │
│   ├── 📂 service/                      # 服务层 (15个实现)
│   │   ├── impl/                        # 服务实现类
│   │   ├── AuthService.java
│   │   ├── AiAssistantService.java      # AI核心服务
│   │   ├── AiModelConfigService.java    # AI模型配置
│   │   ├── DocService.java              # 文档处理服务
│   │   ├── DocContentFilterService.java # 内容过滤服务
│   │   ├── DocSensitiveWordService.java # 敏感词服务
│   │   └── ...
│   │
│   ├── 📂 entity/                       # 实体类 (17个)
│   │   ├── User.java                    # 用户基础表
│   │   ├── Student.java                 # 学生信息
│   │   ├── Teacher.java                 # 教师信息
│   │   ├── Question.java                # 问题
│   │   ├── Answer.java                  # 回答
│   │   ├── Forum.java                   # 论坛帖子
│   │   ├── Subject.java                 # 科目
│   │   ├── AiConversation.java          # AI对话记录
│   │   ├── AiModelConfig.java           # AI模型配置
│   │   ├── DocDocument.java             # 文档信息
│   │   ├── DocParagraph.java            # 文档段落
│   │   ├── DocRewriteVersion.java       # 改写版本
│   │   ├── DocSensitiveWord.java        # 敏感词
│   │   ├── DocConfig.java               # 文档配置
│   │   ├── DocOperationLog.java         # 操作日志
│   │   ├── Follow.java                  # 关注关系
│   │   └── UserCollection.java          # 收藏记录
│   │
│   ├── 📂 dto/                          # 数据传输对象 (19个)
│   ├── 📂 mapper/                       # MyBatis映射 (18个)
│   ├── 📂 config/                       # 配置类 (6个)
│   │   ├── SecurityConfig.java          # Spring Security配置
│   │   ├── WebSocketConfig.java         # WebSocket配置
│   │   ├── RedisConfig.java             # Redis配置
│   │   ├── CorsConfig.java              # 跨域配置
│   │   └── MyBatisPlusConfig.java       # ORM配置
│   │
│   ├── 📂 security/                     # 安全模块 (3个)
│   │   ├── JwtTokenProvider.java        # JWT工具类
│   │   ├── JwtAuthenticationFilter.java # JWT过滤器
│   │   └── CustomUserDetailsService.java
│   │
│   ├── 📂 websocket/                    # WebSocket处理
│   │   └── DocRewriteWebSocketHandler.java  # 降重流式输出
│   │
│   ├── 📂 common/                       # 公共模块
│   │   └── ApiResponse.java             # 统一响应格式
│   │
│   └── 📂 exception/                    # 异常处理
│       └── GlobalExceptionHandler.java
│
├── 📂 src/main/resources/
│   ├── application.yml                  # 主配置文件
│   └── application-dev.yml              # 开发环境配置
│
├── 📂 db/migrations/                    # 数据库迁移脚本
├── 📄 Dockerfile                        # Docker构建文件
├── 📄 pom.xml                           # Maven依赖配置
└── 📄 README.md                         # 项目文档
```

---

## � 快速开始

### 环境要求

| 环境 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 17+ | 必须使用Java 17或更高版本 |
| **Maven** | 3.8+ | 项目构建工具 |
| **MySQL** | 8.0+ | 数据库 |
| **Redis** | 6.0+ | 缓存服务 |

### 本地开发

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/qa-system.git
cd qa-system/qa-system-backend

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE qa_system_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 导入数据库脚本
mysql -u root -p qa_system_v2 < db/migrations/init.sql

# 4. 修改配置文件 (可选，使用环境变量更佳)
# 编辑 src/main/resources/application.yml

# 5. 启动应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/qa-system-backend-2.0.0.jar
```

### 环境变量配置

```bash
# 数据库配置
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=qa_system_v2
export DB_USERNAME=root
export DB_PASSWORD=your_password

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=

# JWT配置
export JWT_SECRET=your-very-long-and-secure-secret-key

# AI服务配置 (硅基流动)
export SILICONFLOW_API_KEY=sk-your-api-key
```

### Docker 部署

```bash
# 构建镜像
docker build -t qa-system-backend:2.0.0 .

# 运行容器
docker run -d \
  --name qa-backend \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD=your_password \
  -e REDIS_HOST=host.docker.internal \
  -e SILICONFLOW_API_KEY=sk-your-key \
  qa-system-backend:2.0.0
```

---

## 📝 API文档

### 接口概览

| 模块 | 基础路径 | 描述 |
|------|----------|------|
| 认证 | `/api/v1/auth` | 登录、注册、登出 |
| 问题 | `/api/v1/questions` | 问题CRUD操作 |
| 回答 | `/api/v1/answers` | 回答CRUD操作 |
| 论坛 | `/forum` | 帖子、评论、互动 |
| AI助手 | `/api/ai` | 智能对话服务 |
| 文档 | `/api/v1/doc` | 文档处理服务 |
| 收藏 | `/api/v1/collections` | 收藏管理 |
| 关注 | `/api/v1/follows` | 关注关系 |
| 科目 | `/api/v1/subjects` | 科目管理 |
| 上传 | `/api/v1/upload` | 文件上传 |
| 个人 | `/api/v1/profile` | 个人中心 |
| 管理 | `/api/v1/admin` | 后台管理 |

### 认证接口

```http
POST   /api/v1/auth/login           # 用户登录
POST   /api/v1/auth/register        # 用户注册
POST   /api/v1/auth/logout          # 退出登录
GET    /api/v1/auth/me              # 获取当前用户信息
```

### 问答接口

```http
GET    /api/v1/questions            # 获取问题列表 (支持分页、筛选)
POST   /api/v1/questions            # 发布新问题
GET    /api/v1/questions/{id}       # 获取问题详情
PUT    /api/v1/questions/{id}       # 更新问题
DELETE /api/v1/questions/{id}       # 删除问题

GET    /api/v1/answers              # 获取回答列表
POST   /api/v1/answers              # 提交回答
PUT    /api/v1/answers/{id}/accept  # 采纳回答
```

### AI助手接口

```http
POST   /api/ai/chat                 # 发送对话消息 (SSE流式响应)
GET    /api/ai/sessions             # 获取会话列表
POST   /api/ai/sessions             # 创建新会话
GET    /api/ai/sessions/{id}/history    # 获取会话历史
DELETE /api/ai/sessions/{id}        # 删除会话
POST   /api/ai/messages/{id}/bookmark   # 收藏消息
POST   /api/ai/messages/{id}/feedback   # 消息反馈
```

### 文档工作台接口

```http
POST   /api/v1/doc/upload           # 上传文档
GET    /api/v1/doc/list             # 获取文档列表
GET    /api/v1/doc/{id}             # 获取文档详情
GET    /api/v1/doc/{id}/paragraphs  # 获取文档段落
POST   /api/v1/doc/{id}/check       # 执行查重检测
POST   /api/v1/doc/paragraph/{id}/rewrite  # AI降重改写 (WebSocket)
GET    /api/v1/doc/{id}/versions    # 获取改写版本历史
```

### 管理接口

```http
GET    /api/v1/admin/dashboard      # 数据统计面板
GET    /api/v1/admin/students       # 学生列表管理
GET    /api/v1/admin/teachers       # 教师列表管理
PUT    /api/v1/admin/users/{id}/status  # 更新用户状态
GET    /api/v1/admin/doc/config     # 查重配置管理
PUT    /api/v1/admin/doc/config     # 更新查重配置
GET    /api/v1/admin/doc/sensitive-words  # 敏感词管理
```

---

## ⚙️ 配置说明

### 主配置文件 `application.yml`

```yaml
spring:
  application:
    name: qa-service
  profiles:
    active: dev

  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:qa_system_v2}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  # Redis配置
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

  # 文件上传限制
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 604800000         # 7天
  refresh-expiration: 2592000000 # 30天

# LangChain4j AI配置 (硅基流动)
langchain4j:
  open-ai:
    api-key: ${SILICONFLOW_API_KEY}
    base-url: https://api.siliconflow.cn/v1
    model-name: Qwen/Qwen2.5-7B-Instruct
    temperature: 0.7
    timeout: 60s
    max-tokens: 2000

# 腾讯云COS配置 (可选)
cos:
  client:
    secretId: your-secret-id
    secretKey: your-secret-key
    region: ap-beijing
    bucket: your-bucket-name

# 监控端点
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

## 🚢 部署指南

### 生产环境部署

#### 1. 构建应用

```bash
mvn clean package -DskipTests -Pprod
```

#### 2. 服务器部署

```bash
# 创建应用目录
mkdir -p /opt/qa-system
cd /opt/qa-system

# 上传jar包
scp target/qa-system-backend-2.0.0.jar server:/opt/qa-system/

# 创建启动脚本
cat > start.sh << 'EOF'
#!/bin/bash
nohup java -Xms512m -Xmx1024m \
  -jar qa-system-backend-2.0.0.jar \
  --spring.profiles.active=prod \
  > logs/app.log 2>&1 &
EOF

chmod +x start.sh
./start.sh
```

#### 3. Nginx反向代理

```nginx
upstream qa-backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name api.your-domain.com;

    # API代理
    location /api {
        proxy_pass http://qa-backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # WebSocket代理 (文档降重)
    location /ws {
        proxy_pass http://qa-backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # SSE代理 (AI对话)
    location /api/ai/chat {
        proxy_pass http://qa-backend;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_buffering off;
        proxy_cache off;
    }
}
```

#### 4. Systemd服务

```ini
# /etc/systemd/system/qa-system.service
[Unit]
Description=QA System Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/qa-system
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar qa-system-backend-2.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

---

## � 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `user` | 用户表 | id, username, password, role, status |
| `student` | 学生信息 | id, user_id, student_no, college, major, grade |
| `teacher` | 教师信息 | id, user_id, teacher_no, title, research |
| `question` | 问题表 | id, user_id, subject_id, title, content, status |
| `answer` | 回答表 | id, question_id, user_id, content, is_accepted |
| `subject` | 科目表 | id, name, description |
| `forum` | 论坛帖子 | id, user_id, title, content, likes, views |
| `follow` | 关注关系 | id, follower_id, following_id |
| `user_collection` | 收藏表 | id, user_id, target_id, target_type |
| `ai_conversation` | AI对话 | id, user_id, session_id, role, content |
| `ai_model_config` | AI模型配置 | id, model_name, api_key, is_active |
| `doc_document` | 文档表 | id, user_id, filename, status |
| `doc_paragraph` | 文档段落 | id, doc_id, content, similarity_rate |
| `doc_rewrite_version` | 改写版本 | id, paragraph_id, content, version |
| `doc_sensitive_word` | 敏感词 | id, word, category, level |
| `doc_config` | 文档配置 | id, similarity_threshold, rewrite_model |
| `doc_operation_log` | 操作日志 | id, user_id, doc_id, operation, details |

---

## 👥 用户角色与权限

| 角色 | 权限范围 |
|------|----------|
| **STUDENT** | 提问、评论、收藏、关注、使用AI助手、文档工作台 |
| **TEACHER** | 全部学生权限 + 回答问题、管理自己的回答 |
| **ADMIN** | 全部权限 + 用户管理、内容审核、系统配置 |

---

## 📈 监控与健康检查

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# Prometheus指标
curl http://localhost:8080/actuator/prometheus
```

---

## 🙏 技术致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 核心框架
- [LangChain4j](https://docs.langchain4j.dev/) - AI集成框架
- [MyBatis-Plus](https://baomidou.com/) - ORM增强
- [硅基流动](https://siliconflow.cn/) - AI模型服务
- [Hutool](https://hutool.cn/) - Java工具库

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议

---

<p align="center">
  <b>⭐ 如果这个项目对你有帮助，请给一个Star支持！</b>
</p>
