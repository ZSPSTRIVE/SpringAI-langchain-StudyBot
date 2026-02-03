<template>
  <div class="doc-studio-page">
    <PageHeader
      title="AI 文档查重 & 降重工作台"
      description="上传文档进行查重，并使用 AI 实时降重与对比"
      :show-back="true"
      :show-home="true"
    />

    <!-- 上传与查重 -->
    <el-card class="upload-card">
      <div class="upload-row">
        <el-upload
          class="upload-btn"
          :show-file-list="false"
          :before-upload="handleBeforeUpload"
        >
          <el-button
            type="primary"
            :icon="Upload"
            :loading="uploading"
          >
            选择 Word 文档 (.doc / .docx)
          </el-button>
        </el-upload>
        <span class="upload-tip">当前文档：{{ currentDocTitle || '未选择' }}</span>
        <el-button
          type="success"
          :disabled="!pendingFile"
          :loading="uploading"
          @click="handleUploadAndCheck"
        >
          开始查重
        </el-button>
        <el-tag
          v-if="overallSimilarity !== null"
          :type="similarityTagType"
          class="overall-tag"
        >
          整体查重率：{{ overallSimilarity.toFixed(1) }}%
        </el-tag>
      </div>
    </el-card>

    <el-row
      :gutter="16"
      class="studio-main"
    >
      <!-- 左侧：段落列表 -->
      <el-col
        :xs="24"
        :md="6"
      >
        <el-card class="paragraph-card">
          <template #header>
            <div class="card-header">
              <div class="left">
                <el-icon><Document /></el-icon>
                <span>段落列表</span>
              </div>
              <div class="right">
                <el-checkbox
                  v-model="multiSelectMode"
                  @change="toggleMultiSelectMode"
                >
                  多选
                </el-checkbox>
                <el-button
                  v-if="multiSelectMode"
                  size="small"
                  type="primary"
                  :disabled="selectedParagraphs.length === 0"
                  @click="batchRewrite"
                >
                  批量降重 ({{ selectedParagraphs.length }})
                </el-button>
              </div>
            </div>
          </template>
          <el-scrollbar class="paragraph-list">
            <div
              v-for="p in paragraphs"
              :key="p.id || p.paragraphIndex"
              class="paragraph-item"
              :class="{ 
                'high-similarity': p.similarity >= 70,
                'medium-similarity': p.similarity >= 40 && p.similarity < 70,
                'active': activeParagraphIndex === p.paragraphIndex,
                'selected': isSelected(p.paragraphIndex)
              }"
              @click="handleParagraphClick(p.paragraphIndex)"
            >
              <div class="paragraph-meta">
                <el-checkbox
                  v-if="multiSelectMode"
                  :model-value="isSelected(p.paragraphIndex)"
                  class="paragraph-checkbox"
                  @click.stop="toggleSelection(p.paragraphIndex)"
                />
                <span class="index">#{{ p.paragraphIndex }}</span>
                <el-tag
                  :type="getSimilarityType(p.similarity)"
                  size="small"
                >
                  {{ formatSimilarity(p.similarity) }}
                </el-tag>
              </div>
              <div class="paragraph-preview">
                {{ p.originalText }}
              </div>
            </div>
            <div
              v-if="!paragraphs.length"
              class="empty-hint"
            >
              请先上传并查重文档
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <!-- 右侧：编辑器 + Diff -->
      <el-col
        :xs="24"
        :md="18"
      >
        <el-card class="editor-card">
          <template #header>
            <div class="card-header editor-header">
              <div class="left">
                <el-icon><MagicStick /></el-icon>
                <span>文档工作台 - 全文编辑</span>
              </div>
              <div class="right">
                <el-button
                  size="small"
                  :disabled="!currentDocumentId || !hasSelection"
                  :icon="MagicStick"
                  type="primary"
                  @click="showRewriteDialog"
                >
                  降重选中文本
                </el-button>
                <el-button
                  size="small"
                  :disabled="!currentDocumentId"
                  @click="saveDocument"
                >
                  保存修改
                </el-button>
                <el-button
                  size="small"
                  :disabled="!currentDocumentId"
                  @click="downloadDoc"
                >
                  下载文档
                </el-button>
              </div>
            </div>
          </template>

          <div class="full-editor-container">
            <div
              ref="fullEditorRef"
              class="full-monaco-editor"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 降重对话框 -->
    <el-dialog 
      v-model="rewriteDialogVisible" 
      title="AI 降重" 
      width="600px"
      append-to-body
      destroy-on-close
    >
      <el-form>
        <el-form-item label="原文">
          <el-input
            v-model="selectedText"
            type="textarea"
            :rows="4"
            readonly
          />
        </el-form-item>
        <el-form-item label="降重风格">
          <el-select
            v-model="rewriteStyle"
            style="width: 100%"
          >
            <el-option
              label="学术降重"
              value="ACADEMIC"
            />
            <el-option
              label="通顺化"
              value="FLUENCY"
            />
            <el-option
              label="扩写"
              value="EXPAND"
            />
            <el-option
              label="逻辑强化"
              value="LOGIC_ENHANCE"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="改写结果">
          <el-input
            v-model="rewrittenText"
            type="textarea"
            :rows="6"
          />
          <div
            v-if="rewriting"
            class="rewriting-hint"
          >
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
            正在改写中...
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rewriteDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="rewriting"
          @click="startRewrite"
        >
          开始降重
        </el-button>
        <el-button
          type="success"
          :disabled="!rewrittenText"
          @click="applyRewrite"
        >
          应用到文档
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Document, MagicStick, Loading } from '@element-plus/icons-vue'
import * as monaco from 'monaco-editor'
import PageHeader from '@/components/common/PageHeader.vue'
import { uploadAndCheck, batchUpdateParagraphs, downloadDocument } from '@/api/doc'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 上传 & 文档信息
const uploading = ref(false)
const pendingFile = ref(null)
const currentDocumentId = ref(null)
const currentDocTitle = ref('')
const overallSimilarity = ref(null)
const paragraphs = ref([])

