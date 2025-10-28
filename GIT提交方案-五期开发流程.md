# 📦 师生答疑系统 - Git 分期提交方案

## 🎯 项目概述
这是一个完整的前后端分离项目，采用 Spring Boot + Vue 3 技术栈。
按照实际开发流程，分5期提交到GitHub，每期包含完整的功能模块。

---

## 📋 六期开发计划

| 期数 | 阶段 | 内容 | 提交数 |
|------|------|------|--------|
| **Phase 1** | 项目初始化 | 基础架构搭建 | 3-5个 |
| **Phase 2** | 核心功能开发 | 用户认证、问答系统 | 8-12个 |
| **Phase 3** | 功能扩展 | 交流区、收藏关注 | 6-8个 |
| **Phase 4** | 后台管理 | 管理端功能 | 5-7个 |
| **Phase 5** | UI优化上线 | 响应式设计、部署 | 4-6个 |
| **Phase 6** | AI助手集成 | LangChain4j + 阿里百练 | 5-7个 |

---

## 🚀 Phase 1: 项目初始化与基础架构 (第1周)

### 1.1 初始化仓库
```bash
# 创建本地仓库
cd "D:\WorkSpace\项目\毕业设计-师生答疑系统\服务器第一版 - 副本"
git init

# 配置用户信息（使用您的GitHub账号）
git config user.name "Your Name"
git config user.email "your.email@example.com"

# 创建 .gitignore
cat > .gitignore << 'EOF'
# Java
*.class
*.jar
*.war
*.ear
target/
*.iml
.idea/

# Node
node_modules/
dist/
.vite/
.cache/

# Logs
*.log
logs/

# OS
.DS_Store
Thumbs.db

# Upload files
upload/
!upload/.gitkeep

# Environment
.env
.env.local
.env.production

# Database
*.sql.backup
EOF

# 创建 README.md
cat > README.md << 'EOF'
# 师生答疑系统

一个基于 Spring Boot + Vue 3 的现代化师生互动答疑平台。

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8.0
- Redis

### 前端
- Vue 3
- Element Plus
- Vite
- Pinia
- Vue Router

## 项目状态

🚧 开发中 - Phase 1: 基础架构搭建

## 开发计划

- [x] Phase 1: 项目初始化
- [ ] Phase 2: 核心功能开发
- [ ] Phase 3: 功能扩展
- [ ] Phase 4: 后台管理
- [ ] Phase 5: 优化上线

## 作者

[Your Name]

## 许可证

MIT
EOF

# 第一次提交
git add .gitignore README.md
git commit -m "chore: 初始化项目仓库

- 添加 .gitignore 文件
- 添加项目 README 文档
- 设置基本的项目结构"
```

### 1.2 后端基础架构
```bash
# 创建后端项目结构
git add qa-system-backend/pom.xml
git add qa-system-backend/src/main/resources/application*.yml
git add qa-system-backend/src/main/java/com/qasystem/QaSystemBackendApplication.java
git commit -m "feat(backend): 搭建 Spring Boot 基础架构

- 配置 pom.xml 依赖管理
- 添加多环境配置文件 (dev/prod)
- 创建应用主类
- 集成 Spring Boot Starter"

# 添加数据库配置
git add qa-system-backend/src/main/resources/db/schema.sql
git commit -m "feat(backend): 添加数据库架构设计

- 创建用户表 (user)
- 创建学生表 (student)
- 创建教师表 (teacher)
- 创建问题表 (question)
- 创建答案表 (answer)
- 创建科目表 (subject)
- 添加索引和外键约束"

# 添加基础配置类
git add qa-system-backend/src/main/java/com/qasystem/config/
git commit -m "feat(backend): 添加核心配置类

- WebMvcConfig: Web MVC 配置
- CorsConfig: 跨域配置
- RedisConfig: Redis 缓存配置
- MyBatisPlusConfig: 分页插件配置"

# 添加通用工具类
git add qa-system-backend/src/main/java/com/qasystem/common/
git commit -m "feat(backend): 添加通用组件和工具类

- Result: 统一响应封装
- PageUtils: 分页工具类
- RedisUtil: Redis 操作工具
- GlobalExceptionHandler: 全局异常处理"
```

### 1.3 前端基础架构
```bash
# 创建前端项目
git add qa-system-frontend/package.json
git add qa-system-frontend/vite.config.js
git add qa-system-frontend/index.html
git commit -m "feat(frontend): 初始化 Vue 3 项目

- 配置 Vite 构建工具
- 添加项目依赖 (Vue 3, Element Plus, Pinia)
- 配置开发服务器代理"

# 添加前端基础配置
git add qa-system-frontend/src/main.js
git add qa-system-frontend/src/App.vue
git add qa-system-frontend/src/router/
git add qa-system-frontend/src/stores/
git commit -m "feat(frontend): 配置前端核心模块

- 配置 Vue Router 路由
- 配置 Pinia 状态管理
- 添加全局样式系统
- 集成 Element Plus UI 框架"

# 添加 Axios 请求封装
git add qa-system-frontend/src/utils/request.js
git add qa-system-frontend/src/api/
git commit -m "feat(frontend): 封装 HTTP 请求工具

- 配置 Axios 拦截器
- 添加 Token 自动注入
- 实现统一错误处理
- 创建 API 接口模块"
```

