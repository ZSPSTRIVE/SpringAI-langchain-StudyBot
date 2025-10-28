<template>
  <el-container class="layout-container">
    <!-- 左侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-section" @click="router.push('/home')">
        <el-icon class="logo-icon"><Setting /></el-icon>
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-text">
            <h2>管理后台</h2>
            <span class="logo-subtitle">Admin Panel</span>
          </div>
        </transition>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="aside-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
        <el-menu-item index="/admin/students">
          <el-icon><User /></el-icon>
          <template #title>学生管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/teachers">
          <el-icon><UserFilled /></el-icon>
          <template #title>教师管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/subjects">
          <el-icon><Reading /></el-icon>
          <template #title>科目管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/questions">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>问题管理</template>
        </el-menu-item>
      </el-menu>
      
      <!-- 折叠按钮 -->
      <el-tooltip :content="isCollapse ? '展开菜单' : '收起菜单'" placement="right">
        <div class="collapse-btn" @click="isCollapse = !isCollapse">
          <el-icon>
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
        </div>
      </el-tooltip>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container class="right-container">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-content">
          <!-- 面包屑 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>

          <!-- 右侧操作区 -->
          <div class="header-actions">
            <!-- 返回首页 -->
            <el-tooltip content="返回首页" placement="bottom">
              <el-button 
                circle 
                class="action-btn"
                @click="router.push('/home')"
              >
                <el-icon><HomeFilled /></el-icon>
              </el-button>
            </el-tooltip>
            
            <!-- 用户下拉菜单 -->
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-info">
                <el-avatar 
                  :size="36" 
                  :src="userStore.userInfo?.avatar"
                  class="user-avatar"
                >
                  {{ userStore.userInfo?.realName?.[0] || '管' }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.realName || '管理员' }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="home">
                    <el-icon><HomeFilled /></el-icon>
                    <span>返回首页</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
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
            <transition name="page" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
      
      <!-- 页脚 -->
      <el-footer class="layout-footer">
        <div class="footer-content">
          <p>&copy; 2025 师生答疑系统 · 管理端</p>
        </div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, DataAnalysis, User, UserFilled, Reading, ChatDotRound,
  HomeFilled, SwitchButton, ArrowDown, Expand, Fold
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

// 当前页面名称
const currentPageName = computed(() => {
  const pathMap = {
    '/admin/dashboard': '数据统计',
    '/admin/students': '学生管理',
    '/admin/teachers': '教师管理',
    '/admin/subjects': '科目管理',
    '/admin/questions': '问题管理'
  }
  return pathMap[route.path] || '管理后台'
})

// 处理用户下拉菜单命令
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '退出确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      await logoutApi()
      userStore.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('退出失败:', error)
        ElMessage.error('退出失败，请重试')
      }
    }
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'home') {
    router.push('/home')
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

// ==================== 整体布局 ====================
.layout-container {
  min-height: 100vh;
}

// ==================== 左侧边栏 ====================
.layout-aside {
  background: $text-primary;
  display: flex;
  flex-direction: column;
  transition: width $transition-base;
  box-shadow: $shadow-lg;
  z-index: $z-index-sticky;
}

// Logo 区域
.logo-section {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  padding: 0 $spacing-lg;
  cursor: pointer;
  border-bottom: 1px solid rgba(white, 0.1);
  transition: all $transition-fast;
  
  &:hover {
    background: rgba(white, 0.05);
  }
  
  .logo-icon {
    font-size: $font-size-3xl;
    color: $admin-color;
    flex-shrink: 0;
    animation: spin 10s linear infinite;
  }
  
  .logo-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
    overflow: hidden;
    
    h2 {
      margin: 0;
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: white;
      line-height: 1;
      white-space: nowrap;
    }
    
    .logo-subtitle {
      font-size: $font-size-xs;
      color: rgba(white, 0.6);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      white-space: nowrap;
    }
  }
}

// 折叠状态的Logo
.layout-aside {
  &.is-collapse,
  &[style*="width: 64px"] {
    .logo-section {
      padding: 0;
      justify-content: center;
    }
  }
}

