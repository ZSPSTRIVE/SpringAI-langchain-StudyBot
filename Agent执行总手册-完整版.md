# 师生答疑系统重构 - Agent执行总手册

> **版本**: v3.1 Final  
> **目标**: 将现有Spring Boot 2 + Vue 2系统升级为现代化Spring Boot 3 + Vue 3架构  
> **适用对象**: AI Agent (Cursor), 开发团队

---

## 📊 项目总览

### 现有系统状况

**数据库表结构（基于springboot7vkr2.sql）:**
- **用户表**: users(管理员), xuesheng(学生), laoshi(教师)
- **问答表**: xueshengwenti(学生问题), laoshihuida(教师回答)
- **社交表**: guanzhuliebiao(关注), storeup(收藏), forum(交流区)
- **内容表**: kemuleixing(科目), laoshixinxi(教师信息)
- **互动表**: discussxueshengwenti, discusslaoshihuida, discusslaoshixinxi(评论)
- **系统表**: token, config

**现有前端分析（基于src/main/resources）:**
- **管理端**: Vue 2 + Element UI + 完整CRUD界面
  - 路径: admin/admin/src/
  - 核心组件: list.vue(列表), add-or-update.vue(表单)
  - 路由: router-static.js (静态路由配置)
  
- **用户端**: 原生HTML + LayUI + jQuery
  - 路径: front/front/
  - 页面: pages/xuesheng/, pages/laoshi/, pages/login/
  - 功能: 个人中心、问题列表、答疑中心

**业务流程:**
1. 学生发布问题 → 2. 教师浏览问题 → 3. 教师回答 → 4. 学生查看回答 → 5. 互动(评论/点赞/收藏)

---

## 🎯 重构目标与价值

### 技术升级对比

| 组件 | 旧版本 | 新版本 | 升级价值 |
|------|--------|--------|---------|
| Spring Boot | 2.2.2 | 3.2+ | Java 17支持、性能提升30% |
| 安全框架 | Token简单验证 | Spring Security 6 + JWT | 标准化、更安全 |
| ORM | MyBatis-Plus 2.3 | MyBatis-Plus 3.5 | 更多特性、更好兼容性 |
| 前端框架 | Vue 2 + 原生HTML混合 | Vue 3统一 | Composition API、更好性能 |
| UI组件 | Element UI 2 + LayUI混合 | Element Plus统一 | 现代化、体验一致 |
| 状态管理 | 无/简单Store | Pinia | 轻量、TypeScript友好 |
| 缓存 | 无 | Redis 7+ | 性能提升、支持分布式 |
| 存储 | 本地文件 | 腾讯云COS | 可扩展、CDN加速 |
| 部署 | 传统部署 | Docker + K8s | 容器化、易扩展 |

### 核心改进

1. **性能优化**: Redis缓存 + CDN + 数据库索引优化
2. **用户体验**: 统一UI + 响应式设计 + Loading反馈
3. **代码质量**: TypeScript + 规范化 + 模块化
4. **可维护性**: 清晰架构 + 完整文档 + 单元测试
5. **可扩展性**: 微服务就绪 + 容器化 + 云原生

---

## 📋 执行清单 (Checklist)

### Phase 1: 基础架构 (Week 1-2)

#### 1.1 后端基础 ✅
- [ ] 创建Spring Boot 3.2项目
- [ ] 配置pom.xml（Spring Security 6, JWT, Redis, MyBatis-Plus 3.5）
- [ ] 配置application.yml（数据源、Redis、JWT）
- [ ] 项目结构搭建（controller/service/mapper/entity/dto）
- [ ] 配置Logback日志（JSON格式 + MDC）
- [ ] 配置Actuator监控端点

**核心代码文件:**
```
src/main/java/com/qasystem/
├── QaSystemApplication.java
├── config/
│   ├── SecurityConfig.java       ✅ 必须实现
│   ├── RedisConfig.java          ✅ 必须实现
│   ├── MyBatisPlusConfig.java    ✅ 必须实现
│   ├── CorsConfig.java
│   └── CosConfig.java
├── security/
│   ├── JwtAuthenticationFilter.java  ✅ 必须实现
│   ├── JwtAuthenticationEntryPoint.java
│   └── UserDetailsServiceImpl.java   ✅ 必须实现
└── common/
    ├── util/JwtUtil.java         ✅ 必须实现
    ├── exception/GlobalExceptionHandler.java
    └── response/Result.java
```