---

## 🔐 Phase 2: 核心功能开发 (第2-4周)

### 2.1 用户认证系统
```bash
# 后端：Spring Security + JWT
git add qa-system-backend/src/main/java/com/qasystem/security/
git commit -m "feat(backend): 实现 Spring Security 安全框架

- SecurityConfig: 安全配置
- JwtAuthenticationFilter: JWT 过滤器
- JwtUtil: JWT 工具类
- 配置密码加密 (BCrypt)"

git add qa-system-backend/src/main/java/com/qasystem/controller/AuthController.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/AuthServiceImpl.java
git commit -m "feat(backend): 实现用户认证功能

- 用户登录接口
- 用户注册接口
- Token 刷新机制
- 登出功能"

# 前端：登录注册页面
git add qa-system-frontend/src/views/auth/Login.vue
git add qa-system-frontend/src/views/auth/Register.vue
git commit -m "feat(frontend): 实现登录注册界面

- 登录表单组件
- 注册表单组件
- 表单验证逻辑
- 角色选择功能"

git add qa-system-frontend/src/stores/user.js
git commit -m "feat(frontend): 实现用户状态管理

- 用户信息存储
- Token 持久化
- 登录状态检查
- 路由守卫集成"
```

### 2.2 用户实体和个人中心
```bash
# 后端：用户相关实体
git add qa-system-backend/src/main/java/com/qasystem/entity/User.java
git add qa-system-backend/src/main/java/com/qasystem/entity/Student.java
git add qa-system-backend/src/main/java/com/qasystem/entity/Teacher.java
git add qa-system-backend/src/main/java/com/qasystem/mapper/
git commit -m "feat(backend): 添加用户相关实体类

- User: 用户基础实体
- Student: 学生扩展信息
- Teacher: 教师扩展信息
- MyBatis Mapper 接口"

# 后端：个人中心功能
git add qa-system-backend/src/main/java/com/qasystem/controller/ProfileController.java
git add qa-system-backend/src/main/java/com/qasystem/service/ProfileService.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/ProfileServiceImpl.java
git commit -m "feat(backend): 实现个人中心功能

- 获取个人信息
- 更新个人资料
- 修改密码
- 头像上传"

# 前端：个人中心页面
git add qa-system-frontend/src/views/profile/Profile.vue
git add qa-system-frontend/src/components/common/ImageUpload.vue
git commit -m "feat(frontend): 实现个人中心页面

- 个人信息展示和编辑
- 头像上传组件
- 密码修改功能
- 表单验证和提交"
```

### 2.3 问答系统核心功能
```bash
# 后端：问题模块
git add qa-system-backend/src/main/java/com/qasystem/entity/Question.java
git add qa-system-backend/src/main/java/com/qasystem/entity/Subject.java
git add qa-system-backend/src/main/java/com/qasystem/controller/QuestionController.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/QuestionServiceImpl.java
git commit -m "feat(backend): 实现问题发布功能

- Question 实体类
- 问题发布接口
- 问题列表查询
- 分页和搜索功能
- 科目分类支持"

# 后端：答案模块
git add qa-system-backend/src/main/java/com/qasystem/entity/Answer.java
git add qa-system-backend/src/main/java/com/qasystem/controller/AnswerController.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/AnswerServiceImpl.java
git commit -m "feat(backend): 实现答案回复功能

- Answer 实体类
- 教师回答接口
- 答案点赞功能
- 采纳答案功能
- 答案列表查询"

# 前端：学生问答界面
git add qa-system-frontend/src/views/student/AskQuestion.vue
git add qa-system-frontend/src/views/student/MyQuestions.vue
git add qa-system-frontend/src/views/common/QuestionDetail.vue
git commit -m "feat(frontend): 实现学生问答界面

- 提问页面 (富文本编辑器)
- 我的问题列表
- 问题详情页
- 科目选择和标签功能"

# 前端：教师答疑界面
git add qa-system-frontend/src/views/teacher/AnswerQuestions.vue
git commit -m "feat(frontend): 实现教师答疑界面

- 问题列表展示
- 筛选和搜索功能
- 回答编辑器
- 答案提交功能"

# 前端：布局组件
git add qa-system-frontend/src/layouts/StudentLayout.vue
git add qa-system-frontend/src/layouts/TeacherLayout.vue
git commit -m "feat(frontend): 添加角色专属布局

- StudentLayout: 学生端布局
- TeacherLayout: 教师端布局
- 侧边导航栏
- 用户信息展示"
```

