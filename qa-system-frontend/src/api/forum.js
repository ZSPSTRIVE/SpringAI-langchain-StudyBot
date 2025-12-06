import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建专门用于论坛API的axios实例（旧系统路径）
const forumRequest = axios.create({
  baseURL: '',  // 使用Vite代理，避免CORS问题
  timeout: 30000
})

console.log('🔧 论坛API配置已加载 - baseURL:', forumRequest.defaults.baseURL || '(空-使用代理)')

// 请求拦截器
forumRequest.interceptors.request.use(
  config => {
    console.log('📤 论坛API请求:', config.method?.toUpperCase(), config.url)
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
forumRequest.interceptors.response.use(
  response => {
    console.log('📦 论坛API响应:', response.data)
    const res = response.data
    
    // 旧系统使用不同的响应格式
    // 成功：{ code: 0, data: {...} }
    // 失败：{ code: 500, msg: "..." }
    if (res.code === 0 || res.code === 200) {
      return res
    }
    
    console.error('❌ 响应code不是0或200:', res)
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    console.error('❌ 论坛API网络错误:', error)
    console.error('错误响应数据:', error.response?.data)
    ElMessage.error(error.response?.data?.msg || error.message || '请求失败')
    return Promise.reject(error)
  }
)

/**
 * 获取论坛帖子列表（分页）
 */
export function getForumList(params) {
  console.log('🚀 发送论坛列表请求:', params)
  return forumRequest({
    url: '/forum/flist',
    method: 'get',
    params
  }).then(res => {
    console.log('✅ 论坛列表响应成功:', res)
    return res
  }).catch(err => {
    console.error('❌ 论坛列表请求失败:', err)
    throw err
  })
}

/**
 * 获取帖子详情及其所有回复
 */
export function getForumDetail(id) {
  return forumRequest({
    url: `/forum/list/${id}`,
    method: 'get'
  })
}

/**
 * 创建新帖子
 */
export function createForum(data) {
  return forumRequest({
    url: '/forum/add',
    method: 'post',
    data
  })
}

/**
 * 回复帖子
 */
export function replyForum(data) {
  return forumRequest({
    url: '/forum/add',
    method: 'post',
    data
  })
}

/**
 * 更新帖子
 */
export function updateForum(data) {
  return forumRequest({
    url: '/forum/update',
    method: 'post',
    data
  })
}

/**
 * 删除帖子
 */
export function deleteForum(ids) {
  return forumRequest({
    url: '/forum/delete',
    method: 'post',
    data: ids
  })
}

