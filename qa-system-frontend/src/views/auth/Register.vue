<template>
  <div class="register-container">
    <!-- 动态背景 -->
    <div class="neo-background">
      <div class="floating-shape shape-1" />
      <div class="floating-shape shape-2" />
      <div class="floating-shape shape-3" />
      <div class="floating-shape shape-4" />
      <div class="dots-layer" />
    </div>

    <div class="register-box">
      <!-- 顶部装饰条 -->
      <div class="top-bars">
        <div class="bar bar--yellow" />
        <div class="bar bar--blue" />
        <div class="bar bar--pink" />
      </div>

      <div class="register-header">
        <div class="logo-box">
          <el-icon class="logo-icon">
            <School />
          </el-icon>
        </div>
        <h2>注册</h2>
        <p>CREATE YOUR ACCOUNT</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
      >
        <!-- 角色选择 -->
        <div class="role-selector">
          <label class="role-label">选择身份</label>
          <div class="role-options">
            <div 
              class="role-card" 
              :class="{ active: registerForm.role === 'STUDENT' }"
              @click="registerForm.role = 'STUDENT'"
            >
              <el-icon :size="32">
                <Reading />
              </el-icon>
              <span>学生</span>
            </div>
            <div 
              class="role-card" 
              :class="{ active: registerForm.role === 'TEACHER' }"
              @click="registerForm.role = 'TEACHER'"
            >
              <el-icon :size="32">
                <UserFilled />
              </el-icon>
              <span>教师</span>
            </div>
          </div>
        </div>

        <!-- 基础信息 -->
        <div class="form-section">
          <h3 class="section-title">
            <span class="title-icon">01</span>
            基础信息
          </h3>
          
          <div class="form-grid">
            <el-form-item
              label="用户名"
              prop="username"
            >
              <el-input
                v-model="registerForm.username"
                placeholder="4-20位字母、数字或下划线"
                clearable
              />
            </el-form-item>

            <el-form-item
              label="真实姓名"
              prop="realName"
            >
              <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                clearable
              />
            </el-form-item>
          </div>

          <div class="form-grid">
            <el-form-item
              label="密码"
              prop="password"
            >
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="6-20位，包含字母和数字"
                show-password
              />
            </el-form-item>

            <el-form-item
              label="确认密码"
              prop="confirmPassword"
            >
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
              />
            </el-form-item>
          </div>

          <div class="form-grid">
            <el-form-item
              label="邮箱"
              prop="email"
            >
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                clearable
              />
            </el-form-item>

            <el-form-item
              label="手机号"
              prop="phone"
            >
              <el-input
                v-model="registerForm.phone"
                placeholder="可选"
                clearable
              />
            </el-form-item>
          </div>
        </div>

        <!-- 学生/教师信息 -->
        <div class="form-section">
          <h3 class="section-title">
            <span class="title-icon">02</span>
            {{ registerForm.role === 'STUDENT' ? '学生信息' : '教师信息' }}
          </h3>

          <template v-if="registerForm.role === 'STUDENT'">
            <div class="form-grid">
              <el-form-item
                label="学号"
                prop="studentNo"
              >
                <el-input
                  v-model="registerForm.studentNo"
                  placeholder="请输入学号"
                  clearable
                />
              </el-form-item>
              <el-form-item
                label="学院"
                prop="college"
              >
                <el-input
                  v-model="registerForm.college"
                  placeholder="请输入学院"
                  clearable
                />
              </el-form-item>
            </div>
            <div class="form-grid">
              <el-form-item
                label="专业"
                prop="major"
              >
                <el-input
                  v-model="registerForm.major"
                  placeholder="请输入专业"
                  clearable
                />
              </el-form-item>
              <el-form-item
                label="班级"
                prop="className"
              >
                <el-input
                  v-model="registerForm.className"
                  placeholder="请输入班级"
                  clearable
                />
              </el-form-item>
            </div>
            <el-form-item
              label="年级"
              prop="grade"
            >
              <el-input-number
                v-model="registerForm.grade"
                :min="2020"
                :max="2030"
                style="width: 100%"
              />
            </el-form-item>
          </template>

          <template v-if="registerForm.role === 'TEACHER'">
            <div class="form-grid">
              <el-form-item
                label="工号"
                prop="teacherNo"
              >
                <el-input
                  v-model="registerForm.teacherNo"
                  placeholder="请输入工号"
                  clearable
                />
              </el-form-item>
              <el-form-item
                label="学院"
                prop="college"
              >
                <el-input
                  v-model="registerForm.college"
                  placeholder="请输入学院"
                  clearable
                />
              </el-form-item>
            </div>
            <div class="form-grid">
              <el-form-item
                label="职称"
                prop="title"
              >
                <el-input
                  v-model="registerForm.title"
                  placeholder="请输入职称"
                  clearable
                />
              </el-form-item>
              <el-form-item
                label="办公室"
                prop="office"
              >
                <el-input
                  v-model="registerForm.office"
                  placeholder="请输入办公室"
                  clearable
                />
              </el-form-item>
            </div>
            <el-form-item
              label="研究方向"
              prop="research"
            >
              <el-input
                v-model="registerForm.research"
                type="textarea"
                placeholder="请输入研究方向"
                :rows="3"
              />
            </el-form-item>
          </template>
        </div>

        <!-- 提交按钮 -->
        <button
          type="button"
          class="neo-register-btn"
          :disabled="loading"
          @click="handleRegister"
        >
          <span v-if="!loading">立即注册</span>
          <span v-else>注册中...</span>
          <el-icon v-if="!loading">
            <Right />
          </el-icon>
        </button>

        <div class="register-footer">
          <router-link
            to="/login"
            class="login-link"
          >
            <span>已有账号？</span>
            <strong>立即登录 →</strong>
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { School, Reading, UserFilled, Right } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { register as registerApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  role: 'STUDENT',
  email: '',
  phone: '',
  // 学生字段
  studentNo: '',
  major: '',
  className: '',
  grade: new Date().getFullYear(),
  college: '',
  // 教师字段
  teacherNo: '',
  title: '',
  research: '',
  office: ''
})

const validatePassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '用户名只能包含字母、数字、下划线，长度4-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,20}$/, message: '密码必须包含字母和数字，长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...data } = registerForm
        
        console.log('注册请求数据:', data)
        
        const res = await registerApi(data)
        
        console.log('注册响应:', res)
        console.log('注册数据:', res.data)
        
        // 注册成功后自动登录
        userStore.login(res.data)
        
        // 验证保存
        console.log('保存后的token:', userStore.token)
        console.log('保存后的userInfo:', userStore.userInfo)
        
        ElMessage.success('注册成功，正在跳转...')
        
        // 延迟跳转，确保数据保存完成
        setTimeout(() => {
          const targetRoute = res.data.userInfo.role === 'STUDENT' ? '/student' : '/teacher'
          console.log('准备跳转到:', targetRoute)
          router.push(targetRoute)
        }, 100)
      } catch (error) {
        console.error('注册失败:', error)
        const errorMsg = error.response?.data?.message || error.message || '注册失败'
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 40px 20px;
  background: $neo-cream;
  position: relative;
  overflow: hidden;
}

// ==================== 动态背景 ====================
.neo-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  
  .dots-layer {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: radial-gradient(circle, rgba($neo-black, 0.06) 2px, transparent 2px);
    background-size: 24px 24px;
    animation: dotsMove 30s linear infinite;
  }
  
  .floating-shape {
    position: absolute;
    border: 3px solid $neo-black;
    animation: floatShape 8s ease-in-out infinite;
    
    &.shape-1 {
      width: 100px;
      height: 100px;
      background: $neo-yellow;
      top: 10%;
      left: 5%;
      border-radius: 50%;
      box-shadow: 6px 6px 0 0 $neo-black;
    }
    
    &.shape-2 {
      width: 80px;
      height: 80px;
      background: $neo-blue;
      top: 60%;
      right: 10%;
      box-shadow: 5px 5px 0 0 $neo-black;
      animation-delay: 2s;
    }
    
    &.shape-3 {
      width: 60px;
      height: 60px;
      background: $neo-pink;
      bottom: 20%;
      left: 15%;
      border-radius: 50%;
      box-shadow: 4px 4px 0 0 $neo-black;
      animation-delay: 4s;
    }
    
    &.shape-4 {
      width: 70px;
      height: 70px;
      background: $neo-green;
      top: 30%;
      right: 5%;
      box-shadow: 5px 5px 0 0 $neo-black;
      animation-delay: 6s;
    }
  }
}

@keyframes dotsMove {
  0% { background-position: 0 0; }
  100% { background-position: 48px 48px; }
}

@keyframes floatShape {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-15px) rotate(5deg); }
  50% { transform: translateY(-8px) rotate(-3deg); }
  75% { transform: translateY(-20px) rotate(2deg); }
}

// ==================== 注册卡片 ====================
.register-box {
  width: 100%;
  max-width: 680px;
  background: $neo-white;
  border: 4px solid $neo-black;
  border-radius: 20px;
  box-shadow: 10px 10px 0 0 $neo-black;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  z-index: 1;
  
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: $neo-cream;
  }
  
  &::-webkit-scrollbar-thumb {
    background: $neo-yellow;
    border: 2px solid $neo-black;
  }
}