### 2.4 科目管理
```bash
# 后端：科目管理
git add qa-system-backend/src/main/java/com/qasystem/controller/SubjectController.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/SubjectServiceImpl.java
git commit -m "feat(backend): 实现科目管理功能

- 科目列表查询
- 科目详情获取
- 科目统计信息
- Redis 缓存集成"

# 前端：科目选择组件
git add qa-system-frontend/src/components/common/SubjectSelector.vue
git commit -m "feat(frontend): 添加科目选择组件

- 科目下拉选择
- 科目图标展示
- 科目描述提示"
```

---

## 🎨 Phase 3: 功能扩展 (第5-6周)

### 3.1 交流区/论坛功能
```bash
# 后端：论坛模块
git add qa-system-backend/src/main/java/com/qasystem/entity/Forum.java
git add qa-system-backend/src/main/java/com/qasystem/controller/ForumController.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/ForumServiceImpl.java
git commit -m "feat(backend): 实现交流区论坛功能

- Forum 实体类（兼容旧系统）
- 帖子发布接口
- 帖子列表查询
- 评论回复功能
- 帖子详情查询"

# 数据库：论坛表
git add qa-system-backend/src/main/resources/db/schema.sql
git commit -m "feat(database): 添加论坛表结构

- 创建 forum 表
- 支持树形结构（父子关系）
- 添加索引优化查询"

# 前端：论坛列表页
git add qa-system-frontend/src/views/forum/ForumList.vue
git commit -m "feat(frontend): 实现交流区列表页

- 帖子列表展示
- 搜索和筛选功能
- 统计数据展示
- 发帖对话框
- 导航按钮（返回上一页/首页）"

# 前端：论坛详情页
git add qa-system-frontend/src/views/forum/ForumDetail.vue
git commit -m "feat(frontend): 实现帖子详情页

- 主帖内容展示
- 评论列表展示
- 回复功能
- 编辑和删除功能
- 导航按钮"
```

### 3.2 收藏和关注功能
```bash
# 后端：收藏功能
git add qa-system-backend/src/main/java/com/qasystem/entity/Collection.java
git add qa-system-backend/src/main/java/com/qasystem/controller/CollectionController.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/CollectionServiceImpl.java
git commit -m "feat(backend): 实现收藏功能

- 收藏问题
- 取消收藏
- 我的收藏列表
- 收藏状态查询"

# 后端：关注功能
git add qa-system-backend/src/main/java/com/qasystem/entity/Follow.java
git add qa-system-backend/src/main/java/com/qasystem/controller/FollowController.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/FollowServiceImpl.java
git commit -m "feat(backend): 实现关注功能

- 关注教师
- 取消关注
- 关注列表查询
- 粉丝统计"

# 前端：收藏和关注页面
git add qa-system-frontend/src/views/student/Collections.vue
git add qa-system-frontend/src/views/student/Following.vue
git commit -m "feat(frontend): 实现收藏和关注页面

- 收藏列表展示
- 关注教师列表
- 取消操作
- 列表筛选和搜索"
```

### 3.3 首页和通用组件
```bash
# 前端：首页设计
git add qa-system-frontend/src/views/Home.vue
git commit -m "feat(frontend): 实现系统首页

- 欢迎页面设计
- 功能入口展示
- 角色导航引导
- 响应式布局"

# 前端：通用组件库
git add qa-system-frontend/src/components/common/PageHeader.vue
git add qa-system-frontend/src/components/common/EmptyState.vue
git add qa-system-frontend/src/components/common/LoadingState.vue
git commit -m "feat(frontend): 添加通用 UI 组件

- PageHeader: 页面头部组件
- EmptyState: 空状态组件
- LoadingState: 加载状态组件
- 统一的视觉风格"
```

---

## 👨‍💼 Phase 4: 后台管理系统 (第7-8周)

### 4.1 管理员认证和布局
```bash
# 后端：管理员控制器
git add qa-system-backend/src/main/java/com/qasystem/controller/AdminController.java
git add qa-system-backend/src/main/java/com/qasystem/service/AdminService.java
git add qa-system-backend/src/main/java/com/qasystem/service/impl/AdminServiceImpl.java
git commit -m "feat(backend): 实现管理员功能基础

- AdminController: 管理端控制器
- 统计数据接口
- 用户管理接口
- 角色权限验证"

# 前端：管理员布局
git add qa-system-frontend/src/layouts/AdminLayout.vue
git commit -m "feat(frontend): 实现管理员布局

- 侧边栏导航
- 顶部导航栏
- 菜单折叠功能
- 用户下拉菜单
- 响应式设计"
```

### 4.2 数据统计和可视化
```bash
# 后端：统计接口
git add qa-system-backend/src/main/java/com/qasystem/dto/StatisticsDTO.java
git commit -m "feat(backend): 添加统计数据接口

- 用户统计（学生/教师数量）
- 问题统计（总数/已解决/待解决）
- 今日数据统计
- 活跃度统计"

# 前端：数据仪表板
git add qa-system-frontend/src/views/admin/Dashboard.vue
git commit -m "feat(frontend): 实现数据统计仪表板

- 统计卡片展示
- 数据可视化
- 实时数据刷新
- 趋势图表（可选）"
```

