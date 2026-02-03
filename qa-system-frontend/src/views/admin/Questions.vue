<template>
  <div class="questions-page">
    <PageHeader
      title="问题管理"
      description="管理系统中的所有问题"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '问题管理' }]"
    />

    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-form
        :inline="true"
        :model="searchForm"
        class="search-form"
      >
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="问题标题、内容"
            clearable
            style="width: 250px"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="科目">
          <el-select
            v-model="searchForm.subjectId"
            placeholder="全部科目"
            clearable
            style="width: 150px"
            @change="handleSearch"
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
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option
              label="待解决"
              value="PENDING"
            />
            <el-option
              label="已解决"
              value="RESOLVED"
            />
            <el-option
              label="已关闭"
              value="CLOSED"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button
            :icon="Refresh"
            @click="handleReset"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        
        <el-table-column
          prop="title"
          label="问题标题"
          min-width="250"
        >
          <template #default="{ row }">
            <div class="question-title-cell">
              <el-link
                type="primary"
                :underline="false"
                @click="handleViewDetail(row)"
              >
                {{ row.title }}
              </el-link>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          label="提问者"
          width="120"
        >
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar
                :size="28"
                :src="row.studentAvatar"
              >
                {{ row.studentName?.[0] }}
              </el-avatar>
              <span>{{ row.studentName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="subjectName"
          label="科目"
          width="120"
        />

        <el-table-column
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.status === 'PENDING'"
              type="warning"
              size="small"
            >
              待解决
            </el-tag>
            <el-tag
              v-else-if="row.status === 'RESOLVED'"
              type="success"
              size="small"
            >
              已解决
            </el-tag>
            <el-tag
              v-else
              type="info"
              size="small"
            >
              已关闭
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="统计"
          width="150"
        >
          <template #default="{ row }">
            <div class="stats-cell">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ row.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ row.answerCount || 0 }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          label="创建时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="180"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="handleViewDetail(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              link
              @click="handleClose(row)"
            >
              关闭
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 问题详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="问题详情"
      width="900px"
      top="5vh"
    >
      <div
        v-if="currentQuestion"
        class="question-detail"
      >
        <div class="detail-header">
          <h2>{{ currentQuestion.title }}</h2>
          <el-tag
            :type="getStatusType(currentQuestion.status)"
            size="large"
          >
            {{ getStatusText(currentQuestion.status) }}
          </el-tag>
        </div>

        <div class="detail-meta">
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>提问者：{{ currentQuestion.studentName }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Reading /></el-icon>
            <span>科目：{{ currentQuestion.subjectName }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Timer /></el-icon>
            <span>{{ formatDate(currentQuestion.createTime) }}</span>
          </div>
        </div>

        <el-divider />

        <div class="detail-content">
          <h4>问题描述</h4>
          <div
            class="content-text"
            v-html="formatContent(currentQuestion.content)"
          />
        </div>

        <div
          v-if="currentQuestion.imageUrls && currentQuestion.imageUrls.length > 0"
          class="detail-images"
        >
          <h4>图片</h4>
          <div class="image-list">
            <el-image
              v-for="(url, index) in currentQuestion.imageUrls"
              :key="index"
              :src="url"
              :preview-src-list="currentQuestion.imageUrls"
              :initial-index="index"
              fit="cover"
              class="preview-image"
            />
          </div>
        </div>

        <div class="detail-stats">
          <div class="stat-item">
            <el-icon><View /></el-icon>
            <span>{{ currentQuestion.viewCount || 0 }} 浏览</span>
          </div>
          <div class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ currentQuestion.answerCount || 0 }} 回答</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">
          关闭
        </el-button>
        <el-button
          v-if="currentQuestion && currentQuestion.status === 'PENDING'"
          type="success"
          @click="handleClose(currentQuestion)"
        >
          关闭问题
        </el-button>
        <el-button
          type="danger"
          @click="handleDelete(currentQuestion)"
        >
          删除问题
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, View, ChatDotRound, User, Reading, Timer
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import PageHeader from '@/components/common/PageHeader.vue'
import { getQuestionList, deleteQuestion, closeQuestion } from '@/api/question'
import { getAllSubjects } from '@/api/subject'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const subjects = ref([])
const detailDialogVisible = ref(false)
const currentQuestion = ref(null)

const searchForm = reactive({
  keyword: '',
  subjectId: null,
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载科目列表
const loadSubjects = async () => {
  try {
    const res = await getAllSubjects()
    subjects.value = res.data || []
  } catch (error) {
    console.error('加载科目列表失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      subjectId: searchForm.subjectId || undefined,
      status: searchForm.status || undefined
    }
    
    const res = await getQuestionList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载问题列表失败:', error)
    ElMessage.error('加载问题列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.subjectId = null
  searchForm.status = ''
  handleSearch()
}

// 分页变化
const handlePageChange = () => {
  loadData()
}

// 查看详情
const handleViewDetail = async (row) => {
  currentQuestion.value = row
  detailDialogVisible.value = true
}

// 关闭问题
const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要关闭问题 "${row.title}" 吗？`,
      '关闭确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await closeQuestion(row.id)
    ElMessage.success('问题已关闭')
    detailDialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭问题失败:', error)
      ElMessage.error('关闭问题失败')
    }
  }
}

// 删除问题
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除问题 "${row.title}" 吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteQuestion(row.id)
    ElMessage.success('删除成功')
    detailDialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 格式化日期
const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 格式化内容（保留换行）
const formatContent = (content) => {
  if (!content) return '-'
  return content.replace(/\n/g, '<br>')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    PENDING: '待解决',
    RESOLVED: '已解决',
    CLOSED: '已关闭'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  loadSubjects()
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.questions-page {
  padding: $spacing-xl;
  animation: fadeIn 0.4s ease;
}

.search-card {
  margin-top: $spacing-xl;
  
  .search-form {
    margin-bottom: 0;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.table-card {
  margin-top: $spacing-lg;
  
  :deep(.el-table) {
    font-size: $font-size-sm;
    
    .el-table__header th {
      background: $bg-tertiary;
      color: $text-primary;
      font-weight: $font-weight-semibold;
    }
  }
}

.question-title-cell {
  .el-link {
    font-weight: $font-weight-medium;
    
    &:hover {
      color: $primary-hover;
    }
  }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  
  span {
    font-size: $font-size-sm;
  }
}

.stats-cell {
  display: flex;
  gap: $spacing-md;
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    font-size: $font-size-xs;
    color: $text-secondary;
    
    .el-icon {
      font-size: $font-size-sm;
    }
  }
}

.pagination-container {
  margin-top: $spacing-lg;
  display: flex;
  justify-content: flex-end;
}

// ==================== 问题详情 ====================
.question-detail {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-md;
    
    h2 {
      flex: 1;
      margin: 0;
      font-size: $font-size-2xl;
      font-weight: $font-weight-semibold;
      color: $text-primary;
    }
  }
  
  .detail-meta {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-lg;
    margin-top: $spacing-md;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      font-size: $font-size-sm;
      color: $text-secondary;
      
      .el-icon {
        color: $text-disabled;
      }
    }
  }
  
  .detail-content {
    h4 {
      margin: 0 0 $spacing-md;
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $text-primary;
    }
    
    .content-text {
      line-height: $line-height-relaxed;
      color: $text-secondary;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
  
  .detail-images {
    margin-top: $spacing-lg;
    
    h4 {
      margin: 0 0 $spacing-md;
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $text-primary;
    }
    
    .image-list {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-md;
      
      .preview-image {
        width: 120px;
        height: 120px;
        border-radius: $radius-md;
        cursor: pointer;
        transition: all $transition-base;
        
        &:hover {
          transform: scale(1.05);
          box-shadow: $shadow-lg;
        }
      }
    }
  }
  
  .detail-stats {
    display: flex;
    gap: $spacing-xl;
    margin-top: $spacing-lg;
    padding: $spacing-md;
    background: $bg-secondary;
    border-radius: $radius-md;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-size: $font-size-md;
      color: $text-secondary;
      
      .el-icon {
        font-size: $font-size-lg;
        color: $primary-color;
      }
    }
  }
}

@media (max-width: $breakpoint-md) {
  .questions-page {
    padding: $spacing-md;
  }
  
  .detail-header {
    flex-direction: column;
    align-items: flex-start !important;
  }
}
</style>
