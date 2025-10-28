# 前端Vue 3实现详细指南

## 📱 前端架构设计

### 技术栈
- **框架**: Vue 3.3+ (Composition API)
- **构建工具**: Vite 5.0+
- **状态管理**: Pinia 2.0+
- **路由**: Vue Router 4.0+
- **UI组件库**: Element Plus
- **HTTP客户端**: Axios
- **类型系统**: TypeScript 5.0+
- **样式**: SCSS
- **工具库**: @vueuse/core, dayjs, lodash-es

### 设计原则
1. **组件化**: 高度组件化，单一职责
2. **类型安全**: 全面使用TypeScript
3. **响应式**: PC端优先，兼顾移动端
4. **性能优化**: 路由懒加载、组件按需引入
5. **用户体验**: Loading反馈、错误提示、骨架屏

---

## 🎨 项目初始化

### Step 1: 创建项目

```bash
# 创建项目
npm create vite@latest qa-system-frontend -- --template vue-ts
cd qa-system-frontend

# 安装核心依赖
npm install

# 安装项目依赖
npm install vue-router@4 pinia axios element-plus
npm install @element-plus/icons-vue @vueuse/core dayjs lodash-es

# 安装开发依赖
npm install -D @types/lodash-es @types/node
npm install -D sass unplugin-vue-components unplugin-auto-import
npm install -D @iconify/vue @iconify-json/ep
```

### Step 2: 配置vite.config.ts

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
    
    // Element Plus 按需导入
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: [
        'vue',
        'vue-router',
        'pinia',
        '@vueuse/core'
      ],
      dts: 'src/types/auto-imports.d.ts',
      eslintrc: {
        enabled: true
      }
    }),
    
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/types/components.d.ts',
      dirs: ['src/components']
    })
  ],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@views': resolve(__dirname, 'src/views'),
      '@api': resolve(__dirname, 'src/api'),
      '@utils': resolve(__dirname, 'src/utils'),
      '@stores': resolve(__dirname, 'src/stores'),
      '@types': resolve(__dirname, 'src/types')
    }
  },
  
  server: {
    port: 3000,
    host: true,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  },
  
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'utils': ['axios', 'dayjs', 'lodash-es']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
})
```

### Step 3: TypeScript配置

**tsconfig.json:**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["src/components/*"],
      "@views/*": ["src/views/*"],
      "@api/*": ["src/api/*"],
      "@utils/*": ["src/utils/*"],
      "@stores/*": ["src/stores/*"],
      "@types/*": ["src/types/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

---

## 🔧 核心工具封装

### 1. Axios请求封装

**src/utils/request.ts:**

```typescript
import axios, { 
  AxiosInstance, 
  AxiosRequestConfig, 
  AxiosResponse, 
  AxiosError 
} from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'

// 响应数据结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

// 请求配置扩展
interface RequestConfig extends AxiosRequestConfig {
  hideLoading?: boolean  // 是否隐藏Loading
  showError?: boolean    // 是否显示错误提示
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// Loading管理
let loadingInstance: LoadingInstance | null = null
let requestCount = 0

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
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    return config
  },
  (error: AxiosError) => {
    hideLoading()
    console.error('请求错误：', error)
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    hideLoading()
    
    const res = response.data
    const config: any = response.config
    
    // 如果是下载文件，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 成功响应
    if (res.code === 200 || res.code === 0) {
      return res.data
    }
    
    // Token过期或无效
    if (res.code === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      removeToken()
      router.push('/login')
      return Promise.reject(new Error('Token过期'))
    }
    
    // 权限不足
    if (res.code === 403) {
      ElMessage.error('权限不足')
      return Promise.reject(new Error('权限不足'))
    }
    
    // 其他错误
    if (config.showError !== false) {
      ElMessage.error(res.message || '请求失败')
    }
    
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error: AxiosError) => {
    hideLoading()
    
    console.error('响应错误：', error)
    
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400:
          message = '请求参数错误'
          break
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
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 导出请求方法
export default service

// 便捷方法
export const request = {
  get<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return service.get(url, config)
  },
  
  post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
  
  put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return service.put(url, data, config)
  },
  
  delete<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}
```

### 2. 本地存储封装

**src/utils/storage.ts:**

```typescript
interface StorageData<T> {
  value: T
  expire?: number
}