### 4.3 用户管理功能
```bash
# 后端：用户管理增强
git add qa-system-backend/src/main/java/com/qasystem/dto/CreateUserRequest.java
git add qa-system-backend/src/main/java/com/qasystem/dto/ResetPasswordRequest.java
git commit -m "feat(backend): 添加用户管理功能

- 创建学生账号接口
- 创建教师账号接口
- 重置密码接口（手动输入）
- 用户状态管理接口
- 用户删除接口"

# 前端：学生管理页面
git add qa-system-frontend/src/views/admin/Students.vue
git commit -m "feat(frontend): 实现学生管理页面

- 学生列表展示
- 添加学生功能
- 编辑学生信息
- 重置密码功能
- 状态切换和删除"

# 前端：教师管理页面
git add qa-system-frontend/src/views/admin/Teachers.vue
git commit -m "feat(frontend): 实现教师管理页面

- 教师列表展示
- 添加教师功能
- 编辑教师信息
- 重置密码功能
- 状态管理"
```

### 4.4 科目和问题管理
```bash
# 前端：科目管理页面
git add qa-system-frontend/src/views/admin/Subjects.vue
git commit -m "feat(frontend): 实现科目管理页面

- 科目列表展示
- 添加科目功能
- 编辑科目信息
- 科目状态管理
- 删除科目"

# 前端：问题管理页面
git add qa-system-frontend/src/views/admin/Questions.vue
git commit -m "feat(frontend): 实现问题管理页面

- 问题列表展示
- 问题筛选和搜索
- 问题详情查看
- 问题状态管理
- 问题删除功能"
```

---

## 🎨 Phase 5: UI优化与部署准备 (第9-10周)

### 5.1 全局样式系统
```bash
# 前端：设计系统
git add qa-system-frontend/src/assets/styles/variables.scss
git add qa-system-frontend/src/assets/styles/utils.scss
git add qa-system-frontend/src/assets/styles/main.scss
git commit -m "style(frontend): 实现设计系统

- 设计令牌（颜色、字体、间距）
- 工具类（布局、文本、卡片）
- 全局样式重置
- Element Plus 主题定制
- 禁用渐变色，使用纯色设计"
```

### 5.2 响应式优化
```bash
# 前端：响应式设计
git add qa-system-frontend/src/layouts/AdminLayout.vue
git add qa-system-frontend/src/layouts/StudentLayout.vue
git add qa-system-frontend/src/layouts/TeacherLayout.vue
git commit -m "style(frontend): 全面优化响应式设计

- 适配桌面、平板、手机
- 移动端菜单优化
- 断点配置（5个尺寸）
- Mac Retina 屏幕优化
- 打印样式优化"
```

### 5.3 性能优化和错误修复
```bash
# 后端：性能优化
git commit -m "perf(backend): 后端性能优化

- Redis 缓存优化
- 数据库索引优化
- N+1 查询优化
- 分页性能提升
- 日志级别调整"

# 前端：性能优化
git commit -m "perf(frontend): 前端性能优化

- 路由懒加载
- 组件按需加载
- 图片懒加载
- Webpack 打包优化
- Gzip 压缩配置"

# 修复已知问题
git commit -m "fix: 修复密码重置BUG

- 添加 Redis 缓存清除
- 重置密码后立即生效
- 详细的操作日志
- 异常处理优化"

git commit -m "fix: 修复菜单折叠问题

- 图标和文字对齐优化
- 折叠状态样式完善
- Tooltip 提示添加
- 过渡动画优化"
```

### 5.4 部署配置
```bash
# Docker 配置
git add qa-system-backend/Dockerfile
git add docker-compose.yml
git add nginx/nginx.conf
git commit -m "chore: 添加 Docker 部署配置

- 后端 Dockerfile
- Docker Compose 编排
- Nginx 反向代理配置
- 环境变量配置"

# 启动脚本
git add start.sh
git add start.bat
git commit -m "chore: 添加启动脚本

- Linux/Mac 启动脚本
- Windows 启动脚本
- 环境检查
- 自动化部署"

# 文档完善
git add README.md
git add QUICK_START_GUIDE.md
git add TEST_ACCOUNTS.md
git commit -m "docs: 完善项目文档

- 更新 README
- 添加快速开始指南
- 添加测试账号说明
- 添加开发文档"
```

### 5.5 最终发布
```bash
# 创建版本标签
git tag -a v1.0.0 -m "Release version 1.0.0

功能列表：
- 用户认证系统（登录/注册/JWT）
- 学生提问功能
- 教师答疑功能
- 交流区论坛
- 收藏和关注
- 个人中心
- 后台管理系统
- 完整的响应式设计

技术栈：
- 后端：Spring Boot 3 + MyBatis-Plus + Redis
- 前端：Vue 3 + Element Plus + Pinia
- 数据库：MySQL 8.0

部署：
- Docker 容器化
- Nginx 反向代理"

# 推送到 GitHub
git push origin main
git push origin --tags
```

