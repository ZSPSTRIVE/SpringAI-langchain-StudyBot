<template>
  <div class="forum-detail-page">
    <div class="detail-container">
      <!-- 导航按钮 -->
      <div class="nav-buttons">
        <el-tooltip
          content="返回上一页"
          placement="bottom"
        >
          <el-button
            circle
            class="nav-btn"
            @click="handleBack"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip
          content="返回工作台"
          placement="bottom"
        >
          <el-button
            circle
            class="nav-btn"
            @click="handleGoHome"
          >
            <el-icon><HomeFilled /></el-icon>
          </el-button>
        </el-tooltip>
      </div>

      <el-skeleton
        :loading="loading"
        :rows="10"
        animated
      >
        <!-- 主帖内容 -->
        <el-card
          v-if="post"
          shadow="never"
          class="main-post-card"
        >
          <div class="post-header">
            <div class="author-section">
              <el-avatar
                :size="56"
                class="author-avatar"
              >
                {{ post.username?.[0] || 'U' }}
              </el-avatar>
              <div class="author-info">
                <div class="author-name">
                  {{ post.username }}
                </div>
                <div class="post-meta">
                  <span class="meta-item">
                    <el-icon><Clock /></el-icon>
                    发布于 {{ formatTime(post.addtime) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="post-status">
              <el-tag
                v-if="post.isdone === '已解决'"
                type="success"
                size="large"
                effect="dark"
              >
                <el-icon><Select /></el-icon>
                已解决
              </el-tag>
              <el-tag
                v-else
                type="warning"
                size="large"
              >
                <el-icon><QuestionFilled /></el-icon>
                待解决
              </el-tag>
            </div>
          </div>

          <el-divider />

          <div class="post-content">
            <h1 class="post-title">
              {{ post.title }}
            </h1>
            <div class="post-text">
              {{ post.content }}
            </div>
          </div>

          <el-divider />

          <div class="post-actions">
            <div class="action-stats">
              <el-button text>
                <el-icon><View /></el-icon>
                {{ viewCount }} 浏览
              </el-button>
              <el-button text>
                <el-icon><ChatDotRound /></el-icon>
                {{ replies.length }} 回复
              </el-button>
            </div>
            <div class="action-buttons">
              <el-button 
                v-if="canEdit" 
                type="warning" 
                :loading="toggling"
                @click="handleToggleStatus"
              >
                <el-icon><Edit /></el-icon>
                {{ post.isdone === '已解决' ? '标记为未解决' : '标记为已解决' }}
              </el-button>
              <el-button 
                v-if="canDelete" 
                type="danger" 
                @click="handleDelete"
              >
                <el-icon><Delete /></el-icon>
                删除帖子
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 回复列表 -->
        <el-card
          v-if="post"
          shadow="never"
          class="replies-card"
        >
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="title-icon">
                  <ChatLineRound />
                </el-icon>
                <span>全部回复 ({{ replies.length }})</span>
              </div>
              <el-button 
                v-if="userStore.isAuthenticated" 
                type="primary" 
                @click="showReplyDialog = true"
              >
                <el-icon><EditPen /></el-icon>
                我来回复
              </el-button>
            </div>
          </template>

          <el-empty
            v-if="replies.length === 0"
            description="暂无回复，快来抢沙发吧！"
          >
            <el-button
              type="primary"
              @click="showReplyDialog = true"
            >
              我来回复
            </el-button>
          </el-empty>

          <div
            v-else
            class="replies-list"
          >
            <div
              v-for="(reply, index) in replies"
              :key="reply.id"
              class="reply-item"
              :class="{ 'highlight-reply': reply.id === highlightReplyId }"
            >
              <div class="reply-number">
                #{{ index + 1 }}
              </div>
              <el-avatar
                :size="48"
                class="reply-avatar"
              >
                {{ reply.username?.[0] || 'U' }}
              </el-avatar>
              <div class="reply-content">
                <div class="reply-header">
                  <div class="reply-author">
                    <span class="author-name">{{ reply.username }}</span>
                    <span class="reply-time">{{ formatTime(reply.addtime) }}</span>
                  </div>
                  <el-button 
                    v-if="canDeleteReply(reply)" 
                    type="danger" 
                    text 
                    size="small"
                    @click="handleDeleteReply(reply.id)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <div class="reply-text">
                  {{ reply.content }}
                </div>
                <div class="reply-footer">
                  <el-button
                    text
                    size="small"
                    @click="handleReplyToReply(reply)"
                  >
                    <el-icon><ChatDotRound /></el-icon>
                    回复
                  </el-button>
                </div>
                
                <!-- 嵌套回复 -->
                <div
                  v-if="reply.childs && reply.childs.length > 0"
                  class="nested-replies"
                >
                  <div
                    v-for="nested in reply.childs"
                    :key="nested.id"
                    class="nested-reply"
                  >
                    <el-avatar
                      :size="32"
                      class="nested-avatar"
                    >
                      {{ nested.username?.[0] || 'U' }}
                    </el-avatar>
                    <div class="nested-content">
                      <div class="nested-author">
                        {{ nested.username }}
                      </div>
                      <div class="nested-text">
                        {{ nested.content }}
                      </div>
                      <div class="nested-time">
                        {{ formatTime(nested.addtime) }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-skeleton>
    </div>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="showReplyDialog"
      :title="replyToUser ? `回复 @${replyToUser}` : '发表回复'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="replyFormRef"
        :model="replyForm"
        :rules="replyRules"
      >
        <el-form-item prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            placeholder="请输入你的回复..."
            :rows="8"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showReplyDialog = false">
            取消
          </el-button>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmitReply"
          >
            <el-icon><Promotion /></el-icon>
            发布回复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Clock,
  Select,
  QuestionFilled,
  View,
  ChatDotRound,
  Edit,
  Delete,
  ChatLineRound,
  EditPen,
  Promotion,
  HomeFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getForumDetail, replyForum, updateForum, deleteForum } from '@/api/forum'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 导航方法
const handleBack = () => {
  router.back()
}

const handleGoHome = () => {
  const role = userStore.userInfo?.role
  if (role === 'ADMIN') {
    router.push('/admin')
    return
  }
  if (role === 'TEACHER') {
    router.push('/teacher')
    return
  }
  if (role === 'STUDENT') {
    router.push('/student')
    return
  }
  router.push('/home')
}

const goForumList = () => {
  const role = userStore.userInfo?.role
  if (role === 'ADMIN') {
    router.push('/admin/forum')
    return
  }
  if (role === 'TEACHER') {
    router.push('/teacher/forum')
    return
  }
  if (role === 'STUDENT') {
    router.push('/student/forum')
    return
  }
  router.push('/forum')
}

const loading = ref(false)
const toggling = ref(false)
const submitting = ref(false)
const showReplyDialog = ref(false)
const post = ref(null)
const replies = ref([])
const viewCount = ref(0)
const replyFormRef = ref()
const replyToUser = ref('')
const highlightReplyId = ref(null)

const replyForm = reactive({
  content: '',
  parentid: null
})

const replyRules = {
  content: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 5, max: 1000, message: '回复内容长度为5-1000个字符', trigger: 'blur' }
  ]
}