class Storage {
  private storage: globalThis.Storage
  
  constructor(type: 'local' | 'session' = 'local') {
    this.storage = type === 'local' ? localStorage : sessionStorage
  }
  
  /**
   * 设置存储
   * @param key 键
   * @param value 值
   * @param expire 过期时间(秒)
   */
  set<T>(key: string, value: T, expire?: number): void {
    const data: StorageData<T> = {
      value,
      expire: expire ? Date.now() + expire * 1000 : undefined
    }
    this.storage.setItem(key, JSON.stringify(data))
  }
  
  /**
   * 获取存储
   * @param key 键
   * @returns 值或null
   */
  get<T>(key: string): T | null {
    const item = this.storage.getItem(key)
    if (!item) return null
    
    try {
      const data: StorageData<T> = JSON.parse(item)
      
      // 检查是否过期
      if (data.expire && data.expire < Date.now()) {
        this.remove(key)
        return null
      }
      
      return data.value
    } catch (error) {
      console.error('解析存储数据失败：', error)
      return null
    }
  }
  
  /**
   * 移除存储
   * @param key 键
   */
  remove(key: string): void {
    this.storage.removeItem(key)
  }
  
  /**
   * 清空存储
   */
  clear(): void {
    this.storage.clear()
  }
  
  /**
   * 获取所有键
   */
  keys(): string[] {
    return Object.keys(this.storage)
  }
}

// 导出实例
export const localCache = new Storage('local')
export const sessionCache = new Storage('session')

export default localCache
```

### 3. 认证工具

**src/utils/auth.ts:**

```typescript
import { localCache } from './storage'

const TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'

/**
 * 获取Token
 */
export function getToken(): string | null {
  return localCache.get<string>(TOKEN_KEY)
}

/**
 * 设置Token
 */
export function setToken(token: string): void {
  localCache.set(TOKEN_KEY, token, 7 * 24 * 3600) // 7天
}

/**
 * 移除Token
 */
export function removeToken(): void {
  localCache.remove(TOKEN_KEY)
  localCache.remove(REFRESH_TOKEN_KEY)
  localCache.remove(USER_INFO_KEY)
}

/**
 * 获取刷新Token
 */
export function getRefreshToken(): string | null {
  return localCache.get<string>(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新Token
 */
export function setRefreshToken(token: string): void {
  localCache.set(REFRESH_TOKEN_KEY, token, 30 * 24 * 3600) // 30天
}

/**
 * 获取用户信息
 */
export function getUserInfo<T>(): T | null {
  return localCache.get<T>(USER_INFO_KEY)
}

/**
 * 设置用户信息
 */
export function setUserInfo<T>(userInfo: T): void {
  localCache.set(USER_INFO_KEY, userInfo, 7 * 24 * 3600)
}

/**
 * 检查是否已登录
 */
export function isAuthenticated(): boolean {
  return !!getToken()
}
```

---

## 🗂️ Pinia状态管理

### 1. 用户状态管理

**src/stores/user.ts:**

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/user'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo } from '@/utils/auth'
import { loginApi, logoutApi, getUserInfoApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(getUserInfo<UserInfo>())
  const roles = ref<string[]>([])
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.id)
  const username = computed(() => userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const role = computed(() => userInfo.value?.role || '')
  
  /**
   * 登录
   */
  const login = async (loginForm: { username: string; password: string; role: string }) => {
    try {
      const data = await loginApi(loginForm)
      
      // 保存Token
      token.value = data.token
      setToken(data.token)
      
      if (data.refreshToken) {
        // 保存刷新Token
        setToken(data.refreshToken)
      }
      
      // 保存用户信息
      userInfo.value = data.userInfo
      setUserInfo(data.userInfo)
      
      return data
    } catch (error) {
      return Promise.reject(error)
    }
  }
  
  /**
   * 登出
   */
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败：', error)
    } finally {
      // 清除本地数据
      token.value = ''
      userInfo.value = null
      roles.value = []
      removeToken()
      
      // 跳转登录页
      router.push('/login')
    }
  }
  
  /**
   * 获取用户信息
   */
  const fetchUserInfo = async () => {
    try {
      const data = await getUserInfoApi()
      userInfo.value = data
      setUserInfo(data)
      return data
    } catch (error) {
      return Promise.reject(error)
    }
  }
  
  /**
   * 更新用户信息
   */
  const updateUserInfo = (info: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
      setUserInfo(userInfo.value)
    }
  }
  
  /**
   * 重置状态
   */
  const reset = () => {
    token.value = ''
    userInfo.value = null
    roles.value = []
    removeToken()
  }
  
  return {
    // 状态
    token,
    userInfo,
    roles,
    
    // 计算属性
    isLoggedIn,
    userId,
    username,
    avatar,
    role,
    
    // 方法
    login,
    logout,
    fetchUserInfo,
    updateUserInfo,
    reset
  }
})
```

### 2. 应用状态管理

**src/stores/app.ts:**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)
  
  // 设备类型
  const device = ref<'mobile' | 'tablet' | 'desktop'>('desktop')
  
  // 主题
  const theme = ref<'light' | 'dark'>('light')
  
  // 页面加载状态
  const pageLoading = ref(false)
  
  /**
   * 切换侧边栏
   */
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  
  /**
   * 设置设备类型
   */
  const setDevice = (type: 'mobile' | 'tablet' | 'desktop') => {
    device.value = type
  }
  
  /**
   * 切换主题
   */
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }
  
  /**
   * 设置页面加载状态
   */
  const setPageLoading = (loading: boolean) => {
    pageLoading.value = loading
  }
  
  return {
    sidebarCollapsed,
    device,
    theme,
    pageLoading,
    
    toggleSidebar,
    setDevice,
    toggleTheme,
    setPageLoading
  }
})
```

---

## 🛣️ 路由配置

### 1. 路由定义

**src/router/routes.ts:**

```typescript
import type { RouteRecordRaw } from 'vue-router'

