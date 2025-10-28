package com.qasystem.service;

import com.qasystem.dto.AnswerDTO;
import com.qasystem.dto.CreateAnswerRequest;

import java.util.List;

/**
 * 回答服务接口
 */
public interface AnswerService {

    /**
     * 根据问题ID获取所有回答
     */
    List<AnswerDTO> getAnswersByQuestionId(Long questionId);

    /**
     * 创建回答
     */
    AnswerDTO createAnswer(Long teacherId, CreateAnswerRequest request);

    /**
     * 更新回答
     */
    AnswerDTO updateAnswer(Long id, Long teacherId, CreateAnswerRequest request);

    /**
     * 删除回答
     */
    void deleteAnswer(Long id, Long userId);

    /**
     * 采纳回答
     */
    void acceptAnswer(Long id, Long studentId);
}

