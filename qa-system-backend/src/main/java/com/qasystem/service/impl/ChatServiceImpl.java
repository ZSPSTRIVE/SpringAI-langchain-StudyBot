package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.*;
import com.qasystem.mapper.*;
import com.qasystem.service.ChatService;
import com.qasystem.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;
    private final ChatGroupMapper groupMapper;
    private final ChatGroupMemberMapper groupMemberMapper;
    private final ChatGroupMessageMapper groupMessageMapper;
    private final EmojiMapper emojiMapper;
    private final UserMapper userMapper;
    private final ChatWebSocketHandler webSocketHandler;

    // ==================== 好友相关 ====================

    @Override
    @Transactional
    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        // 检查是否已经是好友
        if (friendMapper.isFriend(fromUserId, toUserId)) {
            throw new RuntimeException("已经是好友了");
        }

        // 检查是否已有待处理的申请
        FriendRequest existing = friendRequestMapper.findExistingRequest(fromUserId, toUserId);
        if (existing != null) {
            throw new RuntimeException("已发送过好友申请，请等待对方处理");
        }

        // 创建申请
        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus("PENDING");
        friendRequestMapper.insert(request);

        // WebSocket通知对方
        User fromUser = userMapper.selectById(fromUserId);
        webSocketHandler.sendFriendRequest(toUserId, Map.of(
            "requestId", request.getId(),
            "fromUser", Map.of(
                "id", fromUser.getId(),
                "username", fromUser.getUsername(),
                "realName", fromUser.getRealName(),
                "avatar", fromUser.getAvatar() != null ? fromUser.getAvatar() : ""
            ),
            "message", message != null ? message : ""
        ));

        log.info("用户{}向用户{}发送好友申请", fromUserId, toUserId);
    }

    @Override
    @Transactional
    public void handleFriendRequest(Long requestId, Long userId, boolean accept) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new RuntimeException("申请不存在");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("申请已处理");
        }

        request.setStatus(accept ? "ACCEPTED" : "REJECTED");
        friendRequestMapper.updateById(request);

        if (accept) {
            Long fromUserId = request.getFromUserId();
            Long toUserId = request.getToUserId();
            
            // 安全插入好友关系（忽略重复键错误）
            safeInsertFriend(fromUserId, toUserId);
            safeInsertFriend(toUserId, fromUserId);
            
            log.info("处理好友申请完成: {} <-> {}", fromUserId, toUserId);
        }

        // 通知申请方结果
        User toUser = userMapper.selectById(userId);
        webSocketHandler.sendFriendRequestResult(request.getFromUserId(), accept, Map.of(
            "id", toUser.getId(),
            "username", toUser.getUsername(),
            "realName", toUser.getRealName(),
            "avatar", toUser.getAvatar() != null ? toUser.getAvatar() : ""
        ));

        log.info("用户{}{}了用户{}的好友申请", userId, accept ? "同意" : "拒绝", request.getFromUserId());
    }

    /**
     * 安全插入或更新好友关系
     */
    private void safeInsertFriend(Long userId, Long friendId) {
        try {
            // 使用原生SQL查询，包括已删除的记录
            Friend existing = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                    .eq(Friend::getUserId, userId)
                    .eq(Friend::getFriendId, friendId)
                    .last("OR (user_id = " + userId + " AND friend_id = " + friendId + " AND deleted = 1)"));
            
            if (existing == null) {
                // 完全不存在，插入新记录
                Friend friend = new Friend();
                friend.setUserId(userId);
                friend.setFriendId(friendId);
                friend.setStatus("ACCEPTED");
                friend.setDeleted(0);  // 明确设置为未删除
                friendMapper.insert(friend);
                log.info("添加好友关系成功: {} -> {}", userId, friendId);
            } else {
                // 存在记录，更新状态和deleted标志
                existing.setStatus("ACCEPTED");
                existing.setDeleted(0);  // 恢复未删除状态
                friendMapper.updateById(existing);
                log.info("更新好友关系: {} -> {}, status=ACCEPTED, deleted=0", userId, friendId);
            }
        } catch (Exception e) {
            log.error("插入好友关系失败: {} -> {}, error: {}", userId, friendId, e.getMessage());
            // 尝试直接用SQL插入
            try {
                Friend friend = new Friend();
                friend.setUserId(userId);
                friend.setFriendId(friendId);
                friend.setStatus("ACCEPTED");
                friend.setDeleted(0);
                friendMapper.insert(friend);
                log.info("重试插入好友关系成功: {} -> {}", userId, friendId);
            } catch (Exception ex) {
                log.warn("好友关系可能已存在: {} -> {}", userId, friendId);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getFriendList(Long userId) {
        log.info("获取用户{}的好友列表", userId);
        List<Friend> friends = friendMapper.findFriendsByUserId(userId);
        log.info("查询到{}条好友记录", friends.size());
        
        List<Map<String, Object>> result = new ArrayList<>();

        for (Friend friend : friends) {
            log.info("处理好友关系: userId={}, friendId={}, status={}", 
                    friend.getUserId(), friend.getFriendId(), friend.getStatus());
            User user = userMapper.selectById(friend.getFriendId());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("friendshipId", friend.getId());
                item.put("userId", user.getId());
                item.put("username", user.getUsername());
                item.put("realName", user.getRealName());
                item.put("avatar", user.getAvatar());
                item.put("remark", friend.getRemark());
                item.put("groupName", friend.getGroupName());
                item.put("online", webSocketHandler.isOnline(user.getId()));
                result.add(item);
            }
        }

        log.info("返回{}个好友", result.size());
        return result;
    }

    @Override
    public List<FriendRequest> getFriendRequests(Long userId) {
        List<FriendRequest> requests = friendRequestMapper.findReceivedRequests(userId);
        for (FriendRequest request : requests) {
            request.setFromUser(userMapper.selectById(request.getFromUserId()));
        }
        return requests;
    }

    @Override
    public long getPendingRequestCount(Long userId) {
        return friendRequestMapper.countPendingRequests(userId);
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        // 删除双向好友关系
        friendMapper.delete(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId));
        friendMapper.delete(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId));

        log.info("用户{}删除了好友{}", userId, friendId);
    }

    @Override
    public Map<String, Object> getFriendDetail(Long userId, Long friendId) {
        Friend friend = friendMapper.findFriendship(userId, friendId);
        if (friend == null) {
            throw new RuntimeException("不是好友关系");
        }

        User user = userMapper.selectById(friendId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("avatar", user.getAvatar());
        result.put("gender", user.getGender());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("role", user.getRole());
        result.put("remark", friend.getRemark());
        result.put("groupName", friend.getGroupName());
        result.put("online", webSocketHandler.isOnline(friendId));
        result.put("createTime", friend.getCreateTime());
        return result;
    }

    @Override
    @Transactional
    public void updateFriendRemark(Long userId, Long friendId, String remark) {
        Friend friend = friendMapper.findFriendship(userId, friendId);
        if (friend == null) {
            throw new RuntimeException("不是好友关系");
        }
        friend.setRemark(remark);
        friendMapper.updateById(friend);
        log.info("用户{}修改好友{}的备注为: {}", userId, friendId, remark);
    }

    @Override
    @Transactional
    public void setFriendGroup(Long userId, Long friendId, String groupName) {
        Friend friend = friendMapper.findFriendship(userId, friendId);
        if (friend == null) {
            throw new RuntimeException("不是好友关系");
        }
        friend.setGroupName(groupName);
        friendMapper.updateById(friend);
        log.info("用户{}将好友{}移动到分组: {}", userId, friendId, groupName);
    }

    @Override
    @Transactional
    public void blockFriend(Long userId, Long friendId) {
        Friend friend = friendMapper.findFriendship(userId, friendId);
        if (friend == null) {
            throw new RuntimeException("不是好友关系");
        }
        friend.setStatus("BLOCKED");
        friendMapper.updateById(friend);
        log.info("用户{}拉黑了好友{}", userId, friendId);
    }

    @Override
    public List<Map<String, Object>> searchUsers(Long userId, String keyword) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .ne(User::getId, userId)
                .and(w -> w
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword))
                .last("LIMIT 20"));

        return users.stream().map(user -> {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            item.put("username", user.getUsername());
            item.put("realName", user.getRealName());
            item.put("avatar", user.getAvatar());
            item.put("role", user.getRole());
            
            // 检查好友关系状态
            boolean isFriend = friendMapper.isFriend(userId, user.getId());
            item.put("isFriend", isFriend);
            
            // 检查是否有待处理的好友申请（我发给对方的）
            FriendRequest pendingRequest = friendRequestMapper.findExistingRequest(userId, user.getId());
            item.put("hasPendingRequest", pendingRequest != null);
            
            // 检查是否有对方发给我的申请
            FriendRequest receivedRequest = friendRequestMapper.findExistingRequest(user.getId(), userId);
            item.put("hasReceivedRequest", receivedRequest != null);
            
            return item;
        }).collect(Collectors.toList());
    }

    // ==================== 私聊相关 ====================

    @Override
    public List<ChatConversation> getConversationList(Long userId) {
        List<ChatConversation> conversations = conversationMapper.findByUserId(userId);
        
        for (ChatConversation conv : conversations) {
            if ("PRIVATE".equals(conv.getType())) {
                conv.setTargetUser(userMapper.selectById(conv.getTargetId()));
            } else if ("GROUP".equals(conv.getType())) {
                conv.setGroup(groupMapper.selectById(conv.getTargetId()));
            }
        }
        
        return conversations;
    }

    @Override
    @Transactional
    public void toggleConversationTop(Long userId, Long conversationId, Boolean isTop) {
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null && conversation.getUserId().equals(userId)) {
            conversation.setIsTop(isTop);
            conversationMapper.updateById(conversation);
            log.info("用户{}{}会话{}", userId, isTop ? "置顶" : "取消置顶", conversationId);
        }
    }

    @Override
    @Transactional
    public ChatMessage sendPrivateMessage(Long senderId, Long receiverId, String content, String type,
                                          String mediaUrl, String mediaThumbnail, Integer mediaDuration, Long mediaSize) {
        // 获取或创建会话
        ChatConversation senderConv = getOrCreatePrivateConversation(senderId, receiverId);
        ChatConversation receiverConv = getOrCreatePrivateConversation(receiverId, senderId);

        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setConversationId(senderConv.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type != null ? type : "TEXT");
        message.setMediaUrl(mediaUrl);
        message.setMediaThumbnail(mediaThumbnail);
        message.setMediaDuration(mediaDuration);
        message.setMediaSize(mediaSize);
        message.setIsRead(false);
        message.setIsRecalled(false);
        messageMapper.insert(message);

        // 更新会话
        String lastMsg = getLastMessagePreview(type, content);
        updateConversation(senderConv, lastMsg, 0);
        updateConversation(receiverConv, lastMsg, receiverConv.getUnreadCount() + 1);

        // 设置发送者信息
        message.setSender(userMapper.selectById(senderId));

        // WebSocket推送给接收方
        webSocketHandler.sendPrivateMessage(receiverId, message);

        return message;
    }

    @Override
    public IPage<ChatMessage> getPrivateMessages(Long userId, Long targetId, int page, int size) {
        Page<ChatMessage> pageParam = new Page<>(page, size);
        IPage<ChatMessage> messages = messageMapper.findMessagesBetweenUsers(pageParam, userId, targetId);
        
        // 填充发送者信息
        for (ChatMessage msg : messages.getRecords()) {
            msg.setSender(userMapper.selectById(msg.getSenderId()));
        }
        
        return messages;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long userId, Long conversationId) {
        messageMapper.markAsRead(conversationId, userId);
        
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv != null && conv.getUserId().equals(userId)) {
            conv.setUnreadCount(0);
            conversationMapper.updateById(conv);
        }
    }

    @Override
    @Transactional
    public void recallMessage(Long userId, Long messageId) {
        ChatMessage message = messageMapper.selectById(messageId);
        if (message == null || !message.getSenderId().equals(userId)) {
            throw new RuntimeException("无法撤回此消息");
        }

        // 检查是否在2分钟内
        if (message.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("消息发送超过2分钟，无法撤回");
        }

        message.setIsRecalled(true);
        message.setContent("此消息已撤回");
        messageMapper.updateById(message);

        // 通知接收方
        webSocketHandler.sendRecallNotice(message.getReceiverId(), messageId, "PRIVATE");
    }

    @Override
    @Transactional
    public ChatMessage forwardMessage(Long userId, Long messageId, Long targetId, String targetType) {
        ChatMessage original = messageMapper.selectById(messageId);
        if (original == null) {
            throw new RuntimeException("消息不存在");
        }

        User forwardFromUser = userMapper.selectById(original.getSenderId());

        if ("PRIVATE".equals(targetType)) {
            return sendPrivateMessage(userId, targetId, original.getContent(), original.getType(),
                    original.getMediaUrl(), original.getMediaThumbnail(), original.getMediaDuration(), original.getMediaSize());
        } else {
            return null; // 群聊转发在sendGroupMessage中处理
        }
    }

    // ==================== 群聊相关 ====================

    @Override
    @Transactional
    public ChatGroup createGroup(Long ownerId, String name, String avatar, List<Long> memberIds) {
        // 创建群
        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setAvatar(avatar);
        group.setOwnerId(ownerId);
        group.setMemberCount(memberIds.size() + 1);
        group.setType("NORMAL");
        group.setJoinType("APPROVAL");
        group.setIsMuted(false);
        groupMapper.insert(group);

        // 添加群主
        ChatGroupMember ownerMember = new ChatGroupMember();
        ownerMember.setGroupId(group.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole("OWNER");
        ownerMember.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(ownerMember);

        // 添加成员
        for (Long memberId : memberIds) {
            if (!memberId.equals(ownerId)) {
                ChatGroupMember member = new ChatGroupMember();
                member.setGroupId(group.getId());
                member.setUserId(memberId);
                member.setRole("MEMBER");
                member.setJoinTime(LocalDateTime.now());
                member.setInviteUserId(ownerId);
                groupMemberMapper.insert(member);

                // 为每个成员创建群会话
                createGroupConversation(memberId, group.getId());
            }
        }

        // 为群主创建群会话
        createGroupConversation(ownerId, group.getId());

        // 发送系统消息
        sendGroupMessage(group.getId(), ownerId, "创建了群聊", "SYSTEM", null, null, null, null, null, false);

        log.info("用户{}创建群聊：{}", ownerId, name);
        return group;
    }

    @Override
    public List<ChatGroup> getUserGroups(Long userId) {
        List<ChatGroupMember> memberships = groupMemberMapper.findByUserId(userId);
        List<ChatGroup> groups = new ArrayList<>();
        
        for (ChatGroupMember membership : memberships) {
            ChatGroup group = groupMapper.selectById(membership.getGroupId());
            if (group != null) {
                groups.add(group);
            }
        }
        
        return groups;
    }

    @Override
    public ChatGroup getGroupDetail(Long groupId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group != null) {
            group.setOwner(userMapper.selectById(group.getOwnerId()));
            group.setMembers(getGroupMembers(groupId));
        }
        return group;
    }

    @Override
    public List<ChatGroupMember> getGroupMembers(Long groupId) {
        List<ChatGroupMember> members = groupMemberMapper.findByGroupId(groupId);
        for (ChatGroupMember member : members) {
            member.setUser(userMapper.selectById(member.getUserId()));
        }
        return members;
    }

    @Override
    @Transactional
    public void inviteMembers(Long groupId, Long inviterId, List<Long> memberIds) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }

        // 检查是否有邀请权限
        ChatGroupMember inviter = groupMemberMapper.findMember(groupId, inviterId);
        if (inviter == null) {
            throw new RuntimeException("你不是群成员");
        }

        User inviterUser = userMapper.selectById(inviterId);
        StringBuilder names = new StringBuilder();

        for (Long memberId : memberIds) {
            if (!groupMemberMapper.isMember(groupId, memberId)) {
                ChatGroupMember member = new ChatGroupMember();
                member.setGroupId(groupId);
                member.setUserId(memberId);
                member.setRole("MEMBER");
                member.setJoinTime(LocalDateTime.now());
                member.setInviteUserId(inviterId);
                groupMemberMapper.insert(member);

                createGroupConversation(memberId, groupId);

                User user = userMapper.selectById(memberId);
                if (names.length() > 0) names.append("、");
                names.append(user.getRealName());
            }
        }

        // 更新群成员数
        group.setMemberCount((int) groupMemberMapper.countMembers(groupId));
        groupMapper.updateById(group);

        // 发送系统消息
        if (names.length() > 0) {
            sendGroupMessage(groupId, inviterId, inviterUser.getRealName() + " 邀请 " + names + " 加入了群聊",
                    "SYSTEM", null, null, null, null, null, false);
        }
    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }

        if (group.getOwnerId().equals(userId)) {
            throw new RuntimeException("群主不能退出群聊，请先转让群主或解散群聊");
        }

        groupMemberMapper.delete(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId));

        // 更新群成员数
        group.setMemberCount((int) groupMemberMapper.countMembers(groupId));
        groupMapper.updateById(group);

        // 删除会话
        conversationMapper.delete(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getTargetId, groupId)
                .eq(ChatConversation::getType, "GROUP"));

        User user = userMapper.selectById(userId);
        sendGroupMessage(groupId, userId, user.getRealName() + " 退出了群聊", "SYSTEM", null, null, null, null, null, false);
    }

    @Override
    @Transactional
    public void dismissGroup(Long groupId, Long ownerId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null || !group.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("无权限解散群聊");
        }

        // 发送解散通知
        sendGroupMessage(groupId, ownerId, "群聊已解散", "SYSTEM", null, null, null, null, null, false);

        // 删除群成员
        groupMemberMapper.delete(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId));

        // 删除会话
        conversationMapper.delete(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getTargetId, groupId)
                .eq(ChatConversation::getType, "GROUP"));

        // 删除群
        groupMapper.deleteById(groupId);

        log.info("用户{}解散了群聊{}", ownerId, groupId);
    }

    @Override
    @Transactional
    public void updateGroupInfo(Long groupId, Long userId, String name, String avatar, String announcement) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }

        ChatGroupMember member = groupMemberMapper.findMember(groupId, userId);
        if (member == null || "MEMBER".equals(member.getRole())) {
            throw new RuntimeException("无权限修改群信息");
        }

        if (name != null) group.setName(name);
        if (avatar != null) group.setAvatar(avatar);
        if (announcement != null) group.setAnnouncement(announcement);
        groupMapper.updateById(group);
    }

    @Override
    @Transactional
    public ChatGroupMessage sendGroupMessage(Long groupId, Long senderId, String content, String type,
                                             String mediaUrl, String mediaThumbnail, Integer mediaDuration, Long mediaSize,
                                             String atUserIds, boolean atAll) {
        // 检查是否是群成员
        if (!groupMemberMapper.isMember(groupId, senderId)) {
            throw new RuntimeException("你不是群成员");
        }

        // 创建消息
        ChatGroupMessage message = new ChatGroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setType(type != null ? type : "TEXT");
        message.setMediaUrl(mediaUrl);
        message.setMediaThumbnail(mediaThumbnail);
        message.setMediaDuration(mediaDuration);
        message.setMediaSize(mediaSize);
        message.setAtUserIds(atUserIds);
        message.setAtAll(atAll);
        message.setIsRecalled(false);
        message.setReadCount(0);
        groupMessageMapper.insert(message);

        // 设置发送者信息
        message.setSender(userMapper.selectById(senderId));

        // 更新所有成员的会话
        List<ChatGroupMember> members = groupMemberMapper.findByGroupId(groupId);
        Set<Long> memberIds = new HashSet<>();
        String lastMsg = getLastMessagePreview(type, content);

        for (ChatGroupMember member : members) {
            memberIds.add(member.getUserId());
            ChatConversation conv = conversationMapper.findGroupConversation(member.getUserId(), groupId);
            if (conv != null) {
                int unread = member.getUserId().equals(senderId) ? 0 : conv.getUnreadCount() + 1;
                updateConversation(conv, lastMsg, unread);
            }
        }

        // WebSocket推送给群成员
        webSocketHandler.sendGroupMessage(groupId, message, memberIds);

        return message;
    }

    @Override
    public IPage<ChatGroupMessage> getGroupMessages(Long groupId, int page, int size) {
        Page<ChatGroupMessage> pageParam = new Page<>(page, size);
        IPage<ChatGroupMessage> messages = groupMessageMapper.findByGroupId(pageParam, groupId);
        
        for (ChatGroupMessage msg : messages.getRecords()) {
            msg.setSender(userMapper.selectById(msg.getSenderId()));
        }
        
        return messages;
    }

    @Override
    @Transactional
    public void setGroupAdmin(Long groupId, Long ownerId, Long userId, boolean isAdmin) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null || !group.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("无权限操作");
        }

        ChatGroupMember member = groupMemberMapper.findMember(groupId, userId);
        if (member == null) {
            throw new RuntimeException("用户不是群成员");
        }

        member.setRole(isAdmin ? "ADMIN" : "MEMBER");
        groupMemberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void kickMember(Long groupId, Long operatorId, Long targetUserId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }

        ChatGroupMember operator = groupMemberMapper.findMember(groupId, operatorId);
        if (operator == null || "MEMBER".equals(operator.getRole())) {
            throw new RuntimeException("无权限操作");
        }

        if (group.getOwnerId().equals(targetUserId)) {
            throw new RuntimeException("不能踢出群主");
        }

        groupMemberMapper.delete(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, targetUserId));

        group.setMemberCount((int) groupMemberMapper.countMembers(groupId));
        groupMapper.updateById(group);

        User targetUser = userMapper.selectById(targetUserId);
        sendGroupMessage(groupId, operatorId, targetUser.getRealName() + " 被移出了群聊", "SYSTEM", null, null, null, null, null, false);
    }

    @Override
    @Transactional
    public void muteMember(Long groupId, Long operatorId, Long targetUserId, Integer minutes) {
        ChatGroupMember operator = groupMemberMapper.findMember(groupId, operatorId);
        if (operator == null || "MEMBER".equals(operator.getRole())) {
            throw new RuntimeException("无权限操作");
        }

        ChatGroupMember target = groupMemberMapper.findMember(groupId, targetUserId);
        if (target == null) {
            throw new RuntimeException("用户不是群成员");
        }

        if (minutes > 0) {
            target.setIsMuted(true);
            target.setMuteEndTime(LocalDateTime.now().plusMinutes(minutes));
        } else {
            target.setIsMuted(false);
            target.setMuteEndTime(null);
        }
        groupMemberMapper.updateById(target);
    }

    // ==================== 表情相关 ====================

    @Override
    public List<Map<String, Object>> getEmojiPacks() {
        List<Map<String, Object>> result = new ArrayList<>();
        // 这里简化处理，实际应该查询emoji_pack表
        result.add(Map.of("id", 1L, "name", "抖音热门", "cover", "/emojis/douyin/cover.png"));
        result.add(Map.of("id", 2L, "name", "经典表情", "cover", "/emojis/classic/cover.png"));
        return result;
    }

    @Override
    public List<Emoji> getEmojisByPack(Long packId) {
        return emojiMapper.findByPackId(packId);
    }

    @Override
    public List<Emoji> getPopularEmojis(int limit) {
        return emojiMapper.findPopular(limit);
    }

    // ==================== 统计相关 ====================

    @Override
    public int getTotalUnreadCount(Long userId) {
        return conversationMapper.getTotalUnreadCount(userId);
    }

    // ==================== 私有方法 ====================

    private ChatConversation getOrCreatePrivateConversation(Long userId, Long targetId) {
        ChatConversation conv = conversationMapper.findPrivateConversation(userId, targetId);
        if (conv == null) {
            conv = new ChatConversation();
            conv.setUserId(userId);
            conv.setTargetId(targetId);
            conv.setType("PRIVATE");
            conv.setUnreadCount(0);
            conv.setIsTop(false);
            conv.setIsMuted(false);
            conversationMapper.insert(conv);
        }
        return conv;
    }

    private void createGroupConversation(Long userId, Long groupId) {
        ChatConversation conv = conversationMapper.findGroupConversation(userId, groupId);
        if (conv == null) {
            conv = new ChatConversation();
            conv.setUserId(userId);
            conv.setTargetId(groupId);
            conv.setType("GROUP");
            conv.setUnreadCount(0);
            conv.setIsTop(false);
            conv.setIsMuted(false);
            conversationMapper.insert(conv);
        }
    }

    private void updateConversation(ChatConversation conv, String lastMessage, int unreadCount) {
        conv.setLastMessage(lastMessage);
        conv.setLastMessageTime(LocalDateTime.now());
        conv.setUnreadCount(unreadCount);
        conversationMapper.updateById(conv);
    }

    private String getLastMessagePreview(String type, String content) {
        if ("TEXT".equals(type)) {
            return content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content;
        } else if ("IMAGE".equals(type)) {
            return "[图片]";
        } else if ("VIDEO".equals(type)) {
            return "[视频]";
        } else if ("VOICE".equals(type)) {
            return "[语音]";
        } else if ("FILE".equals(type)) {
            return "[文件]";
        } else if ("EMOJI".equals(type)) {
            return "[表情]";
        } else if ("SYSTEM".equals(type)) {
            return content;
        } else {
            return "[消息]";
        }
    }
}
