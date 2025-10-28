import request from '@/utils/request'

/**
 * 根据问题ID获取所有回答
 */
export function getAnswersByQuestionId(questionId) {
  return request({
    url: `/v1/answers/question/${questionId}`,
    method: 'get'
  })
}

/**
 * 创建回答
 */
export function createAnswer(data) {
  return request({
    url: '/v1/answers',
    method: 'post',
    data
  })
}

/**
 * 更新回答
 */
export function updateAnswer(id, data) {
  return request({
    url: `/v1/answers/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除回答
 */
export function deleteAnswer(id) {
  return request({
    url: `/v1/answers/${id}`,
    method: 'delete'
  })
}

/**
 * 采纳回答
 */
export function acceptAnswer(id) {
  return request({
    url: `/v1/answers/${id}/accept`,
    method: 'put'
  })
}

