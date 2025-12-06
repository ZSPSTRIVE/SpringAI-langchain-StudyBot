<template>
  <div class="students-page">
    <PageHeader
      title="学生管理"
      description="管理系统中的所有学生用户"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '学生管理' }]"
    >
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          添加学生
        </el-button>
      </template>
    </PageHeader>

    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="姓名、学号、邮箱"
            clearable
            style="width: 250px"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
            <el-option label="锁定" value="LOCKED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="userId" label="ID" width="80" />
        
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              {{ row.realName?.[0] || '学' }}
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="姓名" width="120" />
        
        <el-table-column prop="studentNo" label="学号" width="120" />
        
        <el-table-column prop="username" label="用户名" width="120" />
        
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 'M'" type="primary" size="small">男</el-tag>
            <el-tag v-else-if="row.gender === 'F'" type="danger" size="small">女</el-tag>
            <el-tag v-else type="info" size="small">保密</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="college" label="学院" min-width="120" />
        
        <el-table-column prop="major" label="专业" min-width="120" />
        
        <el-table-column prop="className" label="班级" width="100" />
        
        <el-table-column prop="email" label="邮箱" min-width="150" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ACTIVE'" type="success" size="small">
              正常
            </el-tag>
            <el-tag v-else-if="row.status === 'DISABLED'" type="warning" size="small">
              禁用
            </el-tag>
            <el-tag v-else-if="row.status === 'LOCKED'" type="danger" size="small">
              锁定
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'ACTIVE'"
              type="warning"
              size="small"
              link
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              link
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button
              type="primary"
              size="small"
              link
              @click="handleResetPassword(row)"
            >
              重置密码
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

    <!-- 添加学生弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加学生"
      width="700px"
    >
      <el-form
        ref="formRef"
        :model="studentForm"
        :rules="studentRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="studentForm.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="studentForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="studentForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="studentForm.studentNo" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="studentForm.gender">
                <el-radio value="M">男</el-radio>
                <el-radio value="F">女</el-radio>
                <el-radio value="U">保密</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-input-number
                v-model="studentForm.grade"
                :min="2000"
                :max="2099"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院" prop="college">
              <el-input v-model="studentForm.college" placeholder="请输入学院" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="studentForm.major" placeholder="请输入专业" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级" prop="className">
              <el-input v-model="studentForm.className" placeholder="请输入班级" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="studentForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="studentForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog
      v-model="resetPasswordVisible"
      title="重置密码"
      width="500px"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="用户">
          <el-input v-model="resetPasswordForm.userName" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="resetPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit">
          确定重置
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
import { getStudentPage, updateUserStatus, deleteUser, resetUserPassword, createStudent } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 添加学生相关
const dialogVisible = ref(false)
const formRef = ref()
const studentForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  gender: 'U',
  studentNo: '',
  major: '',
  className: '',
  grade: null,
  college: ''
})

const studentRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

// 重置密码相关
const resetPasswordVisible = ref(false)
const resetPasswordFormRef = ref()
const resetPasswordForm = reactive({
  userId: null,
  userName: '',
  newPassword: ''
})

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ]
}

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined
    }
    
    const res = await getStudentPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
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
  searchForm.status = ''
  handleSearch()
}

// 分页变化
const handlePageChange = () => {
  loadData()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 禁用用户
const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用学生 "${row.realName}" 吗？`,
      '禁用确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateUserStatus(row.userId, 'DISABLED')
    ElMessage.success('禁用成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('禁用失败:', error)
      ElMessage.error('禁用失败')
    }
  }
}

// 启用用户
const handleEnable = async (row) => {
  try {
    await updateUserStatus(row.userId, 'ACTIVE')
    ElMessage.success('启用成功')
    loadData()
  } catch (error) {
    console.error('启用失败:', error)
    ElMessage.error('启用失败')
  }
}

// 添加学生
const handleAdd = () => {
  Object.assign(studentForm, {
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    gender: 'U',
    studentNo: '',
    major: '',
    className: '',
    grade: null,
    college: ''
  })
  dialogVisible.value = true
}

// 提交创建学生
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createStudent(studentForm)
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('创建失败:', error)
        ElMessage.error(error.response?.data?.message || '创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置密码（打开弹窗）
const handleResetPassword = (row) => {
  resetPasswordForm.userId = row.userId
  resetPasswordForm.userName = row.realName
  resetPasswordForm.newPassword = ''
  resetPasswordVisible.value = true
}

// 提交重置密码
const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return

  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await resetUserPassword(
          resetPasswordForm.userId,
          resetPasswordForm.newPassword
        )
        ElMessage.success('密码重置成功')
        resetPasswordVisible.value = false
      } catch (error) {
        console.error('重置密码失败:', error)
        ElMessage.error(error.response?.data?.message || '重置密码失败')
      }
    }
  })
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${row.realName}" 吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
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

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.students-page {
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

// ==================== 响应式 ====================
@media (max-width: $breakpoint-md) {
  .students-page {
    padding: $spacing-md;
  }
}
</style>
