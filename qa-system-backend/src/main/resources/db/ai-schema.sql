-- AI对话历史表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `role` VARCHAR(20) NOT NULL COMMENT '用户角色',
    `question` TEXT NOT NULL COMMENT '用户提问',
    `answer` TEXT NOT NULL COMMENT 'AI回答',
    `category` VARCHAR(50) COMMENT '问题类别',
    `recommended_resources` JSON COMMENT '推荐资源（JSON）',
    `response_time` BIGINT COMMENT '响应时间（毫秒）',
    `helpful` TINYINT(1) DEFAULT NULL COMMENT '是否有帮助',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话历史表';

