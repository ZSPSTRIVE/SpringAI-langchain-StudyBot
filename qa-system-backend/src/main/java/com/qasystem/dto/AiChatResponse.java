package com.qasystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    
    /**
     * AI回复内容
     */
    private String response;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 问题分类
     */
    private String category;
    
    /**
     * 推荐的学习资源
     */
    private List<ResourceRecommendation> recommendations;
    
    /**
     * Token消耗
     */
    private Integer tokensUsed;
    
    /**
     * 对话ID（用于反馈）
     */
    private Long conversationId;
    
    /**
     * 学习资源推荐
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceRecommendation {
        private String title;
        private String description;
        private String url;
        private String type; // article/video/course/document
    }
}