// 全文编辑器
const fullEditorRef = ref(null)
let fullEditor = null
const documentContent = ref('') // 完整文档内容

// 降重相关
const rewriteDialogVisible = ref(false)
const selectedText = ref('')
const rewrittenText = ref('')
const rewriteStyle = ref('ACADEMIC')
const hasSelection = ref(false)
const selectionRange = ref(null) // 记录选中的范围

// WebSocket
let ws = null
const rewriting = ref(false)

// 段落信息映射（用于标红）
const paragraphDecorations = ref([])
const activeParagraphIndex = ref(null) // 当前激活的段落索引

// 多选功能
const multiSelectMode = ref(false)
const selectedParagraphs = ref([])

const similarityTagType = computed(() => {
  if (overallSimilarity.value == null) return 'info'
  if (overallSimilarity.value >= 70) return 'danger'
  if (overallSimilarity.value >= 40) return 'warning'
  return 'success'
})

const handleBeforeUpload = (file) => {
  const ext = file.name.toLowerCase()
  if (!ext.endsWith('.doc') && !ext.endsWith('.docx')) {
    ElMessage.error('仅支持 .doc / .docx 文件')
    return false
  }
  pendingFile.value = file
  currentDocTitle.value = file.name
  return false // 阻止 el-upload 自动上传
}

const handleUploadAndCheck = async () => {
  if (!pendingFile.value) return
  uploading.value = true
  try {
    const res = await uploadAndCheck(pendingFile.value)
    const data = res.data || {}
    currentDocumentId.value = data.documentId
    currentDocTitle.value = data.title
    overallSimilarity.value = data.overallSimilarity ?? null
    paragraphs.value = data.paragraphs || []
    
    // 构建完整文档内容
    buildFullDocument()
    
    // 显示文档保存位置
    const savedPath = data.savedPath || '服务器本地'
    ElMessage({
      type: 'success',
      message: `文档查重完成！保存位置：${savedPath}`,
      duration: 5000
    })
  } catch (error) {
    console.error('文档查重失败:', error)
    ElMessage.error(error.message || '文档查重失败')
  } finally {
    uploading.value = false
  }
}

const formatSimilarity = (value) => {
  if (value == null) return '-'
  return `${value.toFixed(1)}%`
}

const getSimilarityType = (value) => {
  if (value == null) return 'info'
  if (value >= 70) return 'danger'
  if (value >= 40) return 'warning'
  return 'success'
}

// 构建完整文档内容
const buildFullDocument = () => {
  const lines = []
  paragraphs.value.forEach((p, index) => {
    if (index > 0) lines.push('') // 段落间空行
    lines.push(p.originalText || '')
  })
  documentContent.value = lines.join('\n')
  
  if (fullEditor) {
    fullEditor.setValue(documentContent.value)
    applyHighlighting()
  }
}

