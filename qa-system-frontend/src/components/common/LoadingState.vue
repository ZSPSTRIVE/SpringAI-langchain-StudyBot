<template>
  <div class="loading-state" :class="sizeClass">
    <div class="loading-icon">
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
    </div>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  text: {
    type: String,
    default: '加载中...'
  },
  size: {
    type: String,
    default: 'medium', // small, medium, large
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

const sizeClass = computed(() => `loading-state-${props.size}`)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  
  &.loading-state-small {
    padding: $spacing-lg;
    
    .loading-icon {
      font-size: $font-size-3xl;
      margin-bottom: $spacing-sm;
    }
    
    .loading-text {
      font-size: $font-size-sm;
    }
  }
  
  &.loading-state-medium {
    padding: $spacing-3xl;
    
    .loading-icon {
      font-size: $font-size-4xl;
      margin-bottom: $spacing-md;
    }
    
    .loading-text {
      font-size: $font-size-md;
    }
  }
  
  &.loading-state-large {
    padding: $spacing-3xl;
    min-height: 400px;
    
    .loading-icon {
      font-size: $font-size-5xl;
      margin-bottom: $spacing-lg;
    }
    
    .loading-text {
      font-size: $font-size-lg;
    }
  }
}

.loading-icon {
  color: $primary-color;
  
  .el-icon {
    font-size: inherit;
    animation: spin 1s linear infinite;
  }
}

.loading-text {
  color: $text-secondary;
  margin: 0;
  font-weight: $font-weight-medium;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

