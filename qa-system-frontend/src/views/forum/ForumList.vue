<template>
  <div class="forum-list-page">
    <!-- Hero Banner -->
    <div class="forum-hero">
      <!-- 导航按钮 -->
      <div class="hero-nav">
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
          content="返回首页"
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
      
      <div class="hero-content">
        <h1 class="hero-title">
          <el-icon class="title-icon">
            <ChatDotRound />
          </el-icon>
          师生交流广场
        </h1>
        <p class="hero-subtitle">
          分享知识 · 交流学习 · 共同成长
        </p>
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">
              {{ stats.totalPosts }}
            </div>
            <div class="stat-label">
              总帖子数
            </div>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <div class="stat-number">
              {{ stats.todayPosts }}
            </div>
            <div class="stat-label">
              今日新帖
            </div>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <div class="stat-number">
              {{ stats.activeUsers }}
            </div>
            <div class="stat-label">
              活跃用户
            </div>
          </div>
        </div>
      </div>
      <div class="hero-decoration">
        <div class="decoration-circle circle-1" />
        <div class="decoration-circle circle-2" />
        <div class="decoration-circle circle-3" />
      </div>
    </div>

    <div class="forum-container">
      <!-- 搜索和操作区 -->
      <el-card
        shadow="never"
        class="search-card"
      >
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索帖子标题或内容..."
            clearable
            size="large"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button
            type="primary"
            size="large"
            @click="handleSearch"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button 
            v-if="userStore.isAuthenticated" 
            type="success" 
            size="large" 
            @click="showCreateDialog = true"
          >
            <el-icon><EditPen /></el-icon>
            发布新帖
          </el-button>
        </div>

        <!-- 快捷筛选标签 -->
        <div class="filter-tags">
          <el-tag
            v-for="tag in filterTags"
            :key="tag.value"
            :type="currentFilter === tag.value ? 'primary' : 'info'"
            :effect="currentFilter === tag.value ? 'dark' : 'plain'"
            class="filter-tag"
            @click="handleFilterChange(tag.value)"
          >
            <el-icon>
              <component :is="tag.icon" />
            </el-icon>
            {{ tag.label }}
          </el-tag>
        </div>
      </el-card>

      <!-- 帖子列表 -->
      <div class="posts-section">
        <el-skeleton
          :loading="loading"
          :rows="5"
          animated
        >
          <el-empty
            v-if="postList.length === 0"
            description="暂无帖子，快来发布第一个吧！"
          >
            <el-button
              type="primary"
              @click="showCreateDialog = true"
            >
              发布新帖
            </el-button>
          </el-empty>

          <div
            v-else
            class="posts-grid"
          >
            <div
              v-for="post in postList"
              :key="post.id"
              class="post-card"
              @click="handleViewPost(post.id)"
            >
              <div class="post-header">
                <div class="author-info">
                  <el-avatar
                    :size="42"
                    class="author-avatar"
                  >
                    {{ post.username?.[0] || 'U' }}
                  </el-avatar>
                  <div class="author-detail">
                    <div class="author-name">
                      {{ post.username }}
                    </div>
                    <div class="post-time">
                      <el-icon><Clock /></el-icon>
                      {{ formatTime(post.addtime) }}
                    </div>
                  </div>
                </div>
                <el-tag
                  v-if="post.isdone === '已解决'"
                  type="success"
                  size="small"
                >
                  <el-icon><Select /></el-icon>
                  已解决
                </el-tag>
              </div>

              <div class="post-content">
                <h3 class="post-title">
                  {{ post.title }}
                </h3>
                <p class="post-text">
                  {{ truncateText(post.content, 120) }}
                </p>
              </div>

              <div class="post-footer">
                <div class="post-stats">
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ post.childs?.length || 0 }} 回复
                  </span>
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ Math.floor(Math.random() * 200) + 50 }} 浏览
                  </span>
                </div>
                <el-button
                  type="primary"
                  text
                  class="view-btn"
                >
                  查看详情
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-skeleton>

        <!-- 分页 -->
        <div
          v-if="postList.length > 0"
          class="pagination"
        >
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.limit"
            :total="pagination.total"
            :page-sizes="[12, 24, 36, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="loadPosts"
            @size-change="loadPosts"
          />
        </div>
      </div>
    </div>

    <!-- 发布新帖对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="发布新帖"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="postRules"
        label-width="80px"
      >
        <el-form-item
          label="帖子标题"
          prop="title"
        >
          <el-input
            v-model="postForm.title"
            placeholder="请输入标题（5-50字）"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item
          label="帖子内容"
          prop="content"
        >
          <el-input
            v-model="postForm.content"
            type="textarea"
            placeholder="请输入内容，分享你的想法..."
            :rows="10"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">
            取消
          </el-button>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmitPost"
          >
            <el-icon><Promotion /></el-icon>
            发布
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Search,
  EditPen,
  Clock,
  Select,
  View,
  ArrowRight,
  Promotion,
  Timer,
  Star,
  TrendCharts,
  ArrowLeft,
  HomeFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getForumList, createForum } from '@/api/forum'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const searchKeyword = ref('')
