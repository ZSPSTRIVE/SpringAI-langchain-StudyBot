<template>
  <div class="doc-documents-page">
    <PageHeader
      title="文档查重管理"
      description="查看并管理用户上传的查重文档"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '文档查重管理' }]"
    />

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文档标题">
          <el-input
            v-model="searchForm.title"
            placeholder="标题关键字"
            clearable
            style="width: 250px"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="用户ID">
          <el-input
            v-model="searchForm.userId"
            placeholder="精确用户ID"
            clearable
            style="width: 180px"
            @clear="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option label="已查重" value="CHECKED" />
            <el-option label="处理中" value="CHECKING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文档列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        @row-dblclick="handleViewDetail"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="userId" label="用户ID" width="100" />

        <el-table-column prop="title" label="文档标题" min-width="260" show-overflow-tooltip />

        <el-table-column label="整体查重率" width="140">
          <template #default="{ row }">
            <el-tag
              :type="getSimilarityType(row.overallSimilarity)"
              size="small"
            >
              {{ formatSimilarity(row.overallSimilarity) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt || row.created_at) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              :icon="Delete"
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

    <!-- 文档详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="文档详情"
      width="900px"
      top="5vh"
    >
      <div v-if="currentDetail" class="doc-detail">
        <div class="detail-header">
          <div>
            <h2>{{ currentDetail.document.title }}</h2>
            <p class="sub">文档ID：{{ currentDetail.document.id }} ｜ 用户ID：{{ currentDetail.document.userId }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getSimilarityType(currentDetail.document.overallSimilarity)" size="large">
              整体查重：{{ formatSimilarity(currentDetail.document.overallSimilarity) }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <el-row :gutter="16" class="summary-row">
          <el-col :xs="24" :sm="8">
            <div class="summary-item">
              <span class="label">段落数</span>
              <span class="value">{{ currentDetail.paragraphCount }}</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="summary-item">
              <span class="label">平均查重率</span>
              <span class="value">{{ formatSimilarity(currentDetail.avgSimilarity) }}</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="summary-item">
              <span class="label">高风险段落数</span>
              <span class="value">{{ currentDetail.highRiskParagraphs?.length || 0 }}</span>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <div class="paragraph-list">
          <h4>段落明细</h4>
          <el-table
            :data="currentDetail.paragraphs"
            size="small"
            style="width: 100%"
          >
            <el-table-column prop="paragraphIndex" label="序号" width="80" />
            <el-table-column label="查重率" width="120">
              <template #default="{ row }">
                <el-tag
                  :type="getSimilarityType(row.similarity)"
                  size="small"
                >
                  {{ formatSimilarity(row.similarity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="内容" min-width="400">
              <template #default="{ row }">
                <span class="paragraph-text">{{ row.originalText }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import PageHeader from '@/components/common/PageHeader.vue'
import { getDocPage, getDocDetail, deleteDocument } from '@/api/doc'

const loading = ref(false)
const tableData = ref([])
const detailDialogVisible = ref(false)
const currentDetail = ref(null)

const searchForm = reactive({
  title: '',
  userId: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载文档列表
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      title: searchForm.title || undefined,
      userId: searchForm.userId || undefined,
      status: searchForm.status || undefined
    }
    const res = await getDocPage(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载文档列表失败:', error)
    ElMessage.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.userId = ''
  searchForm.status = ''
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const handleViewDetail = async (row) => {
  try {
    const res = await getDocDetail(row.id)
    currentDetail.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('加载文档详情失败:', error)
    ElMessage.error('加载文档详情失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档「${row.title}」吗？此操作将同时删除该文档的所有段落和版本数据，不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await deleteDocument(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文档失败:', error)
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  }
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatSimilarity = (value) => {
  if (value === null || value === undefined) return '-'
  return `${value.toFixed(1)}%`
}

const getSimilarityType = (value) => {
  if (value === null || value === undefined) return 'info'
  if (value >= 70) return 'danger'
  if (value >= 40) return 'warning'
  return 'success'
}

const getStatusText = (status) => {
  const map = {
    CHECKED: '已查重',
    CHECKING: '查重中',
    COMPLETED: '已完成'
  }
  return map[status] || status || '-'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.doc-documents-page {
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

.pagination-container {
  margin-top: $spacing-lg;
  display: flex;
  justify-content: flex-end;
}

.doc-detail {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-lg;

    h2 {
      margin: 0;
      font-size: $font-size-2xl;
      font-weight: $font-weight-semibold;
      color: $text-primary;
    }

    .sub {
      margin-top: $spacing-xs;
      color: $text-secondary;
      font-size: $font-size-sm;
    }
  }

  .summary-row {
    margin-bottom: $spacing-md;

    .summary-item {
      padding: $spacing-md;
      background: $bg-secondary;
      border-radius: $radius-md;

      .label {
        display: block;
        font-size: $font-size-xs;
        color: $text-secondary;
        margin-bottom: $spacing-xs;
      }

      .value {
        font-size: $font-size-xl;
        font-weight: $font-weight-semibold;
      }
    }
  }

  .paragraph-list {
    margin-top: $spacing-md;

    h4 {
      margin-bottom: $spacing-md;
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
    }

    .paragraph-text {
      display: inline-block;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

@media (max-width: $breakpoint-md) {
  .doc-documents-page {
    padding: $spacing-md;
  }
}
</style>
