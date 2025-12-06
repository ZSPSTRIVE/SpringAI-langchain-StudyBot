package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群成员实体类
 */
@Data
@TableName("chat_group_member")
public class ChatGroupMember implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群ID */
    private Long groupId;

    /** 用户ID */
    private Long userId;

    /** 群昵称 */
    private String nickname;

    /** 角色：OWNER-群主, ADMIN-管理员, MEMBER-普通成员 */
    private String role;

    /** 是否被禁言 */
    private Boolean isMuted;

    /** 禁言结束时间 */
    private LocalDateTime muteEndTime;

    /** 是否置顶该群 */
    private Boolean isTop;

    /** 是否免打扰 */
    private Boolean isDisturb;

    /** 最后阅读时间 */
    private LocalDateTime lastReadTime;

    /** 加入时间 */
    private LocalDateTime joinTime;

    /** 邀请人ID */
    private Long inviteUserId;

    @TableLogic
    private Integer deleted;

    /** 用户信息（非数据库字段） */
    @TableField(exist = false)
    private User user;
}
