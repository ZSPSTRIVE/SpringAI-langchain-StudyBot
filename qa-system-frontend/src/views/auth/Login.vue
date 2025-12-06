<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="neo-background">
      <!-- 漂浮形状 -->
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
      <div class="floating-shape shape-5"></div>
      <div class="floating-shape shape-6"></div>
      <!-- 波点层 -->
      <div class="dots-layer"></div>
    </div>

    <!-- 左侧展示区 -->
    <div class="login-showcase">
      <div class="showcase-content">
        <!-- Logo区域 -->
        <div class="logo-section">
          <div class="logo-box">
            <el-icon class="logo-icon"><School /></el-icon>
          </div>
          <h1 class="system-title">师生答疑</h1>
          <p class="system-slogan">STUDENT & TEACHER Q&A SYSTEM</p>
        </div>
        
        <!-- 特性卡片 -->
        <div class="feature-grid">
          <div 
            class="feature-card" 
            v-for="(feature, index) in features" 
            :key="index"
            :class="`feature-card--${feature.color}`"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="feature-icon">
              <el-icon :size="28"><component :is="feature.icon" /></el-icon>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.desc }}</p>
          </div>
        </div>

        <!-- 装饰文字 -->
        <div class="deco-text">
          <span>LEARN</span>
          <span>·</span>
          <span>SHARE</span>
          <span>·</span>
          <span>GROW</span>
        </div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-box">
      <div class="login-card">
        <div class="login-header">
          <h2>登录</h2>
          <p>LOGIN TO YOUR ACCOUNT</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="username">
            <div class="neo-input-wrapper">
              <label>用户名</label>
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
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="neo-input-wrapper">
              <label>密码</label>
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
            </div>
          </el-form-item>

          <el-form-item>
            <button
              type="button"
              class="neo-login-btn"
              :disabled="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">立即登录</span>
              <span v-else>登录中...</span>
              <el-icon v-if="!loading"><Right /></el-icon>
            </button>
          </el-form-item>

          <div class="login-footer">
            <router-link to="/register" class="register-link">
              <span>没有账号？</span>
              <strong>立即注册 →</strong>
            </router-link>
          </div>
        </el-form>
      </div>

      <!-- 底部装饰 -->
      <div class="bottom-deco">
        <div class="deco-bar deco-bar--yellow"></div>
        <div class="deco-bar deco-bar--blue"></div>
        <div class="deco-bar deco-bar--pink"></div>
      </div>
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
  Right,
  Promotion,
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
  background: $neo-cream;
  position: relative;
  overflow: hidden;
}

// ==================== 动态背景 ====================
.neo-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  
  .dots-layer {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: radial-gradient(circle, rgba($neo-black, 0.08) 2px, transparent 2px);
    background-size: 24px 24px;
    animation: dotsMove 30s linear infinite;
  }
  
  .floating-shape {
    position: absolute;
    border: 4px solid $neo-black;
    animation: floatShape 8s ease-in-out infinite;
    
    &.shape-1 {
      width: 120px;
      height: 120px;
      background: $neo-yellow;
      top: 10%;
      left: 5%;
      border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
      animation-delay: 0s;
      box-shadow: 8px 8px 0 0 $neo-black;
    }
    
    &.shape-2 {
      width: 80px;
      height: 80px;
      background: $neo-blue;
      top: 60%;
      left: 15%;
      border-radius: 50%;
      animation-delay: 1s;
      box-shadow: 6px 6px 0 0 $neo-black;
    }
    
    &.shape-3 {
      width: 100px;
      height: 100px;
      background: $neo-pink;
      top: 25%;
      left: 35%;
      animation-delay: 2s;
      box-shadow: 6px 6px 0 0 $neo-black;
    }
    
    &.shape-4 {
      width: 60px;
      height: 60px;
      background: $neo-green;
      top: 75%;
      left: 40%;
      border-radius: 50%;
      animation-delay: 3s;
      box-shadow: 4px 4px 0 0 $neo-black;
    }
    
    &.shape-5 {
      width: 90px;
      height: 90px;
      background: $neo-purple;
      top: 15%;
      left: 50%;
      border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
      animation-delay: 4s;
      box-shadow: 6px 6px 0 0 $neo-black;
    }
    
    &.shape-6 {
      width: 70px;
      height: 70px;
      background: $neo-orange;
      top: 50%;
      left: 25%;
      animation-delay: 5s;
      box-shadow: 5px 5px 0 0 $neo-black;
    }
  }
}

@keyframes dotsMove {
  0% { background-position: 0 0; }
  100% { background-position: 48px 48px; }
}

@keyframes floatShape {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-20px) rotate(5deg); }
  50% { transform: translateY(-10px) rotate(-3deg); }
  75% { transform: translateY(-25px) rotate(2deg); }
}

