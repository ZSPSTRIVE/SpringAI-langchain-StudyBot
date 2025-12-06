package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关注关系实体类 - 对应数据库follow表
 * 
 * 🎯 作用说明：
 * 存储用户之间的关注关系(主要是学生关注教师)
 * 就像通讯录或好友列表,记录你关注了哪些人
 * 
 * 📊 对应数据库表: follow
 * 
 * 🔗 关系说明：
 * - 多对多关系(通过中间表实现)
 * - 一个学生可以关注多个教师
 * - 一个教师可以被多个学生关注
 * - followerId: 关注者(通常是学生)
 * - followeeId: 被关注者(通常是教师)
 * 
 * 💡 使用场景：
 * 1. 学生关注感兴趣的教师
 * 2. 查看我关注的教师列表
 * 3. 查看关注我的学生列表(教师视角)
 * 4. 取消关注
 * 5. 关注的教师发布新回答时接收通知
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
@TableName("follow")
public class Follow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关注者ID（学生）
     */
    private Long followerId;

    /**
     * 被关注者ID（教师）
     */
    private Long followeeId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