---

## 📝 完整的Git命令执行流程

### 步骤1：创建 GitHub 仓库
```bash
# 在 GitHub 上创建新仓库: qa-system
# 不要初始化 README、.gitignore 或 license
```

### 步骤2：本地初始化并首次推送
```bash
# 1. 初始化本地仓库
cd "D:\WorkSpace\项目\毕业设计-师生答疑系统\服务器第一版 - 副本"
git init

# 2. 配置用户信息
git config user.name "Your Name"
git config user.email "your.email@example.com"

# 3. 设置主分支名称
git branch -M main

# 4. 添加远程仓库
git remote add origin https://github.com/yourusername/qa-system.git

# 5. 执行 Phase 1 的所有提交（见上文）
# ...

# 6. 首次推送
git push -u origin main
```

### 步骤3：分批次提交（每周推送一次）

#### **第1周结束（Phase 1 完成）**
```bash
# 推送 Phase 1 的所有提交
git push origin main

# 创建 Phase 1 标签
git tag -a v0.1.0 -m "Phase 1: 基础架构搭建完成"
git push origin v0.1.0
```

#### **第2-4周结束（Phase 2 完成）**
```bash
# 推送 Phase 2 的所有提交
git push origin main

# 创建 Phase 2 标签
git tag -a v0.3.0 -m "Phase 2: 核心功能开发完成

- 用户认证系统
- 问答功能
- 个人中心"
git push origin v0.3.0
```

#### **第5-6周结束（Phase 3 完成）**
```bash
# 推送 Phase 3 的所有提交
git push origin main

# 创建 Phase 3 标签
git tag -a v0.5.0 -m "Phase 3: 功能扩展完成

- 交流区论坛
- 收藏关注功能
- 通用组件库"
git push origin v0.5.0
```

#### **第7-8周结束（Phase 4 完成）**
```bash
# 推送 Phase 4 的所有提交
git push origin main

# 创建 Phase 4 标签
git tag -a v0.8.0 -m "Phase 4: 后台管理系统完成

- 数据统计
- 用户管理
- 科目管理
- 问题管理"
git push origin v0.8.0
```

#### **第9-10周结束（Phase 5 完成 - 正式发布）**
```bash
# 推送 Phase 5 的所有提交
git push origin main

# 创建正式发布标签
git tag -a v1.0.0 -m "Release v1.0.0: 项目正式发布"
git push origin v1.0.0

# 创建 Release（在 GitHub 网页上操作）
# 1. 进入仓库的 Releases 页面
# 2. 点击 "Create a new release"
# 3. 选择标签 v1.0.0
# 4. 填写发布说明
# 5. 上传部署包（可选）
# 6. 点击 "Publish release"
```

---

## 🔄 日常开发工作流

### 功能开发流程
```bash
# 1. 创建功能分支
git checkout -b feature/user-authentication

# 2. 开发功能，多次提交
git add .
git commit -m "feat: 添加用户登录接口"

git add .
git commit -m "feat: 添加登录表单验证"

git add .
git commit -m "test: 添加登录功能测试"

# 3. 合并到主分支
git checkout main
git merge feature/user-authentication

# 4. 推送到远程
git push origin main

# 5. 删除功能分支
git branch -d feature/user-authentication
```

### BUG 修复流程
```bash
# 1. 创建修复分支
git checkout -b fix/password-reset-bug

# 2. 修复 BUG
git add .
git commit -m "fix: 修复密码重置后无法登录的问题

- 添加 Redis 缓存清除
- 优化密码加密逻辑
- 更新相关测试"

# 3. 合并并推送
git checkout main
git merge fix/password-reset-bug
git push origin main
git branch -d fix/password-reset-bug
```

---

## 📊 Commit 规范 (约定式提交)

### 格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型
- `feat`: 新功能
- `fix`: 修复 BUG
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构（既不是新功能也不是修复BUG）
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具链相关
- `revert`: 回退

### Scope 范围
- `backend`: 后端
- `frontend`: 前端
- `database`: 数据库
- `deploy`: 部署相关
- `docs`: 文档

### 示例
```bash
# 好的提交信息
git commit -m "feat(backend): 添加用户登录接口

- 实现 JWT Token 生成
- 添加密码验证逻辑
- 集成 Redis 缓存

Closes #123"

# 不好的提交信息
git commit -m "update"
git commit -m "fix bug"
git commit -m "修改"
```

---

## 🎯 最佳实践

### 1. 提交频率
- ✅ 每完成一个小功能点就提交
- ✅ 每天至少提交 2-3 次
- ❌ 不要几天写完再一次性提交
- ❌ 不要混合多个不相关的修改

