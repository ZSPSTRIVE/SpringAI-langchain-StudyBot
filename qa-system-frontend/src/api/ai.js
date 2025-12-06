import request from '@/utils/request'

/**
 * AI助手API
 */

// AI聊天对话
export function chatWithAi(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

// 获取用户的会话列表
export function getUserSessions(limit = 20) {
  return request({
    url: '/ai/sessions',
    method: 'get',
    params: { limit }
  })
}

// 获取会话历史
export function getSessionHistory(sessionId) {
  return request({
    url: `/ai/sessions/${sessionId}/history`,
    method: 'get'
  })
}

// 提交反馈
export function submitFeedback(data) {
  return request({
    url: '/ai/feedback',
    method: 'post',
    data
  })
}

// 收藏/取消收藏对话
export function bookmarkConversation(conversationId, isBookmarked) {
  return request({
    url: `/ai/bookmark/${conversationId}`,
    method: 'post',
    params: { isBookmarked }
  })
}

// 获取收藏的对话
export function getBookmarkedConversations() {
  return request({
    url: '/ai/bookmarks',
    method: 'get'
  })
}

// 删除会话
export function deleteSession(sessionId) {
  return request({
    url: `/ai/sessions/${sessionId}`,
    method: 'delete'
  })
}

// 重命名会话
export function renameSession(sessionId, title) {
  return request({
    url: `/ai/sessions/${sessionId}/rename`,
    method: 'put',
    data: { title }
  })
}

