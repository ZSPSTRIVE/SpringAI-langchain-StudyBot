-- 为 RAG 检索增加 FULLTEXT 索引，提升关键词召回性能
-- 兼容重复执行：通过 information_schema 判断后再建索引
SET @db = DATABASE();

SET @question_table_exists = (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = @db
      AND table_name = 'question'
);

SET @question_idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @db
      AND table_name = 'question'
      AND index_name = 'idx_question_title_content_ft'
);

SET @question_idx_ddl = IF(
    @question_table_exists = 1 AND @question_idx_exists = 0,
    'ALTER TABLE `question` ADD FULLTEXT INDEX `idx_question_title_content_ft` (`title`, `content`)',
    'SELECT 1'
);

PREPARE stmt_question_ft FROM @question_idx_ddl;
EXECUTE stmt_question_ft;
DEALLOCATE PREPARE stmt_question_ft;

SET @answer_table_exists = (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = @db
      AND table_name = 'answer'
);

SET @answer_idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @db
      AND table_name = 'answer'
      AND index_name = 'idx_answer_content_ft'
);

SET @answer_idx_ddl = IF(
    @answer_table_exists = 1 AND @answer_idx_exists = 0,
    'ALTER TABLE `answer` ADD FULLTEXT INDEX `idx_answer_content_ft` (`content`)',
    'SELECT 1'
);

PREPARE stmt_answer_ft FROM @answer_idx_ddl;
EXECUTE stmt_answer_ft;
DEALLOCATE PREPARE stmt_answer_ft;
