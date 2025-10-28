<template>
  <div class="questions-page">
    <!-- 搜索筛选 -->
    <el-card shadow="never" class="search-card">
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
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span>问题列表</span>
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
import { Plus, User, View, ChatLineRound, Clock } from '@element-plus/icons-vue'
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
.questions-page {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.question-list {
  .question-item {
    padding: 16px;
    border-bottom: 1px solid #eee;
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background: #f5f7fa;
    }

    &:last-child {
      border-bottom: none;
    }

    .question-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .question-title {
        flex: 1;
        font-size: 16px;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 8px;

        span {
          color: #303133;
        }
      }
    }

    .question-meta {
      display: flex;
      align-items: center;
      gap: 16px;
      font-size: 13px;
      color: #909399;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>


