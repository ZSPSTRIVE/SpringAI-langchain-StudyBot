package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 教师Mapper
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    /**
     * 根据用户ID查询教师信息
     */
    default Optional<Teacher> findByUserId(Long userId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, userId))
        );
    }

    /**
     * 根据工号查询教师信息
     */
    default Optional<Teacher> findByTeacherNo(String teacherNo) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getTeacherNo, teacherNo))
        );
    }
}

