<template>
  <div class="questions-page">
    <!-- 动态背景 -->
    <div class="animated-background">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="floating-particles">
        <span v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></span>
      </div>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Compass /></el-icon>
          问题广场
        </h1>
        <p class="page-subtitle">探索知识的海洋，寻找问题的答案</p>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="hover" class="search-card glass-card">
      <el-form :inline="true">
        <el-form-item label="科目">
          <el-select v-model="searchForm.subjectId" placeholder="全部科目" clearable style="width: 150px">
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待回答" value="PENDING" />
            <el-option label="已回答" value="ANSWERED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索标题或内容"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 问题列表 -->
    <el-card shadow="hover" class="list-card glass-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><List /></el-icon>
            问题列表
          </span>
          <el-button v-if="userStore.isStudent" type="primary" @click="router.push('/student/ask')">
            <el-icon><Plus /></el-icon>
            我要提问
          </el-button>
        </div>
      </template>

      <el-skeleton :loading="loading" :rows="5" animated>
        <el-empty v-if="questionList.length === 0" description="暂无问题" />
        
        <div v-else class="question-list">
          <div
            v-for="question in questionList"
            :key="question.id"
            class="question-item"
            @click="router.push(`/questions/${question.id}`)"
          >
            <div class="question-header">
              <div class="question-title">
                <el-tag v-if="question.isTop" type="danger" size="small">置顶</el-tag>
                <el-tag v-if="question.isFeatured" type="warning" size="small">精选</el-tag>
                <span>{{ question.title }}</span>
              </div>
              <el-tag :type="getStatusType(question.status)" size="small">
                {{ getStatusText(question.status) }}
              </el-tag>
            </div>

            <div class="question-meta">
              <el-tag size="small" effect="plain">{{ question.subjectName }}</el-tag>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ question.studentName }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ question.viewCount }}
              </span>
              <span class="meta-item">
                <el-icon><ChatLineRound /></el-icon>
                {{ question.answerCount }} 回答
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(question.createTime) }}
              </span>
            </div>
          </div>
        </div>
      </el-skeleton>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadQuestions"
          @size-change="loadQuestions"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, User, View, ChatLineRound, Clock, Compass, List } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAllSubjects } from '@/api/subject'
import { getQuestionPage } from '@/api/question'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const subjects = ref([])
const questionList = ref([])

