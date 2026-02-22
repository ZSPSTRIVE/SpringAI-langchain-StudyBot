-- Create or align ai_model_config schema.
-- Compatibility: avoid ADD COLUMN IF NOT EXISTS / CREATE INDEX IF NOT EXISTS.
CREATE TABLE IF NOT EXISTS `ai_model_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `provider` VARCHAR(64) NOT NULL COMMENT '供应商编码',
    `provider_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `model_name` VARCHAR(120) NOT NULL COMMENT '模型标识',
    `model_display_name` VARCHAR(120) NULL COMMENT '模型显示名称',
    `api_key` VARCHAR(512) NOT NULL COMMENT 'API Key',
    `base_url` VARCHAR(512) NOT NULL COMMENT '接口地址',
    `temperature` DECIMAL(4,2) DEFAULT 0.70 COMMENT '温度参数',
    `max_tokens` INT DEFAULT 2000 COMMENT '最大输出Token',
    `default_rewrite_style` VARCHAR(50) NULL COMMENT '默认改写风格',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_active` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否激活',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_ai_model_enabled_active` (`enabled`, `is_active`),
    KEY `idx_ai_model_provider` (`provider`),
    KEY `idx_ai_model_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置';

SET @db = DATABASE();

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'provider'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `provider` VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''供应商编码''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'provider_name'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `provider_name` VARCHAR(100) NOT NULL DEFAULT '''' COMMENT ''供应商名称''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'model_name'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `model_name` VARCHAR(120) NOT NULL DEFAULT '''' COMMENT ''模型标识''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'model_display_name'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `model_display_name` VARCHAR(120) NULL COMMENT ''模型显示名称''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'api_key'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `api_key` VARCHAR(512) NOT NULL DEFAULT '''' COMMENT ''API Key''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'base_url'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `base_url` VARCHAR(512) NOT NULL DEFAULT '''' COMMENT ''接口地址''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'temperature'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `temperature` DECIMAL(4,2) DEFAULT 0.70 COMMENT ''温度参数''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'max_tokens'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `max_tokens` INT DEFAULT 2000 COMMENT ''最大输出Token''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'default_rewrite_style'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `default_rewrite_style` VARCHAR(50) NULL COMMENT ''默认改写风格''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'enabled'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否启用''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'is_active'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否激活''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'remark'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `remark` VARCHAR(500) NULL COMMENT ''备注''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'created_at'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND column_name = 'updated_at'
);
SET @ddl = IF(@exists = 0,
    'ALTER TABLE `ai_model_config` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND index_name = 'idx_ai_model_enabled_active'
);
SET @ddl = IF(@idx_exists = 0,
    'CREATE INDEX `idx_ai_model_enabled_active` ON `ai_model_config` (`enabled`, `is_active`)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND index_name = 'idx_ai_model_provider'
);
SET @ddl = IF(@idx_exists = 0,
    'CREATE INDEX `idx_ai_model_provider` ON `ai_model_config` (`provider`)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = @db AND table_name = 'ai_model_config' AND index_name = 'idx_ai_model_updated_at'
);
SET @ddl = IF(@idx_exists = 0,
    'CREATE INDEX `idx_ai_model_updated_at` ON `ai_model_config` (`updated_at`)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
