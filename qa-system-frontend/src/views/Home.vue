<template>
  <div class="home-container">
    <!-- 动态背景 -->
    <div class="neo-bg-animated">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
        <div class="shape shape-6"></div>
        <div class="shape shape-7"></div>
        <div class="shape shape-8"></div>
      </div>
    </div>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="neo-header">
        <div class="header-content">
          <div class="logo" @click="router.push('/home')">
            <div class="logo-icon">
              <el-icon><School /></el-icon>
            </div>
            <span class="logo-text">Q&A</span>
          </div>
          
          <nav class="nav-menu">
            <a href="#" class="nav-item active" @click.prevent="scrollToTop">首页</a>
            <a href="#features" class="nav-item">功能</a>
            <a href="#" class="nav-item" @click.prevent="router.push('/forum')">交流区</a>
          </nav>
          
          <div class="user-actions">
            <template v-if="userStore.isAuthenticated">
              <el-dropdown @command="handleCommand" trigger="click">
                <div class="user-badge">
                  <el-avatar :size="36" :src="userStore.userInfo?.avatar" class="user-avatar">
                    {{ userStore.userInfo?.realName?.[0] }}
                  </el-avatar>
                  <span class="username">{{ userStore.userInfo?.realName }}</span>
                  <el-icon><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="dashboard">
                      <el-icon><Monitor /></el-icon>工作台
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <button class="neo-btn neo-btn--outline" @click="router.push('/login')">登录</button>
              <button class="neo-btn neo-btn--primary" @click="router.push('/register')">注册</button>
            </template>
          </div>
        </div>
      </el-header>

      <el-main class="home-main">
        <!-- Hero区域 - Neo-Brutalism -->
        <div class="hero-section">
          <div class="hero-content">
            <div class="hero-badge">
              <el-icon><Star /></el-icon>
              <span>SMART LEARNING PLATFORM</span>
            </div>
            
            <h1 class="hero-title">
              <span class="title-line">师生</span>
              <span class="title-line highlight">答疑</span>
              <span class="title-line">系统</span>
            </h1>
            
            <p class="hero-subtitle">连接师生智慧 · 构建知识桥梁 · 让每一个问题都能得到专业解答</p>
            
            <div class="hero-stats">
              <div class="stat-card stat-card--yellow">
                <div class="stat-number">{{ stats.users }}+</div>
                <div class="stat-label">活跃用户</div>
              </div>
              <div class="stat-card stat-card--blue">
                <div class="stat-number">{{ stats.questions }}+</div>
                <div class="stat-label">问题总数</div>
              </div>
              <div class="stat-card stat-card--green">
                <div class="stat-number">{{ stats.answers }}+</div>
                <div class="stat-label">优质解答</div>
              </div>
            </div>
            
            <div class="hero-actions">
              <button class="neo-hero-btn neo-hero-btn--primary" @click="handleGetStarted">
                <span>开始使用</span>
                <el-icon><Promotion /></el-icon>
              </button>
              <button class="neo-hero-btn neo-hero-btn--secondary" @click="router.push('/forum')">
                <span>进入交流区</span>
                <el-icon><ChatLineSquare /></el-icon>
              </button>
            </div>
          </div>
          
          <!-- Hero装饰 -->
          <div class="hero-deco">
            <div class="deco-card deco-card--1">
              <el-icon :size="32"><ChatDotRound /></el-icon>
              <span>即时问答</span>
            </div>
            <div class="deco-card deco-card--2">
              <el-icon :size="32"><TrendCharts /></el-icon>
              <span>数据分析</span>
            </div>
            <div class="deco-card deco-card--3">
              <el-icon :size="32"><Collection /></el-icon>
              <span>知识库</span>
            </div>
          </div>
        </div>

        <!-- 功能特色区域 - Neo-Brutalism -->
        <div id="features" class="features-section">
          <div class="section-header">
            <span class="section-tag">FEATURES</span>
            <h2 class="section-title">核心功能</h2>
            <p class="section-subtitle">为师生打造的全方位学习辅助平台</p>
          </div>
          
          <div class="features-grid">
            <div 
              class="neo-feature-card" 
              v-for="(feature, index) in features" 
              :key="index"
              :class="`neo-feature-card--${feature.colorClass}`"
              :style="{ animationDelay: `${index * 0.1}s` }"
            >
              <div class="feature-number">0{{ index + 1 }}</div>
              <div class="feature-icon">
                <el-icon :size="40"><component :is="feature.icon" /></el-icon>
              </div>
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-desc">{{ feature.desc }}</p>
              <div class="feature-tags">
                <span class="neo-tag" v-for="tag in feature.tags" :key="tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 如何使用区域 - Neo-Brutalism -->
        <div class="howto-section">
          <div class="section-header">
            <span class="section-tag">HOW TO USE</span>
            <h2 class="section-title">如何使用</h2>
            <p class="section-subtitle">三步开启你的学习之旅</p>
          </div>
          
          <div class="steps-container">
            <div 
              class="neo-step" 
              v-for="(step, index) in steps" 
              :key="index"
              :class="[`neo-step--${['yellow', 'blue', 'pink'][index]}`]"
            >
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-icon">
                <el-icon :size="36"><component :is="step.icon" /></el-icon>
              </div>
              <h3>{{ step.title }}</h3>
              <p>{{ step.desc }}</p>
              <div v-if="index < steps.length - 1" class="step-arrow">
                <span>→</span>
              </div>
            </div>
          </div>
        </div>

        <!-- CTA区域 -->
        <div class="cta-section">
          <div class="cta-content">
            <h2>准备好开始了吗？</h2>
            <p>加入我们，与千万师生一起学习成长</p>
            <div class="cta-actions">
              <button class="neo-cta-btn" @click="handleGetStarted">
                <span>立即开始</span>
                <el-icon><Right /></el-icon>
              </button>
            </div>
          </div>
          <div class="cta-deco">
            <div class="cta-shape cta-shape--1"></div>
            <div class="cta-shape cta-shape--2"></div>
            <div class="cta-shape cta-shape--3"></div>
          </div>
        </div>
      </el-main>

      <!-- 页脚 -->
      <el-footer class="neo-footer">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="footer-logo">
              <el-icon><School /></el-icon>
              <span>师生答疑系统</span>
            </div>
            <p>连接师生 · 分享知识 · 共同成长</p>
          </div>
          <div class="footer-links">
            <a href="#">关于我们</a>
            <a href="#">使用帮助</a>
            <a href="#">隐私政策</a>
            <a href="#">联系我们</a>
          </div>
          <div class="footer-copyright">
            <p>&copy; 2025 师生答疑系统. All rights reserved.</p>
          </div>
        </div>
        <div class="footer-bars">
          <div class="bar bar--yellow"></div>
          <div class="bar bar--blue"></div>
          <div class="bar bar--pink"></div>
          <div class="bar bar--green"></div>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ChatDotRound, 
  Collection, 
  ChatLineSquare,
  Star,
  UserFilled,
  Select,
  Promotion,
  TrendCharts,
  Medal,
  User,
  Clock,
  Top,
  Bottom,
  ArrowRight,
  Checked,
  Reading,
  School,
  ArrowDown,
  Monitor,
  SwitchButton,
  Right
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  users: 500,
  questions: 1000,
  answers: 850,
  totalQuestions: 1234,
  solvedQuestions: 987,
  activeUsers: 456,
  avgResponseTime: '2.5h'
})