#### 1.2 数据库迁移 ✅
- [ ] 创建新数据库qa_system_v2
- [ ] 设计优化后的表结构
- [ ] 编写数据迁移脚本
- [ ] 执行迁移并验证数据完整性
- [ ] 创建必要索引

**迁移脚本关键点:**
```sql
-- 用户表重命名和字段优化
users → sys_admin
xuesheng → sys_student (添加student_no, major, grade等)
laoshi → sys_teacher (添加teacher_no, department, title等)

-- 密码加密升级
原始密码 → BCrypt加密（$2a$10$...）

-- 问答表优化
xueshengwenti → question (添加view_count, like_count索引)
laoshihuida → answer (优化关联关系)

-- 社交表规范化
guanzhuliebiao → follow
storeup → collection
forum → forum_post + forum_comment
```

#### 1.3 Spring Security + JWT认证 ✅
- [ ] 实现JwtUtil工具类
- [ ] 配置SecurityFilterChain
- [ ] 实现JWT过滤器
- [ ] 实现UserDetailsService
- [ ] 配置角色权限

**认证流程:**
```
1. 登录请求 → AuthController.login()
2. 验证用户名密码 → UserDetailsService
3. 生成JWT Token → JwtUtil.generateToken()
4. 返回Token + 用户信息
5. 后续请求携带Token → JwtAuthenticationFilter验证
6. 从Redis验证Token有效性
7. 放行请求到Controller
```

**API端点:**
```
POST /api/v1/auth/login       - 登录
POST /api/v1/auth/register    - 注册
POST /api/v1/auth/refresh     - 刷新Token
POST /api/v1/auth/logout      - 登出
GET  /api/v1/auth/me          - 获取当前用户信息
```

#### 1.4 Redis集成 ✅
- [ ] 配置RedisTemplate
- [ ] 实现RedisUtil工具类
- [ ] 设计Redis Key规范
- [ ] 实现Token黑名单机制
- [ ] 实现缓存注解

**Redis Key设计:**
```
# 用户相关
user:info:{userId}              TTL: 30min
user:session:{token}            TTL: 7day
user:token:blacklist:{token}    TTL: 7day

# 认证相关
auth:captcha:{uuid}             TTL: 5min
auth:login:fail:{username}      TTL: 15min

# 业务缓存
question:detail:{id}            TTL: 30min
question:hot:list               TTL: 10min
subject:list                    TTL: 1hour
```

#### 1.5 前端Vue 3项目初始化 ✅
- [ ] 使用Vite创建Vue 3 + TypeScript项目
- [ ] 安装依赖（Vue Router, Pinia, Element Plus, Axios）
- [ ] 配置vite.config.ts
- [ ] 配置TypeScript
- [ ] 搭建项目目录结构

**核心配置:**
```typescript
// vite.config.ts
- AutoImport插件（自动导入Vue/Router/Pinia API）
- Components插件（自动导入Element Plus组件）
- 路径别名配置（@/指向src/）
- 代理配置（/api代理到后端）
- 打包优化（分包策略）
```

#### 1.6 前端工具封装 ✅
- [ ] Axios请求封装（request.ts）
- [ ] 本地存储封装（storage.ts）
- [ ] 认证工具（auth.ts）
- [ ] 表单验证工具（validate.ts）

**关键特性:**
- 统一错误处理
- Loading自动管理
- Token自动注入
- 请求/响应拦截
- TypeScript类型支持

#### 1.7 Pinia状态管理 ✅
- [ ] 用户状态（stores/user.ts）
- [ ] 应用状态（stores/app.ts）
- [ ] 权限状态（stores/permission.ts）

#### 1.8 路由配置 ✅
- [ ] 定义路由表（routes.ts）
- [ ] 实现路由守卫（guards.ts）
- [ ] 角色权限控制
- [ ] 页面标题管理

