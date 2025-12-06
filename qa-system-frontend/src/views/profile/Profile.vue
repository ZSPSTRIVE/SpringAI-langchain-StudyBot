<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <PageHeader
      title="个人中心"
      description="管理您的个人信息和账号设置"
      :show-back="true"
      :breadcrumbs="[{ label: '个人中心', to: '/profile' }]"
    />
    
    <div class="profile-content">
      <el-row :gutter="24">
        <!-- 左侧导航 -->
        <el-col :xs="24" :sm="24" :md="6" :lg="5">
          <div class="profile-nav">
            <div class="profile-header">
              <el-avatar 
                :size="80" 
                :src="profileForm.avatar"
                class="profile-avatar"
              >
                {{ profileForm.realName?.[0] || 'U' }}
              </el-avatar>
              <h3 class="profile-name">{{ profileForm.realName || '用户' }}</h3>
              <el-tag :type="getRoleType(profileForm.role)" class="profile-role">
                {{ getRoleText(profileForm.role) }}
              </el-tag>
            </div>
            
            <el-menu 
              :default-active="activeTab" 
              @select="handleTabSelect"
              class="nav-menu"
            >
              <el-menu-item index="basic">
                <el-icon><User /></el-icon>
                <span>基本信息</span>
              </el-menu-item>
              <el-menu-item index="password">
                <el-icon><Lock /></el-icon>
                <span>修改密码</span>
              </el-menu-item>
              <el-menu-item index="stats">
                <el-icon><TrendCharts /></el-icon>
                <span>我的统计</span>
              </el-menu-item>
            </el-menu>
          </div>
        </el-col>

        <!-- 右侧内容 -->
        <el-col :xs="24" :sm="24" :md="18" :lg="19">
          <!-- 基本信息 -->
          <div v-show="activeTab === 'basic'" class="profile-section">
            <el-card class="profile-card">
              <template #header>
                <div class="card-header">
                  <el-icon><User /></el-icon>
                  <span>基本信息</span>
                </div>
              </template>

              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="120px"
                class="profile-form"
              >
                <!-- 头像上传 -->
                <el-form-item label="头像">
                  <ImageUpload
                    v-model="profileForm.avatar"
                    :width="120"
                    :height="120"
                    :max-size="2"
                    tips="建议尺寸 200x200，支持 JPG、PNG 格式，大小不超过 2MB"
                    @success="handleAvatarSuccess"
                  />
                </el-form-item>
                
                <el-divider />

            <!-- 基本信息 -->
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>

            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>

            <el-form-item label="角色">
              <el-tag v-if="profileForm.role === 'STUDENT'" type="primary">学生</el-tag>
              <el-tag v-else-if="profileForm.role === 'TEACHER'" type="success">教师</el-tag>
              <el-tag v-else-if="profileForm.role === 'ADMIN'" type="danger">管理员</el-tag>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio value="M">男</el-radio>
                <el-radio value="F">女</el-radio>
                <el-radio value="U">保密</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 学生特定信息 -->
            <template v-if="profileForm.role === 'STUDENT'">
              <el-divider content-position="left">学生信息</el-divider>
              
              <el-form-item label="学号">
                <el-input v-model="profileForm.studentNo" disabled />
              </el-form-item>

              <el-form-item label="学院">
                <el-input v-model="profileForm.college" placeholder="请输入学院" />
              </el-form-item>

              <el-form-item label="专业">
                <el-input v-model="profileForm.major" placeholder="请输入专业" />
              </el-form-item>

              <el-form-item label="班级">
                <el-input v-model="profileForm.className" placeholder="请输入班级" />
              </el-form-item>

              <el-form-item label="年级">
                <el-input-number
                  v-model="profileForm.grade"
                  :min="2020"
                  :max="2030"
                  style="width: 100%"
                />
              </el-form-item>
            </template>

            <!-- 教师特定信息 -->
            <template v-if="profileForm.role === 'TEACHER'">
              <el-divider content-position="left">教师信息</el-divider>
              
              <el-form-item label="工号">
                <el-input v-model="profileForm.teacherNo" disabled />
              </el-form-item>

              <el-form-item label="学院">
                <el-input v-model="profileForm.college" placeholder="请输入学院" />
              </el-form-item>

              <el-form-item label="职称">
                <el-input v-model="profileForm.title" placeholder="请输入职称" />
              </el-form-item>

              <el-form-item label="研究方向">
                <el-input
                  v-model="profileForm.research"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入研究方向"
                />
              </el-form-item>

              <el-form-item label="办公室">
                <el-input v-model="profileForm.office" placeholder="请输入办公室位置" />
              </el-form-item>

              <el-form-item label="个人简介">
                <el-input
                  v-model="profileForm.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入个人简介"
                />
              </el-form-item>
            </template>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveProfile">
                保存修改
              </el-button>
              <el-button @click="loadProfile">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

        <!-- 修改密码 -->
        <div v-show="activeTab === 'password'" class="profile-section">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-header">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </div>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            style="max-width: 500px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">
                确认修改
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>

            <!-- 密码强度提示 -->
            <el-alert
              type="info"
              :closable="false"
              show-icon
              class="password-tip"
            >
              <template #title>
                <div class="tip-content">
                  <strong>密码要求：</strong>
                  <ul>
                    <li>长度6-20位</li>
                    <li>必须包含字母和数字</li>
                    <li>可包含特殊字符 @$!%*#?&</li>
                  </ul>
                </div>
              </template>
            </el-alert>
          </el-form>
        </el-card>
        </div>

        <!-- 统计信息 -->
        <div v-show="activeTab === 'stats'" class="profile-section">
          <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(stat, index) in statsData" :key="index">
              <div class="stat-card" :class="stat.iconClass" :style="{ animationDelay: index * 0.1 + 's' }">
                <div class="stat-icon">
                  <el-icon :size="36">
                    <component :is="getIconComponent(stat.icon)" />
                  </el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                </div>
              </div>
            </el-col>
          </el-row>

          <!-- 最近活动 -->
          <el-card shadow="never" class="profile-card activity-card">
            <template #header>
              <div class="card-header">
                <el-icon><Clock /></el-icon>
                <span>最近活动</span>
              </div>
            </template>
            <div v-if="recentActivities.length > 0">
              <el-timeline>
                <el-timeline-item
                  v-for="(activity, index) in recentActivities"
                  :key="index"
                  :timestamp="activity.time"
                  placement="top"
                  :type="activity.type"
                >
                  <div class="activity-item">
                    <h4>
                      <el-icon><component :is="getIconComponent(activity.icon)" /></el-icon>
                      {{ activity.title }}
                    </h4>
                    <p>{{ activity.description }}</p>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
            <el-empty v-else description="暂无活动记录" />
          </el-card>
        </div>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Lock, 
  Clock,
  Document,
  ChatDotRound,
  Star,
  View,
  Trophy,
  Tickets,
  TrendCharts
} from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import { getCurrentUserProfile, updateProfile, changePassword } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 角色类型映射
const getRoleType = (role) => {
  const typeMap = {
    'STUDENT': 'primary',
    'TEACHER': 'success',
    'ADMIN': 'warning'
  }
  return typeMap[role] || 'info'
}

