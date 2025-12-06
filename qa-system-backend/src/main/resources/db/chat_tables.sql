-- ============================================
-- 社交聊天模块数据库表结构
-- 功能：好友关系、私聊、群聊、消息转发
-- ============================================

-- 1. 好友关系表
CREATE TABLE IF NOT EXISTS `friend` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待确认, ACCEPTED-已接受, REJECTED-已拒绝, BLOCKED-已屏蔽',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注名',
    `group_name` VARCHAR(50) DEFAULT '我的好友' COMMENT '好友分组',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_friend_id` (`friend_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 2. 好友申请表
CREATE TABLE IF NOT EXISTS `friend_request` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `from_user_id` BIGINT NOT NULL COMMENT '发送方用户ID',
    `to_user_id` BIGINT NOT NULL COMMENT '接收方用户ID',
    `message` VARCHAR(200) DEFAULT NULL COMMENT '验证消息',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理, ACCEPTED-已同意, REJECTED-已拒绝',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_from_user` (`from_user_id`),
    INDEX `idx_to_user` (`to_user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

-- 3. 私聊会话表
CREATE TABLE IF NOT EXISTS `chat_conversation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_id` BIGINT NOT NULL COMMENT '对方ID',
    `type` VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '会话类型：PRIVATE-私聊, GROUP-群聊',
    `last_message` TEXT DEFAULT NULL COMMENT '最后一条消息内容',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
    `is_muted` TINYINT DEFAULT 0 COMMENT '是否免打扰',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `type`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_last_time` (`last_message_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 4. 私聊消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id` BIGINT DEFAULT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT COMMENT '消息内容',
    `type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本, IMAGE-图片, VIDEO-视频, FILE-文件, EMOJI-表情, VOICE-语音, LOCATION-位置, CARD-名片, FORWARD-转发',
    `media_url` VARCHAR(500) DEFAULT NULL COMMENT '媒体文件URL',
    `media_thumbnail` VARCHAR(500) DEFAULT NULL COMMENT '媒体缩略图URL',
    `media_duration` INT DEFAULT NULL COMMENT '媒体时长(秒)',
    `media_size` BIGINT DEFAULT NULL COMMENT '媒体文件大小(字节)',
    `forward_from_id` BIGINT DEFAULT NULL COMMENT '转发来源消息ID',
    `forward_from_user` VARCHAR(50) DEFAULT NULL COMMENT '转发来源用户名',
    `reply_to_id` BIGINT DEFAULT NULL COMMENT '回复的消息ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `is_recalled` TINYINT DEFAULT 0 COMMENT '是否已撤回',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX `idx_conversation` (`conversation_id`),
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_receiver` (`receiver_id`),
    INDEX `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊消息表';

-- 5. 群聊表
CREATE TABLE IF NOT EXISTS `chat_group` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '群名称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '群头像URL',
    `owner_id` BIGINT NOT NULL COMMENT '群主ID',
    `announcement` TEXT DEFAULT NULL COMMENT '群公告',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '群描述',
    `max_members` INT DEFAULT 500 COMMENT '最大成员数',
    `member_count` INT DEFAULT 1 COMMENT '当前成员数',
    `type` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '群类型：NORMAL-普通群, CLASS-班级群, COURSE-课程群',
    `join_type` VARCHAR(20) DEFAULT 'APPROVAL' COMMENT '加入方式：FREE-自由加入, APPROVAL-需审批, INVITE-仅邀请',
    `is_muted` TINYINT DEFAULT 0 COMMENT '是否全员禁言',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX `idx_owner` (`owner_id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊表';

-- 6. 群成员表
CREATE TABLE IF NOT EXISTS `chat_group_member` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT NOT NULL COMMENT '群ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '群昵称',
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色：OWNER-群主, ADMIN-管理员, MEMBER-普通成员',
    `is_muted` TINYINT DEFAULT 0 COMMENT '是否被禁言',
    `mute_end_time` DATETIME DEFAULT NULL COMMENT '禁言结束时间',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶该群',
    `is_disturb` TINYINT DEFAULT 0 COMMENT '是否免打扰',
    `last_read_time` DATETIME DEFAULT NULL COMMENT '最后阅读时间',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `invite_user_id` BIGINT DEFAULT NULL COMMENT '邀请人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员表';

-- 7. 群消息表
CREATE TABLE IF NOT EXISTS `chat_group_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT NOT NULL COMMENT '群ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `content` TEXT COMMENT '消息内容',
    `type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本, IMAGE-图片, VIDEO-视频, FILE-文件, EMOJI-表情, VOICE-语音, SYSTEM-系统消息, FORWARD-转发',
    `media_url` VARCHAR(500) DEFAULT NULL COMMENT '媒体文件URL',
    `media_thumbnail` VARCHAR(500) DEFAULT NULL COMMENT '媒体缩略图URL',
    `media_duration` INT DEFAULT NULL COMMENT '媒体时长(秒)',
    `media_size` BIGINT DEFAULT NULL COMMENT '媒体文件大小(字节)',
    `forward_from_id` BIGINT DEFAULT NULL COMMENT '转发来源消息ID',
    `forward_from_user` VARCHAR(50) DEFAULT NULL COMMENT '转发来源用户名',
    `reply_to_id` BIGINT DEFAULT NULL COMMENT '回复的消息ID',
    `at_user_ids` VARCHAR(500) DEFAULT NULL COMMENT '@的用户ID列表，逗号分隔',
    `at_all` TINYINT DEFAULT 0 COMMENT '是否@全体',
    `is_recalled` TINYINT DEFAULT 0 COMMENT '是否已撤回',
    `read_count` INT DEFAULT 0 COMMENT '已读人数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群消息表';

-- 8. 表情包表
CREATE TABLE IF NOT EXISTS `emoji_pack` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '表情包名称',
    `cover_url` VARCHAR(500) NOT NULL COMMENT '封面图URL',
    `type` VARCHAR(20) DEFAULT 'STATIC' COMMENT '类型：STATIC-静态, DYNAMIC-动态',
    `source` VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '来源：SYSTEM-系统, USER-用户上传, DOUYIN-抖音风格',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_type` (`type`),
    INDEX `idx_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表情包表';

-- 9. 表情表
CREATE TABLE IF NOT EXISTS `emoji` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `pack_id` BIGINT DEFAULT NULL COMMENT '表情包ID，NULL表示系统表情',
    `name` VARCHAR(50) DEFAULT NULL COMMENT '表情名称/描述',
    `url` VARCHAR(500) NOT NULL COMMENT '表情图片URL',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `type` VARCHAR(20) DEFAULT 'STATIC' COMMENT '类型：STATIC-静态, GIF-动图, LOTTIE-Lottie动画',
    `code` VARCHAR(50) DEFAULT NULL COMMENT '表情代码，如 [微笑]',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_pack_id` (`pack_id`),
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表情表';

-- 10. 用户收藏表情表
CREATE TABLE IF NOT EXISTS `user_emoji` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `emoji_id` BIGINT DEFAULT NULL COMMENT '表情ID',
    `custom_url` VARCHAR(500) DEFAULT NULL COMMENT '自定义表情URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表情表';

-- 插入系统默认表情包（抖音风格）
INSERT INTO `emoji_pack` (`name`, `cover_url`, `type`, `source`, `sort_order`) VALUES
('抖音热门', '/emojis/douyin/cover.png', 'DYNAMIC', 'DOUYIN', 1),
('经典表情', '/emojis/classic/cover.png', 'STATIC', 'SYSTEM', 2),
('可爱萌宠', '/emojis/cute/cover.png', 'DYNAMIC', 'SYSTEM', 3);

-- 插入系统默认表情
INSERT INTO `emoji` (`pack_id`, `name`, `url`, `type`, `code`, `sort_order`) VALUES
(1, '无语', '/emojis/douyin/wuyu.gif', 'GIF', '[无语]', 1),
(1, '裂开', '/emojis/douyin/liekai.gif', 'GIF', '[裂开]', 2),
(1, '社死', '/emojis/douyin/shesi.gif', 'GIF', '[社死]', 3),
(1, '绝绝子', '/emojis/douyin/juejuezi.gif', 'GIF', '[绝绝子]', 4),
(1, 'YYDS', '/emojis/douyin/yyds.gif', 'GIF', '[YYDS]', 5),
(1, '芭比Q了', '/emojis/douyin/babiq.gif', 'GIF', '[芭比Q]', 6),
(1, '破防了', '/emojis/douyin/pofang.gif', 'GIF', '[破防]', 7),
(1, '我不理解', '/emojis/douyin/bulijie.gif', 'GIF', '[不理解]', 8),
(2, '微笑', '/emojis/classic/smile.png', 'STATIC', '[微笑]', 1),
(2, '大笑', '/emojis/classic/laugh.png', 'STATIC', '[大笑]', 2),
(2, '哭泣', '/emojis/classic/cry.png', 'STATIC', '[哭泣]', 3),
(2, '愤怒', '/emojis/classic/angry.png', 'STATIC', '[愤怒]', 4),
(2, '思考', '/emojis/classic/think.png', 'STATIC', '[思考]', 7),
(2, '点赞', '/emojis/classic/like.png', 'STATIC', '[点赞]', 8),
(2, '比心', '/emojis/classic/heart.png', 'STATIC', '[比心]', 9),
(2, 'OK', '/emojis/classic/ok.png', 'STATIC', '[OK]', 10);