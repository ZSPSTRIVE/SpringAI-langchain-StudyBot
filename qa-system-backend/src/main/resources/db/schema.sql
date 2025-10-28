-- 师生答疑系统 v2.0 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS qa_system_v2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE qa_system_v2;

-- ==================== 用户体系 ====================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：STUDENT-学生, TEACHER-教师, ADMIN-管理员',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `gender` CHAR(1) DEFAULT 'U' COMMENT '性别：M-男, F-女, U-未知',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE-正常, DISABLED-禁用, LOCKED-锁定',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 学生扩展信息表
CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `student_no` VARCHAR(50) NOT NULL COMMENT '学号',
    `major` VARCHAR(100) COMMENT '专业',
    `class_name` VARCHAR(50) COMMENT '班级',
    `grade` INT COMMENT '年级',
    `college` VARCHAR(100) COMMENT '学院',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_college` (`college`),
    KEY `idx_grade` (`grade`),
    CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表';

-- 教师扩展信息表
CREATE TABLE IF NOT EXISTS `teacher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `teacher_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `title` VARCHAR(50) COMMENT '职称',
    `college` VARCHAR(100) COMMENT '学院',
    `research` VARCHAR(500) COMMENT '研究方向',
    `office` VARCHAR(100) COMMENT '办公室',
    `bio` TEXT COMMENT '个人简介',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_teacher_no` (`teacher_no`),
    KEY `idx_college` (`college`),
    CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师信息表';

-- ==================== 问答体系 ====================

-- 科目分类表
CREATE TABLE IF NOT EXISTS `subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '科目名称',
    `code` VARCHAR(50) COMMENT '科目代码',
    `description` TEXT COMMENT '科目描述',
    `icon` VARCHAR(500) COMMENT '图标URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科目分类表';

-- 问题表
CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `subject_id` BIGINT NOT NULL COMMENT '科目ID',
    `student_id` BIGINT NOT NULL COMMENT '提问学生ID',
    `title` VARCHAR(255) NOT NULL COMMENT '问题标题',
    `content` TEXT NOT NULL COMMENT '问题内容（富文本）',
    `images` JSON COMMENT '图片URL列表',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待回答, ANSWERED-已回答, CLOSED-已关闭',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `is_top` TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    `is_featured` TINYINT(1) DEFAULT 0 COMMENT '是否精选',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_subject` (`subject_id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_view_count` (`view_count`),
    CONSTRAINT `fk_question_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
    CONSTRAINT `fk_question_student` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- 回答表
CREATE TABLE IF NOT EXISTS `answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `teacher_id` BIGINT NOT NULL COMMENT '回答教师ID',
    `content` TEXT NOT NULL COMMENT '回答内容（富文本）',
    `images` JSON COMMENT '图片URL列表',
    `is_accepted` TINYINT(1) DEFAULT 0 COMMENT '是否被采纳',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_question` (`question_id`),
    KEY `idx_teacher` (`teacher_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
    CONSTRAINT `fk_answer_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答表';

-- ==================== 社交互动 ====================

-- 关注表
CREATE TABLE IF NOT EXISTS `follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID（学生）',
    `followee_id` BIGINT NOT NULL COMMENT '被关注者ID（教师）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `followee_id`),
    KEY `idx_follower` (`follower_id`),
    KEY `idx_followee` (`followee_id`),
    CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `collection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '收藏对象类型：QUESTION-问题, ANSWER-回答, POST-帖子',
    `target_id` BIGINT NOT NULL COMMENT '收藏对象ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_collection` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    CONSTRAINT `fk_collection_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

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

-- 交流区帖子表
CREATE TABLE IF NOT EXISTS `forum_post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '发帖用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '帖子标题',
    `content` TEXT NOT NULL COMMENT '帖子内容（富文本）',
    `images` JSON COMMENT '图片URL列表',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `is_top` TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    `status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态：NORMAL-正常, HIDDEN-隐藏',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_view_count` (`view_count`),
    CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交流区帖子表';

-- 帖子评论表
CREATE TABLE IF NOT EXISTS `forum_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `parent_id` BIGINT COMMENT '父评论ID（用于回复）',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_post` (`post_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_parent` (`parent_id`),
    CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `forum_post` (`id`),
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子评论表';

-- ==================== 初始数据 ====================

-- 插入管理员账号
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `email`, `gender`, `status`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN', 'admin@qa.com', 'U', 'ACTIVE');

-- 插入示例科目
INSERT INTO `subject` (`name`, `code`, `description`, `sort_order`, `status`) VALUES
('数据结构', 'DS101', '学习数据结构的基本概念和算法', 1, 'ACTIVE'),
('操作系统', 'OS102', '深入理解操作系统原理', 2, 'ACTIVE'),
('数据库原理', 'DB103', '关系数据库理论与实践', 3, 'ACTIVE'),
('计算机网络', 'NET104', '网络协议与应用', 4, 'ACTIVE'),
('软件工程', 'SE105', '软件开发方法与实践', 5, 'ACTIVE');

