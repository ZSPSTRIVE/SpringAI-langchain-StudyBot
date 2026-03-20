<template>
  <div class="ai-assistant-container" :class="{ embedded }">
    <aside v-if="!embedded" class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <el-button class="sidebar-toggle" :icon="sidebarCollapsed ? Expand : Fold" circle @click="sidebarCollapsed = !sidebarCollapsed" />
        <div v-if="!sidebarCollapsed" class="sidebar-heading">
          <span class="sidebar-eyebrow">Sessions</span>
          <span class="sidebar-title">对话历史</span>
        </div>
        <el-button v-if="!sidebarCollapsed" class="new-chat-btn" type="primary" :icon="Plus" @click="startNewConversation">新对话</el-button>
      </div>
      <div v-if="!sidebarCollapsed" class="sidebar-content">
        <el-scrollbar class="session-scrollbar">
          <div v-for="session in sessions" :key="session.sessionId" class="session-item"
               :class="{ active: currentSessionId === session.sessionId }"
               @click="loadSession(session.sessionId)" @contextmenu.prevent="showContextMenu($event, session)">
            <el-icon><ChatDotRound /></el-icon>
            <div class="session-meta">
              <div class="session-name">{{ getSessionDisplayTitle(session) }}</div>
              <div class="session-time">{{ formatTime(session.createdAt) }}</div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <div v-show="contextMenuVisible" class="context-menu" :style="{ left: `${contextMenuX}px`, top: `${contextMenuY}px` }">
        <button type="button" class="context-menu-item" @click="handleRename">重命名</button>
        <button type="button" class="context-menu-item delete" @click="handleDelete">删除</button>
      </div>
    </aside>

    <section class="chat-area">
      <div class="chat-header">
        <div class="header-left">
          <el-icon class="ai-icon"><Opportunity /></el-icon>
          <div>
            <div class="ai-title">AI 学习助手</div>
            <div class="ai-subtitle">支持知识库问答和面试场景模拟</div>
          </div>
        </div>
        <div class="header-right">
          <el-button v-if="isAdmin" text class="debug-toggle-btn" @click="adminDebugVisible = !adminDebugVisible">
            {{ adminDebugVisible ? '隐藏调试' : '知识库调试' }}
          </el-button>
          <el-tooltip content="收藏的对话"><el-button :icon="Star" circle @click="showBookmarks" /></el-tooltip>
          <el-tooltip content="清空当前对话"><el-button :icon="Delete" circle @click="clearConversation" /></el-tooltip>
        </div>
      </div>

      <div v-if="isAdmin && adminDebugVisible" class="admin-debug-panel">
        <div class="debug-toolbar">
          <div>
            <div class="debug-title">知识库调试面板</div>
            <div class="debug-subtitle">仅管理员可见，可直接验证同步、检索、路由与聊天透传。</div>
          </div>
          <div class="debug-switch">
            <el-switch v-model="applyDebugToChat" inline-prompt active-text="ON" inactive-text="OFF" />
            <span>附带到聊天请求</span>
          </div>
        </div>
        <div class="debug-grid">
          <el-input v-model="knowledgeBaseId" placeholder="知识库 ID" clearable />
          <el-select v-model="selectedKnowledgePoint" placeholder="知识点" clearable>
            <el-option label="全部知识点" value="" />
            <el-option v-for="item in knowledgePoints" :key="item.code" :label="item.displayName" :value="item.code" />
          </el-select>
          <el-select v-model="selectedScene" placeholder="面试场景">
            <el-option v-for="item in sceneOptions" :key="item.code" :label="item.displayName" :value="item.code" />
          </el-select>
          <el-select v-model="syncSourceTypes" multiple collapse-tags collapse-tags-tooltip placeholder="同步来源">
            <el-option v-for="item in sourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="debug-tags">
          <el-tag v-if="selectedSceneOption" size="small" effect="plain">Route {{ selectedSceneOption.retrievalMode }}</el-tag>
          <el-tag v-if="selectedSceneOption?.useMilvus" size="small" type="success" effect="plain">Milvus</el-tag>
          <el-tag v-if="selectedSceneOption?.useKeyword" size="small" type="warning" effect="plain">Keyword</el-tag>
        </div>
        <div class="debug-actions">
          <el-button :loading="overviewLoading" @click="refreshKnowledgeOverview">刷新概览</el-button>
          <el-button type="primary" :loading="knowledgeSyncing" @click="runKnowledgeSync">同步知识库</el-button>
        </div>
        <div class="debug-metrics">
          <div class="metric-card"><span>Source Docs</span><strong>{{ knowledgeOverview ? knowledgeOverview.sourceDocumentCount : 0 }}</strong></div>
          <div class="metric-card"><span>Indexed Docs</span><strong>{{ knowledgeOverview ? knowledgeOverview.indexedDocumentCount : 0 }}</strong></div>
          <div class="metric-card"><span>Chunks</span><strong>{{ knowledgeOverview ? knowledgeOverview.chunkCount : 0 }}</strong></div>
          <div class="metric-card"><span>Tasks</span><strong>{{ knowledgeOverview?.recentTasks?.length || 0 }}</strong></div>
        </div>
        <div class="debug-search">
          <el-input v-model="knowledgeSearchQuery" placeholder="输入问题验证检索结果" clearable @keyup.enter="runKnowledgeSearch" />
          <el-button :loading="knowledgeSearchLoading" @click="runKnowledgeSearch">检索验证</el-button>
        </div>
        <div v-if="lastRouteMeta" class="debug-block">
          <div class="debug-tags">
            <el-tag size="small">{{ lastRouteMeta.interviewSceneLabel || lastRouteMeta.interviewScene }}</el-tag>
            <el-tag size="small" :type="retrievalTagType(lastRouteMeta.retrievalMode)">{{ lastRouteMeta.retrievalMode }}</el-tag>
            <el-tag size="small" effect="plain">Recall {{ lastRouteMeta.ragRecallCount }}</el-tag>
          </div>
          <div class="debug-note">{{ lastRouteMeta.routeReason || '本次未返回路由原因。' }}</div>
        </div>
        <div v-if="knowledgeOverview?.recentTasks?.length" class="debug-block">
          <div class="debug-block-title">最近同步任务</div>
          <div v-for="task in knowledgeOverview.recentTasks.slice(0, 3)" :key="task.taskId" class="debug-item">
            <div class="debug-item-top">
              <span>{{ task.taskType }}</span>
              <el-tag size="small" :type="syncTaskTagType(task.status)">{{ task.status }}</el-tag>
            </div>
            <div class="debug-note">scanned {{ task.sourceDocumentCount || 0 }} / indexed {{ task.documentCount || 0 }} / chunks {{ task.chunkCount || 0 }} / failed {{ task.failedDocumentCount || 0 }}</div>
            <div v-if="task.message" class="debug-note">{{ task.message }}</div>
          </div>
        </div>
        <div v-if="knowledgeSearchHits.length" class="debug-block">
          <div class="debug-block-title">检索命中</div>
          <div v-for="hit in knowledgeSearchHits" :key="hit.chunkId" class="debug-item">
            <div class="debug-item-top">
              <strong>{{ hit.title }}</strong>
              <el-tag size="small" effect="plain">{{ hit.knowledgePointLabel || hit.knowledgePoint || 'General' }}</el-tag>
            </div>
            <div class="debug-note">{{ hit.snippet }}</div>
            <div class="debug-note">score {{ formatScore(hit.score) }} | {{ hit.sourceType }} | {{ hit.sourceRef }}</div>
          </div>
        </div>
      </div>

      <div class="messages-container">
        <div ref="scrollbarRef" class="messages-scrollbar native-scroll" tabindex="0">
          <div class="messages-content">
            <div v-if="messages.length === 0" class="welcome-message">
              <el-icon class="welcome-icon"><Opportunity /></el-icon>
              <h2>你好，我是 AI 学习助手</h2>
              <p>可以直接提问课程问题、算法题、八股题，或模拟真实面试追问。</p>
              <ul>
                <li>解释知识点和经典面试题</li>
                <li>按场景切换检索模式</li>
                <li>输出结构化回答</li>
                <li>管理员可验证知识库效果</li>
              </ul>
            </div>
            <div v-for="(message, index) in messages" :key="index" class="message-wrapper" :class="message.role">
              <div class="message-avatar">
                <el-avatar v-if="message.role === 'user'" :icon="User" />
                <el-avatar v-else class="ai-avatar"><el-icon><Opportunity /></el-icon></el-avatar>
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="sender-name">{{ message.role === 'user' ? '你' : 'AI 助手' }}</span>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                </div>
                <div class="message-body">
                  <div v-if="message.role === 'user'" class="user-text">{{ message.content }}</div>
                  <div v-else class="ai-text">
                    <div v-if="message.typing" class="typing-indicator"><span /><span /><span /></div>
                    <div v-else class="markdown-body" v-html="renderMessageHtml(message.content)" />
                    <div v-if="message.recommendations?.length" class="recommendations">
                      <div class="recommendations-title"><el-icon><Reading /></el-icon>推荐学习资源</div>
                      <div class="resource-list">
                        <div v-for="(resource, resourceIndex) in message.recommendations" :key="resourceIndex" class="resource-item">
                          <div class="resource-header">
                            <el-icon><Document v-if="resource.type === 'article'" /><VideoPlay v-else-if="resource.type === 'video'" /><Reading v-else /></el-icon>
                            <span class="resource-title">{{ resource.title }}</span>
                          </div>
                          <p class="resource-desc">{{ resource.description }}</p>
                          <el-link :href="resource.url" target="_blank" type="primary">查看资源 <el-icon><TopRight /></el-icon></el-link>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="message.role === 'assistant' && !message.typing" class="message-actions">
                  <el-button text :icon="CopyDocument" size="small" @click="copyMessage(message.content)">复制</el-button>
                  <el-button text :icon="message.feedback === 'helpful' ? StarFilled : Star" size="small"
                             :type="message.feedback === 'helpful' ? 'success' : ''"
                             @click="submitFeedback(message.conversationId, 'helpful')">有帮助</el-button>
                  <el-button text :icon="Collection" size="small" :type="message.bookmarked ? 'warning' : ''"
                             @click="toggleBookmark(message)">{{ message.bookmarked ? '已收藏' : '收藏' }}</el-button>
                </div>
                <div v-if="isAdmin && message.debugMeta" class="message-debug-meta">
                  <el-tag size="small">{{ message.debugMeta.interviewSceneLabel || message.debugMeta.interviewScene }}</el-tag>
                  <el-tag size="small" :type="retrievalTagType(message.debugMeta.retrievalMode)">{{ message.debugMeta.retrievalMode }}</el-tag>
                  <span class="debug-note">Recall {{ message.debugMeta.ragRecallCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-wrapper">
          <el-input v-model="inputMessage" type="textarea" :rows="2" :autosize="{ minRows: 2, maxRows: 6 }"
                    placeholder="输入你的问题... (Ctrl+Enter 发送)" @keydown.enter.ctrl="sendMessage" @keydown.enter.meta="sendMessage" />
          <div class="input-actions">
            <el-checkbox v-model="needRecommendation">需要推荐学习资源</el-checkbox>
            <div class="action-buttons">
              <el-button :disabled="loading" @click="inputMessage = ''">清空</el-button>
              <el-button type="primary" :loading="loading" :disabled="!inputMessage.trim()" @click="sendMessage">
                发送 <span class="shortcut">Ctrl+Enter</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="bookmarksDialogVisible" title="收藏的对话" width="800px">
      <el-scrollbar max-height="500px">
        <div v-if="bookmarkedConversations.length === 0" class="empty-state"><el-empty description="暂无收藏的对话" /></div>
        <div v-else class="bookmarks-list">
          <div v-for="conv in bookmarkedConversations" :key="conv.id" class="bookmark-item">
            <div class="bookmark-header">
              <span class="bookmark-category">{{ conv.questionCategory }}</span>
              <span class="bookmark-time">{{ formatTime(conv.createdAt) }}</span>
            </div>
            <div class="bookmark-content">
              <div class="bookmark-question"><strong>问：</strong>{{ conv.userMessage }}</div>
              <div class="bookmark-answer"><strong>答：</strong><div v-html="renderMarkdown(conv.aiResponse)" /></div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElLoading, ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Collection,
  CopyDocument,
  Delete,
  Document,
  Expand,
  Fold,
  Opportunity,
  Plus,
  Reading,
  Star,
  StarFilled,
  TopRight,
  User,
  VideoPlay
} from '@element-plus/icons-vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import * as aiApi from '@/api/ai'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')
const props = defineProps({ embedded: { type: Boolean, default: false } })
const embedded = props.embedded
const userStore = useUserStore()
const defaultKnowledgeBaseId = 'intern-rag-playbook'
const escapeHtml = (content) => String(content).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#39;')
const resolveCodeLanguageLabel = (language) => {
  const normalized = String(language || '').trim().toLowerCase()
  const labels = {
    js: 'JavaScript',
    javascript: 'JavaScript',
    ts: 'TypeScript',
    typescript: 'TypeScript',
    py: 'Python',
    python: 'Python',
    java: 'Java',
    c: 'C',
    cpp: 'C++',
    'c++': 'C++',
    cs: 'C#',
    csharp: 'C#',
    'c#': 'C#',
    go: 'Go',
    html: 'HTML',
    css: 'CSS',
    scss: 'SCSS',
    sql: 'SQL',
    json: 'JSON',
    xml: 'XML',
    bash: 'Bash',
    shell: 'Shell',
    sh: 'Shell',
    php: 'PHP',
    ruby: 'Ruby',
    rust: 'Rust',
    kotlin: 'Kotlin',
    swift: 'Swift'
  }
  return labels[normalized] || (normalized ? normalized.toUpperCase() : '代码')
}
const markdownRenderer = new marked.Renderer()
markdownRenderer.code = (code, infostring, escaped) => {
  const language = String((infostring || '').match(/^\S*/)?.[0] || '').trim()
  const languageClass = language ? ` language-${language}` : ''
  const languageLabel = resolveCodeLanguageLabel(language)
  const renderedCode = (escaped ? code : escapeHtml(code)).replace(/\n?$/, '\n')

  return `<div class="code-block">
    <div class="code-block-header">
      <span class="code-block-language">${escapeHtml(languageLabel)}</span>
    </div>
    <pre><code class="hljs${languageClass}">${renderedCode}</code></pre>
  </div>\n`
}
const sourceTypeOptions = [
  { label: 'Seed Internet', value: 'seed_internet' },
  { label: 'Question Answer', value: 'question_answer' },
  { label: 'Doc Paragraph', value: 'doc_paragraph' }
]
marked.setOptions({ renderer: markdownRenderer, highlight(code, lang) { const language = hljs.getLanguage(lang) ? lang : 'plaintext'; return hljs.highlight(code, { language }).value }, breaks: true, gfm: true })
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const sidebarCollapsed = ref(false), currentSessionId = ref(''), sessions = ref([]), messages = ref([]), inputMessage = ref(''), needRecommendation = ref(false), loading = ref(false), scrollbarRef = ref(null), bookmarksDialogVisible = ref(false), bookmarkedConversations = ref([])
const contextMenuVisible = ref(false), contextMenuX = ref(0), contextMenuY = ref(0), selectedSession = ref(null)
const adminDebugVisible = ref(!embedded), applyDebugToChat = ref(true), knowledgeBaseId = ref(defaultKnowledgeBaseId), selectedKnowledgePoint = ref(''), selectedScene = ref('general'), knowledgePoints = ref([]), sceneOptions = ref([]), knowledgeOverview = ref(null), knowledgeSearchQuery = ref(''), knowledgeSearchHits = ref([]), lastRouteMeta = ref(null), overviewLoading = ref(false), knowledgeSearchLoading = ref(false), knowledgeSyncing = ref(false), syncSourceTypes = ref(['seed_internet'])
const selectedSceneOption = computed(() => sceneOptions.value.find(item => item.code === selectedScene.value) || null)
const closeContextMenu = () => { contextMenuVisible.value = false }
onMounted(() => { loadSessions(); if (isAdmin.value) loadAdminDebugContext(); document.addEventListener('click', closeContextMenu) })
onBeforeUnmount(() => { document.removeEventListener('click', closeContextMenu) })
const normalizeText = (value) => typeof value === 'string' ? value.trim() : ''
const autoSegmentParagraphs = (text) => text
  .split(/\n{2,}/)
  .map(block => {
    const trimmed = block.trim()
    if (!trimmed) return ''
    if (trimmed.split('\n').some(line => /^(#{1,6}\s|[-*+]\s|>\s|\d+[.)]\s|```|---+$|\|)/.test(line.trim()))) {
      return trimmed
    }

    const normalizedBlock = trimmed
      .replace(/\s*\n\s*/g, ' ')
      .replace(/[ \t]{2,}/g, ' ')
      .trim()

    if (normalizedBlock.length < 88) {
      return normalizedBlock
    }

    const sentences = normalizedBlock.split(/(?<=[。！？；!?;])\s*/).map(item => item.trim()).filter(Boolean)
    if (sentences.length < 2) {
      return normalizedBlock
    }

    const paragraphs = []
    let current = ''

    sentences.forEach(sentence => {
      const candidate = `${current}${sentence}`
      if (current && candidate.length > 92) {
        paragraphs.push(current.trim())
        current = sentence
        return
      }
      current = candidate
    })

    if (current.trim()) {
      paragraphs.push(current.trim())
    }

    return paragraphs.join('\n\n')
  })
  .filter(Boolean)
  .join('\n\n')
const resolveKnowledgeBaseId = () => normalizeText(knowledgeBaseId.value) || defaultKnowledgeBaseId
const resolveSceneLabel = (sceneCode) => sceneOptions.value.find(item => item.code === sceneCode)?.displayName || sceneCode || 'general'
const buildDebugMeta = (data = {}) => ({ interviewScene: data.interviewScene || selectedScene.value || 'general', interviewSceneLabel: data.interviewSceneLabel || resolveSceneLabel(data.interviewScene || selectedScene.value), retrievalMode: data.retrievalMode || 'NONE', ragRecallCount: Number.isFinite(Number(data.ragRecallCount)) ? Number(data.ragRecallCount) : 0, routeReason: data.routeReason || '' })
async function loadAdminDebugContext() { const [pointsResult, scenesResult] = await Promise.allSettled([aiApi.getKnowledgePoints(), aiApi.getInterviewScenes()]); if (pointsResult.status === 'fulfilled') knowledgePoints.value = pointsResult.value.data || []; if (scenesResult.status === 'fulfilled') sceneOptions.value = scenesResult.value.data || []; await refreshKnowledgeOverview() }
async function loadSessions() { try { const res = await aiApi.getUserSessions(20); sessions.value = (res.data || []).filter(session => typeof session?.sessionId === 'string' && session.sessionId.trim()) } catch (error) { console.error('Load sessions failed', error) } }
async function refreshKnowledgeOverview() { if (!isAdmin.value) return; overviewLoading.value = true; try { const res = await aiApi.getKnowledgeOverview(resolveKnowledgeBaseId()); knowledgeOverview.value = res.data || null } catch { knowledgeOverview.value = null; ElMessage.error('加载知识库概览失败') } finally { overviewLoading.value = false } }
async function runKnowledgeSync() { if (!isAdmin.value) return; knowledgeSyncing.value = true; try { const res = await aiApi.syncKnowledgeBase({ knowledgeBaseId: resolveKnowledgeBaseId(), sourceTypes: syncSourceTypes.value.length ? syncSourceTypes.value : ['seed_internet'] }); await refreshKnowledgeOverview(); const summary = res.data || {}; const status = String(summary.status || '').toUpperCase(); const message = summary.message || `scanned ${summary?.sourceDocumentCount || 0} / indexed ${summary?.documentCount || 0} / chunks ${summary?.chunkCount || 0} / failed ${summary?.failedDocumentCount || 0}`; if (status.includes('FAIL')) ElMessage.error(message); else if (status.includes('PARTIAL') || status.includes('RUNNING') || status.includes('PROCESS')) ElMessage.warning(message); else ElMessage.success(message) } catch (error) { ElMessage.error(error?.response?.data?.message || '同步知识库失败') } finally { knowledgeSyncing.value = false } }
async function runKnowledgeSearch() { if (!isAdmin.value) return; const query = normalizeText(knowledgeSearchQuery.value); if (!query) { ElMessage.warning('请先输入检索问题'); return } knowledgeSearchLoading.value = true; try { const res = await aiApi.searchKnowledge({ query, knowledgeBaseId: resolveKnowledgeBaseId(), knowledgePoint: normalizeText(selectedKnowledgePoint.value) || undefined, limit: 5 }); knowledgeSearchHits.value = res.data?.hits || []; if (!knowledgeSearchHits.value.length) ElMessage.info('未命中相关知识片段') } catch { knowledgeSearchHits.value = []; ElMessage.error('知识库检索失败') } finally { knowledgeSearchLoading.value = false } }
function startNewConversation() { currentSessionId.value = ''; messages.value = []; inputMessage.value = ''; lastRouteMeta.value = null }
async function loadSession(sessionId) { const safeSessionId = normalizeText(sessionId); if (!safeSessionId) { ElMessage.warning('当前会话缺少有效的会话 ID'); return } try { const res = await aiApi.getSessionHistory(safeSessionId); const history = res.data || []; currentSessionId.value = safeSessionId; messages.value = []; history.forEach(conv => { messages.value.push({ role: 'user', content: conv.userMessage, timestamp: conv.createdAt }); messages.value.push({ role: 'assistant', content: conv.aiResponse, timestamp: conv.createdAt, conversationId: conv.id, feedback: conv.feedback, bookmarked: conv.isBookmarked, category: conv.questionCategory, recommendations: parseRecommendations(conv.recommendedResources), debugMeta: null }) }); scrollToBottom() } catch (error) { console.error('Load session history failed', { sessionId: safeSessionId, error }); ElMessage.error('加载会话历史失败') } }
function buildChatPayload(message) { const payload = { message, sessionId: currentSessionId.value || undefined, needRecommendation: needRecommendation.value }; if (isAdmin.value && applyDebugToChat.value) { payload.knowledgeBaseId = resolveKnowledgeBaseId(); payload.knowledgePoint = normalizeText(selectedKnowledgePoint.value) || undefined; payload.messageType = normalizeText(selectedScene.value) || 'general' } return payload }
function rollbackPendingConversation(userMessage, assistantMessage) { const assistantIndex = messages.value.indexOf(assistantMessage); if (assistantIndex >= 0) messages.value.splice(assistantIndex, 1); const lastMessage = messages.value[messages.value.length - 1]; if (lastMessage?.role === 'user' && lastMessage.content === userMessage) messages.value.pop(); loading.value = false }
async function sendMessage() { if (!inputMessage.value.trim() || loading.value) return; const userMessage = inputMessage.value.trim(); messages.value.push({ role: 'user', content: userMessage, timestamp: new Date() }); inputMessage.value = ''; scrollToBottom(); loading.value = true; const assistantMessage = reactive({ role: 'assistant', content: '', timestamp: new Date(), typing: true, conversationId: null, category: null, recommendations: null, bookmarked: false, feedback: null, debugMeta: null }); messages.value.push(assistantMessage); scrollToBottom(); try { await aiApi.chatWithAiStream(buildChatPayload(userMessage), (token, sessionId) => { if (sessionId && !currentSessionId.value) currentSessionId.value = sessionId; assistantMessage.content = `${assistantMessage.content || ''}${token || ''}`; assistantMessage.typing = false; scrollToBottom() }, (data) => { if (data.sessionId) currentSessionId.value = data.sessionId; if (data.content && !assistantMessage.content) assistantMessage.content = data.content; assistantMessage.conversationId = data.conversationId; assistantMessage.category = data.category; assistantMessage.typing = false; if (isAdmin.value) { const debugMeta = buildDebugMeta(data); assistantMessage.debugMeta = debugMeta; lastRouteMeta.value = debugMeta } loading.value = false; loadSessions(); scrollToBottom() }, (error) => { ElMessage.error(error || 'AI 服务暂时不可用'); rollbackPendingConversation(userMessage, assistantMessage) }) } catch (error) { ElMessage.error(error.message || 'AI 服务暂时不可用'); rollbackPendingConversation(userMessage, assistantMessage) } }
const normalizeMarkdownSource = (content) => {
  if (!content) return ''

  let text = String(content)
    .replace(/\r\n?/g, '\n')
    .replace(/\u00a0/g, ' ')

  if (!text.includes('\n') && text.includes('\\n')) {
    text = text.replace(/\\n/g, '\n')
  }

  const fencedBlocks = []
  text = text.replace(/```[\s\S]*?```/g, (block) => {
    const token = `@@CODE_BLOCK_${fencedBlocks.length}@@`
    fencedBlocks.push(block)
    return token
  })

  const codeLanguagePattern = '(?:java|javascript|typescript|js|ts|python|py|go|sql|html|css|json|xml|bash|shell|sh|c\\+\\+|cpp|c#|csharp|kotlin|swift|php|ruby|rust)'

  text = text
    .replace(/([。！？；：:])\s*(#{1,6})(?=\S)/g, '$1\n\n$2 ')
    .replace(/([^\n])\s*(#{1,6})(?=\S)/g, '$1\n\n$2 ')
    .replace(/(^|\n)(#{1,6})(?=\S)/g, '$1$2 ')
    .replace(/(#{1,6}\s*[^#\n]{2,18}?(?:题意|思路|分析|条件|总结|示例|实现|步骤|方法|原理|法))(?!\n)(?=\S)/g, '$1\n\n')
    .replace(/([。！？；：:])\s*((?:[-*+])\s+)/g, '$1\n$2')
    .replace(/([。！？；：:])\s*((?:\d+[.)])\s+)/g, '$1\n$2')
    .replace(/([。！？；：:])\s*(>\s+)/g, '$1\n$2')
    .replace(/(^|\n)([-*+>])(?=\S)/g, '$1$2 ')
    .replace(/(^|\n)(\d+[.)])(?=\S)/g, '$1$2 ')
    .replace(/([^\n])(```)/g, '$1\n\n$2')
    .replace(/(```[A-Za-z0-9#+-]*)([^\n])/g, '$1\n$2')
    .replace(new RegExp(`(#{1,6}\\s*[^\\n#]{1,24}?实现)\\s*(${codeLanguagePattern})(?=(?:public|class|def|function|const|let|var|package|import|SELECT|INSERT|UPDATE|DELETE|<))`, 'gi'), '$1\n\n```$2\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()

  text = autoSegmentParagraphs(text)

  fencedBlocks.forEach((block, index) => {
    text = text.replace(`@@CODE_BLOCK_${index}@@`, block)
  })

  const fenceCount = (text.match(/```/g) || []).length
  if (fenceCount % 2 === 1) {
    text = `${text}\n\`\`\``
  }

  return text
}

const renderMarkdown = (content) => !content ? '' : marked.parse(normalizeMarkdownSource(content))
const renderMessageHtml = (content) => { if (!content) return ''; const normalizedContent = normalizeMarkdownSource(content); try { const rendered = marked.parse(normalizedContent); if (typeof rendered === 'string' && rendered.trim()) return rendered } catch (error) { console.error('Render markdown failed', error) } return escapeHtml(normalizedContent).replace(/\r?\n/g, '<br>') }
const formatTime = (time) => !time ? '' : dayjs(time).fromNow()
const formatScore = (score) => Number.isFinite(Number(score)) ? Number(score).toFixed(3) : '-'
const retrievalTagType = (mode) => mode === 'HYBRID' ? 'success' : mode === 'MILVUS_ONLY' ? 'warning' : mode === 'KEYWORD_ONLY' ? 'info' : 'danger'
const syncTaskTagType = (status) => { const normalized = String(status || '').toUpperCase(); if (normalized.includes('FAIL') || normalized.includes('ERROR')) return 'danger'; if (normalized.includes('PARTIAL') || normalized.includes('RUNNING') || normalized.includes('PROCESS')) return 'warning'; if (normalized.includes('SUCCESS') || normalized.includes('DONE') || normalized.includes('COMPLETED')) return 'success'; return 'info' }
const getSessionPreview = (session) => { const preview = session?.userMessage?.trim?.() || ''; if (!preview) return '新对话'; return preview.length > 30 ? `${preview.substring(0, 30)}...` : preview }
const getSessionDisplayTitle = (session) => session?.sessionTitle?.trim?.() || getSessionPreview(session)
async function copyMessage(content) { try { await navigator.clipboard.writeText(normalizeMarkdownSource(content)); ElMessage.success('已复制到剪贴板') } catch { ElMessage.error('复制失败') } }
async function submitFeedback(conversationId, feedback) { try { await aiApi.submitFeedback({ conversationId, feedback }); const message = messages.value.find(item => item.conversationId === conversationId); if (message) message.feedback = feedback; ElMessage.success('感谢你的反馈') } catch { ElMessage.error('提交反馈失败') } }
async function toggleBookmark(message) { try { const nextState = !message.bookmarked; await aiApi.bookmarkConversation(message.conversationId, nextState); message.bookmarked = nextState; ElMessage.success(nextState ? '已收藏' : '已取消收藏') } catch { ElMessage.error('操作失败') } }
async function showBookmarks() { try { const res = await aiApi.getBookmarkedConversations(); bookmarkedConversations.value = res.data || []; bookmarksDialogVisible.value = true } catch { ElMessage.error('加载收藏失败') } }
async function clearConversation() { try { await ElMessageBox.confirm('确定要清空当前对话吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }); startNewConversation() } catch {} }
function showContextMenu(event, session) { event.stopPropagation(); selectedSession.value = session; contextMenuX.value = event.clientX; contextMenuY.value = event.clientY; contextMenuVisible.value = true }
async function handleRename() { contextMenuVisible.value = false; if (!selectedSession.value) { ElMessage.warning('请选择要重命名的会话'); return } const oldTitle = getSessionDisplayTitle(selectedSession.value); try { const { value: newTitle } = await ElMessageBox.prompt('请输入新的对话标题', '重命名', { confirmButtonText: '确定', cancelButtonText: '取消', inputValue: oldTitle, inputPattern: /^.{1,100}$/, inputErrorMessage: '标题长度必须在 1-100 个字符之间' }); if (newTitle && newTitle.trim() !== oldTitle) { const loadingInstance = ElLoading.service({ lock: true, text: '正在重命名...', background: 'rgba(0, 0, 0, 0.7)' }); try { await aiApi.renameSession(selectedSession.value.sessionId, newTitle.trim()); const session = sessions.value.find(item => item.sessionId === selectedSession.value.sessionId); if (session) session.sessionTitle = newTitle.trim(); loadingInstance.close(); ElMessage.success(`已重命名为 ${newTitle.trim()}`) } catch (err) { loadingInstance.close(); throw err } } else if (newTitle && newTitle.trim() === oldTitle) { ElMessage.info('标题未变化') } } catch (error) { if (error !== 'cancel') { console.error('Rename session failed', error); ElMessage.error(error.response?.data?.message || '重命名失败，请稍后重试') } } }
async function handleDelete() { contextMenuVisible.value = false; if (!selectedSession.value) { ElMessage.warning('请选择要删除的会话'); return } const sessionTitle = getSessionDisplayTitle(selectedSession.value); const isCurrentSession = currentSessionId.value === selectedSession.value.sessionId; try { await ElMessageBox.confirm(`确定要删除会话「${sessionTitle}」吗？`, '警告', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }); const loadingInstance = ElLoading.service({ lock: true, text: '正在删除...', background: 'rgba(0, 0, 0, 0.7)' }); try { await aiApi.deleteSession(selectedSession.value.sessionId); sessions.value = sessions.value.filter(item => item.sessionId !== selectedSession.value.sessionId); if (isCurrentSession) startNewConversation(); loadingInstance.close(); ElMessage.success(`已删除会话 ${sessionTitle}`) } catch (err) { loadingInstance.close(); throw err } } catch (error) { if (error !== 'cancel') { console.error('Delete session failed', error); ElMessage.error(error.response?.data?.message || '删除失败，请稍后重试') } } }
function getScrollElement() { if (!scrollbarRef.value) return null; if (scrollbarRef.value instanceof HTMLElement) return scrollbarRef.value; return scrollbarRef.value.$el?.querySelector('.el-scrollbar__wrap') || null }
function scrollToBottom() { nextTick(() => { const scrollElement = getScrollElement(); if (scrollElement) scrollElement.scrollTop = scrollElement.scrollHeight }) }

function parseRecommendations(jsonString) { if (!jsonString) return []; try { const parsed = JSON.parse(jsonString); if (Array.isArray(parsed)) return parsed } catch {} try { return jsonString.split(';').map(item => { const [title, url] = item.split('|'); return { title, url, type: 'article', description: '' } }).filter(item => item.title || item.url) } catch { return [] } }
</script>
<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.ai-assistant-container {
  display: flex;
  width: 100%;
  max-width: 1280px;
  height: 100%;
  min-height: 0;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.44);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: $radius-xl;
  box-shadow: $shadow-lg;
  backdrop-filter: blur(22px) saturate(150%);
  overflow: hidden;
}

.ai-assistant-container.embedded {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  border-radius: 0;
  backdrop-filter: none;
  container-type: size;
  container-name: ai-assistant-embedded;
}

.ai-assistant-container.embedded .chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.ai-assistant-container.embedded .chat-header,
.ai-assistant-container.embedded .admin-debug-panel {
  display: none;
}

.ai-assistant-container.embedded .messages-content {
  max-width: 100%;
  padding: 18px 18px 12px;
}

.ai-assistant-container.embedded .welcome-message {
  width: 100%;
  max-width: none;
  padding: clamp(18px, 5cqh, 32px) clamp(18px, 5cqw, 30px);
  border-radius: 20px;
}

.ai-assistant-container.embedded .message-wrapper {
  margin-bottom: 16px;
}

.ai-assistant-container.embedded .message-content {
  max-width: min(88%, 640px);
}

.ai-assistant-container.embedded .input-area {
  padding: 12px 16px 16px;
}

.ai-assistant-container.embedded .input-wrapper {
  max-width: 100%;
}

.ai-assistant-container.embedded .input-wrapper :deep(.el-textarea__inner) {
  min-height: 72px !important;
  padding: 12px 14px;
}

.ai-assistant-container.embedded .input-actions {
  margin-top: 10px;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 304px;
  flex: 0 0 304px;
  min-width: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(246, 248, 255, 0.68));
  border-right: 1px solid rgba(255, 255, 255, 0.65);
  transition: width $transition-slow;
  position: relative;
}

.sidebar.collapsed {
  width: 76px;
  flex-basis: 76px;
}

.sidebar-header {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 18px 18px 14px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
}

.sidebar-toggle,
.new-chat-btn {
  flex-shrink: 0;
}

.sidebar-heading {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #64748b;
}

.sidebar-title {
  font-size: 18px;
  line-height: 1.2;
  font-weight: 700;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.new-chat-btn {
  min-width: 118px;
}

.sidebar-content {
  flex: 1;
  min-height: 0;
}

.session-scrollbar {
  height: 100%;
}

.session-scrollbar :deep(.el-scrollbar__view) {
  padding: 12px;
}

.session-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  margin-bottom: 8px;
  cursor: pointer;
  border-radius: 16px;
  border: 1px solid transparent;
  transition: background $transition-base, border-color $transition-base, transform $transition-base;
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.72);
  border-color: rgba(191, 219, 254, 0.9);
  transform: translateY(-1px);
}

.session-item.active {
  background: rgba(0, 122, 255, 0.1);
  border-color: rgba(0, 122, 255, 0.22);
  box-shadow: inset 0 0 0 1px rgba(0, 122, 255, 0.08);
}

.session-meta {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 14px;
  line-height: 1.45;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time,
.ai-subtitle,
.debug-note,
.message-time,
.bookmark-time {
  font-size: 12px;
  color: #64748b;
}

.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 132px;
  padding: 6px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: $shadow-md;
}

.context-menu-item {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
}

.context-menu-item:hover {
  background: rgba(0, 122, 255, 0.08);
}

.context-menu-item.delete:hover {
  background: rgba(255, 59, 48, 0.1);
  color: $color-danger;
}

.chat-area {
  flex: 1 1 auto;
  height: 100%;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.62), rgba(248, 250, 255, 0.52));
  overflow: hidden;
}

.chat-header,
.header-left,
.header-right,
.debug-tags,
.debug-actions,
.message-actions,
.message-debug-meta,
.recommendations-title,
.resource-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.chat-header {
  justify-content: space-between;
  padding: 22px 28px 18px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.85);
  background: rgba(255, 255, 255, 0.52);
  flex: 0 0 auto;
}

.header-left {
  min-width: 0;
}

.header-left > div {
  min-width: 0;
}

.header-right {
  justify-content: flex-end;
}

.ai-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  font-size: 20px;
  color: $color-primary;
  background: rgba(0, 122, 255, 0.1);
}

.ai-title {
  font-size: 17px;
  line-height: 1.25;
  font-weight: 700;
  color: #111827;
}

.ai-subtitle {
  margin-top: 4px;
}

.debug-toggle-btn {
  font-weight: 600;
}

.admin-debug-panel {
  display: grid;
  gap: 16px;
  padding: 18px 28px 22px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.85);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(244, 247, 255, 0.76));
  flex: 0 0 auto;
}

.debug-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.debug-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.debug-subtitle {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.5;
  color: #475569;
}

.debug-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  font-size: 13px;
  color: #334155;
}

.debug-grid,
.debug-metrics {
  display: grid;
  gap: 12px;
}

.debug-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.debug-metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.debug-actions {
  justify-content: flex-start;
}

.metric-card,
.debug-block {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-card span {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.metric-card strong {
  font-size: 18px;
  line-height: 1;
  color: #111827;
}

.debug-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.debug-block-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #1f2937;
}

.debug-item {
  padding: 10px 0;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
}

.debug-item:first-child {
  padding-top: 0;
  border-top: none;
}

.debug-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.messages-container {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
  position: relative;
}

.messages-scrollbar {
  display: block;
  flex: 1 1 auto;
  width: 100%;
  height: auto;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
  touch-action: pan-y;
}

.messages-scrollbar.native-scroll {
  -webkit-overflow-scrolling: touch;
}

.messages-content {
  min-height: 100%;
  width: 100%;
  box-sizing: border-box;
  max-width: 960px;
  margin: 0 auto;
  padding: 26px 28px 20px;
  display: flex;
  flex-direction: column;
}

.welcome-message {
  width: min(100%, 620px);
  margin: auto;
  padding: 34px 30px;
  text-align: center;
  color: #475569;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 24px;
  box-shadow: $shadow-sm;
}

.welcome-icon {
  font-size: 46px;
  color: $color-primary;
  margin-bottom: 14px;
}

.welcome-message h2 {
  margin: 0 0 12px;
  font-size: 22px;
  line-height: 1.2;
  color: #1f2937;
}

.welcome-message p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
}

.welcome-message ul {
  margin: 18px 0 0;
  padding: 0;
  list-style: none;
}

.welcome-message li {
  margin: 8px 0;
  font-size: 14px;
}

@container ai-assistant-embedded (max-height: 760px) {
  .ai-assistant-container.embedded .messages-content {
    padding: 12px 14px 8px;
  }

  .ai-assistant-container.embedded .welcome-message {
    padding: 18px 16px;
  }

  .ai-assistant-container.embedded .welcome-icon {
    font-size: 34px;
    margin-bottom: 10px;
  }

  .ai-assistant-container.embedded .welcome-message h2 {
    margin-bottom: 8px;
    font-size: 18px;
  }

  .ai-assistant-container.embedded .welcome-message p,
  .ai-assistant-container.embedded .welcome-message li {
    font-size: 13px;
    line-height: 1.6;
  }

  .ai-assistant-container.embedded .welcome-message ul {
    margin-top: 12px;
  }

  .ai-assistant-container.embedded .input-area {
    padding: 10px 12px 12px;
  }

  .ai-assistant-container.embedded .input-wrapper :deep(.el-textarea__inner) {
    min-height: 60px !important;
    font-size: 14px;
  }

  .ai-assistant-container.embedded .input-actions {
    margin-top: 8px;
    gap: 10px;
  }
}

@container ai-assistant-embedded (max-width: 720px) {
  .ai-assistant-container.embedded .messages-content {
    padding: 12px;
  }

  .ai-assistant-container.embedded .welcome-message {
    padding: 18px 14px;
  }

  .ai-assistant-container.embedded .message-content {
    max-width: 100%;
  }

  .ai-assistant-container.embedded .input-area {
    padding: 10px 12px 12px;
  }

  .ai-assistant-container.embedded .input-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .ai-assistant-container.embedded .action-buttons {
    justify-content: flex-end;
  }
}

@container ai-assistant-embedded (max-width: 560px) {
  .ai-assistant-container.embedded .welcome-message {
    border-radius: 16px;
  }

  .ai-assistant-container.embedded .shortcut {
    display: none;
  }

  .ai-assistant-container.embedded .action-buttons {
    width: 100%;
  }

  .ai-assistant-container.embedded .action-buttons :deep(.el-button) {
    flex: 1;
  }
}

.message-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-wrapper.user {
  flex-direction: row-reverse;
}

.message-wrapper.user .message-content {
  align-items: flex-end;
}

.message-wrapper.user .message-header,
.message-wrapper.user .message-debug-meta {
  justify-content: flex-end;
}

.message-wrapper.user .user-text {
  background: rgba(0, 122, 255, 0.12);
  border: 1px solid rgba(0, 122, 255, 0.18);
  border-radius: 18px 18px 6px 18px;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
}

.message-content {
  max-width: min(76%, 760px);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-header {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #94a3b8;
}

.sender-name {
  font-weight: 700;
  color: #475569;
}

.message-body {
  min-width: 0;
}

.user-text,
.ai-text {
  padding: 9px 16px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  border-radius: 18px;
}

.ai-text {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 18px 18px 18px 6px;
}

.markdown-body {
  font-size: 14px;
  line-height: 1.85;
  letter-spacing: 0.01em;
  color: #1f2937;
  overflow-wrap: anywhere;
}

.markdown-body :deep(*:first-child) {
  margin-top: 0;
}

.markdown-body :deep(*:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin: 18px 0 10px;
  font-weight: 800;
  line-height: 1.45;
  color: #0f172a;
}

.markdown-body :deep(h1) {
  font-size: 20px;
}

.markdown-body :deep(h2) {
  font-size: 18px;
}

.markdown-body :deep(h3) {
  font-size: 16px;
}

.markdown-body :deep(code) {
  font-family: $font-family-mono;
}

.markdown-body :deep(.code-block) {
  margin: 14px 0;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.9);
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98) 0%, rgba(2, 6, 23, 0.98) 100%);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.16);
}

.markdown-body :deep(.code-block-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(30, 41, 59, 0.92);
}

.markdown-body :deep(.code-block-language) {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.16);
  color: #bfdbfe;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.markdown-body :deep(.code-block pre) {
  margin: 0;
  padding: 16px 18px;
  overflow-x: auto;
  background: transparent;
}

.markdown-body :deep(:not(pre) > code) {
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(37, 99, 235, 0.08);
  color: #1d4ed8;
  font-size: 13px;
}

.markdown-body :deep(.code-block pre code) {
  padding: 0;
  color: inherit;
  background: transparent;
  display: block;
  font-size: 13px;
  line-height: 1.75;
  tab-size: 2;
  white-space: pre;
}

.markdown-body :deep(p) {
  margin: 10px 0;
}

.markdown-body :deep(p + p) {
  margin-top: 14px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.markdown-body :deep(li + li) {
  margin-top: 6px;
}

.markdown-body :deep(ol li::marker),
.markdown-body :deep(ul li::marker) {
  color: #3b82f6;
}

.markdown-body :deep(blockquote) {
  margin: 14px 0;
  padding: 12px 14px;
  border-left: 4px solid rgba(59, 130, 246, 0.35);
  border-radius: 0 12px 12px 0;
  background: rgba(239, 246, 255, 0.72);
  color: #334155;
}

.markdown-body :deep(blockquote p) {
  margin: 0;
}

.markdown-body :deep(strong) {
  color: #0f172a;
  font-weight: 800;
}

.markdown-body :deep(hr) {
  margin: 18px 0;
  border: 0;
  border-top: 1px solid rgba(203, 213, 225, 0.9);
}

.markdown-body :deep(table) {
  width: 100%;
  margin: 14px 0;
  border-collapse: collapse;
  overflow: hidden;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 8px 10px;
  border: 1px solid rgba(203, 213, 225, 0.9);
  text-align: left;
}

.markdown-body :deep(th) {
  background: rgba(248, 250, 252, 0.96);
  font-weight: 700;
}

.markdown-body :deep(a) {
  color: #2563eb;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 2px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.recommendations {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(148, 163, 184, 0.4);
}

.resource-list {
  margin-top: 10px;
  display: grid;
  gap: 10px;
}

.resource-item {
  padding: 12px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.95);
}

.resource-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.resource-desc {
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
}

.message-debug-meta {
  font-size: 12px;
}

.input-area {
  padding: 18px 24px 24px;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(18px);
  box-shadow: 0 -12px 30px rgba(15, 23, 42, 0.08);
  position: relative;
  z-index: 2;
  flex: 0 0 auto;
}

.input-wrapper {
  max-width: 960px;
  margin: 0 auto;
}

.input-wrapper :deep(.el-textarea__inner) {
  min-height: 112px !important;
  padding: 16px 18px;
  font-size: 15px;
  line-height: 1.7;
  border-radius: 18px;
  box-shadow: none;
}

.input-actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.shortcut {
  margin-left: 6px;
  font-size: 12px;
  opacity: 0.8;
}

.empty-state {
  padding: 20px 0;
}

.bookmark-item {
  padding: 16px 0;
  border-top: 1px solid rgba(226, 232, 240, 0.92);
}

.bookmark-item:first-child {
  padding-top: 0;
  border-top: none;
}

.bookmark-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748b;
}

.bookmark-category {
  font-weight: 700;
  color: $color-primary;
}

.bookmark-question,
.bookmark-answer {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

@media (max-width: 1280px) {
  .sidebar {
    width: 272px;
    flex-basis: 272px;
  }

  .debug-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .ai-assistant-container {
    flex-direction: column;
    height: calc(100dvh - 170px);
    min-height: 0;
  }

  .sidebar {
    width: 100%;
    flex-basis: auto;
    border-right: none;
    border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  }

  .debug-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .chat-header,
  .admin-debug-panel,
  .input-area {
    padding-left: 18px;
    padding-right: 18px;
  }

  .sidebar-header,
  .debug-toolbar,
  .input-actions,
  .chat-header {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .header-right {
    justify-content: flex-start;
  }

  .debug-grid,
  .debug-metrics,
  .debug-search {
    grid-template-columns: 1fr;
  }

  .messages-content {
    padding-left: 18px;
    padding-right: 18px;
  }

  .message-content {
    max-width: 100%;
  }

  .welcome-message {
    padding: 28px 22px;
  }
}
</style>
