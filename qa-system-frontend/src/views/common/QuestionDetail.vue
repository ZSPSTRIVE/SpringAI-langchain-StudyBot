<template>
  <div class="question-detail-page">
    <el-skeleton :loading="loading" :rows="10" animated>
      <!-- 问题详情 -->
      <el-card v-if="question" shadow="never" class="question-card">
        <div class="question-header">
          <div class="question-title">
            <el-tag v-if="question.isTop" type="danger" size="small">置顶</el-tag>
            <el-tag v-if="question.isFeatured" type="warning" size="small">精选</el-tag>
            <h2>{{ question.title }}</h2>
          </div>
          <el-tag :type="getStatusType(question.status)" size="large">
            {{ getStatusText(question.status) }}
          </el-tag>
        </div>

        <div class="question-meta">
          <el-tag>{{ question.subjectName }}</el-tag>
          <span class="meta-item">
            <el-icon><User /></el-icon>
            {{ question.studentName }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            {{ question.viewCount }} 浏览
          </span>
          <span class="meta-item">
            <el-icon><Clock /></el-icon>
            {{ formatTime(question.createTime) }}
          </span>
        </div>

        <el-divider />

        <div class="question-content" v-html="question.content"></div>

        <div v-if="question.images && question.images.length > 0" class="question-images">
          <el-image
            v-for="(image, index) in question.images"
            :key="index"
            :src="image"
            :preview-src-list="question.images"
            fit="cover"
            style="width: 200px; height: 200px; margin-right: 10px"
          />
        </div>

        <!-- 操作按钮 -->
        <div v-if="canManageQuestion" class="question-actions">
          <el-button v-if="question.status === 'PENDING'" type="warning" size="small" @click="handleCloseQuestion">
            关闭问题
          </el-button>
          <el-button type="danger" size="small" @click="handleDeleteQuestion">
            删除问题
          </el-button>
        </div>
      </el-card>

      <!-- 回答列表 -->
      <el-card v-if="question" shadow="never" class="answers-card">
        <template #header>
          <div class="card-header">
            <span>{{ answers.length }} 个回答</span>
            <el-button v-if="userStore.isTeacher && question.status !== 'CLOSED'" type="primary" @click="showAnswerDialog = true">
              <el-icon><EditPen /></el-icon>
              我来回答
            </el-button>
          </div>
        </template>

        <el-empty v-if="answers.length === 0" description="暂无回答" />

        <div v-else class="answers-list">
          <div
            v-for="answer in answers"
            :key="answer.id"
            class="answer-item"
            :class="{ accepted: answer.isAccepted }"
          >
            <div class="answer-header">
              <div class="teacher-info">
                <el-avatar :size="40">{{ answer.teacherName?.[0] }}</el-avatar>
                <div class="teacher-detail">
                  <div class="teacher-name">
                    {{ answer.teacherName }}
                    <el-tag v-if="answer.teacherTitle" size="small" type="success">
                      {{ answer.teacherTitle }}
                    </el-tag>
                  </div>
                  <div class="answer-time">{{ formatTime(answer.createTime) }}</div>
                </div>
              </div>
              <div class="answer-badge">
                <el-tag v-if="answer.isAccepted" type="success" effect="dark">
                  <el-icon><Select /></el-icon>
                  已采纳
                </el-tag>
              </div>
            </div>

            <div class="answer-content" v-html="answer.content"></div>

            <div v-if="answer.images && answer.images.length > 0" class="answer-images">
              <el-image
                v-for="(image, index) in answer.images"
                :key="index"
                :src="image"
                :preview-src-list="answer.images"
                fit="cover"
                style="width: 150px; height: 150px; margin-right: 10px"
              />
            </div>

            <div class="answer-actions">
              <el-button text :icon="answer.isAccepted ? 'Check' : ''">
                <el-icon><Promotion /></el-icon>
                {{ answer.likeCount }} 点赞
              </el-button>
              <el-button
                v-if="canAcceptAnswer && !answer.isAccepted"
                type="success"
                text
                @click="handleAcceptAnswer(answer.id)"
              >
                <el-icon><Select /></el-icon>
                采纳
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </el-skeleton>

    <!-- 回答对话框 -->
    <el-dialog v-model="showAnswerDialog" title="回答问题" width="700px">
      <el-form :model="answerForm" :rules="answerRules" ref="answerFormRef">
        <el-form-item prop="content">
          <el-input
            v-model="answerForm.content"
            type="textarea"
            placeholder="请输入你的回答..."
            :rows="8"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAnswerDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingAnswer" @click="handleSubmitAnswer">
          提交回答
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, View, Clock, EditPen, Select, Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getQuestionById, deleteQuestion, closeQuestion } from '@/api/question'
import { getAnswersByQuestionId, createAnswer, acceptAnswer } from '@/api/answer'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const question = ref(null)
const answers = ref([])
const showAnswerDialog = ref(false)
const answerFormRef = ref()
const submittingAnswer = ref(false)

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

