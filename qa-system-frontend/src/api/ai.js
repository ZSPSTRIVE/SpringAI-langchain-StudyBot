import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

export function chatWithAi(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

export function getInterviewScenes() {
  return request({
    url: '/ai/interview/scenes',
    method: 'get'
  })
}

function parseSseEvent(rawEvent) {
  const lines = rawEvent.split(/\r?\n/)
  let eventType = null
  const dataLines = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventType = line.substring(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.substring(5).trimStart())
    }
  }

  return {
    eventType,
    eventData: dataLines.join('\n')
  }
}

function createSseDispatcher(onMessage, onDone, onError, streamState) {
  return (rawEvent, sessionId) => {
    const { eventType, eventData } = parseSseEvent(rawEvent)
    if (!eventType) return sessionId

    if (eventType === 'session') {
      try {
        const sessionData = JSON.parse(eventData)
        return sessionData.sessionId || sessionId
      } catch {
        return sessionId
      }
    }

    if (eventType === 'message') {
      if (eventData) {
        streamState.hasMessageChunk = true
      }
      onMessage && onMessage(eventData, sessionId)
      return sessionId
    }

    if (eventType === 'done') {
      try {
        const doneData = JSON.parse(eventData)
        if (!streamState.hasMessageChunk && doneData.content) {
          onMessage && onMessage(doneData.content, sessionId)
        }
        onDone && onDone({ ...doneData, sessionId })
      } catch {
        onDone && onDone({ sessionId })
      }
      return sessionId
    }

    if (eventType === 'error') {
      try {
        const errorData = JSON.parse(eventData)
        onError && onError(errorData.error || 'AI服务出错')
      } catch {
        onError && onError('AI服务出错')
      }
    }

    return sessionId
  }
}

export function chatWithAiStream(data, onMessage, onDone, onError) {
  const userStore = useUserStore()
  const token = userStore.token
  const baseURL = '/api'
  const streamState = { hasMessageChunk: false }
  const dispatchSseEvent = createSseDispatcher(onMessage, onDone, onError, streamState)

  return fetch(`${baseURL}/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : '',
      Accept: 'text/event-stream'
    },
    body: JSON.stringify(data)
  })
    .then(async response => {
      if (!response.ok) {
        throw new Error('请求失败')
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('无法读取流式响应')
      }

      const decoder = new TextDecoder()
      let sessionId = null
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        buffer += decoder.decode(value || new Uint8Array(), { stream: !done })

        const events = buffer.split(/\r?\n\r?\n/)
        buffer = events.pop() || ''

        for (const event of events) {
          if (!event.trim()) continue
          sessionId = dispatchSseEvent(event, sessionId)
        }

        if (done) break
      }

      if (buffer.trim()) {
        dispatchSseEvent(buffer, sessionId)
      }
    })
    .catch(error => {
      onError && onError(error.message || 'AI服务暂时不可用')
    })
}

export function getUserSessions(limit = 20) {
  return request({
    url: '/ai/sessions',
    method: 'get',
    params: { limit }
  })
}

export function getSessionHistory(sessionId) {
  return request({
    url: `/ai/sessions/${encodeURIComponent(sessionId)}/history`,
    method: 'get'
  })
}

export function submitFeedback(data) {
  return request({
    url: '/ai/feedback',
    method: 'post',
    data
  })
}

export function bookmarkConversation(conversationId, isBookmarked) {
  return request({
    url: `/ai/bookmark/${conversationId}`,
    method: 'post',
    params: { isBookmarked }
  })
}

export function getBookmarkedConversations() {
  return request({
    url: '/ai/bookmarks',
    method: 'get'
  })
}

export function deleteSession(sessionId) {
  return request({
    url: `/ai/sessions/${sessionId}`,
    method: 'delete'
  })
}

export function renameSession(sessionId, title) {
  return request({
    url: `/ai/sessions/${sessionId}/rename`,
    method: 'put',
    data: { title }
  })
}

export function getKnowledgeOverview(knowledgeBaseId) {
  return request({
    url: '/ai/knowledge/overview',
    method: 'get',
    params: { knowledgeBaseId }
  })
}

export function getKnowledgePoints() {
  return request({
    url: '/ai/knowledge/points',
    method: 'get'
  })
}

export function searchKnowledge(data) {
  return request({
    url: '/ai/knowledge/search',
    method: 'post',
    data
  })
}

export function syncKnowledgeBase(data) {
  return request({
    url: '/ai/knowledge/sync',
    method: 'post',
    data
  })
}
