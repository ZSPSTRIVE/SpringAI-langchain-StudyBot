<template>
  <div class="login-container">
    <!-- 浮动粒子背景 -->
    <div class="particles">
      <div
        class="orb orb--blue"
        style="top: -120px; left: -80px;"
      />
      <div
        class="orb orb--teal"
        style="top: 18%; right: -140px;"
      />
      <div
        class="orb orb--green"
        style="bottom: -160px; left: 12%;"
      />
      <div
        class="orb orb--sky"
        style="bottom: 15%; right: 12%;"
      />
      <div class="beam beam--a" />
      <div class="beam beam--b" />
      <div class="beam beam--c" />
      <div
        class="particle"
        style="left: 10%;"
      />
      <div
        class="particle"
        style="left: 25%;"
      />
      <div
        class="particle"
        style="left: 40%;"
      />
      <div
        class="particle"
        style="left: 55%;"
      />
      <div
        class="particle"
        style="left: 70%;"
      />
      <div
        class="particle"
        style="left: 85%;"
      />
      <div
        class="particle"
        style="left: 15%;"
      />
      <div
        class="particle"
        style="left: 30%;"
      />
      <div
        class="particle"
        style="left: 45%;"
      />
      <div
        class="particle"
        style="left: 60%;"
      />
      <div
        class="particle"
        style="left: 75%;"
      />
      <div
        class="particle"
        style="left: 90%;"
      />
    </div>
    
    <!-- 左侧展示区 -->
    <div class="login-showcase">
      <div class="showcase-content">
        <!-- Logo区域 -->
        <div class="logo-section">
          <div class="logo-box">
            <el-icon class="logo-icon">
              <School />
            </el-icon>
          </div>
          <h1 class="system-title">
            师生答疑系统
          </h1>
          <p class="system-slogan">
            智能化在线答疑平台
          </p>
        </div>
        
        <!-- 特性卡片 -->
        <div class="feature-grid">
          <div 
            v-for="(feature, index) in features" 
            :key="index" 
            class="feature-card"
          >
            <div
              class="feature-icon"
              :class="`feature-icon--${feature.color}`"
            >
              <el-icon :size="24">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <div class="feature-text">
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-box">
      <div class="login-card">
        <div class="brand-header">
          <div class="brand-mark">
            <el-icon><School /></el-icon>
          </div>
          <div class="brand-text">
            <div class="brand-title">
              师生答疑系统
            </div>
            <div class="brand-subtitle">
              安全登录 · 工作台体验
            </div>
          </div>
        </div>

        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>登录您的账号继续</p>
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
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>

          <div class="login-footer">
            <span class="footer-text">还没有账号？</span>
            <router-link
              to="/register"
              class="register-link"
            >
              立即注册
            </router-link>
          </div>
        </el-form>
      </div>
      
      <p class="copyright">
        © 2025 师生答疑系统
      </p>
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
  Star
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
    desc: '快速获得专业解答',
    color: 'yellow'
  },
  {
    icon: 'TrendCharts',
    title: '知识沉淀',
    desc: '构建知识体系',
    color: 'blue'
  },
  {
    icon: 'Star',
    title: '互动交流',
    desc: '促进思维碰撞',
    color: 'pink'
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
@import '@/assets/styles/variables.scss';

.login-container {
  display: flex;
  min-height: 100vh;
  background: #f2f4f8;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  position: relative;
  overflow: hidden;
  z-index: 0;
}

.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  width: 640px;
  height: 640px;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(160px);
  opacity: 0.85;
  z-index: 0;
}

.login-container::before {
  top: -200px;
  left: -200px;
  background: rgba(0, 122, 255, 0.45);
  animation: float 16s ease-in-out infinite;
}

.login-container::after {
  bottom: -220px;
  right: -220px;
  background: rgba(52, 199, 89, 0.4);
  animation: float 20s ease-in-out infinite reverse;
}

// 浮动粒子
.login-container .particles {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 1;
}

.login-container .orb {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.9;
  mix-blend-mode: screen;
  animation: orb-float 22s ease-in-out infinite;
}

.login-container .orb--blue {
  background: rgba(0, 122, 255, 0.7);
}

.login-container .orb--teal {
  background: rgba(90, 200, 250, 0.6);
  animation-duration: 26s;
}

.login-container .orb--green {
  background: rgba(52, 199, 89, 0.6);
  animation-duration: 24s;
}

.login-container .orb--sky {
  background: rgba(94, 92, 230, 0.45);
  animation-duration: 28s;
}

.login-container .beam {
  position: absolute;
  width: 560px;
  height: 140px;
  border-radius: 999px;
  background: rgba(0, 122, 255, 0.2);
  filter: blur(45px);
  opacity: 0.7;
  mix-blend-mode: screen;
  animation: beam-sweep 18s ease-in-out infinite;
}

.login-container .beam--a {
  top: -6%;
  left: -18%;
  transform: rotate(-12deg);
}

.login-container .beam--b {
  top: 28%;
  right: -24%;
  transform: rotate(18deg);
  background: rgba(90, 200, 250, 0.22);
  animation-delay: -6s;
  animation-duration: 20s;
}

.login-container .beam--c {
  bottom: -12%;
  left: 18%;
  transform: rotate(-8deg);
  background: rgba(52, 199, 89, 0.22);
  animation-delay: -10s;
  animation-duration: 22s;
}