// 侧边菜单
.aside-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: $spacing-md 0;
  overflow-y: auto;
  
  :deep(.el-menu-item) {
    margin: $spacing-xs $spacing-md;
    border-radius: $radius-md;
    color: rgba(white, 0.7);
    transition: all $transition-fast;
    height: 48px;
    line-height: 48px;
    display: flex;
    align-items: center;
    padding: 0 $spacing-lg !important;
    
    &:hover {
      background: rgba(white, 0.1);
      color: white;
    }
    
    &.is-active {
      background: $admin-color;
      color: white;
      box-shadow: $shadow-md;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 60%;
        background: white;
        border-radius: 0 $radius-md $radius-md 0;
      }
    }
    
    .el-icon {
      color: inherit;
      margin-right: 12px;
      font-size: 18px;
      vertical-align: middle;
      flex-shrink: 0;
    }
    
    span {
      vertical-align: middle;
      flex: 1;
    }
  }
  
  // 折叠状态
  &.el-menu--collapse {
    :deep(.el-menu-item) {
      justify-content: center;
      padding: 0 !important;
      
      .el-icon {
        margin-right: 0;
        font-size: 20px;
      }
      
      span {
        display: none;
      }
    }
  }
}

// 折叠按钮
.collapse-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(white, 0.7);
  border-top: 1px solid rgba(white, 0.1);
  transition: all $transition-fast;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 80%;
    height: 1px;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(white, 0.2),
      transparent
    );
  }
  
  &:hover {
    background: rgba(white, 0.1);
    color: white;
    
    .el-icon {
      transform: scale(1.1);
    }
  }
  
  &:active {
    background: rgba(white, 0.15);
  }
  
  .el-icon {
    font-size: 20px;
    transition: transform $transition-fast;
  }
}

// ==================== 右侧容器 ====================
.right-container {
  display: flex;
  flex-direction: column;
  background: $bg-secondary;
}

// ==================== 顶部导航栏 ====================
.layout-header {
  background: $bg-primary;
  border-bottom: 1px solid $border-color;
  padding: 0 $spacing-xl;
  height: 64px;
  box-shadow: $shadow-sm;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  gap: $spacing-lg;
}

// 面包屑
.breadcrumb {
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: $text-secondary;
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      transition: color $transition-fast;
      
      &:hover {
        color: $admin-color;
      }
    }
    
    &:last-child .el-breadcrumb__inner {
      color: $text-primary;
      font-weight: $font-weight-medium;
    }
  }
}

// 右侧操作区
.header-actions {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  
  .action-btn {
    background: $bg-hover;
    border: none;
    color: $text-primary;
    transition: all $transition-fast;
    
    &:hover {
      background: $admin-color;
      color: white;
      transform: translateY(-2px);
      box-shadow: $shadow-md;
    }
  }
}

// 用户信息下拉
.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  cursor: pointer;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-lg;
  transition: all $transition-fast;
  
  &:hover {
    background: $bg-hover;
  }
  
  .user-avatar {
    background: $admin-color;
    font-weight: $font-weight-semibold;
    border: 2px solid white;
    box-shadow: $shadow-sm;
  }
  
  .username {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $text-primary;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .dropdown-icon {
    color: $text-secondary;
    font-size: $font-size-sm;
    transition: transform $transition-fast;
  }
  
  &:hover .dropdown-icon {
    transform: rotate(180deg);
  }
}

// 下拉菜单项增强
:deep(.el-dropdown-menu__item) {
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  transition: all $transition-fast;
  
  .el-icon {
    color: $text-secondary;
  }
  
  &:hover {
    background: $bg-hover;
    color: $admin-color;
    
    .el-icon {
      color: $admin-color;
    }
  }
}

// ==================== 主内容区 ====================
.layout-main {
  flex: 1;
  padding: 0;
}

.main-content {
  min-height: calc(100vh - 124px);
}

// 页面过渡动画
.page-enter-active,
.page-leave-active {
  transition: all $transition-base;
}

.page-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

// ==================== 页脚 ====================
.layout-footer {
  background: $bg-primary;
  border-top: 1px solid $border-color;
  padding: $spacing-md 0;
  height: auto;
}

.footer-content {
  text-align: center;
  color: $text-secondary;
  font-size: $font-size-sm;
  
  p {
    margin: 0;
  }
}

