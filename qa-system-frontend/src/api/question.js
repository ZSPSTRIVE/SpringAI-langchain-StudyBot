import request from '@/utils/request'

/**
 * 分页查询问题
 */
export function getQuestionPage(params) {
  return request({
    url: '/v1/questions',
    method: 'get',
    params
  })
}

/**
 * 分页查询问题列表（别名，用于管理页面）
 */
export function getQuestionList(params) {
  return getQuestionPage(params)
}

/**
 * 获取问题详情
 */
export function getQuestionById(id) {
  return request({
    url: `/v1/questions/${id}`,
    method: 'get'
  })
}

/**
 * 创建问题
 */
export function createQuestion(data) {
  return request({
    url: '/v1/questions',
    method: 'post',
    data
  })
}

/**
 * 更新问题
 */
export function updateQuestion(id, data) {
  return request({
    url: `/v1/questions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除问题
 */
export function deleteQuestion(id) {
  return request({
    url: `/v1/questions/${id}`,
    method: 'delete'
  })
}

/**
 * 关闭问题
 */
export function closeQuestion(id) {
  return request({
    url: `/v1/questions/${id}/close`,
    method: 'put'
  })
}

