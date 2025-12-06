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
@import '@/assets/styles/variables.scss';

.question-detail-page {
  padding: $spacing-xl;
  max-width: 1200px;
  margin: 0 auto;
  animation: fadeIn 0.5s ease-out;
}

.question-card {
  margin-bottom: $spacing-xl;
  border-radius: $radius-xl;
  border: 1px solid $border-color;
  box-shadow: $shadow-lg;
  transition: all $transition-base;
  animation: slideInUp 0.4s ease-out;
  overflow: hidden;
  
  &:hover {
    box-shadow: $shadow-2xl;
  }
  
  :deep(.el-card__body) {
    padding: $spacing-2xl;
  }

  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-lg;
    gap: $spacing-lg;
    padding-bottom: $spacing-lg;
    border-bottom: 2px solid $border-color;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 100px;
      height: 2px;
      background: $primary-color;
      animation: expand 0.6s ease-out 0.3s both;
    }

    .question-title {
      flex: 1;
      display: flex;
      align-items: flex-start;
      gap: $spacing-sm;
      flex-wrap: wrap;
      
      :deep(.el-tag) {
        animation: bounceIn 0.5s ease-out 0.2s both;
        transition: all $transition-fast;
        
        &:hover {
          transform: scale(1.1);
        }
      }

      h2 {
        width: 100%;
        margin: $spacing-sm 0 0;
        font-size: $font-size-3xl;
        font-weight: $font-weight-bold;
        color: $text-primary;
        line-height: 1.4;
        animation: fadeIn 0.6s ease-out 0.1s both;
      }
    }
    
    :deep(.el-tag) {
      flex-shrink: 0;
      animation: fadeInScale 0.5s ease-out 0.3s both;
    }
  }

  .question-meta {
    display: flex;
    align-items: center;
    gap: $spacing-xl;
    font-size: $font-size-sm;
    color: $text-secondary;
    flex-wrap: wrap;
    margin-bottom: $spacing-lg;
    
    :deep(.el-tag) {
      transition: all $transition-fast;
      
      &:hover {
        transform: scale(1.05);
        box-shadow: $shadow-md;
      }
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      padding: $spacing-xs $spacing-sm;
      border-radius: $radius-md;
      transition: all $transition-fast;
      
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

  .question-content {
    font-size: $font-size-md;
    line-height: $line-height-relaxed;
    color: $text-primary;
    white-space: pre-wrap;
    padding: $spacing-lg;
    background: $bg-secondary;
    border-radius: $radius-lg;
    border-left: 4px solid $primary-color;
    animation: slideInLeft 0.5s ease-out 0.3s both;
  }

  .question-images {
    margin-top: $spacing-lg;
    display: flex;
    gap: $spacing-md;
    flex-wrap: wrap;
    
    :deep(.el-image) {
      border-radius: $radius-lg;
      overflow: hidden;
      box-shadow: $shadow-md;
      transition: all $transition-base;
      cursor: pointer;
      
      &:hover {
        transform: scale(1.05) rotate(2deg);
        box-shadow: $shadow-xl;
      }
    }
  }

  .question-actions {
    margin-top: $spacing-xl;
    padding-top: $spacing-lg;
    border-top: 1px solid $border-color;
    display: flex;
    gap: $spacing-md;
    
    :deep(.el-button) {
      transition: all $transition-fast;
      
      &:hover {
        transform: translateY(-2px);
      }
      
      &:active {
        transform: scale(0.98);
      }
    }
  }
}

.answers-card {
  border-radius: $radius-xl;
  border: 1px solid $border-color;
  box-shadow: $shadow-lg;
  animation: slideInUp 0.5s ease-out 0.2s both;
  
  :deep(.el-card__header) {
    padding: $spacing-lg $spacing-xl;
    background: $bg-secondary;
    border-bottom: 2px solid $border-color;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $text-primary;
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      
      &::before {
        content: '';
        width: 4px;
        height: 24px;
        background: $primary-color;
        border-radius: $radius-sm;
      }
    }
  }

  .answers-list {
    .answer-item {
      padding: $spacing-xl;
      border-bottom: 1px solid $border-color;
      transition: all $transition-base;
      position: relative;
      
      // 为每个回答添加延迟动画
      @for $i from 1 through 5 {
        &:nth-child(#{$i}) {
          animation: slideInRight 0.5s ease-out #{$i * 0.1}s both;
        }
      }
      
      &:hover {
        background: $bg-hover;
        transform: translateX(-4px);
      }

      &:last-child {
        border-bottom: none;
      }

      &.accepted {
        background: rgba($success-color, 0.05);
        padding: $spacing-xl;
        border-radius: $radius-lg;
        margin-bottom: $spacing-lg;
        border: 2px solid rgba($success-color, 0.3);
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 4px;
          background: $success-color;
        }
        
        &:hover {
          box-shadow: $shadow-success;
        }
      }

      .answer-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: $spacing-lg;

        .teacher-info {
          display: flex;
          gap: $spacing-md;
          align-items: center;
          
          :deep(.el-avatar) {
            transition: all $transition-fast;
            box-shadow: $shadow-md;
            
            &:hover {
              transform: scale(1.1) rotate(5deg);
              box-shadow: $shadow-primary;
            }
          }

          .teacher-detail {
            .teacher-name {
              font-size: $font-size-lg;
              font-weight: $font-weight-semibold;
              display: flex;
              align-items: center;
              gap: $spacing-sm;
              color: $text-primary;
              
              :deep(.el-tag) {
                animation: pulse 2s ease-in-out infinite;
              }
            }

            .answer-time {
              font-size: $font-size-sm;
              color: $text-secondary;
              margin-top: $spacing-xs;
            }
          }
        }
        
        .answer-badge {
          :deep(.el-tag) {
            animation: bounceIn 0.6s ease-out;
            box-shadow: $shadow-success;
          }
        }
      }

      .answer-content {
        font-size: $font-size-md;
        line-height: $line-height-relaxed;
        color: $text-primary;
        white-space: pre-wrap;
        padding: $spacing-lg;
        background: $bg-secondary;
        border-radius: $radius-lg;
        border-left: 3px solid rgba($primary-color, 0.3);
      }

      .answer-images {
        margin-top: $spacing-md;
        display: flex;
        gap: $spacing-sm;
        flex-wrap: wrap;
        
        :deep(.el-image) {
          border-radius: $radius-lg;
          overflow: hidden;
          box-shadow: $shadow-md;
          transition: all $transition-base;
          cursor: pointer;
          
          &:hover {
            transform: scale(1.05);
            box-shadow: $shadow-xl;
          }
        }
      }

      .answer-actions {
        margin-top: $spacing-lg;
        display: flex;
        gap: $spacing-md;
        
        :deep(.el-button) {
          transition: all $transition-fast;
          
          &:hover {
            transform: translateY(-2px);
          }
          
          &:active {
            transform: scale(0.98);
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: $breakpoint-md) {
  .question-detail-page {
    padding: $spacing-md;
  }
  
  .question-card {
    :deep(.el-card__body) {
      padding: $spacing-lg;
    }
    
    .question-header {
      flex-direction: column;
      
      .question-title h2 {
        font-size: $font-size-2xl;
      }
    }
  }
  
  .answers-card {
    .answers-list .answer-item {
      padding: $spacing-md;
      
      .answer-header {
        flex-direction: column;
        align-items: flex-start;
        gap: $spacing-md;
      }
    }
  }
}
</style>

