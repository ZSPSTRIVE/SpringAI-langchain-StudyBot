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
        <!-- Logo Pill (Green) -->
        <div
          class="header-pill logo-pill"
          @click="router.push('/home')"
        >
          <div class="logo-circle teacher-theme">
            <el-icon><UserFilled /></el-icon>
          </div>
          <span class="logo-text-short">教师面板</span>
        </div>
        
        <!-- Navigation Pill -->
        <div class="header-pill nav-pill">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
            class="pill-menu teacher-pill-menu"
            :ellipsis="false"
          >
            <el-menu-item index="/teacher/answers">
              <el-icon><ChatLineSquare /></el-icon>
              <span>待办</span>
            </el-menu-item>
            <el-menu-item index="/teacher/questions">
              <el-icon><QuestionFilled /></el-icon>
              <span>广场</span>
            </el-menu-item>
            <el-menu-item index="/teacher/ai-assistant">
              <el-icon><Opportunity /></el-icon>
              <span>AI资源</span>
            </el-menu-item>
            <el-menu-item index="/teacher/doc-studio">
              <el-icon><Document /></el-icon>
              <span>文档库</span>
            </el-menu-item>
            <el-menu-item index="/teacher-more">
              <el-icon><MoreFilled /></el-icon>
              <span>更多</span>
            </el-menu-item>
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
                class="mini-avatar teacher-border"
              >
                {{ userStore.userInfo?.realName?.[0] || '教' }}
              </el-avatar>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="glass-dropdown">
                <el-dropdown-item command="profile">
                  教师档案
                </el-dropdown-item>
                <el-dropdown-item
                  divided
                  command="logout"
                >
                  退出
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
    
    <!-- 页脚 -->
    <el-footer class="layout-footer">
      <p>&copy; 2025 答疑系统 · Teacher Elite Edition</p>
    </el-footer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  UserFilled, QuestionFilled, ChatLineSquare, ChatLineRound, Document, Opportunity,
  MoreFilled, Edit, Check, Warning, DataLine, Monitor, MagicStick
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
      await ElMessageBox.confirm('确定要退出吗？', '确认', { type: 'warning' })
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

// ==================== modular Header ====================
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

// Pills styling
.logo-pill {
  cursor: pointer;
  gap: $spacing-sm;
  padding: 0 $spacing-md;
  
  .logo-circle.teacher-theme {
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, $color-success, #00BFA5);
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
  }
}

.nav-pill {
  padding: 0 4px;
  
  .pill-menu.teacher-pill-menu {
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
      
      &:hover {
        background: rgba(255, 255, 255, 0.4);
        color: $color-success;
      }
      
      &.is-active {
        background: $color-success;
        color: white !important;
        font-weight: $font-weight-semibold;
      }
    }
  }
}

.user-pill {
  gap: $spacing-sm;
  padding: 0 8px;
  
  .icon-pill-btn:hover {
    color: $color-success;
  }
  
  .mini-avatar.teacher-border {
    border: 2px solid $color-success-bg;
  }
}

// ==================== Content ====================
.layout-main {
  flex: 1;
  position: relative;
  z-index: 1;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-xl;
}

.content-sub-nav {
  margin-bottom: $spacing-xl;
  display: flex;
  justify-content: center;
  
  .teacher-sub-nav {
    background: rgba(255, 255, 255, 0.6);
  }
  
  .tab-pill.teacher-active.active {
    background: $color-success;
    box-shadow: 0 4px 10px rgba($color-success, 0.3);
  }
}

// Floating Bubble
.layout-footer {
  text-align: center;
  padding: $spacing-xl;
  color: rgba(255, 255, 255, 0.5);
  font-size: 11px;
}
</style>
