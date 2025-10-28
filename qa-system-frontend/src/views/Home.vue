<template>
  <div class="home-container">
    <el-container>
      <el-header class="home-header">
        <div class="header-content">
          <div class="logo">
            <h1>师生答疑系统</h1>
          </div>
          <div class="nav-menu">
            <el-button text @click="router.push('/home')">首页</el-button>
            <el-button text @click="router.push('/forum')">交流区</el-button>
          </div>
          <div class="user-actions">
            <template v-if="userStore.isAuthenticated">
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                    {{ userStore.userInfo?.realName?.[0] }}
                  </el-avatar>
                  <span class="username">{{ userStore.userInfo?.realName }}</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                    <el-dropdown-item command="dashboard">工作台</el-dropdown-item>
                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button @click="router.push('/login')">登录</el-button>
              <el-button type="primary" @click="router.push('/register')">注册</el-button>
            </template>
          </div>
        </div>
      </el-header>

      <el-main class="home-main">
        <!-- Hero区域 -->
        <div class="hero-section">
          <div class="hero-background">
            <div class="hero-shape hero-shape-1"></div>
            <div class="hero-shape hero-shape-2"></div>
            <div class="hero-shape hero-shape-3"></div>
          </div>
          <div class="hero-content">
            <div class="hero-badge">
              <el-icon><Star /></el-icon>
              <span>智能化学习平台</span>
            </div>
            <h1 class="hero-title">欢迎来到师生答疑系统</h1>
            <p class="hero-subtitle">连接师生智慧，构建知识桥梁，让每一个问题都能得到专业解答</p>
            <div class="hero-stats-inline">
              <div class="stat-inline-item">
                <el-icon><UserFilled /></el-icon>
                <span>{{ stats.users }}+ 用户</span>
              </div>
              <div class="stat-inline-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ stats.questions }}+ 问题</span>
              </div>
              <div class="stat-inline-item">
                <el-icon><Select /></el-icon>
                <span>{{ stats.answers }}+ 解答</span>
              </div>
            </div>
            <div class="hero-actions">
              <el-button type="primary" size="large" @click="handleGetStarted">
                <el-icon><Promotion /></el-icon>
                开始使用
              </el-button>
              <el-button size="large" @click="router.push('/forum')">
                <el-icon><ChatLineSquare /></el-icon>
                进入交流区
              </el-button>
            </div>
          </div>
        </div>

        <!-- 功能特色区域 -->
        <div class="features-section">
          <div class="section-header">
            <h2 class="section-title">核心功能</h2>
            <p class="section-subtitle">为师生打造的全方位学习辅助平台</p>
          </div>
          
          <div class="features-grid">
            <div class="feature-card" v-for="(feature, index) in features" :key="index">
              <div class="feature-icon" :style="{ background: feature.gradient }">
                <el-icon :size="36"><component :is="feature.icon" /></el-icon>
              </div>
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-desc">{{ feature.desc }}</p>
              <div class="feature-tags">
                <el-tag 
                  v-for="tag in feature.tags" 
                  :key="tag" 
                  size="small" 
                  effect="plain"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 数据统计区域 -->
        <div class="stats-section">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="48"><TrendCharts /></el-icon>
              </div>
              <div class="stat-number" data-target="1000">{{ stats.totalQuestions }}</div>
              <div class="stat-label">总问题数</div>
              <div class="stat-growth">
                <el-icon><Top /></el-icon>
                <span>+15% 本月</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="48"><Medal /></el-icon>
              </div>
              <div class="stat-number" data-target="850">{{ stats.solvedQuestions }}</div>
              <div class="stat-label">已解决问题</div>
              <div class="stat-growth">
                <el-icon><Top /></el-icon>
                <span>+22% 本月</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="48"><User /></el-icon>
              </div>
              <div class="stat-number" data-target="500">{{ stats.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
              <div class="stat-growth">
                <el-icon><Top /></el-icon>
                <span>+8% 本月</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon :size="48"><Clock /></el-icon>
              </div>
              <div class="stat-number">{{ stats.avgResponseTime }}</div>
              <div class="stat-label">平均响应时间</div>
              <div class="stat-growth">
                <el-icon><Bottom /></el-icon>
                <span>-10% 本月</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 如何使用区域 -->
        <div class="howto-section">
          <div class="section-header">
            <h2 class="section-title">如何使用</h2>
            <p class="section-subtitle">三步开启你的学习之旅</p>
          </div>
          
          <div class="steps-container">
            <div class="step-item" v-for="(step, index) in steps" :key="index">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">
                <div class="step-icon">
                  <el-icon :size="32"><component :is="step.icon" /></el-icon>
                </div>
                <h3>{{ step.title }}</h3>
                <p>{{ step.desc }}</p>
              </div>
              <div v-if="index < steps.length - 1" class="step-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </el-main>

      <el-footer class="home-footer">
        <p>&copy; 2025 师生答疑系统. All rights reserved.</p>
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
  Reading
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
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    tags: ['实时', '高效', '专业']
  },
  {
    icon: 'Collection',
    title: '知识沉淀',
    desc: '问题答案长期保存，形成可检索的知识库，供后续学习参考',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    tags: ['知识库', '可检索', '可复用']
  },
  {
    icon: 'ChatLineSquare',
    title: '社区交流',
    desc: '开放式论坛，师生自由讨论，促进知识共享与思维碰撞',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    tags: ['开放', '互动', '共享']
  },
  {
    icon: 'TrendCharts',
    title: '数据分析',
    desc: '统计学习数据，分析问题趋势，为教学提供数据支撑',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    tags: ['数据', '分析', '洞察']
  },
  {
    icon: 'Medal',
    title: '成就体系',
    desc: '激励机制，认可贡献，提升参与积极性',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    tags: ['激励', '成长', '认可']
  },
  {
    icon: 'Star',
    title: '精选推荐',
    desc: '智能推荐优质内容，个性化学习路径',
    gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    tags: ['智能', '推荐', '个性化']
  }
]

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
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.home-header {
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logo h1 {
    margin: 0;
    font-size: 24px;
    color: #409eff;
  }

  .nav-menu {
    flex: 1;
    margin-left: 60px;
  }

  .user-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;

    .username {
      font-size: 14px;
    }
  }
}

