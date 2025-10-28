package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 关注Mapper
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 查询是否已关注
     */
    default Optional<Follow> findByFollowerAndFollowee(Long followerId, Long followeeId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId))
        );
    }

    /**
     * 查询用户关注的所有教师
     */
    default List<Follow> findByFollower(Long followerId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .orderByDesc(Follow::getCreateTime));
    }

    /**
     * 查询关注某教师的所有学生
     */
    default List<Follow> findByFollowee(Long followeeId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                .eq(Follow::getFolloweeId, followeeId)
                .orderByDesc(Follow::getCreateTime));
    }
}

