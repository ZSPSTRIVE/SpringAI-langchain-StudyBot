package com.qasystem.service.impl;

import com.qasystem.dto.QuestionDTO;
import com.qasystem.entity.UserCollection;
import com.qasystem.mapper.CollectionMapper;
import com.qasystem.mapper.QuestionMapper;
import com.qasystem.service.CollectionService;
import com.qasystem.service.impl.QuestionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionMapper collectionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionServiceImpl questionService;

    @Override
    @Transactional
    public void collect(Long userId, String targetType, Long targetId) {
        // 检查是否已收藏
        if (collectionMapper.findByUserAndTarget(userId, targetType, targetId).isPresent()) {
            throw new RuntimeException("已经收藏过了");
        }

        // 创建收藏记录
        UserCollection collection = new UserCollection();
        collection.setUserId(userId);
        collection.setTargetType(targetType);
        collection.setTargetId(targetId);
        collectionMapper.insert(collection);

        log.info("收藏成功: userId={}, type={}, targetId={}", userId, targetType, targetId);
    }

    @Override
    @Transactional
    public void uncollect(Long userId, String targetType, Long targetId) {
        UserCollection collection = collectionMapper.findByUserAndTarget(userId, targetType, targetId)
                .orElseThrow(() -> new RuntimeException("未收藏"));

        collectionMapper.deleteById(collection.getId());

        log.info("取消收藏成功: userId={}, type={}, targetId={}", userId, targetType, targetId);
    }

    @Override
    public boolean isCollected(Long userId, String targetType, Long targetId) {
        return collectionMapper.findByUserAndTarget(userId, targetType, targetId).isPresent();
    }

    @Override
    public List<QuestionDTO> getCollectedQuestions(Long userId) {
        List<UserCollection> collections = collectionMapper.findByUserAndType(userId, "QUESTION");
        
        return collections.stream()
                .map(collection -> {
                    try {
                        return questionService.getQuestionById(collection.getTargetId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public long getCollectionCount(Long userId) {
        return collectionMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCollection>()
                .eq(UserCollection::getUserId, userId)
        );
    }
}

