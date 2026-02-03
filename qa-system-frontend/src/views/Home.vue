<template>
  <div class="landing">
    <div
      v-if="themeStore.backgroundMode === 'dynamic'"
      class="glow-bg"
      style="--glow-color-1: var(--theme-glow-1); --glow-color-2: var(--theme-glow-2);"
    />
    <div
      v-else
      class="pure-bg"
    />
    <FormulaBackdrop v-if="themeStore.backgroundMode === 'dynamic'" />

    <header class="landing-header">
      <div class="header-surface">
        <div
          class="brand"
          @click="router.push('/home')"
        >
          <div class="brand-mark">
            <el-icon><School /></el-icon>
          </div>
          <div class="brand-text">
            <div class="brand-name">
              师生答疑系统
            </div>
            <div class="brand-sub">
              Student • Teacher • AI
            </div>
          </div>
        </div>

        <nav class="nav">
          <a
            class="nav-link is-active"
            href="#"
            @click.prevent="scrollToTop"
          >首页</a>
          <a
            class="nav-link"
            href="#features"
            @click.prevent="scrollTo('features')"
          >功能</a>
          <a
            class="nav-link"
            href="#howto"
            @click.prevent="scrollTo('howto')"
          >使用</a>
          <a
            class="nav-link"
            href="#"
            @click.prevent="router.push('/forum')"
          >交流区</a>
        </nav>

        <div class="actions">
          <button
            class="icon-btn"
            :title="themeStore.backgroundMode === 'dynamic' ? '切换到纯色背景' : '切换到动态背景'"
            @click="themeStore.toggleBackground"
          >
            <el-icon><Monitor /></el-icon>
          </button>
          <button
            class="icon-btn"
            title="切换背景色"
            @click="themeStore.nextPalette"
          >
            <el-icon><MagicStick /></el-icon>
          </button>

          <template v-if="userStore.isAuthenticated">
            <el-dropdown
              trigger="click"
              @command="handleCommand"
            >
              <div class="user-pill">
                <el-avatar
                  :size="28"
                  :src="userStore.userInfo?.avatar"
                  class="user-avatar"
                >
                  {{ userStore.userInfo?.realName?.[0] || 'U' }}
                </el-avatar>
                <span class="user-name">{{ userStore.userInfo?.realName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="glass-dropdown">
                  <el-dropdown-item command="profile">
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="dashboard">
                    工作台
                  </el-dropdown-item>
                  <el-dropdown-item
                    divided
                    command="logout"
                  >
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button
              plain
              @click="router.push('/login')"
            >
              登录
            </el-button>
            <el-button
              type="primary"
              @click="router.push('/register')"
            >
              注册
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="landing-main">
      <section class="hero">
        <div class="hero-copy">
          <div class="hero-badge">
            <span class="dot" />
            SMART LEARNING PLATFORM
          </div>

          <h1 class="hero-title">
            让每一个问题都得到
            <span class="hero-highlight">专业解答</span>
          </h1>

          <p class="hero-subtitle">
            面向师生的问答与学习平台：提问、解答、沉淀知识，并用 AI 加速学习与写作。
          </p>

          <div class="hero-cta">
            <el-button
              type="primary"
              size="large"
              @click="handleGetStarted"
            >
              开始使用
            </el-button>
            <el-button
              size="large"
              @click="router.push('/forum')"
            >
              进入交流区
            </el-button>
          </div>

          <div class="metrics">
            <div class="metric glass-card">
              <div class="metric-value">
                {{ stats.users }}+
              </div>
              <div class="metric-label">
                活跃用户
              </div>
            </div>
            <div class="metric glass-card">
              <div class="metric-value">
                {{ stats.questions }}+
              </div>
              <div class="metric-label">
                问题总数
              </div>
            </div>
            <div class="metric glass-card">
              <div class="metric-value">
                {{ stats.answers }}+
              </div>
              <div class="metric-label">
                优质解答
              </div>
            </div>
          </div>
        </div>

        <div class="hero-panel glass-card">
          <div class="panel-title">
            即刻提升学习效率
          </div>
          <div class="panel-sub">
            更快提问、更快得到答案、更好沉淀知识。
          </div>

          <div class="panel-items">
            <div class="panel-item">
              <div class="panel-icon">
                AI
              </div>
              <div class="panel-text">
                <div class="panel-item-title">
                  智能问答
                </div>
                <div class="panel-item-desc">
                  多轮对话 + 流式输出，支持资料检索。
                </div>
              </div>
            </div>
            <div class="panel-item">
              <div class="panel-icon">
                Doc
              </div>
              <div class="panel-text">
                <div class="panel-item-title">
                  文档工作台
                </div>
                <div class="panel-item-desc">
                  查重、改写、结构优化，一站式完成。
                </div>
              </div>
            </div>
            <div class="panel-item">
              <div class="panel-icon">
                ∞
              </div>
              <div class="panel-text">
                <div class="panel-item-title">
                  知识沉淀
                </div>
                <div class="panel-item-desc">
                  问题与答案长期保存，可检索复用。
                </div>
              </div>
            </div>
          </div>

          <div class="panel-footer">
            <el-button
              class="panel-ghost"
              @click="scrollTo('features')"
            >
              查看核心功能
              <el-icon class="ml-6">
                <Right />
              </el-icon>
            </el-button>
          </div>
        </div>
      </section>

      <section
        id="features"
        class="section"
      >
        <div class="section-head">
          <div class="section-kicker">
            FEATURES
          </div>
          <h2 class="section-title">
            核心能力
          </h2>
          <p class="section-desc">
            简洁的信息层级 + 玻璃质感，让内容更聚焦。
          </p>
        </div>

        <div class="feature-grid">
          <div
            v-for="(feature, index) in features"
            :key="index"
            class="feature-card glass-card"
          >
            <div class="feature-top">
              <div class="feature-icon">
                <el-icon :size="22">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
              <div class="feature-title">
                {{ feature.title }}
              </div>
            </div>
            <div class="feature-desc">
              {{ feature.desc }}
            </div>
            <div class="feature-tags">
              <span
                v-for="tag in feature.tags"
                :key="tag"
                class="tag"
              >{{ tag }}</span>
            </div>
          </div>
        </div>
      </section>

      <section
        id="howto"
        class="section"
      >
        <div class="section-head">
          <div class="section-kicker">
            HOW TO USE
          </div>
          <h2 class="section-title">
            三步开始
          </h2>
          <p class="section-desc">
            从注册到沉淀，只需更少的操作。
          </p>
        </div>

        <div class="steps">
          <div
            v-for="(step, index) in steps"
            :key="index"
            class="step glass-card"
          >
            <div class="step-no">
              0{{ index + 1 }}
            </div>
            <div class="step-title">
              <el-icon :size="18">
                <component :is="step.icon" />
              </el-icon>
              <span>{{ step.title }}</span>
            </div>
            <div class="step-desc">
              {{ step.desc }}
            </div>
          </div>
        </div>
      </section>

      <section class="cta">
        <div class="cta-card glass-card">
          <div class="cta-title">
            准备好开始了吗？
          </div>
          <div class="cta-desc">
            加入我们，与更多师生一起学习与成长。
          </div>
          <div class="cta-actions">
            <el-button
              type="primary"
              size="large"
              @click="handleGetStarted"
            >
              立即开始
            </el-button>
            <el-button
              size="large"
              @click="router.push('/login')"
            >
              先登录
            </el-button>
          </div>
        </div>
      </section>
    </main>

    <footer class="landing-footer">
      <div class="footer-inner">
        <div class="footer-left">
          © 2025 师生答疑系统
        </div>
        <div class="footer-links">
          <a
            href="#"
            @click.prevent="scrollToTop"
          >回到顶部</a>
          <a
            href="#"
            @click.prevent="router.push('/forum')"
          >交流区</a>
          <a
            href="#"
            @click.prevent="router.push('/login')"
          >登录</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  School,
  Monitor,
  MagicStick,
  Right,
  ChatDotRound,
  Collection,
  TrendCharts,
  Medal,
  Star,
  UserFilled,
  Reading,
  Checked,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { logout as logoutApi } from '@/api/auth'
import FormulaBackdrop from '@/components/background/FormulaBackdrop.vue'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const stats = reactive({
  users: 500,
  questions: 1000,
  answers: 850,
})

const features = [
  {
    icon: ChatDotRound,
    title: '智能问答',
    desc: '学生提问，教师即时响应，构建高效的师生互动与知识桥梁。',
    tags: ['实时', '高效', '专业'],
  },
  {
    icon: Collection,
    title: '知识沉淀',
    desc: '问题与答案长期保存，形成可检索、可复用的知识库。',
    tags: ['知识库', '可检索', '可复用'],
  },
  {
    icon: TrendCharts,
    title: '数据分析',
    desc: '统计学习数据，洞察问题趋势，用数据驱动教学与自我提升。',
    tags: ['统计', '洞察', '趋势'],
  },
  {
    icon: Medal,
    title: '成就体系',
    desc: '用认可与激励鼓励贡献，让学习与答疑更有成就感。',
    tags: ['激励', '成长', '认可'],
  },
  {
    icon: Star,
    title: '精选推荐',
    desc: '智能推荐优质内容，帮助你更快定位到高价值解答。',
    tags: ['智能', '推荐', '个性化'],
  },
]

const steps = [
  {
    icon: UserFilled,
    title: '注册登录',
    desc: '创建账号并选择身份（学生/教师/管理员），完善基础信息。',
  },
  {
    icon: Reading,
    title: '提问/解答',
    desc: '发布问题、回答问题，支持交流区讨论与内容沉淀。',
  },
  {
    icon: Checked,
    title: '沉淀复用',
    desc: '将优质内容沉淀为知识库，持续积累与复用。',
  },
]

const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })
const scrollTo = (id) => {
  const el = document.getElementById(id)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleGetStarted = () => {
  if (userStore.isAuthenticated) {
    const role = userStore.userInfo?.role
    if (role === 'ADMIN') return router.push('/admin')
    if (role === 'TEACHER') return router.push('/teacher')
    if (role === 'STUDENT') return router.push('/student')
  }
  router.push('/login')
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await logoutApi()
      userStore.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
    } catch (error) {
      console.error('退出登录失败', error)
      ElMessage.error('退出登录失败')
    }
    return
  }

  if (command === 'profile') {
    const role = userStore.userInfo?.role
    if (role === 'STUDENT') return router.push('/student/profile')
    if (role === 'TEACHER') return router.push('/teacher/profile')
    return router.push('/home')
  }

  if (command === 'dashboard') {
    const role = userStore.userInfo?.role
    if (role === 'ADMIN') return router.push('/admin')
    if (role === 'TEACHER') return router.push('/teacher')
    if (role === 'STUDENT') return router.push('/student')
  }
}
</script>

