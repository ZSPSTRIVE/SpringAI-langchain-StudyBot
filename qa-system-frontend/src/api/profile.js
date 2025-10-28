import request from '@/utils/request'

/**
 * 获取当前用户个人信息
 */
export function getCurrentUserProfile() {
  return request({
    url: '/v1/profile/me',
    method: 'get'
  })
}

/**
 * 更新个人信息
 */
export function updateProfile(data) {
  return request({
    url: '/v1/profile/me',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export function changePassword(data) {
  return request({
    url: '/v1/profile/password',
    method: 'put',
    data
  })
}

