<template>
  <div
    class="ai-assistant-container"
    :class="{ embedded }"
  >
    <!-- 侧边栏 - 会话历史 -->
    <div
      class="sidebar"
      :class="{ collapsed: sidebarCollapsed }"
    >
      <div class="sidebar-header">
        <el-button 
          :icon="sidebarCollapsed ? 'Expand' : 'Fold'" 
          circle 
          @click="sidebarCollapsed = !sidebarCollapsed"
        />
        <span
          v-if="!sidebarCollapsed"
          class="title"
        >对话历史</span>
        <el-button 
          v-if="!sidebarCollapsed"
          type="primary" 
          :icon="'Plus'" 
          @click="startNewConversation"
        >
          新对话
        </el-button>
      </div>

      <div
        v-if="!sidebarCollapsed"
        class="sidebar-content"
      >
        <el-scrollbar>
          <div 
            v-for="session in sessions" 
            :key="session.sessionId"
            class="session-item"
            :class="{ active: currentSessionId === session.sessionId }"
            @click="loadSession(session.sessionId)"
            @contextmenu.prevent="showContextMenu($event, session)"
          >
            <el-icon><ChatDotRound /></el-icon>
            <div class="session-info">
              <div class="session-preview">
                {{ session.title || getSessionPreview(session) }}
              </div>
              <div class="session-time">
                {{ formatTime(session.createdAt) }}
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      
      <!-- 右键菜单 -->
      <div 
        v-show="contextMenuVisible" 
        :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
        class="context-menu"
        role="menu"
        aria-label="会话操作菜单"
        @keydown.escape="contextMenuVisible = false"
      >
        <div 
          class="context-menu-item" 
          role="menuitem"
          tabindex="0"
          @click="handleRename"
          @keydown.enter="handleRename"
          @keydown.space.prevent="handleRename"
        >
          <el-icon :icon="'Edit'" />
          <span>重命名</span>
        </div>
        <div 
          class="context-menu-item delete" 
          role="menuitem"
          tabindex="0"
          @click="handleDelete"
          @keydown.enter="handleDelete"
          @keydown.space.prevent="handleDelete"
        >
          <el-icon :icon="'Delete'" />
          <span>删除</span>
        </div>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-area">
      <!-- 顶部工具栏 -->
      <div class="chat-header">
        <div class="header-left">
          <el-icon class="ai-icon">
            <Opportunity />
          </el-icon>
          <span class="ai-title">AI学习助手</span>
        </div>
        <div class="header-right">
          <el-tooltip content="收藏的对话">
            <el-button
              :icon="'Star'"
              circle
              @click="showBookmarks"
            />
          </el-tooltip>
          <el-tooltip content="清空对话">
            <el-button
              :icon="'Delete'"
              circle
              @click="clearConversation"
            />
          </el-tooltip>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="messages-container">
        <el-scrollbar ref="scrollbarRef">
          <div class="messages-content">
            <!-- 欢迎消息 -->
            <div
              v-if="messages.length === 0"
              class="welcome-message"
            >
              <el-icon class="welcome-icon">
                <Opportunity />
              </el-icon>
              <h2>你好！我是AI学习助手</h2>
              <p>我可以帮助你：</p>
              <ul>
                <li>📚 解答学习问题</li>
                <li>💡 提供学习建议</li>
                <li>🔍 推荐学习资源</li>
                <li>🎯 理解复杂概念</li>
              </ul>
              <p class="tip">
                有什么问题可以随时问我哦！
              </p>
            </div>

            <!-- 消息列表 -->
            <div 
              v-for="(message, index) in messages" 
              :key="index"
              class="message-wrapper"
              :class="message.role"
            >
              <div class="message-avatar">
                <el-avatar
                  v-if="message.role === 'user'"
                  :icon="'User'"
                />
                <el-avatar
                  v-else
                  class="ai-avatar"
                >
                  <el-icon><Opportunity /></el-icon>
                </el-avatar>
              </div>
              
              <div class="message-content">
                <div class="message-header">
                  <span class="sender-name">
                    {{ message.role === 'user' ? '你' : 'AI助手' }}
                  </span>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                </div>
                
                <div class="message-body">
                  <!-- 用户消息 -->
                  <div
                    v-if="message.role === 'user'"
                    class="user-text"
                  >
                    {{ message.content }}
                  </div>
                  
                  <!-- AI消息 - 支持Markdown -->
                  <div
                    v-else
                    class="ai-text"
                  >
                    <div
                      v-if="message.typing"
                      class="typing-indicator"
                    >
                      <span /><span /><span />
                    </div>
                    <div
                      v-else
                      class="markdown-body"
                      v-html="renderMarkdown(message.content)"
                    />
                    
                    <!-- 推荐资源 -->
                    <div
                      v-if="message.recommendations && message.recommendations.length > 0"
                      class="recommendations"
                    >
                      <div class="recommendations-title">
                        <el-icon><Reading /></el-icon>
                        推荐学习资源
                      </div>
                      <div class="resource-list">
                        <div 
                          v-for="(resource, rIndex) in message.recommendations" 
                          :key="rIndex"
                          class="resource-item"
                        >
                          <div class="resource-header">
                            <el-icon>
                              <Document v-if="resource.type === 'article'" />
                              <VideoPlay v-else-if="resource.type === 'video'" />
                              <Reading v-else />
                            </el-icon>
                            <span class="resource-title">{{ resource.title }}</span>
                          </div>
                          <p class="resource-desc">
                            {{ resource.description }}
                          </p>
                          <el-link
                            :href="resource.url"
                            target="_blank"
                            type="primary"
                          >
                            查看资源 <el-icon><TopRight /></el-icon>
                          </el-link>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- AI消息操作按钮 -->
                <div
                  v-if="message.role === 'assistant' && !message.typing"
                  class="message-actions"
                >
                  <el-button 
                    text 
                    :icon="'CopyDocument'" 
                    size="small"
                    @click="copyMessage(message.content)"
                  >
                    复制
                  </el-button>
                  <el-button 
                    text 
                    :icon="message.feedback === 'helpful' ? 'Star' : 'StarFilled'" 
                    size="small"
                    :type="message.feedback === 'helpful' ? 'success' : ''"
                    @click="submitFeedback(message.conversationId, 'helpful')"
                  >
                    有帮助
                  </el-button>
                  <el-button 
                    text 
                    :icon="'Collection'" 
                    size="small"
                    :type="message.bookmarked ? 'warning' : ''"
                    @click="toggleBookmark(message)"
                  >
                    {{ message.bookmarked ? '已收藏' : '收藏' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            :autosize="{ minRows: 2, maxRows: 6 }"
            placeholder="输入你的问题... (Ctrl+Enter发送)"
            @keydown.enter.ctrl="sendMessage"
            @keydown.enter.meta="sendMessage"
          />
          <div class="input-actions">
            <el-checkbox v-model="needRecommendation">
              需要学习资源推荐
            </el-checkbox>
            <div class="action-buttons">
              <el-button
                :disabled="loading"
                @click="inputMessage = ''"
              >
                清空
              </el-button>
              <el-button 
                type="primary" 
                :loading="loading"
                :disabled="!inputMessage.trim()"
                @click="sendMessage"
              >
                发送 <span class="shortcut">Ctrl+Enter</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 收藏对话对话框 -->
    <el-dialog 
      v-model="bookmarksDialogVisible" 
      title="收藏的对话" 
      width="800px"
    >
      <el-scrollbar max-height="500px">
        <div
          v-if="bookmarkedConversations.length === 0"
          class="empty-state"
        >
          <el-empty description="暂无收藏的对话" />
        </div>
        <div
          v-else
          class="bookmarks-list"
        >
          <div 
            v-for="conv in bookmarkedConversations" 
            :key="conv.id"
            class="bookmark-item"
          >
            <div class="bookmark-header">
              <span class="bookmark-category">{{ conv.questionCategory }}</span>
              <span class="bookmark-time">{{ formatTime(conv.createdAt) }}</span>
            </div>
            <div class="bookmark-content">
              <div class="bookmark-question">
                <strong>问：</strong>{{ conv.userMessage }}
              </div>
              <div class="bookmark-answer">
                <strong>答：</strong>
                <div v-html="renderMarkdown(conv.aiResponse)" />
              </div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import * as aiApi from '@/api/ai'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  embedded: {
    type: Boolean,
    default: false
  }
})