**路由结构:**
```
/login                    - 登录页
/register                 - 注册页
/admin/*                  - 管理端（ADMIN角色）
  /dashboard              - 控制台
  /users/students         - 学生管理
  /users/teachers         - 教师管理
  /questions              - 问题管理
  /subjects               - 科目管理
/student/*                - 学生端（STUDENT角色）
  /home                   - 首页
  /questions              - 问题广场
  /ask                    - 我要提问
  /teachers               - 教师列表
  /profile                - 个人中心
/teacher/*                - 教师端（TEACHER角色）
  /home                   - 首页
  /answer-center          - 答疑中心
  /my-answers             - 我的回答
  /profile                - 个人中心
```

### Phase 2: 个人中心 + COS (Week 3-4)

#### 2.1 个人中心后端 ✅
- [ ] ProfileController实现
- [ ] 获取个人信息API
- [ ] 更新个人资料API
- [ ] 修改密码API
- [ ] 个人统计数据API

**核心接口:**
```java
GET  /api/v1/profile/me              - 获取当前用户信息
PUT  /api/v1/profile/me              - 更新个人资料
PUT  /api/v1/profile/password        - 修改密码
POST /api/v1/profile/avatar          - 上传头像
GET  /api/v1/profile/stats           - 个人统计
GET  /api/v1/profile/questions       - 我的提问
GET  /api/v1/profile/answers         - 我的回答
GET  /api/v1/profile/collections     - 我的收藏
```

#### 2.2 腾讯云COS集成 ✅
- [ ] 配置COS客户端（CosConfig.java）
- [ ] 实现COS服务（CosService.java）
- [ ] 实现文件上传接口（FileController.java）
- [ ] 前端上传组件（CosUpload.vue）

**上传流程:**
```
1. 前端选择文件
2. 调用/api/v1/file/avatar上传
3. 后端验证文件（大小、类型）
4. 生成唯一文件名（UUID + 日期路径）
5. 上传到COS
6. 返回CDN URL
7. 前端显示图片
```

**文件夹结构:**
```
COS Bucket根目录/
├── avatar/2025/01/27/xxx.jpg       - 头像
├── question/2025/01/27/xxx.jpg     - 问题图片
├── answer/2025/01/27/xxx.jpg       - 回答图片
└── editor/2025/01/27/xxx.jpg       - 富文本图片
```

#### 2.3 个人中心前端 ✅
- [ ] 学生个人中心页面
- [ ] 教师个人中心页面
- [ ] 头像上传组件
- [ ] 资料编辑表单
- [ ] 我的提问/回答列表

**页面布局:**
```
┌─────────────────────────────────┐
│         顶部导航栏                 │
├─────────┬───────────────────────┤
│         │  个人信息卡片            │
│ 侧边菜单 │  - 头像                │
│ - 个人资料│  - 姓名/账号           │
│ - 我的提问│  - 角色                │
│ - 我的收藏│  - 统计数据            │
│ - 账号安全├───────────────────────┤
│         │  内容区域              │
│         │  （根据菜单切换）        │
└─────────┴───────────────────────┘
```

### Phase 3: 核心问答模块 (Week 5-6)

#### 3.1 问答数据库表 ✅
- [ ] subject表（科目类型）
- [ ] question表（学生问题）
- [ ] answer表（教师回答）
- [ ] comment表（评论）

#### 3.2 科目管理 ✅
- [ ] 后端CRUD接口
- [ ] 管理端科目管理页面
- [ ] Redis缓存科目列表

**接口:**
```
GET  /api/v1/subjects              - 获取科目列表
POST /api/v1/admin/subjects        - 创建科目（管理员）
PUT  /api/v1/admin/subjects/{id}   - 更新科目
DELETE /api/v1/admin/subjects/{id} - 删除科目
```

#### 3.3 问题模块 ✅
- [ ] 发布问题接口
- [ ] 问题列表接口（分页、筛选）
- [ ] 问题详情接口
- [ ] 问题搜索接口
- [ ] 问题点赞/收藏接口

