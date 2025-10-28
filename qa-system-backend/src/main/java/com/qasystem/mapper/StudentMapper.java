package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 学生Mapper
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 根据用户ID查询学生信息
     */
    default Optional<Student> findByUserId(Long userId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId))
        );
    }

    /**
     * 根据学号查询学生信息
     */
    default Optional<Student> findByStudentNo(String studentNo) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo))
        );
    }
}

