package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.QuestionDTO;
import com.qasystem.service.CollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 收藏
     */
    @PostMapping
    public Result<Void> collect(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("收藏: userId={}, type={}, targetId={}", userId, targetType, targetId);
        
        collectionService.collect(userId, targetType, targetId);
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public Result<Void> uncollect(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("取消收藏: userId={}, type={}, targetId={}", userId, targetType, targetId);
        
        collectionService.uncollect(userId, targetType, targetId);
        return Result.success("取消收藏成功", null);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkCollected(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isCollected = collectionService.isCollected(userId, targetType, targetId);
        return Result.success(isCollected);
    }

    /**
     * 获取收藏的问题列表
     */
    @GetMapping("/questions")
    public Result<List<QuestionDTO>> getCollectedQuestions(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("获取收藏列表: userId={}", userId);
        
        List<QuestionDTO> questions = collectionService.getCollectedQuestions(userId);
        return Result.success(questions);
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    public Result<Long> getCollectionCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = collectionService.getCollectionCount(userId);
        return Result.success(count);
    }
}

