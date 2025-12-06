<template>
  <div class="doc-operation-logs-page">
    <PageHeader
      title="文档操作日志"
      description="审计用户在文档查重与降重模块中的关键操作记录"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '文档操作日志' }]"
    />

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input
            v-model="searchForm.userId"
            placeholder="精确用户ID"
            clearable
            style="width: 180px"
            @clear="handleSearch"
          />
        </el-form-item>

        <el-form-item label="角色">
          <el-select
            v-model="searchForm.userRole"
            placeholder="全部角色"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="操作类型">
          <el-select
            v-model="searchForm.operationType"
            placeholder="全部类型"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option label="上传并查重" value="UPLOAD_AND_CHECK" />
            <el-option label="AI降重" value="REWRITE" />
            <el-option label="保存版本" value="SAVE_VERSION" />
            <el-option label="下载报告" value="DOWNLOAD" />
          </el-select>
        </el-form-item>

        <el-form-item label="文档ID">
          <el-input
            v-model="searchForm.documentId"
            placeholder="精确文档ID"
            clearable
            style="width: 180px"
            @clear="handleSearch"
          />
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
            @change="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="userId" label="用户ID" width="100" />

        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ formatRole(row.userRole) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作类型" width="140">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)" size="small">
              {{ formatOperationType(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="documentId" label="文档ID" width="100" />

        <el-table-column prop="paragraphId" label="段落ID" width="100" />

        <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />

        <el-table-column prop="clientIp" label="IP" width="140" />

        <el-table-column prop="userAgent" label="User-Agent" min-width="220" show-overflow-tooltip />

        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt || row.created_at) }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import PageHeader from '@/components/common/PageHeader.vue'
import { getDocOperationLogPage } from '@/api/doc'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  userId: '',
  userRole: '',
  operationType: '',
  documentId: '',
  timeRange: []
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      userId: searchForm.userId || undefined,
      userRole: searchForm.userRole || undefined,
      operationType: searchForm.operationType || undefined,
      documentId: searchForm.documentId || undefined,
      startTime: searchForm.timeRange && searchForm.timeRange.length > 0 ? searchForm.timeRange[0] : undefined,
      endTime: searchForm.timeRange && searchForm.timeRange.length > 1 ? searchForm.timeRange[1] : undefined
    }
    const res = await getDocOperationLogPage(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载操作日志失败:', error)
    ElMessage.error('加载操作日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.userId = ''
  searchForm.userRole = ''
  searchForm.operationType = ''
  searchForm.documentId = ''
  searchForm.timeRange = []
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatRole = (role) => {
  const map = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return map[role] || role || '-'
}

const formatOperationType = (type) => {
  const map = {
    UPLOAD_AND_CHECK: '上传并查重',
    REWRITE: 'AI降重',
    SAVE_VERSION: '保存版本',
    DOWNLOAD: '下载报告'
  }
  return map[type] || type || '-'
}

const getOperationTypeTag = (type) => {
  if (type === 'UPLOAD_AND_CHECK') return 'primary'
  if (type === 'REWRITE') return 'success'
  if (type === 'SAVE_VERSION') return 'warning'
  if (type === 'DOWNLOAD') return 'info'
  return 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.doc-operation-logs-page {
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

@media (max-width: $breakpoint-md) {
  .doc-operation-logs-page {
    padding: $spacing-md;
  }
}
</style>
