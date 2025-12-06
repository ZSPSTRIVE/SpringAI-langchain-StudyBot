package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.entity.ChatGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 群聊Mapper
 */
@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    /**
     * 根据群主查找群
     */
    default List<ChatGroup> findByOwnerId(Long ownerId) {
        return selectList(new LambdaQueryWrapper<ChatGroup>()
                .eq(ChatGroup::getOwnerId, ownerId)
                .orderByDesc(ChatGroup::getCreateTime));
    }

    /**
     * 搜索群
     */
    default List<ChatGroup> searchByName(String keyword) {
        return selectList(new LambdaQueryWrapper<ChatGroup>()
                .like(ChatGroup::getName, keyword)
                .eq(ChatGroup::getJoinType, "FREE")
                .orderByDesc(ChatGroup::getMemberCount));
    }
}
