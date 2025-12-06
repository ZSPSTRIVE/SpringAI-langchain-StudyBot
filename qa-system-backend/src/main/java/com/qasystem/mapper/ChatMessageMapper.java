package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 私聊消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 分页获取会话消息
     */
    default IPage<ChatMessage> findByConversationId(Page<ChatMessage> page, Long conversationId) {
        return selectPage(page, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getIsRecalled, false)
                .orderByDesc(ChatMessage::getCreateTime));
    }

    /**
     * 获取两人之间的消息
     */
    default IPage<ChatMessage> findMessagesBetweenUsers(Page<ChatMessage> page, Long userId1, Long userId2) {
        return selectPage(page, new LambdaQueryWrapper<ChatMessage>()
                .and(w -> w
                        .and(inner -> inner.eq(ChatMessage::getSenderId, userId1).eq(ChatMessage::getReceiverId, userId2))
                        .or(inner -> inner.eq(ChatMessage::getSenderId, userId2).eq(ChatMessage::getReceiverId, userId1)))
                .eq(ChatMessage::getIsRecalled, false)
                .orderByDesc(ChatMessage::getCreateTime));
    }

    /**
     * 标记消息为已读
     */
    default int markAsRead(Long conversationId, Long receiverId) {
        ChatMessage message = new ChatMessage();
        message.setIsRead(true);
        return update(message, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getReceiverId, receiverId)
                .eq(ChatMessage::getIsRead, false));
    }

    /**
     * 获取未读消息数
     */
    default long countUnread(Long conversationId, Long receiverId) {
        return selectCount(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getReceiverId, receiverId)
                .eq(ChatMessage::getIsRead, false));
    }
}
