package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关注实体
 */
@Data
@TableName("follow")
public class Follow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关注者ID（学生）
     */
    private Long followerId;

    /**
     * 被关注者ID（教师）
     */
    private Long followeeId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

