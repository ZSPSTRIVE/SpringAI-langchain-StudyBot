package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 好友申请Mapper
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    /**
     * 获取用户收到的好友申请
     */
    default List<FriendRequest> findReceivedRequests(Long userId) {
        return selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getToUserId, userId)
                .orderByDesc(FriendRequest::getCreateTime));
    }

    /**
     * 获取用户发送的好友申请
     */
    default List<FriendRequest> findSentRequests(Long userId) {
        return selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getFromUserId, userId)
                .orderByDesc(FriendRequest::getCreateTime));
    }

    /**
     * 获取待处理的申请数量
     */
    default long countPendingRequests(Long userId) {
        return selectCount(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, "PENDING"));
    }

    /**
     * 检查是否已有申请记录
     */
    default FriendRequest findExistingRequest(Long fromUserId, Long toUserId) {
        return selectOne(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getFromUserId, fromUserId)
                .eq(FriendRequest::getToUserId, toUserId)
                .eq(FriendRequest::getStatus, "PENDING"));
    }
}
