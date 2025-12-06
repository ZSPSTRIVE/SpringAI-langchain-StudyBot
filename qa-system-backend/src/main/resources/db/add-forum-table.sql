-- 添加forum表以兼容旧系统API
-- 执行时间: 2025-10-27

USE qa_system_v2;

-- 互助区/论坛表（兼容旧系统）
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

-- 插入一些测试数据（可选）
INSERT INTO `forum` (`id`, `title`, `content`, `parentid`, `userid`, `username`, `isdone`, `addtime`) VALUES
(1, '欢迎来到师生答疑系统交流区', '<p>这里是师生交流互助的平台，大家可以自由讨论学习问题。</p>', 0, 1, 'admin', '开放', NOW()),
(2, 'Java学习交流', '<p>欢迎大家交流Java学习心得！</p>', 0, 1, 'admin', '开放', NOW()),
(3, '前端技术讨论', '<p>Vue、React等前端技术讨论专区</p>', 0, 1, 'admin', '开放', NOW());