const features = [
  {
    icon: 'ChatDotRound',
    title: '智能问答',
    desc: '学生提问，教师及时响应，构建高效的师生互动平台',
    colorClass: 'yellow',
    tags: ['实时', '高效', '专业']
  },
  {
    icon: 'Collection',
    title: '知识沉淀',
    desc: '问题答案长期保存，形成可检索的知识库',
    colorClass: 'blue',
    tags: ['知识库', '可检索', '可复用']
  },
  {
    icon: 'ChatLineSquare',
    title: '社区交流',
    desc: '开放式论坛，师生自由讨论，促进知识共享',
    colorClass: 'pink',
    tags: ['开放', '互动', '共享']
  },
  {
    icon: 'TrendCharts',
    title: '数据分析',
    desc: '统计学习数据，分析问题趋势，数据驱动教学',
    colorClass: 'green',
    tags: ['数据', '分析', '洞察']
  },
  {
    icon: 'Medal',
    title: '成就体系',
    desc: '激励机制，认可贡献，提升参与积极性',
    colorClass: 'orange',
    tags: ['激励', '成长', '认可']
  },
  {
    icon: 'Star',
    title: '精选推荐',
    desc: '智能推荐优质内容，个性化学习路径',
    colorClass: 'purple',
    tags: ['智能', '推荐', '个性化']
  }
]

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const steps = [
  {
    icon: 'UserFilled',
    title: '注册登录',
    desc: '创建账号，选择身份（学生/教师），完善个人信息'
  },
  {
    icon: 'Reading',
    title: '提问/解答',
    desc: '学生发布问题，教师专业解答，互动交流'
  },
  {
    icon: 'Checked',
    title: '知识沉淀',
    desc: '优质内容沉淀成知识库，持续学习成长'
  }
]

