import request from '@/utils/request'

/**
 * 获取所有启用的科目
 */
export function getAllSubjects() {
  return request({
    url: '/v1/subjects',
    method: 'get'
  })
}

/**
 * 获取科目详情
 */
export function getSubjectById(id) {
  return request({
    url: `/v1/subjects/${id}`,
    method: 'get'
  })
}

/**
 * 创建科目
 */
export function createSubject(data) {
  return request({
    url: '/v1/subjects',
    method: 'post',
    data
  })
}

/**
 * 更新科目
 */
export function updateSubject(id, data) {
  return request({
    url: `/v1/subjects/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除科目
 */
export function deleteSubject(id) {
  return request({
    url: `/v1/subjects/${id}`,
    method: 'delete'
  })
}

