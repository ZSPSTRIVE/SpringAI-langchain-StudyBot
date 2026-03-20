package com.qasystem.controller;

import com.qasystem.ai.rag.InterviewRoute;
import com.qasystem.ai.rag.InterviewScene;
import com.qasystem.ai.rag.InterviewSceneRouter;
import com.qasystem.common.response.Result;
import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.dto.ConversationFeedbackRequest;
import com.qasystem.dto.InterviewSceneOptionResponse;
import com.qasystem.dto.RenameSessionRequest;
import com.qasystem.entity.AiConversation;
import com.qasystem.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final InterviewSceneRouter interviewSceneRouter;

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request,
                                       Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("User {} start AI chat. message={}", userId, request.getMessage());
        return Result.success(aiAssistantService.chat(userId, request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequest request,
                                 Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("User {} start AI streaming chat. message={}", userId, request.getMessage());
        return aiAssistantService.chatStream(userId, request);
    }

    @GetMapping("/interview/scenes")
    public Result<List<InterviewSceneOptionResponse>> interviewScenes() {
        return Result.success(Arrays.stream(InterviewScene.values())
                .map(this::toSceneOption)
                .toList());
    }

    @GetMapping("/sessions")
    public Result<List<AiConversation>> getUserSessions(@RequestParam(defaultValue = "20") Integer limit,
                                                        Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(aiAssistantService.getUserSessions(userId, limit));
    }

    @GetMapping("/sessions/{sessionId}/history")
    public Result<List<AiConversation>> getSessionHistory(@PathVariable String sessionId,
                                                          Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(aiAssistantService.getSessionHistory(userId, sessionId));
    }

    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@Valid @RequestBody ConversationFeedbackRequest request,
                                       Authentication authentication) {
        Long userId = getUserId(authentication);
        aiAssistantService.submitFeedback(userId, request.getConversationId(), request.getFeedback());
        return Result.success();
    }

    @PostMapping("/bookmark/{conversationId}")
    public Result<Void> bookmarkConversation(@PathVariable Long conversationId,
                                             @RequestParam Boolean isBookmarked,
                                             Authentication authentication) {
        Long userId = getUserId(authentication);
        aiAssistantService.bookmarkConversation(userId, conversationId, isBookmarked);
        return Result.success();
    }

    @GetMapping("/bookmarks")
    public Result<List<AiConversation>> getBookmarkedConversations(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(aiAssistantService.getBookmarkedConversations(userId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId,
                                      Authentication authentication) {
        Long userId = getUserId(authentication);
        aiAssistantService.deleteSession(userId, sessionId);
        return Result.success();
    }

    @PutMapping("/sessions/{sessionId}/rename")
    public Result<Void> renameSession(@PathVariable String sessionId,
                                      @RequestBody RenameSessionRequest request,
                                      Authentication authentication) {
        Long userId = getUserId(authentication);
        aiAssistantService.renameSession(userId, sessionId, request.getTitle());
        return Result.success();
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        return Long.parseLong(authentication.getName());
    }

    private InterviewSceneOptionResponse toSceneOption(InterviewScene scene) {
        InterviewRoute route = interviewSceneRouter.route("mock", scene.getCode());
        return InterviewSceneOptionResponse.builder()
                .code(scene.getCode())
                .displayName(scene.getDisplayName())
                .retrievalMode(route.retrievalMode().name())
                .useMilvus(route.useMilvus())
                .useKeyword(route.useKeyword())
                .description(interviewSceneRouter.buildAnswerDirective(scene))
                .build();
    }
}