const canEdit = computed(() => {
  if (!post.value || !userStore.userInfo) return false
  return post.value.userid === userStore.userInfo.userId || userStore.isAdmin
})

const canDelete = computed(() => {
  if (!post.value || !userStore.userInfo) return false
  return post.value.userid === userStore.userInfo.userId || userStore.isAdmin
})

const canDeleteReply = (reply) => {
  if (!userStore.userInfo) return false
  return reply.userid === userStore.userInfo.userId || userStore.isAdmin
}

// 加载帖子详情
const loadPost = async () => {
  loading.value = true
  try {
    const res = await getForumDetail(route.params.id)
    
    if (res.data) {
      post.value = res.data
      replies.value = res.data.childs || []
      viewCount.value = Math.floor(Math.random() * 500) + 100
    }
  } catch (error) {
    console.error('加载帖子详情失败:', error)
    ElMessage.error('帖子不存在或已被删除')
    goForumList()
  } finally {
    loading.value = false
  }
}

// 切换状态
const handleToggleStatus = async () => {
  toggling.value = true
  try {
    const newStatus = post.value.isdone === '已解决' ? '待解决' : '已解决'
    await updateForum({
      id: post.value.id,
      isdone: newStatus
    })
    ElMessage.success('状态更新成功')
    loadPost()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error(error.response?.data?.msg || '更新失败')
  } finally {
    toggling.value = false
  }
}

// 删除帖子
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定删除此帖子吗？此操作不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteForum([post.value.id])
    ElMessage.success('删除成功')
    goForumList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