.home-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
}

// Hero区域
.hero-section {
  position: relative;
  padding: 120px 60px;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  
  .hero-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    
    .hero-shape {
      position: absolute;
      border-radius: 50%;
      filter: blur(100px);
      opacity: 0.2;
      
      &.hero-shape-1 {
        width: 600px;
        height: 600px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        top: -200px;
        right: -200px;
        animation: float 20s ease-in-out infinite;
      }
      
      &.hero-shape-2 {
        width: 500px;
        height: 500px;
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        bottom: -150px;
        left: -150px;
        animation: float 25s ease-in-out infinite reverse;
      }
      
      &.hero-shape-3 {
        width: 400px;
        height: 400px;
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        top: 50%;
        left: 50%;
        animation: float 30s ease-in-out infinite;
      }
    }
  }
  
  .hero-content {
    position: relative;
    text-align: center;
    color: white;
    z-index: 2;
    
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 8px 20px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50px;
      border: 1px solid rgba(255, 255, 255, 0.2);
      font-size: 14px;
      margin-bottom: 24px;
      backdrop-filter: blur(10px);
    }
    
    .hero-title {
      font-size: 56px;
      font-weight: 800;
      margin: 0 0 24px 0;
      line-height: 1.2;
      background: linear-gradient(135deg, #fff 0%, #a78bfa 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .hero-subtitle {
      font-size: 20px;
      opacity: 0.9;
      margin: 0 0 40px 0;
      max-width: 700px;
      margin-left: auto;
      margin-right: auto;
      line-height: 1.6;
    }
    
    .hero-stats-inline {
      display: flex;
      justify-content: center;
      gap: 48px;
      margin-bottom: 40px;
      
      .stat-inline-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        
        .el-icon {
          font-size: 20px;
        }
      }
    }
    
    .hero-actions {
      display: flex;
      justify-content: center;
      gap: 16px;
    }
  }
}

