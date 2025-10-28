<template>
  <div class="subjects-page">
    <PageHeader
      title="科目管理"
      description="管理系统中的所有科目信息"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '科目管理' }]"
    >
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          添加科目
        </el-button>
      </template>
    </PageHeader>

    <!-- 科目卡片列表 -->
    <div class="subjects-grid">
      <LoadingState v-if="loading" size="large" text="加载中..." />
      
      <EmptyState
        v-else-if="!loading && subjects.length === 0"
        title="暂无科目"
        description="点击右上角【添加科目】按钮创建第一个科目"
        :icon="Reading"
      >
        <template #action>
          <el-button type="primary" @click="handleAdd">添加科目</el-button>
        </template>
      </EmptyState>

      <div v-else class="subject-cards">
        <el-card
          v-for="subject in subjects"
          :key="subject.id"
          class="subject-card"
          shadow="hover"
        >
          <div class="subject-header">
            <div class="subject-title">
              <el-icon class="subject-icon"><Reading /></el-icon>
              <h3>{{ subject.name }}</h3>
            </div>
            <el-tag :type="subject.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ subject.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </div>

          <div class="subject-code">
            <el-icon><DocumentCopy /></el-icon>
            <span>科目代码：{{ subject.code }}</span>
          </div>

          <div class="subject-description">
            {{ subject.description || '暂无描述' }}
          </div>

          <div class="subject-footer">
            <div class="subject-meta">
              <span class="meta-item">
                <el-icon><Sort /></el-icon>
                排序：{{ subject.sortOrder }}
              </span>
            </div>
            <div class="subject-actions">
              <el-button
                type="primary"
                size="small"
                link
                @click="handleEdit(subject)"
              >
                编辑
              </el-button>
              <el-button
                v-if="subject.status === 'ACTIVE'"
                type="warning"
                size="small"
                link
                @click="handleDisable(subject)"
              >
                禁用
              </el-button>
              <el-button
                v-else
                type="success"
                size="small"
                link
                @click="handleEnable(subject)"
              >
                启用
              </el-button>
              <el-button
                type="danger"
                size="small"
                link
                @click="handleDelete(subject)"
              >
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑科目' : '添加科目'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="科目名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入科目名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="科目代码" prop="code">
          <el-input
            v-model="form.code"
            placeholder="请输入科目代码，如：CS101"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="科目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入科目描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
            :max="999"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="INACTIVE">禁用</el-radio>
          </el-radio-group>
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
import { Plus, Reading, DocumentCopy, Sort } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import LoadingState from '@/components/common/LoadingState.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { getAllSubjects, createSubject, updateSubject, deleteSubject } from '@/api/subject'

const loading = ref(false)
const submitting = ref(false)
const subjects = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  sortOrder: 0,
  status: 'ACTIVE'
})

const rules = {
  name: [
    { required: true, message: '请输入科目名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入科目代码', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序顺序', trigger: 'blur' }
  ]
}

// 加载科目列表
const loadSubjects = async () => {
  loading.value = true
  try {
    const res = await getAllSubjects()
    subjects.value = res.data || []
  } catch (error) {
    console.error('加载科目列表失败:', error)
    ElMessage.error('加载科目列表失败')
  } finally {
    loading.value = false
  }
}

// 添加科目
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑科目
const handleEdit = (subject) => {
  isEdit.value = true
  Object.assign(form, {
    id: subject.id,
    name: subject.name,
    code: subject.code,
    description: subject.description,
    sortOrder: subject.sortOrder,
    status: subject.status
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateSubject(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createSubject(form)
          ElMessage.success('添加成功')
        }
        
        dialogVisible.value = false
        loadSubjects()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 禁用科目
const handleDisable = async (subject) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用科目 "${subject.name}" 吗？`,
      '禁用确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateSubject(subject.id, { ...subject, status: 'INACTIVE' })
    ElMessage.success('禁用成功')
    loadSubjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('禁用失败:', error)
      ElMessage.error('禁用失败')
    }
  }
}

// 启用科目
const handleEnable = async (subject) => {
  try {
    await updateSubject(subject.id, { ...subject, status: 'ACTIVE' })
    ElMessage.success('启用成功')
    loadSubjects()
  } catch (error) {
    console.error('启用失败:', error)
    ElMessage.error('启用失败')
  }
}

// 删除科目
const handleDelete = async (subject) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除科目 "${subject.name}" 吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteSubject(subject.id)
    ElMessage.success('删除成功')
    loadSubjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    code: '',
    description: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  formRef.value?.resetFields()
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.subjects-page {
  padding: $spacing-xl;
  animation: fadeIn 0.4s ease;
}

.subjects-grid {
  margin-top: $spacing-xl;
  min-height: 400px;
}

.subject-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-lg;
  animation: slideInRight 0.3s ease;
}

.subject-card {
  transition: all $transition-base;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-xl;
  }
  
  :deep(.el-card__body) {
    padding: $spacing-lg;
  }
}

.subject-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
  
  .subject-title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    
    .subject-icon {
      font-size: $font-size-2xl;
      color: $primary-color;
    }
    
    h3 {
      margin: 0;
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $text-primary;
    }
  }
}

.subject-code {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  
  .el-icon {
    color: $text-disabled;
  }
}

.subject-description {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: $line-height-relaxed;
  margin-bottom: $spacing-lg;
  min-height: 60px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.subject-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: $spacing-md;
  border-top: 1px solid $border-color;
  
  .subject-meta {
    display: flex;
    gap: $spacing-md;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
  
  .subject-actions {
    display: flex;
    gap: $spacing-xs;
  }
}

@media (max-width: $breakpoint-md) {
  .subjects-page {
    padding: $spacing-md;
  }
  
  .subject-cards {
    grid-template-columns: 1fr;
  }
}
</style>
