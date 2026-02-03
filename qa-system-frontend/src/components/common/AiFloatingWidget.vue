<template>
  <div>
    <div
      ref="bubbleRef"
      class="floating-bubble"
      :class="[bubbleThemeClass, { dragging: bubbleDragging }]"
      :style="{ left: `${bubblePosition.x}px`, top: `${bubblePosition.y}px` }"
      role="button"
      aria-label="AI assistant"
      tabindex="0"
      @pointerdown="handleBubblePointerDown"
      @keydown.enter="openAiDialog"
      @keydown.space.prevent="openAiDialog"
    >
      <span class="ai-text">AI</span>
    </div>

    <el-dialog
      v-model="aiDialogVisible"
      title="AI助手"
      width="680px"
      :align-center="true"
      :draggable="true"
      :modal="false"
      :modal-penetrable="true"
      :lock-scroll="false"
      :append-to-body="true"
      :close-on-click-modal="false"
      class="ai-assistant-dialog"
      @opened="handleAiDialogOpened"
      @closed="handleAiDialogClosed"
    >
      <AiAssistant embedded />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import AiAssistant from '@/views/common/AiAssistant.vue'
import { attachDialogCornerResize } from '@/utils/dialogCornerResize'

const userStore = useUserStore()

const aiDialogVisible = ref(false)
const bubbleRef = ref(null)
const bubbleDragging = ref(false)
const bubblePosition = ref({ x: 0, y: 0 })

let detachDialogCornerResize = null

const bubbleThemeClass = computed(() => {
  const role = userStore.userInfo?.role
  if (role === 'TEACHER') return 'teacher-orange'
  if (role === 'ADMIN') return 'admin-bubble'
  return ''
})

const openAiDialog = () => {
  aiDialogVisible.value = true
}

const clamp = (value, min, max) => Math.min(Math.max(value, min), max)

const updateBubblePosition = (x, y) => {
  const bubbleEl = bubbleRef.value
  const width = bubbleEl?.offsetWidth || 56
  const height = bubbleEl?.offsetHeight || 56
  const maxX = Math.max(16, window.innerWidth - width - 16)
  const maxY = Math.max(16, window.innerHeight - height - 16)
  bubblePosition.value = {
    x: clamp(x, 16, maxX),
    y: clamp(y, 16, maxY)
  }
}

let bubbleDragState = null

const handleBubblePointerDown = (event) => {
  if (event.button && event.button !== 0) return
  const bubbleEl = bubbleRef.value
  if (!bubbleEl) return
  event.preventDefault()
  bubbleDragState = {
    startX: event.clientX,
    startY: event.clientY,
    originX: bubblePosition.value.x,
    originY: bubblePosition.value.y,
    moved: false,
    pointerId: event.pointerId
  }
  bubbleDragging.value = true
  bubbleEl.setPointerCapture?.(event.pointerId)

  const handleMove = (moveEvent) => {
    if (!bubbleDragState) return
    const dx = moveEvent.clientX - bubbleDragState.startX
    const dy = moveEvent.clientY - bubbleDragState.startY
    if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
      bubbleDragState.moved = true
    }
    if (bubbleDragState.moved) {
      updateBubblePosition(bubbleDragState.originX + dx, bubbleDragState.originY + dy)
    }
  }

  const handleUp = () => {
    bubbleDragging.value = false
    window.removeEventListener('pointermove', handleMove)
    window.removeEventListener('pointerup', handleUp)
    bubbleEl.releasePointerCapture?.(event.pointerId)
    if (bubbleDragState && !bubbleDragState.moved) {
      openAiDialog()
    }
    bubbleDragState = null
  }

  window.addEventListener('pointermove', handleMove)
  window.addEventListener('pointerup', handleUp)
}

const handleResize = () => {
  updateBubblePosition(bubblePosition.value.x, bubblePosition.value.y)
}

onMounted(() => {
  nextTick(() => {
    const bubbleEl = bubbleRef.value
    const width = bubbleEl?.offsetWidth || 56
    const height = bubbleEl?.offsetHeight || 56
    updateBubblePosition(window.innerWidth - width - 40, window.innerHeight - height - 40)
  })
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  detachDialogCornerResize?.()
  detachDialogCornerResize = null
})

const handleAiDialogOpened = () => {
  nextTick(() => {
    requestAnimationFrame(() => {
      const dialogEl = document.querySelector('.ai-assistant-dialog')
      if (!dialogEl) return
      detachDialogCornerResize?.()
      detachDialogCornerResize = attachDialogCornerResize(dialogEl)
    })
  })
}

const handleAiDialogClosed = () => {
  detachDialogCornerResize?.()
  detachDialogCornerResize = null
}
</script>
