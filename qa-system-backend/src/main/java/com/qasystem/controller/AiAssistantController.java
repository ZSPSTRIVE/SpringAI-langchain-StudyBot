package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.dto.ConversationFeedbackRequest;
import com.qasystem.dto.RenameSessionRequest;
import com.qasystem.entity.AiConversation;
import com.qasystem.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 🤖 AI助手控制器 - 处理用户与AI助手的交互请求
 * 
 * 📖 功能说明：
 * AI助手模块是系统的核心功能，为师生提供智能答疑服务。
 * 本控制器主要功能包括：
 * 1. 智能对话 - 处理用户提问，返回AI生成的回答
 * 2. 会话管理 - 创建、查询、重命名、删除对话会话
 * 3. 历史记录 - 查询历史对话内容和上下文
 * 4. 反馈收集 - 收集用户对AI回答的评价和反馈
 * 5. 收藏功能 - 允许用户收藏有价值的对话内容
 * 6. 上下文记忆 - 维持多轮对话的上下文连贯性
 * 
 * 🔒 权限控制：
 * - 所有接口需要用户登录认证
 * - 用户只能访问自己的对话记录
 * - 通过Spring Security在方法层面验证用户身份
 * 
 * 🌍 RESTful 设计：
 * POST   /api/ai/chat                          发起AI对话
 * GET    /api/ai/sessions                       获取会话列表
 * GET    /api/ai/sessions/{id}/history          获取会话历史
 * POST   /api/ai/feedback                       提交反馈
 * POST   /api/ai/bookmark/{id}                  收藏/取消收藏对话
 * GET    /api/ai/bookmarks                      获取收藏的对话
 * DELETE /api/ai/sessions/{id}                  删除会话
 * PUT    /api/ai/sessions/{id}/rename            重命名会话
 * 
 * 🤖 AI技术栈：
 * - 使用LangChain框架集成大语言模型
 * - 支持多种AI模型切换（GPT、Claude等）
 * - 实现向量数据库存储知识库
 * - 支持RAG（检索增强生成）技术
 * 
 * 📝 使用场景：
 * - 学生提问学习问题，AI提供解答
 * - 教师获取教学建议和资源推荐
 * - 系统自动生成学习路径和练习题
 * - 提供个性化的学习辅导
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象log，用于记录AI对话日志
@RestController  // 标识这是一个REST控制器，返回JSON数据
@RequestMapping("/api/ai")  // 定义AI助手接口的基础路径
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class AiAssistantController {
    
    // AI助手服务层接口，处理所有AI相关的业务逻辑
    // final确保注入后不可修改
    private final AiAssistantService aiAssistantService;
    
    /**
     * 💬 AI聊天对话 - 处理用户提问并返回AI生成的回答
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 解析用户请求：消息内容、会话ID、是否需要推荐资源
     * 3. 检查会话ID，如果是新会话则创建
     * 4. 将用户消息添加到对话历史
     * 5. 调用AI模型生成回答：
     *    - 构建包含历史对话的提示词
     *    - 如果启用RAG，先检索相关知识
     *    - 调用大语言模型API
     *    - 解析并验证AI回答内容
     * 6. 处理AI回答：
     *    - 过滤敏感内容
     *    - 检测回答质量
     *    - 统计token消耗量
     * 7. 保存对话记录到数据库
     * 8. 更新会话的最后活动时间
     * 9. 如果需要推荐资源，调用知识库检索相关资源
     * 10. 返回完整的响应对象
     * 
     * 技术实现：
     * - 使用LangChain框架处理AI模型调用
     * - 支持流式响应，提高用户体验
     * - 实现对话上下文管理，维持多轮对话连贯性
     * - 集成向量数据库，实现RAG增强回答
     * 
     * 请求示例：
     * POST /api/ai/chat
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * Body:
     * {
     *   "message": "请解释一下二叉树的概念",
     *   "sessionId": "session-abc123",  // 可选，新对话可不传
     *   "needRecommendation": true     // 可选，是否需要推荐学习资源
     * }
     * 
     * 成功响应示例：
     * {
     *   "code": 200,
     *   "message": "成功",
     *   "data": {
     *     "response": "二叉树是一种每个节点最多有两个子节点的树形数据结构...",
     *     "sessionId": "session-abc123",
     *     "category": "数据结构",
     *     "recommendations": [
     *       {
     *         "title": "二叉树入门教程",
     *         "description": "详细讲解二叉树的基本概念和遍历方法",
     *         "url": "https://example.com/binary-tree",
     *         "type": "article"
     *       }
     *     ],
     *     "tokensUsed": 150,
     *     "conversationId": 12345
     *   }
     * }
     * 
     * @param request AI聊天请求对象，包含用户消息和会话信息
     *                @Valid - 启用参数校验
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<AiChatResponse> AI聊天响应对象，包含回答和会话信息
     * @throws BusinessException 当AI服务不可用或请求参数无效时抛出
     */
    @PostMapping("/chat")  // 处理POST请求
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request, 
                                       Authentication authentication) {
        // 从认证信息中获取用户ID
        Long userId = getUserId(authentication);
        // 记录AI对话日志，包含用户ID和消息内容
        log.info("用户 {} 发起AI对话: {}", userId, request.getMessage());
        
        // 调用服务层处理AI对话
        // 服务层会调用AI模型并处理整个对话流程
        AiChatResponse response = aiAssistantService.chat(userId, request);
        
        // 返回AI响应
        return Result.success(response);
    }
    
    /**
     * 🌊 流式AI聊天对话 - 使用SSE实现打字机效果的实时响应
     * 
     * 业务流程：
     * 1. 建立SSE连接
     * 2. 发送会话ID给客户端
     * 3. AI生成回答过程中，实时推送每个token
     * 4. 生成完成后发送done事件
     * 
     * SSE事件类型：
     * - session: 会话信息 {"sessionId":"xxx"}
     * - message: AI回复的token片段
     * - done: 完成事件 {"conversationId":123,"category":"xxx"}
     * - error: 错误事件 {"error":"错误信息"}
     * 
     * 请求示例：
     * POST /api/ai/chat/stream
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     *   Accept: text/event-stream
     * Body:
     * {
     *   "message": "请解释一下二叉树的概念",
     *   "sessionId": "session-abc123"
     * }
     * 
     * @param request AI聊天请求对象
     * @param authentication Spring Security认证对象
     * @return SseEmitter SSE事件流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequest request,
                                  Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("用户 {} 发起流式AI对话: {}", userId, request.getMessage());
        return aiAssistantService.chatStream(userId, request);
    }
    
    /**
     * 📋 获取用户的会话列表 - 展示用户所有的AI对话会话
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 查询用户的所有会话：
     *    - 按最后活动时间降序排序
     *    - 限制返回数量，避免数据过多
     *    - 只返回会话基本信息，不包含详细对话内容
     * 3. 统计每个会话的消息数量
     * 4. 获取每个会话的最后一条消息预览
     * 5. 标记收藏状态
     * 6. 组装会话列表返回
     * 
     * 会话信息包含：
     * - 会话ID
     * - 会话标题（自动生成或用户自定义）
     * - 创建时间
     * - 最后活动时间
     * - 消息数量
     * - 最后一条消息预览
     * - 是否已收藏
     * 
     * 请求示例：
     * GET /api/ai/sessions?limit=20
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * @param limit 返回的会话数量限制，默认20，最大100
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<List<AiConversation>> 会话列表，每个会话包含基本信息
     */
    @GetMapping("/sessions")  // 处理GET请求
    public Result<List<AiConversation>> getUserSessions(@RequestParam(defaultValue = "20") Integer limit,
                                                        Authentication authentication) {
        // 从认证信息中获取用户ID
        Long userId = getUserId(authentication);
        // 调用服务层获取用户会话列表
        List<AiConversation> sessions = aiAssistantService.getUserSessions(userId, limit);
        // 返回会话列表
        return Result.success(sessions);
    }
    
    /**
     * 📜 获取会话历史 - 查询特定会话的完整对话记录
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 验证会话ID是否属于当前用户
     * 3. 查询会话的所有对话记录：
     *    - 按时间顺序排序
     *    - 包含用户问题和AI回答
     *    - 包含每条消息的时间戳
     * 4. 获取会话的基本信息（标题、创建时间等）
     * 5. 返回完整的对话历史
     * 
     * 使用场景：
     * - 用户查看历史对话内容
     * - 继续之前的对话，AI可以基于历史上下文回答
     * - 分享有价值的对话内容给其他同学
     * 
     * 请求示例：
     * GET /api/ai/sessions/session-abc123/history
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * @param sessionId 会话ID，从URL路径获取
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<List<AiConversation>> 对话历史列表，按时间顺序排列
     * @throws NotFoundException 当会话不存在或不属于当前用户时抛出
     */
    @GetMapping("/sessions/{sessionId}/history")  // 处理GET请求
    public Result<List<AiConversation>> getSessionHistory(@PathVariable String sessionId,
                                                          Authentication authentication) {
        // 从认证信息中获取用户ID
        Long userId = getUserId(authentication);
        // 调用服务层获取会话历史
        List<AiConversation> history = aiAssistantService.getSessionHistory(userId, sessionId);
        // 返回会话历史
        return Result.success(history);
    }
    
    /**
     * 👍 提交反馈 - 用户对AI回答质量进行评价
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 验证对话ID是否属于当前用户
     * 3. 记录用户反馈：
     *    - 点赞/点踩
     *    - 文字评价（可选）
     *    - 反馈时间
     * 4. 更新对话的反馈统计
     * 5. 如果是负面反馈，记录到问题列表供人工审核
     * 6. 将反馈数据用于AI模型优化
     * 
     * 反馈类型：
     * - LIKE: 点赞，表示回答有帮助
     * - DISLIKE: 点踩，表示回答不准确或无帮助
     * - REPORT: 举报，表示回答有问题或不合适
     * 
     * 请求示例：
     * POST /api/ai/feedback
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * Body:
     * {
     *   "conversationId": 12345,
     *   "feedback": "LIKE",
     *   "comment": "回答很详细，解决了我的问题"  // 可选
     * }
     * 
     * @param request 反馈请求对象，包含对话ID和反馈内容
     *                @Valid - 启用参数校验
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<Void> 无数据返回
     * @throws NotFoundException 当对话不存在或不属于当前用户时抛出
     */
    @PostMapping("/feedback")  // 处理POST请求
    public Result<Void> submitFeedback(@Valid @RequestBody ConversationFeedbackRequest request,
                                       Authentication authentication) {
        Long userId = getUserId(authentication);
        aiAssistantService.submitFeedback(userId, request.getConversationId(), request.getFeedback());
        // 返回成功响应
        return Result.success();
    }
    
    /**
     * ⭐ 收藏/取消收藏对话 - 用户标记有价值的对话内容
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 验证对话ID是否属于当前用户
     * 3. 根据isBookmarked参数执行收藏或取消收藏：
     *    - 收藏：将对话添加到用户的收藏列表
     *    - 取消收藏：从用户的收藏列表移除对话
     * 4. 更新对话的收藏状态
     * 5. 返回操作结果
     * 
     * 使用场景：
     * - 用户收藏有价值的问答内容，方便后续查看
     * - 教师收藏优秀的学生问题，用于教学案例
     * - 收藏AI推荐的学习资源，方便后续学习
     * 
     * 请求示例：
     * POST /api/ai/bookmark/12345?isBookmarked=true
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * @param conversationId 对话ID，从URL路径获取
     * @param isBookmarked 是否收藏，true为收藏，false为取消收藏
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<Void> 无数据返回
     * @throws NotFoundException 当对话不存在或不属于当前用户时抛出
     */
    @PostMapping("/bookmark/{conversationId}")  // 处理POST请求
    public Result<Void> bookmarkConversation(@PathVariable Long conversationId,
                                            @RequestParam Boolean isBookmarked,
                                            Authentication authentication) {
        Long userId = getUserId(authentication);
        aiAssistantService.bookmarkConversation(userId, conversationId, isBookmarked);
        // 返回成功响应
        return Result.success();
    }
    
    /**
     * 🌟 获取收藏的对话 - 查询用户收藏的所有有价值对话
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 查询用户收藏的所有对话：
     *    - 按收藏时间降序排序
     *    - 包含对话基本信息和内容预览
     *    - 支持分页查询，避免数据过多
     * 3. 返回收藏列表
     * 
     * 使用场景：
     * - 用户查看自己收藏的有价值内容
     * - 复习重要的知识点和解答
     * - 整理学习资料，构建个人知识库
     * 
     * 请求示例：
     * GET /api/ai/bookmarks
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<List<AiConversation>> 收藏的对话列表
     */
    @GetMapping("/bookmarks")  // 处理GET请求
    public Result<List<AiConversation>> getBookmarkedConversations(Authentication authentication) {
        // 从认证信息中获取用户ID
        Long userId = getUserId(authentication);
        // 调用服务层获取收藏的对话
        List<AiConversation> bookmarks = aiAssistantService.getBookmarkedConversations(userId);
        // 返回收藏列表
        return Result.success(bookmarks);
    }
    
    /**
     * 🗑️ 删除会话 - 用户删除不再需要的对话会话
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 验证会话ID是否属于当前用户
     * 3. 执行软删除：
     *    - 标记会话为已删除，而不是物理删除
     *    - 保留数据用于分析和审计
     *    - 用户无法再访问该会话
     * 4. 清除相关的缓存数据
     * 5. 返回操作结果
     * 
     * 删除规则：
     * - 使用软删除，保留数据用于分析
     * - 删除后用户无法恢复会话
     * - 管理员可以查看已删除的会话用于审计
     * 
     * 请求示例：
     * DELETE /api/ai/sessions/session-abc123
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * 
     * @param sessionId 会话ID，从URL路径获取
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<Void> 无数据返回
     * @throws NotFoundException 当会话不存在或不属于当前用户时抛出
     */
    @DeleteMapping("/sessions/{sessionId}")  // 处理DELETE请求
    public Result<Void> deleteSession(@PathVariable String sessionId,
                                     Authentication authentication) {
        // 从认证信息中获取用户ID
        Long userId = getUserId(authentication);
        // 记录删除会话日志
        log.info("用户 {} 删除会话: {}", userId, sessionId);
        // 调用服务层删除会话
        aiAssistantService.deleteSession(userId, sessionId);
        // 返回成功响应
        return Result.success();
    }
    
    /**
     * ✏️ 重命名会话 - 用户自定义会话标题，便于识别和管理
     * 
     * 业务流程：
     * 1. 验证用户身份，获取用户ID
     * 2. 验证会话ID是否属于当前用户
     * 3. 验证新标题格式：
     *    - 长度限制：1-50个字符
     *    - 内容过滤：不允许包含敏感词
     * 4. 更新会话标题
     * 5. 更新会话的最后修改时间
     * 6. 返回操作结果
     * 
     * 使用场景：
     * - 用户根据讨论主题自定义会话名称
     * - 区分不同科目的学习会话
     * - 标记重要会话，方便后续查找
     * 
     * 请求示例：
     * PUT /api/ai/sessions/session-abc123/rename
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiI...
     * Body:
     * {
     *   "title": "二叉树数据结构学习"
     * }
     * 
     * @param sessionId 会话ID，从URL路径获取
     * @param request 重命名请求对象，包含新标题
     * @param authentication Spring Security认证对象，包含用户信息
     * @return Result<Void> 无数据返回
     * @throws NotFoundException 当会话不存在或不属于当前用户时抛出
     * @throws BusinessException 当标题格式不合规时抛出
     */
    @PutMapping("/sessions/{sessionId}/rename")  // 处理PUT请求
    public Result<Void> renameSession(@PathVariable String sessionId,
                                     @RequestBody RenameSessionRequest request,
                                     Authentication authentication) {
        // 从认证信息中获取用户ID
        Long userId = getUserId(authentication);
        // 记录重命名会话日志
        log.info("用户 {} 重命名会话: {} -> {}", userId, sessionId, request.getTitle());
        // 调用服务层重命名会话
        aiAssistantService.renameSession(userId, sessionId, request.getTitle());
        // 返回成功响应
        return Result.success();
    }
    
    /**
     * 🔐 从认证信息中获取用户ID - 辅助方法，统一处理用户身份验证
     * 
     * 实现逻辑：
     * 1. 检查认证对象是否为空
     * 2. 检查认证对象的主体是否为空
     * 3. 从主体中获取用户名（在JWT认证中，用户名存储的是用户ID）
     * 4. 将用户名转换为Long类型的用户ID
     * 5. 返回用户ID
     * 
     * 安全考虑：
     * - 严格验证认证信息，防止未授权访问
     * - 统一处理认证异常，提供一致的错误响应
     * - 不暴露敏感信息到日志中
     * 
     * @param authentication Spring Security认证对象
     * @return Long 用户ID
     * @throws RuntimeException 当用户未登录或认证信息无效时抛出
     */
    private Long getUserId(Authentication authentication) {
        // 检查认证对象是否为空
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        // 从认证对象中获取用户ID（在JWT认证中，用户名存储的是用户ID）
        return Long.parseLong(authentication.getName());
    }
}
