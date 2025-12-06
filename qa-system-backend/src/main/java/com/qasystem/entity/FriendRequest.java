package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友申请实体类
 */
@Data
@TableName("friend_request")
public class FriendRequest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送方用户ID */
    private Long fromUserId;

    /** 接收方用户ID */
    private Long toUserId;

    /** 验证消息 */
    private String message;

    /** 状态：PENDING-待处理, ACCEPTED-已同意, REJECTED-已拒绝 */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 发送方用户信息（非数据库字段） */
    @TableField(exist = false)
    private User fromUser;

    /** 接收方用户信息（非数据库字段） */
    @TableField(exist = false)
    private User toUser;
}
