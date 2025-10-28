package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 对话反馈请求DTO
 */
@Data
public class ConversationFeedbackRequest {
    
    /**
     * 对话ID
     */
    @NotNull(message = "对话ID不能为空")
    private Long conversationId;
    
    /**
     * 反馈类型：helpful/not_helpful
     */
    @NotBlank(message = "反馈类型不能为空")
    private String feedback;
    
    /**
     * 反馈备注（可选）
     */
    private String comment;
}

