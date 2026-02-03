<template>
  <div class="questions-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Compass /></el-icon>
        问题广场
      </h1>
      <p class="page-subtitle">
        探索知识的海洋，寻找问题的答案
      </p>
    </div>

    <!-- 搜索筛选 -->
    <el-card
      shadow="hover"
      class="search-card"
    >
      <el-form :inline="true">
        <el-form-item label="科目">
          <el-select
            v-model="searchForm.subjectId"
            placeholder="全部科目"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 120px"
          >
            <el-option
              label="待回答"
              value="PENDING"
            />
            <el-option
              label="已回答"
              value="ANSWERED"
            />
            <el-option
              label="已关闭"
              value="CLOSED"
            />
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
          <el-button
            type="primary"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 问题列表 -->
    <el-card
      shadow="hover"
      class="list-card"
    >
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><List /></el-icon>
            问题列表
          </span>
          <el-button
            v-if="userStore.isStudent"
            type="primary"
            @click="router.push('/student/ask')"
          >
            <el-icon><Plus /></el-icon>
            我要提问
          </el-button>
        </div>
      </template>

      <el-skeleton
        :loading="loading"
        :rows="5"
        animated
      >
        <el-empty
          v-if="questionList.length === 0"
          description="暂无问题"
        />
        
        <div
          v-else
          class="question-list"
        >
          <div
            v-for="question in questionList"
            :key="question.id"
            class="question-item"
            :data-status="question.status"
            @click="router.push(`/questions/${question.id}`)"
          >
            <div class="question-header">
              <div class="question-title">
                <el-tag
                  v-if="question.isTop"
                  type="danger"
                  size="small"
                >
                  置顶
                </el-tag>
                <el-tag
                  v-if="question.isFeatured"
                  type="warning"
                  size="small"
                >
                  精选
                </el-tag>
                <span>{{ question.title }}</span>
              </div>
              <el-tag
                :type="getStatusType(question.status)"
                size="small"
              >
                {{ getStatusText(question.status) }}
              </el-tag>
            </div>

            <div class="question-meta">
              <el-tag
                size="small"
                effect="plain"
              >
                {{ question.subjectName }}
              </el-tag>
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

onMounted(() => {
  loadSubjects()
  loadQuestions()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.questions-page {
  position: relative;
  width: 100%;
  min-height: 100%;
  padding: 0;
  max-width: none;
  margin: 0;
}

// 页面标题 - 简洁风格
.page-header {
  margin-bottom: $spacing-6;
  
  .page-title {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin: 0 0 $spacing-sm 0;
    
    .el-icon {
      font-size: $font-size-2xl;
      color: $color-primary;
    }
  }
  
  .page-subtitle {
    font-size: $font-size-md;
    color: $text-secondary;
    margin: 0;
    font-weight: $font-weight-regular;
  }
}

// 搜索卡片
.search-card {
  margin-bottom: $spacing-6;
  border-radius: $radius-lg;
  
  :deep(.el-card__body) {
    padding: $spacing-5;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

// 列表卡片
.list-card {
  border-radius: $radius-lg;
  
  :deep(.el-card__header) {
    padding: $spacing-5;
    background: $bg-secondary;
    border-bottom: 1px solid $border-color-light;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      
      .el-icon {
        font-size: $font-size-xl;
        color: $color-primary;
      }
    }
  }
}

// 问题列表 - 教育社区风格
.question-list {
  .question-item {
    padding: $spacing-5;
    margin-bottom: $spacing-md;
    border-radius: $radius-lg;
    cursor: pointer;
    position: relative;
    transition: all $transition-base;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.78), rgba(255, 255, 255, 0.5));
    border: 1px solid rgba(255, 255, 255, 0.7);
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12), inset 0 1px 1px rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(24px) saturate(170%);
    -webkit-backdrop-filter: blur(24px) saturate(170%);
    
    &:hover {
      border-color: rgba(255, 255, 255, 0.85);
      box-shadow: 0 24px 52px rgba(15, 23, 42, 0.18), inset 0 1px 1px rgba(255, 255, 255, 0.9);

      .question-title span {
        color: $color-primary;
      }
    }
    
    .question-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: $spacing-md;
      gap: $spacing-md;
      
      .question-title {
        flex: 1;
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        flex-wrap: wrap;
        
        span {
          font-size: $font-size-lg;
          font-weight: $font-weight-semibold;
          color: $text-primary;
          transition: color $transition-fast;
          line-height: 1.5;
        }
      }
    }
    
    .question-meta {
      display: flex;
      align-items: center;
      gap: $spacing-lg;
      font-size: $font-size-sm;
      color: $text-secondary;
      flex-wrap: wrap;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        transition: all $transition-fast;
        
        &:hover {
          color: $color-primary;
        }
        
        .el-icon {
          font-size: $font-size-md;
        }
      }
    }
  }
}

// 分页
.pagination {
  margin-top: $spacing-6;
  display: flex;
  justify-content: center;
  padding: $spacing-4 0;
}

// 响应式设计
@media (max-width: $breakpoint-md) {
  .questions-page {
    padding: 0;
  }
  
  .page-header .page-title {
    font-size: $font-size-2xl;
  }
  
  .question-list .question-item {
    padding: $spacing-4;
    
    .question-header {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .question-meta {
      gap: $spacing-sm;
    }
  }
}
</style>