### 2. 提交粒度
- ✅ 一个提交只做一件事
- ✅ 提交信息清晰描述做了什么
- ❌ 不要把多个功能混在一个提交里
- ❌ 不要提交未完成或有明显错误的代码

### 3. 分支管理
```bash
# 主分支保护
main        # 稳定版本，只接受合并
develop     # 开发分支（可选）
feature/*   # 功能分支
fix/*       # 修复分支
release/*   # 发布分支
```

### 4. 代码审查
```bash
# 使用 Pull Request 进行代码审查
git push origin feature/new-feature
# 然后在 GitHub 上创建 PR
```

---

## 🚀 一键执行脚本

### Phase 1 自动化脚本
```bash
#!/bin/bash
# phase1-init.sh

echo "🚀 开始 Phase 1: 项目初始化"

# 初始化仓库
git init
git config user.name "Your Name"
git config user.email "your.email@example.com"
git branch -M main

# 添加 .gitignore 和 README
cat > .gitignore << 'EOF'
# Java
*.class
*.jar
*.war
target/
*.iml
.idea/

# Node
node_modules/
dist/

# Logs
*.log

# Upload
upload/
!upload/.gitkeep
EOF

cat > README.md << 'EOF'
# 师生答疑系统

🚧 开发中 - Phase 1
EOF

git add .gitignore README.md
git commit -m "chore: 初始化项目仓库"

# 添加后端
git add qa-system-backend/pom.xml qa-system-backend/src/main/resources/application*.yml
git commit -m "feat(backend): 搭建 Spring Boot 基础架构"

git add qa-system-backend/src/main/resources/db/schema.sql
git commit -m "feat(backend): 添加数据库架构设计"

git add qa-system-backend/src/main/java/com/qasystem/config/
git commit -m "feat(backend): 添加核心配置类"

git add qa-system-backend/src/main/java/com/qasystem/common/
git commit -m "feat(backend): 添加通用组件和工具类"

# 添加前端
git add qa-system-frontend/package.json qa-system-frontend/vite.config.js
git commit -m "feat(frontend): 初始化 Vue 3 项目"

git add qa-system-frontend/src/main.js qa-system-frontend/src/router/
git commit -m "feat(frontend): 配置前端核心模块"

git add qa-system-frontend/src/utils/request.js qa-system-frontend/src/api/
git commit -m "feat(frontend): 封装 HTTP 请求工具"

# 推送到 GitHub
git remote add origin https://github.com/yourusername/qa-system.git
git push -u origin main

git tag -a v0.1.0 -m "Phase 1: 基础架构搭建完成"
git push origin v0.1.0

echo "✅ Phase 1 完成！"
```

---

## 📚 参考资源

