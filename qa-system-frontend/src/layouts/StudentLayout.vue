<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="layout-header">
      <div class="header-content">
        <!-- Logo 和标题 -->
        <div class="logo-section" @click="router.push('/home')">
          <el-icon class="logo-icon"><School /></el-icon>
          <div class="logo-text">
            <h2>学生工作台</h2>
            <span class="logo-subtitle">Student Workspace</span>
          </div>
        </div>
        
        <!-- 导航菜单 -->
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          class="header-menu"
          :ellipsis="false"
        >
          <el-menu-item index="/student/questions">
            <el-icon><QuestionFilled /></el-icon>
            <span>问题广场</span>
          </el-menu-item>
          <el-menu-item index="/student/ask">
            <el-icon><EditPen /></el-icon>
            <span>我要提问</span>
          </el-menu-item>
          <el-menu-item index="/student/follows">
            <el-icon><Star /></el-icon>
            <span>我的关注</span>
          </el-menu-item>
          <el-menu-item index="/student/collections">
            <el-icon><StarFilled /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="/student/ai-assistant">
            <el-icon><Opportunity /></el-icon>
            <span>AI助手</span>
          </el-menu-item>
          <el-menu-item index="/student/doc-studio">
            <el-icon><Document /></el-icon>
            <span>文档工作台</span>
          </el-menu-item>
          <el-menu-item index="/forum">
            <el-icon><ChatDotSquare /></el-icon>
            <span>交流区</span>
          </el-menu-item>
        </el-menu>
        
        <!-- 右侧操作区 -->
        <div class="header-actions">
          <!-- 返回首页按钮 -->
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
                {{ userStore.userInfo?.realName?.[0] || '学' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || '学生' }}</span>
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
        <p>&copy; 2025 师生答疑系统 · 学生端</p>
        <p class="footer-links">
          <a href="#">帮助中心</a>
          <span>·</span>
          <a href="#">使用指南</a>
          <span>·</span>
          <a href="#">联系我们</a>
        </p>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  School, QuestionFilled, EditPen, Star, StarFilled, 
  ChatDotSquare, HomeFilled, User, SwitchButton, ArrowDown, Document, Opportunity
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

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
  display: flex;
  flex-direction: column;
  background: $neo-cream;
  position: relative;
  
  // 波点背景
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
  padding: 0;
  z-index: $z-index-sticky;
  position: sticky;
  top: 0;
  height: 72px;
}

.header-content {
  max-width: 1600px;
  height: 100%;
  margin: 0 auto;
  padding: 0 $spacing-xl;
  display: flex;
  align-items: center;
  gap: $spacing-xl;
}

// Logo 区域
.logo-section {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  cursor: pointer;
  transition: all 150ms $bounce-curve;
  
  &:hover {
    transform: translate(-2px, -2px);
  }
  
  .logo-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    background: $neo-purple;
    color: $neo-white;
    border: 3px solid $neo-black;
    border-radius: 12px;
    box-shadow: 4px 4px 0 0 $neo-black;
  }
  
  .logo-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 900;
      color: $neo-black;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    
    .logo-subtitle {
      font-size: 10px;
      font-weight: 700;
      color: $neo-black;
      opacity: 0.5;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
  }
}

// 导航菜单
.header-menu {
  flex: 1;
  border: none;
  background: transparent;
  
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    margin: 0 4px;
    padding: 0 16px;
    font-weight: 700;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border: 2px solid transparent;
    border-radius: 8px;
    transition: all 150ms $bounce-curve;
    
    &:hover {
      background: $neo-purple;
      background: rgba($neo-purple, 0.1);
      border-color: $neo-black;
      box-shadow: 3px 3px 0 0 $neo-black;
      transform: translate(-2px, -2px);
    }
    
    &.is-active {
      background: $neo-purple;
      color: $neo-white;
      border-color: $neo-black;
      box-shadow: 3px 3px 0 0 $neo-black;
    }
    
    .el-icon {
      margin-right: 6px;
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
      background: $neo-yellow-dark;
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
  background: $neo-purple;
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
    color: $neo-purple;
    font-weight: 900;
    border: 2px solid $neo-black;
  }
  
  .username {
    font-size: 14px;
    font-weight: 800;
    color: $neo-white;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .dropdown-icon {
    color: $neo-white;
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
  background: transparent;
  padding: 0;
  position: relative;
  z-index: 1;
}

.main-content {
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
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
  padding: $spacing-lg 0 0;
  height: auto;
  position: relative;
  z-index: 1;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 8px;
    display: flex;
    background: linear-gradient(90deg, $neo-yellow 25%, $neo-blue 25%, $neo-blue 50%, $neo-pink 50%, $neo-pink 75%, $neo-green 75%);
  }
}

.footer-content {
  max-width: 1600px;
  margin: 0 auto;
  text-align: center;
  color: $neo-white;
  font-size: $font-size-sm;
  padding-bottom: 20px;
  
  p {
    margin: $spacing-xs 0;
    font-weight: 600;
  }
  
  .footer-links {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-md;
    margin-top: $spacing-sm;
    
    a {
      color: rgba($neo-white, 0.7);
      font-weight: 600;
      transition: color 150ms;
      
      &:hover {
        color: $neo-yellow;
      }
    }
    
    span {
      color: rgba($neo-white, 0.3);
    }
  }
}

// ==================== 响应式设计 ====================
@media (max-width: $breakpoint-lg) {
  .header-content {
    padding: 0 $spacing-md;
  }
  
  .logo-text .logo-subtitle {
    display: none;
  }
  
  .header-menu {
    :deep(.el-menu-item span) {
      display: none;
    }
  }
  
  .main-content {
    padding: $spacing-md;
  }
}

@media (max-width: $breakpoint-md) {
  .logo-section {
    .logo-text {
      display: none;
    }
  }
  
  .header-actions .action-btn {
    display: none;
  }
  
  .user-info .username {
    display: none;
  }
}
</style>


