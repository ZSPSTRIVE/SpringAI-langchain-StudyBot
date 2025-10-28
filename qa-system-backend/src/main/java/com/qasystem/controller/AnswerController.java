package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.AnswerDTO;
import com.qasystem.dto.CreateAnswerRequest;
import com.qasystem.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 回答控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    /**
     * 根据问题ID获取所有回答
     */
    @GetMapping("/question/{questionId}")
    public Result<List<AnswerDTO>> getAnswersByQuestionId(@PathVariable Long questionId) {
        log.info("获取问题的所有回答: questionId={}", questionId);
        List<AnswerDTO> answers = answerService.getAnswersByQuestionId(questionId);
        return Result.success(answers);
    }

    /**
     * 创建回答 (教师)
     */
    @PostMapping
    public Result<AnswerDTO> createAnswer(
            Authentication authentication,
            @Valid @RequestBody CreateAnswerRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("创建回答: userId={}, questionId={}", userId, request.getQuestionId());
        
        AnswerDTO created = answerService.createAnswer(userId, request);
        return Result.success("回答成功", created);
    }

    /**
     * 更新回答 (教师)
     */
    @PutMapping("/{id}")
    public Result<AnswerDTO> updateAnswer(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody CreateAnswerRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("更新回答: id={}, userId={}", id, userId);
        
        AnswerDTO updated = answerService.updateAnswer(id, userId, request);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除回答 (教师本人或管理员)
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnswer(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("删除回答: id={}, userId={}", id, userId);
        
        answerService.deleteAnswer(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 采纳回答 (学生)
     */
    @PutMapping("/{id}/accept")
    public Result<Void> acceptAnswer(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("采纳回答: id={}, userId={}", id, userId);
        
        answerService.acceptAnswer(id, userId);
        return Result.success("采纳成功", null);
    }
}

