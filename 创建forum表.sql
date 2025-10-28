-- ========================================
-- 创建forum表以支持论坛功能
-- 执行说明：
-- 1. 使用数据库客户端（Navicat/MySQL Workbench等）连接到MySQL
-- 2. 选择 qa_system_v2 数据库
-- 3. 执行此SQL脚本
-- ========================================

USE qa_system_v2;

-- 创建forum表
CREATE TABLE IF NOT EXISTS `forum` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) COMMENT '帖子标题',
    `content` LONGTEXT NOT NULL COMMENT '帖子内容',
    `parentid` BIGINT DEFAULT 0 COMMENT '父节点ID（0表示顶级帖子，其他表示评论）',
    `userid` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(200) COMMENT '用户名',
    `isdone` VARCHAR(200) COMMENT '状态',
    `addtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_parentid` (`parentid`),
    KEY `idx_userid` (`userid`),
    KEY `idx_addtime` (`addtime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互助区/论坛';

-- 插入初始测试数据
INSERT INTO `forum` (`title`, `content`, `parentid`, `userid`, `username`, `isdone`, `addtime`) VALUES
('欢迎来到师生答疑系统交流区', '<p>这里是师生交流互助的平台，大家可以自由讨论学习问题。</p>', 0, 1, '系统管理员', '开放', NOW()),
('Java学习交流', '<p>欢迎大家交流Java学习心得！</p>', 0, 1, '系统管理员', '开放', NOW()),
('前端技术讨论', '<p>Vue、React等前端技术讨论专区</p>', 0, 1, '系统管理员', '开放', NOW());

-- 验证表是否创建成功
SELECT '✅ forum表创建成功！' AS message;
SELECT COUNT(*) AS record_count FROM forum;
SELECT * FROM forum LIMIT 10;