**核心功能:**
```java
// QuestionController.java
POST   /api/v1/questions                 - 发布问题
GET    /api/v1/questions                 - 问题列表
GET    /api/v1/questions/{id}            - 问题详情
PUT    /api/v1/questions/{id}            - 更新问题
DELETE /api/v1/questions/{id}            - 删除问题
POST   /api/v1/questions/{id}/like       - 点赞
POST   /api/v1/questions/{id}/collect    - 收藏
GET    /api/v1/questions/hot             - 热门问题
GET    /api/v1/questions/search          - 搜索问题
```

**Redis缓存策略:**
```java
// 问题详情缓存
@Cacheable(value = "question:detail", key = "#id")
public QuestionDetail getQuestionDetail(Long id) {
    // 查询数据库
    // 返回详情
}

// 问题列表分页缓存
String cacheKey = "question:list:page:" + pageNum;
redisUtil.set(cacheKey, questionList, 5, TimeUnit.MINUTES);

// 热门问题缓存
String hotKey = "question:hot";
redisUtil.set(hotKey, hotQuestions, 10, TimeUnit.MINUTES);
```

#### 3.4 回答模块 ✅
- [ ] 回答问题接口
- [ ] 获取回答列表接口
- [ ] 更新回答接口
- [ ] 删除回答接口
- [ ] 采纳回答接口
- [ ] 回答点赞接口

**接口:**
```
POST   /api/v1/questions/{id}/answer   - 回答问题
GET    /api/v1/questions/{id}/answers  - 获取回答列表
PUT    /api/v1/answers/{id}            - 更新回答
DELETE /api/v1/answers/{id}            - 删除回答
POST   /api/v1/answers/{id}/accept     - 采纳回答
POST   /api/v1/answers/{id}/like       - 点赞回答
```

#### 3.5 问答前端页面 ✅
- [ ] 问题广场页面（列表 + 筛选 + 搜索）
- [ ] 问题详情页面（问题内容 + 回答列表 + 评论）
- [ ] 发布问题页面（富文本编辑器 + 图片上传）
- [ ] 答疑中心页面（教师查看待回答问题）
- [ ] 回答编辑页面（富文本编辑器）

**问题广场页面结构:**
```vue
<template>
  <div class="question-list">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="subjectId">科目</el-select>
      <el-select v-model="status">状态</el-select>
      <el-input v-model="keyword">搜索</el-input>
    </div>
    
    <!-- 问题列表 -->
    <div class="question-item" v-for="item in questionList">
      <img :src="item.coverImage" />
      <h3>{{ item.title }}</h3>
      <p>{{ item.content }}</p>
      <div class="meta">
        <span>{{ item.studentName }}</span>
        <span>{{ item.viewCount }}浏览</span>
        <span>{{ item.answerCount }}回答</span>
      </div>
    </div>
    
    <!-- 分页 -->
    <el-pagination />
  </div>
</template>
```

**问题详情页面结构:**
```vue
<template>
  <div class="question-detail">
    <!-- 问题内容 -->
    <div class="question-content">
      <h1>{{ question.title }}</h1>
      <div class="meta">
        <span>{{ question.studentName }}</span>
        <span>{{ question.publishTime }}</span>
        <span>{{ question.viewCount }}浏览</span>
      </div>
      <div class="content" v-html="question.content"></div>
      <div class="actions">
        <el-button @click="handleLike">点赞</el-button>
        <el-button @click="handleCollect">收藏</el-button>
      </div>
    </div>
    
    <!-- 回答列表 -->
    <div class="answer-list">
      <h3>{{ question.answerCount }}个回答</h3>
      <div class="answer-item" v-for="answer in answerList">
        <div class="teacher-info">
          <img :src="answer.teacherAvatar" />
          <span>{{ answer.teacherName }}</span>
        </div>
        <div class="content" v-html="answer.content"></div>
        <div class="actions">
          <el-button @click="handleLike">点赞</el-button>
          <el-button v-if="isOwner">采纳</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
```

#### 3.6 富文本编辑器 ✅
- [ ] 集成WangEditor
- [ ] 封装RichEditor组件
- [ ] 实现图片上传到COS
- [ ] 图片URL替换为CDN地址

