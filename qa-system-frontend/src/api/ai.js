import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

/**
 * AI助手API
 */

// AI聊天对话（普通模式）
export function chatWithAi(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

// AI聊天对话（流式模式 SSE）
export function chatWithAiStream(data, onMessage, onDone, onError) {
  const userStore = useUserStore()
  const token = userStore.token
  const baseURL = '/api'
  
  return fetch(`${baseURL}/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : '',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(data)
  }).then(async response => {
    if (!response.ok) {
      throw new Error('请求失败')
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let sessionId = null
    let buffer = ''  // 缓冲区，处理跨chunk的数据
    let currentEventType = null
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      // 将新数据追加到缓冲区
      buffer += decoder.decode(value, { stream: true })
      
      // 按双换行符分割完整的SSE事件
      const events = buffer.split('\n\n')
      // 最后一个可能是不完整的，保留在缓冲区
      buffer = events.pop() || ''
      
      for (const event of events) {
        if (!event.trim()) continue
        
        const lines = event.split('\n')
        let eventType = null
        let eventData = ''
        
        for (const line of lines) {
          if (line.startsWith('event:')) {
            eventType = line.substring(6).trim()
          } else if (line.startsWith('data:')) {
            eventData = line.substring(5)
          }
        }
        
        if (eventType === 'session') {
          try {
            const sessionData = JSON.parse(eventData)
            sessionId = sessionData.sessionId
          } catch (e) { /* ignore */ }
        } else if (eventType === 'message') {
          onMessage && onMessage(eventData, sessionId)
        } else if (eventType === 'done') {
          try {
            const doneData = JSON.parse(eventData)
            onDone && onDone({ ...doneData, sessionId })
          } catch (e) {
            onDone && onDone({ sessionId })
          }
        } else if (eventType === 'error') {
          try {
            const errorData = JSON.parse(eventData)
            onError && onError(errorData.error || 'AI服务出错')
          } catch (e) {
            onError && onError('AI服务出错')
          }
        }
      }
    }
    
    // 处理缓冲区中剩余的数据
    if (buffer.trim()) {
      const lines = buffer.split('\n')
      let eventType = null
      let eventData = ''
      
      for (const line of lines) {
        if (line.startsWith('event:')) {
          eventType = line.substring(6).trim()
        } else if (line.startsWith('data:')) {
          eventData = line.substring(5)
        }
      }
      
      if (eventType === 'message') {
        onMessage && onMessage(eventData, sessionId)
      } else if (eventType === 'done') {
        try {
          const doneData = JSON.parse(eventData)
          onDone && onDone({ ...doneData, sessionId })
        } catch (e) {
          onDone && onDone({ sessionId })
        }
      }
    }
  }).catch(error => {
    onError && onError(error.message || 'AI服务暂时不可用')
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

