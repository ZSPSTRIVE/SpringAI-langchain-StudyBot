package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.entity.ChatGroupMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 群成员Mapper
 */
@Mapper
public interface ChatGroupMemberMapper extends BaseMapper<ChatGroupMember> {

    /**
     * 获取群成员列表
     */
    default List<ChatGroupMember> findByGroupId(Long groupId) {
        return selectList(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .orderByAsc(ChatGroupMember::getRole)
                .orderByAsc(ChatGroupMember::getJoinTime));
    }

    /**
     * 获取用户加入的群
     */
    default List<ChatGroupMember> findByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getUserId, userId)
                .orderByDesc(ChatGroupMember::getIsTop)
                .orderByDesc(ChatGroupMember::getJoinTime));
    }

    /**
     * 获取群成员
     */
    default ChatGroupMember findMember(Long groupId, Long userId) {
        return selectOne(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId));
    }

    /**
     * 检查是否是群成员
     */
    default boolean isMember(Long groupId, Long userId) {
        return selectCount(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId)) > 0;
    }

    /**
     * 获取群管理员列表
     */
    default List<ChatGroupMember> findAdmins(Long groupId) {
        return selectList(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .in(ChatGroupMember::getRole, "OWNER", "ADMIN"));
    }

    /**
     * 获取群成员数量
     */
    default long countMembers(Long groupId) {
        return selectCount(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId));
    }
}
