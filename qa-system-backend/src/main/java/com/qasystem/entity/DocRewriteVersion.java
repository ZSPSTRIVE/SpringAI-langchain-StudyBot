package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档降重版本记录表
 */
@Data
@TableName("doc_rewrite_version")
public class DocRewriteVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 版本号（从1开始递增）
     */
    private Integer versionNo;

    /**
     * 降重风格：ACADEMIC/FLUENCY/EXPAND/LOGIC_ENHANCE 等
     */
    private String style;

    /**
     * 当前版本内容（可以是整篇文本或JSON结构）
     */
    private String content;

    /**
     * 版本备注（例如：自动保存、手动保存等）
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Long createdBy;

    private LocalDateTime createdAt;
}
