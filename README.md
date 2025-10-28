# 🎓 师生答疑系统 v2.0

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.15-4FC08D.svg)](https://vuejs.org/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-blue.svg)](https://baomidou.com/)
[![Redis](https://img.shields.io/badge/Redis-6.0+-DC382D.svg)](https://redis.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg)](https://www.mysql.com/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-0.35.0-orange.svg)](https://docs.langchain4j.dev/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.5.4-409EFF.svg)](https://element-plus.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

一个现代化的师生互动答疑平台，集成AI智能助手，支持实时问答、交流论坛和学习资源推荐。

 [快速开始](#快速开始) | [功能特性](#功能特性) | [技术栈](#技术栈)

## ✨ 功能特性

### 核心功能
- 🙋 **智能问答** - 学生提问，教师解答，支持富文本编辑和图片上传
- 💬 **交流论坛** - 实时讨论区，支持点赞、评论、关注
- ⭐ **收藏关注** - 收藏问题、关注用户，个性化内容推荐
- 👨‍💼 **后台管理** - 完善的管理端，用户、问题、科目统一管理

### 🤖 AI助手 (v2.0新增)
- **智能对话** - 基于阿里通义千问qwen-turbo模型
- **多轮对话** - 保持上下文，支持连续提问
- **问题分类** - 自动识别技术、学科、课程等问题类型
- **资源推荐** - 根据问题智能推荐学习资源
- **对话管理** - 会话历史、收藏、反馈一体化

## 🚀 快速开始

### 环境要求
- **JDK** 17+
- **Node.js** 16+
- **MySQL** 8.0+
- **Redis** 6.0+
- **Maven** 3.8+

### 后端启动

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/qa-system.git
cd qa-system

# 2. 导入数据库
mysql -u root -p qa_system_v2 < qa-system-backend/src/main/resources/db/schema.sql

# 3. 配置application.yml
cd qa-system-backend/src/main/resources
cp application-dev.yml.example application-dev.yml
# 修改数据库、Redis、AI API配置

# 4. 启动后端
cd qa-system-backend
mvn clean install
mvn spring-boot:run
```

后端服务运行在 `http://localhost:8080`

### 前端启动

```bash
# 1. 安装依赖
cd qa-system-frontend
npm install

# 2. 启动开发服务器
npm run dev
```

前端应用运行在 `http://localhost:5173`

### Docker部署

```bash
# 使用docker-compose一键启动
docker-compose up -d
```

## 📁 项目结构

```
qa-system/
├── qa-system-backend/          # Spring Boot后端
│   ├── src/main/java/
│   │   └── com/qasystem/
│   │       ├── controller/     # REST API控制器
│   │       ├── service/        # 业务逻辑层
│   │       ├── mapper/         # MyBatis-Plus数据访问
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── config/         # 配置类
│   │       └── security/       # 安全认证
│   └── src/main/resources/
│       ├── db/                 # 数据库脚本
│       └── application.yml     # 配置文件
│
└── qa-system-frontend/         # Vue 3前端
    ├── src/
    │   ├── api/                # API接口封装
    │   ├── views/              # 页面组件
    │   │   ├── admin/          # 管理端
    │   │   ├── student/        # 学生端
    │   │   ├── teacher/        # 教师端
    │   │   ├── forum/          # 论坛
    │   │   └── common/         # 公共页面(AI助手)
    │   ├── components/         # 通用组件
    │   ├── layouts/            # 布局组件
    │   ├── stores/             # Pinia状态管理
    │   ├── router/             # 路由配置
    │   └── utils/              # 工具函数
    └── package.json
```

## 🛠 技术栈

### 后端技术栈
| 技术 | 版本 | 说明 |
|------|------|------|
| **核心框架** |
| Spring Boot | 3.1.5 | 应用框架 |
| Spring Web MVC | 6.0.13 | Web框架 |
| Spring Security | 6.1.5 | 安全认证框架 |
| Spring Data Redis | 3.1.5 | Redis数据访问 |
| **数据持久化** |
| MyBatis-Plus | 3.5.7 | ORM增强框架 |
| HikariCP | 5.0.1 | 数据库连接池 |
| MySQL Connector | 8.0.33 | MySQL驱动 |
| **认证授权** |
| JWT (jjwt) | 0.12.3 | Token生成与验证 |
| Spring Security JWT | - | JWT集成 |
| **缓存中间件** |
| Redis | 6.0+ | 缓存数据库 |
| Lettuce | 6.2.6 | Redis客户端 |
| **AI集成** |
| LangChain4j Core | 0.35.0 | AI应用框架 |
| LangChain4j DashScope | 0.35.0 | 阿里百练集成 |
| 阿里百练 | - | 通义千问API服务 |
| **工具库** |
| Hutool | 5.8.25 | Java工具集 |
| Lombok | 1.18.30 | 代码简化 |
| Jackson | 2.15.3 | JSON处理 |
| **监控运维** |
| Spring Boot Actuator | 3.1.5 | 应用监控 |
| Micrometer | 1.11.5 | 指标收集 |
| Prometheus | - | 监控系统 |
| **构建部署** |
| Maven | 3.8+ | 项目构建 |
| Docker | - | 容器化部署 |

### 前端技术栈
| 技术 | 版本 | 说明 |
|------|------|------|
| **核心框架** |
| Vue 3 | 3.4.15 | 渐进式JavaScript框架 |
| Vue Router | 4.2.5 | 官方路由管理器 |
| Pinia | 2.1.7 | 官方状态管理 |
| Pinia Persist | 3.2.1 | 状态持久化插件 |
| **UI组件** |
| Element Plus | 2.5.4 | Vue 3 UI组件库 |
| Element Icons | 2.3.1 | 图标库 |
| **富文本编辑** |
| wangEditor | 5.1.23 | 富文本编辑器 |
| wangEditor for Vue | 5.1.12 | Vue集成 |
| **HTTP通信** |
| Axios | 1.6.5 | HTTP客户端 |
| **Markdown渲染** |
| Marked | 11.1.1 | Markdown解析器 |
| Highlight.js | 11.9.0 | 代码语法高亮 |
| **工具库** |
| Day.js | 1.11.10 | 日期时间处理 |
| js-cookie | 3.0.5 | Cookie操作 |
| **构建工具** |
| Vite | 5.0.11 | 下一代前端构建工具 |
| Vite Plugin Vue | 5.0.3 | Vue插件 |
| **代码质量** |
| ESLint | 8.56.0 | 代码检查 |
| ESLint Plugin Vue | 9.20.1 | Vue规则 |
| **CSS预处理** |
| Sass | 1.70.0 | CSS预处理器 |
| **自动导入** |
| unplugin-auto-import | 0.17.3 | API自动导入 |
| unplugin-vue-components | 0.26.0 | 组件自动导入 |

### 数据库
| 技术 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 内存数据库 |

### DevOps
| 技术 | 版本 | 说明 |
|------|------|------|
| Docker | - | 容器化平台 |
| Docker Compose | - | 容器编排 |
| Nginx | - | Web服务器/反向代理 |
| Git | - | 版本控制 |

## 📝 API文档

### 认证接口
```
POST   /api/v1/auth/login          # 用户登录
POST   /api/v1/auth/register       # 用户注册
POST   /api/v1/auth/logout         # 退出登录
```

### 问答接口
```
GET    /api/v1/questions           # 获取问题列表
POST   /api/v1/questions           # 发布问题
GET    /api/v1/questions/{id}      # 获取问题详情
POST   /api/v1/questions/{id}/answers  # 回答问题
```

### AI助手接口
```
POST   /api/ai/chat                # AI对话
GET    /api/ai/sessions            # 获取会话列表
GET    /api/ai/sessions/{id}/history  # 获取会话历史
POST   /api/ai/feedback            # 提交反馈
POST   /api/ai/bookmark/{id}       # 收藏对话
```

完整API文档：[API Documentation](docs/API.md)

## 🎨 功能演示

### 学生端
- 浏览问题广场，按科目筛选
- 发布问题，富文本编辑支持
- 查看教师回答，采纳最佳答案
- 使用AI助手获取学习帮助

### 教师端
- 查看待回答问题列表
- 回答学生问题
- 查看历史回答记录

### 管理端
- 用户管理（学生、教师）
- 问题审核与管理
- 科目分类管理
- 系统数据统计

### AI助手
- ChatGPT风格对话界面
- 实时Markdown渲染
- 代码语法高亮
- 学习资源推荐
- 对话历史管理

## 🔑 配置说明

### 后端配置 `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qa_system_v2
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your-secret-key
  expiration: 604800000  # 7天

# AI配置
langchain4j:
  dashscope:
    api-key: your-dashscope-api-key
    model-name: qwen-turbo
```

### 前端配置 `vite.config.js`
```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 👥 用户角色

| 角色 | 权限 |
|------|------|
| **学生** | 提问、收藏、关注、使用AI助手 |
| **教师** | 回答问题、查看问题列表、使用AI助手 |
| **管理员** | 全部权限，包括用户管理、内容审核 |

## 🔐 默认账号

```
管理员:  admin / admin123
教师:    teacher / teacher123
学生:    student / student123
```

## 📊 数据库设计

### 核心表
- `user` - 用户表
- `student` - 学生信息表
- `teacher` - 教师信息表
- `question` - 问题表
- `answer` - 回答表
- `subject` - 科目表
- `forum` - 论坛帖子表
- `ai_conversation` - AI对话历史表

完整数据库设计：[Database Schema](docs/DATABASE.md)

## 🚢 部署指南

### 生产环境部署

1. **构建前端**
```bash
cd qa-system-frontend
npm run build
```

2. **打包后端**
```bash
cd qa-system-backend
mvn clean package -DskipTests
```

3. **Nginx配置**
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /var/www/qa-system-frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
    }
}
```

4. **运行后端**
```bash
java -jar qa-system-backend/target/qa-system-backend-2.0.0.jar
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 📄 开源协议

本项目采用 [MIT](LICENSE) 协议开源

## 📮 联系方式

- 作者：Your Name
- 邮箱：your.email@example.com
- 项目地址：[GitHub](https://github.com/yourusername/qa-system)

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [LangChain4j](https://docs.langchain4j.dev/)
- [阿里云百炼](https://www.aliyun.com/product/bailian)

---

⭐ 如果这个项目对你有帮助，请给一个Star支持一下！
