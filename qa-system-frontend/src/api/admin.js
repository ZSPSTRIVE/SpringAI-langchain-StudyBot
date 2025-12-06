import request from '@/utils/request'

/**
 * 获取数据统计
 */
export function getStatistics() {
  return request({
    url: '/v1/admin/statistics',
    method: 'get'
  })
}

/**
 * 分页查询学生
 */
export function getStudentPage(params) {
  return request({
    url: '/v1/admin/students',
    method: 'get',
    params
  })
}

/**
 * 分页查询教师
 */
export function getTeacherPage(params) {
  return request({
    url: '/v1/admin/teachers',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 */
export function updateUserStatus(userId, status) {
  return request({
    url: `/v1/admin/users/${userId}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 删除用户
 */
export function deleteUser(userId) {
  return request({
    url: `/v1/admin/users/${userId}`,
    method: 'delete'
  })
}

/**
 * 重置用户密码（手动输入）
 */
export function resetUserPassword(userId, newPassword) {
  return request({
    url: `/v1/admin/users/${userId}/reset-password`,
    method: 'put',
    data: { newPassword }
  })
}

/**
 * 创建学生账号
 */
export function createStudent(data) {
  return request({
    url: '/v1/admin/students',
    method: 'post',
    data
  })
}

/**
 * 创建教师账号
 */
export function createTeacher(data) {
  return request({
    url: '/v1/admin/teachers',
    method: 'post',
    data
  })
}