// 布局组件
const AdminLayout = () => import('@/layouts/AdminLayout.vue')
const UserLayout = () => import('@/layouts/UserLayout.vue')
const BlankLayout = () => import('@/layouts/BlankLayout.vue')

/**
 * 公共路由（无需认证）
 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

/**
 * 管理端路由
 */
export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { title: '管理后台', roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '控制台', icon: 'dashboard' }
      },
      {
        path: 'users',
        name: 'UserManage',
        redirect: '/admin/users/students',
        meta: { title: '用户管理', icon: 'user' },
        children: [
          {
            path: 'students',
            name: 'StudentManage',
            component: () => import('@/views/admin/StudentManage.vue'),
            meta: { title: '学生管理' }
          },
          {
            path: 'teachers',
            name: 'TeacherManage',
            component: () => import('@/views/admin/TeacherManage.vue'),
            meta: { title: '教师管理' }
          }
        ]
      },
      {
        path: 'questions',
        name: 'QuestionManage',
        component: () => import('@/views/admin/QuestionManage.vue'),
        meta: { title: '问题管理', icon: 'question' }
      },
      {
        path: 'subjects',
        name: 'SubjectManage',
        component: () => import('@/views/admin/SubjectManage.vue'),
        meta: { title: '科目管理', icon: 'subject' }
      },
      {
        path: 'forum',
        name: 'ForumManage',
        component: () => import('@/views/admin/ForumManage.vue'),
        meta: { title: '交流区管理', icon: 'forum' }
      }
    ]
  }
]

/**
 * 学生端路由
 */
