package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 回答Mapper
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {

    /**
     * 根据问题ID查询所有回答
     */
    default List<Answer> findByQuestionId(Long questionId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Answer>()
                .eq(Answer::getQuestionId, questionId)
                .orderByDesc(Answer::getIsAccepted)
                .orderByDesc(Answer::getLikeCount)
                .orderByDesc(Answer::getCreateTime));
    }
}

