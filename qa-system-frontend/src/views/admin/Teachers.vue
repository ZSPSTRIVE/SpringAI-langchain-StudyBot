<template>
  <div class="teachers-page">
    <PageHeader
      title="教师管理"
      description="管理系统中的所有教师用户"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '教师管理' }]"
    />

    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="姓名、工号、邮箱"
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
          <el-button type="success" :icon="Plus" @click="handleAddTeacher">
            新增教师
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="userId" label="ID" width="80" />
        
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              {{ row.realName?.[0] || '教' }}
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="姓名" width="120" />
        
        <el-table-column prop="teacherNo" label="工号" width="120" />
        
        <el-table-column prop="username" label="用户名" width="120" />
        
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 'M'" type="primary" size="small">男</el-tag>
            <el-tag v-else-if="row.gender === 'F'" type="danger" size="small">女</el-tag>
            <el-tag v-else type="info" size="small">保密</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="college" label="学院" min-width="120" />
        
        <el-table-column prop="title" label="职称" width="120" />
        
        <el-table-column prop="office" label="办公室" width="120" />
        
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

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              type="info"
              size="small"
              link
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
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

    <!-- 新增教师对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增教师账号"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="100px"
        class="add-teacher-form"
      >
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="addForm.username" placeholder="请输入登录用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="addForm.password" type="password" show-password placeholder="请输入初始密码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="addForm.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="M" />
                <el-option label="女" value="F" />
                <el-option label="保密" value="U" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="addForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="addForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">教师信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="teacherNo">
              <el-input v-model="addForm.teacherNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="addForm.title" placeholder="请选择职称" style="width: 100%" allow-create filterable>
                <el-option label="助教" value="助教" />
                <el-option label="讲师" value="讲师" />
                <el-option label="副教授" value="副教授" />
                <el-option label="教授" value="教授" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院" prop="college">
              <el-input v-model="addForm.college" placeholder="请输入所属学院" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="办公室" prop="office">
              <el-input v-model="addForm.office" placeholder="请输入办公室位置" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="研究方向" prop="research">
          <el-input v-model="addForm.research" type="textarea" :rows="2" placeholder="请输入研究方向" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleSubmitAdd">
          确认创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 教师详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="教师详情"
      width="600px"
    >
      <div v-if="currentTeacher" class="teacher-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">
            {{ currentTeacher.realName }}
          </el-descriptions-item>
          <el-descriptions-item label="工号">
            {{ currentTeacher.teacherNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ currentTeacher.username }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            <el-tag v-if="currentTeacher.gender === 'M'" type="primary" size="small">男</el-tag>
            <el-tag v-else-if="currentTeacher.gender === 'F'" type="danger" size="small">女</el-tag>
            <el-tag v-else type="info" size="small">保密</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ currentTeacher.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ currentTeacher.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="学院">
            {{ currentTeacher.college || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="职称">
            {{ currentTeacher.title || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="办公室">
            {{ currentTeacher.office || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="研究方向" :span="2">
            {{ currentTeacher.research || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ currentTeacher.bio || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">
            {{ formatDate(currentTeacher.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import PageHeader from '@/components/common/PageHeader.vue'
import { getTeacherPage, updateUserStatus, deleteUser, resetUserPassword, createTeacher } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const detailDialogVisible = ref(false)
const currentTeacher = ref(null)

// 新增教师相关
const addDialogVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref(null)

const addForm = reactive({
  username: '',
  password: '',
  realName: '',
  gender: 'U',
  email: '',
  phone: '',
  teacherNo: '',
  title: '',
  college: '',
  office: '',
  research: ''
})

const addFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
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
    
    const res = await getTeacherPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载教师列表失败:', error)
    ElMessage.error('加载教师列表失败')
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

// 查看详情
const handleViewDetail = (row) => {
  currentTeacher.value = row
  detailDialogVisible.value = true
}

// 禁用用户
const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用教师 "${row.realName}" 吗？`,
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

// 重置密码
const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置教师 "${row.realName}" 的密码吗？`,
      '重置密码确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await resetUserPassword(row.userId)
    await ElMessageBox.alert(
      `新密码：${res.data}`,
      '密码重置成功',
      {
        confirmButtonText: '我知道了',
        type: 'success'
      }
    )
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除教师 "${row.realName}" 吗？此操作不可恢复！`,
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

// 打开新增教师对话框
const handleAddTeacher = () => {
  // 重置表单
  Object.assign(addForm, {
    username: '',
    password: '',
    realName: '',
    gender: 'U',
    email: '',
    phone: '',
    teacherNo: '',
    title: '',
    college: '',
    office: '',
    research: ''
  })
  addDialogVisible.value = true
}

// 提交新增教师
const handleSubmitAdd = async () => {
  if (!addFormRef.value) return
  
  try {
    await addFormRef.value.validate()
    addLoading.value = true
    
    await createTeacher(addForm)
    ElMessage.success('教师账号创建成功')
    addDialogVisible.value = false
    loadData()  // 刷新列表
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('创建教师失败:', error)
      ElMessage.error(error.response?.data?.message || '创建失败，请重试')
    }
  } finally {
    addLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.teachers-page {
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

.teacher-detail {
  :deep(.el-descriptions__label) {
    font-weight: $font-weight-semibold;
  }
}

.add-teacher-form {
  :deep(.el-divider__text) {
    font-weight: $font-weight-semibold;
    color: $text-secondary;
  }
}

@media (max-width: $breakpoint-md) {
  .teachers-page {
    padding: $spacing-md;
  }
}
</style>