// 角色文字映射
const getRoleText = (role) => {
  const textMap = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'ADMIN': '管理员'
  }
  return textMap[role] || '未知'
}

// 头像上传成功回调
const handleAvatarSuccess = (data) => {
  ElMessage.success('头像上传成功')
  profileForm.avatar = data.url
}

const activeTab = ref('basic')
const profileFormRef = ref()
const passwordFormRef = ref()
const saving = ref(false)
const changingPassword = ref(false)

const profileForm = reactive({
  userId: null,
  username: '',
  realName: '',
  role: '',
  email: '',
  phone: '',
  avatar: '',
  gender: 'U',
  // 学生
  studentNo: '',
  major: '',
  className: '',
  grade: null,
  college: '',
  // 教师
  teacherNo: '',
  title: '',
  research: '',
  office: '',
  bio: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const validatePassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { 
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,20}$/, 
      message: '密码必须包含字母和数字，长度6-20位', 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ]
}

const handleTabSelect = (key) => {
  activeTab.value = key
}

const loadProfile = async () => {
  try {
    const res = await getCurrentUserProfile()
    Object.assign(profileForm, res.data)
  } catch (error) {
    console.error('加载个人信息失败:', error)
  }
}

const handleSaveProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const res = await updateProfile(profileForm)
        ElMessage.success('保存成功')
        
        // 更新本地用户信息
        userStore.setUserInfo({
          ...userStore.userInfo,
          realName: res.data.realName,
          email: res.data.email,
          avatar: res.data.avatar
        })
        
        Object.assign(profileForm, res.data)
      } catch (error) {
        console.error('保存失败:', error)
      } finally {
        saving.value = false
      }
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        await changePassword(passwordForm)
        ElMessage.success('密码修改成功，请重新登录')
        
        // 清空表单
        resetPasswordForm()
        
        // 延迟登出
        setTimeout(() => {
          userStore.logout()
          window.location.href = '/login'
        }, 1500)
      } catch (error) {
        console.error('密码修改失败:', error)
      } finally {
        changingPassword.value = false
      }
    }
  })
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}

