<template>
  <div class="follows-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的关注（{{ teachers.length }}）</span>
        </div>
      </template>

      <el-skeleton :loading="loading" :rows="5" animated>
        <el-empty v-if="teachers.length === 0" description="暂无关注的教师" />

        <div v-else class="teacher-list">
          <div v-for="teacher in teachers" :key="teacher.userId" class="teacher-item">
            <div class="teacher-info">
              <el-avatar :size="60">{{ teacher.realName?.[0] }}</el-avatar>
              <div class="teacher-detail">
                <div class="teacher-name">
                  {{ teacher.realName }}
                  <el-tag v-if="teacher.title" size="small" type="success">
                    {{ teacher.title }}
                  </el-tag>
                </div>
                <div class="teacher-meta">
                  <span v-if="teacher.college">{{ teacher.college }}</span>
                  <span v-if="teacher.research">{{ teacher.research }}</span>
                </div>
                <div class="teacher-contact">
                  <span v-if="teacher.email">
                    <el-icon><Message /></el-icon>
                    {{ teacher.email }}
                  </span>
                  <span v-if="teacher.office">
                    <el-icon><Location /></el-icon>
                    {{ teacher.office }}
                  </span>
                </div>
              </div>
            </div>
            <div class="teacher-actions">
              <el-button type="danger" plain @click="handleUnfollow(teacher.userId)">
                取消关注
              </el-button>
            </div>
          </div>
        </div>
      </el-skeleton>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, Location } from '@element-plus/icons-vue'
import { getFollowingTeachers, unfollowTeacher } from '@/api/follow'

const loading = ref(false)
const teachers = ref([])

const loadTeachers = async () => {
  loading.value = true
  try {
    const res = await getFollowingTeachers()
    teachers.value = res.data
  } catch (error) {
    console.error('加载关注列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleUnfollow = async (teacherId) => {
  try {
    await ElMessageBox.confirm('确定取消关注吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await unfollowTeacher(teacherId)
    ElMessage.success('取消关注成功')
    loadTeachers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消关注失败:', error)
    }
  }
}

onMounted(() => {
  loadTeachers()
})
</script>

<style scoped lang="scss">
.follows-page {
  padding: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
}

.teacher-list {
  .teacher-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #eee;

    &:last-child {
      border-bottom: none;
    }

    .teacher-info {
      flex: 1;
      display: flex;
      gap: 16px;

      .teacher-detail {
        flex: 1;

        .teacher-name {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 8px;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .teacher-meta {
          font-size: 14px;
          color: #606266;
          margin-bottom: 6px;

          span {
            margin-right: 16px;
          }
        }

        .teacher-contact {
          font-size: 13px;
          color: #909399;
          display: flex;
          gap: 20px;

          span {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
    }

    .teacher-actions {
      margin-left: 20px;
    }
  }
}
</style>


