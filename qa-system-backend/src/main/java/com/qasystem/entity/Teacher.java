package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教师信息实体类 - 对应数据库teacher表
 * 
 * 🎯 作用说明：
 * 存储教师的职业相关信息,是User表的扩展
 * 就像教师人事档案,记录工号、职称、研究方向等专业信息
 * 
 * 📊 对应数据库表: teacher
 * 
 * 🔗 关系说明：
 * - 与User表一对一关系(通过userId关联)
 * - User表存储基本信息(用户名、密码、邮箱等)
 * - Teacher表存储教师特有信息(工号、职称、研究方向等)
 * 
 * 💡 使用场景：
 * 1. 教师注册时创建Teacher记录
 * 2. 查看教师详情时JOIN User表获取完整信息
 * 3. 按职称/学院筛选教师
 * 4. 展示教师的研究方向和办公室信息
 * 
 * 📝 注解说明：
 * @Data - Lombok注解,自动生成getter、setter、toString等方法
 * @TableName - 指定对应的数据库表名
 * implements Serializable - 实现序列化,可以存入Redis缓存
 * 
 * @author QA System Team
 * @version 1.0
 * @since 2024-01-01
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

