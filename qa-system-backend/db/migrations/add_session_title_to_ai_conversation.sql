-- 为 ai_conversation 表添加 session_title 字段
ALTER TABLE ai_conversation ADD COLUMN session_title VARCHAR(255) DEFAULT NULL COMMENT '会话标题（用户自定义）' AFTER session_id;

-- 为现有会话添加索引以提高查询性能
CREATE INDEX idx_session_id ON ai_conversation(session_id);
CREATE INDEX idx_user_session ON ai_conversation(user_id, session_id);
