package com.qasystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QuestionDTO - 问题数据传输对象
 * 
 * 🎯 作用：返回给前端的问题详细信息
 * 用于在问题列表、问题详情等页面展示问题的完整信息。
 * 相比数据库实体，DTO增加了关联数据（如科目名称、学生姓名）和统计数据（如浏览数、回答数）。
 * 
 * 📝 字段分类：
 * 1. 基本信息：id, title, content, images, createTime, updateTime
 * 2. 关联信息：subjectId, subjectName, studentId, studentName
 * 3. 状态信息：status（待解决/已解决/已关闭）
 * 4. 统计信息：viewCount, answerCount
 * 5. 标记信息：isTop（置顶）, isFeatured（精选）
 * 
 * 💬 响应示例：
 * {
 *   "id": 123,
 *   "subjectId": 1,
 *   "subjectName": "数据结构",
 *   "studentId": 10,
 *   "studentName": "张三",
 *   "title": "二叉树的前序遍历怎么实现？",
 *   "content": "老师您好，我在学习二叉树时...",
 *   "images": ["/uploads/question/img1.jpg"],
 *   "status": "PENDING",
 *   "viewCount": 25,
 *   "isTop": 0,
 *   "isFeatured": 1,
 *   "answerCount": 3,
 *   "createTime": "2024-11-17T10:30:00",
 *   "updateTime": "2024-11-17T15:20:00"
 * }
 */
@Data
public class QuestionDTO {
    /** 问题ID */
    private Long id;
    
    /** 科目ID */
    private Long subjectId;
    
    /** 科目名称，如"数据结构"、"高等数学" */
    private String subjectName;
    
    /** 提问学生的ID */
    private Long studentId;
    
    /** 提问学生的姓名 */
    private String studentName;
    
    /** 问题标题 */
    private String title;
    
    /** 问题详细内容，支持Markdown格式 */
    private String content;
    
    /** 问题配图URL列表 */
    private List<String> images;
    
    /** 
     * 问题状态
     * PENDING - 待解决（刚发布，还没有回答或没有被采纳）
     * RESOLVED - 已解决（有回答被学生采纳）
     * CLOSED - 已关闭（学生或管理员关闭）
     */
    private String status;
    
    /** 浏览次数，记录有多少人查看过这个问题 */
    private Integer viewCount;
    
    /** 是否置顶：0-否，1-是（管理员可以将热门问题置顶） */
    private Integer isTop;
    
    /** 是否精选：0-否，1-是（管理员可以将优质问题标记为精选） */
    private Integer isFeatured;
    
    /** 回答数量，有多少个教师回答了这个问题 */
    private Integer answerCount;
    
    /** 创建时间（问题发布时间） */
    private LocalDateTime createTime;
    
    /** 更新时间（最后修改时间，如编辑问题、新增回答等） */
    private LocalDateTime updateTime;
}

