<template>
  <el-container class="layout-container">
    <!-- 动态/纯色背景 (基于 ThemeStore) -->
    <div v-if="themeStore.backgroundMode === 'dynamic'" class="glow-bg" style="--glow-color-1: var(--theme-glow-1); --glow-color-2: var(--theme-glow-2);"></div>
    <div v-else class="pure-bg"></div>

    <!-- Sidebar (Glass Panel) -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside glass-panel">
      <!-- Logo Pill in Sidebar -->
      <div class="sidebar-logo" @click="router.push('/home')">
        <div class="logo-circle admin-theme">
          <el-icon><Setting /></el-icon>
        </div>
        <transition name="fade">
          <span v-show="!isCollapse" class="logo-text-short">管理系统</span>
        </transition>
      </div>
      
      <!-- Menu -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="aside-menu admin-aside-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>统计</template>
        </el-menu-item>
        <el-menu-item index="/admin/students">
          <el-icon><User /></el-icon>
          <template #title>学生</template>
        </el-menu-item>
        <el-menu-item index="/admin/teachers">
          <el-icon><UserFilled /></el-icon>
          <template #title>教师</template>
        </el-menu-item>
        <el-menu-item index="/admin/questions">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>问题</template>
        </el-menu-item>
        <el-menu-item index="/admin/documents">
          <el-icon><Document /></el-icon>
          <template #title>文档</template>
        </el-menu-item>
      </el-menu>
      
      <!-- Collapse Trigger -->
      <div class="collapse-btn-wrapper">
        <div class="collapse-pill-btn" @click="isCollapse = !isCollapse">
          <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
        </div>
      </div>
    </el-aside>

    <!-- Right Container -->
    <el-container class="right-container">
      <!-- Header with modular capsules -->
      <el-header class="layout-header">
        <div class="admin-header-row">
          <!-- Breadcrumb Pill -->
          <div class="header-pill breadcrumb-pill">
            <el-breadcrumb separator="/" class="admin-breadcrumb">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <!-- Actions Pill -->
          <div class="header-pill action-pill">
            <button class="icon-pill-btn" @click="themeStore.toggleBackground" :title="themeStore.backgroundMode === 'dynamic' ? '切换到纯色背景' : '切换到动态背景'">
              <el-icon><Monitor /></el-icon>
            </button>
            <button class="icon-pill-btn" @click="themeStore.nextPalette" title="切换背景色">
              <el-icon><MagicStick /></el-icon>
            </button>
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-capsule">
                <el-avatar :size="28" :src="userStore.userInfo?.avatar" class="mini-avatar admin-border">
                  {{ userStore.userInfo?.realName?.[0] || '管' }}
                </el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="glass-dropdown">
                  <el-dropdown-item command="profile">管理中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">注销</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="layout-main">
        <div class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="page" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
      
      <!-- Footer -->
      <el-footer class="layout-footer">
        <p>&copy; 2025 答疑系统 · Admin Pro</p>
      </el-footer>

    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Setting, DataAnalysis, User, UserFilled, ChatDotRound,
  Expand, Fold, Document, Monitor, MagicStick
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { logout as logoutApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const currentPageName = computed(() => {
  const pathMap = {
    '/admin/dashboard': '统计',
    '/admin/students': '学生',
    '/admin/teachers': '教师',
    '/admin/questions': '问题',
    '/admin/documents': '文档'
  }
  return pathMap[route.path] || '管理'
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要注销吗？', '确认', { type: 'warning' })
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
  background: transparent;
  position: relative;
  overflow: hidden;
}

// ==================== Sidebar Refinement ====================
.layout-aside {
  margin: $spacing-md;
  border-radius: $radius-xl;
  height: calc(100vh - 32px);
  transition: width $transition-base;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  cursor: pointer;
  
  .logo-circle.admin-theme {
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, $color-warning, #FF5252);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
  }
  
  .logo-text-short {
    font-weight: $font-weight-bold;
    color: $text-primary;
    white-space: nowrap;
  }
}

.admin-aside-menu {
  flex: 1;
  border: none;
  background: transparent;
  
  :deep(.el-menu-item) {
    margin: 4px 12px;
    border-radius: $radius-lg;
    height: 48px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.4);
      color: $color-warning;
    }
    
    &.is-active {
      background: $color-warning;
      color: white !important;
    }
  }
}

.collapse-btn-wrapper {
  padding: 16px;
  display: flex;
  justify-content: center;
  
  .collapse-pill-btn {
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.4);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: $text-secondary;
    
    &:hover {
      color: $color-warning;
    }
  }
}

// ==================== Header Refinement ====================
.admin-header-row {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.breadcrumb-pill {
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.5);
}

.action-pill {
  gap: 16px;
  padding: 0 16px;
  
  .icon-pill-btn {
    background: transparent;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: $text-secondary;
    
    &:hover {
      color: $color-warning;
    }
  }
}

.mini-avatar.admin-border {
  border: 2px solid $color-warning-bg;
}

// ==================== Main ====================
.main-content {
  padding: $spacing-xl;
}

.layout-footer {
  text-align: center;
  padding: $spacing-xl;
  color: rgba(255, 255, 255, 0.4);
}
</style>