// 图标组件映射
const iconComponents = {
  Document,
  ChatDotRound,
  Star,
  View,
  Trophy,
  Tickets,
  Clock,
  User,
  Lock,
  TrendCharts
}

// 获取图标组件
const getIconComponent = (name) => {
  return iconComponents[name] || Document
}

// 统计数据
const statsData = computed(() => {
  const role = profileForm.role
  
  if (role === 'STUDENT') {
    return [
      { icon: 'Document', label: '提问数', value: Math.floor(Math.random() * 50) + 10, iconClass: 'icon-primary' },
      { icon: 'ChatDotRound', label: '回复数', value: Math.floor(Math.random() * 100) + 20, iconClass: 'icon-success' },
      { icon: 'Star', label: '收藏数', value: Math.floor(Math.random() * 30) + 5, iconClass: 'icon-warning' },
      { icon: 'View', label: '浏览量', value: Math.floor(Math.random() * 500) + 100, iconClass: 'icon-info' }
    ]
  } else if (role === 'TEACHER') {
    return [
      { icon: 'ChatDotRound', label: '回答数', value: Math.floor(Math.random() * 80) + 20, iconClass: 'icon-success' },
      { icon: 'Trophy', label: '被采纳', value: Math.floor(Math.random() * 50) + 10, iconClass: 'icon-warning' },
      { icon: 'Star', label: '获赞数', value: Math.floor(Math.random() * 200) + 50, iconClass: 'icon-danger' },
      { icon: 'Tickets', label: '关注者', value: Math.floor(Math.random() * 100) + 20, iconClass: 'icon-primary' }
    ]
  }
  
  return []
})

// 最近活动
const recentActivities = ref([
  {
    title: '发布了新问题',
    description: 'Java多线程编程的最佳实践是什么？',
    time: '2025-11-12 10:30',
    type: 'primary',
    icon: 'Document'
  },
  {
    title: '收藏了一个问题',
    description: '如何优化数据库查询性能',
    time: '2025-11-11 16:20',
    type: 'warning',
    icon: 'Star'
  },
  {
    title: '获得一个回答',
    description: '你的问题「Spring Boot配置」收到了新回答',
    time: '2025-11-11 14:15',
    type: 'success',
    icon: 'ChatDotRound'
  }
])

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.profile-page {
  padding: $spacing-xl;
  animation: fadeIn 0.4s ease;
}

.profile-content {
  margin-top: $spacing-xl;
}

// ==================== 左侧导航 ====================
.profile-nav {
  background: $bg-primary;
  border-radius: $radius-lg;
  box-shadow: $shadow-md;
  overflow: hidden;
  position: sticky;
  top: 80px;
}

.profile-header {
  padding: $spacing-xl;
  text-align: center;
  border-bottom: 1px solid $border-color;
  background: $bg-secondary;
  
  .profile-avatar {
    margin: 0 auto $spacing-md;
    box-shadow: $shadow-lg;
    border: 3px solid white;
    transition: all $transition-base;
    
    &:hover {
      transform: scale(1.05);
      box-shadow: $shadow-primary;
    }
  }
  
  .profile-name {
    margin: 0 0 $spacing-sm;
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }
  
  .profile-role {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
  }
}

.nav-menu {
  border: none;
  padding: $spacing-sm 0;
  
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    margin: $spacing-xs $spacing-md;
    border-radius: $radius-md;
    transition: all $transition-fast;
    
    &:hover {
      background: $bg-hover;
    }
    
    &.is-active {
      background: rgba($primary-color, 0.1);
      color: $primary-color;
      font-weight: $font-weight-semibold;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 60%;
        background: $primary-color;
        border-radius: 0 $radius-md $radius-md 0;
      }
    }
    
    .el-icon {
      margin-right: $spacing-sm;
    }
  }
}

// ==================== 右侧内容 ====================
.profile-section {
  animation: slideInRight 0.3s ease;
}

.profile-card {
  margin-bottom: $spacing-lg;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $text-primary;
    
    .el-icon {
      color: $primary-color;
    }
  }
}

