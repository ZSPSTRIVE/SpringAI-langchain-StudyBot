package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群消息实体类
 */
@Data
@TableName("chat_group_message")
public class ChatGroupMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群ID */
    private Long groupId;

    /** 发送者ID */
    private Long senderId;

    /** 消息内容 */
    private String content;

    /** 消息类型：TEXT-文本, IMAGE-图片, VIDEO-视频, FILE-文件, EMOJI-表情, VOICE-语音, SYSTEM-系统消息, FORWARD-转发 */
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

    /** @的用户ID列表，逗号分隔 */
    private String atUserIds;

    /** 是否@全体 */
    private Boolean atAll;

    /** 是否已撤回 */
    private Boolean isRecalled;

    /** 已读人数 */
    private Integer readCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    /** 发送者信息（非数据库字段） */
    @TableField(exist = false)
    private User sender;

    /** @的用户列表（非数据库字段） */
    @TableField(exist = false)
    private List<User> atUsers;

    /** 回复的消息（非数据库字段） */
    @TableField(exist = false)
    private ChatGroupMessage replyMessage;
}
