import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/v1/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/v1/auth/register',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/v1/auth/logout',
    method: 'post'
  })
}

/**
 * 刷新Token
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/v1/auth/refresh',
    method: 'post',
    params: { refreshToken }
  })
}

/**
 * 健康检查
 */
export function healthCheck() {
  return request({
    url: '/v1/auth/health',
    method: 'get'
  })
}


