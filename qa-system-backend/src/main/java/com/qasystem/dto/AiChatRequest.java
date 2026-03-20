package com.qasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequest {

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String sessionId;

    private Boolean needRecommendation = false;

    private String knowledgeBaseId;

    private String knowledgePoint;

    private String messageType = "general";
}
