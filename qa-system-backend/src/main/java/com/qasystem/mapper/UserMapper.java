package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username))
        );
    }

    /**
     * 根据邮箱查询用户
     */
    default Optional<User> findByEmail(String email) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email))
        );
    }
}