.top-bars {
  display: flex;
  height: 10px;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
  
  .bar {
    flex: 1;
    
    &--yellow { background: $neo-yellow; }
    &--blue { background: $neo-blue; }
    &--pink { background: $neo-pink; }
  }
}

.register-header {
  text-align: center;
  padding: 32px 40px 24px;
  
  .logo-box {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 72px;
    height: 72px;
    background: $neo-yellow;
    border: 3px solid $neo-black;
    border-radius: 16px;
    box-shadow: 5px 5px 0 0 $neo-black;
    margin-bottom: 20px;
    
    .logo-icon {
      font-size: 40px;
      color: $neo-black;
    }
  }

  h2 {
    margin: 0;
    font-size: 42px;
    font-weight: 900;
    color: $neo-black;
    text-transform: uppercase;
    letter-spacing: 4px;
  }

  p {
    margin: 8px 0 0;
    font-size: 12px;
    font-weight: 700;
    color: $neo-black;
    opacity: 0.5;
    letter-spacing: 2px;
  }
}

// ==================== 角色选择 ====================
.role-selector {
  padding: 0 40px;
  margin-bottom: 24px;
  
  .role-label {
    display: block;
    font-size: 12px;
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 1px;
    color: $neo-black;
    margin-bottom: 12px;
  }
  
  .role-options {
    display: flex;
    gap: 16px;
  }
  
  .role-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
    background: $neo-white;
    border: 3px solid $neo-black;
    border-radius: 12px;
    box-shadow: 4px 4px 0 0 $neo-black;
    cursor: pointer;
    transition: all 150ms cubic-bezier(0.34, 1.56, 0.64, 1);
    
    span {
      font-size: 14px;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    
    &:hover {
      transform: translate(-2px, -2px);
      box-shadow: 6px 6px 0 0 $neo-black;
    }
    
    &.active {
      background: $neo-blue;
      color: $neo-white;
      
      &:first-child {
        background: $neo-purple;
      }
    }
  }
}

// ==================== 表单区块 ====================
.form-section {
  padding: 0 40px;
  margin-bottom: 24px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 16px;
    font-weight: 800;
    color: $neo-black;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin: 0 0 20px 0;
    padding-bottom: 12px;
    border-bottom: 3px solid $neo-black;
    
    .title-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      background: $neo-yellow;
      border: 2px solid $neo-black;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 900;
      box-shadow: 2px 2px 0 0 $neo-black;
    }
  }
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 11px;
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: $neo-black;
    padding-bottom: 6px;
  }
}

// ==================== 提交按钮 ====================
.neo-register-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: calc(100% - 80px);
  margin: 32px 40px 0;
  padding: 18px;
  font-size: 18px;
  font-weight: 900;
  font-family: inherit;
  text-transform: uppercase;
  letter-spacing: 2px;
  background: $neo-green;
  color: $neo-black;
  border: 4px solid $neo-black;
  border-radius: 12px;
  box-shadow: 6px 6px 0 0 $neo-black;
  cursor: pointer;
  transition: all 150ms cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &:hover:not(:disabled) {
    transform: translate(-3px, -3px);
    box-shadow: 9px 9px 0 0 $neo-black;
    background: $neo-green-dark;
  }
  
  &:active:not(:disabled) {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0 0 $neo-black;
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

// ==================== 页脚 ====================
.register-footer {
  text-align: center;
  padding: 24px 40px 32px;

  .login-link {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    background: $neo-yellow;
    border: 3px solid $neo-black;
    border-radius: 8px;
    box-shadow: 4px 4px 0 0 $neo-black;
    font-size: 14px;
    color: $neo-black;
    text-decoration: none;
    transition: all 150ms cubic-bezier(0.34, 1.56, 0.64, 1);
    
    span {
      font-weight: 500;
    }
    
    strong {
      font-weight: 800;
    }

    &:hover {
      transform: translate(-2px, -2px);
      box-shadow: 6px 6px 0 0 $neo-black;
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 640px) {
  .register-container {
    padding: 20px;
  }
  
  .register-box {
    border-radius: 16px;
    box-shadow: 6px 6px 0 0 $neo-black;
  }
  
  .register-header {
    padding: 24px 24px 20px;
    
    h2 {
      font-size: 32px;
    }
  }
  
  .role-selector,
  .form-section {
    padding: 0 24px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .neo-register-btn {
    width: calc(100% - 48px);
    margin: 24px 24px 0;
  }
  
  .register-footer {
    padding: 20px 24px 24px;
  }
}
</style>


