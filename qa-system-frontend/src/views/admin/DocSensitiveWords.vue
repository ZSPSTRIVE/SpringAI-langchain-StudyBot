<template>
  <div class="doc-sensitive-words-page">
    <PageHeader
      title="敏感词配置"
      description="管理文档敏感词库，用于查重与降重内容过滤"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '敏感词配置' }]"
    >
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增敏感词
        </el-button>
      </template>
    </PageHeader>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="敏感词或说明关键字"
            clearable
            style="width: 240px"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="分类">
          <el-select
            v-model="searchForm.category"
            placeholder="全部分类"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option label="默认" value="DEFAULT" />
            <el-option label="政治" value="POLITICS" />
            <el-option label="暴力" value="VIOLENCE" />
            <el-option label="色情" value="PORN" />
            <el-option label="违法" value="ILLEGAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="searchForm.enabled"
            placeholder="全部状态"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option label="启用" value="true" />
            <el-option label="禁用" value="false" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 敏感词列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="word" label="敏感词" min-width="180" show-overflow-tooltip />

        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ formatCategory(row.category) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="风险等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small">
              {{ formatLevel(row.level) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="是否启用" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt || row.created_at) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
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

    <!-- 新增/编辑敏感词对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
      >
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="form.word" placeholder="请输入敏感词内容" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="默认" value="DEFAULT" />
            <el-option label="政治" value="POLITICS" />
            <el-option label="暴力" value="VIOLENCE" />
            <el-option label="色情" value="PORN" />
            <el-option label="违法" value="ILLEGAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="风险等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择风险等级">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否启用" prop="enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>

        <el-form-item label="说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="可选，补充敏感词使用说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import PageHeader from '@/components/common/PageHeader.vue'
import {
  getSensitiveWordPage,
  createSensitiveWord,
  updateSensitiveWord,
  deleteSensitiveWord
} from '@/api/doc'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  keyword: '',
  category: '',
  enabled: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增敏感词')
const formRef = ref()
const form = reactive({
  id: null,
  word: '',
  category: 'DEFAULT',
  level: 'MEDIUM',
  enabled: true,
  description: ''
})
const submitting = ref(false)

const rules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      category: searchForm.category || undefined,
      enabled: searchForm.enabled === '' ? undefined : searchForm.enabled
    }
    const res = await getSensitiveWordPage(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载敏感词列表失败:', error)
    ElMessage.error('加载敏感词列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.enabled = ''
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增敏感词'
  Object.assign(form, {
    id: null,
    word: '',
    category: 'DEFAULT',
    level: 'MEDIUM',
    enabled: true,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑敏感词'
  Object.assign(form, {
    id: row.id,
    word: row.word,
    category: row.category || 'DEFAULT',
    level: row.level || 'MEDIUM',
    enabled: row.enabled !== false,
    description: row.description || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除敏感词 "${row.word}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteSensitiveWord(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除敏感词失败:', error)
      ElMessage.error('删除敏感词失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        word: form.word,
        category: form.category,
        level: form.level,
        enabled: form.enabled,
        description: form.description
      }
      if (form.id) {
        await updateSensitiveWord(form.id, payload)
        ElMessage.success('更新成功')
      } else {
        await createSensitiveWord(payload)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('保存敏感词失败:', error)
      ElMessage.error('保存敏感词失败')
    } finally {
      submitting.value = false
    }
  })
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatCategory = (category) => {
  const map = {
    DEFAULT: '默认',
    POLITICS: '政治',
    VIOLENCE: '暴力',
    PORN: '色情',
    ILLEGAL: '违法',
    OTHER: '其他'
  }
  return map[category] || category || '-'
}

const formatLevel = (level) => {
  const map = {
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }
  return map[level] || level || '-'
}

const getLevelType = (level) => {
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  if (level === 'LOW') return 'success'
  return 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.doc-sensitive-words-page {
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
  .doc-sensitive-words-page {
    padding: $spacing-md;
  }
}
</style>
