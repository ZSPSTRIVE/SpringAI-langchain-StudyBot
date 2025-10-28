package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体
 * 注意：类名改为UserCollection避免与java.util.Collection冲突
 */
@Data
@TableName("collection")
public class UserCollection implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏对象类型：QUESTION-问题, ANSWER-回答, POST-帖子
     */
    private String targetType;

    /**
     * 收藏对象ID
     */
    private Long targetId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

