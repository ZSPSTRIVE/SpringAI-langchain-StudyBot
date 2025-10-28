# 师生答疑系统 v2.0 - 快速开发指南

## 🚀 5分钟快速开始

### 前置要求

确保你已安装:
- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0
- Redis 6.0+

### 快速启动 (3步)

```bash
# 1. 初始化数据库
mysql -u root -p < qa-system-backend/src/main/resources/db/schema.sql

# 2. 启动Redis
docker run -d -p 6379:6379 redis:7-alpine

# 3. 运行启动脚本
# Windows用户
start.bat

# Linux/Mac用户
bash start.sh
```

访问: http://localhost:5173

---

## 📦 Docker快速部署

### 一键启动所有服务

```bash
# 1. 构建前端
cd qa-system-frontend
npm install
npm run build

# 2. 启动Docker服务
cd ..
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f
```

### 访问地址

- 前端: http://localhost
- 后端API: http://localhost:8080
- 监控: http://localhost:8080/actuator

### 停止服务

```bash
docker-compose down
```

---

## 🔧 开发环境配置

### 后端配置

1. **配置数据库连接**

编辑 `qa-system-backend/src/main/resources/application-dev.yml`:

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
```

2. **运行后端**

```bash
cd qa-system-backend
mvn clean spring-boot:run
```

后端将在 http://localhost:8080 启动

### 前端配置

1. **安装依赖**

```bash
cd qa-system-frontend
npm install
```

2. **配置API地址** (可选)

创建 `.env.development`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

3. **运行前端**

```bash
npm run dev
```

前端将在 http://localhost:5173 启动

---

## 🎯 核心API测试

### 1. 用户注册

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student001",
    "password": "pass123",
    "realName": "张三",
    "role": "STUDENT",
    "email": "student@example.com",
    "studentNo": "2024001",
    "major": "计算机科学与技术",
    "className": "计科2401",
    "grade": 2024,
    "college": "计算机学院"
  }'
```

### 2. 用户登录

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 3. 获取个人信息

```bash
curl http://localhost:8080/api/v1/profile/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. 更新个人信息

```bash
curl -X PUT http://localhost:8080/api/v1/profile/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "张三丰",
    "phone": "13800138000"
  }'
```

---

## 🛠️ 常见问题

### Q1: 后端启动失败，提示数据库连接错误

**解决方案:**
1. 确认MySQL已启动
2. 确认数据库`qa_system_v2`已创建
3. 检查用户名密码是否正确
4. 运行SQL初始化脚本:
   ```bash
   mysql -u root -p < qa-system-backend/src/main/resources/db/schema.sql
   ```

### Q2: Redis连接失败

**解决方案:**
1. 确认Redis已启动
   ```bash
   redis-cli ping  # 应返回PONG
   ```
2. 或使用Docker启动Redis:
   ```bash
   docker run -d -p 6379:6379 redis:7-alpine
   ```

### Q3: 前端请求跨域错误

**解决方案:**
1. 确认后端已正确配置CORS (已在SecurityConfig中配置)
2. 确认Vite代理配置正确 (vite.config.js)
3. 确认前端请求使用相对路径 `/api/...`

### Q4: 登录后Token无效

**解决方案:**
1. 检查JWT密钥配置
2. 检查Token是否正确保存到Cookie
3. 检查请求头是否正确携带Token
4. 查看浏览器开发者工具Network标签

### Q5: Docker构建失败

**解决方案:**
1. 确认Docker已安装并启动
2. 检查Docker镜像拉取是否成功
3. 检查前端是否已build:
   ```bash
   cd qa-system-frontend
   npm run build
   ```

---

## 📁 项目结构速查

### 后端关键目录

```
qa-system-backend/src/main/java/com/qasystem/
├── common/          # 公共工具
│   ├── response/    # 统一响应
│   └── util/        # 工具类
├── config/          # 配置类
├── controller/      # 控制器
├── dto/             # 数据传输对象
├── entity/          # 实体类
├── mapper/          # 数据访问层
├── security/        # 安全配置
└── service/         # 服务层
    └── impl/        # 服务实现
```

### 前端关键目录

```
qa-system-frontend/src/
├── api/             # API接口
├── assets/          # 静态资源
├── components/      # 公共组件
├── layouts/         # 布局组件
├── router/          # 路由配置
├── stores/          # 状态管理
├── utils/           # 工具函数
└── views/           # 页面组件
```

---

## 🔑 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理员，需修改密码后使用 |

> 学生和教师账号需通过注册页面创建

---

## 📝 开发规范

### 后端规范

1. **命名规范**
   - Controller: `XxxController`
   - Service接口: `XxxService`
   - Service实现: `XxxServiceImpl`
   - DTO: `XxxRequest`, `XxxResponse`, `XxxDTO`
   - Entity: 实体名 (如 `User`, `Student`)

2. **包结构**
   - controller: REST API控制器
   - service: 业务逻辑层
   - mapper: 数据访问层
   - entity: 实体类
   - dto: 数据传输对象
   - config: 配置类

3. **异常处理**
   - 统一使用 `RuntimeException`
   - 由 `GlobalExceptionHandler` 全局处理

### 前端规范

1. **命名规范**
   - 组件: PascalCase (如 `UserProfile.vue`)
   - API文件: camelCase (如 `userApi.js`)
   - 变量/函数: camelCase
   - 常量: UPPER_SNAKE_CASE

2. **目录结构**
   - views: 页面组件
   - components: 可复用组件
   - layouts: 布局组件
   - api: API接口封装

3. **状态管理**
   - 使用Pinia stores
   - 模块化管理 (user, app, etc.)

---

## 🎯 下一步开发

### Phase 3: 核心问答模块

**后端任务:**
1. 创建 `SubjectController` 和 `SubjectService`
2. 创建 `QuestionController` 和 `QuestionService`
3. 创建 `AnswerController` 和 `AnswerService`
4. 实现分页查询和搜索

**前端任务:**
1. 实现问题广场页面
2. 实现提问页面 (集成WangEditor)
3. 实现问题详情页面
4. 实现答疑中心页面

### Phase 4: COS对象存储

1. 后端COS SDK集成
2. 图片上传接口
3. 前端图片上传组件
4. WangEditor图片插入

---

## 📚 推荐阅读

### 官方文档

- [Spring Boot 3 文档](https://spring.io/projects/spring-boot)
- [Spring Security 6 文档](https://spring.io/projects/spring-security)
- [Vue 3 文档](https://cn.vuejs.org/)
- [Element Plus 文档](https://element-plus.org/zh-CN/)
- [Vite 文档](https://cn.vitejs.dev/)

### 技术博客

- MyBatis-Plus 使用指南
- JWT 认证最佳实践
- Redis 缓存策略
- Docker 部署教程

---

## 💬 获取帮助

1. **查看文档**
   - PROJECT_README.md
   - DEVELOPMENT_PROGRESS.md
   - PROJECT_SUMMARY.md

2. **查看日志**
   ```bash
   # 后端日志
   tail -f logs/qa-system.log
   
   # Docker日志
   docker-compose logs -f backend
   ```

3. **Debug模式**
   - IDEA: Run -> Debug 'QaSystemApplication'
   - VSCode: F5 启动调试

---

**祝开发顺利！** 🚀

最后更新: 2025-01-27

