package com.qasystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AiChatResponse - AI聊天响应数据传输对象
 * 
 * 🎯 作用：AI助手处理完用户消息后返回的数据
 * 包含AI的回复内容、会话ID、资源推荐等信息。
 * 
 * 📝 字段说明：
 * - response: AI回复的内容
 * - sessionId: 会话ID，前端下次请求时携带用于多轮对话
 * - category: AI自动识别的问题分类
 * - recommendations: 推荐的学习资源列表
 * - tokensUsed: 该次对话消耗的token数
 * - conversationId: 对话记录ID，用户可以反馈回答质量
 * 
 * 💬 响应示例：
 * {
 *   "response": "二叉树是一种常见的数据结构，每个节点最多有两个子节点...",
 *   "sessionId": "session-abc123",
 *   "category": "数据结构",
 *   "recommendations": [
 *     {
 *       "title": "二叉树入门教程",
 *       "description": "详细讲解二叉树的基本概念",
 *       "url": "https://example.com/binary-tree",
 *       "type": "article"
 *     }
 *   ],
 *   "tokensUsed": 150,
 *   "conversationId": 12345
 * }
 * 
 * 💡 使用场景：
 * - 前端展示AI回复在聊天窗口
 * - 如果有推荐资源，会在回复下方展示为卡片
 * - sessionId保存用于下次请求，维持对话上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    
    /**
     * AI回复内容
     * AI处理用户问题后生成的回答，支持Markdown格式
     * 示例："二叉树是一种每个节点最多有两个子节点的树形结构..."
     */
    private String response;
    
    /**
     * 会话ID
     * 用于多轮对话，前端需要保存并在下次请求时携带
     * 这样AI才能记住之前的对话内容，给出更准确的回答
     */
    private String sessionId;
    
    /**
     * 问题分类
     * AI自动识别的问题所属科目或领域
     * 示例："数据结构"、"算法设计"、"高等数学"
     */
    private String category;
    
    /**
     * 推荐的学习资源列表
     * 如果用户请求时needRecommendation=true，AI会推荐相关学习资源
     * 包括文章、视频、课程等
     */
    private List<ResourceRecommendation> recommendations;
    
    /**
     * Token消耗量
     * 该次AI对话消耗的token数量，用于统计和费用控制
     * 示例：150（表示消耗了150个token）
     */
    private Integer tokensUsed;
    
    /**
     * 对话ID
     * 数据库中的对话记录ID，用于用户反馈功能
     * 用户可以对AI回答点赞或者反馈问题
     */
    private Long conversationId;
    
    /**
     * 学习资源推荐内部类
     * 包含推荐资源的详细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceRecommendation {
        /** 资源标题 */
        private String title;
        
        /** 资源描述，简要说明内容 */
        private String description;
        
        /** 资源链接URL */
        private String url;
        
        /** 
         * 资源类型
         * article - 文章
         * video - 视频
         * course - 课程
         * document - 文档
         */
        private String type;
    }
}
