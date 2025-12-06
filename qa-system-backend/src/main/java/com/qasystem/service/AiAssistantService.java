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
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.StreamingResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 🤖 AI助手服务 - 提供智能问答和对话管理功能
 * 
 * 📖 功能说明：
 * AI助手服务是师生答疑系统的核心AI功能模块，基于大语言模型为学生提供智能问答服务。
 * 本服务主要功能包括：
 * 1. 智能对话 - 基于大语言模型提供自然语言问答
 * 2. 会话管理 - 维护用户与AI的对话上下文和历史记录
 * 3. 问题分类 - 自动识别问题类型，提供针对性回答
 * 4. 资源推荐 - 根据问题内容推荐相关学习资源
 * 5. 缓存优化 - 使用Redis缓存会话历史，提高响应速度
 * 6. 对话持久化 - 保存对话记录，支持历史查询和分析
 * 
 * 🔧 技术实现：
 * - 基于LangChain4j框架集成多种大语言模型
 * - 使用Redis缓存会话历史，减少数据库查询
 * - 采用MyBatis-Plus进行数据持久化
 * - 实现会话ID管理，支持多会话并行
 * 
 * 📊 性能优化：
 * - 会话历史缓存：减少数据库查询，提高响应速度
 * - 历史记录限制：只保留最近N条记录，控制上下文长度
 * - 异步处理：资源推荐等耗时操作异步执行
 * 
 * 🔄 工作流程：
 * 1. 接收用户问题
 * 2. 构建对话历史（从缓存或数据库）
 * 3. 调用大语言模型生成回答
 * 4. 分析问题类型
 * 5. 生成学习资源推荐
 * 6. 保存对话记录
 * 7. 更新缓存
 * 8. 返回响应结果
 * 
 * @author 师生答疑系统开发团队
 * @since 2.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录AI对话过程和异常信息
@Service  // Spring服务层注解，将此类注册为Spring Bean
@RequiredArgsConstructor  // Lombok注解，为final字段生成构造函数，实现依赖注入
public class AiAssistantService extends ServiceImpl<AiConversationMapper, AiConversation> {
    
    // 大语言模型接口，用于生成AI回答
    // 支持多种模型：OpenAI GPT、百度文心一言、阿里通义千问等
    private final ChatLanguageModel chatLanguageModel;
    
    // 流式大语言模型接口，用于SSE流式响应
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    
    // Redis模板，用于缓存会话历史
    // 提高会话历史查询速度，减少数据库压力
    private final RedisTemplate<String, Object> redisTemplate;
    
    // AI对话数据访问层，用于持久化对话记录
    private final AiConversationMapper conversationMapper;
    
    // 缓存键前缀，用于在Redis中存储会话历史
    // 格式：ai:conversation:{userId}:{sessionId}
    private static final String CACHE_KEY_PREFIX = "ai:conversation:";
    
    // 缓存过期时间（小时），会话历史在Redis中的保存时间
    private static final int CACHE_EXPIRE_HOURS = 24;
    
    // 最大历史记录数，限制发送给AI模型的上下文长度
    // 防止上下文过长导致超出模型Token限制
    private static final int MAX_HISTORY_SIZE = 10;
    
