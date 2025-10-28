<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <!-- 左侧展示区 -->
    <div class="login-showcase">
      <div class="showcase-content">
        <div class="logo-section">
          <el-icon class="logo-icon"><School /></el-icon>
          <h1 class="system-title">师生答疑系统</h1>
          <p class="system-slogan">连接师生 · 分享知识 · 共同成长</p>
        </div>
        
        <div class="feature-list">
          <div class="feature-item" v-for="(feature, index) in features" :key="index">
            <div class="feature-icon">
              <el-icon :size="32"><component :is="feature.icon" /></el-icon>
            </div>
            <div class="feature-content">
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-box">
      <div class="login-header">
        <h2>欢迎回来</h2>
        <p>登录你的账号</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <router-link to="/register" class="register-link">
            还没有账号？立即注册
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Lock, 
  School,
  ChatDotRound,
  TrendCharts,
  Lock as ShieldIcon
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login as loginApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const features = [
  {
    icon: 'ChatDotRound',
    title: '智能问答',
    desc: '快速获得专业解答，提升学习效率'
  },
  {
    icon: 'TrendCharts',
    title: '知识管理',
    desc: '系统化管理学习资源，构建知识体系'
  },
  {
    icon: 'Lock',
    title: '安全可靠',
    desc: '严格的权限管理，保护用户隐私'
  }
]

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await loginApi(loginForm)
        
        console.log('登录响应:', res)
        console.log('登录数据:', res.data)
        
        // 保存用户信息和Token
        userStore.login(res.data)
        
        // 验证保存
        console.log('保存后的token:', userStore.token)
        console.log('保存后的userInfo:', userStore.userInfo)
        
        ElMessage.success('登录成功')
        
        // 延迟跳转，确保数据保存完成
        setTimeout(() => {
          const redirect = route.query.redirect || getDefaultRoute(res.data.userInfo.role)
          console.log('准备跳转到:', redirect)
          router.push(redirect)
        }, 100)
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error(error.response?.data?.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const getDefaultRoute = (role) => {
  switch (role) {
    case 'ADMIN':
      return '/admin'
    case 'TEACHER':
      return '/teacher'
    case 'STUDENT':
      return '/student'
    default:
      return '/home'
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  min-height: 100vh;
  background: #0f172a;
  position: relative;
  overflow: hidden;
}

// 背景装饰
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  
  .shape {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.3;
    animation: float 15s ease-in-out infinite;
    
    &.shape-1 {
      width: 500px;
      height: 500px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      top: -100px;
      left: -100px;
      animation-delay: 0s;
    }
    
    &.shape-2 {
      width: 400px;
      height: 400px;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      bottom: -100px;
      right: -100px;
      animation-delay: 3s;
    }
    
    &.shape-3 {
      width: 350px;
      height: 350px;
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      top: 50%;
      right: 20%;
      animation-delay: 6s;
    }
    
    &.shape-4 {
      width: 300px;
      height: 300px;
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      bottom: 20%;
      left: 30%;
      animation-delay: 9s;
    }
  }
}

// 左侧展示区
.login-showcase {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
  
  .showcase-content {
    max-width: 600px;
    color: white;
    
    .logo-section {
      text-align: center;
      margin-bottom: 60px;
      
      .logo-icon {
        font-size: 80px;
        margin-bottom: 24px;
        animation: pulse 3s ease-in-out infinite;
      }
      
      .system-title {
        font-size: 48px;
        font-weight: 700;
        margin: 0 0 16px 0;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .system-slogan {
        font-size: 18px;
        opacity: 0.9;
        letter-spacing: 2px;
        margin: 0;
      }
    }
    
    .feature-list {
      display: flex;
      flex-direction: column;
      gap: 24px;
      
      .feature-item {
        display: flex;
        align-items: flex-start;
        gap: 20px;
        padding: 24px;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 16px;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.1);
        transition: all 0.3s;
        
        &:hover {
          transform: translateX(10px);
          background: rgba(255, 255, 255, 0.1);
          box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
        }
        
        .feature-icon {
          flex-shrink: 0;
          width: 64px;
          height: 64px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 16px;
          box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
        }
        
        .feature-content {
          h3 {
            font-size: 20px;
            font-weight: 600;
            margin: 0 0 8px 0;
          }
          
          p {
            font-size: 15px;
            opacity: 0.8;
            margin: 0;
            line-height: 1.6;
          }
        }
      }
    }
  }
}

// 右侧登录区
.login-box {
  width: 480px;
  padding: 60px;
  background: white;
  box-shadow: -20px 0 60px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  h2 {
    margin: 0;
    font-size: 32px;
    color: #1f2937;
    font-weight: 700;
  }

  p {
    margin: 12px 0 0;
    color: #6b7280;
    font-size: 16px;
  }
}

.login-form {
  .login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
    }
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  .register-link {
    color: #667eea;
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s;

    &:hover {
      color: #764ba2;
      text-decoration: underline;
    }
  }
}

// 动画
@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(5deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(-5deg);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

// 响应式
@media (max-width: 1024px) {
  .login-showcase {
    display: none;
  }
  
  .login-box {
    width: 100%;
    max-width: 480px;
    margin: 0 auto;
  }
}

@media (max-width: 768px) {
  .login-box {
    padding: 40px 24px;
  }
}
</style>