const embedded = props.embedded

// 配置marked
marked.setOptions({
  highlight: function(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  },
  breaks: true,
  gfm: true
})

// 数据
const sidebarCollapsed = ref(false)
const currentSessionId = ref('')
const sessions = ref([])
const messages = ref([])
const inputMessage = ref('')
const needRecommendation = ref(false)
const loading = ref(false)
const scrollbarRef = ref(null)
const bookmarksDialogVisible = ref(false)
const bookmarkedConversations = ref([])

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const selectedSession = ref(null)

// 生命周期
onMounted(() => {
  loadSessions()
  
  // 监听点击关闭右键菜单
  document.addEventListener('click', () => {
    contextMenuVisible.value = false
  })
})

// 方法
const loadSessions = async () => {
  try {
    const res = await aiApi.getUserSessions(20)
    sessions.value = res.data || []
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

const startNewConversation = () => {
  currentSessionId.value = ''
  messages.value = []
  inputMessage.value = ''
}

const loadSession = async (sessionId) => {
  try {
    currentSessionId.value = sessionId
    const res = await aiApi.getSessionHistory(sessionId)
    const history = res.data || []
    
    messages.value = []
    history.forEach(conv => {
      messages.value.push({
        role: 'user',
        content: conv.userMessage,
        timestamp: conv.createdAt
      })
      messages.value.push({
        role: 'assistant',
        content: conv.aiResponse,
        timestamp: conv.createdAt,
        conversationId: conv.id,
        feedback: conv.feedback,
        bookmarked: conv.isBookmarked,
        category: conv.questionCategory,
        recommendations: parseRecommendations(conv.recommendedResources)
      })
    })
    
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载会话历史失败')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage = inputMessage.value.trim()
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage,
    timestamp: new Date()
  })

  // 清空输入
  inputMessage.value = ''
  scrollToBottom()

  // 显示加载状态
  loading.value = true
  
  // 添加AI回复占位（用于流式显示）
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    typing: true,  // 显示打字指示器
    conversationId: null,
    category: null,
    recommendations: null,
    bookmarked: false,
    feedback: null
  })
  scrollToBottom()

  try {
    await aiApi.chatWithAiStream(
      {
        message: userMessage,
        sessionId: currentSessionId.value || undefined,
        needRecommendation: needRecommendation.value
      },
      // onMessage - 每收到一个token就更新显示
      (token, sessionId) => {
        if (sessionId && !currentSessionId.value) {
          currentSessionId.value = sessionId
        }
        messages.value[aiMessageIndex].content += token
        messages.value[aiMessageIndex].typing = false
        scrollToBottom()
      },
      // onDone - 完成时更新元数据
      (data) => {
        if (data.sessionId) {
          currentSessionId.value = data.sessionId
        }
        messages.value[aiMessageIndex].conversationId = data.conversationId
        messages.value[aiMessageIndex].category = data.category
        messages.value[aiMessageIndex].typing = false
        loading.value = false
        
        // 重新加载会话列表
        loadSessions()
        scrollToBottom()
      },
      // onError - 错误处理
      (error) => {
        ElMessage.error(error || 'AI服务暂时不可用')
        // 移除AI回复占位
        messages.value.splice(aiMessageIndex, 1)
        // 移除用户消息
        messages.value.pop()
        loading.value = false
      }
    )
  } catch (error) {
    ElMessage.error(error.message || 'AI服务暂时不可用')
    // 移除AI回复占位
    messages.value.splice(aiMessageIndex, 1)
    // 移除用户消息
    messages.value.pop()
    loading.value = false
  }
}

