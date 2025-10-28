<template>
  <div class="collections-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的收藏（{{ questions.length }}）</span>
        </div>
      </template>

      <el-skeleton :loading="loading" :rows="5" animated>
        <el-empty v-if="questions.length === 0" description="暂无收藏" />

        <div v-else class="question-list">
          <div
            v-for="question in questions"
            :key="question.id"
            class="question-item"
            @click="router.push(`/questions/${question.id}`)"
          >
            <div class="question-header">
              <div class="question-title">
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, ChatLineRound, Clock } from '@element-plus/icons-vue'
import { getCollectedQuestions } from '@/api/collection'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const loading = ref(false)
const questions = ref([])

const loadCollections = async () => {
  loading.value = true
  try {
    const res = await getCollectedQuestions()
    questions.value = res.data
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  } finally {
    loading.value = false
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
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadCollections()
})
</script>

<style scoped lang="scss">
.collections-page {
  padding: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
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
</style>