**RichEditor.vue核心代码:**
```vue
<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { uploadEditorImage } from '@/api/file'

const editorConfig = {
  MENU_CONF: {
    uploadImage: {
      async customUpload(file: File, insertFn: Function) {
        const url = await uploadEditorImage(file)
        insertFn(url, '', url)
      }
    }
  }
}
</script>
```

### Phase 4: 社交互动功能 (Week 7-8)

#### 4.1 关注功能 ✅
- [ ] follow表设计
- [ ] 关注/取关接口
- [ ] 我关注的教师列表
- [ ] 关注我的学生列表
- [ ] Redis Set存储关注关系

**接口:**
```
POST   /api/v1/follows/teacher/{id}   - 关注教师
DELETE /api/v1/follows/teacher/{id}   - 取消关注
GET    /api/v1/follows/teachers       - 我关注的教师
GET    /api/v1/follows/students       - 关注我的学生（教师）
```

**Redis存储:**
```
# 学生关注的教师集合
follows:student:{studentId}:teachers    Set<TeacherId>

# 教师的粉丝（学生）集合
follows:teacher:{teacherId}:students    Set<StudentId>

# 操作
SADD follows:student:123:teachers 456   # 学生123关注教师456
SREM follows:student:123:teachers 456   # 取消关注
SISMEMBER follows:student:123:teachers 456  # 检查是否关注
```

#### 4.2 收藏功能 ✅
- [ ] collection表设计
- [ ] 收藏/取消收藏接口
- [ ] 我的收藏列表
- [ ] 收藏数统计

#### 4.3 交流区功能 ✅
- [ ] forum_post表设计
- [ ] 发布帖子接口
- [ ] 帖子列表接口
- [ ] 帖子详情接口
- [ ] 评论帖子接口
- [ ] 热帖榜单

**交流区前端页面:**
```vue
<!-- 帖子列表 -->
<template>
  <div class="forum-list">
    <!-- 发帖按钮 -->
    <el-button @click="showPostDialog = true">发布帖子</el-button>
    
    <!-- 帖子列表 -->
    <div class="post-item" v-for="post in postList">
      <div class="user-info">
        <img :src="post.userAvatar" />
        <span>{{ post.userName }}</span>
      </div>
      <h3>{{ post.title }}</h3>
      <div class="content" v-html="post.content"></div>
      <div class="meta">
        <span>{{ post.viewCount }}浏览</span>
        <span>{{ post.likeCount }}点赞</span>
        <span>{{ post.commentCount }}评论</span>
      </div>
    </div>
  </div>
</template>
```

### Phase 5: 容器化部署 + 监控 (Week 9)

#### 5.1 Docker化 ✅
- [ ] 编写后端Dockerfile
- [ ] 编写前端Dockerfile
- [ ] 编写docker-compose.yml
- [ ] 配置Nginx

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

**docker-compose.yml:**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: qa_system_v2
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  
  backend:
    build: ./qa-system-backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
  
  frontend:
    build: ./qa-system-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql-data:
```

#### 5.2 监控配置 ✅
- [ ] 配置Actuator端点
- [ ] 配置Prometheus指标
- [ ] 配置日志JSON输出
- [ ] 配置MDC追踪

**application-prod.yml监控配置:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

**Logback配置（JSON格式）:**
```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <includeContext>true</includeContext>
    <includeMdc>true</includeMdc>
    <customFields>{"app":"qa-system"}</customFields>
</encoder>
```

#### 5.3 性能测试 ✅
- [ ] JMeter压测脚本
- [ ] 测试登录接口
- [ ] 测试问题列表接口
- [ ] 测试问题详情接口
- [ ] 验证Redis缓存效果

**测试指标:**
- 并发用户: 100/500/1000
- 响应时间: P95 < 500ms, P99 < 1000ms
- 错误率: < 0.1%
- Redis缓存命中率: > 80%

---

## 🚀 快速开始（本地开发）

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+

### 后端启动

```bash
# 1. 克隆代码
git clone <repository>
cd qa-system-backend