// 初始化全文编辑器
const initEditors = () => {
  if (fullEditor) return
  if (!fullEditorRef.value) return

  fullEditor = monaco.editor.create(fullEditorRef.value, {
    value: documentContent.value,
    language: 'plaintext',
    theme: 'vs',
    automaticLayout: true,
    wordWrap: 'on',
    fontSize: 14,
    lineNumbers: 'on',
    minimap: { enabled: false }
  })

  // 监听文本选择
  fullEditor.onDidChangeCursorSelection((e) => {
    const selection = fullEditor.getSelection()
    const text = fullEditor.getModel().getValueInRange(selection)
    hasSelection.value = text.trim().length > 0
    if (hasSelection.value) {
      selectedText.value = text
      selectionRange.value = selection
    }
  })
}

// 点击段落跳转到编辑器对应位置
const jumpToParagraph = (paragraphIndex) => {
  if (!fullEditor) return
  
  activeParagraphIndex.value = paragraphIndex
  
  // 计算目标段落在编辑器中的起始行号
  let targetLine = 1
  for (let i = 0; i < paragraphIndex; i++) {
    if (i > 0) targetLine++ // 空行
    const text = paragraphs.value[i].originalText || ''
    targetLine += text.split('\n').length
  }
  
  if (paragraphIndex > 0) targetLine++ // 当前段落前的空行
  
  // 跳转并高亮该段落
  fullEditor.revealLineInCenter(targetLine)
  fullEditor.setPosition({ lineNumber: targetLine, column: 1 })
  fullEditor.focus()
  
  // 添加短暂的选中效果
  const text = paragraphs.value[paragraphIndex].originalText || ''
  const lineCount = text.split('\n').length
  const range = new monaco.Range(targetLine, 1, targetLine + lineCount - 1, text.split('\n')[lineCount - 1].length + 1)
  
  fullEditor.setSelection(range)
  
  // 1秒后清除选中，保留光标在段落开头
  setTimeout(() => {
    if (fullEditor) {
      fullEditor.setSelection(new monaco.Range(targetLine, 1, targetLine, 1))
    }
  }, 1000)
}

// 处理段落点击：多选模式下选择，单选模式下跳转
const handleParagraphClick = (paragraphIndex) => {
  if (multiSelectMode.value) {
    toggleSelection(paragraphIndex)
  } else {
    jumpToParagraph(paragraphIndex)
  }
}

// 切换多选模式
const toggleMultiSelectMode = () => {
  if (!multiSelectMode.value) {
    // 关闭多选模式时清空选中
    selectedParagraphs.value = []
  }
}

// 切换段落选中状态
const toggleSelection = (paragraphIndex) => {
  const index = selectedParagraphs.value.indexOf(paragraphIndex)
  if (index > -1) {
    selectedParagraphs.value.splice(index, 1)
  } else {
    selectedParagraphs.value.push(paragraphIndex)
  }
}

// 检查段落是否被选中
const isSelected = (paragraphIndex) => {
  return selectedParagraphs.value.includes(paragraphIndex)
}

// 批量降重
const batchRewrite = () => {
  if (selectedParagraphs.value.length === 0) {
    ElMessage.warning('请先选择要降重的段落')
    return
  }
  
  // 将选中的段落文本合并
  const selectedTexts = selectedParagraphs.value
    .sort((a, b) => a - b)
    .map(idx => {
      const p = paragraphs.value.find(para => para.paragraphIndex === idx)
      return p ? p.originalText : ''
    })
    .filter(t => t)
  
  if (selectedTexts.length === 0) {
    ElMessage.warning('选中的段落为空')
    return
  }
  
  // 合并文本，段落间用空行分隔
  selectedText.value = selectedTexts.join('\n\n')
  rewrittenText.value = ''
  rewriteDialogVisible.value = true
}

// 应用高亮（标红高重复段落）
const applyHighlighting = () => {
  if (!fullEditor) return
  
  const decorations = []
  let currentLine = 1
  
  paragraphs.value.forEach((p, index) => {
    if (index > 0) currentLine++ // 空行
    
    const text = p.originalText || ''
    const lineCount = text.split('\n').length
    
    // 根据相似度添加背景色
    if (p.similarity >= 70) {
      decorations.push({
        range: new monaco.Range(currentLine, 1, currentLine + lineCount - 1, 1),
        options: {
          isWholeLine: true,
          className: 'high-similarity-line',
          glyphMarginClassName: 'high-similarity-glyph'
        }
      })
    } else if (p.similarity >= 40) {
      decorations.push({
        range: new monaco.Range(currentLine, 1, currentLine + lineCount - 1, 1),
        options: {
          isWholeLine: true,
          className: 'medium-similarity-line'
        }
      })
    }
    
    currentLine += lineCount
  })
  
  paragraphDecorations.value = fullEditor.deltaDecorations(paragraphDecorations.value, decorations)
}