<style scoped lang="scss">
.landing {
  min-height: 100vh;
  position: relative;
  background: transparent;
}

.landing-header {
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
  padding: 14px $spacing-lg;
}

.header-surface {
  max-width: 1200px;
  margin: 0 auto;
  padding: 10px 12px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 12px;
  background: $glass-floating;
  border: 1px solid $glass-border-light;
  border-radius: 999px;
  box-shadow: $shadow-glass-md;
  backdrop-filter: blur($blur-md) saturate(180%);
  -webkit-backdrop-filter: blur($blur-md) saturate(180%);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  min-width: 0;
}

.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, $color-primary, $re-purple);
  box-shadow: 0 10px 24px rgba(0, 122, 255, 0.22);
}

.brand-text {
  min-width: 0;
}

.brand-name {
  font-weight: $font-weight-semibold;
  color: $text-primary;
  font-size: 13px;
  letter-spacing: 0.3px;
  @include text-ellipsis;
}

.brand-sub {
  font-size: 11px;
  color: $text-tertiary;
  letter-spacing: 0.6px;
  text-transform: uppercase;
}

.nav {
  display: flex;
  gap: 4px;
  justify-content: center;
  align-items: center;
}

.nav-link {
  height: 38px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: $text-secondary;
  font-size: 13px;
  transition: all $transition-base;

  &:hover {
    background: rgba(255, 255, 255, 0.55);
    color: $re-purple;
    text-decoration: none;
  }

  &.is-active {
    background: rgba(255, 255, 255, 0.8);
    color: $text-primary;
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: $shadow-glass-sm;
  }
}

.actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.icon-btn {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.5);
  color: $text-secondary;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-base;

  &:hover {
    background: rgba(255, 255, 255, 0.72);
    color: $re-purple;
    box-shadow: $shadow-glass-sm;
  }
}

.user-pill {
  height: 38px;
  padding: 0 12px 0 8px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.55);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    background: rgba(255, 255, 255, 0.72);
    box-shadow: $shadow-glass-sm;
  }
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.9);
}

.user-name {
  max-width: 140px;
  font-size: 13px;
  color: $text-primary;
  @include text-ellipsis;
}

.landing-main {
  position: relative;
  z-index: 1;
  padding: 18px $spacing-lg 64px;
}

.hero {
  max-width: 1200px;
  margin: 0 auto;
  padding: 56px 0 20px;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 22px;
  align-items: start;
}

.hero-copy {
  padding: 22px 6px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.55);
  color: $text-secondary;
  font-size: 12px;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  backdrop-filter: blur($blur-sm);

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: linear-gradient(135deg, $color-primary, $re-purple);
    box-shadow: 0 0 0 4px rgba(139, 128, 249, 0.14);
  }
}

.hero-title {
  margin-top: 14px;
  font-size: 46px;
  letter-spacing: -0.03em;
  line-height: 1.05;
  color: $text-primary;
}

