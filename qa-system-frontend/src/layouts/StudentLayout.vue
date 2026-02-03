<template>
  <div class="layout-container">
    <!-- 动态/纯色背景 (基于 ThemeStore) -->
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

    <!-- 顶部分离式导航 capsules -->
    <el-header class="layout-header">
      <div class="header-pills-container">
        <!-- Logo Pill -->
        <div
          class="header-pill logo-pill"
          @click="router.push('/home')"
        >
          <div class="logo-circle">
            <el-icon><School /></el-icon>
          </div>
          <span class="logo-text-short">学生面板</span>
        </div>
        
        <!-- Navigation Pill -->
        <div class="header-pill nav-pill">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
            class="pill-menu"
            :ellipsis="false"
          >
            <el-menu-item index="/student/questions">
              <el-icon><QuestionFilled /></el-icon>
              <span>主页</span>
            </el-menu-item>
            <el-menu-item index="/student/ask">
              <el-icon><EditPen /></el-icon>
              <span>我要提问</span>
            </el-menu-item>
            <el-menu-item
              index="/student/ai-assistant"
              class="pill-active-target"
            >
              <el-icon><Opportunity /></el-icon>
              <span>AI助手</span>
            </el-menu-item>
            <el-sub-menu
              index="more"
              popper-class="glass-dropdown"
            >
              <template #title>
                <el-icon><MoreFilled /></el-icon>
                <span>更多</span>
              </template>
              <el-menu-item index="/student/follows">
                <el-icon><Star /></el-icon>
                <span>我的关注</span>
              </el-menu-item>
              <el-menu-item index="/student/collections">
                <el-icon><StarFilled /></el-icon>
                <span>我的收藏</span>
              </el-menu-item>
              <el-menu-item index="/student/doc-studio">
                <el-icon><Document /></el-icon>
                <span>文档工作台</span>
              </el-menu-item>
              <el-menu-item index="/student/chat">
                <el-icon><ChatLineRound /></el-icon>
                <span>消息</span>
              </el-menu-item>
              <el-menu-item index="/student/forum">
                <el-icon><ChatDotSquare /></el-icon>
                <span>交流区</span>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
        
        <!-- User & Actions Pill -->
        <div class="header-pill user-pill">
          <button
            class="icon-pill-btn"
            :title="themeStore.backgroundMode === 'dynamic' ? '切换到纯色背景' : '切换到动态背景'"
            @click="themeStore.toggleBackground"
          >
            <el-icon><Monitor /></el-icon>
          </button>
          <button
            class="icon-pill-btn"
            title="切换背景色"
            @click="themeStore.nextPalette"
          >
            <el-icon><MagicStick /></el-icon>
          </button>
          <el-dropdown
            trigger="click"
            @command="handleCommand"
          >
            <div class="user-capsule">
              <el-avatar
                :size="28"
                :src="userStore.userInfo?.avatar"
                class="mini-avatar"
              >
                {{ userStore.userInfo?.realName?.[0] || '学' }}
              </el-avatar>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="glass-dropdown">
                <el-dropdown-item command="profile">
                  个人中心
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
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="layout-main">
      <div class="main-content">
        <router-view v-slot="{ Component }">
          <transition
            name="page"
            mode="out-in"
          >
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </el-main>
    
    <!-- 页脚 (Minimal) -->
    <el-footer class="layout-footer">
      <p>&copy; 2025 答疑系统 · Premium Experience</p>
    </el-footer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  School, QuestionFilled, EditPen, Opportunity, MoreFilled, 
  Monitor, MagicStick, Iphone, Reading,
  Star, StarFilled, Document, ChatLineRound, ChatDotSquare
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { logout as logoutApi } from '@/api/auth'
import FormulaBackdrop from '@/components/background/FormulaBackdrop.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()

const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '确认', { type: 'warning' })
      await logoutApi()
      userStore.logout()
      router.push('/login')
    } catch {}
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background: transparent;
}

// ==================== Top Modular Header ====================
.layout-header {
  height: auto;
  padding: $spacing-lg 0;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.header-pills-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: $spacing-md;
}

// Logo Pill
.logo-pill {
  cursor: pointer;
  gap: $spacing-sm;
  padding: 0 $spacing-md;
  
  .logo-circle {
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, $color-primary, $re-purple);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 16px;
  }
  
  .logo-text-short {
    font-weight: $font-weight-bold;
    color: $text-primary;
    font-size: 14px;
    letter-spacing: 0.5px;
  }
}

// Nav Pill
.nav-pill {
  padding: 0 4px;
  
  .pill-menu {
    border: none;
    background: transparent;
    height: 48px;
    display: flex;
    align-items: center;
    
    :deep(.el-menu-item) {
      height: 40px;
      line-height: 40px;
      padding: 0 16px;
      margin: 0 2px;
      border-radius: 999px;
      border: none;
      color: $text-secondary;
      transition: all $transition-base;
      position: relative;
      overflow: hidden;
      
      &:hover {
        background: rgba(255, 255, 255, 0.4);
        color: $re-purple;
      }
      
      &.is-active {
        background: linear-gradient(135deg, rgba(139, 128, 249, 0.85), rgba(139, 128, 249, 0.4));
        color: #ffffff !important;
        font-weight: $font-weight-semibold;
        border: 1px solid rgba(255, 255, 255, 0.65);
        backdrop-filter: blur($blur-sm) saturate(180%);
        box-shadow: 0 10px 24px rgba(139, 128, 249, 0.35), inset 0 1px 1px rgba(255, 255, 255, 0.6);
      }
      
      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

// User Pill
.user-pill {
  gap: $spacing-sm;
  padding: 0 8px;
  
  .icon-pill-btn {
    background: transparent;
    border: none;
    color: $text-secondary;
    font-size: 18px;
    cursor: pointer;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    
    &:hover {
      background: rgba(255, 255, 255, 0.4);
      color: $re-purple;
    }
  }
  
  .user-capsule {
    cursor: pointer;
    .mini-avatar {
      border: 2px solid white;
    }
  }
}

// ==================== Content Area ====================
.layout-main {
  flex: 1;
  z-index: 1;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-xl;
}

// Sub-nav for tabs inside content (Reference Style)
.content-sub-nav {
  margin-bottom: $spacing-xl;
  display: flex;
  justify-content: center;
  
  .glass-pill-tab-group {
    height: 42px;
    padding: 0 4px;
    background: rgba(255, 255, 255, 0.6);
  }
  
  .tab-pill {
    background: transparent;
    border: none;
    padding: 0 20px;
    height: 34px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: $font-weight-medium;
    color: $text-secondary;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
    transition: all $transition-base;
    
    &:hover {
      color: $re-purple;
    }
    
    &.active {
      background: $re-purple;
      color: white;
      box-shadow: 0 4px 10px rgba(139, 128, 249, 0.3);
    }
  }
}

// Floating Bubble
.layout-footer {
  text-align: center;
  padding: $spacing-xl;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

@media (max-width: $breakpoint-md) {
  .header-pills-container {
    flex-wrap: wrap;
    padding: 0 16px;
  }
}
</style>
