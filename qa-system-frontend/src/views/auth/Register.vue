<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>师生答疑系统</h2>
        <p>创建新账号</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-width="80px"
      >
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="registerForm.role" size="large">
            <el-radio value="STUDENT">学生</el-radio>
            <el-radio value="TEACHER">教师</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="4-20位字母、数字或下划线"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="6-20位，包含字母和数字"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号（可选）"
            clearable
          />
        </el-form-item>

        <!-- 学生信息 -->
        <template v-if="registerForm.role === 'STUDENT'">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="registerForm.studentNo" placeholder="请输入学号" clearable />
          </el-form-item>

          <el-form-item label="学院" prop="college">
            <el-input v-model="registerForm.college" placeholder="请输入学院" clearable />
          </el-form-item>

          <el-form-item label="专业" prop="major">
            <el-input v-model="registerForm.major" placeholder="请输入专业" clearable />
          </el-form-item>

          <el-form-item label="班级" prop="className">
            <el-input v-model="registerForm.className" placeholder="请输入班级" clearable />
          </el-form-item>

          <el-form-item label="年级" prop="grade">
            <el-input-number
              v-model="registerForm.grade"
              :min="2020"
              :max="2030"
              placeholder="请选择年级"
              style="width: 100%"
            />
          </el-form-item>
        </template>

        <!-- 教师信息 -->
        <template v-if="registerForm.role === 'TEACHER'">
          <el-form-item label="工号" prop="teacherNo">
            <el-input v-model="registerForm.teacherNo" placeholder="请输入工号" clearable />
          </el-form-item>

          <el-form-item label="学院" prop="college">
            <el-input v-model="registerForm.college" placeholder="请输入学院" clearable />
          </el-form-item>

          <el-form-item label="职称" prop="title">
            <el-input v-model="registerForm.title" placeholder="请输入职称" clearable />
          </el-form-item>

          <el-form-item label="研究方向" prop="research">
            <el-input
              v-model="registerForm.research"
              type="textarea"
              placeholder="请输入研究方向"
              :rows="3"
            />
          </el-form-item>

          <el-form-item label="办公室" prop="office">
            <el-input v-model="registerForm.office" placeholder="请输入办公室位置" clearable />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <router-link to="/login" class="login-link">
            已有账号？立即登录
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 100%;
  max-width: 600px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-height: 90vh;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    margin: 0;
    font-size: 28px;
    color: #333;
  }

  p {
    margin: 10px 0 0;
    color: #666;
    font-size: 14px;
  }
}

.register-footer {
  text-align: center;
  margin-top: 20px;

  .login-link {
    color: #409eff;
    text-decoration: none;
    font-size: 14px;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>