// ==================== 过渡动画 ====================
.fade-enter-active,
.fade-leave-active {
  transition: opacity $transition-base;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// ==================== 响应式设计 ====================

// 超大屏幕 (>1920px)
@media (min-width: 1920px) {
  .layout-container {
    max-width: 2400px;
    margin: 0 auto;
  }
  
  .header-content,
  .main-content,
  .footer-content {
    max-width: 2200px;
    margin: 0 auto;
  }
}

// 桌面 (1200px - 1920px)
@media (min-width: $breakpoint-lg) and (max-width: 1919px) {
  .layout-aside {
    width: 240px;
  }
  
  .aside-menu :deep(.el-menu-item) {
    font-size: $font-size-sm;
  }
}

// 平板横屏 (768px - 1199px)
@media (min-width: $breakpoint-md) and (max-width: $breakpoint-lg - 1) {
  .layout-aside {
    width: 200px;
  }
  
  .layout-header {
    height: 56px;
    
    .header-content {
      padding: 0 $spacing-md;
    }
  }
  
  .logo-section {
    height: 56px;
    padding: 0 $spacing-md;
    
    .logo-text h2 {
      font-size: $font-size-md;
    }
    
    .logo-subtitle {
      display: none;
    }
  }
  
  .aside-menu :deep(.el-menu-item) {
    margin: $spacing-xs $spacing-sm;
    font-size: $font-size-sm;
    height: 44px;
    line-height: 44px;
  }
  
  .user-info .username {
    display: none;
  }
}

// 平板竖屏 & 大手机 (576px - 767px)
@media (min-width: $breakpoint-sm) and (max-width: $breakpoint-md - 1) {
  .layout-aside {
    position: fixed;
    left: -260px;
    top: 0;
    bottom: 0;
    z-index: $z-index-modal;
    transition: left $transition-base;
    
    &.mobile-visible {
      left: 0;
    }
  }
  
  .layout-header {
    height: 56px;
    
    .breadcrumb {
      display: none;
    }
    
    .header-content {
      padding: 0 $spacing-md;
    }
  }
  
  .main-content {
    padding: $spacing-md;
  }
  
  .user-info {
    .username {
      display: none;
    }
    
    .user-avatar {
      margin-right: 0;
    }
  }
  
  // 添加遮罩层
  .layout-container {
    &::before {
      content: '';
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      z-index: $z-index-modal - 1;
      display: none;
    }
    
    &.mobile-menu-open::before {
      display: block;
    }
  }
}

// 手机 (<576px)
@media (max-width: $breakpoint-sm - 1) {
  .layout-aside {
    position: fixed;
    left: -100%;
    top: 0;
    bottom: 0;
    width: 260px;
    z-index: $z-index-modal;
    transition: left $transition-base;
    
    &.mobile-visible {
      left: 0;
    }
  }
  
  .layout-header {
    height: 50px;
    
    .header-content {
      padding: 0 $spacing-sm;
    }
    
    .breadcrumb {
      display: none;
    }
    
    .action-btn {
      display: none;
    }
  }
  
  .layout-main {
    padding: $spacing-sm;
  }
  
  .main-content {
    padding: $spacing-sm;
  }
  
  .user-info {
    .username {
      display: none;
    }
    
    .user-avatar {
      width: 32px;
      height: 32px;
      margin-right: 0;
    }
    
    .dropdown-icon {
      display: none;
    }
  }
  
  .layout-footer {
    padding: $spacing-sm 0;
    font-size: $font-size-xs;
  }
  
  // 添加遮罩层
  .layout-container::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: $z-index-modal - 1;
    display: none;
    opacity: 0;
    transition: opacity $transition-base;
  }
  
  .layout-container.mobile-menu-open::before {
    display: block;
    opacity: 1;
  }
}

// Mac Retina屏幕优化
@media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
  .logo-icon,
  .aside-menu :deep(.el-icon) {
    transform: translateZ(0);
    -webkit-font-smoothing: antialiased;
  }
}

// 打印样式
@media print {
  .layout-aside,
  .layout-header,
  .layout-footer {
    display: none;
  }
  
  .layout-main {
    padding: 0;
  }
}
</style>
