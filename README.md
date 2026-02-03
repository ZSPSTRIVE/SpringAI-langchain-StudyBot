<p align="center">
  <img src="pictures/logo.svg" alt="师生答疑系统" width="120" />
</p>

<h1 align="center">🎓 师生答疑系统 (Student-Teacher Q&A System)</h1>

<p align="center">
  <strong>一站式智能教学交互平台 | AI驱动的现代化答疑解决方案</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen?style=flat-square&logo=spring-boot" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Vue.js-3.4-42b883?style=flat-square&logo=vue.js" alt="Vue.js" />
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk" alt="Java" />
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql" alt="MySQL" />
  <img src="https://img.shields.io/badge/Redis-6.0%2B-red?style=flat-square&logo=redis" alt="Redis" />
  <img src="https://img.shields.io/badge/LangChain4j-0.35-purple?style=flat-square" alt="LangChain4j" />
</p>

<p align="center">
  <a href="#-功能特性">功能特性</a> •
  <a href="#-技术栈">技术栈</a> •
  <a href="#-快速开始">快速开始</a> •
  <a href="#-项目截图">项目截图</a> •
  <a href="#-项目结构">项目结构</a>
</p>

---

## 📖 项目简介

**师生答疑系统**是一款面向高校/教育机构的教学交互平台，集成**问答管理**、**AI 学习助手**、**文档查重 & AI 降重**、**即时通讯**与**交流区/论坛**等能力，帮助师生更高效地完成「提问 - 解答 - 讨论 - 复盘」。

### 🎯 核心亮点

-   **AI 智能助手** - 基于 LangChain4j + 大语言模型，提供智能答疑、学习辅导
- 💬 **实时通讯** - WebSocket 支持的即时聊天，支持私聊、群聊、图片/视频消息
- 📝 **文档查重 & AI 降重** - Word 文档解析、段落查重分析，WebSocket 流式降重体验
- 🏛️ **交流广场** - 类知乎的论坛系统，支持帖子、评论、点赞、收藏
- 📊 **后台管理** - 完善的管理后台，数据统计一目了然

---

## ✨ 功能特性

### 👨‍🎓 学生端

| 功能模块 | 描述 |
|---------|------|
| 📚 问题广场 | 浏览所有问题，按科目/状态筛选 |
| ✏️ 我要提问 | 发布问题，支持富文本、代码高亮 |
| ⭐ 我的关注 | 关注的教师、收藏的问题 |
| 🤖 AI 助手 | 智能学习助手，即时解答疑问 |
| 📄 文档工作台 | AI 文档查重与智能降重改写 |
| 💬 即时通讯 | 私聊教师、加入群聊交流 |
| 🏛️ 交流广场 | 发帖讨论、知识分享 |

### 👨‍🏫 教师端

| 功能模块 | 描述 |
|---------|------|
| 📋 答疑中心 | 查看待回答问题，快速响应 |
| 📊 问题广场 | 浏览全部问题动态 |
| 🤖 AI 助手 | 辅助备课、生成答案参考 |
| 📄 文档工作台 | 论文批阅辅助、查重检测 |
| 💬 即时通讯 | 与学生私聊、管理群组 |
| 🏛️ 交流广场 | 发布学术讨论、课程通知 |

### 🛠️ 管理后台

| 功能模块 | 描述 |
|---------|------|
| 📈 数据统计 | 用户数、问题数、回答率等可视化 |
| 👥 学生管理 | 学生账号管理、信息维护 |
| 👨‍🏫 教师管理 | 教师信息管理、权限配置 |
| 📚 科目管理 | 学科分类管理 |
| ❓ 问题管理 | 问题审核、违规处理 |
| 📄 文档审核 | 敏感词管理、文档记录 |
| 🏛️ 论坛管理 | 帖子审核、评论管理 |

---

## 🖼️ 项目截图

