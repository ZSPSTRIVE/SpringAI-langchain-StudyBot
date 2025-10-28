# 师生答疑系统 v2.0

> 现代化、分布式的师生在线问答平台

## 📋 项目概述

师生答疑系统v2.0是一个基于前后端分离架构的现代化问答平台，采用Spring Boot 3 + Vue 3技术栈，支持学生提问、教师答疑、社交互动等功能。

### 核心特性

- ✅ **现代化技术栈**: Spring Boot 3 + Vue 3 + Element Plus
- ✅ **安全认证**: Spring Security 6 + JWT无状态认证
- ✅ **高性能缓存**: Redis分布式缓存
- ✅ **对象存储**: 腾讯云COS，支持海量文件存储
- ✅ **容器化部署**: Docker + Docker Compose一键部署
- ✅ **可观测性**: Prometheus + Actuator监控
- ✅ **响应式设计**: 适配PC和移动端

## 🏗️ 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.1 | 核心框架 |
| Spring Security | 6.x | 安全认证 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7.x | 缓存 |
| JWT | 0.12.3 | Token认证 |
| Lombok | - | 简化代码 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| Vite | 5.0 | 构建工具 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.2 | 路由管理 |
| Element Plus | 2.5 | UI组件库 |
| Axios | 1.6 | HTTP客户端 |
| WangEditor | 5.1 | 富文本编辑器 |

## 📁 项目结构

```
师生答疑系统-v2/
├── qa-system-backend/          # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/qasystem/
│   │   │   │   ├── common/     # 公共工具
│   │   │   │   ├── config/     # 配置类
│   │   │   │   ├── controller/ # 控制器
│   │   │   │   ├── dto/        # 数据传输对象
│   │   │   │   ├── entity/     # 实体类
│   │   │   │   ├── mapper/     # 数据访问层
│   │   │   │   ├── security/   # 安全配置
│   │   │   │   └── service/    # 服务层
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/schema.sql
│   │   └── test/
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
├── qa-system-frontend/         # 前端应用
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── views/              # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
├── nginx/                      # Nginx配置
│   ├── nginx.conf
│   └── conf.d/
│       └── default.conf
├── docker-compose.yml          # Docker编排
└── PROJECT_README.md           # 项目说明（本文件）
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Docker**: 20.10+ (可选)
- **Maven**: 3.6+

### 方式一：Docker Compose 部署（推荐）

```bash
# 1. 克隆项目
git clone <repository-url>
cd qa-system-v2

# 2. 构建前端
cd qa-system-frontend
npm install
npm run build

# 3. 启动所有服务
cd ..
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f backend
```

访问：
- 前端: http://localhost
- 后端API: http://localhost:8080
- 监控端点: http://localhost:8080/actuator

### 方式二：本地开发

#### 后端启动

```bash
# 1. 进入后端目录
cd qa-system-backend

# 2. 初始化数据库
mysql -u root -p < src/main/resources/db/schema.sql

# 3. 修改配置文件
# 编辑 src/main/resources/application-dev.yml
# 配置数据库和Redis连接信息

# 4. 启动后端
mvn spring-boot:run
```

#### 前端启动

```bash
# 1. 进入前端目录
cd qa-system-frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

访问 http://localhost:5173

## 👥 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

> 注意：首次使用需要通过注册页面创建学生和教师账号

## 📊 功能模块

### Phase 1: 用户认证与权限 ✅

- [x] 用户登录/注册
- [x] JWT Token认证
- [x] 角色权限控制（学生/教师/管理员）
- [x] 路由守卫
- [x] Token刷新机制

### Phase 2: 个人中心 🚧

- [ ] 个人资料查看/编辑
- [ ] 头像上传（COS）
- [ ] 密码修改
- [ ] 历史记录查看

### Phase 3: 核心问答 🚧

- [ ] 学生提问
- [ ] 教师回答
- [ ] 问题分类（科目）
- [ ] 问题搜索
- [ ] 问题详情页
- [ ] 回答采纳

### Phase 4: 社交互动 🚧

- [ ] 关注教师
- [ ] 收藏问题/回答
- [ ] 交流区发帖
- [ ] 帖子评论
- [ ] 点赞功能

### Phase 5: 管理后台 🚧

- [ ] 用户管理
- [ ] 科目管理
- [ ] 问题审核
- [ ] 数据统计

## 🔧 配置说明

### 后端配置

编辑 `qa-system-backend/src/main/resources/application.yml`:

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
  expiration: 604800000

cos:
  secret-id: your-cos-secret-id
  secret-key: your-cos-secret-key
  region: ap-beijing
  bucket-name: your-bucket
```

### 前端配置

编辑 `qa-system-frontend/.env.development`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 📝 API文档

### 认证接口

#### 登录
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "student001",
  "password": "pass123"
}
```

#### 注册
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "student001",
  "password": "pass123",
  "realName": "张三",
  "role": "STUDENT",
  "email": "student@example.com",
  ...
}
```

更多API文档请参考后端README。

## 🔍 监控与运维

### 健康检查

```bash
curl http://localhost:8080/actuator/health
```

### Prometheus指标

```bash
curl http://localhost:8080/actuator/prometheus
```

### 查看日志

```bash
# Docker环境
docker-compose logs -f backend

# 本地开发
tail -f logs/qa-system.log
```

## 🛠️ 开发指南

### 代码规范

- 后端遵循阿里巴巴Java开发规范
- 前端使用ESLint + Vue官方风格指南
- 提交前请运行代码检查：
  ```bash
  # 前端
  npm run lint
  
  # 后端
  mvn checkstyle:check
  ```

### 分支管理

- `main`: 主分支，生产环境代码
- `develop`: 开发分支
- `feature/*`: 功能分支
- `hotfix/*`: 紧急修复分支

## 📄 许可证

MIT License

## 👨‍💻 贡献者

- 开发团队：QA System Team
- 架构审查：P9 Architect

## 📮 联系方式

如有问题或建议，请提交Issue或联系开发团队。

---

**更新日期**: 2025-01-27  
**版本**: v2.0.0

