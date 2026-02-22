package com.qasystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.ai.QuestionCategory;
import com.qasystem.ai.rag.RagContextResult;
import com.qasystem.ai.rag.RagPipelineService;
import com.qasystem.config.AiAssistantProperties;
import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.entity.AiConversation;
import com.qasystem.mapper.AiConversationMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 对话主服务：对话编排、RAG 接入、会话持久化与安全控制。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService extends ServiceImpl<AiConversationMapper, AiConversation> {

    private static final String CACHE_KEY_PREFIX = "ai:conversation:";
    private static final int CACHE_EXPIRE_HOURS = 24;
    private static final int DEFAULT_SESSION_LIMIT = 20;
    private static final int MAX_SESSION_LIMIT = 100;

    private static final String METRIC_AI_REQUEST_LATENCY_MS = "qa_ai_request_latency_ms";
    private static final String METRIC_AI_TOKENS_TOTAL = "qa_ai_tokens_total";
    private static final String METRIC_RAG_RECALL_COUNT = "qa_rag_recall_count";
    private static final String METRIC_RAG_HIT_RATE = "qa_rag_hit_rate";
    private static final String METRIC_RAG_NO_CONTEXT_RATE = "qa_rag_no_context_rate";

    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AiConversationMapper conversationMapper;
    private final RagPipelineService ragPipelineService;
    private final AiAssistantProperties aiAssistantProperties;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    private final Map<QuestionCategory, List<AiAssistantProperties.RecommendationItem>> fallbackRecommendationRules =
            buildFallbackRecommendationRules();

    public AiChatResponse chat(Long userId, AiChatRequest request) {
        long startNanos = System.nanoTime();
        String message = normalizeMessage(request.getMessage());
        String sessionId = resolveSessionId(request.getSessionId());
        QuestionCategory category = QuestionCategory.detect(message);
        RagContextResult ragContext = ragPipelineService.buildContext(message);

        int tokensUsed = 0;
        String aiResponse;

        try {
            List<ChatMessage> messages = buildConversationHistory(userId, sessionId, message, ragContext.context());
            Response<AiMessage> response = generateWithRetry(messages);
            aiResponse = safeModelResponse(response);
            tokensUsed = extractTokenUsage(response);
        } catch (Exception ex) {
            log.error("AI chat failed, use degraded response. userId={}, sessionId={}", userId, sessionId, ex);
            aiResponse = buildFallbackAnswer(ragContext);
        }

        aiResponse = appendCitations(aiResponse, ragContext);

        List<AiChatResponse.ResourceRecommendation> recommendations = null;
        if (Boolean.TRUE.equals(request.getNeedRecommendation())) {
            recommendations = generateRecommendations(message, category);
        }

        AiConversation conversation = saveConversation(
                userId,
                sessionId,
                message,
                aiResponse,
                category.getDisplayName(),
                recommendations,
                tokensUsed
        );

        cacheConversationHistory(userId, sessionId);
        recordMetrics(startNanos, tokensUsed, ragContext);

        return AiChatResponse.builder()
                .response(aiResponse)
                .sessionId(sessionId)
                .category(category.getDisplayName())
                .recommendations(recommendations)
                .tokensUsed(tokensUsed)
                .conversationId(conversation.getId())
                .build();
    }

    public SseEmitter chatStream(Long userId, AiChatRequest request) {
        long startNanos = System.nanoTime();
        String message = normalizeMessage(request.getMessage());
        String sessionId = resolveSessionId(request.getSessionId());
        QuestionCategory category = QuestionCategory.detect(message);
        RagContextResult ragContext = ragPipelineService.buildContext(message);

        SseEmitter emitter = new SseEmitter(300000L);
        StringBuilder fullResponse = new StringBuilder();

        try {
            List<ChatMessage> messages = buildConversationHistory(userId, sessionId, message, ragContext.context());
            emitter.send(SseEmitter.event().name("session").data(Map.of("sessionId", sessionId)));

            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<>() {

                @Override
                public void onNext(String token) {
                    try {
                        fullResponse.append(token);
                        emitter.send(SseEmitter.event().name("message").data(token));
                    } catch (Exception ex) {
                        log.error("SSE send token failed", ex);
                        emitter.completeWithError(ex);
                    }
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    try {
                        int tokensUsed = extractTokenUsage(response);
                        String finalResponse = appendCitations(fullResponse.toString(), ragContext);
                        AiConversation conversation = saveConversation(
                                userId,
                                sessionId,
                                message,
                                finalResponse,
                                category.getDisplayName(),
                                null,
                                tokensUsed
                        );
                        cacheConversationHistory(userId, sessionId);
                        recordMetrics(startNanos, tokensUsed, ragContext);

                        emitter.send(SseEmitter.event().name("done").data(Map.of(
                                "conversationId", conversation.getId(),
                                "category", category.getDisplayName()
                        )));
                        emitter.complete();
                    } catch (Exception ex) {
                        log.error("SSE complete callback failed", ex);
                        emitter.completeWithError(ex);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("SSE stream failed", error);
                    try {
                        emitter.send(SseEmitter.event().name("error").data(Map.of("error", "AI服务暂时不可用，请稍后重试")));
                    } catch (Exception ex) {
                        log.error("SSE send error event failed", ex);
                    }
                    emitter.completeWithError(error);
                }
            });
        } catch (Exception ex) {
            log.error("SSE init failed", ex);
            emitter.completeWithError(ex);
        }

        emitter.onTimeout(() -> {
            log.warn("SSE timeout, userId={}, sessionId={}", userId, sessionId);
            emitter.complete();
        });

        return emitter;
    }

    public List<AiConversation> getUserSessions(Long userId, Integer limit) {
        int safeLimit = limit == null ? DEFAULT_SESSION_LIMIT : Math.max(1, Math.min(limit, MAX_SESSION_LIMIT));
        return conversationMapper.getUserSessions(userId, safeLimit);
    }

    public List<AiConversation> getSessionHistory(Long userId, String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new RuntimeException("会话ID不能为空");
        }
        return conversationMapper.getSessionHistory(userId, sessionId);
    }

    @Transactional
    public void submitFeedback(Long userId, Long conversationId, String feedback) {
        AiConversation conversation = getOwnedConversation(userId, conversationId);
        conversation.setFeedback(feedback);
        updateById(conversation);
    }

    @Transactional
    public void bookmarkConversation(Long userId, Long conversationId, Boolean isBookmarked) {
        AiConversation conversation = getOwnedConversation(userId, conversationId);
        conversation.setIsBookmarked(Boolean.TRUE.equals(isBookmarked));
        updateById(conversation);
    }

    public List<AiConversation> getBookmarkedConversations(Long userId) {
        return list(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getIsBookmarked, true)
                .orderByDesc(AiConversation::getCreatedAt));
    }

    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new RuntimeException("会话ID不能为空");
        }

        long count = count(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId));

        if (count == 0) {
            throw new RuntimeException("会话不存在或无权限访问");
        }

        boolean removed = remove(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId));

        if (!removed) {
            throw new RuntimeException("删除会话失败");
        }

        removeConversationCache(userId, sessionId);
    }

    @Transactional
    public void renameSession(Long userId, String sessionId, String title) {
        if (!StringUtils.hasText(sessionId)) {
            throw new RuntimeException("会话ID不能为空");
        }
        String safeTitle = normalizeSessionTitle(title);

        long count = count(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId));

        if (count == 0) {
            throw new RuntimeException("会话不存在或无权限访问");
        }

        boolean updated = update(new LambdaUpdateWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId)
                .set(AiConversation::getSessionTitle, safeTitle));

        if (!updated) {
            throw new RuntimeException("会话重命名失败");
        }

        removeConversationCache(userId, sessionId);
    }

    private List<ChatMessage> buildConversationHistory(Long userId, String sessionId, String currentMessage, String ragContext) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt()));

        if (StringUtils.hasText(ragContext)) {
            messages.add(new SystemMessage("【参考知识片段】\n" + ragContext + "\n\n请优先依据这些信息回答，并明确区分已知事实与推断。"));
        }

        List<AiConversation> history = loadConversationHistory(userId, sessionId);
        int maxHistorySize = Math.max(1, aiAssistantProperties.getMaxHistorySize());
        int start = Math.max(0, history.size() - maxHistorySize);

        for (int i = start; i < history.size(); i++) {
            AiConversation conversation = history.get(i);
            if (StringUtils.hasText(conversation.getUserMessage())) {
                messages.add(new UserMessage(conversation.getUserMessage()));
            }
            if (StringUtils.hasText(conversation.getAiResponse())) {
                messages.add(new AiMessage(conversation.getAiResponse()));
            }
        }

        messages.add(new UserMessage(currentMessage));
        return messages;
    }

    private List<AiConversation> loadConversationHistory(Long userId, String sessionId) {
        String cacheKey = buildCacheKey(userId, sessionId);
        try {
            @SuppressWarnings("unchecked")
            List<AiConversation> cached = (List<AiConversation>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception ex) {
            log.warn("load conversation history from redis failed, fallback db. key={}, reason={}", cacheKey, ex.getMessage());
        }
        List<AiConversation> history = conversationMapper.getSessionHistory(userId, sessionId);
        return history == null ? List.of() : history;
    }

    private Response<AiMessage> generateWithRetry(List<ChatMessage> messages) {
        int maxAttempts = Math.max(1, aiAssistantProperties.getMaxRequestRetries() + 1);
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return chatLanguageModel.generate(messages);
            } catch (Exception ex) {
                lastException = new RuntimeException(ex);
                log.warn("AI generate failed, attempt={}/{}, reason={}", attempt, maxAttempts, ex.getMessage());
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt);
                }
            }
        }

        throw lastException != null ? lastException : new RuntimeException("AI模型调用失败");
    }

    private void sleepBackoff(int attempt) {
        long backoffMillis = Math.max(100L, aiAssistantProperties.getRetryBackoffMillis()) * attempt;
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("重试等待被中断", interruptedException);
        }
    }

    private String buildSystemPrompt() {
        return """
                你是高校课程答疑助手，请遵守以下规则：
                1. 回答必须准确、可解释、步骤清晰；
                2. 对不确定内容要显式说明“这是推断”；
                3. 遇到明显越权或危险请求时拒绝并给出合规替代建议；
                4. 输出默认使用中文，必要时补充英文术语。
                """;
    }

    private String safeModelResponse(Response<AiMessage> response) {
        if (response == null || response.content() == null || !StringUtils.hasText(response.content().text())) {
            return "暂未生成有效回答，请稍后重试。";
        }
        return response.content().text();
    }

    private int extractTokenUsage(Response<AiMessage> response) {
        if (response == null || response.tokenUsage() == null || response.tokenUsage().totalTokenCount() == null) {
            return 0;
        }
        return response.tokenUsage().totalTokenCount();
    }

    private String appendCitations(String answer, RagContextResult ragContext) {
        String safeAnswer = StringUtils.hasText(answer) ? answer : "";
        if (!ragContext.hasContext() || ragContext.citations().isEmpty() || safeAnswer.contains("参考来源")) {
            return safeAnswer;
        }

        String citationText = ragContext.citations().stream()
                .limit(3)
                .collect(Collectors.joining("\n- ", "\n\n参考来源：\n- ", ""));
        return safeAnswer + citationText;
    }

    private String buildFallbackAnswer(RagContextResult ragContext) {
        if (ragContext.hasContext()) {
            String context = ragContext.context();
            String preview = context.length() > 600 ? context.substring(0, 600) + "..." : context;
            return "当前 AI 服务暂时不可用，你可以先参考系统中已命中的相关问答片段：\n" + preview;
        }
        return "AI服务暂时不可用，请稍后重试。";
    }

    private List<AiChatResponse.ResourceRecommendation> generateRecommendations(String message, QuestionCategory category) {
        Map<QuestionCategory, List<AiAssistantProperties.RecommendationItem>> rules = buildMergedRecommendationRules();
        List<AiAssistantProperties.RecommendationItem> items = rules.getOrDefault(category, List.of());

        return items.stream()
                .map(item -> AiChatResponse.ResourceRecommendation.builder()
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .url(item.getUrl())
                        .type(StringUtils.hasText(item.getType()) ? item.getType() : "article")
                        .build())
                .collect(Collectors.toList());
    }

    private Map<QuestionCategory, List<AiAssistantProperties.RecommendationItem>> buildMergedRecommendationRules() {
        Map<QuestionCategory, List<AiAssistantProperties.RecommendationItem>> merged = new EnumMap<>(fallbackRecommendationRules);
        if (aiAssistantProperties.getRecommendationRules() == null || aiAssistantProperties.getRecommendationRules().isEmpty()) {
            return merged;
        }

        for (Map.Entry<String, List<AiAssistantProperties.RecommendationItem>> entry
                : aiAssistantProperties.getRecommendationRules().entrySet()) {
            QuestionCategory category = QuestionCategory.fromCode(entry.getKey());
            if (category == QuestionCategory.GENERAL && !"general".equals(entry.getKey())) {
                continue;
            }
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                merged.put(category, entry.getValue());
            }
        }
        return merged;
    }

    private Map<QuestionCategory, List<AiAssistantProperties.RecommendationItem>> buildFallbackRecommendationRules() {
        Map<QuestionCategory, List<AiAssistantProperties.RecommendationItem>> rules = new EnumMap<>(QuestionCategory.class);
        rules.put(QuestionCategory.COMPUTER_SCIENCE, List.of(
                buildRecommendation("菜鸟教程", "覆盖主流编程语言与基础入门", "https://www.runoob.com/", "article"),
                buildRecommendation("MDN Web Docs", "权威 Web 技术文档与示例", "https://developer.mozilla.org/zh-CN/", "document")
        ));
        rules.put(QuestionCategory.ADVANCED_MATH, List.of(
                buildRecommendation("Khan Academy 微积分", "系统学习导数与积分", "https://www.khanacademy.org/math/calculus-1", "video")
        ));
        rules.put(QuestionCategory.LINEAR_ALGEBRA, List.of(
                buildRecommendation("3Blue1Brown 线性代数", "图形化理解线代核心概念", "https://www.3blue1brown.com/topics/linear-algebra", "video")
        ));
        rules.put(QuestionCategory.PROBABILITY_STATS, List.of(
                buildRecommendation("StatQuest", "概率统计与机器学习基础讲解", "https://www.youtube.com/c/joshstarmer", "video")
        ));
        rules.put(QuestionCategory.COLLEGE_PHYSICS, List.of(
                buildRecommendation("MIT OCW Physics", "高校物理公开课程与课件", "https://ocw.mit.edu/", "course")
        ));
        return rules;
    }

    private AiAssistantProperties.RecommendationItem buildRecommendation(String title, String description, String url, String type) {
        AiAssistantProperties.RecommendationItem item = new AiAssistantProperties.RecommendationItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setUrl(url);
        item.setType(type);
        return item;
    }

    private AiConversation saveConversation(Long userId,
                                            String sessionId,
                                            String userMessage,
                                            String aiResponse,
                                            String category,
                                            List<AiChatResponse.ResourceRecommendation> recommendations,
                                            int tokensUsed) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setSessionId(sessionId);
        conversation.setSessionTitle(resolveSessionTitle(userId, sessionId, userMessage));
        conversation.setUserMessage(userMessage);
        conversation.setAiResponse(aiResponse);
        conversation.setMessageType("text");
        conversation.setQuestionCategory(category);
        conversation.setIsBookmarked(false);
        conversation.setTokensUsed(Math.max(tokensUsed, 0));
        conversation.setRecommendedResources(serializeRecommendations(recommendations));

        LocalDateTime now = LocalDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);

        save(conversation);
        return conversation;
    }

    private String resolveSessionTitle(Long userId, String sessionId, String userMessage) {
        AiConversation existed = getOne(new LambdaQueryWrapper<AiConversation>()
                .select(AiConversation::getSessionTitle)
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId)
                .orderByAsc(AiConversation::getId)
                .last("LIMIT 1"));

        if (existed != null && StringUtils.hasText(existed.getSessionTitle())) {
            return existed.getSessionTitle();
        }
        return buildSessionTitle(userMessage);
    }

    private String buildSessionTitle(String message) {
        String normalized = message.replaceAll("\\s+", " ").trim();
        int maxLength = Math.max(10, aiAssistantProperties.getSessionTitleMaxLength());
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private String serializeRecommendations(List<AiChatResponse.ResourceRecommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(recommendations);
        } catch (JsonProcessingException ex) {
            log.warn("serialize recommendations failed, use fallback format. reason={}", ex.getMessage());
            return recommendations.stream()
                    .map(item -> item.getTitle() + "|" + item.getUrl())
                    .collect(Collectors.joining(";"));
        }
    }

    private void cacheConversationHistory(Long userId, String sessionId) {
        String cacheKey = buildCacheKey(userId, sessionId);
        try {
            List<AiConversation> history = conversationMapper.getSessionHistory(userId, sessionId);
            redisTemplate.opsForValue().set(cacheKey, history, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception ex) {
            log.warn("cache conversation history failed. key={}, reason={}", cacheKey, ex.getMessage());
        }
    }

    private void removeConversationCache(Long userId, String sessionId) {
        try {
            redisTemplate.delete(buildCacheKey(userId, sessionId));
        } catch (Exception ex) {
            log.warn("delete conversation cache failed. userId={}, sessionId={}, reason={}", userId, sessionId, ex.getMessage());
        }
    }

    private String buildCacheKey(Long userId, String sessionId) {
        return CACHE_KEY_PREFIX + userId + ":" + sessionId;
    }

    private AiConversation getOwnedConversation(Long userId, Long conversationId) {
        if (conversationId == null) {
            throw new RuntimeException("对话ID不能为空");
        }

        AiConversation conversation = getOne(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getId, conversationId)
                .eq(AiConversation::getUserId, userId)
                .last("LIMIT 1"));

        if (conversation == null) {
            throw new RuntimeException("对话不存在或无权限访问");
        }
        return conversation;
    }

    private String normalizeMessage(String message) {
        if (!StringUtils.hasText(message)) {
            throw new RuntimeException("消息内容不能为空");
        }
        String normalized = message.trim();
        if (normalized.length() > aiAssistantProperties.getMaxUserMessageLength()) {
            throw new RuntimeException("消息内容过长，请精简后重试");
        }
        return normalized;
    }

    private String resolveSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return UUID.randomUUID().toString();
        }
        return sessionId.trim();
    }

    private String normalizeSessionTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new RuntimeException("标题不能为空");
        }

        String normalized = title
                .replace("<", "")
                .replace(">", "")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("标题不能为空");
        }

        int maxLength = Math.max(10, aiAssistantProperties.getSessionTitleMaxLength());
        if (normalized.length() > maxLength) {
            throw new RuntimeException("标题长度不能超过" + maxLength + "个字符");
        }

        return normalized;
    }

    private void recordMetrics(long startNanos, int tokensUsed, RagContextResult ragContext) {
        Timer.builder(METRIC_AI_REQUEST_LATENCY_MS)
                .description("AI request latency")
                .register(meterRegistry)
                .record(Duration.ofNanos(System.nanoTime() - startNanos));

        Counter.builder(METRIC_AI_TOKENS_TOTAL)
                .description("AI token usage")
                .register(meterRegistry)
                .increment(Math.max(tokensUsed, 0));

        Counter.builder(METRIC_RAG_RECALL_COUNT)
                .description("RAG recalled candidates")
                .register(meterRegistry)
                .increment(Math.max(ragContext.recallCount(), 0));

        Counter.builder(METRIC_RAG_HIT_RATE)
                .description("RAG hit count")
                .tag("hit", String.valueOf(ragContext.hasContext()))
                .register(meterRegistry)
                .increment();

        if (!ragContext.hasContext()) {
            Counter.builder(METRIC_RAG_NO_CONTEXT_RATE)
                    .description("RAG no-context count")
                    .register(meterRegistry)
                    .increment();
        }
    }
}
