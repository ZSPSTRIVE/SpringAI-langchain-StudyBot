package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 */
@Data
@TableName("chat_conversation")
public class ChatConversation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 对方ID（私聊为用户ID，群聊为群ID） */
    private Long targetId;

    /** 会话类型：PRIVATE-私聊, GROUP-群聊 */
    private String type;

    /** 最后一条消息内容 */
    private String lastMessage;

    /** 最后消息时间 */
    private LocalDateTime lastMessageTime;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 是否置顶 */
    private Boolean isTop;

    /** 是否免打扰 */
    private Boolean isMuted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /** 对方用户信息（私聊时使用，非数据库字段） */
    @TableField(exist = false)
    private User targetUser;

    /** 群信息（群聊时使用，非数据库字段） */
    @TableField(exist = false)
    private ChatGroup group;
}