const handleGetStarted = () => {
  if (userStore.isAuthenticated) {
    const role = userStore.userInfo?.role
    if (role === 'ADMIN') {
      router.push('/admin')
    } else if (role === 'TEACHER') {
      router.push('/teacher')
    } else if (role === 'STUDENT') {
      router.push('/student')
    }
  } else {
    router.push('/login')
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await logoutApi()
      userStore.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
    } catch (error) {
      console.error('退出失败:', error)
    }
  } else if (command === 'profile') {
    const role = userStore.userInfo?.role
    if (role === 'STUDENT') {
      router.push('/student/profile')
    } else if (role === 'TEACHER') {
      router.push('/teacher/profile')
    }
  } else if (command === 'dashboard') {
    const role = userStore.userInfo?.role
    if (role === 'ADMIN') {
      router.push('/admin')
    } else if (role === 'TEACHER') {
      router.push('/teacher')
    } else if (role === 'STUDENT') {
      router.push('/student')
    }
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.home-container {
  min-height: 100vh;
  background: $neo-cream;
  position: relative;
}

// ==================== 动态背景 ====================
.neo-bg-animated {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  background-image: radial-gradient(circle, rgba($neo-black, 0.06) 2px, transparent 2px);
  background-size: 24px 24px;
  
  .floating-shapes {
    .shape {
      position: absolute;
      border: 3px solid $neo-black;
      animation: floatShape 10s ease-in-out infinite;
      
      &.shape-1 { width: 80px; height: 80px; background: $neo-yellow; top: 5%; left: 5%; border-radius: 50%; animation-delay: 0s; box-shadow: 4px 4px 0 0 $neo-black; }
      &.shape-2 { width: 60px; height: 60px; background: $neo-blue; top: 20%; right: 10%; animation-delay: 1s; box-shadow: 4px 4px 0 0 $neo-black; }
      &.shape-3 { width: 50px; height: 50px; background: $neo-pink; top: 60%; left: 8%; border-radius: 50%; animation-delay: 2s; box-shadow: 3px 3px 0 0 $neo-black; }
      &.shape-4 { width: 70px; height: 70px; background: $neo-green; bottom: 15%; right: 5%; animation-delay: 3s; box-shadow: 4px 4px 0 0 $neo-black; }
      &.shape-5 { width: 40px; height: 40px; background: $neo-purple; top: 40%; left: 3%; animation-delay: 4s; box-shadow: 3px 3px 0 0 $neo-black; }
      &.shape-6 { width: 55px; height: 55px; background: $neo-orange; top: 80%; left: 15%; border-radius: 50%; animation-delay: 5s; box-shadow: 3px 3px 0 0 $neo-black; }
      &.shape-7 { width: 45px; height: 45px; background: $neo-cyan; top: 10%; left: 40%; animation-delay: 6s; box-shadow: 3px 3px 0 0 $neo-black; }
      &.shape-8 { width: 65px; height: 65px; background: $neo-lime; bottom: 30%; right: 15%; border-radius: 50%; animation-delay: 7s; box-shadow: 4px 4px 0 0 $neo-black; }
    }
  }
}

@keyframes floatShape {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-15px) rotate(5deg); }
  50% { transform: translateY(-8px) rotate(-3deg); }
  75% { transform: translateY(-20px) rotate(2deg); }
}