export const studentRoutes: RouteRecordRaw[] = [
  {
    path: '/student',
    component: UserLayout,
    redirect: '/student/home',
    meta: { title: '学生端', roles: ['STUDENT'] },
    children: [
      {
        path: 'home',
        name: 'StudentHome',
        component: () => import('@/views/student/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'questions',
        name: 'QuestionList',
        component: () => import('@/views/student/QuestionList.vue'),
        meta: { title: '问题广场' }
      },
      {
        path: 'questions/:id',
        name: 'QuestionDetail',
        component: () => import('@/views/student/QuestionDetail.vue'),
        meta: { title: '问题详情' }
      },
      {
        path: 'ask',
        name: 'AskQuestion',
        component: () => import('@/views/student/AskQuestion.vue'),
        meta: { title: '我要提问' }
      },
      {
        path: 'teachers',
        name: 'TeacherList',
        component: () => import('@/views/student/TeacherList.vue'),
        meta: { title: '教师列表' }
      },
      {
        path: 'forum',
        name: 'ForumList',
        component: () => import('@/views/student/ForumList.vue'),
        meta: { title: '交流区' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

/**
 * 教师端路由
 */
export const teacherRoutes: RouteRecordRaw[] = [
  {
    path: '/teacher',
    component: UserLayout,
    redirect: '/teacher/home',
    meta: { title: '教师端', roles: ['TEACHER'] },
    children: [
      {
        path: 'home',
        name: 'TeacherHome',
        component: () => import('@/views/teacher/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'answer-center',
        name: 'AnswerCenter',
        component: () => import('@/views/teacher/AnswerCenter.vue'),
        meta: { title: '答疑中心' }
      },
      {
        path: 'questions/:id',
        name: 'TeacherQuestionDetail',
        component: () => import('@/views/teacher/QuestionDetail.vue'),
        meta: { title: '问题详情' }
      },
      {
        path: 'my-answers',
        name: 'MyAnswers',
        component: () => import('@/views/teacher/MyAnswers.vue'),
        meta: { title: '我的回答' }
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/views/teacher/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

/**
 * 所有路由
 */
export const routes: RouteRecordRaw[] = [
  ...constantRoutes,
  ...adminRoutes,
  ...studentRoutes,
  ...teacherRoutes,
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]
```

### 2. 路由守卫

**src/router/guards.ts:**

```typescript
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/404']

export function setupRouterGuards(router: Router) {
  // 全局前置守卫
  router.beforeEach(async (to, from, next) => {
    NProgress.start()
    
    const userStore = useUserStore()
    const hasToken = userStore.token
    
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 师生答疑系统` : '师生答疑系统'
    
    if (hasToken) {
      // 已登录
      if (to.path === '/login') {
        // 如果已登录，跳转到对应角色首页
        const role = userStore.role
        if (role === 'ADMIN') {
          next('/admin/dashboard')
        } else if (role === 'TEACHER') {
          next('/teacher/home')
        } else if (role === 'STUDENT') {
          next('/student/home')
        } else {
          next('/')
        }
        NProgress.done()
      } else {
        // 检查用户信息
        if (!userStore.userInfo) {
          try {
            await userStore.fetchUserInfo()
          } catch (error) {
            // 获取用户信息失败，清除token并跳转登录
            userStore.reset()
            ElMessage.error('获取用户信息失败，请重新登录')
            next(`/login?redirect=${to.path}`)
            NProgress.done()
            return
          }
        }
        
        // 检查角色权限
        const roles = to.meta.roles as string[]
        if (roles && roles.length > 0) {
          const hasPermission = roles.includes(userStore.role)
          if (hasPermission) {
            next()
          } else {
            ElMessage.error('权限不足')
            next('/404')
            NProgress.done()
          }
        } else {
          next()
        }
      }
    } else {
      // 未登录
      if (whiteList.includes(to.path)) {
        next()
      } else {
        next(`/login?redirect=${to.path}`)
        NProgress.done()
      }
    }
  })
  
  // 全局后置守卫
  router.afterEach(() => {
    NProgress.done()
  })
  
  // 全局错误处理
  router.onError((error) => {
    console.error('路由错误：', error)
    NProgress.done()
  })
}
```

### 3. 路由初始化

**src/router/index.ts:**

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { setupRouterGuards } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 设置路由守卫
setupRouterGuards(router)

export default router
```

---

## 📦 API接口封装

### src/api/auth.ts

```typescript
import request from '@/utils/request'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types/user'

/**
 * 登录
 */
export function loginApi(data: LoginRequest) {
  return request.post<LoginResponse>('/auth/login', data)
}

/**
 * 注册
 */
export function registerApi(data: any) {
  return request.post('/auth/register', data)
}

/**
 * 登出
 */
export function logoutApi() {
  return request.post('/auth/logout')
}

/**
 * 获取用户信息
 */
export function getUserInfoApi() {
  return request.get<UserInfo>('/auth/me')
}

/**
 * 刷新Token
 */
export function refreshTokenApi(refreshToken: string) {
  return request.post('/auth/refresh', { refreshToken })
}
```

---

**完整的组件实现代码请查看下一部分文档...**