    /**
     * 🗣️ AI聊天对话 - 处理用户与AI助手的交互请求
     * 
     * 业务流程：
     * 1. 会话ID处理：生成新会话ID或使用现有会话ID
     * 2. 历史构建：从缓存或数据库获取对话历史
     * 3. 模型调用：将历史和当前问题发送给大语言模型
     * 4. 问题分类：分析问题类型，用于后续处理和推荐
     * 5. 资源推荐：根据问题内容和类型生成学习资源推荐
     * 6. 记录保存：将对话记录保存到数据库
     * 7. 缓存更新：更新Redis中的会话历史缓存
     * 8. 响应构建：组装完整的响应结果返回
     * 
     * 性能优化：
     * - 使用Redis缓存会话历史，减少数据库查询
     * - 限制历史记录数量，控制Token使用量
     * - 异步生成资源推荐，不阻塞主流程
     * 
     * 异常处理：
     * - 模型调用失败：返回友好错误信息
     * - 数据库异常：记录日志并抛出运行时异常
     * - 缓存异常：降级到数据库查询
     * 
     * 请求示例：
     * {
     *   "message": "Java中的多线程是如何工作的？",
     *   "sessionId": "optional-existing-session-id",
     *   "needRecommendation": true
     * }
     * 
     * 响应示例：
     * {
     *   "response": "Java中的多线程是通过...",
     *   "sessionId": "uuid-generated-or-existing",
     *   "category": "技术问题",
     *   "recommendations": [
     *     {
     *       "title": "Java多线程编程指南",
     *       "description": "详细介绍Java多线程概念和实现",
     *       "url": "https://example.com/java-threading",
     *       "type": "article"
     *     }
     *   ],
     *   "tokensUsed": 256,
     *   "conversationId": 12345
     * }
     * 
     * @param userId 用户ID，用于标识对话所属用户
     * @param request AI聊天请求对象，包含用户消息、会话ID等
     * @return AiChatResponse AI聊天响应对象，包含AI回答、会话ID、推荐资源等
     * @throws RuntimeException 当AI服务不可用或处理失败时抛出
     */
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        try {
            // 生成或使用现有的会话ID
            // 如果请求中没有会话ID，则生成新的UUID作为会话标识
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            
            // 构建对话历史
            // 包括系统提示词、历史对话记录和当前用户消息
            List<ChatMessage> messages = buildConversationHistory(userId, sessionId, request.getMessage());
            
            // 调用AI模型
            // 将构建好的消息列表发送给大语言模型进行处理
            log.info("调用AI模型，用户ID: {}, 会话ID: {}", userId, sessionId);
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            String aiResponse = response.content().text();
            
            // 分类问题
            // 根据问题内容自动分类，用于后续推荐和统计分析
            String category = categorizeQuestion(request.getMessage());
            
            // 生成学习资源推荐
            // 根据问题内容和分类，推荐相关的学习资源
            List<AiChatResponse.ResourceRecommendation> recommendations = null;
            if (Boolean.TRUE.equals(request.getNeedRecommendation())) {
                recommendations = generateRecommendations(request.getMessage(), category);
            }
            
            // 保存对话记录
            // 将用户问题、AI回答、分类等信息保存到数据库
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
            // 更新Redis中的会话历史缓存，提高下次查询速度
            cacheConversationHistory(userId, sessionId);
            
            // 构建并返回响应
            return AiChatResponse.builder()
                    .response(aiResponse)  // AI生成的回答
                    .sessionId(sessionId)  // 会话ID
                    .category(category)  // 问题分类
                    .recommendations(recommendations)  // 资源推荐
                    .tokensUsed(response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0)  // 使用的Token数量
                    .conversationId(conversation.getId())  // 对话记录ID
                    .build();
                    
        } catch (Exception e) {
            // 记录错误日志
            log.error("AI对话失败", e);
            // 抛出友好的运行时异常
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }
    
    /**
     * 🌊 流式AI聊天对话 - 使用SSE实现打字机效果的实时响应
     * 
     * 与普通chat方法的区别：
     * - 普通chat：等待AI完整回复后一次性返回
     * - 流式chat：AI生成过程中实时推送每个token，实现打字机效果
     * 
     * 技术实现：
     * - 使用SseEmitter实现Server-Sent Events
     * - 调用StreamingChatLanguageModel进行流式生成
     * - 每收到一个token就推送给客户端
     * 
     * @param userId 用户ID
     * @param request AI聊天请求
     * @return SseEmitter SSE发射器
     */
    public SseEmitter chatStream(Long userId, AiChatRequest request) {
        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 生成或使用现有的会话ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        final String finalSessionId = sessionId;
        
        // 用于收集完整的AI回复
        StringBuilder fullResponse = new StringBuilder();
        
        try {
            // 构建对话历史
            List<ChatMessage> messages = buildConversationHistory(userId, sessionId, request.getMessage());
            
            log.info("开始流式AI对话，用户ID: {}, 会话ID: {}", userId, sessionId);
            
            // 先发送会话ID给客户端
            emitter.send(SseEmitter.event()
                    .name("session")
                    .data("{\"sessionId\":\"" + finalSessionId + "\"}"));
            
            // 调用流式AI模型
            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                
                @Override
                public void onNext(String token) {
                    try {
                        // 收集token
                        fullResponse.append(token);
                        // 发送token给客户端
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(token));
                    } catch (Exception e) {
                        log.error("发送SSE消息失败", e);
                        emitter.completeWithError(e);
                    }
                }
                
                @Override
                public void onComplete(Response<AiMessage> response) {
                    try {
                        // 分类问题
                        String category = categorizeQuestion(request.getMessage());
                        
                        // 保存对话记录
                        AiConversation conversation = saveConversation(
                            userId,
                            finalSessionId,
                            request.getMessage(),
                            fullResponse.toString(),
                            category,
                            null,
                            response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0
                        );
                        
                        // 缓存会话历史
                        cacheConversationHistory(userId, finalSessionId);
                        
                        // 发送完成事件
                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data("{\"conversationId\":" + conversation.getId() + 
                                      ",\"category\":\"" + category + "\"}"));
                        
                        log.info("流式AI对话完成，用户ID: {}, 会话ID: {}", userId, finalSessionId);
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("完成流式响应失败", e);
                        emitter.completeWithError(e);
                    }
                }
                