const renderMarkdown = (content) => {
  if (!content) return ''
  return marked.parse(content)
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

const getSessionPreview = (session) => {
  return session.userMessage?.substring(0, 30) + '...' || '新对话'
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const submitFeedback = async (conversationId, feedback) => {
  try {
    await aiApi.submitFeedback({
      conversationId,
      feedback
    })
    
    // 更新本地状态
    const message = messages.value.find(m => m.conversationId === conversationId)
    if (message) {
      message.feedback = feedback
    }
    
    ElMessage.success('感谢你的反馈！')
  } catch (error) {
    ElMessage.error('提交反馈失败')
  }
}

const toggleBookmark = async (message) => {
  try {
    const newBookmarkState = !message.bookmarked
    await aiApi.bookmarkConversation(message.conversationId, newBookmarkState)
    message.bookmarked = newBookmarkState
    ElMessage.success(newBookmarkState ? '已收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const showBookmarks = async () => {
  try {
    const res = await aiApi.getBookmarkedConversations()
    bookmarkedConversations.value = res.data || []
    bookmarksDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载收藏失败')
  }
}

const clearConversation = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    startNewConversation()
  } catch {
    // 取消
  }
}

// 显示右键菜单
const showContextMenu = (event, session) => {
  event.stopPropagation()
  selectedSession.value = session
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true
}

// 处理重命名
const handleRename = async () => {
  contextMenuVisible.value = false
  
  if (!selectedSession.value) {
    ElMessage.warning('请选择要重命名的会话')
    return
  }
  
  const oldTitle = selectedSession.value.title || getSessionPreview(selectedSession.value)
  
  try {
    const { value: newTitle } = await ElMessageBox.prompt('请输入新的对话标题', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: oldTitle,
      inputPattern: /^.{1,100}$/,
      inputErrorMessage: '标题长度必须在 1-100 个字符之间',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '标题不能为空'
        }
        if (value.trim().length > 100) {
          return '标题太长，最多 100 个字符'
        }
        return true
      }
    })
    
    if (newTitle && newTitle.trim() !== oldTitle) {
      const loading = ElLoading.service({
        lock: true,
        text: '正在重命名...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      
      try {
        await aiApi.renameSession(selectedSession.value.sessionId, newTitle.trim())
        
        // 更新本地列表
        const session = sessions.value.find(s => s.sessionId === selectedSession.value.sessionId)
        if (session) {
          session.title = newTitle.trim()
        }
        
        loading.close()
        ElMessage.success({
          message: `已将会话重命名为「${newTitle.trim()}」`,
          duration: 2000
        })
      } catch (err) {
        loading.close()
        throw err
      }
    } else if (newTitle && newTitle.trim() === oldTitle) {
      ElMessage.info('标题未改变')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重命名失败:', error)
      ElMessage.error({
        message: error.response?.data?.message || '重命名失败，请稍后重试',
        duration: 3000
      })
    }
  }
}

// 处理删除
const handleDelete = async () => {
  contextMenuVisible.value = false
  
  if (!selectedSession.value) {
    ElMessage.warning('请选择要删除的会话')
    return
  }
  
  const sessionTitle = selectedSession.value.title || getSessionPreview(selectedSession.value)
  const isCurrentSession = currentSessionId.value === selectedSession.value.sessionId
  
  try {
    await ElMessageBox.confirm(
      `确定要删除会话「${sessionTitle}」吗？`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        buttonSize: 'default',
        dangerouslyUseHTMLString: true,
        message: `
          <div style="margin-bottom: 12px;">
            <strong>会话：</strong>${sessionTitle}
          </div>
          <div style="color: #f56c6c;">
            <i class="el-icon-warning"></i>
            此操作不可恢复，所有对话记录将被永久删除。
          </div>
          ${isCurrentSession ? '<div style="color: #e6a23c; margin-top: 8px;">⚠️ 这是当前打开的会话</div>' : ''}
        `
      }
    )
    
    const loading = ElLoading.service({
      lock: true,
      text: '正在删除...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    try {
      await aiApi.deleteSession(selectedSession.value.sessionId)
      
      // 从列表中移除
      sessions.value = sessions.value.filter(s => s.sessionId !== selectedSession.value.sessionId)
      
      // 如果删除的是当前对话，清空消息
      if (isCurrentSession) {
        startNewConversation()
      }
      
      loading.close()
      ElMessage.success({
        message: `已删除会话「${sessionTitle}」`,
        duration: 2000
      })
    } catch (err) {
      loading.close()
      throw err
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error({
        message: error.response?.data?.message || '删除失败，请稍后重试',
        duration: 3000
      })
    }
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      const scrollElement = scrollbarRef.value.$el.querySelector('.el-scrollbar__wrap')
      if (scrollElement) {
        scrollElement.scrollTop = scrollElement.scrollHeight
      }
    }
  })
}