const searchForm = reactive({
  subjectId: null,
  status: null,
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadSubjects = async () => {
  try {
    const res = await getAllSubjects()
    subjects.value = res.data
  } catch (error) {
    console.error('加载科目失败:', error)
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    
    const res = await getQuestionPage(params)
    questionList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载问题列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadQuestions()
}

const handleReset = () => {
  searchForm.subjectId = null
  searchForm.status = null
  searchForm.keyword = ''
  handleSearch()
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'ANSWERED': 'success',
    'CLOSED': 'info'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待回答',
    'ANSWERED': '已回答',
    'CLOSED': '已关闭'
  }
  return textMap[status] || status
}

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

const getParticleStyle = (index) => {
  const size = Math.random() * 4 + 2
  const left = Math.random() * 100
  const delay = Math.random() * 10
  const duration = Math.random() * 20 + 10
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

onMounted(() => {
  loadSubjects()
  loadQuestions()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.questions-page {
  position: relative;
  min-height: 100vh;
  padding: 20px; // 缩小15%: 24 -> 20
  max-width: 1190px; // 缩小15%: 1400 -> 1190
  margin: 0 auto;
  overflow: hidden;
  transform: scale(0.85); // 整体缩小15%
  transform-origin: top center;
}

// 动态背景 - 纯色+动效
.animated-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  background: #5b7ef5; // 纯色背景
  pointer-events: none;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
    animation: backgroundPulse 8s ease-in-out infinite;
  }
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      repeating-linear-gradient(
        45deg,
        transparent,
        transparent 100px,
        rgba(255, 255, 255, 0.03) 100px,
        rgba(255, 255, 255, 0.03) 200px
      );
    animation: moveStripes 20s linear infinite;
  }
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.3;
  
  &.orb-1 {
    width: 425px; // 缩小15%: 500 -> 425
    height: 425px;
    background: rgba(255, 255, 255, 0.15);
    top: -85px;
    right: -85px;
    animation: float 20s ease-in-out infinite, pulse 10s ease-in-out infinite;
  }
  
  &.orb-2 {
    width: 340px; // 缩小15%: 400 -> 340
    height: 340px;
    background: rgba(255, 255, 255, 0.12);
    bottom: -85px;
    left: -85px;
    animation: float 25s ease-in-out infinite reverse, pulse 12s ease-in-out infinite 2s;
  }
  
  &.orb-3 {
    width: 297px; // 缩小15%: 350 -> 297
    height: 297px;
    background: rgba(255, 255, 255, 0.1);
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    animation: pulse 15s ease-in-out infinite, spin 40s linear infinite;
  }
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  
  .particle {
    position: absolute;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 50%;
    animation: floatUp linear infinite, twinkle 3s ease-in-out infinite;
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
  }
}

// 页面标题
.page-header {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-bottom: 12px; // 高度缩短30%: 20 -> 12
  animation: fadeInDown 0.8s ease-out;
  
  .header-content {
    padding: 12px 17px; // 高度缩短30%: 20 -> 12
    
    .page-title {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px; // 高度缩短30%: 12 -> 10
      font-size: 30px; // 高度缩短30%: 36 -> 30
      font-weight: 800;
      margin: 0 0 6px 0; // 高度缩短30%: 10 -> 6
      color: #fff;
      text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
      
      .title-icon {
        font-size: 32px; // 高度缩短30%: 40 -> 32
        color: #fff;
        filter: drop-shadow(0 4px 12px rgba(255, 255, 255, 0.4));
        animation: rotate 20s linear infinite, bounce 2s ease-in-out infinite;
      }
    }
    
    .page-subtitle {
      font-size: 12px; // 高度缩短30%: 13 -> 12
      color: rgba(255, 255, 255, 0.95);
      margin: 0;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      font-weight: 300;
      letter-spacing: 0.5px; // 稍微减少
    }
  }
}

// 玻璃态卡片
.glass-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.95) !important;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15) !important;
    transform: translateY(-4px);
  }
}

.search-card {
  margin-bottom: 12px; // 高度缩短30%: 20 -> 12
  border-radius: 17px;
  animation: slideInUp 0.6s ease-out;
  overflow: hidden;
  
  :deep(.el-card__body) {
    padding: 14px 20px; // 高度缩短30%: 20 -> 14
  }
  
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
  
  :deep(.el-button) {
    transition: all 0.2s ease;
    font-weight: 500;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
  
  :deep(.el-select), :deep(.el-input) {
    transition: all 0.2s ease;
    
    &:hover {
      transform: translateY(-1px);
    }
  }
}

.list-card {
  border-radius: 17px; // 缩小15%: 20 -> 17
  animation: slideInUp 0.7s ease-out;
  overflow: hidden;
  
  :deep(.el-card__header) {
    padding: 12px 20px; // 高度缩短30%: 20 -> 12
    background: rgba(255, 255, 255, 0.15);
    border-bottom: none;
    backdrop-filter: blur(10px);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: 20px;
      font-weight: 700;
      color: #1f2937;
      display: flex;
      align-items: center;
      gap: 12px;
      
      .el-icon {
        font-size: 24px;
        color: #667eea;
      }
    }
    
    .el-button {
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
      
      &:hover {
        box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
      }
    }
  }
}