### 首页
> 系统首页展示核心能力入口（问答、AI、文档工作台、交流区等）

![首页](pictures/1home.png)

### 问题广场
> 支持按科目、状态、关键词进行筛选/搜索，并可在页面内唤起 AI 助手

![问题广场](pictures/1homeexhibition1.png)

### AI 学习助手（独立页面）
> 支持多轮对话，会话列表、历史记录与收藏

![AI 学习助手](pictures/1AI.png)

### AI 助手（页面内嵌）
> 在业务页面中以弹窗形式快速调用 AI 辅助学习

![AI 助手内嵌](pictures/1sAI.png)

### 管理端 - 数据统计
> 统计面板、快捷入口与核心数据概览

![管理端数据统计](pictures/1admin.png)

### 个人中心
> 支持头像上传与资料维护

![个人中心](pictures/1personalsetting.png)

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.1.5 | 核心框架 |
| Spring Security | 6.x | 安全认证 |
| Spring WebSocket | - | 即时通讯、文档降重流式输出 |
| MyBatis-Plus | 3.5.7 | ORM框架 |
| MySQL | 8.0 | 数据存储 |
| Redis | 6.0+ | 缓存/会话 |
| JWT | 0.12.3 | Token认证 |
| LangChain4j | 0.35.0 | AI集成框架 |
| Apache POI | 5.2.5 | Word 文档解析 |
| Micrometer + Prometheus | - | 指标采集与监控 |
| Hutool | 5.8.25 | 常用工具库 |

### 前端技术

| 技术 | 版本 | 说明 |
|-----|------|------|
| Vue.js | 3.4.x | 渐进式框架 |
| Vite | 5.0 | 构建工具 |
| Element Plus | 2.5.4 | UI组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.5 | HTTP客户端 |
| WangEditor | 5.1 | 富文本编辑器 |
| Monaco Editor | 0.52 | 代码编辑器 |
| marked + highlight.js | - | Markdown 渲染与代码高亮 |
| diff2html | - | 文本差异对比 |
| Day.js | 1.11 | 日期处理 |

---

## 🚀 快速开始

### 环境要求

- **JDK** 17+
- **Node.js** 18+
- **MySQL** 8.0+
- **Redis** 6.0+
- **Maven** 3.8+

### 1. 初始化数据库

1) 创建数据库（默认库名见 `qa-system-backend/src/main/resources/application.yml.example`）

2) 执行项目内 SQL（按需启用模块）

- `qa-system-backend/src/main/resources/db/doc-check-schema.sql` 文档查重/降重表
- `qa-system-backend/src/main/resources/db/chat_tables.sql` 聊天/好友/群聊表
- `qa-system-backend/src/main/resources/db/ai-schema.sql` AI 对话相关表
- `qa-system-backend/src/main/resources/db/add-forum-table.sql` 交流区/论坛表

如需使用测试账号（若你的数据库已存在 `admin / student1 / teacher1` 等测试用户），可执行：

- `qa-system-backend/src/main/resources/db/init-data.sql`（将测试用户密码修复为 `123456`）

### 2. 后端配置与启动

1) 复制配置模板（本仓库默认忽略 `application.yml`）

Windows (PowerShell)：

```bash
copy src\main\resources\application.yml.example src\main\resources\application.yml
```

macOS/Linux：

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

2) 配置数据库/Redis/AI Key

后端 AI 使用 LangChain4j 的 OpenAI 兼容接口（见 `LangChainConfig`），常用配置项如下：

```yaml
langchain4j:
  open-ai:
    api-key: ${SILICONFLOW_API_KEY:}
    base-url: https://api.siliconflow.cn/v1
    model-name: Qwen/Qwen2.5-7B-Instruct
```

3) 启动后端

```bash
mvn spring-boot:run
```

### 3. 前端启动

```bash
cd qa-system-frontend

npm install
npm run dev
```

前端开发环境默认通过 Vite 代理转发：

