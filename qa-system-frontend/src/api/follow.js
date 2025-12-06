import request from '@/utils/request'

/**
 * 关注教师
 */
export function followTeacher(teacherId) {
  return request({
    url: `/v1/follows/teacher/${teacherId}`,
    method: 'post'
  })
}

/**
 * 取消关注
 */
export function unfollowTeacher(teacherId) {
  return request({
    url: `/v1/follows/teacher/${teacherId}`,
    method: 'delete'
  })
}

/**
 * 检查是否已关注
 */
export function checkFollowing(teacherId) {
  return request({
    url: `/v1/follows/teacher/${teacherId}/check`,
    method: 'get'
  })
}

/**
 * 获取关注的教师列表
 */
export function getFollowingTeachers() {
  return request({
    url: '/v1/follows/teachers',
    method: 'get'
  })
}

/**
 * 获取关注数量
 */
export function getFollowingCount() {
  return request({
    url: '/v1/follows/count',
    method: 'get'
  })
}