.profile-form {
  :deep(.el-form-item__label) {
    font-weight: $font-weight-medium;
    color: $text-primary;
  }
  
  :deep(.el-input),
  :deep(.el-textarea) {
    .el-input__wrapper {
      transition: all $transition-fast;
      
      &:hover {
        border-color: $primary-light;
      }
    }
  }
  
  :deep(.el-divider) {
    margin: $spacing-xl 0;
  }
}

// ==================== 密码提示 ====================
.password-tip {
  margin-top: $spacing-lg;
  background: rgba($info-color, 0.05) !important;
  border: 1px solid rgba($info-color, 0.2);
  
  .tip-content {
    ul {
      margin: $spacing-sm 0 0;
      padding-left: $spacing-lg;
      list-style: disc;
      
      li {
        margin: $spacing-xs 0;
        color: $text-secondary;
      }
    }
  }
}

// ==================== 统计卡片 ====================
.stats-row {
  margin-bottom: $spacing-2xl;
}

.stat-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: $spacing-xl;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  box-shadow: $shadow-md;
  border: 1px solid $border-color;
  transition: all $transition-base;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  animation: slideInUp 0.5s ease both;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: $primary-color;
    transform: scaleX(0);
    transform-origin: left;
    transition: transform $transition-base;
  }
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: $shadow-xl;
    
    &::before {
      transform: scaleX(1);
    }
    
    .stat-icon {
      transform: scale(1.1) rotate(10deg);
    }
  }
  
  .stat-icon {
    width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-xl;
    flex-shrink: 0;
    transition: all $transition-base;
  }
  
  .stat-content {
    flex: 1;
    
    .stat-value {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      margin-bottom: $spacing-xs;
      background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .stat-label {
      font-size: $font-size-sm;
      color: $text-secondary;
      font-weight: $font-weight-medium;
    }
  }
  
  &.icon-primary {
    .stat-icon {
      background: rgba($primary-color, 0.1);
      color: $primary-color;
    }
    &:hover {
      border-color: $primary-color;
    }
  }
  
  &.icon-success {
    .stat-icon {
      background: rgba($success-color, 0.1);
      color: $success-color;
    }
    &::before {
      background: $success-color;
    }
    &:hover {
      border-color: $success-color;
    }
  }
  
  &.icon-warning {
    .stat-icon {
      background: rgba($warning-color, 0.1);
      color: $warning-color;
    }
    &::before {
      background: $warning-color;
    }
    &:hover {
      border-color: $warning-color;
    }
  }
  
  &.icon-info {
    .stat-icon {
      background: rgba($info-color, 0.1);
      color: $info-color;
    }
    &::before {
      background: $info-color;
    }
    &:hover {
      border-color: $info-color;
    }
  }
  
  &.icon-danger {
    .stat-icon {
      background: rgba($danger-color, 0.1);
      color: $danger-color;
    }
    &::before {
      background: $danger-color;
    }
    &:hover {
      border-color: $danger-color;
    }
  }
}

// ==================== 活动卡片 ====================
.activity-card {
  animation: slideInUp 0.6s ease both;
  
  :deep(.el-timeline) {
    padding-left: 0;
    
    .el-timeline-item__wrapper {
      padding-left: $spacing-lg;
    }
    
    .el-timeline-item__timestamp {
      color: $text-secondary;
      font-size: $font-size-sm;
    }
  }
  
  .activity-item {
    padding: $spacing-md;
    background: $bg-secondary;
    border-radius: $radius-lg;
    border: 1px solid $border-color;
    transition: all $transition-fast;
    
    &:hover {
      background: $bg-hover;
      transform: translateX(4px);
      box-shadow: $shadow-sm;
    }
    
    h4 {
      margin: 0 0 $spacing-sm;
      font-size: $font-size-md;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      
      .el-icon {
        color: $primary-color;
      }
    }
    
    p {
      margin: 0;
      font-size: $font-size-sm;
      color: $text-secondary;
      line-height: 1.6;
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: $breakpoint-md) {
  .profile-page {
    padding: $spacing-md;
  }
  
  .profile-nav {
    position: static;
    margin-bottom: $spacing-lg;
  }
  
  .profile-form {
    :deep(.el-form-item__label) {
      width: 100px !important;
    }
  }
  
  .stat-card {
    margin-bottom: $spacing-md;
    
    .stat-content .stat-value {
      font-size: $font-size-2xl;
    }
  }
}
</style>
