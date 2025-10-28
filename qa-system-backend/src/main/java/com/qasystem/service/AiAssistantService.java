package com.qasystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.entity.AiConversation;
import com.qasystem.mapper.AiConversationMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI助手服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService extends ServiceImpl<AiConversationMapper, AiConversation> {
    
    private final ChatLanguageModel chatLanguageModel;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AiConversationMapper conversationMapper;
    
    private static final String CACHE_KEY_PREFIX = "ai:conversation:";
    private static final int CACHE_EXPIRE_HOURS = 24;
    private static final int MAX_HISTORY_SIZE = 10;
    
    /**
     * AI聊天对话
     */
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        try {
            // 生成或使用现有的会话ID
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            
            // 构建对话历史
            List<ChatMessage> messages = buildConversationHistory(userId, sessionId, request.getMessage());
            
            // 调用AI模型
            log.info("调用AI模型，用户ID: {}, 会话ID: {}", userId, sessionId);
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            String aiResponse = response.content().text();
            
            // 分类问题
            String category = categorizeQuestion(request.getMessage());
            
            // 生成学习资源推荐
            List<AiChatResponse.ResourceRecommendation> recommendations = null;
            if (Boolean.TRUE.equals(request.getNeedRecommendation())) {
                recommendations = generateRecommendations(request.getMessage(), category);
            }
            
            // 保存对话记录
            AiConversation conversation = saveConversation(
                userId, 
                sessionId, 
                request.getMessage(), 
                aiResponse,
                category,
                recommendations,
                response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0
            );
            
            // 缓存会话历史
            cacheConversationHistory(userId, sessionId);
            
            return AiChatResponse.builder()
                    .response(aiResponse)
                    .sessionId(sessionId)
                    .category(category)
                    .recommendations(recommendations)
                    .tokensUsed(response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0)
                    .conversationId(conversation.getId())
                    .build();
                    
        } catch (Exception e) {
            log.error("AI对话失败", e);
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }
    
    /**
     * 构建对话历史
     */
    private List<ChatMessage> buildConversationHistory(Long userId, String sessionId, String currentMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统提示词
        String systemPrompt = """
                你是一个专业的教育助手，专门帮助学生解决学习问题。
                你的职责是：
                1. 回答学生的学习问题，提供清晰、准确的解释
                2. 推荐相关的学习资源和材料
                3. 帮助学生理解复杂的概念
                4. 鼓励学生独立思考，不直接给出完整答案
                5. 使用友好、耐心的语气
                
                请用中文回答，保持专业但不失亲和力。
                """;
        messages.add(new SystemMessage(systemPrompt));
        
        // 尝试从缓存获取历史记录
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + sessionId;
        @SuppressWarnings("unchecked")
        List<AiConversation> history = (List<AiConversation>) redisTemplate.opsForValue().get(cacheKey);
        
        // 如果缓存不存在，从数据库加载
        if (history == null || history.isEmpty()) {
            history = conversationMapper.getSessionHistory(userId, sessionId);
        }
        
        // 只保留最近的N条历史记录
        if (history != null && !history.isEmpty()) {
            int startIndex = Math.max(0, history.size() - MAX_HISTORY_SIZE);
            for (int i = startIndex; i < history.size(); i++) {
                AiConversation conv = history.get(i);
                messages.add(new UserMessage(conv.getUserMessage()));
                if (conv.getAiResponse() != null) {
                    messages.add(new AiMessage(conv.getAiResponse()));
                }
            }
        }
        
        // 添加当前消息
        messages.add(new UserMessage(currentMessage));
        
        return messages;
    }
    
    /**
     * 问题分类
     */
    private String categorizeQuestion(String message) {
        message = message.toLowerCase();
        
        if (message.contains("java") || message.contains("python") || message.contains("编程") || 
            message.contains("代码") || message.contains("算法")) {
            return "技术问题";
        } else if (message.contains("数学") || message.contains("物理") || message.contains("化学")) {
            return "学科问题";
        } else if (message.contains("课程") || message.contains("学分") || message.contains("考试")) {
            return "课程问题";
        } else {
            return "一般咨询";
        }
    }
    
    /**
     * 生成学习资源推荐
     */
    private List<AiChatResponse.ResourceRecommendation> generateRecommendations(String message, String category) {
        List<AiChatResponse.ResourceRecommendation> recommendations = new ArrayList<>();
        
        // 这里可以根据问题内容和分类，推荐相关资源
        // 示例推荐
        if ("技术问题".equals(category)) {
            recommendations.add(AiChatResponse.ResourceRecommendation.builder()
                    .title("菜鸟教程 - " + extractKeyword(message))
                    .description("基础教程和实例")
                    .url("https://www.runoob.com/")
                    .type("article")
                    .build());
                    
            recommendations.add(AiChatResponse.ResourceRecommendation.builder()
                    .title("MDN Web Docs")
                    .description("权威的Web技术文档")
                    .url("https://developer.mozilla.org/zh-CN/")
                    .type("document")
                    .build());
        }
        
        return recommendations;
    }
    
    /**
     * 提取关键词
     */
    private String extractKeyword(String message) {
        String[] keywords = {"java", "python", "javascript", "html", "css", "sql", "react", "vue"};
        for (String keyword : keywords) {
            if (message.toLowerCase().contains(keyword)) {
                return keyword.toUpperCase();
            }
        }
        return "编程";
    }
    
    /**
     * 保存对话记录
     */
    private AiConversation saveConversation(Long userId, String sessionId, String userMessage, 
                                           String aiResponse, String category, 
                                           List<AiChatResponse.ResourceRecommendation> recommendations,
                                           int tokensUsed) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setSessionId(sessionId);
        conversation.setUserMessage(userMessage);
        conversation.setAiResponse(aiResponse);
        conversation.setMessageType("text");
        conversation.setQuestionCategory(category);
        conversation.setIsBookmarked(false);
        conversation.setTokensUsed(tokensUsed);
        
        // 手动设置时间
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        
        // 保存推荐资源（简化为JSON字符串）
        if (recommendations != null && !recommendations.isEmpty()) {
            conversation.setRecommendedResources(
                recommendations.stream()
                    .map(r -> r.getTitle() + "|" + r.getUrl())
                    .collect(Collectors.joining(";"))
            );
        }
        
        save(conversation);
        return conversation;
    }
    
    /**
     * 缓存会话历史
     */
    private void cacheConversationHistory(Long userId, String sessionId) {
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + sessionId;
        List<AiConversation> history = conversationMapper.getSessionHistory(userId, sessionId);
        redisTemplate.opsForValue().set(cacheKey, history, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 获取用户的会话列表
     */
    public List<AiConversation> getUserSessions(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        return conversationMapper.getUserSessions(userId, limit);
    }

    /**
     * 获取会话历史
     */
    public List<AiConversation> getSessionHistory(Long userId, String sessionId) {
        return conversationMapper.getSessionHistory(userId, sessionId);
    }
    
    /**
     * 提交反馈
     */
    public void submitFeedback(Long conversationId, String feedback) {
        AiConversation conversation = getById(conversationId);
        if (conversation != null) {
            conversation.setFeedback(feedback);
            updateById(conversation);
        }
    }
    
    /**
     * 收藏对话
     */
    public void bookmarkConversation(Long conversationId, Boolean isBookmarked) {
        AiConversation conversation = getById(conversationId);
        if (conversation != null) {
            conversation.setIsBookmarked(isBookmarked);
            updateById(conversation);
        }
    }
    
    /**
     * 获取收藏的对话
     */
    public List<AiConversation> getBookmarkedConversations(Long userId) {
        return list(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getIsBookmarked, true)
                .orderByDesc(AiConversation::getCreatedAt));
    }
}
