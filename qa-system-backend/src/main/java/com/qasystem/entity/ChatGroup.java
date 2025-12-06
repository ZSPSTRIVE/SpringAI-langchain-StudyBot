package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群聊实体类
 */
@Data
@TableName("chat_group")
public class ChatGroup implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群名称 */
    private String name;

    /** 群头像URL */
    private String avatar;

    /** 群主ID */
    private Long ownerId;

    /** 群公告 */
    private String announcement;

    /** 群描述 */
    private String description;

    /** 最大成员数 */
    private Integer maxMembers;

    /** 当前成员数 */
    private Integer memberCount;

    /** 群类型：NORMAL-普通群, CLASS-班级群, COURSE-课程群 */
    private String type;

    /** 加入方式：FREE-自由加入, APPROVAL-需审批, INVITE-仅邀请 */
    private String joinType;

    /** 是否全员禁言 */
    private Boolean isMuted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /** 群主信息（非数据库字段） */
    @TableField(exist = false)
    private User owner;

    /** 群成员列表（非数据库字段） */
    @TableField(exist = false)
    private List<ChatGroupMember> members;
}
