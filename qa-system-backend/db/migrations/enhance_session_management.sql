-- 为会话管理增加增强功能字段

-- 添加置顶标记
ALTER TABLE ai_conversation 
ADD COLUMN is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶' AFTER session_title;

-- 添加归档标记
ALTER TABLE ai_conversation 
ADD COLUMN is_archived TINYINT(1) DEFAULT 0 COMMENT '是否归档' AFTER is_pinned;

-- 添加标签颜色（可选，用于区分不同类型的会话）
ALTER TABLE ai_conversation 
ADD COLUMN session_color VARCHAR(20) DEFAULT NULL COMMENT '会话标记颜色' AFTER is_archived;

-- 为常用查询添加复合索引
CREATE INDEX idx_user_pinned ON ai_conversation(user_id, is_pinned, created_at DESC);
CREATE INDEX idx_user_archived ON ai_conversation(user_id, is_archived);

-- 更新说明：
-- 1. is_pinned: 0=未置顶, 1=已置顶。置顶的会话将在列表中优先显示
-- 2. is_archived: 0=活跃, 1=已归档。归档的会话可以隐藏，需要时才显示
-- 3. session_color: 可选颜色标记，如 'blue', 'green', 'red' 等，用于视觉分类
