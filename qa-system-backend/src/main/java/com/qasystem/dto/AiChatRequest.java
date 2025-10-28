package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI聊天请求DTO
 */
@Data
public class AiChatRequest {
    
    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    /**
     * 会话ID（多轮对话）
     */
    private String sessionId;
    
    /**
     * 是否需要学习资源推荐
     */
    private Boolean needRecommendation = false;
    
    /**
     * 消息类型：general/question/help
     */
    private String messageType = "general";
}
