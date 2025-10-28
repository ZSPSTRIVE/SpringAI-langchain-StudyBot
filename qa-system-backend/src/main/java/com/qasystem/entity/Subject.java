package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科目实体
 */
@Data
@TableName("subject")
public class Subject implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 科目代码
     */
    private String code;

    /**
     * 科目描述
     */
    private String description;

    /**
     * 图标URL
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：ACTIVE-启用, DISABLED-禁用
     */
    private String status;

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