// ==================== 左侧展示区 ====================
.login-showcase {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
  
  .showcase-content {
    max-width: 560px;
    
    .logo-section {
      text-align: center;
      margin-bottom: 48px;
      
      .logo-box {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 100px;
        height: 100px;
        background: $neo-yellow;
        border: 4px solid $neo-black;
        border-radius: 24px;
        box-shadow: 8px 8px 0 0 $neo-black;
        margin-bottom: 24px;
        animation: wobble 3s ease-in-out infinite;
        
        .logo-icon {
          font-size: 56px;
          color: $neo-black;
        }
      }
      
      .system-title {
        font-size: 56px;
        font-weight: 900;
        margin: 0 0 12px 0;
        color: $neo-black;
        text-transform: uppercase;
        letter-spacing: 4px;
        text-shadow: 4px 4px 0 $neo-yellow;
      }
      
      .system-slogan {
        font-size: 14px;
        font-weight: 700;
        letter-spacing: 3px;
        color: $neo-black;
        opacity: 0.7;
        margin: 0;
      }
    }
    
    .feature-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
      margin-bottom: 48px;
      
      .feature-card {
        padding: 24px 16px;
        background: $neo-white;
        border: 3px solid $neo-black;
        border-radius: 16px;
        text-align: center;
        box-shadow: 6px 6px 0 0 $neo-black;
        transition: all 200ms cubic-bezier(0.34, 1.56, 0.64, 1);
        animation: neoPopIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) backwards;
        
        &:hover {
          transform: translate(-4px, -4px);
          box-shadow: 10px 10px 0 0 $neo-black;
        }
        
        &--yellow {
          background: $neo-yellow;
          .feature-icon { background: $neo-white; }
        }
        
        &--blue {
          background: $neo-blue;
          color: $neo-white;
          .feature-icon { background: $neo-white; color: $neo-blue; }
          h3, p { color: $neo-white; }
        }
        
        &--pink {
          background: $neo-pink;
          color: $neo-white;
          .feature-icon { background: $neo-white; color: $neo-pink; }
          h3, p { color: $neo-white; }
        }
        
        .feature-icon {
          width: 56px;
          height: 56px;
          margin: 0 auto 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: $neo-yellow;
          border: 3px solid $neo-black;
          border-radius: 12px;
          box-shadow: 3px 3px 0 0 $neo-black;
        }
        
        h3 {
          font-size: 16px;
          font-weight: 800;
          margin: 0 0 8px 0;
          color: $neo-black;
          text-transform: uppercase;
          letter-spacing: 1px;
        }
        
        p {
          font-size: 12px;
          color: rgba($neo-black, 0.8);
          margin: 0;
          line-height: 1.4;
        }
      }
    }
    
    .deco-text {
      text-align: center;
      font-size: 24px;
      font-weight: 900;
      letter-spacing: 8px;
      color: $neo-black;
      opacity: 0.15;
      
      span {
        margin: 0 8px;
      }
    }
  }
}

@keyframes wobble {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(3deg); }
  75% { transform: rotate(-3deg); }
}

@keyframes neoPopIn {
  0% {
    opacity: 0;
    transform: scale(0) translateY(50px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// ==================== 右侧登录区 ====================
.login-box {
  width: 500px;
  padding: 60px 48px;
  background: $neo-white;
  border-left: 4px solid $neo-black;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.login-card {
  .login-header {
    text-align: center;
    margin-bottom: 40px;
    
    h2 {
      margin: 0;
      font-size: 48px;
      color: $neo-black;
      font-weight: 900;
      text-transform: uppercase;
      letter-spacing: 4px;
    }
    
    p {
      margin: 8px 0 0;
      color: $neo-black;
      font-size: 12px;
      font-weight: 700;
      letter-spacing: 2px;
      opacity: 0.5;
    }
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
  
  .neo-input-wrapper {
    width: 100%;
    
    label {
      display: block;
      font-size: 12px;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 1px;
      color: $neo-black;
      margin-bottom: 8px;
    }
  }
}

.neo-login-btn {
  width: 100%;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 900;
  font-family: inherit;
  text-transform: uppercase;
  letter-spacing: 2px;
  background: $neo-blue;
  color: $neo-white;
  border: 4px solid $neo-black;
  border-radius: 12px;
  box-shadow: 6px 6px 0 0 $neo-black;
  cursor: pointer;
  transition: all 150ms cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &:hover:not(:disabled) {
    transform: translate(-3px, -3px);
    box-shadow: 9px 9px 0 0 $neo-black;
    background: $neo-blue-dark;
  }
  
  &:active:not(:disabled) {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0 0 $neo-black;
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  
  .register-link {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    background: $neo-yellow;
    border: 3px solid $neo-black;
    border-radius: 8px;
    box-shadow: 4px 4px 0 0 $neo-black;
    font-size: 14px;
    color: $neo-black;
    text-decoration: none;
    transition: all 150ms cubic-bezier(0.34, 1.56, 0.64, 1);
    
    span {
      font-weight: 500;
    }
    
    strong {
      font-weight: 800;
    }
    
    &:hover {
      transform: translate(-2px, -2px);
      box-shadow: 6px 6px 0 0 $neo-black;
      background: $neo-yellow-dark;
    }
  }
}

.bottom-deco {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  height: 12px;
  
  .deco-bar {
    flex: 1;
    
    &--yellow { background: $neo-yellow; }
    &--blue { background: $neo-blue; }
    &--pink { background: $neo-pink; }
  }
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .login-showcase {
    padding: 40px;
    
    .showcase-content {
      .system-title {
        font-size: 42px;
      }
      
      .feature-grid {
        gap: 16px;
        
        .feature-card {
          padding: 20px 12px;
        }
      }
    }
  }
}

@media (max-width: 1024px) {
  .login-showcase {
    display: none;
  }
  
  .login-box {
    width: 100%;
    max-width: 480px;
    margin: 0 auto;
    border-left: none;
    border-top: 4px solid $neo-black;
  }
  
  .neo-background {
    .floating-shape {
      &.shape-1 { left: 10%; top: 5%; }
      &.shape-2 { left: auto; right: 10%; top: 15%; }
      &.shape-3 { left: 50%; top: auto; bottom: 20%; }
      &.shape-4 { display: none; }
      &.shape-5 { display: none; }
      &.shape-6 { display: none; }
    }
  }
}

@media (max-width: 768px) {
  .login-box {
    padding: 48px 24px;
  }
  
  .login-card .login-header h2 {
    font-size: 36px;
  }
  
  .neo-login-btn {
    height: 52px;
    font-size: 16px;
  }
}
</style>