- [Git 官方文档](https://git-scm.com/doc)
- [约定式提交规范](https://www.conventionalcommits.org/zh-hans/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)
- [语义化版本](https://semver.org/lang/zh-CN/)

---

## ✅ 总结

这个方案：
1. ✅ **符合真实开发流程** - 从基础架构到功能开发再到优化上线
2. ✅ **提交粒度合理** - 每个提交都有明确的目的
3. ✅ **易于追溯** - 清晰的提交历史和标签
4. ✅ **专业规范** - 遵循业界最佳实践
5. ✅ **展示项目进度** - 通过标签和分支清晰展现开发阶段

**立即开始执行，打造一个专业的 GitHub 项目！** 🎉

---

## 🤖 Phase 6: AI助手功能集成 (第11-12周)

### 6.1 后端AI基础架构
```bash
# 添加LangChain4j依赖
git add qa-system-backend/pom.xml
git commit -m "feat(backend): 集成 LangChain4j 依赖

- 添加 langchain4j-core
- 添加 langchain4j-dashscope (阿里百练)
- 添加 langchain4j-spring-boot-starter
- 配置版本管理"

# 添加AI配置
git add qa-system-backend/src/main/resources/application.yml
git commit -m "feat(backend): 配置 AI 助手参数

- 配置阿里百练 API Key
- 设置 qwen-plus 模型
- 配置温度、超时、最大Token等参数
- 设置中文语言"

# 创建AI配置类
git add qa-system-backend/src/main/java/com/qasystem/config/LangChainConfig.java
git commit -m "feat(backend): 创建 LangChain 配置类

- 配置通义千问聊天模型 QwenChatModel
- 集成 Spring Boot 自动配置
- 配置温度、最大Token、超时等参数
- 添加详细的Bean配置"
```

### 6.2 AI数据库和实体
```bash
# 创建AI对话表
git add qa-system-backend/src/main/resources/db/V6__create_ai_conversation.sql
git commit -m "feat(database): 添加 AI 对话历史表

- 创建 ai_conversation 表
- 支持会话ID管理（多轮对话）
- 记录问题、答案、类别、推荐资源
- 添加Token消耗、收藏、用户反馈字段
- 创建必要的索引（user_id, session_id, created_at）"

# 创建AI实体类和DTO
git add qa-system-backend/src/main/java/com/qasystem/entity/AiConversation.java
git add qa-system-backend/src/main/java/com/qasystem/dto/AiChatRequest.java
git add qa-system-backend/src/main/java/com/qasystem/dto/AiChatResponse.java
git add qa-system-backend/src/main/java/com/qasystem/dto/ConversationFeedbackRequest.java
git commit -m "feat(backend): 添加 AI 助手实体和 DTO

- AiConversation: 对话历史实体（MyBatis-Plus）
- AiChatRequest: 聊天请求DTO（支持会话ID、资源推荐标识）
- AiChatResponse: 聊天响应DTO（包含资源推荐、分类、Token统计）
- ConversationFeedbackRequest: 用户反馈DTO
- 支持学习资源推荐结构（ResourceRecommendation）"
```

### 6.3 AI核心服务实现
```bash
# 创建Mapper接口
git add qa-system-backend/src/main/java/com/qasystem/mapper/AiConversationMapper.java
git commit -m "feat(backend): 添加 AI 对话数据访问层

- AiConversationMapper: MyBatis-Plus Mapper
- 查询会话历史方法
- 查询用户最近对话方法
- 支持分页和排序"

# 创建AI服务
git add qa-system-backend/src/main/java/com/qasystem/service/AiAssistantService.java
git commit -m "feat(backend): 实现 AI 助手核心服务

- 多轮对话支持（保持上下文，最近10轮）
- 智能问题分类（技术问题、学科问题、课程问题等）
- 学习资源自动推荐（菜鸟教程、MDN等）
- Redis 缓存优化（24小时缓存历史）
- 系统提示词配置（专业教育助手）
- 对话历史管理（数据库+缓存双层存储）
- 收藏和反馈功能
- 基于MyBatis-Plus的ServiceImpl"
```

### 6.4 AI API接口
```bash
# 创建AI控制器
git add qa-system-backend/src/main/java/com/qasystem/controller/AiAssistantController.java
git commit -m "feat(backend): 实现 AI 助手 REST API

- POST /api/ai/chat - 与AI对话
- GET /api/ai/sessions - 获取用户会话列表
- GET /api/ai/sessions/{sessionId}/history - 获取会话历史
- POST /api/ai/feedback - 提交用户反馈
- POST /api/ai/bookmark/{conversationId} - 收藏/取消收藏对话
- GET /api/ai/bookmarks - 获取收藏的对话
- 完整的JWT认证和参数验证"

# 验证Security权限（已在现有配置中）
# AI接口使用统一的JWT认证
# 所有已登录用户都可以访问AI助手功能
```

### 6.5 前端AI聊天界面
```bash
# 前端API封装
git add qa-system-frontend/src/api/ai.js
git commit -m "feat(frontend): 封装 AI 助手 API

- chatWithAi: 发送消息
- getUserSessions: 获取用户会话列表
- getSessionHistory: 获取会话历史
- submitFeedback: 提交用户反馈
- bookmarkConversation: 收藏对话
- getBookmarkedConversations: 获取收藏列表"

# 状态管理（使用组件内状态管理）
# AI聊天界面使用Vue 3 Composition API
# ref/reactive 进行状态管理
# 无需单独的Pinia Store

# 前端依赖
git add qa-system-frontend/package.json
git commit -m "feat(frontend): 添加 AI 聊天所需依赖

- marked: Markdown渲染
- highlight.js: 代码高亮
- clipboard: 代码复制功能"
```

### 6.6 ChatGPT风格聊天界面
```bash
# 主聊天页面（一体化组件）
git add qa-system-frontend/src/views/common/AiAssistant.vue
git commit -m "feat(frontend): 实现 ChatGPT 风格 AI 助手界面

🎨 界面设计：
- 仿ChatGPT/DeepSeek三栏布局
- 侧边栏会话历史（可折叠）
- 主聊天区域（消息流展示）
- 底部输入区域（多行输入）

✨ 核心功能：
- 用户消息和AI消息区分显示
- Markdown渲染（使用marked库）
- 代码高亮（使用highlight.js）
- 打字动画效果
- 学习资源推荐卡片
- 消息操作（复制、收藏、反馈）
- 会话管理（新建、切换、清空）

📱 响应式设计：
- 移动端适配
- 侧边栏响应式折叠
- 消息气泡自适应
- 欢迎页面

🎯 交互优化：
- Ctrl+Enter 快捷发送
- 自动滚动到最新消息
- 收藏对话对话框
- 时间相对显示（dayjs）
- 复制到剪贴板功能"
```

### 6.7 路由和导航集成
```bash
# 路由配置
git add qa-system-frontend/src/router/index.js
git commit -m "feat(frontend): 添加 AI 助手路由

- /student/ai-assistant: 学生端AI助手
- /teacher/ai-assistant: 教师端AI助手
- 路由守卫保护（需要认证）
- 懒加载配置（动态导入）"

# 导航菜单
git add qa-system-frontend/src/layouts/StudentLayout.vue
git add qa-system-frontend/src/layouts/TeacherLayout.vue
git commit -m "feat(frontend): 在导航栏添加 AI 助手入口

- 学生端AI助手菜单项（Opportunity图标）
- 教师端AI助手菜单项（Opportunity图标）
- 响应式菜单布局
- 与其他功能无缝集成"
```

### 6.8 测试和文档
```bash
# API文档
git add docs/AI-ASSISTANT-API.md
git commit -m "docs: 添加 AI 助手 API 文档

- 详细的接口说明
- 请求/响应示例
- 错误码说明
- 使用场景说明"

# 用户手册
git add docs/AI-ASSISTANT-USER-GUIDE.md
git commit -m "docs: 添加 AI 助手使用指南

- 功能介绍
- 使用步骤
- 常见问题
- 最佳实践"

# 更新README
git add README.md
git commit -m "docs: 更新 README 添加 AI 助手功能

- 添加LangChain4j技术栈
- 更新功能列表
- 添加AI助手截图
- 更新开发计划"
```

### 6.9 Phase 6 完成标签
```bash
# 推送 Phase 6 的所有提交
git push origin main

# 创建 Phase 6 标签
git tag -a v1.2.0 -m "Phase 6: AI 助手功能完成

核心功能：
- 集成 LangChain4j + 阿里百练 API
- 通义千问 qwen-plus 模型
- 多轮对话支持（保持上下文）
- 智能问题分类（6大计算机课程类别）
- 学习资源自动推荐
- ChatGPT 风格聊天界面
- Markdown + 代码高亮渲染
- Redis 缓存优化
- 用户反馈机制

技术亮点：
- Spring Boot + LangChain4j 无缝集成
- Vue 3 + Pinia 状态管理
- 完整的前后端分离架构
- 响应式设计，多设备适配
- 专业的计算机课程知识库

适用场景：
- 学生学习辅导
- 课程问题解答
- 学习资源推荐
- 24/7 智能助手"

git push origin v1.2.0
```

---

## 📊 更新后的项目进度表

| Phase | 任务 | 状态 | 版本 | 周期 |
|-------|------|------|------|------|
| 1 | 项目初始化 | ✅ 完成 | v0.1.0 | 第1周 |
| 2 | 核心功能开发 | ✅ 完成 | v0.3.0 | 第2-4周 |
| 3 | 功能扩展 | ✅ 完成 | v0.5.0 | 第5-6周 |
| 4 | 后台管理 | ✅ 完成 | v0.8.0 | 第7-8周 |
| 5 | UI优化上线 | ✅ 完成 | v1.0.0 | 第9-10周 |
| 6 | AI助手集成 | 🚀 进行中 | v1.2.0 | 第11-12周 |

---

## 🎯 Phase 6 最终发布

```bash
# 最终版本标签
git tag -a v2.0.0 -m "Release v2.0.0: AI 增强版正式发布

完整功能列表：
✅ 用户认证系统（登录/注册/JWT）
✅ 学生提问功能
✅ 教师答疑功能
✅ 交流区论坛
✅ 收藏和关注
✅ 个人中心
✅ 后台管理系统
✅ 完整的响应式设计
✅ AI 智能助手（NEW!）
  - 计算机专业课程辅导
  - 多轮对话支持
  - 学习资源推荐
  - ChatGPT 风格界面

技术栈升级：
- 后端：Spring Boot 3 + MyBatis-Plus + Redis + LangChain4j
- 前端：Vue 3 + Element Plus + Pinia + Marked
- AI：阿里百练 qwen-plus 模型
- 数据库：MySQL 8.0

部署方案：
- Docker 容器化
- Nginx 反向代理
- CI/CD 自动化部署"

git push origin v2.0.0
```

---

## 🎓 AI助手功能亮点

### 1. 智能化学习辅导
- ✅ 专注计算机专业课程
- ✅ 6大课程领域覆盖（数据结构、算法、OS、数据库、网络、编程）
- ✅ 精准的问题分类
- ✅ 专业的系统提示词

### 2. 多轮对话体验
- ✅ 会话ID管理
- ✅ 保持上下文（最近5轮）
- ✅ 智能追问支持
- ✅ 对话历史保存

### 3. 学习资源推荐
- ✅ 根据问题类别推荐
- ✅ 书籍、视频、在线资源
- ✅ 经典教材和工具推荐
- ✅ 精准的资源描述

### 4. 性能优化
- ✅ Redis 缓存（60分钟）
- ✅ 响应时间记录
- ✅ 异步处理
- ✅ 错误重试机制

### 5. 用户体验
- ✅ ChatGPT 风格界面
- ✅ Markdown 渲染
- ✅ 代码语法高亮
- ✅ 一键复制代码
- ✅ 实时打字效果
- ✅ 用户反馈机制

---

**AI增强版师生答疑系统 - 让学习更智能！** 🤖🎓

