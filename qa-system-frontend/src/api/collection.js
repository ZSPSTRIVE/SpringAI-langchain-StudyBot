import request from '@/utils/request'

/**
 * 收藏
 */
export function collect(targetType, targetId) {
  return request({
    url: '/v1/collections',
    method: 'post',
    params: { targetType, targetId }
  })
}

/**
 * 取消收藏
 */
export function uncollect(targetType, targetId) {
  return request({
    url: '/v1/collections',
    method: 'delete',
    params: { targetType, targetId }
  })
}

/**
 * 检查是否已收藏
 */
export function checkCollected(targetType, targetId) {
  return request({
    url: '/v1/collections/check',
    method: 'get',
    params: { targetType, targetId }
  })
}

/**
 * 获取收藏的问题列表
 */
export function getCollectedQuestions() {
  return request({
    url: '/v1/collections/questions',
    method: 'get'
  })
}

/**
 * 获取收藏数量
 */
export function getCollectionCount() {
  return request({
    url: '/v1/collections/count',
    method: 'get'
  })
}