// 显示降重对话框
const showRewriteDialog = () => {
  if (!selectedText.value) {
    ElMessage.warning('请先选中要降重的文本')
    return
  }
  rewrittenText.value = ''
  rewriteDialogVisible.value = true
}

// 开始 AI 降重
const startRewrite = async () => {
  if (!selectedText.value) return

  // 如果已经存在连接，先关闭
  if (ws) {
    ws.close()
    ws = null
  }

  const userId = userStore.userInfo?.userId || userStore.userInfo?.id || 0
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const wsUrl = `${protocol}://localhost:8080/ws/doc-rewrite?userId=${userId}`

  rewriting.value = true
  rewrittenText.value = ''

  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    if (!ws) return // 防止 ws 已被清空
    const payload = {
      documentId: currentDocumentId.value,
      text: selectedText.value,
      style: rewriteStyle.value
    }
    ws.send(JSON.stringify(payload))
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'start') {
        rewrittenText.value = ''
      } else if (msg.type === 'chunk') {
        rewrittenText.value += msg.content
      } else if (msg.type === 'end') {
        rewriting.value = false
      } else if (msg.type === 'error') {
        rewriting.value = false
        ElMessage.error(msg.message || 'AI 降重失败')
      }
    } catch (e) {
      console.error('解析 WebSocket 消息失败:', e)
    }
  }

  ws.onerror = (event) => {
    console.error('WebSocket 错误:', event)
    rewriting.value = false
    ElMessage.error('AI 降重连接异常')
  }

  ws.onclose = () => {
    ws = null
    rewriting.value = false
  }
}

// 应用降重结果到文档
const applyRewrite = () => {
  if (!fullEditor || !rewrittenText.value) return
  
  const currentScrollTop = fullEditor.getScrollTop()
  
  // 如果是批量降重，需要替换多个段落
  if (multiSelectMode.value && selectedParagraphs.value.length > 0) {
    // 将降重结果按空行分割，对应到选中的段落
    const rewrittenParts = rewrittenText.value.split('\n\n')
    const sortedIndexes = [...selectedParagraphs.value].sort((a, b) => a - b)
    
    // 建立段落索引到降重结果的映射（按顺序对应）
    const paraToRewrittenMap = new Map()
    sortedIndexes.forEach((paraIndex, i) => {
      paraToRewrittenMap.set(paraIndex, rewrittenParts[i] || rewrittenParts[0] || '')
    })
    
    // 从后向前替换，避免行号偏移
    for (let i = sortedIndexes.length - 1; i >= 0; i--) {
      const paraIndex = sortedIndexes[i]
      const newText = paraToRewrittenMap.get(paraIndex)
      
      // 计算该段落在编辑器中的起始行
      let targetLine = 1
      for (let j = 0; j < paraIndex; j++) {
        if (j > 0) targetLine++
        const text = paragraphs.value[j].originalText || ''
        targetLine += text.split('\n').length
      }
      if (paraIndex > 0) targetLine++
      
      const oldText = paragraphs.value[paraIndex].originalText || ''
      const lineCount = oldText.split('\n').length
      const lastLineLength = oldText.split('\n')[lineCount - 1].length
      
      const range = new monaco.Range(
        targetLine, 1,
        targetLine + lineCount - 1, lastLineLength + 1
      )
      
      fullEditor.executeEdits('apply-rewrite', [{
        range: range,
        text: newText
      }])
      
      // 更新本地 paragraphs 数据
      paragraphs.value[paraIndex].originalText = newText
    }
    
    // 清空选中
    selectedParagraphs.value = []
    multiSelectMode.value = false
    
  } else if (selectionRange.value) {
    // 单个选中文本的降重
    fullEditor.executeEdits('apply-rewrite', [{
      range: selectionRange.value,
      text: rewrittenText.value
    }])
  } else {
    ElMessage.warning('未找到要替换的内容')
    return
  }
  
  // 保持滚动位置
  nextTick(() => {
    if (fullEditor) {
      fullEditor.setScrollTop(currentScrollTop)
    }
  })
  
  rewriteDialogVisible.value = false
  ElMessage.success('已应用降重结果')
}

