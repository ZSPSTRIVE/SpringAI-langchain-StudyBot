package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话历史实体类 - 对应数据库ai_conversation表
 * 
 * 🎯 作用说明：
 * 存储用户与AI助手的对话记录,支持多轮对话和会话管理
 * 就像聊天记录,保存每一次对话的内容和AI的回复
 * 
 * 📊 对应数据库表: ai_conversation
 * 
 * 🔗 关系说明：
 * - 属于某个用户(userId)
 * - 通过sessionId关联多轮对话
 * - 同一个sessionId的记录组成一个完整的对话会话
 * 
 * 💡 使用场景：
 * 1. AI助手功能 - 学生提问,AI回答
 * 2. 对话历史查询 - 查看之前的对话记录
 * 3. 会话管理 - 管理多个对话主题
 * 4. 数据分析 - 统计使用情况和Token消耗
 * 5. 用户反馈 - 收集AI回答的有效性反馈
 * 
 * @author QA System Team
 * @version 1.0
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
     * 会话标题（用户自定义）
     */
    private String sessionTitle;
    
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
