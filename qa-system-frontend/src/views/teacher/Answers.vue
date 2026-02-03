<template>
  <div class="answers-page">
    <!-- 筛选区域 -->
    <el-card
      shadow="never"
      class="filter-card"
    >
      <el-radio-group
        v-model="filterStatus"
        @change="handleFilterChange"
      >
        <el-radio-button value="">
          全部问题
        </el-radio-button>
        <el-radio-button value="PENDING">
          待回答
        </el-radio-button>
        <el-radio-button value="ANSWERED">
          已回答
        </el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 问题列表 -->
    <el-card
      shadow="never"
      class="list-card"
    >
      <template #header>
        <div class="card-header">
          <span>问题列表（共 {{ pagination.total }} 个）</span>
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
          >
            <div
              class="question-main"
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
                  <el-icon><ChatLineRound /></el-icon>
                  {{ question.answerCount }} 回答
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(question.createTime) }}
                </span>
              </div>
            </div>

            <div class="question-actions">
              <el-button
                v-if="question.status !== 'CLOSED'"
                type="primary"
                size="small"
                @click="handleQuickAnswer(question)"
              >
                <el-icon><EditPen /></el-icon>
                快速回答
              </el-button>
              <el-button
                size="small"
                @click="router.push(`/questions/${question.id}`)"
              >
                查看详情
              </el-button>
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

    <!-- 快速回答对话框 -->
    <el-dialog
      v-model="showAnswerDialog"
      :title="`回答问题: ${currentQuestion?.title}`"
      width="800px"
    >
      <div class="question-preview">
        <div class="question-content">
          {{ currentQuestion?.content }}
        </div>
      </div>

      <el-divider />

      <el-form
        ref="answerFormRef"
        :model="answerForm"
        :rules="answerRules"
      >
        <el-form-item
          label="回答内容"
          prop="content"
        >
          <el-input
            v-model="answerForm.content"
            type="textarea"
            placeholder="请输入你的回答..."
            :rows="10"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAnswerDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmitAnswer"
        >
          提交回答
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ChatLineRound, Clock, EditPen } from '@element-plus/icons-vue'
import { getQuestionPage } from '@/api/question'
import { createAnswer } from '@/api/answer'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const loading = ref(false)
const questionList = ref([])
const filterStatus = ref('PENDING')
const showAnswerDialog = ref(false)
const currentQuestion = ref(null)
const answerFormRef = ref()
const submitting = ref(false)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const answerForm = reactive({
  questionId: null,
  content: '',
  images: []
})

const answerRules = {
  content: [
    { required: true, message: '请输入回答内容', trigger: 'blur' },
    { min: 10, message: '回答内容至少10个字符', trigger: 'blur' }
  ]
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      status: filterStatus.value || undefined
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

const handleFilterChange = () => {
  pagination.page = 1
  loadQuestions()
}

const handleQuickAnswer = (question) => {
  currentQuestion.value = question
  answerForm.questionId = question.id
  answerForm.content = ''
  showAnswerDialog.value = true
}

const handleSubmitAnswer = async () => {
  if (!answerFormRef.value) return

  await answerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createAnswer(answerForm)
        ElMessage.success('回答成功')
        showAnswerDialog.value = false
        loadQuestions()
      } catch (error) {
        console.error('回答失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
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
  loadQuestions()
})
</script>

<style scoped lang="scss">
.answers-page {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.list-card {
  .card-header {
    font-size: 16px;
    font-weight: 500;
  }
}

.question-list {
  .question-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #eee;
    transition: background 0.3s;

    &:hover {
      background: #f5f7fa;
    }

    &:last-child {
      border-bottom: none;
    }

    .question-main {
      flex: 1;
      cursor: pointer;

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

    .question-actions {
      margin-left: 20px;
      display: flex;
      gap: 8px;
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.question-preview {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;

  .question-content {
    font-size: 14px;
    line-height: 1.6;
    color: #606266;
    white-space: pre-wrap;
    max-height: 200px;
    overflow-y: auto;
  }
}
</style>


