-- Align ai_conversation schema with current entity and mapper usage.
-- Compatibility: avoid ADD COLUMN IF NOT EXISTS / CREATE INDEX IF NOT EXISTS.
SET @db = DATABASE();

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'session_title'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `session_title` VARCHAR(200) NULL COMMENT ''会话标题'' AFTER `session_id`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'message_type'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `message_type` VARCHAR(50) DEFAULT ''text'' COMMENT ''消息类型'' AFTER `ai_response`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'question_category'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `question_category` VARCHAR(100) NULL COMMENT ''问题分类'' AFTER `message_type`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'is_bookmarked'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `is_bookmarked` TINYINT(1) DEFAULT 0 COMMENT ''是否收藏'' AFTER `question_category`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'feedback'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `feedback` VARCHAR(50) NULL COMMENT ''用户反馈'' AFTER `is_bookmarked`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'recommended_resources'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `recommended_resources` TEXT NULL COMMENT ''推荐资源'' AFTER `feedback`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'tokens_used'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `tokens_used` INT DEFAULT 0 COMMENT ''Token 消耗'' AFTER `recommended_resources`',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'created_at'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND column_name = 'updated_at'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_conversation` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE `ai_conversation`
    MODIFY COLUMN `session_id` VARCHAR(100) NOT NULL,
    MODIFY COLUMN `user_message` TEXT NOT NULL,
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = @db AND table_name = 'ai_conversation' AND index_name = 'idx_user_session_created'
);
SET @ddl = IF(@idx_exists = 0,
    'CREATE INDEX `idx_user_session_created` ON `ai_conversation` (`user_id`, `session_id`, `created_at`)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
