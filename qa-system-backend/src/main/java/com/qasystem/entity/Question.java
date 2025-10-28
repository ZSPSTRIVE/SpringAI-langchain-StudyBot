package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问题实体
 */
@Data
@TableName("question")
public class Question implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 科目ID
     */
    private Long subjectId;

    /**
     * 提问学生ID
     */
    private Long studentId;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题内容（富文本）
     */
    private String content;

    /**
     * 图片URL列表（JSON）
     */
    private String images;

    /**
     * 状态：PENDING-待回答, ANSWERED-已回答, CLOSED-已关闭
     */
    private String status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否精选
     */
    private Integer isFeatured;

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