.question-list {
  .question-item {
    padding: 12px 20px; // 高度缩短30%: 20 -> 12
    margin-bottom: 10px; // 高度缩短30%: 14 -> 10
    border-radius: 14px;
    cursor: pointer;
    position: relative;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(15px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    
    // 左侧装饰线
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #4a90e2; // 纯色
      border-radius: 4px 0 0 4px;
      transform: scaleY(0);
      transition: transform 0.3s ease;
    }
    
    // 右上角装饰
    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 0;
      width: 85px; // 缩小15%: 100 -> 85
      height: 85px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      opacity: 0;
      transition: opacity 0.3s ease;
      animation: ripple 3s ease-in-out infinite;
    }
    
    &:hover {
      background: rgba(255, 255, 255, 0.95);
      transform: translateY(-4px) scale(1.01);
      box-shadow: 0 12px 32px rgba(74, 144, 226, 0.25);
      border-color: rgba(74, 144, 226, 0.4);
      
      &::before {
        transform: scaleY(1);
        box-shadow: 0 0 20px rgba(74, 144, 226, 0.5);
      }
      
      &::after {
        opacity: 1;
      }
      
      .question-title span {
        color: #4a90e2;
      }
    }
    
    &:active {
      transform: translateY(-2px) scale(0.99);
    }
    
    // 为每个卡片添加延迟动画
    @for $i from 1 through 10 {
      &:nth-child(#{$i}) {
        animation: slideInLeft 0.5s cubic-bezier(0.4, 0, 0.2, 1) #{$i * 0.08}s both;
      }
    }
    
    .question-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 8px; // 高度缩短30%
      gap: 12px;
      
      .question-title {
        flex: 1;
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        flex-wrap: wrap;
        
        :deep(.el-tag) {
          transition: all $transition-fast;
          
          &:hover {
            transform: scale(1.05);
          }
        }
        
        span {
          font-size: $font-size-lg;
          font-weight: $font-weight-semibold;
          color: $text-primary;
          transition: color $transition-fast;
          line-height: 1.5;
        }
      }
      
      :deep(.el-tag) {
        flex-shrink: 0;
      }
    }
    
    .question-meta {
      display: flex;
      align-items: center;
      gap: $spacing-lg;
      font-size: $font-size-sm;
      color: $text-secondary;
      flex-wrap: wrap;
      
      :deep(.el-tag) {
        transition: all $transition-fast;
        
        &:hover {
          transform: scale(1.05);
          box-shadow: $shadow-sm;
        }
      }
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        transition: all $transition-fast;
        padding: $spacing-xs $spacing-sm;
        border-radius: $radius-md;
        
        &:hover {
          background: rgba($primary-color, 0.1);
          color: $primary-color;
          transform: translateY(-2px);
        }
        
        .el-icon {
          font-size: $font-size-md;
        }
      }
    }
  }
}

.pagination {
  margin-top: 12px; // 高度缩短30%
  display: flex;
  justify-content: center;
  padding: 12px; // 高度缩短30%
  
  :deep(.el-pagination) {
    .el-pager li {
      transition: all $transition-fast;
      
      &:hover {
        transform: scale(1.1);
      }
      
      &.is-active {
        box-shadow: $shadow-primary;
      }
    }
    
    button {
      transition: all $transition-fast;
      
      &:hover:not(:disabled) {
        transform: scale(1.1);
      }
    }
  }
}

// 动画关键帧
@keyframes backgroundPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

@keyframes moveStripes {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 200px 200px;
  }
}

@keyframes twinkle {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes spin {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

@keyframes ripple {
  0% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.3;
  }
  100% {
    transform: scale(1);
    opacity: 0.5;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) translateX(0); }
  25% { transform: translateY(-20px) translateX(10px); }
  50% { transform: translateY(-10px) translateX(-10px); }
  75% { transform: translateY(-30px) translateX(5px); }
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.7; }
}

@keyframes floatUp {
  0% {
    bottom: -10px;
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    bottom: 100%;
    opacity: 0;
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 响应式设计
@media (max-width: 768px) {
  .questions-page {
    padding: 16px;
  }
  
  .page-header .header-content .page-title {
    font-size: 32px;
    
    .title-icon {
      font-size: 36px;
    }
  }
  
  .page-header .header-content .page-subtitle {
    font-size: 14px;
  }
  
  .question-list {
    .question-item {
      padding: 16px;
      
      .question-header {
        flex-direction: column;
        align-items: flex-start;
      }
      
      .question-meta {
        gap: 8px;
      }
    }
  }
  
  .gradient-orb {
    &.orb-1, &.orb-2 {
      width: 300px;
      height: 300px;
    }
    
    &.orb-3 {
      width: 250px;
      height: 250px;
    }
  }
}
</style>


