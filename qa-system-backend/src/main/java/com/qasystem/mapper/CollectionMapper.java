package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.UserCollection;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 收藏Mapper
 */
@Mapper
public interface CollectionMapper extends BaseMapper<UserCollection> {

    /**
     * 查询是否已收藏
     */
    default Optional<UserCollection> findByUserAndTarget(Long userId, String targetType, Long targetId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCollection>()
                .eq(UserCollection::getUserId, userId)
                .eq(UserCollection::getTargetType, targetType)
                .eq(UserCollection::getTargetId, targetId))
        );
    }

    /**
     * 查询用户的所有收藏
     */
    default List<UserCollection> findByUserAndType(Long userId, String targetType) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCollection>()
                .eq(UserCollection::getUserId, userId)
                .eq(targetType != null, UserCollection::getTargetType, targetType)
                .orderByDesc(UserCollection::getCreateTime));
    }
}

