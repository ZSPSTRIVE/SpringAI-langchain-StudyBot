package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回答实体
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

