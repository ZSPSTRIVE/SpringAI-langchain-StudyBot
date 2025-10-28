# 师生答疑系统 v2.0 - 前端

## 技术栈

- **框架**: Vue 3.4 + Vite 5.0
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **UI组件**: Element Plus 2.5
- **HTTP客户端**: Axios 1.6
- **富文本编辑器**: WangEditor 5.1
- **工具库**: Day.js, js-cookie

## 快速开始

### 环境要求

- Node.js 18+
- npm 9+ 或 pnpm 8+

### 安装依赖

```bash
# 使用npm
npm install

# 或使用pnpm (推荐)
pnpm install
```

### 开发模式

```bash
npm run dev
```

访问 `http://localhost:5173`

### 生产构建

```bash
npm run build
```

构建产物在 `dist/` 目录

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
qa-system-frontend/
├── public/                     # 静态资源
├── src/
│   ├── api/                    # API接口
│   │   └── auth.js             # 认证接口
│   ├── assets/                 # 资源文件
│   │   └── styles/
│   │       └── main.scss       # 全局样式
│   ├── components/             # 公共组件
│   ├── layouts/                # 布局组件
│   │   ├── AdminLayout.vue     # 管理员布局
│   │   ├── StudentLayout.vue   # 学生布局
│   │   └── TeacherLayout.vue   # 教师布局
│   ├── router/                 # 路由配置
│   │   └── index.js
│   ├── stores/                 # Pinia状态管理
│   │   └── user.js             # 用户状态
│   ├── utils/                  # 工具函数
│   │   └── request.js          # Axios封装
│   ├── views/                  # 页面组件
│   │   ├── admin/              # 管理员页面
│   │   ├── auth/               # 认证页面
│   │   │   ├── Login.vue       # 登录
│   │   │   └── Register.vue    # 注册
│   │   ├── common/             # 公共页面
│   │   ├── forum/              # 论坛页面
│   │   ├── profile/            # 个人中心
│   │   ├── student/            # 学生页面
│   │   ├── teacher/            # 教师页面
│   │   ├── Home.vue            # 首页
│   │   └── NotFound.vue        # 404页面
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── index.html                  # HTML模板
├── package.json                # 依赖配置
└── vite.config.js              # Vite配置
```

## 功能模块

### 已完成 (Phase 1)

- ✅ 项目基础架构搭建
- ✅ 用户登录/注册
- ✅ JWT认证与路由守卫
- ✅ 三端布局（学生、教师、管理员）
- ✅ 全局状态管理
- ✅ HTTP请求封装
- ✅ 响应式设计

### 开发中 (Phase 2-5)

- ⏳ 个人中心与资料修改
- ⏳ COS图片上传
- ⏳ 问答核心功能
- ⏳ 社交互动功能
- ⏳ 交流区论坛

## 环境变量

创建 `.env.development` 文件:

```env
VITE_API_BASE_URL=http://localhost:8080
```

创建 `.env.production` 文件:

```env
VITE_API_BASE_URL=https://your-production-api.com
```

## 代码规范

项目使用 ESLint 进行代码规范检查:

```bash
npm run lint
```

## API接口

后端API基础路径: `/api/v1`

### 认证接口

- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `POST /auth/logout` - 用户登出
- `POST /auth/refresh` - 刷新Token

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 开发计划

- [x] Phase 1: 用户认证与基础架构
- [ ] Phase 2: 个人中心与COS上传
- [ ] Phase 3: 核心问答模块
- [ ] Phase 4: 社交互动功能
- [ ] Phase 5: 优化与部署

## 许可证

MIT License