.login-container .particle {
  position: absolute;
  width: 10px;
  height: 10px;
  background: rgba(0, 122, 255, 0.55);
  border-radius: 50%;
  animation: particle-float 10s linear infinite;
  filter: blur(0.6px);
  opacity: 0.9;
  box-shadow: 0 0 16px rgba(0, 122, 255, 0.35);
}

.login-container .particle:nth-child(2n) {
  background: rgba(4, 127, 179, 0.5);
  animation-duration: 12s;
  animation-delay: -8s;
  box-shadow: 0 0 16px rgba(4, 127, 179, 0.35);
}

.login-container .particle:nth-child(3n) {
  background: rgba(90, 200, 250, 0.55);
  animation-duration: 16s;
  animation-delay: -12s;
  box-shadow: 0 0 18px rgba(90, 200, 250, 0.35);
}

.login-container .particle:nth-child(1) { left: 6%; animation-delay: -2s; }
.login-container .particle:nth-child(2) { left: 14%; animation-delay: -6s; width: 4px; height: 4px; }
.login-container .particle:nth-child(3) { left: 22%; animation-delay: -9s; }
.login-container .particle:nth-child(4) { left: 32%; animation-delay: -3s; width: 5px; height: 5px; }
.login-container .particle:nth-child(5) { left: 44%; animation-delay: -7s; }
.login-container .particle:nth-child(6) { left: 56%; animation-delay: -11s; width: 4px; height: 4px; }
.login-container .particle:nth-child(7) { left: 64%; animation-delay: -5s; }
.login-container .particle:nth-child(8) { left: 72%; animation-delay: -12s; width: 5px; height: 5px; }
.login-container .particle:nth-child(9) { left: 80%; animation-delay: -8s; }
.login-container .particle:nth-child(10) { left: 88%; animation-delay: -10s; width: 4px; height: 4px; }
.login-container .particle:nth-child(11) { left: 18%; animation-delay: -13s; }
.login-container .particle:nth-child(12) { left: 92%; animation-delay: -15s; }

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.05);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.95);
  }
}

@keyframes particle-float {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) rotate(360deg);
    opacity: 0;
  }
}

@keyframes beam-sweep {
  0%, 100% {
    transform: translate(0, 0) scale(1) rotate(-8deg);
  }
  45% {
    transform: translate(60px, -40px) scale(1.08) rotate(6deg);
  }
  70% {
    transform: translate(-40px, 30px) scale(0.98) rotate(-4deg);
  }
}

@keyframes orb-float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  40% {
    transform: translate(40px, -30px) scale(1.05);
  }
  70% {
    transform: translate(-30px, 20px) scale(0.96);
  }
}

// ==================== 左侧展示区 ====================
.login-showcase {
  display: none;
}

// ==================== 右侧登录区 ====================
.login-box {
  width: 100%;
  max-width: 420px;
  padding: 0;
  background: transparent;
  border-left: none;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.login-card {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 20px;
  padding: 44px 40px;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.12);
  position: relative;

  .brand-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 22px;

    .brand-mark {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 14px;
      background: linear-gradient(135deg, rgba($color-primary, 0.16) 0%, rgba($color-primary, 0.08) 100%);
      border: 1px solid rgba(0, 0, 0, 0.06);
      color: $color-primary;

      :deep(.el-icon) {
        font-size: 22px;
      }
    }

    .brand-text {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .brand-title {
        font-size: 16px;
        font-weight: 700;
        color: $text-primary;
        letter-spacing: -0.01em;
      }

      .brand-subtitle {
        font-size: 12px;
        color: $text-tertiary;
      }
    }
  }

  .login-header {
    margin-bottom: 32px;

    h2 {
      margin: 0;
      font-size: 28px;
      color: $text-primary;
      font-weight: 600;
      letter-spacing: -0.02em;
    }

    p {
      margin: 8px 0 0;
      color: $text-tertiary;
      font-size: 14px;
    }
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-input) {
    .el-input__wrapper {
      padding: 4px 14px;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.92);
      box-shadow: none;
      border: 1px solid $border-color;
      transition: all 0.2s ease;

      &:hover {
        border-color: $color-gray-300;
      }

      &.is-focus {
        border-color: rgba($color-primary, 0.55);
        box-shadow: 0 10px 26px rgba($color-primary, 0.10);
      }
    }

    .el-input__inner {
      height: 44px;
      font-size: 15px;
    }

    .el-input__prefix {
      color: $text-tertiary;
    }
  }

  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    border: none;
    background: linear-gradient(180deg, lighten($color-primary, 4%) 0%, $color-primary 100%);
    transition: all 0.2s ease;

    &:hover {
      background: linear-gradient(180deg, lighten($color-primary, 2%) 0%, darken($color-primary, 6%) 100%);
      box-shadow: 0 14px 32px rgba($color-primary, 0.20);
    }

    &:active {
      box-shadow: 0 10px 22px rgba($color-primary, 0.16);
    }
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  .footer-text {
    color: $text-tertiary;
    font-size: 14px;
  }

  .register-link {
    color: $color-primary;
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
    margin-left: 6px;
    transition: color 0.2s ease;

    &:hover {
      color: darken($color-primary, 8%);
    }
  }
}

.copyright {
  position: static;
  text-align: center;
  font-size: 13px;
  color: $text-placeholder;
  margin: 16px 0 0;
}

// ==================== 响应式 ====================
@media (max-width: 1024px) {
  .login-box {
    max-width: 420px;
    padding: 0;

    .login-card {
      padding: 40px;
    }
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: 0;

    .login-card {
      padding: 32px 24px;
    }
  }
  
  .login-card .login-header h2 {
    font-size: 24px;
  }
}
</style>


