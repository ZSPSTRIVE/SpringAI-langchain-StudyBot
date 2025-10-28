package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.common.response.Result;
import com.qasystem.dto.CreateQuestionRequest;
import com.qasystem.dto.QuestionDTO;
import com.qasystem.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 问题控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 分页查询问题
     */
    @GetMapping
    public Result<IPage<QuestionDTO>> getQuestionPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        log.info("分页查询问题: page={}, size={}, subjectId={}, status={}, keyword={}", 
                 page, size, subjectId, status, keyword);
        
        IPage<QuestionDTO> result = questionService.getQuestionPage(page, size, subjectId, status, keyword);
        return Result.success(result);
    }

    /**
     * 获取问题详情
     */
    @GetMapping("/{id}")
    public Result<QuestionDTO> getQuestionById(@PathVariable Long id) {
        log.info("获取问题详情: id={}", id);
        
        // 增加浏览次数
        questionService.incrementViewCount(id);
        
        QuestionDTO question = questionService.getQuestionById(id);
        return Result.success(question);
    }

    /**
     * 创建问题 (学生)
     */
    @PostMapping
    public Result<QuestionDTO> createQuestion(
            Authentication authentication,
            @Valid @RequestBody CreateQuestionRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("创建问题: userId={}, title={}", userId, request.getTitle());
        
        QuestionDTO created = questionService.createQuestion(userId, request);
        return Result.success("提问成功", created);
    }

    /**
     * 更新问题 (学生)
     */
    @PutMapping("/{id}")
    public Result<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody CreateQuestionRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("更新问题: id={}, userId={}", id, userId);
        
        QuestionDTO updated = questionService.updateQuestion(id, userId, request);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除问题 (学生本人或管理员)
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("删除问题: id={}, userId={}", id, userId);
        
        questionService.deleteQuestion(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 关闭问题 (学生)
     */
    @PutMapping("/{id}/close")
    public Result<Void> closeQuestion(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("关闭问题: id={}, userId={}", id, userId);
        
        questionService.closeQuestion(id, userId);
        return Result.success("问题已关闭", null);
    }
}

