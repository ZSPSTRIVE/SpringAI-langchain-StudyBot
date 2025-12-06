package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.entity.Friend;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 好友关系Mapper
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    /**
     * 获取用户的好友列表
     */
    default List<Friend> findFriendsByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getStatus, "ACCEPTED")
                .orderByDesc(Friend::getUpdateTime));
    }

    /**
     * 检查是否是好友关系
     */
    default boolean isFriend(Long userId, Long friendId) {
        return selectCount(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId)
                .eq(Friend::getStatus, "ACCEPTED")) > 0;
    }

    /**
     * 获取好友关系
     */
    default Friend findFriendship(Long userId, Long friendId) {
        return selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId));
    }
}