const currentFilter = ref('all')
const postList = ref([])
const postFormRef = ref()

const stats = reactive({
  totalPosts: 0,
  todayPosts: 0,
  activeUsers: 0
})

const pagination = reactive({
  page: 1,
  limit: 12,
  total: 0
})

const postForm = reactive({
  title: '',
  content: '',
  parentid: 0
})

const postRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度为5-50个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度为10-2000个字符', trigger: 'blur' }
  ]
}

const filterTags = [
  { label: '全部', value: 'all', icon: 'TrendCharts' },
  { label: '最新', value: 'latest', icon: 'Timer' },
  { label: '热门', value: 'hot', icon: 'Star' },
  { label: '已解决', value: 'solved', icon: 'Select' }
]

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

// 加载帖子列表
const loadPosts = async () => {
  console.log('📋 开始加载帖子列表...')
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      limit: pagination.limit
    }
    
    if (searchKeyword.value) {
      params.title = searchKeyword.value
    }

    const res = await getForumList(params)
    console.log('✅ 帖子列表加载成功:', res)
    
    if (res.data && res.data.list) {
      // 只显示顶级帖子（parentid为0或null的）
      postList.value = res.data.list.filter(post => !post.parentid || post.parentid === 0)
      pagination.total = postList.value.length
      
      console.log(`📊 处理后的帖子数: ${postList.value.length}`)
      
      // 更新统计数据
      stats.totalPosts = postList.value.length
      stats.todayPosts = postList.value.filter(post => 
        dayjs(post.addtime).isAfter(dayjs().startOf('day'))
      ).length
      stats.activeUsers = new Set(postList.value.map(p => p.userid)).size
    } else {
      console.warn('⚠️ 响应数据格式异常:', res)
    }
  } catch (error) {
    console.error('❌ 加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadPosts()
}

// 筛选切换
const handleFilterChange = (filter) => {
  currentFilter.value = filter
  // 这里可以根据筛选条件重新排序或过滤数据
  // 由于后端接口限制，这里只做前端模拟
}

// 查看帖子详情
const handleViewPost = (id) => {
  const role = userStore.userInfo?.role
  if (role === 'ADMIN') {
    router.push(`/admin/forum/${id}`)
    return
  }
  if (role === 'TEACHER') {
    router.push(`/teacher/forum/${id}`)
    return
  }
  if (role === 'STUDENT') {
    router.push(`/student/forum/${id}`)
    return
  }
  router.push(`/forum/${id}`)
}

// 提交新帖
const handleSubmitPost = async () => {
  if (!postFormRef.value) return

  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createForum(postForm)
        ElMessage.success('发布成功！')
        showCreateDialog.value = false
        postForm.title = ''
        postForm.content = ''
        postForm.parentid = 0
        loadPosts()
      } catch (error) {
        console.error('发布失败:', error)
        ElMessage.error(error.response?.data?.msg || '发布失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped lang="scss">
.forum-list-page {
  min-height: 100%;
  background: transparent;
}

// Hero Banner
.forum-hero {
  display: none;
  
  // 导航按钮
  .hero-nav {
    position: absolute;
    top: 20px;
    left: 20px;
    display: flex;
    gap: 12px;
    z-index: 10;
    
    .nav-btn {
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      color: white;
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        border-color: rgba(255, 255, 255, 0.5);
        box-shadow: 0 10px 26px rgba(0, 0, 0, 0.18);
      }
      
      &:active {
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.16);
      }
      
      :deep(.el-icon) {
        font-size: 18px;
      }
    }
  }
  
  .hero-content {
    position: relative;
    max-width: 1200px;
    margin: 0 auto;
    text-align: center;
    color: white;
    z-index: 2;
    
    .hero-title {
      font-size: 48px;
      font-weight: 700;
      margin: 0 0 16px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      
      .title-icon {
        font-size: 52px;
      }
    }
    
    .hero-subtitle {
      font-size: 18px;
      opacity: 0.95;
      margin-bottom: 40px;
      letter-spacing: 2px;
    }
    
    .hero-stats {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 32px;
      
      .stat-item {
        .stat-number {
          font-size: 36px;
          font-weight: 700;
          margin-bottom: 8px;
        }
        
        .stat-label {
          font-size: 14px;
          opacity: 0.9;
        }
      }
      
      .stat-divider {
        width: 1px;
        height: 40px;
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
  
  .hero-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    
    .decoration-circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      
      &.circle-1 {
        width: 300px;
        height: 300px;
        top: -100px;
        right: -50px;
        animation: float 6s ease-in-out infinite;
      }
      
      &.circle-2 {
        width: 200px;
        height: 200px;
        bottom: -50px;
        left: 10%;
        animation: float 8s ease-in-out infinite reverse;
      }
      
      &.circle-3 {
        width: 150px;
        height: 150px;
        top: 50%;
        left: -30px;
        animation: float 7s ease-in-out infinite;
      }
    }
  }
}

.forum-container {
  max-width: none;
  margin: 0;
  padding: 0;
  position: relative;
  z-index: 3;
}

// 搜索卡片
.search-card {
  margin-bottom: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    
    .search-input {
      flex: 1;
    }
  }
  
  .filter-tags {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    
    .filter-tag {
      cursor: pointer;
      padding: 8px 16px;
      font-size: 14px;
      transition: all 0.3s;
      
      &:hover {
        box-shadow: 0 10px 26px rgba(0, 0, 0, 0.12);
      }
    }
  }
}

// 帖子网格
.posts-section {
  .posts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 24px;
    margin-bottom: 40px;
  }
}