// 保存文档修改
const saveDocument = async () => {
  if (!fullEditor || !currentDocumentId.value) return
  
  try {
    const content = fullEditor.getValue()
    const lines = content.split('\n')
    const updatedParagraphs = []
    
    let lineIdx = 0
    paragraphs.value.forEach((p, pIdx) => {
      if (pIdx > 0 && lines[lineIdx] === '') lineIdx++ // 跳过空行
      
      const originalLineCount = (p.originalText || '').split('\n').length
      const newText = lines.slice(lineIdx, lineIdx + originalLineCount).join('\n')
      
      updatedParagraphs.push({
        paragraphIndex: p.paragraphIndex,
        originalText: newText
      })
      
      lineIdx += originalLineCount
    })
    
    await batchUpdateParagraphs(currentDocumentId.value, updatedParagraphs)
    ElMessage.success('文档保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + (error.message || ''))
  }
}

// 下载文档
const downloadDoc = async () => {
  if (!currentDocumentId.value) return
  
  try {
    const response = await downloadDocument(currentDocumentId.value)
    
    // 检查响应是否有效
    if (!response || response.size === 0) {
      throw new Error('文档下载失败：空响应')
    }
    
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${currentDocTitle.value || 'document'}.docx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('文档下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  nextTick(() => {
    initEditors()
  })
})

onBeforeUnmount(() => {
  if (ws) {
    ws.close()
    ws = null
  }
  if (fullEditor) {
    fullEditor.dispose()
    fullEditor = null
  }
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.doc-studio-page {
  padding: $spacing-xl;
  animation: fadeIn 0.4s ease;
}

.upload-card {
  margin-top: $spacing-xl;

  .upload-row {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }

  .upload-tip {
    color: $text-secondary;
    font-size: $font-size-sm;
  }

  .overall-tag {
    margin-left: auto;
  }
}

.studio-main {
  margin-top: $spacing-lg;
}

.paragraph-card {
  height: calc(100vh - 220px);
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  .el-icon {
    color: $primary-color;
  }
}

.paragraph-list {
  flex: 1;
  margin-top: $spacing-sm;
  /* 确保 el-scrollbar 有明确的高度才能滚动 */
  max-height: calc(100vh - 320px);
  overflow: hidden;

  .paragraph-item {
    padding: $spacing-sm;
    border-radius: $radius-md;
    cursor: pointer;
    transition: all $transition-fast;
    margin-bottom: $spacing-xs;

    &:hover {
      background: $bg-hover;
    }

    &.active {
      background: rgba($primary-color, 0.08);
      border: 1px solid rgba($primary-color, 0.3);
    }

    &.high-similarity {
      background: rgba(#f56c6c, 0.1);
      border-left: 3px solid #f56c6c;

      .paragraph-preview {
        color: #f56c6c;
        font-weight: $font-weight-medium;
      }
    }

    &.medium-similarity {
      background: rgba(#e6a23c, 0.08);
      border-left: 3px solid #e6a23c;

      .paragraph-preview {
        color: #e6a23c;
      }
    }

    .paragraph-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-xs;

      .index {
        font-weight: $font-weight-medium;
        color: $text-secondary;
      }
    }

    .paragraph-preview {
      font-size: $font-size-xs;
      color: $text-secondary;
      max-height: 3.2em;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}

.editor-card {
  /* 固定一个较大的最小高度，避免只显示一条横线 */
  min-height: 600px;
  height: calc(100vh - 220px);
  display: flex;
  flex-direction: column;
}

.editor-header {
  justify-content: space-between;

  .left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  .right {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }
}

.full-editor-container {
  padding: $spacing-md;
  /* 保证编辑区域有足够高度 */
  min-height: 600px;
}

.full-monaco-editor {
  width: 100%;
  /* 直接指定一个固定高度，避免被内部样式压缩成一行 */
  height: 580px;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  overflow: hidden;
}

.rewriting-hint {
  margin-top: $spacing-sm;
  color: $primary-color;
  font-size: $font-size-sm;
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.empty-hint {
  padding: $spacing-lg;
  text-align: center;
  color: $text-secondary;
  font-size: $font-size-sm;
}

.typing-indicator {
  font-size: $font-size-xs;
  color: $primary-color;
}

@media (max-width: $breakpoint-md) {
  .doc-studio-page {
    padding: $spacing-md;
  }

  .paragraph-card,
  .editor-card {
    height: auto;
  }
}
</style>

<style>
/* 强制 Monaco 编辑器占满高度，覆盖其内部行高样式的影响 */
.monaco-editor,
.monaco-editor .overflow-guard {
  height: 100% !important;
}

/* Monaco Editor 自定义高亮样式（不能 scoped） */
.high-similarity-line {
  background-color: rgba(245, 108, 108, 0.15) !important;
}

.medium-similarity-line {
  background-color: rgba(230, 162, 60, 0.1) !important;
}

.high-similarity-glyph {
  background-color: #f56c6c !important;
  width: 4px !important;
}
</style>