// ==================== 顶部导航 ====================
.neo-header {
  background: $neo-white;
  border-bottom: 4px solid $neo-black;
  padding: 0;
  height: 80px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  height: 100%;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    
    .logo-icon {
      width: 48px;
      height: 48px;
      background: $neo-yellow;
      border: 3px solid $neo-black;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 4px 4px 0 0 $neo-black;
      font-size: 24px;
      transition: all 150ms $bounce-curve;
      
      &:hover {
        transform: translate(-2px, -2px);
        box-shadow: 6px 6px 0 0 $neo-black;
      }
    }
    
    .logo-text {
      font-size: 28px;
      font-weight: 900;
      color: $neo-black;
      letter-spacing: 2px;
    }
  }
  
  .nav-menu {
    display: flex;
    gap: 8px;
    
    .nav-item {
      padding: 12px 24px;
      font-size: 14px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 1px;
      color: $neo-black;
      text-decoration: none;
      border: 3px solid transparent;
      border-radius: 8px;
      transition: all 150ms $bounce-curve;
      
      &:hover, &.active {
        background: $neo-yellow;
        border-color: $neo-black;
        box-shadow: 3px 3px 0 0 $neo-black;
      }
      
      &:active {
        transform: translate(2px, 2px);
        box-shadow: 1px 1px 0 0 $neo-black;
      }
    }
  }
  
  .user-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .user-badge {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 16px;
    background: $neo-yellow;
    border: 3px solid $neo-black;
    border-radius: 12px;
    box-shadow: 4px 4px 0 0 $neo-black;
    cursor: pointer;
    transition: all 150ms $bounce-curve;
    
    &:hover {
      transform: translate(-2px, -2px);
      box-shadow: 6px 6px 0 0 $neo-black;
    }
    
    .user-avatar {
      border: 2px solid $neo-black;
    }
    
    .username {
      font-weight: 700;
      color: $neo-black;
    }
  }
}

.neo-btn {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  border: 3px solid $neo-black;
  border-radius: 8px;
  cursor: pointer;
  transition: all 150ms $bounce-curve;
  box-shadow: 4px 4px 0 0 $neo-black;
  
  &:hover {
    transform: translate(-2px, -2px);
    box-shadow: 6px 6px 0 0 $neo-black;
  }
  
  &:active {
    transform: translate(2px, 2px);
    box-shadow: 1px 1px 0 0 $neo-black;
  }
  
  &--outline {
    background: $neo-white;
    color: $neo-black;
  }
  
  &--primary {
    background: $neo-blue;
    color: $neo-white;
  }
}

.home-main {
  position: relative;
  z-index: 1;
}

// ==================== Hero区域 ====================
.hero-section {
  padding: 100px 60px 120px;
  position: relative;
  
  .hero-content {
    max-width: 900px;
    margin: 0 auto;
    text-align: center;
    
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 10px 20px;
      background: $neo-blue;
      color: $neo-white;
      border: 3px solid $neo-black;
      border-radius: 50px;
      font-size: 12px;
      font-weight: 800;
      letter-spacing: 2px;
      box-shadow: 4px 4px 0 0 $neo-black;
      margin-bottom: 32px;
    }
    
    .hero-title {
      margin: 0 0 32px 0;
      
      .title-line {
        display: inline-block;
        font-size: 80px;
        font-weight: 900;
        color: $neo-black;
        line-height: 1.1;
        text-transform: uppercase;
        letter-spacing: 4px;
        
        &.highlight {
          background: $neo-yellow;
          padding: 0 20px;
          border: 4px solid $neo-black;
          box-shadow: 6px 6px 0 0 $neo-black;
          margin: 0 16px;
        }
      }
    }
    
    .hero-subtitle {
      font-size: 20px;
      font-weight: 500;
      color: $neo-black;
      opacity: 0.8;
      margin: 0 0 48px 0;
      letter-spacing: 2px;
    }
    
    .hero-stats {
      display: flex;
      justify-content: center;
      gap: 24px;
      margin-bottom: 48px;
      
      .stat-card {
        padding: 24px 32px;
        background: $neo-white;
        border: 3px solid $neo-black;
        border-radius: 16px;
        box-shadow: 6px 6px 0 0 $neo-black;
        transition: all 200ms $bounce-curve;
        
        &:hover {
          transform: translate(-3px, -3px);
          box-shadow: 9px 9px 0 0 $neo-black;
        }
        
        &--yellow { background: $neo-yellow; }
        &--blue { background: $neo-blue; color: $neo-white; }
        &--green { background: $neo-green; }
        
        .stat-number {
          font-size: 36px;
          font-weight: 900;
          line-height: 1;
        }
        
        .stat-label {
          font-size: 12px;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 1px;
          margin-top: 8px;
        }
      }
    }
    
    .hero-actions {
      display: flex;
      justify-content: center;
      gap: 20px;
    }
  }
  
  .hero-deco {
    position: absolute;
    top: 50%;
    right: 5%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    gap: 20px;
    
    .deco-card {
      padding: 20px;
      background: $neo-white;
      border: 3px solid $neo-black;
      border-radius: 12px;
      box-shadow: 5px 5px 0 0 $neo-black;
      display: flex;
      align-items: center;
      gap: 12px;
      font-weight: 700;
      font-size: 14px;
      text-transform: uppercase;
      animation: floatShape 6s ease-in-out infinite;
      
      &--1 { background: $neo-yellow; animation-delay: 0s; }
      &--2 { background: $neo-pink; color: $neo-white; animation-delay: 2s; }
      &--3 { background: $neo-green; animation-delay: 4s; }
    }
  }
}

