package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回答实体类 - 对应数据库answer表
 * 
 * 🎯 作用说明：
 * 存储教师对学生问题的回答
 * 就像答题卡,教师针对每个问题给出的解答
 * 
 * 📊 对应数据库表: answer
 * 
 * 🔗 关系说明：
 * - 回答某个问题(questionId) - 多对一
 * - 由某个教师回答(teacherId) - 多对一
 * - 一个问题可以有多个回答
 * - 一个教师可以回答多个问题
 * 
 * 💡 使用场景：
 * 1. 教师回答学生问题
 * 2. 查看某个问题的所有回答
 * 3. 学生采纳最佳答案
 * 4. 统计教师的回答数量
 * 5. 显示回答的点赞数
 * 
 * 📝 注解说明：
 * @Data - Lombok注解,自动生成getter、setter等方法
 * @TableName - 指定对应的数据库表名
 * implements Serializable - 实现序列化
 * 
 * @author QA System Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@TableName("answer")
public class Answer implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 问题ID
     */
    private Long questionId;

    /**
     * 回答教师ID
     */
    private Long teacherId;

    /**
     * 回答内容（富文本）
     */
    private String content;

    /**
     * 图片URL列表（JSON）
     */
    private String images;

    /**
     * 是否被采纳
     */
    private Integer isAccepted;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    private Integer deleted;
}

