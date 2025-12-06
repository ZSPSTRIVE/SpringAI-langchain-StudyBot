package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ConversationFeedbackRequest - AI对话反馈请求数据传输对象
 * 
 * 🎯 作用：用户对AI助手的回答进行反馈时使用
 * 就像用户在AI回答下方点击“有帮助”或“无帮助”按钮，帮助AI改进回答质量。
 * 
 * 📝 字段说明：
 * - conversationId: 对话ID，指定要反馈的是哪次对话
 * - feedback: 反馈类型，helpful（有帮助）或not_helpful（无帮助）
 * - comment: 反馈备注，用户可以详细说明问题
 * 
 * 💬 请求示例：
 * {
 *   "conversationId": 12345,
 *   "feedback": "helpful",
 *   "comment": "AI的回答非常详细，帮我解决了问题"
 * }
 * 
 * 💡 使用场景：
 * - 用户在AI聊天窗口查看AI回答
 * - 每条AI回答下方有“有帮助”和“无帮助”按钮
 * - 点击后调用 POST /api/ai/conversation/feedback 接口
 * - 系统收集反馈数据用于优化AI模型和提示词
 */
@Data
public class ConversationFeedbackRequest {
    
    /**
     * 对话ID（必填）
     * 指定要反馈的AI对话记录
     * 从 AiChatResponse 的 conversationId 字段获取
     */
    @NotNull(message = "对话ID不能为空")
    private Long conversationId;
    
    /**
     * 反馈类型（必填）
     * - helpful: 有帮助，表示AI回答解决了用户问题
     * - not_helpful: 无帮助，表示AI回答没有解决问题或不准确
     */
    @NotBlank(message = "反馈类型不能为空")
    private String feedback;
    
    /**
     * 反馈备注（可选）
     * 用户可以详细说明AI回答的好与不好，帮助系统改进
     * 示例：“解释很详细”、“没有回答我的问题”、“代码示例有错误”
     */
    private String comment;
}