.hero-highlight {
  background: linear-gradient(135deg, $color-primary, $re-purple);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  margin-top: 16px;
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.7;
  max-width: 48ch;
}

.hero-cta {
  margin-top: 22px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.metrics {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.metric {
  padding: 14px 14px 12px;
  text-align: left;
}

.metric-value {
  font-size: 20px;
  font-weight: $font-weight-bold;
  color: $text-primary;
}

.metric-label {
  margin-top: 6px;
  font-size: 12px;
  color: $text-tertiary;
  letter-spacing: 0.4px;
}

.hero-panel {
  padding: 22px;
}

.panel-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.panel-sub {
  margin-top: 8px;
  color: $text-secondary;
  font-size: 13px;
  line-height: 1.6;
}

.panel-items {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.panel-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: $radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.45);
}

.panel-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.18), rgba(139, 128, 249, 0.12));
  border: 1px solid rgba(255, 255, 255, 0.85);
}

.panel-item-title {
  font-size: 13px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.panel-item-desc {
  margin-top: 4px;
  font-size: 12px;
  color: $text-secondary;
  line-height: 1.55;
}

.panel-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.panel-ghost {
  border-radius: 999px !important;
}

.ml-6 {
  margin-left: 6px;
}

.section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 58px 0 0;
}

.section-head {
  max-width: 640px;
}

.section-kicker {
  font-size: 12px;
  letter-spacing: 0.9px;
  color: $text-tertiary;
}

.section-title {
  margin-top: 10px;
  font-size: 28px;
  letter-spacing: -0.02em;
  color: $text-primary;
}

.section-desc {
  margin-top: 10px;
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.7;
}

.feature-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.feature-card {
  padding: 18px;
}

.feature-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feature-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.75), rgba(255, 255, 255, 0.4));
  display: flex;
  align-items: center;
  justify-content: center;
  color: $re-purple;
}

.feature-title {
  font-size: 15px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.feature-desc {
  margin-top: 10px;
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.65;
}

.feature-tags {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  color: $text-secondary;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.75);
}

.steps {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.step {
  padding: 18px;
}

.step-no {
  font-size: 12px;
  letter-spacing: 1px;
  color: $text-tertiary;
}

.step-title {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.step-desc {
  margin-top: 10px;
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.7;
}

.cta {
  max-width: 1200px;
  margin: 0 auto;
  padding: 64px 0 0;
}

.cta-card {
  padding: 26px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-radius: $radius-2xl;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.86), rgba(255, 255, 255, 0.62)),
    $glass-tint-student;
}

.cta-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.cta-desc {
  margin-top: 6px;
  font-size: 13px;
  color: $text-secondary;
}

.cta-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.landing-footer {
  position: relative;
  z-index: 1;
  padding: 26px $spacing-lg 36px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: $text-tertiary;
  font-size: 13px;
}

.footer-links {
  display: flex;
  gap: 16px;

  a {
    color: $text-tertiary;
    text-decoration: none;

    &:hover {
      color: $text-secondary;
      text-decoration: none;
    }
  }
}

@media (max-width: 1100px) {
  .header-surface {
    grid-template-columns: 1fr auto;
    grid-template-areas:
      'brand actions'
      'nav nav';
    border-radius: $radius-2xl;
    padding: 12px;
  }

  .brand {
    grid-area: brand;
  }

  .nav {
    grid-area: nav;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .actions {
    grid-area: actions;
  }

  .hero {
    grid-template-columns: 1fr;
  }

  .metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .feature-grid,
  .steps {
    grid-template-columns: repeat(2, 1fr);
  }

  .cta-card {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .landing-header {
    padding: 12px 12px;
  }

  .hero-title {
    font-size: 34px;
  }

  .metrics {
    grid-template-columns: 1fr;
  }

  .feature-grid,
  .steps {
    grid-template-columns: 1fr;
  }

  .user-name {
    display: none;
  }
}
</style>

