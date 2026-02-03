<template>
  <div class="dashboard-page">
    <PageHeader
      title="数据统计"
      description="查看系统整体运营数据"
      :show-back="false"
      :show-home="true"
    />

    <!-- 统计卡片 -->
    <el-row
      :gutter="24"
      class="stats-row"
    >
      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <div class="stat-card user-card">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{ statistics.totalUsers || 0 }}
            </div>
            <div class="stat-label">
              总用户数
            </div>
          </div>
        </div>
      </el-col>

      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <div class="stat-card question-card">
          <div class="stat-icon">
            <el-icon><QuestionFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{ statistics.totalQuestions || 0 }}
            </div>
            <div class="stat-label">
              问题总数
            </div>
          </div>
        </div>
      </el-col>

      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <div class="stat-card answer-card">
          <div class="stat-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{ statistics.totalAnswers || 0 }}
            </div>
            <div class="stat-label">
              回答总数
            </div>
          </div>
        </div>
      </el-col>

      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <div class="stat-card resolved-card">
          <div class="stat-icon">
            <el-icon><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">
              {{ statistics.resolvedQuestions || 0 }}
            </div>
            <div class="stat-label">
              已解决问题
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 详细统计 -->
    <el-row
      :gutter="24"
      class="detail-row"
    >
      <el-col
        :xs="24"
        :md="12"
      >
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>用户分布</span>
            </div>
          </template>
          
          <div class="user-distribution">
            <div class="distribution-item">
              <div class="distribution-label">
                <el-icon class="student-icon">
                  <Reading />
                </el-icon>
                <span>学生用户</span>
              </div>
              <div class="distribution-value">
                {{ statistics.studentCount || 0 }}
              </div>
            </div>
            <el-divider />
            <div class="distribution-item">
              <div class="distribution-label">
                <el-icon class="teacher-icon">
                  <User />
                </el-icon>
                <span>教师用户</span>
              </div>
              <div class="distribution-value">
                {{ statistics.teacherCount || 0 }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col
        :xs="24"
        :md="12"
      >
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>问题状态</span>
            </div>
          </template>
          
          <div class="question-status">
            <div class="status-item">
              <div class="status-label">
                <el-icon class="pending-icon">
                  <Clock />
                </el-icon>
                <span>待解决</span>
              </div>
              <div class="status-value">
                {{ statistics.pendingQuestions || 0 }}
              </div>
            </div>
            <el-divider />
            <div class="status-item">
              <div class="status-label">
                <el-icon class="resolved-icon">
                  <CircleCheck />
                </el-icon>
                <span>已解决</span>
              </div>
              <div class="status-value">
                {{ statistics.resolvedQuestions || 0 }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作 -->
    <el-card class="action-card">
      <template #header>
        <div class="card-header">
          <el-icon><Operation /></el-icon>
          <span>快速操作</span>
        </div>
      </template>
      
      <div class="quick-actions">
        <el-button 
          type="primary" 
          :icon="User" 
          @click="$router.push('/admin/students')"
        >
          学生管理
        </el-button>
        <el-button 
          type="success" 
          :icon="UserFilled" 
          @click="$router.push('/admin/teachers')"
        >
          教师管理
        </el-button>
        <el-button 
          type="warning" 
          :icon="Reading" 
          @click="$router.push('/admin/subjects')"
        >
          科目管理
        </el-button>
        <el-button 
          type="info" 
          :icon="QuestionFilled" 
          @click="$router.push('/admin/questions')"
        >
          问题管理
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, UserFilled, QuestionFilled, ChatDotRound, SuccessFilled,
  TrendCharts, Reading, DataAnalysis, Clock, CircleCheck, Operation
} from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { getStatistics } from '@/api/admin'

const statistics = ref({})
const loading = ref(false)

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getStatistics()
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.dashboard-page {
  padding: $spacing-xl;
  animation: fadeIn 0.4s ease;
}

// ==================== 统计卡片 ====================
.stats-row {
  margin-top: $spacing-xl;
}

.stat-card {
  background: $bg-primary;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  box-shadow: $shadow-md;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  transition: all $transition-base;
  cursor: pointer;
  animation: slideInRight 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-xl;
  }
  
  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: $radius-xl;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-3xl;
    flex-shrink: 0;
  }
  
  .stat-content {
    flex: 1;
    
    .stat-value {
      font-size: $font-size-4xl;
      font-weight: $font-weight-bold;
      line-height: 1;
      margin-bottom: $spacing-xs;
    }
    
    .stat-label {
      font-size: $font-size-md;
      color: $text-secondary;
    }
  }
  
  &.user-card {
    .stat-icon {
      background: rgba($primary-color, 0.1);
      color: $primary-color;
    }
    .stat-value {
      color: $primary-color;
    }
  }
  
  &.question-card {
    .stat-icon {
      background: rgba($warning-color, 0.1);
      color: $warning-color;
    }
    .stat-value {
      color: $warning-color;
    }
  }
  
  &.answer-card {
    .stat-icon {
      background: rgba($info-color, 0.1);
      color: $info-color;
    }
    .stat-value {
      color: $info-color;
    }
  }
  
  &.resolved-card {
    .stat-icon {
      background: rgba($success-color, 0.1);
      color: $success-color;
    }
    .stat-value {
      color: $success-color;
    }
  }
}

// ==================== 详细统计 ====================
.detail-row {
  margin-top: $spacing-xl;
}

.detail-card {
  animation: slideInLeft 0.3s ease;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    
    .el-icon {
      color: $primary-color;
    }
  }
}

.user-distribution,
.question-status {
  .distribution-item,
  .status-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-lg 0;
    
    .distribution-label,
    .status-label {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      font-size: $font-size-md;
      color: $text-primary;
      
      .el-icon {
        font-size: $font-size-2xl;
      }
      
      .student-icon {
        color: $student-color;
      }
      
      .teacher-icon {
        color: $teacher-color;
      }
      
      .pending-icon {
        color: $warning-color;
      }
      
      .resolved-icon {
        color: $success-color;
      }
    }
    
    .distribution-value,
    .status-value {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }
  }
  
  .el-divider {
    margin: 0;
  }
}

// ==================== 快速操作 ====================
.action-card {
  margin-top: $spacing-xl;
  animation: fadeIn 0.5s ease;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    
    .el-icon {
      color: $primary-color;
    }
  }
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  
  .el-button {
    flex: 1;
    min-width: 150px;
    height: 48px;
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
  }
}

// ==================== 响应式 ====================
@media (max-width: $breakpoint-md) {
  .dashboard-page {
    padding: $spacing-md;
  }
  
  .stat-card {
    padding: $spacing-md;
    
    .stat-icon {
      width: 48px;
      height: 48px;
      font-size: $font-size-2xl;
    }
    
    .stat-content .stat-value {
      font-size: $font-size-3xl;
    }
  }
  
  .quick-actions .el-button {
    min-width: 120px;
  }
}
</style>
