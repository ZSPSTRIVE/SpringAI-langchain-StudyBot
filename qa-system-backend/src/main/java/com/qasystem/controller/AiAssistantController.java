package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.dto.ConversationFeedbackRequest;
import com.qasystem.entity.AiConversation;
import com.qasystem.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI助手控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {
    
    private final AiAssistantService aiAssistantService;
    
    /**
     * AI聊天对话
     */
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request, 
                                       Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("用户 {} 发起AI对话: {}", userId, request.getMessage());
        
        AiChatResponse response = aiAssistantService.chat(userId, request);
        return Result.success(response);
    }
    
    /**
     * 获取用户的会话列表
     */
    @GetMapping("/sessions")
    public Result<List<AiConversation>> getUserSessions(@RequestParam(defaultValue = "20") Integer limit,
                                                        Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AiConversation> sessions = aiAssistantService.getUserSessions(userId, limit);
        return Result.success(sessions);
    }
    
    /**
     * 获取会话历史
     */
    @GetMapping("/sessions/{sessionId}/history")
    public Result<List<AiConversation>> getSessionHistory(@PathVariable String sessionId,
                                                          Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AiConversation> history = aiAssistantService.getSessionHistory(userId, sessionId);
        return Result.success(history);
    }
    
    /**
     * 提交反馈
     */
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@Valid @RequestBody ConversationFeedbackRequest request,
                                       Authentication authentication) {
        getUserId(authentication); // 验证登录
        aiAssistantService.submitFeedback(request.getConversationId(), request.getFeedback());
        return Result.success();
    }
    
    /**
     * 收藏/取消收藏对话
     */
    @PostMapping("/bookmark/{conversationId}")
    public Result<Void> bookmarkConversation(@PathVariable Long conversationId,
                                            @RequestParam Boolean isBookmarked,
                                            Authentication authentication) {
        getUserId(authentication); // 验证登录
        aiAssistantService.bookmarkConversation(conversationId, isBookmarked);
        return Result.success();
    }
    
    /**
     * 获取收藏的对话
     */
    @GetMapping("/bookmarks")
    public Result<List<AiConversation>> getBookmarkedConversations(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AiConversation> bookmarks = aiAssistantService.getBookmarkedConversations(userId);
        return Result.success(bookmarks);
    }
    
    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        return Long.parseLong(authentication.getName());
    }
}