const canManageQuestion = computed(() => {
  if (!question.value || !userStore.userInfo) return false
  return question.value.studentId === userStore.userInfo.userId || userStore.isAdmin
})

const canAcceptAnswer = computed(() => {
  if (!question.value || !userStore.userInfo) return false
  return question.value.studentId === userStore.userInfo.userId && question.value.status !== 'CLOSED'
})

const loadQuestion = async () => {
  loading.value = true
  try {
    const res = await getQuestionById(route.params.id)
    question.value = res.data
    answerForm.questionId = res.data.id
  } catch (error) {
    console.error('加载问题失败:', error)
    ElMessage.error('问题不存在或已被删除')
    router.back()
  } finally {
    loading.value = false
  }
}

const loadAnswers = async () => {
  try {
    const res = await getAnswersByQuestionId(route.params.id)
    answers.value = res.data
  } catch (error) {
    console.error('加载回答失败:', error)
  }
}

const handleSubmitAnswer = async () => {
  if (!answerFormRef.value) return

  await answerFormRef.value.validate(async (valid) => {
    if (valid) {
      submittingAnswer.value = true
      try {
        await createAnswer(answerForm)
        ElMessage.success('回答成功')
        showAnswerDialog.value = false
        answerForm.content = ''
        loadQuestion()
        loadAnswers()
      } catch (error) {
        console.error('回答失败:', error)
      } finally {
        submittingAnswer.value = false
      }
    }
  })
}

const handleAcceptAnswer = async (answerId) => {
  try {
    await ElMessageBox.confirm('确定采纳这个回答吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await acceptAnswer(answerId)
    ElMessage.success('采纳成功')
    loadAnswers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('采纳失败:', error)
    }
  }
}

const handleCloseQuestion = async () => {
  try {
    await ElMessageBox.confirm('确定关闭这个问题吗？关闭后将无法继续回答', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await closeQuestion(question.value.id)
    ElMessage.success('问题已关闭')
    loadQuestion()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭失败:', error)
    }
  }
}

const handleDeleteQuestion = async () => {
  try {
    await ElMessageBox.confirm('确定删除这个问题吗？此操作不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteQuestion(question.value.id)
    ElMessage.success('删除成功')
    router.push('/student/questions')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
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
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadQuestion()
  loadAnswers()
})
</script>

<style scoped lang="scss">
.question-detail-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.question-card {
  margin-bottom: 20px;

  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;

    .question-title {
      flex: 1;

      h2 {
        margin: 8px 0 0;
        font-size: 24px;
        font-weight: 500;
      }
    }
  }

  .question-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 14px;
    color: #909399;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .question-content {
    font-size: 15px;
    line-height: 1.8;
    color: #303133;
    white-space: pre-wrap;
  }

  .question-images {
    margin-top: 16px;
  }

  .question-actions {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #eee;
  }
}

.answers-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .answers-list {
    .answer-item {
      padding: 20px 0;
      border-bottom: 1px solid #eee;

      &:last-child {
        border-bottom: none;
      }

      &.accepted {
        background: #f0f9ff;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 16px;
      }

      .answer-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .teacher-info {
          display: flex;
          gap: 12px;

          .teacher-detail {
            .teacher-name {
              font-size: 15px;
              font-weight: 500;
              display: flex;
              align-items: center;
              gap: 8px;
            }

            .answer-time {
              font-size: 13px;
              color: #909399;
              margin-top: 4px;
            }
          }
        }
      }

      .answer-content {
        font-size: 15px;
        line-height: 1.8;
        color: #303133;
        white-space: pre-wrap;
      }

      .answer-images {
        margin-top: 12px;
      }

      .answer-actions {
        margin-top: 12px;
        display: flex;
        gap: 16px;
      }
    }
  }
}
</style>

