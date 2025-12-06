import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: false }
    },
    // 学生端路由
    {
      path: '/student',
      component: () => import('@/layouts/StudentLayout.vue'),
      meta: { requiresAuth: true, roles: ['STUDENT'] },
      children: [
        {
          path: '',
          redirect: '/student/questions'
        },
        {
          path: 'questions',
          name: 'StudentQuestions',
          component: () => import('@/views/student/Questions.vue')
        },
        {
          path: 'ask',
          name: 'AskQuestion',
          component: () => import('@/views/student/AskQuestion.vue')
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: () => import('@/views/profile/Profile.vue')
        },
        {
          path: 'follows',
          name: 'StudentFollows',
          component: () => import('@/views/student/Follows.vue')
        },
        {
          path: 'collections',
          name: 'StudentCollections',
          component: () => import('@/views/student/Collections.vue')
        },
        {
          path: 'ai-assistant',
          name: 'StudentAiAssistant',
          component: () => import('@/views/common/AiAssistant.vue')
        },
        {
          path: 'doc-studio',
          name: 'StudentDocStudio',
          component: () => import('@/views/common/DocStudio.vue')
        }
      ]
    },
    // 教师端路由
    {
      path: '/teacher',
      component: () => import('@/layouts/TeacherLayout.vue'),
      meta: { requiresAuth: true, roles: ['TEACHER'] },
      children: [
        {
          path: '',
          redirect: '/teacher/questions'
        },
        {
          path: 'questions',
          name: 'TeacherQuestions',
          component: () => import('@/views/student/Questions.vue')  // 复用学生的问题广场组件
        },
        {
          path: 'answers',
          name: 'TeacherAnswers',
          component: () => import('@/views/teacher/Answers.vue')
        },
        {
          path: 'profile',
          name: 'TeacherProfile',
          component: () => import('@/views/profile/Profile.vue')
        },
        {
          path: 'ai-assistant',
          name: 'TeacherAiAssistant',
          component: () => import('@/views/common/AiAssistant.vue')
        },
        {
          path: 'doc-studio',
          name: 'TeacherDocStudio',
          component: () => import('@/views/common/DocStudio.vue')
        }
      ]
    },
    // 管理员路由
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: 'students',
          name: 'AdminStudents',
          component: () => import('@/views/admin/Students.vue')
        },
        {
          path: 'teachers',
          name: 'AdminTeachers',
          component: () => import('@/views/admin/Teachers.vue')
        },
        {
          path: 'subjects',
          name: 'AdminSubjects',
          component: () => import('@/views/admin/Subjects.vue')
        },
        {
          path: 'questions',
          name: 'AdminQuestions',
          component: () => import('@/views/admin/Questions.vue')
        },
        {
          path: 'documents',
          name: 'AdminDocDocuments',
          component: () => import('@/views/admin/DocDocuments.vue')
        },
        {
          path: 'doc-config',
          name: 'AdminDocConfig',
          component: () => import('@/views/admin/DocConfig.vue')
        },
        {
          path: 'doc-sensitive-words',
          name: 'AdminDocSensitiveWords',
          component: () => import('@/views/admin/DocSensitiveWords.vue')
        },
        {
          path: 'doc-operation-logs',
          name: 'AdminDocOperationLogs',
          component: () => import('@/views/admin/DocOperationLogs.vue')
        }
      ]
    },
    // 公共路由
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/profile/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/questions/:id',
      name: 'QuestionDetail',
      component: () => import('@/views/common/QuestionDetail.vue')
    },
    {
      path: '/forum',
      name: 'Forum',
      component: () => import('@/views/forum/ForumList.vue')
    },
    {
      path: '/forum/:id',
      name: 'ForumDetail',
      component: () => import('@/views/forum/ForumDetail.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 打印调试信息
  console.log('路由守卫:', {
    to: to.path,
    isAuthenticated: userStore.isAuthenticated,
    token: userStore.token ? '存在' : '不存在',
    userInfo: userStore.userInfo
  })

  // 需要认证的路由
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    console.log('需要认证但未登录，跳转到登录页')
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 角色权限检查
  if (to.meta.roles && userStore.isAuthenticated) {
    const userRole = userStore.userInfo?.role
    if (!to.meta.roles.includes(userRole)) {
      ElMessage.error('没有权限访问该页面')
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router


