package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话历史实体
 */
@Data
@TableName("ai_conversation")
public class AiConversation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话ID（用于多轮对话）
     */
    private String sessionId;
    
    /**
     * 用户消息
     */
    private String userMessage;
    
    /**
     * AI回复
     */
    @TableField(value = "ai_response")
    private String aiResponse;
    
    /**
     * 消息类型：text/question/resource
     */
    private String messageType;
    
    /**
     * 问题分类：学习问题/技术问题/课程问题/其他
     */
    private String questionCategory;
    
    /**
     * 是否已收藏
     */
    private Boolean isBookmarked;
    
    /**
     * 用户反馈：helpful/not_helpful
     */
    private String feedback;
    
    /**
     * 推荐的学习资源（JSON格式）
     */
    private String recommendedResources;
    
    /**
     * Token消耗
     */
    private Integer tokensUsed;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
