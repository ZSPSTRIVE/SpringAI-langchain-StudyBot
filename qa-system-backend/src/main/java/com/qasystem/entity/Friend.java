package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
@Data
@TableName("friend")
public class Friend implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 好友ID */
    private Long friendId;

    /** 状态：PENDING-待确认, ACCEPTED-已接受, REJECTED-已拒绝, BLOCKED-已屏蔽 */
    private String status;

    /** 好友备注名 */
    private String remark;

    /** 好友分组 */
    private String groupName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
