-- 文档查重与AI降重相关表结构

-- 文档主表
CREATE TABLE IF NOT EXISTS `doc_document` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      BIGINT      NOT NULL COMMENT '上传用户ID',
    `title`        VARCHAR(255) NOT NULL COMMENT '文档标题',
    `file_url`     VARCHAR(512) NOT NULL COMMENT '原始文件路径',
    `status`       VARCHAR(50)  NOT NULL DEFAULT 'CHECKED' COMMENT '状态：UPLOADED/CHECKING/CHECKED/REWRITING/COMPLETED',
    `overall_similarity` DECIMAL(5,2) DEFAULT 0 COMMENT '整体查重率(0-100)',
    `algorithm_detail`   TEXT        NULL COMMENT '算法详细得分(JSON)',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档查重-文档主表';

-- 段落表
CREATE TABLE IF NOT EXISTS `doc_paragraph` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id`     BIGINT      NOT NULL COMMENT '文档ID',
    `paragraph_index` INT         NOT NULL COMMENT '段落序号',
    `original_text`   LONGTEXT    NOT NULL COMMENT '原始段落文本',
    `similarity`      DECIMAL(5,2) NULL COMMENT '段落查重率(0-100)',
    `similar_source`  TEXT        NULL COMMENT '相似来源描述',
    `similar_spans`   TEXT        NULL COMMENT '相似片段位置(JSON)',
    `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_paragraph_index` (`paragraph_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档查重-段落表';

-- 降重版本表
CREATE TABLE IF NOT EXISTS `doc_rewrite_version` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT      NOT NULL COMMENT '文档ID',
    `version_no`  INT         NOT NULL COMMENT '版本号',
    `style`       VARCHAR(50) NULL COMMENT '降重风格',
    `content`     LONGTEXT    NOT NULL COMMENT '版本内容',
    `remark`      VARCHAR(255) NULL COMMENT '备注',
    `created_by`  BIGINT      NOT NULL COMMENT '创建人ID',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_version_no` (`version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档降重版本表';

-- 配置表
CREATE TABLE IF NOT EXISTS `doc_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key`   VARCHAR(191) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(1024) NULL COMMENT '配置值',
    `description`  VARCHAR(255) NULL COMMENT '说明',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档查重与AI降重配置表';

-- 文档敏感词表
CREATE TABLE IF NOT EXISTS `doc_sensitive_word` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `word`        VARCHAR(255) NOT NULL COMMENT '敏感词内容',
    `category`    VARCHAR(100) NULL COMMENT '分类：POLITICS/VIOLENCE/DEFAULT 等',
    `level`       VARCHAR(50)  NULL COMMENT '风险等级：HIGH/MEDIUM/LOW',
    `enabled`     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
    `description` VARCHAR(255) NULL COMMENT '说明',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档敏感词表';

-- 文档操作日志表
CREATE TABLE IF NOT EXISTS `doc_operation_log` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
    `user_role`      VARCHAR(50)  NULL COMMENT '用户角色：STUDENT/TEACHER/ADMIN 等',
    `operation_type` VARCHAR(100) NOT NULL COMMENT '操作类型：UPLOAD_AND_CHECK/REWRITE/SAVE_VERSION/DOWNLOAD 等',
    `document_id`    BIGINT       NULL COMMENT '文档ID',
    `paragraph_id`   BIGINT       NULL COMMENT '段落ID',
    `detail`         VARCHAR(1024) NULL COMMENT '操作详情',
    `client_ip`      VARCHAR(64)  NULL COMMENT '客户端IP',
    `user_agent`     VARCHAR(255) NULL COMMENT 'User-Agent',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档操作日志表';
