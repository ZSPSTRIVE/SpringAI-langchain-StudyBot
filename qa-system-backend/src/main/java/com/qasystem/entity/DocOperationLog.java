package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档操作日志表
 */
@Data
@TableName("doc_operation_log")
public class DocOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户角色：STUDENT/TEACHER/ADMIN 等
     */
    private String userRole;

    /**
     * 操作类型：UPLOAD_AND_CHECK/REWRITE/SAVE_VERSION/DOWNLOAD 等
     */
    private String operationType;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 段落ID（可选）
     */
    private Long paragraphId;

    /**
     * 操作详情（简要描述）
     */
    private String detail;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    private LocalDateTime createdAt;
}