- `/api` -> `http://localhost:8080`
- `/forum` -> `http://localhost:8080`
- `/uploads` -> `http://localhost:8080`
- `/ws` -> `ws://localhost:8080`

### 4. 访问

- **前端地址**: http://localhost:5173
- **后端地址**: http://localhost:8080

---

## 🔌 接口与协议约定（与代码一致）

### REST API

- **认证**: `/api/v1/auth/*`
- **问答**: `/api/v1/questions/*`、`/api/v1/answers/*`
- **文档**: `/api/v1/doc/*`（上传查重、批量更新段落、下载等）
- **聊天**: `/api/v1/chat/*`（好友/群聊/会话/消息等）
- **管理端**: `/api/v1/admin/*`

### AI 助手（HTTP + SSE）

- **普通对话**: `POST /api/ai/chat`
- **流式对话（SSE）**: `POST /api/ai/chat/stream`（`text/event-stream`）

### WebSocket

- **文档降重**: `ws://localhost:8080/ws/doc-rewrite`
- **即时聊天**: `ws://localhost:8080/ws/chat`

### 交流区/论坛（兼容旧路径）

- 基础路径：`/forum/*`

---

## 📁 项目结构

```
qa-system/
├── qa-system-backend/              # 后端项目
│   ├── src/main/java/com/qasystem/
│   │   ├── config/                 # 配置类
│   │   ├── controller/             # 控制器
│   │   │   ├── AuthController      # 认证登录
│   │   │   ├── QuestionController  # 问题管理
│   │   │   ├── AnswerController    # 回答管理
│   │   │   ├── ChatController      # 聊天/好友/群聊
│   │   │   ├── ForumController     # 论坛交流
│   │   │   ├── DocController       # 文档查重/降重
│   │   │   ├── AiAssistantController # AI助手
│   │   │   └── AdminController     # 后台管理
│   │   ├── entity/                 # 实体类
│   │   ├── mapper/                 # 数据层
│   │   ├── service/                # 业务层
│   │   └── websocket/              # WebSocket处理
│   └── src/main/resources/
│       └── application.yml         # 配置文件
│
├── qa-system-frontend/             # 前端项目
│   ├── src/
│   │   ├── api/                    # API接口
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 公共组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # 状态管理
│   │   └── views/                  # 页面视图
│   │       ├── admin/              # 管理后台
│   │       ├── student/            # 学生端
│   │       ├── teacher/            # 教师端
│   │       ├── forum/              # 论坛模块
│   │       └── common/             # 公共页面
│   └── package.json
│
├── pictures/                       # 项目截图
└── README.md                       # 项目文档
```

---

## 🔧 核心功能实现

### AI 智能助手

基于 **LangChain4j** 集成 OpenAI 兼容大模型接口，支持：

- 多轮对话上下文管理
- SSE 流式输出实时响应（`/api/ai/chat/stream`）
- 对话历史持久化
- 自定义 System Prompt

### 实时通讯系统

基于 **WebSocket** 实现的即时通讯（`/ws/chat`）：

- 私聊/群聊消息
- 图片、视频消息
- 在线状态实时同步
- 消息已读/撤回
- 群组管理（创建、邀请、踢人、转让）

### 文档降重系统

- Word 文档解析（Apache POI）
- 段落级查重分析（整体查重率 + 段落相似度）
- WebSocket 流式降重（`/ws/doc-rewrite`）
- Monaco Editor 全文编辑 + Diff 对比能力
- 下载处理后的 Word 文档（`/api/v1/doc/{documentId}/download`）

 
---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [MyBatis-Plus](https://baomidou.com/)

---

<p align="center">
  <strong>⭐ 如果这个项目对你有帮助，请给一个 Star 支持一下！</strong>
</p>

<p align="center">
  Made with ❤️ by <a href="https://github.com/ZSPSTRIVE">ZSPSTRIVE</a>
</p>
