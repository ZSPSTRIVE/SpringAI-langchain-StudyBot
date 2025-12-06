-- AI对话历史表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID',
  `user_message` TEXT NOT NULL COMMENT '用户消息',
  `ai_response` TEXT COMMENT 'AI回复',
  `message_type` VARCHAR(50) DEFAULT 'text' COMMENT '消息类型',
  `question_category` VARCHAR(100) COMMENT '问题分类',
  `is_bookmarked` BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
  `feedback` VARCHAR(50) COMMENT '用户反馈',
  `recommended_resources` TEXT COMMENT '推荐资源JSON',
  `tokens_used` INT DEFAULT 0 COMMENT 'Token消耗',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

