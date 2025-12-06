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
        <el-menu-item index="/admin/documents">
          <el-icon><Document /></el-icon>
          <template #title>文档查重</template>
        </el-menu-item>
        <el-menu-item index="/admin/doc-config">
          <el-icon><Setting /></el-icon>
          <template #title>查重配置</template>
        </el-menu-item>
        <el-menu-item index="/admin/doc-sensitive-words">
          <el-icon><Setting /></el-icon>
          <template #title>敏感词配置</template>
        </el-menu-item>
        <el-menu-item index="/admin/doc-operation-logs">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>文档操作日志</template>
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
  HomeFilled, SwitchButton, ArrowDown, Expand, Fold, Document
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
    '/admin/questions': '问题管理',
    '/admin/documents': '文档查重',
    '/admin/doc-config': '查重与降重配置',
    '/admin/doc-sensitive-words': '敏感词配置',
    '/admin/doc-operation-logs': '文档操作日志'
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
  background: $neo-black;
  display: flex;
  flex-direction: column;
  transition: width 300ms $bounce-curve;
  border-right: 4px solid $neo-black;
  z-index: $z-index-sticky;
}

// Logo 区域
.logo-section {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  padding: 0 $spacing-lg;
  cursor: pointer;
  border-bottom: 3px solid rgba(white, 0.1);
  transition: all 150ms $bounce-curve;
  
  &:hover {
    background: rgba(white, 0.05);
  }
  
  .logo-icon {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    background: $neo-orange;
    color: $neo-black;
    border: 3px solid $neo-white;
    border-radius: 10px;
    box-shadow: 3px 3px 0 0 rgba(white, 0.2);
    flex-shrink: 0;
  }
  
  .logo-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
    overflow: hidden;
    
    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 900;
      color: white;
      line-height: 1;
      white-space: nowrap;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    
    .logo-subtitle {
      font-size: 9px;
      font-weight: 700;
      color: rgba(white, 0.5);
      text-transform: uppercase;
      letter-spacing: 1px;
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
    margin: 6px 12px;
    border-radius: 8px;
    color: rgba(white, 0.7);
    transition: all 150ms $bounce-curve;
    height: 44px;
    line-height: 44px;
    display: flex;
    align-items: center;
    padding: 0 16px !important;
    font-weight: 700;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border: 2px solid transparent;
    
    &:hover {
      background: rgba(white, 0.1);
      color: white;
      border-color: rgba(white, 0.2);
      transform: translate(-2px, -2px);
      box-shadow: 4px 4px 0 0 rgba(white, 0.1);
    }
    
    &.is-active {
      background: $neo-orange;
      color: $neo-black;
      border-color: $neo-white;
      box-shadow: 4px 4px 0 0 rgba(white, 0.2);
    }
    
    .el-icon {
      color: inherit;
      margin-right: 10px;
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
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(white, 0.7);
  border-top: 3px solid rgba(white, 0.1);
  transition: all 150ms $bounce-curve;
  
  &:hover {
    background: $neo-orange;
    color: $neo-black;
    
    .el-icon {
      transform: scale(1.2);
    }
  }
  
  .el-icon {
    font-size: 20px;
    transition: transform 150ms $bounce-curve;
  }
}

// ==================== 右侧容器 ====================
.right-container {
  display: flex;
  flex-direction: column;
  background: $neo-cream;
  position: relative;
  
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: radial-gradient(circle, rgba($neo-black, 0.04) 2px, transparent 2px);
    background-size: 24px 24px;
    pointer-events: none;
    z-index: 0;
  }
}

// ==================== 顶部导航栏 ====================
.layout-header {
  background: $neo-white;
  border-bottom: 4px solid $neo-black;
  padding: 0 $spacing-xl;
  height: 72px;
  position: relative;
  z-index: 1;
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
      color: $neo-black;
      opacity: 0.6;
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      font-size: 12px;
      transition: all 150ms;
      
      &:hover {
        opacity: 1;
        color: $neo-orange;
      }
    }
    
    &:last-child .el-breadcrumb__inner {
      color: $neo-black;
      opacity: 1;
      font-weight: 800;
    }
  }
}

// 右侧操作区
.header-actions {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  
  .action-btn {
    width: 40px;
    height: 40px;
    background: $neo-yellow;
    border: 3px solid $neo-black;
    color: $neo-black;
    box-shadow: 3px 3px 0 0 $neo-black;
    transition: all 150ms $bounce-curve;
    
    &:hover {
      transform: translate(-2px, -2px);
      box-shadow: 5px 5px 0 0 $neo-black;
    }
  }
}

// 用户信息下拉
.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  cursor: pointer;
  padding: 8px 16px;
  background: $neo-orange;
  border: 3px solid $neo-black;
  border-radius: 12px;
  box-shadow: 4px 4px 0 0 $neo-black;
  transition: all 150ms $bounce-curve;
  
  &:hover {
    transform: translate(-2px, -2px);
    box-shadow: 6px 6px 0 0 $neo-black;
  }
  
  .user-avatar {
    background: $neo-white;
    color: $neo-orange;
    font-weight: 900;
    border: 2px solid $neo-black;
  }
  
  .username {
    font-size: 14px;
    font-weight: 800;
    color: $neo-black;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .dropdown-icon {
    color: $neo-black;
    font-size: 14px;
    transition: transform 150ms $bounce-curve;
  }
  
  &:hover .dropdown-icon {
    transform: rotate(180deg);
  }
}

// ==================== 主内容区 ====================
.layout-main {
  flex: 1;
  padding: 0;
  position: relative;
  z-index: 1;
}

.main-content {
  min-height: calc(100vh - 140px);
  padding: $spacing-xl;
}

// 页面过渡动画
.page-enter-active,
.page-leave-active {
  transition: all 300ms $bounce-curve;
}

.page-enter-from {
  opacity: 0;
  transform: translateX(30px) rotate(1deg);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-30px) rotate(-1deg);
}

// ==================== 页脚 ====================
.layout-footer {
  background: $neo-black;
  border-top: 4px solid $neo-black;
  padding: $spacing-md 0 0;
  height: auto;
  position: relative;
  z-index: 1;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 6px;
    background: linear-gradient(90deg, $neo-orange 25%, $neo-yellow 25%, $neo-yellow 50%, $neo-blue 50%, $neo-blue 75%, $neo-pink 75%);
  }
}

.footer-content {
  text-align: center;
  color: $neo-white;
  font-size: $font-size-sm;
  padding-bottom: 16px;
  
  p {
    margin: 0;
    font-weight: 600;
  }
}

// ==================== 过渡动画 ====================
.fade-enter-active,
.fade-leave-active {
  transition: opacity 300ms $bounce-curve;
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
