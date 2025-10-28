<template>
  <div class="empty-state" :class="sizeClass">
    <div class="empty-icon">
      <el-icon v-if="icon">
        <component :is="icon" />
      </el-icon>
      <el-icon v-else>
        <Document />
      </el-icon>
    </div>
    
    <div class="empty-content">
      <h3 class="empty-title">{{ title || '暂无数据' }}</h3>
      <p v-if="description" class="empty-description">{{ description }}</p>
    </div>
    
    <div v-if="$slots.action" class="empty-action">
      <slot name="action"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Document } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  icon: {
    type: Object,
    default: null
  },
  size: {
    type: String,
    default: 'medium', // small, medium, large
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

const sizeClass = computed(() => `empty-state-${props.size}`)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  animation: fadeIn 0.5s ease;
  
  &.empty-state-small {
    padding: $spacing-xl;
    
    .empty-icon {
      font-size: $font-size-4xl;
      margin-bottom: $spacing-md;
    }
    
    .empty-title {
      font-size: $font-size-md;
    }
    
    .empty-description {
      font-size: $font-size-sm;
    }
  }
  
  &.empty-state-medium {
    padding: $spacing-3xl $spacing-xl;
    
    .empty-icon {
      font-size: $font-size-5xl;
      margin-bottom: $spacing-lg;
    }
    
    .empty-title {
      font-size: $font-size-xl;
    }
    
    .empty-description {
      font-size: $font-size-md;
    }
  }
  
  &.empty-state-large {
    padding: $spacing-3xl $spacing-xl;
    min-height: 400px;
    
    .empty-icon {
      font-size: 80px;
      margin-bottom: $spacing-xl;
    }
    
    .empty-title {
      font-size: $font-size-2xl;
    }
    
    .empty-description {
      font-size: $font-size-lg;
    }
  }
}

.empty-icon {
  color: $text-disabled;
  transition: all $transition-slow;
  animation: float 3s ease-in-out infinite;
  
  .el-icon {
    font-size: inherit;
  }
}

.empty-content {
  margin-bottom: $spacing-lg;
}

.empty-title {
  color: $text-primary;
  font-weight: $font-weight-semibold;
  margin: 0 0 $spacing-sm;
}

.empty-description {
  color: $text-secondary;
  line-height: $line-height-relaxed;
  margin: 0;
  max-width: 400px;
}

.empty-action {
  margin-top: $spacing-md;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>

