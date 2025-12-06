<template>
  <div class="ask-question-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我要提问</span>
        </div>
      </template>

      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionRules"
        label-width="100px"
        style="max-width: 900px"
      >
        <el-form-item label="科目" prop="subjectId">
          <el-select v-model="questionForm.subjectId" placeholder="请选择科目" style="width: 100%">
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="问题标题" prop="title">
          <el-input
            v-model="questionForm.title"
            placeholder="请输入问题标题（简明扼要）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="问题描述" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            placeholder="请详细描述你的问题..."
            :rows="10"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="图片上传">
          <div class="image-upload-area">
            <div v-for="(image, index) in questionForm.images" :key="index" class="image-item">
              <el-image :src="image" fit="cover" style="width: 100px; height: 100px" />
              <el-icon class="remove-icon" @click="removeImage(index)">
                <CircleClose />
              </el-icon>
            </div>
            <div v-if="questionForm.images.length < 5" class="upload-btn" @click="handleUploadClick">
              <el-icon :size="30"><Plus /></el-icon>
              <div class="upload-text">上传图片</div>
            </div>
          </div>
          <div class="upload-tip">支持jpg、png格式，最多5张，每张不超过2MB</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交问题
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 隐藏的文件上传输入 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/jpeg,image/png"
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, CircleClose } from '@element-plus/icons-vue'
import { getAllSubjects } from '@/api/subject'
import { createQuestion } from '@/api/question'

const router = useRouter()

const questionFormRef = ref()
const fileInputRef = ref()
const submitting = ref(false)
const subjects = ref([])

const questionForm = reactive({
  subjectId: null,
  title: '',
  content: '',
  images: []
})

const questionRules = {
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在5-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度在10-2000个字符之间', trigger: 'blur' }
  ]
}

const loadSubjects = async () => {
  try {
    const res = await getAllSubjects()
    subjects.value = res.data
  } catch (error) {
    console.error('加载科目失败:', error)
  }
}

const handleUploadClick = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    ElMessage.error('只支持jpg、png格式的图片')
    return
  }

  // 验证文件大小
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  // 预览图片（本地预览，实际上传需要COS）
  const reader = new FileReader()
  reader.onload = (e) => {
    questionForm.images.push(e.target.result)
  }
  reader.readAsDataURL(file)

  // 清空input值，允许重复选择同一文件
  event.target.value = ''
}

const removeImage = (index) => {
  questionForm.images.splice(index, 1)
}

const handleSubmit = async () => {
  if (!questionFormRef.value) return

  await questionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createQuestion(questionForm)
        ElMessage.success('提问成功')
        router.push('/student/questions')
      } catch (error) {
        console.error('提问失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleCancel = () => {
  router.back()
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped lang="scss">
.ask-question-page {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .image-item {
    position: relative;
    width: 100px;
    height: 100px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;

    .remove-icon {
      position: absolute;
      top: 2px;
      right: 2px;
      font-size: 20px;
      color: #f56c6c;
      background: white;
      border-radius: 50%;
      cursor: pointer;

      &:hover {
        color: #f56c6c;
        transform: scale(1.1);
      }
    }
  }

  .upload-btn {
    width: 100px;
    height: 100px;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      color: #409eff;
    }

    .upload-text {
      margin-top: 5px;
      font-size: 12px;
    }
  }
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>


