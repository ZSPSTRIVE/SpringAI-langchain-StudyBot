package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类 - 对应数据库collection表
 * 
 * 🎯 作用说明：
 * 存储用户的收藏记录(可以收藏问题、回答或帖子)
 * 就像浏览器的收藏夹,保存感兴趣的内容以便后续查看
 * 
 * 📊 对应数据库表: collection
 * 
 * 🔗 关系说明：
 * - 用户可以收藏多个对象 - 一对多
 * - 采用多态设计(targetType + targetId)
 * - targetType指定收藏类型:QUESTION/ANSWER/POST
 * - targetId指定具体收藏的对象ID
 * 
 * 💡 使用场景：
 * 1. 收藏感兴趣的问题或回答
 * 2. 查看我的收藏列表
 * 3. 取消收藏
 * 4. 统计收藏数量
 * 5. 按类型筛选收藏(只看问题/只看回答)
 * 
 * 💡 设计说明：
 * 类名使用UserCollection而不是Collection
 * 原因:避免与Java标准库java.util.Collection冲突
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
@TableName("collection")
public class UserCollection implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏对象类型：QUESTION-问题, ANSWER-回答, POST-帖子
     */
    private String targetType;

    /**
     * 收藏对象ID
     */
    private Long targetId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