// 帖子卡片
.post-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  
  &:hover {
    box-shadow: 0 12px 32px rgba(79, 70, 229, 0.15);
    border-color: #4f46e5;
    
    .view-btn {
      color: #4f46e5;
    }
  }
  
  .post-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .author-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .author-avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        font-weight: 600;
      }
      
      .author-detail {
        .author-name {
          font-size: 15px;
          font-weight: 600;
          color: #1f2937;
          margin-bottom: 4px;
        }
        
        .post-time {
          font-size: 13px;
          color: #6b7280;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }
  
  .post-content {
    margin-bottom: 16px;
    
    .post-title {
      font-size: 18px;
      font-weight: 600;
      color: #111827;
      margin: 0 0 12px 0;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .post-text {
      font-size: 14px;
      color: #6b7280;
      line-height: 1.6;
      margin: 0;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
  
  .post-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #f3f4f6;
    
    .post-stats {
      display: flex;
      gap: 16px;
      
      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #6b7280;
      }
    }
    
    .view-btn {
      font-weight: 500;
      
      .el-icon {
        transition: transform 0.3s;
      }
      
      &:hover .el-icon {
        transform: translateX(4px);
      }
    }
  }
}

// 分页
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

// 对话框
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 动画
@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

// 响应式
@media (max-width: 768px) {
  .forum-hero {
    padding: 40px 20px;
    
    .hero-content {
      .hero-title {
        font-size: 32px;
        
        .title-icon {
          font-size: 36px;
        }
      }
      
      .hero-stats {
        flex-direction: column;
        gap: 20px;
        
        .stat-divider {
          display: none;
        }
      }
    }
  }
  
  .posts-grid {
    grid-template-columns: 1fr !important;
  }
  
  .search-bar {
    flex-direction: column !important;
  }
}
</style>

