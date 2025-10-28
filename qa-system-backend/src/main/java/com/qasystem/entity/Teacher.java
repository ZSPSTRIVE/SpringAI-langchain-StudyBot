package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教师信息扩展表
 */
@Data
@TableName("teacher")
public class Teacher implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 工号
     */
    private String teacherNo;

    /**
     * 职称
     */
    private String title;

    /**
     * 学院
     */
    private String college;

    /**
     * 研究方向
     */
    private String research;

    /**
     * 办公室
     */
    private String office;

    /**
     * 个人简介
     */
    private String bio;

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