// 功能特色区域
.features-section {
  padding: 100px 60px;
  background: white;
  
  .section-header {
    text-align: center;
    margin-bottom: 60px;
    
    .section-title {
      font-size: 42px;
      font-weight: 700;
      color: #1f2937;
      margin: 0 0 16px 0;
    }
    
    .section-subtitle {
      font-size: 18px;
      color: #6b7280;
      margin: 0;
    }
  }
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    gap: 32px;
    
    .feature-card {
      padding: 32px;
      background: white;
      border-radius: 20px;
      border: 1px solid #e5e7eb;
      transition: all 0.3s;
      cursor: pointer;
      
      &:hover {
        transform: translateY(-8px);
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12);
        border-color: transparent;
      }
      
      .feature-icon {
        width: 72px;
        height: 72px;
        border-radius: 18px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        margin-bottom: 20px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      }
      
      .feature-title {
        font-size: 22px;
        font-weight: 600;
        color: #1f2937;
        margin: 0 0 12px 0;
      }
      
      .feature-desc {
        font-size: 15px;
        color: #6b7280;
        line-height: 1.7;
        margin: 0 0 20px 0;
      }
      
      .feature-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }
}

// 数据统计区域
.stats-section {
  padding: 100px 60px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 32px;
    
    .stat-card {
      background: white;
      padding: 40px;
      border-radius: 20px;
      text-align: center;
      transition: all 0.3s;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      
      &:hover {
        transform: translateY(-8px);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      }
      
      .stat-icon {
        color: #667eea;
        margin-bottom: 20px;
      }
      
      .stat-number {
        font-size: 48px;
        font-weight: 700;
        color: #1f2937;
        margin-bottom: 12px;
      }
      
      .stat-label {
        font-size: 16px;
        color: #6b7280;
        margin-bottom: 12px;
      }
      
      .stat-growth {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        color: #10b981;
        font-size: 14px;
        font-weight: 600;
        
        &:has(.el-icon[data-name="Bottom"]) {
          color: #ef4444;
        }
      }
    }
  }
}

// 如何使用区域
.howto-section {
  padding: 100px 60px;
  background: white;
  
  .section-header {
    text-align: center;
    margin-bottom: 60px;
    
    .section-title {
      font-size: 42px;
      font-weight: 700;
      color: #1f2937;
      margin: 0 0 16px 0;
    }
    
    .section-subtitle {
      font-size: 18px;
      color: #6b7280;
      margin: 0;
    }
  }
  
  .steps-container {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 40px;
    flex-wrap: wrap;
    
    .step-item {
      position: relative;
      flex: 1;
      min-width: 280px;
      max-width: 350px;
      
      .step-number {
        position: absolute;
        top: -20px;
        left: 20px;
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 24px;
        font-weight: 700;
        box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
        z-index: 2;
      }
      
      .step-content {
        background: white;
        padding: 40px 24px 24px;
        border-radius: 20px;
        border: 2px solid #e5e7eb;
        text-align: center;
        transition: all 0.3s;
        
        &:hover {
          border-color: #667eea;
          box-shadow: 0 12px 40px rgba(102, 126, 234, 0.15);
          transform: translateY(-4px);
        }
        
        .step-icon {
          width: 64px;
          height: 64px;
          margin: 0 auto 20px;
          background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #667eea;
        }
        
        h3 {
          font-size: 20px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 12px 0;
        }
        
        p {
          font-size: 15px;
          color: #6b7280;
          line-height: 1.6;
          margin: 0;
        }
      }
      
      .step-arrow {
        position: absolute;
        top: 50%;
        right: -40px;
        transform: translateY(-50%);
        font-size: 32px;
        color: #d1d5db;
      }
    }
  }
}

.home-footer {
  background: white;
  text-align: center;
  color: #666;
  border-top: 1px solid #eee;

  p {
    margin: 0;
    line-height: 60px;
  }
}
</style>


