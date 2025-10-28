<template>
  <div class="page-header">
    <div class="page-header-content">
      <!-- 左侧：返回按钮 + 面包屑 -->
      <div class="page-header-left">
        <el-button 
          v-if="showBack" 
          class="back-button" 
          :icon="ArrowLeft" 
          circle
          @click="handleBack"
        />
        
        <div class="breadcrumb-wrapper">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item 
              v-if="showHome"
              :to="{ name: 'Home' }"
              class="breadcrumb-home"
            >
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-breadcrumb-item>
            
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbs"
              :key="index"
              :to="item.to"
            >
              {{ item.label }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </div>

      <!-- 中间：标题和描述 -->
      <div class="page-header-center">
        <h1 class="page-title">{{ title }}</h1>
        <p v-if="description" class="page-description">{{ description }}</p>
      </div>

      <!-- 右侧：操作按钮 -->
      <div class="page-header-right">
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ArrowLeft, HomeFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  showBack: {
    type: Boolean,
    default: true
  },
  showHome: {
    type: Boolean,
    default: true
  },
  breadcrumbs: {
    type: Array,
    default: () => []
  },
  backPath: {
    type: String,
    default: ''
  }
})

const handleBack = () => {
  if (props.backPath) {
    router.push(props.backPath)
  } else {
    router.back()
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-header {
  background: $bg-primary;
  padding: $spacing-lg $spacing-xl;
  margin-bottom: $spacing-xl;
  border-radius: $radius-lg;
  box-shadow: $shadow-md;
  animation: slideInRight 0.3s ease;
}

.page-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-lg;
  flex-wrap: wrap;
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex: 1;
  min-width: 200px;
}

.back-button {
  background: $bg-hover;
  border: none;
  color: $text-primary;
  transition: all $transition-fast;
  
  &:hover {
    background: $primary-color;
    color: white;
    transform: translateX(-3px);
  }
  
  &:active {
    transform: translateX(-1px);
  }
}

.breadcrumb-wrapper {
  flex: 1;
  
  :deep(.el-breadcrumb) {
    font-size: $font-size-sm;
    
    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        color: $text-secondary;
        transition: color $transition-fast;
        display: flex;
        align-items: center;
        gap: 4px;
        
        &:hover {
          color: $primary-color;
        }
      }
      
      &:last-child .el-breadcrumb__inner {
        color: $text-primary;
        font-weight: $font-weight-medium;
      }
    }
  }
}

.breadcrumb-home {
  :deep(.el-breadcrumb__inner) {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.page-header-center {
  flex: 2;
  text-align: center;
}

.page-title {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin: 0;
  line-height: $line-height-tight;
}

.page-description {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: $spacing-xs 0 0;
  line-height: $line-height-normal;
}

.page-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex: 1;
  justify-content: flex-end;
  min-width: 200px;
}

// 响应式
@media (max-width: $breakpoint-md) {
  .page-header {
    padding: $spacing-md;
  }
  
  .page-header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .page-header-left,
  .page-header-right {
    width: 100%;
    min-width: auto;
  }
  
  .page-header-center {
    width: 100%;
    text-align: left;
  }
  
  .page-title {
    font-size: $font-size-2xl;
  }
}
</style>

