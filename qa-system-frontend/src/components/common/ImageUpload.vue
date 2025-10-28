<template>
  <div class="image-upload">
    <el-upload
      class="avatar-uploader"
      :action="uploadUrl"
      :show-file-list="false"
      :headers="uploadHeaders"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :disabled="loading"
    >
      <div class="upload-area">
        <div v-if="imageUrl && !loading" class="image-preview">
          <img :src="imageUrl" alt="预览图片" />
          <div class="image-overlay">
            <el-icon class="overlay-icon"><Plus /></el-icon>
            <span>更换图片</span>
          </div>
        </div>
        
        <div v-else-if="loading" class="upload-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>上传中...</span>
        </div>
        
        <div v-else class="upload-placeholder">
          <el-icon class="placeholder-icon"><Plus /></el-icon>
          <div class="placeholder-text">
            <span class="text-main">点击上传图片</span>
            <span class="text-hint">支持 JPG、PNG 格式</span>
            <span class="text-hint">建议尺寸 {{ width }}x{{ height }}px</span>
          </div>
        </div>
      </div>
    </el-upload>
    
    <div v-if="tips" class="upload-tips">
      <el-icon><InfoFilled /></el-icon>
      <span>{{ tips }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Plus, Loading, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  width: {
    type: Number,
    default: 200
  },
  height: {
    type: Number,
    default: 200
  },
  maxSize: {
    type: Number,
    default: 2 // MB
  },
  tips: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'error'])

const loading = ref(false)
const imageUrl = ref(props.modelValue)

// 上传地址（根据实际API调整）
const uploadUrl = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/api/v1/upload/image'
})

// 上传请求头
const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${userStore.token}`
  }
})

// 上传前验证
const beforeUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }

  // 检查文件格式
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isGIF = file.type === 'image/gif'
  const isWEBP = file.type === 'image/webp'
  
  if (!isJPG && !isPNG && !isGIF && !isWEBP) {
    ElMessage.error('图片格式必须是 JPG、PNG、GIF 或 WEBP！')
    return false
  }

  // 检查文件大小
  const isLt2M = file.size / 1024 / 1024 < props.maxSize
  if (!isLt2M) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB！`)
    return false
  }

  loading.value = true
  return true
}

// 上传成功
const handleSuccess = (response) => {
  loading.value = false
  
  if (response.success && response.data) {
    imageUrl.value = response.data.url
    emit('update:modelValue', response.data.url)
    emit('success', response.data)
    ElMessage.success('图片上传成功！')
  } else {
    ElMessage.error(response.message || '图片上传失败！')
    emit('error', response)
  }
}

// 上传失败
const handleError = (error) => {
  loading.value = false
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试！')
  emit('error', error)
}

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  imageUrl.value = newVal
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.image-upload {
  display: inline-block;
}

.avatar-uploader {
  :deep(.el-upload) {
    border-radius: $radius-lg;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all $transition-base;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: $shadow-lg;
    }
  }
}

.upload-area {
  width: v-bind(width + 'px');
  height: v-bind(height + 'px');
  border: 2px dashed $border-color;
  border-radius: $radius-lg;
  background: $bg-secondary;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-fast;
  
  &:hover {
    border-color: $primary-color;
    background: rgba($primary-color, 0.02);
  }
}

.image-preview {
  position: relative;
  width: 100%;
  height: 100%;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
  
  .image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: $spacing-sm;
    opacity: 0;
    transition: opacity $transition-base;
    color: white;
    
    .overlay-icon {
      font-size: $font-size-3xl;
    }
    
    span {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
    }
  }
  
  &:hover .image-overlay {
    opacity: 1;
  }
}

.upload-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  color: $primary-color;
  
  .el-icon {
    font-size: $font-size-4xl;
  }
  
  span {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  
  .placeholder-icon {
    font-size: $font-size-5xl;
    color: $text-disabled;
    transition: all $transition-base;
  }
  
  .placeholder-text {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-xs;
    
    .text-main {
      font-size: $font-size-md;
      color: $text-primary;
      font-weight: $font-weight-medium;
    }
    
    .text-hint {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
  
  &:hover .placeholder-icon {
    color: $primary-color;
    transform: scale(1.1);
  }
}

.upload-tips {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-top: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: rgba($info-color, 0.1);
  border-radius: $radius-md;
  font-size: $font-size-xs;
  color: $info-color;
  
  .el-icon {
    flex-shrink: 0;
  }
}
</style>

