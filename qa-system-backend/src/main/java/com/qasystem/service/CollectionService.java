package com.qasystem.service;

import com.qasystem.dto.QuestionDTO;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface CollectionService {

    /**
     * 收藏
     */
    void collect(Long userId, String targetType, Long targetId);

    /**
     * 取消收藏
     */
    void uncollect(Long userId, String targetType, Long targetId);

    /**
     * 检查是否已收藏
     */
    boolean isCollected(Long userId, String targetType, Long targetId);

    /**
     * 获取用户收藏的问题列表
     */
    List<QuestionDTO> getCollectedQuestions(Long userId);

    /**
     * 获取收藏数量
     */
    long getCollectionCount(Long userId);
}

