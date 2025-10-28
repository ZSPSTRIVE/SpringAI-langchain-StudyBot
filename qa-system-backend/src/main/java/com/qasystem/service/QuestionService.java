package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.dto.CreateQuestionRequest;
import com.qasystem.dto.QuestionDTO;

/**
 * 问题服务接口
 */
public interface QuestionService {

    /**
     * 分页查询问题
     */
    IPage<QuestionDTO> getQuestionPage(Integer page, Integer size, Long subjectId, String status, String keyword);

    /**
     * 根据ID获取问题详情
     */
    QuestionDTO getQuestionById(Long id);

    /**
     * 创建问题
     */
    QuestionDTO createQuestion(Long studentId, CreateQuestionRequest request);

    /**
     * 更新问题
     */
    QuestionDTO updateQuestion(Long id, Long studentId, CreateQuestionRequest request);

    /**
     * 删除问题
     */
    void deleteQuestion(Long id, Long userId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 关闭问题
     */
    void closeQuestion(Long id, Long studentId);
}

