package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档敏感词表
 */
@Data
@TableName("doc_sensitive_word")
public class DocSensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 分类，例如：POLITICS/VIOLENCE/DEFAULT 等
     */
    private String category;

    /**
     * 风险等级：HIGH/MEDIUM/LOW
     */
    private String level;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 说明
     */
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
