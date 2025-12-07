package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 聊天服务接口
 */
public interface ChatService {

    // ==================== 在线状态 ====================
    
    /**
     * 获取在线用户ID列表
     */
    Set<Long> getOnlineUserIds();

    // ==================== 好友相关 ====================
    
    /**
     * 发送好友申请
     */
    void sendFriendRequest(Long fromUserId, Long toUserId, String message);

    /**
     * 处理好友申请
     */
    void handleFriendRequest(Long requestId, Long userId, boolean accept);

    /**
     * 获取好友列表
     */
    List<Map<String, Object>> getFriendList(Long userId);

    /**
     * 获取好友申请列表
     */
    List<FriendRequest> getFriendRequests(Long userId);

    /**
     * 获取待处理申请数量
     */
    long getPendingRequestCount(Long userId);

    /**
     * 删除好友
     */
    void deleteFriend(Long userId, Long friendId);

    /**
     * 获取好友详情
     */
    Map<String, Object> getFriendDetail(Long userId, Long friendId);

    /**
     * 修改好友备注
     */
    void updateFriendRemark(Long userId, Long friendId, String remark);

    /**
     * 设置好友分组
     */
    void setFriendGroup(Long userId, Long friendId, String groupName);

    /**
     * 拉黑好友
     */
    void blockFriend(Long userId, Long friendId);

    /**
     * 搜索用户（添加好友时）
     */
    List<Map<String, Object>> searchUsers(Long userId, String keyword);

    // ==================== 私聊相关 ====================

    /**
     * 获取会话列表
     */
    List<ChatConversation> getConversationList(Long userId);

    /**
     * 会话置顶/取消置顶
     */
    void toggleConversationTop(Long userId, Long conversationId, Boolean isTop);

    /**
     * 发送私聊消息
     */
    ChatMessage sendPrivateMessage(Long senderId, Long receiverId, String content, String type, 
                                   String mediaUrl, String mediaThumbnail, Integer mediaDuration, Long mediaSize);

    /**
     * 获取私聊消息记录
     */
    IPage<ChatMessage> getPrivateMessages(Long userId, Long targetId, int page, int size);

    /**
     * 标记消息已读
     */
    void markMessagesAsRead(Long userId, Long conversationId);

    /**
     * 撤回消息
     */
    void recallMessage(Long userId, Long messageId);

    /**
     * 转发消息
     */
    ChatMessage forwardMessage(Long userId, Long messageId, Long targetId, String targetType);

    // ==================== 群聊相关 ====================

    /**
     * 创建群聊
     */
    ChatGroup createGroup(Long ownerId, String name, String avatar, List<Long> memberIds);

    /**
     * 获取用户的群列表
     */
    List<ChatGroup> getUserGroups(Long userId);

    /**
     * 获取群详情
     */
    ChatGroup getGroupDetail(Long groupId);

    /**
     * 获取群成员
     */
    List<ChatGroupMember> getGroupMembers(Long groupId);

    /**
     * 邀请成员加入群
     */
    void inviteMembers(Long groupId, Long inviterId, List<Long> memberIds);

    /**
     * 退出群聊
     */
    void leaveGroup(Long groupId, Long userId);

    /**
     * 解散群聊
     */
    void dismissGroup(Long groupId, Long ownerId);

    /**
     * 修改群信息
     */
    void updateGroupInfo(Long groupId, Long userId, String name, String avatar, String announcement);

    /**
     * 发送群消息
     */
    ChatGroupMessage sendGroupMessage(Long groupId, Long senderId, String content, String type,
                                      String mediaUrl, String mediaThumbnail, Integer mediaDuration, Long mediaSize,
                                      String atUserIds, boolean atAll);

    /**
     * 获取群消息记录
     */
    IPage<ChatGroupMessage> getGroupMessages(Long groupId, int page, int size);

    /**
     * 设置/取消管理员
     */
    void setGroupAdmin(Long groupId, Long ownerId, Long userId, boolean isAdmin);

    /**
     * 踢出群成员
     */
    void kickMember(Long groupId, Long operatorId, Long targetUserId);

    /**
     * 禁言/解除禁言
     */
    void muteMember(Long groupId, Long operatorId, Long targetUserId, Integer minutes);

    /**
     * 转让群主
     */
    void transferGroupOwner(Long groupId, Long currentOwnerId, Long newOwnerId);

    /**
     * 获取我的群聊列表（包含角色信息）
     */
    List<Map<String, Object>> getMyGroupsWithRole(Long userId);

    // ==================== 表情相关 ====================

    /**
     * 获取表情包列表
     */
    List<Map<String, Object>> getEmojiPacks();

    /**
     * 获取表情包内的表情
     */
    List<Emoji> getEmojisByPack(Long packId);

    /**
     * 获取热门表情
     */
    List<Emoji> getPopularEmojis(int limit);

    // ==================== 统计相关 ====================

    /**
     * 获取未读消息总数
     */
    int getTotalUnreadCount(Long userId);
}