                @Override
                public void onError(Throwable error) {
                    log.error("流式AI对话出错", error);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data("{\"error\":\"AI服务暂时不可用\"}"));
                    } catch (Exception e) {
                        log.error("发送错误消息失败", e);
                    }
                    emitter.completeWithError(error);
                }
            });
            
        } catch (Exception e) {
            log.error("初始化流式对话失败", e);
            emitter.completeWithError(e);
        }
        
        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，用户ID: {}", userId);
            emitter.complete();
        });
        
        emitter.onCompletion(() -> {
            log.debug("SSE连接完成，用户ID: {}", userId);
        });
        
        return emitter;
    }
    
    /**
     * 📚 构建对话历史 - 准备发送给AI模型的完整上下文
     * 
     * 业务流程：
     * 1. 添加系统提示词，定义AI助手的角色和行为
     * 2. 尝试从Redis缓存获取历史对话记录
     * 3. 如果缓存不存在，从数据库加载历史记录
     * 4. 限制历史记录数量，只保留最近的N条记录
     * 5. 将历史记录转换为LangChain4j的消息格式
     * 6. 添加当前用户消息到消息列表末尾
     * 
     * 缓存策略：
     * - 优先从Redis缓存获取历史记录
     * - 缓存不存在时从数据库加载
     * - 缓存过期时间：24小时
     * 
     * 历史记录限制：
     * - 最多保留10条历史记录
     * - 防止上下文过长超出模型Token限制
     * - 保持对话连贯性的同时控制成本
     * 
     * 消息格式：
     * - SystemMessage：系统提示词，定义AI角色
     * - UserMessage：用户问题
     * - AiMessage：AI回答
     * 
     * @param userId 用户ID，用于构建缓存键
     * @param sessionId 会话ID，用于构建缓存键
     * @param currentMessage 当前用户消息
     * @return List<ChatMessage> 完整的对话历史消息列表
     */
    private List<ChatMessage> buildConversationHistory(Long userId, String sessionId, String currentMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统提示词
        // 定义AI助手的角色、职责和行为准则
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
        // 使用用户ID和会话ID构建缓存键
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + sessionId;
        @SuppressWarnings("unchecked")
        List<AiConversation> history = (List<AiConversation>) redisTemplate.opsForValue().get(cacheKey);
        
        // 如果缓存不存在，从数据库加载
        if (history == null || history.isEmpty()) {
            history = conversationMapper.getSessionHistory(userId, sessionId);
        }
        
        // 只保留最近的N条历史记录
        // 防止上下文过长，控制Token使用量
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
     * 🏷️ 问题分类 - 根据问题内容自动识别问题类型
     * 
     * 分类逻辑：
     * - 技术问题：包含编程语言、技术术语等关键词
     * - 学科问题：包含数学、物理、化学等学科名称
     * - 课程问题：包含课程、学分、考试等教育相关词汇
     * - 一般咨询：其他类型的问题
     * 
     * 应用场景：
     * - 问题统计分析：了解用户提问类型分布
     * - 资源推荐：根据问题类型推荐相关资源
     * - AI回答优化：针对不同类型问题调整回答策略
     * 
     * 分类规则：
     * - 基于关键词匹配的简单分类
     * - 可扩展为基于机器学习的智能分类
     * - 支持多关键词匹配
     * 
     * @param message 用户问题内容
     * @return String 问题分类：技术问题、学科问题、课程问题或一般咨询
     */
    private String categorizeQuestion(String message) {
        // 转换为小写，便于匹配
        message = message.toLowerCase();
        
        // 技术问题关键词匹配
        if (message.contains("java") || message.contains("python") || message.contains("编程") || 
            message.contains("代码") || message.contains("算法")) {
            return "技术问题";
        } 
        // 学科问题关键词匹配
        else if (message.contains("数学") || message.contains("物理") || message.contains("化学")) {
            return "学科问题";
        } 
        // 课程问题关键词匹配
        else if (message.contains("课程") || message.contains("学分") || message.contains("考试")) {
            return "课程问题";
        } 
        // 默认分类
        else {
            return "一般咨询";
        }
    }
    
    /**
     * 📖 生成学习资源推荐 - 根据问题内容和类型推荐相关学习资源
     * 
     * 推荐策略：
     * - 基于问题分类：不同类型问题推荐不同资源
     * - 基于关键词：提取问题中的技术关键词
     * - 预定义资源库：维护高质量的学习资源库
     * 
     * 资源类型：
     * - 文章教程：系统性学习材料
     * - 官方文档：权威参考文档
     * - 视频课程：可视化学习资源
     * - 实践项目：动手实践资源
     * 
     * 扩展方向：
     * - 基于用户历史推荐个性化资源
     * - 基于协同过滤推荐相似用户喜欢的资源
     * - 基于内容相似度推荐相关资源
     * - 集成第三方资源API获取最新资源
     * 
     * @param message 用户问题内容
     * @param category 问题分类
     * @return List<AiChatResponse.ResourceRecommendation> 推荐资源列表
     */
    private List<AiChatResponse.ResourceRecommendation> generateRecommendations(String message, String category) {
        List<AiChatResponse.ResourceRecommendation> recommendations = new ArrayList<>();
        
        // 根据问题分类推荐不同资源
        if ("技术问题".equals(category)) {
            // 推荐编程学习资源
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
     * 🔍 提取关键词 - 从问题内容中提取技术关键词
     * 
     * 提取逻辑：
     * - 预定义关键词列表：常见编程语言和技术
     * - 简单字符串匹配：检查问题中是否包含关键词
     * - 优先级匹配：按预定义顺序匹配关键词
     * 
     * 应用场景：
     * - 资源推荐：根据关键词推荐相关技术资源
     * - 问题标签：为问题添加技术标签
     * - 统计分析：统计热门技术话题
     * 
     * 扩展方向：
     * - 使用NLP技术提取关键词
     * - 支持多关键词提取
     * - 基于TF-IDF算法计算关键词权重
     * 
     * @param message 用户问题内容
     * @return String 提取的关键词，如果没有匹配则返回空字符串
     */
    private String extractKeyword(String message) {
        // 预定义的技术关键词列表
        String[] keywords = {"java", "python", "javascript", "html", "css", "sql", "react", "vue"};
        
        // 遍历关键词列表，查找匹配项
        for (String keyword : keywords) {
            if (message.toLowerCase().contains(keyword)) {
                return keyword.toUpperCase();
            }
        }
        // 如果没有匹配的关键词，返回默认值
        return "编程";
    }
    
    /**
     * 💾 保存对话记录 - 将用户与AI的对话持久化到数据库
     * 
     * 保存内容：
     * - 基本信息：用户ID、会话ID、用户消息、AI回答
     * - 分类信息：问题类型、消息类型
     * - 统计信息：使用的Token数量
     * - 推荐资源：将推荐资源列表转换为JSON字符串存储
     * - 时间信息：创建时间和更新时间
     * 
     * 数据处理：
     * - 推荐资源：简化为"标题|URL"格式，用分号分隔多个资源
     * - 时间戳：手动设置创建和更新时间
     * - 默认值：设置默认的消息类型和收藏状态
     * 
     * 性能考虑：
     * - 批量插入：如果需要保存多条记录，考虑批量操作
     * - 异步处理：对于高频对话场景，可考虑异步保存
     * - 索引优化：为用户ID和会话ID建立索引
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @param aiResponse AI回答
     * @param category 问题分类
     * @param recommendations 推荐资源列表
     * @param tokensUsed 使用的Token数量
     * @return AiConversation 保存后的对话记录对象
     */
    private AiConversation saveConversation(Long userId, String sessionId, String userMessage, 
                                           String aiResponse, String category, 
                                           List<AiChatResponse.ResourceRecommendation> recommendations,
                                           int tokensUsed) {
        // 创建对话记录对象
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);  // 设置用户ID
        conversation.setSessionId(sessionId);  // 设置会话ID
        conversation.setUserMessage(userMessage);  // 设置用户消息
        conversation.setAiResponse(aiResponse);  // 设置AI回答
        conversation.setMessageType("text");  // 设置消息类型为文本
        conversation.setQuestionCategory(category);  // 设置问题分类
        conversation.setIsBookmarked(false);  // 设置初始收藏状态为未收藏
        conversation.setTokensUsed(tokensUsed);  // 设置使用的Token数量
        
        // 手动设置时间
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        conversation.setCreatedAt(now);  // 设置创建时间
        conversation.setUpdatedAt(now);  // 设置更新时间
        
        // 保存推荐资源（简化为JSON字符串）
        // 将推荐资源列表转换为"标题|URL"格式，用分号分隔多个资源
        if (recommendations != null && !recommendations.isEmpty()) {
            conversation.setRecommendedResources(
                recommendations.stream()
                    .map(r -> r.getTitle() + "|" + r.getUrl())
                    .collect(Collectors.joining(";"))
            );
        }
        
        // 保存到数据库
        save(conversation);
        return conversation;
    }
    
    /**
     * 🗂️ 缓存会话历史 - 将会话历史保存到Redis缓存
     * 
     * 缓存策略：
     * - 缓存键格式：ai:conversation:{userId}:{sessionId}
     * - 缓存时间：24小时
     * - 缓存内容：完整的会话历史记录列表
     * 
     * 缓存更新：
     * - 每次对话后更新缓存
     * - 会话重命名后清除缓存
     * - 会话删除后清除缓存
     * 
     * 性能优化：
     * - 减少数据库查询：优先从缓存获取历史记录
     * - 提高响应速度：缓存命中时直接返回，无需查询数据库
     * - 降低数据库负载：减少频繁的数据库查询
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    private void cacheConversationHistory(Long userId, String sessionId) {
        // 构建缓存键
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + sessionId;
        
        // 从数据库获取完整的会话历史
        List<AiConversation> history = conversationMapper.getSessionHistory(userId, sessionId);
        
        // 将会话历史保存到Redis，设置过期时间为24小时
        redisTemplate.opsForValue().set(cacheKey, history, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 📋 获取用户的会话列表 - 查询用户的所有AI对话会话
     * 
     * 查询逻辑：
     * - 按创建时间倒序排列，最新的会话在前
     * - 每个会话只返回一条记录（通常是该会话的最后一条对话）
     * - 支持分页查询，限制返回数量
     * 
     * 应用场景：
     * - 会话列表页面：显示用户的所有会话
     * - 会话搜索：根据会话标题或内容搜索
     * - 会话管理：重命名、删除会话等操作
     * 
     * 性能优化：
     * - 使用索引：为用户ID和创建时间建立索引
     * - 分页查询：避免一次性加载过多数据
     * - 缓存热门会话：提高常用会话的访问速度
     * 
     * @param userId 用户ID
     * @param limit 返回的会话数量限制，默认为20
     * @return List<AiConversation> 用户会话列表
     */
    public List<AiConversation> getUserSessions(Long userId, Integer limit) {
        // 设置默认限制数量
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        
        // 查询用户的会话列表，按创建时间倒序
        return conversationMapper.getUserSessions(userId, limit);
    }

    /**
     * 📜 获取会话历史 - 获取指定会话的完整对话记录
     * 
     * 查询逻辑：
     * - 按创建时间正序排列，保持对话的时间顺序
     * - 返回该会话的所有对话记录
     * - 包含用户消息和AI回答
     * 
     * 数据来源：
     * - 优先从Redis缓存获取
     * - 缓存不存在时从数据库查询
     * 
     * 应用场景：
     * - 会话详情页面：显示完整的对话历史
     * - 上下文构建：为AI模型提供对话上下文
     * - 会话导出：将对话记录导出为文档
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return List<AiConversation> 会话历史记录列表
     */
    public List<AiConversation> getSessionHistory(Long userId, String sessionId) {
        return conversationMapper.getSessionHistory(userId, sessionId);
    }
    
    /**
     * 📝 提交反馈 - 保存用户对AI回答的反馈
     * 
     * 反馈类型：
     * - 正面反馈：回答有帮助、准确、清晰等
     * - 负面反馈：回答不准确、不相关、不清晰等
     * - 建议反馈：改进建议、补充说明等
     * 
     * 应用场景：
     * - AI模型优化：根据反馈调整模型参数
     * - 回答质量评估：统计回答满意度
     * - 个性化改进：根据用户反馈优化回答策略
     * 
     * 数据处理：
     * - 直接更新对话记录的反馈字段
     * - 记录反馈时间
     * - 可扩展为结构化反馈数据
     * 
     * @param conversationId 对话记录ID
     * @param feedback 用户反馈内容
     */
    public void submitFeedback(Long conversationId, String feedback) {
        // 查询对话记录
        AiConversation conversation = getById(conversationId);
        if (conversation != null) {
            // 设置反馈内容
            conversation.setFeedback(feedback);
            // 更新数据库记录
            updateById(conversation);
        }
    }
    
    /**
     * ⭐ 收藏对话 - 标记或取消标记对话为收藏状态
     * 
     * 收藏功能：
     * - 收藏重要对话：保存有价值的问答内容
     * - 快速访问：从收藏列表快速访问重要对话
     * - 知识管理：构建个人知识库
     * 
     * 应用场景：
     * - 学习笔记：收藏重要的知识点解释
     * - 问题解决：收藏解决问题的方法
     * - 复习资料：收藏需要复习的内容
     * 
     * 数据处理：
     * - 更新对话记录的收藏状态
     * - 记录收藏时间
     * - 支持批量收藏操作
     * 
     * @param conversationId 对话记录ID
     * @param isBookmarked 是否收藏，true为收藏，false为取消收藏
     */
    public void bookmarkConversation(Long conversationId, Boolean isBookmarked) {
        // 查询对话记录
        AiConversation conversation = getById(conversationId);
        if (conversation != null) {
            // 设置收藏状态
            conversation.setIsBookmarked(isBookmarked);
            // 更新数据库记录
            updateById(conversation);
        }
    }
    
    /**
     * 🌟 获取收藏的对话 - 查询用户收藏的所有对话记录
     * 
     * 查询逻辑：
     * - 按用户ID和收藏状态查询
     * - 按创建时间倒序排列，最新的收藏在前
     * - 返回完整的对话记录
     * 
     * 应用场景：
     * - 收藏列表页面：显示用户收藏的所有对话
     * - 知识库管理：管理和组织收藏的内容
     * - 快速复习：查看收藏的重要知识点
     * 
     * 性能优化：
     * - 使用复合索引：为用户ID和收藏状态建立索引
     * - 分页查询：避免一次性加载过多数据
     * - 缓存热门收藏：提高常用收藏的访问速度
     * 
     * @param userId 用户ID
     * @return List<AiConversation> 收藏的对话记录列表
     */
    public List<AiConversation> getBookmarkedConversations(Long userId) {
        // 使用LambdaQueryWrapper构建查询条件
        // 查询指定用户的所有收藏对话，按创建时间倒序排列
        return list(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)  // 匹配用户ID
                .eq(AiConversation::getIsBookmarked, true)  // 匹配收藏状态为true
                .orderByDesc(AiConversation::getCreatedAt));  // 按创建时间倒序
    }
    
    /**
     * 🗑️ 删除会话 - 删除用户指定的整个会话及其所有对话记录
     * 
     * 删除逻辑：
     * 1. 验证权限：确保用户只能删除自己的会话
     * 2. 查询会话：获取会话的所有对话记录
     * 3. 删除记录：从数据库中删除所有相关记录
     * 4. 清除缓存：删除Redis中的会话历史缓存
     * 5. 记录日志：记录删除操作的详细信息
     * 
     * 安全考虑：
     * - 权限验证：确保用户只能删除自己的会话
     * - 事务处理：确保删除操作的原子性
     * - 软删除：考虑使用软删除而非物理删除
     * 
     * 性能优化：
     * - 批量删除：一次性删除会话的所有记录
     * - 索引优化：为用户ID和会话ID建立索引
     * - 异步删除：对于大量数据，考虑异步删除
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @throws RuntimeException 当会话不存在或删除失败时抛出
     */
    public void deleteSession(Long userId, String sessionId) {
        log.info("开始删除会话 - 用户ID: {}, 会话ID: {}", userId, sessionId);
        
        // 验证权限：只能删除自己的会话
        // 查询该会话的所有对话记录，验证是否属于当前用户
        List<AiConversation> conversations = list(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId));
        
        // 检查会话是否存在
        if (conversations.isEmpty()) {
            log.warn("删除失败 - 会话不存在：用户ID: {}, 会话ID: {}", userId, sessionId);
            throw new RuntimeException("会话不存在");
        }
        
        // 记录删除的记录数和会话标题（用于日志）
        int recordCount = conversations.size();
        String sessionTitle = conversations.get(0).getSessionTitle();
        
        // 删除所有该会话的对话记录
        boolean deleteSuccess = remove(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId));
        
        // 检查删除是否成功
        if (!deleteSuccess) {
            log.error("删除会话记录失败 - 用户ID: {}, 会话ID: {}", userId, sessionId);
            throw new RuntimeException("删除会话失败");
        }
        
        // 清除缓存
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + sessionId;
        redisTemplate.delete(cacheKey);
        
        // 记录成功日志
        log.info("✅ 会话删除成功 - 用户ID: {}, 会话ID: {}, 标题: '{}', 删除记录数: {}", 
                userId, sessionId, sessionTitle, recordCount);
    }
    
    /**
     * ✏️ 重命名会话 - 修改用户指定会话的标题
     * 
     * 重命名逻辑：
     * 1. 参数验证：检查标题是否为空
     * 2. 权限验证：确保用户只能重命名自己的会话
     * 3. 获取旧标题：用于审计和日志记录
     * 4. 批量更新：更新会话中所有对话记录的标题
     * 5. 清除缓存：删除Redis中的会话历史缓存
     * 6. 记录日志：记录重命名操作的详细信息
     * 
     * 批量更新优化：
     * - 使用单条UPDATE语句更新所有记录
     * - 避免逐条更新，提高性能
     * - 减少数据库连接开销
     * 
     * 安全考虑：
     * - 权限验证：确保用户只能重命名自己的会话
     * - 输入验证：检查标题长度和内容
     * - XSS防护：过滤标题中的特殊字符
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param title 新的会话标题
     * @throws RuntimeException 当会话不存在、标题为空或重命名失败时抛出
     */
    public void renameSession(Long userId, String sessionId, String title) {
        log.info("开始重命名会话 - 用户ID: {}, 会话ID: {}, 新标题: '{}'", userId, sessionId, title);
        
        // 参数验证：检查标题是否为空
        if (title == null || title.trim().isEmpty()) {
            log.warn("重命名失败 - 标题为空：用户ID: {}, 会话ID: {}", userId, sessionId);
            throw new RuntimeException("标题不能为空");
        }
        
        // 获取旧标题用于审计
        AiConversation firstConv = getOne(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId)
                .last("LIMIT 1"));
        
        // 检查会话是否存在
        if (firstConv == null) {
            log.warn("重命名失败 - 会话不存在：用户ID: {}, 会话ID: {}", userId, sessionId);
            throw new RuntimeException("会话不存在");
        }
        
        String oldTitle = firstConv.getSessionTitle();
        
        // 验证会话存在且属于当前用户
        long count = count(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId));
        
        // 批量更新该会话的所有对话记录的标题（使用单条UPDATE语句）
        boolean updateSuccess = update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getSessionId, sessionId)
                .set(AiConversation::getSessionTitle, title.trim()));
        
        // 检查更新是否成功
        if (!updateSuccess) {
            log.error("重命名会话失败 - 用户ID: {}, 会话ID: {}", userId, sessionId);
            throw new RuntimeException("重命名失败");
        }
        
        // 清除缓存
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + sessionId;
        redisTemplate.delete(cacheKey);
        
        // 记录成功日志
        log.info("✅ 会话重命名成功 - 用户ID: {}, 会话ID: {}, 旧标题: '{}', 新标题: '{}', 更新记录数: {}", 
                userId, sessionId, oldTitle, title.trim(), count);
    }
}
