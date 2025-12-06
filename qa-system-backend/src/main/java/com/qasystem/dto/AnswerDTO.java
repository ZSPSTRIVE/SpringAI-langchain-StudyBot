package com.qasystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AnswerDTO - 回答数据传输对象
 * 
 * 🎯 作用：返回给前端的回答详细信息
 * 用于在问题详情页展示教师的回答列表，包含回答内容、教师信息和互动数据。
 * 
 * 📝 字段分类：
 * 1. 基本信息：id, content, images, createTime, updateTime
 * 2. 关联信息：questionId, teacherId, teacherName, teacherTitle
 * 3. 状态信息：isAccepted（是否被采纳）
 * 4. 互动信息：likeCount（点赞数）
 * 
 * 💬 响应示例：
 * {
 *   "id": 456,
 *   "questionId": 123,
 *   "teacherId": 5,
 *   "teacherName": "李老师",
 *   "teacherTitle": "副教授",
 *   "content": "你的问题在于对递归理解不够深入...",
 *   "images": ["/uploads/answer/solution.jpg"],
 *   "isAccepted": 1,
 *   "likeCount": 15,
 *   "createTime": "2024-11-17T14:20:00",
 *   "updateTime": "2024-11-17T14:20:00"
 * }
 * 
 * 💡 使用场景：
 * - 学生查看问题详情时，下方显示所有教师的回答列表
 * - 被采纳的回答（isAccepted=1）会显示在最前面并高亮
 * - 显示教师姓名、职称，增加回答的权威性
 */
@Data
public class AnswerDTO {
    /** 回答ID */
    private Long id;
    
    /** 所属问题的ID */
    private Long questionId;
    
    /** 回答教师的ID */
    private Long teacherId;
    
    /** 回答教师的姓名 */
    private String teacherName;
    
    /** 
     * 回答教师的职称
     * 示例："助教"、"讲师"、"副教授"、"教授"
     * 用于展示教师的专业资质，增加回答的可信度
     */
    private String teacherTitle;
    
    /** 回答详细内容，支持Markdown格式 */
    private String content;
    
    /** 回答配图URL列表，如解题步骤图、代码示例等 */
    private List<String> images;
    
    /** 
     * 是否被采纳：0-未采纳，1-已采纳
     * 学生可以将最满意的回答设置为"采纳答案"，相当于"最佳答案"
     * 被采纳的回答会在列表中置顶显示，教师也会获得更多积分
     */
    private Integer isAccepted;
    
    /** 
     * 点赞数
     * 其他学生或教师可以为优质回答点赞
     * 点赞数高的回答说明更受欢迎，更有参考价值
     */
    private Integer likeCount;
    
    /** 回答创建时间（教师提交回答的时间） */
    private LocalDateTime createTime;
    
    /** 回答更新时间（最后编辑时间） */
    private LocalDateTime updateTime;
}