.neo-hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 20px 40px;
  font-size: 18px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 2px;
  border: 4px solid $neo-black;
  border-radius: 12px;
  cursor: pointer;
  transition: all 150ms $bounce-curve;
  box-shadow: 6px 6px 0 0 $neo-black;
  
  &:hover {
    transform: translate(-3px, -3px);
    box-shadow: 9px 9px 0 0 $neo-black;
  }
  
  &:active {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0 0 $neo-black;
  }
  
  &--primary {
    background: $neo-blue;
    color: $neo-white;
  }
  
  &--secondary {
    background: $neo-yellow;
    color: $neo-black;
  }
}

// ==================== 功能区域 ====================
.features-section {
  padding: 100px 60px;
  background: $neo-white;
  border-top: 4px solid $neo-black;
  
  .section-header {
    text-align: center;
    margin-bottom: 60px;
    
    .section-tag {
      display: inline-block;
      padding: 8px 20px;
      background: $neo-pink;
      color: $neo-white;
      border: 3px solid $neo-black;
      border-radius: 50px;
      font-size: 12px;
      font-weight: 800;
      letter-spacing: 2px;
      box-shadow: 3px 3px 0 0 $neo-black;
      margin-bottom: 20px;
    }
    
    .section-title {
      font-size: 56px;
      font-weight: 900;
      color: $neo-black;
      margin: 0 0 16px 0;
      text-transform: uppercase;
      letter-spacing: 4px;
    }
    
    .section-subtitle {
      font-size: 18px;
      color: rgba($neo-black, 0.7);
      margin: 0;
    }
  }
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    max-width: 1200px;
    margin: 0 auto;
  }
}

