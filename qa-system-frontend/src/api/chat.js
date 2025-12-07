import request from '@/utils/request'

// ==================== 好友相关 ====================

/**
 * 搜索用户
 */
export function searchUsers(keyword) {
  return request({
    url: '/v1/chat/users/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 发送好友申请
 */
export function sendFriendRequest(toUserId, message) {
  return request({
    url: '/v1/chat/friends/request',
    method: 'post',
    data: { toUserId, message }
  })
}

/**
 * 获取好友申请列表
 */
export function getFriendRequests() {
  return request({
    url: '/v1/chat/friends/requests',
    method: 'get'
  })
}

/**
 * 获取待处理申请数量
 */
export function getPendingRequestCount() {
  return request({
    url: '/v1/chat/friends/requests/count',
    method: 'get'
  })
}

/**
 * 处理好友申请
 */
export function handleFriendRequest(requestId, accept) {
  return request({
    url: `/v1/chat/friends/requests/${requestId}/handle`,
    method: 'post',
    params: { accept }
  })
}

/**
 * 获取好友列表
 */
export function getFriendList() {
  return request({
    url: '/v1/chat/friends',
    method: 'get'
  })
}

/**
 * 获取在线用户ID列表
 */
export function getOnlineUsers() {
  return request({
    url: '/v1/chat/online-users',
    method: 'get'
  })
}

/**
 * 删除好友
 */
export function deleteFriend(friendId) {
  return request({
    url: `/v1/chat/friends/${friendId}`,
    method: 'delete'
  })
}

/**
 * 修改好友备注
 */
export function updateFriendRemark(friendId, remark) {
  return request({
    url: `/v1/chat/friends/${friendId}/remark`,
    method: 'put',
    data: { remark }
  })
}

/**
 * 获取好友详情
 */
export function getFriendDetail(friendId) {
  return request({
    url: `/v1/chat/friends/${friendId}`,
    method: 'get'
  })
}

/**
 * 设置好友分组
 */
export function setFriendGroup(friendId, groupName) {
  return request({
    url: `/v1/chat/friends/${friendId}/group`,
    method: 'put',
    data: { groupName }
  })
}

/**
 * 拉黑好友
 */
export function blockFriend(friendId) {
  return request({
    url: `/v1/chat/friends/${friendId}/block`,
    method: 'post'
  })
}

/**
 * 会话置顶/取消置顶
 */
export function toggleConversationTop(conversationId, isTop) {
  return request({
    url: `/v1/chat/conversations/${conversationId}/top`,
    method: 'put',
    data: { isTop }
  })
}

// ==================== 会话相关 ====================

/**
 * 获取会话列表
 */
export function getConversations() {
  return request({
    url: '/v1/chat/conversations',
    method: 'get'
  })
}

/**
 * 获取未读消息总数
 */
export function getUnreadCount() {
  return request({
    url: '/v1/chat/unread/count',
    method: 'get'
  })
}

// ==================== 私聊相关 ====================

/**
 * 发送私聊消息
 */
export function sendPrivateMessage(data) {
  return request({
    url: '/v1/chat/messages/private',
    method: 'post',
    data
  })
}

/**
 * 获取私聊消息记录
 */
export function getPrivateMessages(targetId, page = 1, size = 20) {
  return request({
    url: `/v1/chat/messages/private/${targetId}`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 标记消息已读
 */
export function markMessagesAsRead(conversationId) {
  return request({
    url: `/v1/chat/messages/read/${conversationId}`,
    method: 'post'
  })
}

/**
 * 撤回消息
 */
export function recallMessage(messageId) {
  return request({
    url: `/v1/chat/messages/${messageId}/recall`,
    method: 'post'
  })
}

/**
 * 转发消息
 */
export function forwardMessage(messageId, targetId, targetType) {
  return request({
    url: `/v1/chat/messages/${messageId}/forward`,
    method: 'post',
    data: { targetId, targetType }
  })
}

// ==================== 群聊相关 ====================

/**
 * 创建群聊
 */
export function createGroup(name, avatar, memberIds) {
  return request({
    url: '/v1/chat/groups',
    method: 'post',
    data: { name, avatar, memberIds }
  })
}

/**
 * 获取用户的群列表
 */
export function getUserGroups() {
  return request({
    url: '/v1/chat/groups',
    method: 'get'
  })
}

/**
 * 获取群详情
 */
export function getGroupDetail(groupId) {
  return request({
    url: `/v1/chat/groups/${groupId}`,
    method: 'get'
  })
}

/**
 * 获取群成员
 */
export function getGroupMembers(groupId) {
  return request({
    url: `/v1/chat/groups/${groupId}/members`,
    method: 'get'
  })
}

/**
 * 邀请成员加入群
 */
export function inviteMembers(groupId, memberIds) {
  return request({
    url: `/v1/chat/groups/${groupId}/invite`,
    method: 'post',
    data: { memberIds }
  })
}

/**
 * 退出群聊
 */
export function leaveGroup(groupId) {
  return request({
    url: `/v1/chat/groups/${groupId}/leave`,
    method: 'post'
  })
}

/**
 * 解散群聊
 */
export function dismissGroup(groupId) {
  return request({
    url: `/v1/chat/groups/${groupId}`,
    method: 'delete'
  })
}

/**
 * 修改群信息
 */
export function updateGroupInfo(groupId, data) {
  return request({
    url: `/v1/chat/groups/${groupId}`,
    method: 'put',
    data
  })
}

/**
 * 发送群消息
 */
export function sendGroupMessage(data) {
  return request({
    url: '/v1/chat/messages/group',
    method: 'post',
    data
  })
}

/**
 * 获取群消息记录
 */
export function getGroupMessages(groupId, page = 1, size = 20) {
  return request({
    url: `/v1/chat/messages/group/${groupId}`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 设置/取消管理员
 */
export function setGroupAdmin(groupId, userId, isAdmin) {
  return request({
    url: `/v1/chat/groups/${groupId}/admin`,
    method: 'post',
    data: { userId, isAdmin }
  })
}

/**
 * 踢出群成员
 */
export function kickMember(groupId, userId) {
  return request({
    url: `/v1/chat/groups/${groupId}/kick`,
    method: 'post',
    data: { userId }
  })
}

/**
 * 禁言/解除禁言
 */
export function muteMember(groupId, userId, minutes) {
  return request({
    url: `/v1/chat/groups/${groupId}/mute`,
    method: 'post',
    data: { userId, minutes }
  })
}

// ==================== 表情相关 ====================

/**
 * 获取表情包列表
 */
export function getEmojiPacks() {
  return request({
    url: '/v1/chat/emojis/packs',
    method: 'get'
  })
}

/**
 * 获取表情包内的表情
 */
export function getEmojisByPack(packId) {
  return request({
    url: `/v1/chat/emojis/packs/${packId}`,
    method: 'get'
  })
}

/**
 * 获取热门表情
 */
export function getPopularEmojis(limit = 20) {
  return request({
    url: '/v1/chat/emojis/popular',
    method: 'get',
    params: { limit }
  })
}
