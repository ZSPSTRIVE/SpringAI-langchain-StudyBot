package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.common.response.Result;
import com.qasystem.entity.*;
import com.qasystem.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 聊天控制器 - 提供好友、私聊、群聊功能
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ==================== 好友相关 ====================

    /**
     * 搜索用户（添加好友）
     */
    @GetMapping("/users/search")
    public Result<List<Map<String, Object>>> searchUsers(
            Authentication authentication,
            @RequestParam String keyword) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.searchUsers(userId, keyword));
    }

    /**
     * 发送好友申请
     */
    @PostMapping("/friends/request")
    public Result<Void> sendFriendRequest(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        Long toUserId = ((Number) request.get("toUserId")).longValue();
        String message = (String) request.get("message");
        chatService.sendFriendRequest(userId, toUserId, message);
        return Result.success("好友申请已发送", null);
    }

    /**
     * 获取好友申请列表
     */
    @GetMapping("/friends/requests")
    public Result<List<FriendRequest>> getFriendRequests(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getFriendRequests(userId));
    }

    /**
     * 获取待处理申请数量
     */
    @GetMapping("/friends/requests/count")
    public Result<Long> getPendingRequestCount(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getPendingRequestCount(userId));
    }

    /**
     * 处理好友申请
     */
    @PostMapping("/friends/requests/{requestId}/handle")
    public Result<Void> handleFriendRequest(
            Authentication authentication,
            @PathVariable Long requestId,
            @RequestParam boolean accept) {
        Long userId = getUserId(authentication);
        chatService.handleFriendRequest(requestId, userId, accept);
        return Result.success(accept ? "已添加好友" : "已拒绝", null);
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/friends")
    public Result<List<Map<String, Object>>> getFriendList(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getFriendList(userId));
    }

    /**
     * 获取在线用户ID列表
     */
    @GetMapping("/online-users")
    public Result<Set<Long>> getOnlineUsers() {
        return Result.success(chatService.getOnlineUserIds());
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/friends/{friendId}")
    public Result<Void> deleteFriend(
            Authentication authentication,
            @PathVariable Long friendId) {
        Long userId = getUserId(authentication);
        chatService.deleteFriend(userId, friendId);
        return Result.success("已删除好友", null);
    }

    /**
     * 获取好友详情
     */
    @GetMapping("/friends/{friendId}")
    public Result<Map<String, Object>> getFriendDetail(
            Authentication authentication,
            @PathVariable Long friendId) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getFriendDetail(userId, friendId));
    }

    /**
     * 修改好友备注
     */
    @PutMapping("/friends/{friendId}/remark")
    public Result<Void> updateFriendRemark(
            Authentication authentication,
            @PathVariable Long friendId,
            @RequestBody Map<String, String> request) {
        Long userId = getUserId(authentication);
        chatService.updateFriendRemark(userId, friendId, request.get("remark"));
        return Result.success("备注修改成功", null);
    }

    /**
     * 设置好友分组
     */
    @PutMapping("/friends/{friendId}/group")
    public Result<Void> setFriendGroup(
            Authentication authentication,
            @PathVariable Long friendId,
            @RequestBody Map<String, String> request) {
        Long userId = getUserId(authentication);
        chatService.setFriendGroup(userId, friendId, request.get("groupName"));
        return Result.success("分组设置成功", null);
    }

    /**
     * 拉黑好友
     */
    @PostMapping("/friends/{friendId}/block")
    public Result<Void> blockFriend(
            Authentication authentication,
            @PathVariable Long friendId) {
        Long userId = getUserId(authentication);
        chatService.blockFriend(userId, friendId);
        return Result.success("已拉黑", null);
    }

    // ==================== 会话相关 ====================

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ChatConversation>> getConversations(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getConversationList(userId));
    }

    /**
     * 获取未读消息总数
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getTotalUnreadCount(userId));
    }

    /**
     * 会话置顶/取消置顶
     */
    @PutMapping("/conversations/{conversationId}/top")
    public Result<Void> toggleConversationTop(
            Authentication authentication,
            @PathVariable Long conversationId,
            @RequestBody Map<String, Boolean> request) {
        Long userId = getUserId(authentication);
        Boolean isTop = request.get("isTop");
        chatService.toggleConversationTop(userId, conversationId, isTop);
        return Result.success(isTop ? "已置顶" : "已取消置顶", null);
    }

    // ==================== 私聊相关 ====================

    /**
     * 发送私聊消息
     */
    @PostMapping("/messages/private")
    public Result<ChatMessage> sendPrivateMessage(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        Long receiverId = ((Number) request.get("receiverId")).longValue();
        String content = (String) request.get("content");
        String type = (String) request.get("type");
        String mediaUrl = (String) request.get("mediaUrl");
        String mediaThumbnail = (String) request.get("mediaThumbnail");
        Integer mediaDuration = request.get("mediaDuration") != null ? ((Number) request.get("mediaDuration")).intValue() : null;
        Long mediaSize = request.get("mediaSize") != null ? ((Number) request.get("mediaSize")).longValue() : null;

        ChatMessage message = chatService.sendPrivateMessage(userId, receiverId, content, type, mediaUrl, mediaThumbnail, mediaDuration, mediaSize);
        return Result.success(message);
    }

    /**
     * 获取私聊消息记录
     */
    @GetMapping("/messages/private/{targetId}")
    public Result<IPage<ChatMessage>> getPrivateMessages(
            Authentication authentication,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getPrivateMessages(userId, targetId, page, size));
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/messages/read/{conversationId}")
    public Result<Void> markMessagesAsRead(
            Authentication authentication,
            @PathVariable Long conversationId) {
        Long userId = getUserId(authentication);
        chatService.markMessagesAsRead(userId, conversationId);
        return Result.success(null);
    }

    /**
     * 撤回消息
     */
    @PostMapping("/messages/{messageId}/recall")
    public Result<Void> recallMessage(
            Authentication authentication,
            @PathVariable Long messageId) {
        Long userId = getUserId(authentication);
        chatService.recallMessage(userId, messageId);
        return Result.success("消息已撤回", null);
    }

    /**
     * 转发消息
     */
    @PostMapping("/messages/{messageId}/forward")
    public Result<ChatMessage> forwardMessage(
            Authentication authentication,
            @PathVariable Long messageId,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        Long targetId = ((Number) request.get("targetId")).longValue();
        String targetType = (String) request.get("targetType");
        ChatMessage message = chatService.forwardMessage(userId, messageId, targetId, targetType);
        return Result.success(message);
    }

    // ==================== 群聊相关 ====================

    /**
     * 创建群聊
     */
    @PostMapping("/groups")
    public Result<ChatGroup> createGroup(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        String name = (String) request.get("name");
        String avatar = (String) request.get("avatar");
        @SuppressWarnings("unchecked")
        List<Number> memberIdNumbers = (List<Number>) request.get("memberIds");
        List<Long> memberIds = memberIdNumbers.stream().map(Number::longValue).toList();

        ChatGroup group = chatService.createGroup(userId, name, avatar, memberIds);
        return Result.success("群聊创建成功", group);
    }

    /**
     * 获取用户的群列表
     */
    @GetMapping("/groups")
    public Result<List<ChatGroup>> getUserGroups(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getUserGroups(userId));
    }

    /**
     * 获取群详情
     */
    @GetMapping("/groups/{groupId}")
    public Result<ChatGroup> getGroupDetail(@PathVariable Long groupId) {
        return Result.success(chatService.getGroupDetail(groupId));
    }

    /**
     * 获取群成员
     */
    @GetMapping("/groups/{groupId}/members")
    public Result<List<ChatGroupMember>> getGroupMembers(@PathVariable Long groupId) {
        return Result.success(chatService.getGroupMembers(groupId));
    }

    /**
     * 邀请成员加入群
     */
    @PostMapping("/groups/{groupId}/invite")
    public Result<Void> inviteMembers(
            Authentication authentication,
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        @SuppressWarnings("unchecked")
        List<Number> memberIdNumbers = (List<Number>) request.get("memberIds");
        List<Long> memberIds = memberIdNumbers.stream().map(Number::longValue).toList();

        chatService.inviteMembers(groupId, userId, memberIds);
        return Result.success("邀请成功", null);
    }

    /**
     * 退出群聊
     */
    @PostMapping("/groups/{groupId}/leave")
    public Result<Void> leaveGroup(
            Authentication authentication,
            @PathVariable Long groupId) {
        Long userId = getUserId(authentication);
        chatService.leaveGroup(groupId, userId);
        return Result.success("已退出群聊", null);
    }

    /**
     * 解散群聊
     */
    @DeleteMapping("/groups/{groupId}")
    public Result<Void> dismissGroup(
            Authentication authentication,
            @PathVariable Long groupId) {
        Long userId = getUserId(authentication);
        chatService.dismissGroup(groupId, userId);
        return Result.success("群聊已解散", null);
    }

    /**
     * 修改群信息
     */
    @PutMapping("/groups/{groupId}")
    public Result<Void> updateGroupInfo(
            Authentication authentication,
            @PathVariable Long groupId,
            @RequestBody Map<String, String> request) {
        Long userId = getUserId(authentication);
        chatService.updateGroupInfo(groupId, userId, request.get("name"), request.get("avatar"), request.get("announcement"));
        return Result.success("修改成功", null);
    }

    /**
     * 发送群消息
     */
    @PostMapping("/messages/group")
    public Result<ChatGroupMessage> sendGroupMessage(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        Long groupId = ((Number) request.get("groupId")).longValue();
        String content = (String) request.get("content");
        String type = (String) request.get("type");
        String mediaUrl = (String) request.get("mediaUrl");
        String mediaThumbnail = (String) request.get("mediaThumbnail");
        Integer mediaDuration = request.get("mediaDuration") != null ? ((Number) request.get("mediaDuration")).intValue() : null;
        Long mediaSize = request.get("mediaSize") != null ? ((Number) request.get("mediaSize")).longValue() : null;
        String atUserIds = (String) request.get("atUserIds");
        Boolean atAll = request.get("atAll") != null && (Boolean) request.get("atAll");

        ChatGroupMessage message = chatService.sendGroupMessage(groupId, userId, content, type, mediaUrl, mediaThumbnail, mediaDuration, mediaSize, atUserIds, atAll);
        return Result.success(message);
    }

    /**
     * 获取群消息记录
     */
    @GetMapping("/messages/group/{groupId}")
    public Result<IPage<ChatGroupMessage>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(chatService.getGroupMessages(groupId, page, size));
    }

    /**
     * 设置/取消管理员
     */
    @PostMapping("/groups/{groupId}/admin")
    public Result<Void> setGroupAdmin(
            Authentication authentication,
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        Long targetUserId = ((Number) request.get("userId")).longValue();
        Boolean isAdmin = (Boolean) request.get("isAdmin");
        chatService.setGroupAdmin(groupId, userId, targetUserId, isAdmin);
        return Result.success(isAdmin ? "已设为管理员" : "已取消管理员", null);
    }

    /**
     * 踢出群成员
     */
    @PostMapping("/groups/{groupId}/kick")
    public Result<Void> kickMember(
            Authentication authentication,
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        Long targetUserId = ((Number) request.get("userId")).longValue();
        chatService.kickMember(groupId, userId, targetUserId);
        return Result.success("已移出群聊", null);
    }

    /**
     * 禁言/解除禁言
     */
    @PostMapping("/groups/{groupId}/mute")
    public Result<Void> muteMember(
            Authentication authentication,
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(authentication);
        Long targetUserId = ((Number) request.get("userId")).longValue();
        Integer minutes = ((Number) request.get("minutes")).intValue();
        chatService.muteMember(groupId, userId, targetUserId, minutes);
        return Result.success(minutes > 0 ? "已禁言" : "已解除禁言", null);
    }

    // ==================== 表情相关 ====================

    /**
     * 获取表情包列表
     */
    @GetMapping("/emojis/packs")
    public Result<List<Map<String, Object>>> getEmojiPacks() {
        return Result.success(chatService.getEmojiPacks());
    }

    /**
     * 获取表情包内的表情
     */
    @GetMapping("/emojis/packs/{packId}")
    public Result<List<Emoji>> getEmojisByPack(@PathVariable Long packId) {
        return Result.success(chatService.getEmojisByPack(packId));
    }

    /**
     * 获取热门表情
     */
    @GetMapping("/emojis/popular")
    public Result<List<Emoji>> getPopularEmojis(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(chatService.getPopularEmojis(limit));
    }

    // ==================== 私有方法 ====================

    private Long getUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
