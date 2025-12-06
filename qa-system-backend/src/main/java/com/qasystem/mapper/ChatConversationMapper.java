package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    /**
     * 获取用户的会话列表
     */
    default List<ChatConversation> findByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .orderByDesc(ChatConversation::getIsTop)
                .orderByDesc(ChatConversation::getLastMessageTime));
    }

    /**
     * 获取私聊会话
     */
    default ChatConversation findPrivateConversation(Long userId, Long targetId) {
        return selectOne(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getTargetId, targetId)
                .eq(ChatConversation::getType, "PRIVATE"));
    }

    /**
     * 获取群聊会话
     */
    default ChatConversation findGroupConversation(Long userId, Long groupId) {
        return selectOne(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getTargetId, groupId)
                .eq(ChatConversation::getType, "GROUP"));
    }

    /**
     * 获取用户总未读数
     */
    default int getTotalUnreadCount(Long userId) {
        List<ChatConversation> list = selectList(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .gt(ChatConversation::getUnreadCount, 0));
        return list.stream().mapToInt(ChatConversation::getUnreadCount).sum();
    }
}
