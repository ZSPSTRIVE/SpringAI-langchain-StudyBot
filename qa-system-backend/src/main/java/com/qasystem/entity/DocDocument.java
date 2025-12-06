package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档查重-文档主表
 */
@Data
@TableName("doc_document")
public class DocDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 文档标题（默认使用文件名，可自定义）
     */
    private String title;

    /**
     * 原始文件在存储中的路径（本地或COS）
     */
    private String fileUrl;

    /**
     * 文档当前状态：UPLOADED/CHECKING/CHECKED/REWRITING/COMPLETED
     */
    private String status;

    /**
     * 文档整体查重率（0-100）
     */
    private Double overallSimilarity;

    /**
     * 各算法得分JSON（例如存储MinHash/LCS/语义相似度详细信息）
     */
    private String algorithmDetail;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
