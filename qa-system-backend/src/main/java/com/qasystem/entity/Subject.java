package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科目实体类 - 对应数据库subject表
 * 
 * 🎯 作用说明：
 * 存储科目分类信息,用于组织和分类问题
 * 就像图书馆的书籍分类,帮助快速查找相关内容
 * 
 * 📊 对应数据库表: subject
 * 
 * 🔗 关系说明：
 * - 包含多个问题(Question表) - 一对多
 * - 用于问题的分类和筛选
 * - 例如:Java科目下有很多Java相关的问题
 * 
 * 💡 使用场景：
 * 1. 管理员创建和管理科目
 * 2. 学生选择科目发布问题
 * 3. 按科目浏览和筛选问题
 * 4. 首页显示科目列表
 * 5. 统计各科目的问题数量
 * 
 * 📝 注解说明：
 * @Data - Lombok注解,自动生成getter、setter等方法
 * @TableName - 指定对应的数据库表名
 * implements Serializable - 实现序列化
 * 
 * @author QA System Team
 * @version 1.0
 * @since 2024-01-01
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

