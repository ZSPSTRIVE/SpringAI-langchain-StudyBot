package com.qasystem.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.entity.ChatMessage;
import com.qasystem.entity.ChatGroupMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天WebSocket处理器
 * 处理实时消息推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // 用户ID -> WebSocket会话映射
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // 用户ID -> 用户所在群ID集合
    private static final Map<Long, Set<Long>> userGroups = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("用户 {} WebSocket连接建立", userId);
            
            // 发送连接成功消息
            sendMessage(session, Map.of(
                "type", "CONNECTED",
                "message", "连接成功",
                "userId", userId
            ));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            userGroups.remove(userId);
            log.info("用户 {} WebSocket连接关闭", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            
            switch (type) {
                case "PING" -> handlePing(session);
                case "JOIN_GROUP" -> handleJoinGroup(session, payload);
                case "LEAVE_GROUP" -> handleLeaveGroup(session, payload);
                case "READ_MESSAGE" -> handleReadMessage(session, payload);
                default -> log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    /**
     * 发送私聊消息给接收方
     */
    public void sendPrivateMessage(Long receiverId, ChatMessage message) {
        WebSocketSession session = userSessions.get(receiverId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, Map.of(
                    "type", "PRIVATE_MESSAGE",
                    "data", message
                ));
            } catch (Exception e) {
                log.error("发送私聊消息失败", e);
            }
        }
    }

    /**
     * 发送群消息给群成员
     */
    public void sendGroupMessage(Long groupId, ChatGroupMessage message, Set<Long> memberIds) {
        for (Long memberId : memberIds) {
            // 不发送给发送者自己
            if (memberId.equals(message.getSenderId())) {
                continue;
            }
            
            WebSocketSession session = userSessions.get(memberId);
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, Map.of(
                        "type", "GROUP_MESSAGE",
                        "groupId", groupId,
                        "data", message
                    ));
                } catch (Exception e) {
                    log.error("发送群消息失败", e);
                }
            }
        }
    }

    /**
     * 发送好友申请通知
     */
    public void sendFriendRequest(Long toUserId, Map<String, Object> requestInfo) {
        WebSocketSession session = userSessions.get(toUserId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, Map.of(
                    "type", "FRIEND_REQUEST",
                    "data", requestInfo
                ));
            } catch (Exception e) {
                log.error("发送好友申请通知失败", e);
            }
        }
    }

    /**
     * 发送好友申请结果通知
     */
    public void sendFriendRequestResult(Long toUserId, boolean accepted, Map<String, Object> userInfo) {
        WebSocketSession session = userSessions.get(toUserId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, Map.of(
                    "type", "FRIEND_REQUEST_RESULT",
                    "accepted", accepted,
                    "data", userInfo
                ));
            } catch (Exception e) {
                log.error("发送好友申请结果失败", e);
            }
        }
    }

    /**
     * 发送消息撤回通知
     */
    public void sendRecallNotice(Long receiverId, Long messageId, String conversationType) {
        WebSocketSession session = userSessions.get(receiverId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, Map.of(
                    "type", "MESSAGE_RECALLED",
                    "messageId", messageId,
                    "conversationType", conversationType
                ));
            } catch (Exception e) {
                log.error("发送撤回通知失败", e);
            }
        }
    }

    /**
     * 发送在线状态变更
     */
    public void sendOnlineStatus(Long userId, boolean online, Set<Long> friendIds) {
        for (Long friendId : friendIds) {
            WebSocketSession session = userSessions.get(friendId);
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, Map.of(
                        "type", "ONLINE_STATUS",
                        "userId", userId,
                        "online", online
                    ));
                } catch (Exception e) {
                    log.error("发送在线状态失败", e);
                }
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户ID列表
     */
    public Set<Long> getOnlineUserIds() {
        return userSessions.keySet();
    }

    // ==================== 私有方法 ====================

    private void handlePing(WebSocketSession session) throws IOException {
        sendMessage(session, Map.of("type", "PONG"));
    }

    private void handleJoinGroup(WebSocketSession session, Map<String, Object> payload) {
        Long userId = getUserIdFromSession(session);
        Long groupId = ((Number) payload.get("groupId")).longValue();
        
        userGroups.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(groupId);
        log.info("用户 {} 加入群 {} 的实时推送", userId, groupId);
    }

    private void handleLeaveGroup(WebSocketSession session, Map<String, Object> payload) {
        Long userId = getUserIdFromSession(session);
        Long groupId = ((Number) payload.get("groupId")).longValue();
        
        Set<Long> groups = userGroups.get(userId);
        if (groups != null) {
            groups.remove(groupId);
        }
    }

    private void handleReadMessage(WebSocketSession session, Map<String, Object> payload) {
        // 标记消息已读，可以通知发送方
        Long senderId = ((Number) payload.get("senderId")).longValue();
        Long messageId = ((Number) payload.get("messageId")).longValue();
        
        WebSocketSession senderSession = userSessions.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            try {
                sendMessage(senderSession, Map.of(
                    "type", "MESSAGE_READ",
                    "messageId", messageId
                ));
            } catch (Exception e) {
                log.error("发送已读回执失败", e);
            }
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String userIdStr = query.split("userId=")[1].split("&")[0];
                return Long.parseLong(userIdStr);
            }
        } catch (Exception e) {
            log.error("从WebSocket会话获取用户ID失败", e);
        }
        return null;
    }

    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        }
    }
}
