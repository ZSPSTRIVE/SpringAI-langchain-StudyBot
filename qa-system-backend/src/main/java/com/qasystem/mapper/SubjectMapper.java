package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Subject;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 科目Mapper
 */
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    /**
     * 查询所有启用的科目（按排序）
     */
    default List<Subject> findAllActive() {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Subject>()
                .eq(Subject::getStatus, "ACTIVE")
                .orderByAsc(Subject::getSortOrder, Subject::getId));
    }
}

