package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 论坛帖子实体类 - 对应数据库forum表(兼容旧系统)
 * 
 * 🎯 作用说明：
 * 存储论坛帖子和评论,支持树形结构的讨论
 * 就像留言板或论坛,大家可以发帖讨论,也可以回复评论
 * 
 * 📊 对应数据库表: forum
 * 
 * 🔗 关系说明：
 * - 树形结构设计(通过parentid实现)
 * - parentid=0: 表示顶级帖子(主题帖)
 * - parentid>0: 表示评论(回复某个帖子)
 * - 支持多层嵌套评论
 * 
 * 💡 使用场景：
 * 1. 发布讨论帖子
 * 2. 评论帖子
 * 3. 回复评论(多层嵌套)
 * 4. 查看帖子详情和所有评论
 * 5. 互助交流区
 * 
 * 🔧 兼容说明：
 * 此表兼容旧系统设计,字段命名保持原样
 * - parentid: 保持小写(旧系统命名)
 * - userid: 保持小写(旧系统命名)
 * - addtime: 保持原命名(旧系统命名)
 * 
 * 📝 注解说明：
 * @Data - Lombok注解,自动生成getter、setter等方法
 * @TableName - 指定对应的数据库表名
 * @TableField(exist = false) - 标记字段不映射到数据库
 * @JsonFormat - 指定JSON序列化时的日期格式
 * 
 * @author QA System Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@TableName("forum")
public class Forum implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 父节点ID（用于评论）
     */
    private Long parentid;

    /**
     * 用户ID
     */
    private Long userid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 状态
     */
    private String isdone;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addtime;

    /**
     * 子评论列表（不存储在数据库）
     */
    @TableField(exist = false)
    private List<Forum> childs;
}