.neo-feature-card {
  padding: 32px;
  background: $neo-white;
  border: 3px solid $neo-black;
  border-radius: 16px;
  box-shadow: 6px 6px 0 0 $neo-black;
  position: relative;
  transition: all 200ms $bounce-curve;
  animation: neoPopIn 0.5s $bounce-curve backwards;
  
  &:hover {
    transform: translate(-4px, -4px);
    box-shadow: 10px 10px 0 0 $neo-black;
  }
  
  &--yellow { background: $neo-yellow; }
  &--blue { background: $neo-blue; color: $neo-white; .feature-icon { background: $neo-white; color: $neo-blue; } .neo-tag { background: $neo-white; color: $neo-blue; } }
  &--pink { background: $neo-pink; color: $neo-white; .feature-icon { background: $neo-white; color: $neo-pink; } .neo-tag { background: $neo-white; color: $neo-pink; } }
  &--green { background: $neo-green; .feature-icon { background: $neo-white; color: $neo-green-dark; } }
  &--orange { background: $neo-orange; color: $neo-white; .feature-icon { background: $neo-white; color: $neo-orange; } .neo-tag { background: $neo-white; color: $neo-orange; } }
  &--purple { background: $neo-purple; color: $neo-white; .feature-icon { background: $neo-white; color: $neo-purple; } .neo-tag { background: $neo-white; color: $neo-purple; } }
  
  .feature-number {
    position: absolute;
    top: 16px;
    right: 16px;
    font-size: 48px;
    font-weight: 900;
    opacity: 0.2;
    line-height: 1;
  }
  
  .feature-icon {
    width: 64px;
    height: 64px;
    background: $neo-yellow;
    border: 3px solid $neo-black;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 3px 3px 0 0 $neo-black;
    margin-bottom: 20px;
  }
  
  .feature-title {
    font-size: 22px;
    font-weight: 800;
    margin: 0 0 12px 0;
    text-transform: uppercase;
    letter-spacing: 1px;
  }
  
  .feature-desc {
    font-size: 14px;
    line-height: 1.6;
    margin: 0 0 20px 0;
    opacity: 0.9;
  }
  
  .feature-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .neo-tag {
    padding: 4px 12px;
    background: $neo-black;
    color: $neo-white;
    border: 2px solid $neo-black;
    border-radius: 4px;
    font-size: 10px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

@keyframes neoPopIn {
  0% { opacity: 0; transform: scale(0.8) translateY(30px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}

// ==================== 如何使用 ====================
.howto-section {
  padding: 100px 60px;
  background: $neo-cream;
  border-top: 4px solid $neo-black;
  
  .section-header {
    text-align: center;
    margin-bottom: 60px;
    
    .section-tag {
      display: inline-block;
      padding: 8px 20px;
      background: $neo-blue;
      color: $neo-white;
      border: 3px solid $neo-black;
      border-radius: 50px;
      font-size: 12px;
      font-weight: 800;
      letter-spacing: 2px;
      box-shadow: 3px 3px 0 0 $neo-black;
      margin-bottom: 20px;
    }
    
    .section-title {
      font-size: 56px;
      font-weight: 900;
      color: $neo-black;
      margin: 0 0 16px 0;
      text-transform: uppercase;
      letter-spacing: 4px;
    }
    
    .section-subtitle {
      font-size: 18px;
      color: rgba($neo-black, 0.7);
      margin: 0;
    }
  }
  
  .steps-container {
    display: flex;
    justify-content: center;
    gap: 32px;
    max-width: 1000px;
    margin: 0 auto;
  }
}

.neo-step {
  flex: 1;
  padding: 40px 24px;
  background: $neo-white;
  border: 3px solid $neo-black;
  border-radius: 16px;
  box-shadow: 6px 6px 0 0 $neo-black;
  text-align: center;
  position: relative;
  transition: all 200ms $bounce-curve;
  
  &:hover {
    transform: translate(-4px, -4px);
    box-shadow: 10px 10px 0 0 $neo-black;
  }
  
  &--yellow { background: $neo-yellow; }
  &--blue { background: $neo-blue; color: $neo-white; .step-icon { background: $neo-white; color: $neo-blue; } }
  &--pink { background: $neo-pink; color: $neo-white; .step-icon { background: $neo-white; color: $neo-pink; } }
  
  .step-number {
    position: absolute;
    top: -20px;
    left: 50%;
    transform: translateX(-50%);
    width: 48px;
    height: 48px;
    background: $neo-black;
    color: $neo-white;
    border: 3px solid $neo-black;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: 900;
    box-shadow: 3px 3px 0 0 rgba($neo-black, 0.3);
  }
  
  .step-icon {
    width: 72px;
    height: 72px;
    margin: 16px auto 20px;
    background: $neo-yellow;
    border: 3px solid $neo-black;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 4px 4px 0 0 $neo-black;
  }
  
  h3 {
    font-size: 20px;
    font-weight: 800;
    margin: 0 0 12px 0;
    text-transform: uppercase;
    letter-spacing: 1px;
  }
  
  p {
    font-size: 14px;
    line-height: 1.6;
    margin: 0;
    opacity: 0.9;
  }
  
  .step-arrow {
    position: absolute;
    top: 50%;
    right: -28px;
    transform: translateY(-50%);
    font-size: 32px;
    font-weight: 900;
    color: $neo-black;
    z-index: 2;
  }
}

// ==================== CTA区域 ====================
.cta-section {
  padding: 100px 60px;
  background: $neo-blue;
  border-top: 4px solid $neo-black;
  position: relative;
  overflow: hidden;
  
  .cta-content {
    text-align: center;
    position: relative;
    z-index: 2;
    
    h2 {
      font-size: 56px;
      font-weight: 900;
      color: $neo-white;
      margin: 0 0 16px 0;
      text-transform: uppercase;
      letter-spacing: 4px;
    }
    
    p {
      font-size: 20px;
      color: rgba($neo-white, 0.9);
      margin: 0 0 40px 0;
    }
  }
  
  .cta-deco {
    .cta-shape {
      position: absolute;
      border: 4px solid rgba($neo-white, 0.3);
      animation: floatShape 8s ease-in-out infinite;
      
      &--1 { width: 100px; height: 100px; background: $neo-yellow; top: 10%; left: 5%; border-radius: 50%; border-color: $neo-black; box-shadow: 5px 5px 0 0 $neo-black; }
      &--2 { width: 80px; height: 80px; background: $neo-pink; bottom: 15%; right: 10%; border-color: $neo-black; box-shadow: 4px 4px 0 0 $neo-black; animation-delay: 2s; }
      &--3 { width: 60px; height: 60px; background: $neo-green; top: 60%; left: 10%; border-color: $neo-black; box-shadow: 3px 3px 0 0 $neo-black; animation-delay: 4s; }
    }
  }
}

.neo-cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 24px 48px;
  background: $neo-yellow;
  color: $neo-black;
  font-size: 20px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 2px;
  border: 4px solid $neo-black;
  border-radius: 12px;
  box-shadow: 8px 8px 0 0 $neo-black;
  cursor: pointer;
  transition: all 150ms $bounce-curve;
  
  &:hover {
    transform: translate(-4px, -4px);
    box-shadow: 12px 12px 0 0 $neo-black;
  }
  
  &:active {
    transform: translate(2px, 2px);
    box-shadow: 4px 4px 0 0 $neo-black;
  }
}

// ==================== 页脚 ====================
.neo-footer {
  background: $neo-black;
  color: $neo-white;
  padding: 60px 60px 0;
  height: auto;
  position: relative;
  
  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 40px;
  }
  
  .footer-brand {
    .footer-logo {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 24px;
      font-weight: 900;
      margin-bottom: 12px;
      
      .el-icon {
        font-size: 32px;
        color: $neo-yellow;
      }
    }
    
    p {
      font-size: 14px;
      opacity: 0.7;
      margin: 0;
    }
  }
  
  .footer-links {
    display: flex;
    gap: 32px;
    
    a {
      color: $neo-white;
      text-decoration: none;
      font-weight: 600;
      transition: color 150ms;
      
      &:hover {
        color: $neo-yellow;
      }
    }
  }
  
  .footer-copyright {
    p {
      font-size: 12px;
      opacity: 0.5;
      margin: 0;
    }
  }
  
  .footer-bars {
    display: flex;
    height: 8px;
    
    .bar {
      flex: 1;
      
      &--yellow { background: $neo-yellow; }
      &--blue { background: $neo-blue-light; }
      &--pink { background: $neo-pink; }
      &--green { background: $neo-green; }
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .hero-section .hero-deco {
    display: none;
  }
  
  .hero-section .hero-content .hero-title .title-line {
    font-size: 60px;
  }
  
  .features-section .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .neo-header {
    height: 70px;
  }
  
  .header-content {
    padding: 0 20px;
    
    .nav-menu {
      display: none;
    }
    
    .logo .logo-text {
      display: none;
    }
  }
  
  .hero-section {
    padding: 60px 24px 80px;
    
    .hero-content {
      .hero-title .title-line {
        font-size: 36px;
        display: block;
        
        &.highlight {
          margin: 8px 0;
        }
      }
      
      .hero-stats {
        flex-direction: column;
        gap: 16px;
      }
      
      .hero-actions {
        flex-direction: column;
        
        .neo-hero-btn {
          width: 100%;
          justify-content: center;
        }
      }
    }
  }
  
  .features-section, .howto-section, .cta-section {
    padding: 60px 24px;
    
    .section-header .section-title {
      font-size: 36px;
    }
  }
  
  .features-section .features-grid {
    grid-template-columns: 1fr;
  }
  
  .howto-section .steps-container {
    flex-direction: column;
    
    .neo-step .step-arrow {
      display: none;
    }
  }
  
  .neo-footer .footer-content {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }
}
</style>


