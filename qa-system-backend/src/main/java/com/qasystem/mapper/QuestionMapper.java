package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 问题Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 分页查询问题（带学生和科目信息）
     */
    IPage<Question> selectQuestionPage(Page<Question> page, 
                                       @Param("subjectId") Long subjectId,
                                       @Param("status") String status,
                                       @Param("keyword") String keyword);
}

