import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref('')
  const refreshToken = ref('')
  const userInfo = ref(null)

  const isAuthenticated = computed(() => {
    const hasToken = !!token.value
    console.log('检查认证状态:', { hasToken, token: token.value?.substring(0, 20) })
    return hasToken
  })
  const isStudent = computed(() => userInfo.value?.role === 'STUDENT')
  const isTeacher = computed(() => userInfo.value?.role === 'TEACHER')
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  function setToken(accessToken, refreshTokenValue) {
    console.log('✅ setToken被调用')
    token.value = accessToken
    refreshToken.value = refreshTokenValue
  }

  function setUserInfo(info) {
    console.log('✅ setUserInfo被调用:', info)
    userInfo.value = info
  }

  function clearAuth() {
    console.log('🔴 clearAuth被调用')
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
  }

  function login(loginData) {
    console.log('🚀 store.login被调用:', loginData)
    token.value = loginData.accessToken
    refreshToken.value = loginData.refreshToken
    userInfo.value = loginData.userInfo
    console.log('✅ 登录信息已保存到store')
  }

  function logout() {
    clearAuth()
  }

  return {
    token,
    refreshToken,
    userInfo,
    isAuthenticated,
    isStudent,
    isTeacher,
    isAdmin,
    setToken,
    setUserInfo,
    clearAuth,
    login,
    logout
  }
}, {
  // 🔑 关键配置：使用Pinia持久化插件
  persist: {
    key: 'qa-system-user',
    storage: localStorage,
    paths: ['token', 'refreshToken', 'userInfo']
  }
})


