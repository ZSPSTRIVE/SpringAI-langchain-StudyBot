package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AiChatRequest - AI聊天请求数据传输对象
 * 
 * 🎯 作用：用户与AI助手聊天时从前端接收的数据
 * 就像学生在"智能AI助手"聊天窗口发送消息，AI会回答问题、推荐学习资源等。
 * 
 * 📝 字段说明：
 * - message: 用户消息内容（必填）
 * - sessionId: 会话ID，用于多轮对话上下文连续
 * - needRecommendation: 是否需要AI推荐学习资源
 * - messageType: 消息类型，帮助AI理解意图
 * 
 * 💬 请求示例：
 * {
 *   "message": "什么是二叉树？",
 *   "sessionId": "session-12345",
 *   "needRecommendation": true,
 *   "messageType": "question"
 * }
 * 
 * 💡 使用场景：
 * - 学生在AI助手页面输入问题后点击发送
 * - 系统会调用 POST /api/ai/chat 接口
 * - AI会根据上下文（sessionId）和消息类型给出合适的回答
 */
@Data
public class AiChatRequest {
    
    /**
     * 用户消息内容（必填）
     * 用户发送给AI的问题或说明
     * 示例："什么是递归？"、"帮我解释一下指针的用法"
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    /**
     * 会话ID（可选）
     * 用于多轮对话，保持上下文连续性
     * 首次对话不需要传，后续AI会返回sessionId，前端下次请求时携带
     * 示例："session-abc123-20241117"
     */
    private String sessionId;
    
    /**
     * 是否需要学习资源推荐（默认false）
     * 如果设置true，AI会在回答中附带相关学习资源链接
     * 如视频教程、文章、课程等
     */
    private Boolean needRecommendation = false;
    
    /**
     * 消息类型（默认general）
     * - general: 通用聊天，闲聊或简单咨询
     * - question: 学习问题，AI会给出更专业的解答
     * - help: 寻求帮助，如使用系统功能的指导
     */
    private String messageType = "general";
}
