package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.ChatGroupMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群消息Mapper
 */
@Mapper
public interface ChatGroupMessageMapper extends BaseMapper<ChatGroupMessage> {

    /**
     * 分页获取群消息
     */
    default IPage<ChatGroupMessage> findByGroupId(Page<ChatGroupMessage> page, Long groupId) {
        return selectPage(page, new LambdaQueryWrapper<ChatGroupMessage>()
                .eq(ChatGroupMessage::getGroupId, groupId)
                .eq(ChatGroupMessage::getIsRecalled, false)
                .orderByDesc(ChatGroupMessage::getCreateTime));
    }

    /**
     * 获取群最新消息
     */
    default ChatGroupMessage findLatestMessage(Long groupId) {
        return selectOne(new LambdaQueryWrapper<ChatGroupMessage>()
                .eq(ChatGroupMessage::getGroupId, groupId)
                .eq(ChatGroupMessage::getIsRecalled, false)
                .orderByDesc(ChatGroupMessage::getCreateTime)
                .last("LIMIT 1"));
    }
}