const parseRecommendations = (jsonString) => {
  if (!jsonString) return []
  try {
    // 简单解析格式：title|url;title|url
    return jsonString.split(';').map(item => {
      const [title, url] = item.split('|')
      return { title, url, type: 'article', description: '' }
    })
  } catch {
    return []
  }
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';
.ai-assistant-container {
  display: flex;
  height: calc(100vh - 140px); // 减去header与footer高度
  background: $bg-glass;
  border: 1px solid $glass-border;
  border-radius: $radius-xl;
  backdrop-filter: blur($blur-lg) saturate(160%);
  -webkit-backdrop-filter: blur($blur-lg) saturate(160%);

  &.embedded {
    height: 100%;
    background: transparent;
    border: none;
    border-radius: 0;
    backdrop-filter: none;

    .sidebar {
      width: 220px;
    }

    .sidebar-header {
      padding: 12px;
      gap: 8px;

      .title {
        font-size: 14px;
      }
    }

    .session-item {
      padding: 8px 12px;
    }

    .chat-header {
      padding: 8px 16px;

      .ai-icon {
        font-size: 20px;
      }

      .ai-title {
        font-size: 16px;
      }
    }

    .messages-content {
      padding: 12px 16px;
      max-width: 720px;
    }

    .welcome-message {
      padding: 24px 16px;

      h2 {
        font-size: 18px;
      }
    }

    .message-header {
      font-size: 11px;
    }

    .message-body {
      .user-text,
      .ai-text {
        padding: 8px 12px;
        font-size: 13px;
      }
    }

    .input-area {
      padding: 8px 16px;
    }
  }
}

// 侧边栏
.sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.35);
  border-right: 1px solid $glass-border;
  backdrop-filter: blur($blur-md) saturate(150%);
  -webkit-backdrop-filter: blur($blur-md) saturate(150%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;

  .ai-assistant-container.embedded & {
    background: rgba(255, 255, 255, 0.08);
    border-right: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur($blur-lg) saturate(170%);
  }

  &.collapsed {
    width: 60px;

    .sidebar-header {
      justify-content: center;
    }
  }

  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid $glass-border;
    display: flex;
    align-items: center;
    gap: 12px;

    .title {
      flex: 1;
      font-weight: 600;
      font-size: 16px;
    }
  }

  .sidebar-content {
    flex: 1;
    overflow: hidden;
  }

  .session-item {
    padding: 12px 16px;
    display: flex;
    align-items: flex-start;
    gap: 10px;
    cursor: pointer;
    border-bottom: 1px solid rgba(255, 255, 255, 0.25);
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.35);
    }

    &.active {
      background: rgba($color-primary, 0.12);
      border-left: 3px solid $color-primary;
    }

    .el-icon {
      margin-top: 2px;
      color: #909399;
    }

    .session-info {
      flex: 1;
      min-width: 0;
    }

    .session-preview {
      font-size: 14px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 4px;
    }

    .session-time {
      font-size: 12px;
      color: #909399;
    }
  }
  
  // 右键菜单
  .context-menu {
    position: fixed;
    background: $glass-white-strong;
    backdrop-filter: blur($blur-lg) saturate(160%);
    -webkit-backdrop-filter: blur($blur-lg) saturate(160%);
    border-radius: 8px;
    box-shadow: $shadow-glass-md;
    padding: 4px 0;
    z-index: 9999;
    min-width: 140px;
    
    .context-menu-item {
      padding: 10px 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      font-size: 14px;
      color: #303133;
      transition: all 0.2s;
      
      .el-icon {
        font-size: 16px;
        color: #909399;
      }
      
      &:hover {
        background: rgba($color-primary, 0.12);
        color: $color-primary;
        
        .el-icon {
          color: #409eff;
        }
      }
      
      &.delete {
        &:hover {
          background: rgba($color-danger, 0.12);
          color: $color-danger;
          
          .el-icon {
            color: #f56c6c;
          }
        }
      }
    }
  }
}

