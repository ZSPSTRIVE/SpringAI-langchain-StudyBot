package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档查重-段落表
 */
@Data
@TableName("doc_paragraph")
public class DocParagraph {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属文档ID
     */
    private Long documentId;

    /**
     * 段落序号（从0开始）
     */
    private Integer paragraphIndex;

    /**
     * 原始段落文本
     */
    private String originalText;

    /**
     * 当前段落查重率（0-100）
     */
    private Double similarity;

    /**
     * 相似来源描述（可存来源文档/链接等信息）
     */
    private String similarSource;

    /**
     * 相似片段位置，JSON格式，例如：[{"start":10,"end":30}]
     */
    private String similarSpans;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