// 回复帖子
const handleReplyToReply = (reply) => {
  replyToUser.value = reply.username
  replyForm.parentid = reply.id
  showReplyDialog.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyFormRef.value) return

  await replyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await replyForum({
          title: `Re: ${post.value.title}`,
          content: replyForm.content,
          parentid: replyForm.parentid || post.value.id
        })
        ElMessage.success('回复成功！')
        showReplyDialog.value = false
        replyForm.content = ''
        replyForm.parentid = null
        replyToUser.value = ''
        loadPost()
      } catch (error) {
        console.error('回复失败:', error)
        ElMessage.error(error.response?.data?.msg || '回复失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除回复
const handleDeleteReply = async (replyId) => {
  try {
    await ElMessageBox.confirm('确定删除此回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteForum([replyId])
    ElMessage.success('删除成功')
    loadPost()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  const diff = now.diff(date, 'hour')
  
  if (diff < 24) {
    return date.fromNow()
  } else {
    return date.format('YYYY-MM-DD HH:mm')
  }
}

onMounted(() => {
  loadPost()
})
</script>

<style scoped lang="scss">
.forum-detail-page {
  min-height: 100%;
  background: transparent;
  padding: 0;
}

.detail-container {
  max-width: none;
  margin: 0;
}

.nav-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  
  .nav-btn {
    background: #fff;
    border: 1px solid #e5e7eb;
    color: #374151;
    transition: all 0.2s ease;
    
    &:hover {
      background: #f9fafb;
      border-color: #4f46e5;
      color: #4f46e5;
      box-shadow: 0 10px 26px rgba(79, 70, 229, 0.14);
    }
    
    :deep(.el-icon) {
      font-size: 16px;
    }
  }
}

// 主帖卡片
.main-post-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  
  .post-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .author-section {
      display: flex;
      gap: 16px;
      
      .author-avatar {
        background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
        color: white;
        font-weight: 700;
        font-size: 24px;
      }
      
      .author-info {
        .author-name {
          font-size: 18px;
          font-weight: 600;
          color: #1f2937;
          margin-bottom: 8px;
        }
        
        .post-meta {
          display: flex;
          gap: 16px;
          
          .meta-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: #6b7280;
          }
        }
      }
    }
  }
  
  .post-content {
    padding: 24px 0;
    
    .post-title {
      font-size: 32px;
      font-weight: 700;
      color: #111827;
      margin: 0 0 24px 0;
      line-height: 1.4;
    }
    
    .post-text {
      font-size: 16px;
      line-height: 1.8;
      color: #374151;
      white-space: pre-wrap;
    }
  }
  
  .post-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .action-stats {
      display: flex;
      gap: 16px;
    }
    
    .action-buttons {
      display: flex;
      gap: 12px;
    }
  }
}

// 回复卡片
.replies-card {
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      
      .title-icon {
        font-size: 20px;
        color: #4f46e5;
      }
    }
  }
}

// 回复列表
.replies-list {
  .reply-item {
    display: flex;
    gap: 16px;
    padding: 24px;
    border-bottom: 1px solid #e5e7eb;
    position: relative;
    transition: all 0.3s;
    
    &:hover {
      background: #f9fafb;
    }
    
    &:last-child {
      border-bottom: none;
    }
    
    &.highlight-reply {
      background: #eff6ff;
      animation: highlight 1s ease-in-out;
    }
    
    .reply-number {
      position: absolute;
      top: 24px;
      left: 0;
      font-size: 12px;
      color: #9ca3af;
      font-weight: 600;
    }
    
    .reply-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      font-weight: 600;
      flex-shrink: 0;
    }
    
    .reply-content {
      flex: 1;
      
      .reply-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .reply-author {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .author-name {
            font-size: 15px;
            font-weight: 600;
            color: #1f2937;
          }
          
          .reply-time {
            font-size: 13px;
            color: #9ca3af;
          }
        }
      }
      
      .reply-text {
        font-size: 15px;
        line-height: 1.7;
        color: #374151;
        margin-bottom: 12px;
        white-space: pre-wrap;
      }
      
      .reply-footer {
        display: flex;
        gap: 16px;
      }
      
      // 嵌套回复
      .nested-replies {
        margin-top: 16px;
        padding-left: 16px;
        border-left: 3px solid #e5e7eb;
        
        .nested-reply {
          display: flex;
          gap: 12px;
          padding: 12px;
          background: #f9fafb;
          border-radius: 8px;
          margin-bottom: 12px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .nested-avatar {
            background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
            color: white;
            font-weight: 600;
            flex-shrink: 0;
          }
          
          .nested-content {
            flex: 1;
            
            .nested-author {
              font-size: 14px;
              font-weight: 600;
              color: #1f2937;
              margin-bottom: 6px;
            }
            
            .nested-text {
              font-size: 14px;
              line-height: 1.6;
              color: #4b5563;
              margin-bottom: 6px;
            }
            
            .nested-time {
              font-size: 12px;
              color: #9ca3af;
            }
          }
        }
      }
    }
  }
}

// 对话框
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 动画
@keyframes highlight {
  0%, 100% {
    background: #f9fafb;
  }
  50% {
    background: #dbeafe;
  }
}

// 响应式
@media (max-width: 768px) {
  .main-post-card {
    .post-header {
      flex-direction: column;
      gap: 16px;
    }
    
    .post-content {
      .post-title {
        font-size: 24px;
      }
    }
    
    .post-actions {
      flex-direction: column;
      gap: 16px;
      align-items: flex-start;
      
      .action-buttons {
        width: 100%;
        flex-direction: column;
        
        button {
          width: 100%;
        }
      }
    }
  }
  
  .reply-item {
    .reply-number {
      display: none;
    }
  }
}
</style>