// 聊天区域
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur($blur-md) saturate(150%);
  -webkit-backdrop-filter: blur($blur-md) saturate(150%);

  .ai-assistant-container.embedded & {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(18px) saturate(160%);
  }
}

.chat-header {
  padding: 12px 24px;
  border-bottom: 1px solid $glass-border;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur($blur-sm) saturate(160%);
  -webkit-backdrop-filter: blur($blur-sm) saturate(160%);

  .ai-assistant-container.embedded & {
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    background: rgba(255, 255, 255, 0.06);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .ai-icon {
      font-size: 24px;
      color: #409eff;
    }

    .ai-title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .header-right {
    display: flex;
    gap: 8px;
  }
}

// 消息容器
.messages-container {
  flex: 1;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur($blur-sm) saturate(140%);
  -webkit-backdrop-filter: blur($blur-sm) saturate(140%);

  .ai-assistant-container.embedded & {
    background: transparent;
  }
}

.messages-content {
  padding: 16px 24px;
  max-width: 900px;
  margin: 0 auto;
}

// 欢迎消息
.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #606266;

  .welcome-icon {
    font-size: 48px;
    color: #409eff;
    margin-bottom: 16px;
  }

  h2 {
    font-size: 20px;
    margin-bottom: 12px;
    color: #303133;
  }

  p {
    font-size: 14px;
    margin-bottom: 8px;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 16px 0;
    font-size: 14px;

    li {
      margin: 8px 0;
    }
  }

  .tip {
    color: #909399;
    font-size: 13px;
    margin-top: 16px;
  }
}