# 2. 配置数据库
# 修改 src/main/resources/application-dev.yml
# 配置MySQL和Redis连接信息

# 3. 执行数据库迁移
mysql -u root -p < database/migration.sql

# 4. 安装依赖并启动
mvn clean install
mvn spring-boot:run
```

### 前端启动

```bash
# 1. 进入前端目录
cd qa-system-frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 浏览器访问
# http://localhost:3000
```

### 测试账号

```
管理员: admin / admin123
教师: teacher_001 / 123456
学生: 学生1 / 123456
```

---

## ✅ 验收标准

### Phase 1 验收
- [ ] 后端项目成功启动，端口8080
- [ ] 前端项目成功启动，端口3000
- [ ] 数据库连接成功，表结构完整
- [ ] Redis连接成功，可读写数据
- [ ] Postman测试登录接口返回Token
- [ ] 前端登录页面可以正常登录
- [ ] JWT Token验证机制生效
- [ ] 路由守卫正确拦截未登录用户

### Phase 2 验收
- [ ] 个人中心页面正确显示用户信息
- [ ] 头像上传成功，返回COS URL
- [ ] 修改资料后数据正确保存
- [ ] Redis缓存用户信息生效
- [ ] 修改资料后缓存正确失效

### Phase 3 验收
- [ ] 学生可以成功发布问题（含图片）
- [ ] 问题列表页正确显示所有问题
- [ ] 问题详情页正确显示问题和回答
- [ ] 教师可以成功回答问题
- [ ] Redis缓存问题详情生效
- [ ] 点赞、收藏功能正常
- [ ] 搜索功能正常

### Phase 4 验收
- [ ] 学生可以关注/取关教师
- [ ] 关注列表正确显示
- [ ] Redis Set正确存储关注关系
- [ ] 交流区可以发帖
- [ ] 交流区可以评论
- [ ] 热帖列表正确排序

### Phase 5 验收
- [ ] docker-compose up成功启动所有服务
- [ ] 容器间网络通信正常
- [ ] Nginx正确代理前后端请求
- [ ] Prometheus端点可访问（/actuator/prometheus）
- [ ] 日志JSON格式输出正确
- [ ] 压测通过性能指标

---

## 📚 参考文档

1. **PHASE1-重构执行计划.md** - Phase 1详细实现
2. **PHASE2-5-重构执行计划.md** - Phase 2-5详细实现
3. **前端Vue3实现详细指南.md** - 前端完整实现
4. **springboot7vkr2.sql** - 原始数据库结构
5. **现有前端代码** - src/main/resources/admin/ 和 front/

---

## 🎯 关键成功因素

1. **严格遵循文档**: 每个Phase按顺序完成，不跳跃
2. **完整测试**: 每完成一个模块立即测试，不积压问题
3. **代码规范**: 遵循Java/TypeScript编码规范
4. **Git管理**: 每个Phase提交一次，便于回滚
5. **文档同步**: 及时更新文档，记录问题和解决方案

---

## ⚠️ 常见问题与解决方案

### 1. 数据库连接失败
**问题**: `Communications link failure`  
**解决**: 
- 检查MySQL是否启动
- 检查application.yml中的数据库配置
- 确保MySQL允许远程连接

### 2. JWT Token验证失败
**问题**: `401 Unauthorized`  
**解决**:
- 检查Token是否正确携带在Header中
- 检查JWT密钥配置是否一致
- 检查Token是否过期

### 3. Redis连接失败
**问题**: `Cannot get Jedis connection`  
**解决**:
- 检查Redis是否启动
- 检查Redis配置（host, port, password）
- 检查防火墙设置

### 4. COS上传失败
**问题**: `The specified bucket does not exist`  
**解决**:
- 检查COS配置（secretId, secretKey, region, bucket）
- 确保bucket已创建
- 检查COS权限配置

### 5. 前端跨域问题
**问题**: `CORS policy blocked`  
**解决**:
- 检查后端CorsConfig配置
- 检查Vite proxy配置
- 确保allowedOrigins包含前端地址

---

## 📞 支持与联系

如有问题，请查阅详细文档或联系技术负责人。

**祝开发顺利！🎉**

