package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私聊消息实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 发送者ID */
    private Long senderId;

    /** 接收者ID */
    private Long receiverId;

    /** 消息内容 */
    private String content;

    /** 消息类型：TEXT-文本, IMAGE-图片, VIDEO-视频, FILE-文件, EMOJI-表情, VOICE-语音, FORWARD-转发 */
    private String type;

    /** 媒体文件URL */
    private String mediaUrl;

    /** 媒体缩略图URL */
    private String mediaThumbnail;

    /** 媒体时长(秒) */
    private Integer mediaDuration;

    /** 媒体文件大小(字节) */
    private Long mediaSize;

    /** 转发来源消息ID */
    private Long forwardFromId;

    /** 转发来源用户名 */
    private String forwardFromUser;

    /** 回复的消息ID */
    private Long replyToId;

    /** 是否已读 */
    private Boolean isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 是否已撤回 */
    private Boolean isRecalled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    /** 发送者信息（非数据库字段） */
    @TableField(exist = false)
    private User sender;

    /** 回复的消息（非数据库字段） */
    @TableField(exist = false)
    private ChatMessage replyMessage;
}