// 消息项
.message-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  &.user {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;

      .message-header {
        justify-content: flex-end;
      }

      .user-text {
        background: rgba($color-primary, 0.2);
        border: 1px solid rgba($color-primary, 0.35);
        color: $text-primary;
        backdrop-filter: blur($blur-sm) saturate(160%);
        -webkit-backdrop-filter: blur($blur-sm) saturate(160%);
        border-radius: 12px 12px 0 12px;
      }
    }
  }

  &.assistant {
    .ai-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .ai-text {
      background: $glass-white-light;
      border-radius: 12px 12px 12px 0;
      border: 1px solid $glass-border;
      backdrop-filter: blur($blur-sm) saturate(150%);
      -webkit-backdrop-filter: blur($blur-sm) saturate(150%);
    }
  }

  .message-avatar {
    flex-shrink: 0;
  }

  .message-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-width: 70%;
  }

  .message-header {
    display: flex;
    gap: 12px;
    align-items: center;
    font-size: 12px;
    color: #909399;
  }

  .sender-name {
    font-weight: 500;
    color: #606266;
  }

  .message-body {
    .user-text,
    .ai-text {
      padding: 10px 14px;
      line-height: 1.5;
      word-wrap: break-word;
      font-size: 14px;
    }
  }

  .message-actions {
    display: flex;
    gap: 8px;
    margin-top: 4px;
  }
}

