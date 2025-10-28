# 学生管理页面 - 新功能实现说明

## ✅ 已完成的后端接口

### 1. 创建学生账号
```
POST /api/v1/admin/students
Body: {
  "username": "string",      // 必填
  "password": "string",      // 必填  
  "realName": "string",      // 必填
  "email": "string",        // 可选，邮箱格式
  "phone": "string",        // 可选
  "gender": "M/F/U",        // 可选
  "studentNo": "string",    // 学号
  "major": "string",        // 专业
  "className": "string",    // 班级
  "grade": integer,         // 年级
  "college": "string"       // 学院
}
```

### 2. 重置密码（手动输入）
```
PUT /api/v1/admin/users/{userId}/reset-password
Body: {
  "newPassword": "string"  // 必填，6-20位
}
```

## 📋 前端需要添加的功能

### Students.vue 需要添加：

1. **添加学生按钮** ✅ 已添加到PageHeader
2. **创建学生弹窗** - 需添加
3. **重置密码弹窗** - 需修改

### 完整代码片段（需要添加到Students.vue）

#### 在 `<script setup>` 部分添加：

```javascript
import { Plus } from '@element-plus/icons-vue'
import { createStudent } from '@/api/admin'

//添加/编辑弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
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
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 重置密码弹窗
const resetPasswordVisible = ref(false)
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

// 添加学生
const handleAdd = () => {
  isEdit.value = false
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
        ElMessage.error('创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置密码（修改）
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
        ElMessage.error('重置密码失败')
      }
    }
  })
}
```

#### 在模板末尾（表格后）添加弹窗：

```vue
<!-- 添加/编辑学生弹窗 -->
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
```

## 🔧 快速实施步骤

1. 打开 `Students.vue`
2. 在 `<script setup>` 部分：
   - 导入 `Plus` 图标和 `createStudent` API
   - 添加上述所有响应式变量和方法
3. 在模板末尾（`</el-card>` 之后，`</div>` 之前）添加两个弹窗
4. 保存文件，前端会自动热更新

## 同样的操作应用到 Teachers.vue

Teachers.vue也需要类似的修改，只需替换字段即可。

希望这能帮助您快速完成功能！

