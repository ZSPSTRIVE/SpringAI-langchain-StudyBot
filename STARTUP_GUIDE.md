# 🚀 师生答疑系统 v2.0 - 启动指南

## 📋 环境要求

- **JDK**: 17 或 21
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.0+ (可选，用于缓存)
- **Maven**: 3.8+

---

## 🔧 快速启动步骤

### 1️⃣ 数据库准备

#### 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS qa_system_v2 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

#### 导入数据库结构
```bash
# 进入MySQL
mysql -u root -p

# 使用数据库
USE qa_system_v2;

# 导入结构
SOURCE qa-system-backend/src/main/resources/db/schema.sql;

# 导入初始数据
SOURCE qa-system-backend/src/main/resources/db/init-data.sql;
```

或者使用命令行直接导入：
```bash
mysql -u root -p qa_system_v2 < qa-system-backend/src/main/resources/db/schema.sql
mysql -u root -p qa_system_v2 < qa-system-backend/src/main/resources/db/init-data.sql
```

### 2️⃣ 启动Redis（可选）

```bash
# Docker方式
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 或使用本地Redis
redis-server
```

### 3️⃣ 启动后端

#### 方式1: IDEA运行（推荐）
1. 打开IDEA，导入项目
2. 找到 `QaSystemApplication.java`
3. 右键 → Run 'QaSystemApplication'

#### 方式2: Maven命令
```bash
cd qa-system-backend
mvn clean package -DskipTests
java -jar target/qa-system-backend-2.0.0.jar
```

#### 方式3: Maven Spring Boot插件
```bash
cd qa-system-backend
mvn spring-boot:run
```

#### 验证后端启动成功
访问: http://localhost:8080/actuator/health

应该看到:
```json
{
  "status": "UP"
}
```

### 4️⃣ 启动前端

```bash
cd qa-system-frontend
npm install
npm run dev
```

访问: http://localhost:5173

---

## 👤 测试账号

### 管理员
- 用户名: `admin`
- 密码: `123456`

### 学生
- 用户名: `student1`
- 密码: `123456`
- 学号: 2021001
- 专业: 计算机科学与技术

### 教师
- 用户名: `teacher1`
- 密码: `123456`
- 工号: T2020001
- 职称: 教授

---

## 🐛 常见问题

### 1. 后端启动失败 - 数据库连接错误

**错误信息**: `Communications link failure`

**解决方案**:
检查 `application-dev.yml` 配置:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qa_system_v2
    username: root
    password: 你的密码
```

### 2. 后端启动失败 - Redis连接错误

**错误信息**: `Unable to connect to Redis`

**解决方案**:
- 启动Redis服务
- 或修改 `application-dev.yml`，将Redis host改为你的Redis地址

### 3. 前端401错误

**原因**: 未登录或Token过期

**解决方案**:
1. 访问登录页面
2. 使用测试账号登录
3. 系统会自动保存Token

### 4. 前端500错误

**原因**: 后端接口异常

**解决方案**:
1. 查看后端控制台日志
2. 检查数据库是否正常
3. 确认初始数据已导入

### 5. 登录时提示"用户名或密码错误"

**原因**: 数据库中没有该用户

**解决方案**:
1. 确认已导入 `init-data.sql`
2. 或使用注册功能创建新用户
3. 检查密码是否为 `123456`

---

## 📊 系统架构

```
┌─────────────────┐
│   Vue 3 前端    │ :5173
│  (Element Plus) │
└────────┬────────┘
         │ HTTP
┌────────▼────────┐
│   Nginx代理     │ :80 (生产环境)
└────────┬────────┘
         │
┌────────▼────────┐
│  Spring Boot 3  │ :8080
│   (后端API)     │
└────┬───────┬────┘
     │       │
┌────▼──┐ ┌─▼────┐
│ MySQL │ │ Redis│
│  :3306│ │ :6379│
└───────┘ └──────┘
```

---

## 🎯 功能测试流程

### 学生端测试
1. ✅ 登录系统 (student1/123456)
2. ✅ 浏览问题广场
3. ✅ 发布新问题
4. ✅ 查看问题详情
5. ✅ 采纳教师回答
6. ✅ 关注教师
7. ✅ 收藏问题
8. ✅ 修改个人资料

### 教师端测试
1. ✅ 登录系统 (teacher1/123456)
2. ✅ 查看待回答问题
3. ✅ 快速回答问题
4. ✅ 编辑回答
5. ✅ 修改个人资料

### 管理员测试
1. ✅ 登录系统 (admin/123456)
2. ✅ 管理科目
3. ✅ 审核问题
4. ✅ 管理用户

---

## 🔑 API端点

### 认证相关
- `POST /api/v1/auth/login` - 用户登录
- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/logout` - 用户登出
- `POST /api/v1/auth/refresh` - 刷新Token

### 个人中心
- `GET /api/v1/profile/me` - 获取个人信息
- `PUT /api/v1/profile/me` - 更新个人信息
- `PUT /api/v1/profile/password` - 修改密码

### 问题管理
- `GET /api/v1/questions` - 问题列表（分页）
- `POST /api/v1/questions` - 发布问题
- `GET /api/v1/questions/{id}` - 问题详情
- `PUT /api/v1/questions/{id}` - 更新问题
- `DELETE /api/v1/questions/{id}` - 删除问题

### 回答管理
- `GET /api/v1/questions/{id}/answers` - 获取问题的所有回答
- `POST /api/v1/questions/{id}/answer` - 回答问题
- `PUT /api/v1/answers/{id}` - 更新回答
- `DELETE /api/v1/answers/{id}` - 删除回答
- `PUT /api/v1/answers/{id}/accept` - 采纳回答

### 社交功能
- `POST /api/v1/follows/teacher/{id}` - 关注教师
- `DELETE /api/v1/follows/teacher/{id}` - 取消关注
- `GET /api/v1/follows/teachers` - 关注列表
- `POST /api/v1/collections` - 收藏
- `DELETE /api/v1/collections` - 取消收藏
- `GET /api/v1/collections/questions` - 收藏列表

---

## 📝 开发提示

### 后端开发
- 代码位置: `qa-system-backend/src/main/java/com/qasystem/`
- 配置文件: `qa-system-backend/src/main/resources/`
- 热部署: 使用Spring Boot DevTools

### 前端开发
- 代码位置: `qa-system-frontend/src/`
- 组件位置: `qa-system-frontend/src/views/`
- API封装: `qa-system-frontend/src/api/`
- 热更新: Vite自动热更新

---

## 🎊 完成！

系统启动成功后，您应该能看到：

**后端控制台**:
```
========================================
师生答疑系统 v2.0 启动成功！
========================================
```

**前端界面**:
精美的Element Plus登录页面

---

## 📞 技术支持

如有问题，请查看:
- `PROJECT_SUMMARY.md` - 项目总结
- `DEVELOPMENT_PROGRESS.md` - 开发进度
- `FINAL_SOLUTION.md` - 最终解决方案

---

**祝使用愉快！** 🚀