// Markdown样式
.markdown-body {
  :deep(pre) {
    background: rgba(20, 24, 36, 0.82);
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 12px 0;

    code {
      color: #abb2bf;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
    }
  }

  :deep(code) {
    background: rgba(255, 255, 255, 0.4);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 13px;
  }

  :deep(p) {
    margin: 8px 0;
  }

  :deep(ul), :deep(ol) {
    margin: 12px 0;
    padding-left: 24px;
  }

  :deep(h1), :deep(h2), :deep(h3) {
    margin: 16px 0 8px;
    font-weight: 600;
  }

  :deep(a) {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

// 打字指示器
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #909399;
    animation: typing 1.4s infinite;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }

    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

// 推荐资源
.recommendations {
  margin-top: 12px;
  padding: 12px;
  background: $glass-white-light;
  border-radius: 8px;
  border: 1px solid $glass-border;
  backdrop-filter: blur($blur-sm) saturate(140%);
  -webkit-backdrop-filter: blur($blur-sm) saturate(140%);

  .recommendations-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    margin-bottom: 10px;
    color: #409eff;
    font-size: 14px;
  }

  .resource-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .resource-item {
    padding: 10px;
    background: $glass-white;
    border-radius: 6px;
    border: 1px solid $glass-border;
    backdrop-filter: blur($blur-sm) saturate(140%);
    -webkit-backdrop-filter: blur($blur-sm) saturate(140%);

    .resource-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .el-icon {
        color: #409eff;
      }

      .resource-title {
        font-weight: 500;
        color: #303133;
      }
    }

    .resource-desc {
      font-size: 13px;
      color: #606266;
      margin-bottom: 8px;
    }
  }
}

// 输入区域
.input-area {
  padding: 12px 24px;
  border-top: 1px solid $glass-border;
  background: $glass-white-light;
  backdrop-filter: blur($blur-sm) saturate(150%);
  -webkit-backdrop-filter: blur($blur-sm) saturate(150%);

  .ai-assistant-container.embedded & {
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    background: rgba(255, 255, 255, 0.08);
    backdrop-filter: blur(18px) saturate(160%);
  }

  .input-wrapper {
    max-width: 900px;
    margin: 0 auto;
  }

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;

    .action-buttons {
      display: flex;
      gap: 12px;

      .shortcut {
        font-size: 12px;
        opacity: 0.7;
        margin-left: 4px;
      }
    }
  }
}

// 收藏对话
.bookmarks-list {
  .bookmark-item {
    padding: 16px;
    margin-bottom: 16px;
    background: $glass-white-light;
    border-radius: 8px;
    border: 1px solid $glass-border;
    backdrop-filter: blur($blur-sm) saturate(140%);
    -webkit-backdrop-filter: blur($blur-sm) saturate(140%);

    .bookmark-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: 12px;

      .bookmark-category {
        color: #409eff;
        font-weight: 500;
      }

      .bookmark-time {
        color: #909399;
      }
    }

    .bookmark-content {
      .bookmark-question,
      .bookmark-answer {
        margin-bottom: 12px;
        line-height: 1.6;

        strong {
          color: #303133;
          margin-right: 8px;
        }
      }

      .bookmark-answer {
        color: #606266;
      }
    }
  }
}

.empty-state {
  padding: 40px;
  text-align: center;
}
</style>

