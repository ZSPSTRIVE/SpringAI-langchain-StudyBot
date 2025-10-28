/*
 Navicat Premium Data Transfer

 Source Server         : springboot7vkr1
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : 182.92.211.184:3306
 Source Schema         : springboot7vkr2

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 27/10/2025 19:13:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置参数名称',
  `value` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置参数值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置文件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config
-- ----------------------------
INSERT INTO `config` VALUES (1, 'picture1', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740701951295.jpg');
INSERT INTO `config` VALUES (2, 'picture2', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740701962938.jpg');
INSERT INTO `config` VALUES (3, 'picture3', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740701979880.jpg');
INSERT INTO `config` VALUES (6, 'picture4', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740701998857.jpg');

-- ----------------------------
-- Table structure for discusslaoshihuida
-- ----------------------------
DROP TABLE IF EXISTS `discusslaoshihuida`;
CREATE TABLE `discusslaoshihuida`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `refid` bigint(20) NOT NULL COMMENT '关联表id',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `nickname` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '评论内容',
  `reply` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '回复内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1736347780678 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '老师回答评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of discusslaoshihuida
-- ----------------------------
INSERT INTO `discusslaoshihuida` VALUES (111, '2021-04-15 14:47:38', 1, 1, '用户名1', '评论内容1', '回复内容1');
INSERT INTO `discusslaoshihuida` VALUES (112, '2021-04-15 14:47:38', 2, 2, '用户名2', '评论内容2', '回复内容2');
INSERT INTO `discusslaoshihuida` VALUES (113, '2021-04-15 14:47:38', 3, 3, '用户名3', '评论内容3', '回复内容3');
INSERT INTO `discusslaoshihuida` VALUES (114, '2021-04-15 14:47:38', 4, 4, '用户名4', '评论内容4', '回复内容4');
INSERT INTO `discusslaoshihuida` VALUES (115, '2021-04-15 14:47:38', 5, 5, '用户名5', '评论内容5', '回复内容5');
INSERT INTO `discusslaoshihuida` VALUES (116, '2021-04-15 14:47:38', 6, 6, '用户名6', '评论内容6', '回复内容6');
INSERT INTO `discusslaoshihuida` VALUES (1618469753454, '2021-04-15 14:55:52', 1618469632218, 1618469461656, '12', '谢谢老师', NULL);
INSERT INTO `discusslaoshihuida` VALUES (1736347780677, '2025-01-08 22:49:40', 52, 1735632090571, '大鸟yyds', 'AAC', NULL);

-- ----------------------------
-- Table structure for discusslaoshixinxi
-- ----------------------------
DROP TABLE IF EXISTS `discusslaoshixinxi`;
CREATE TABLE `discusslaoshixinxi`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `refid` bigint(20) NOT NULL COMMENT '关联表id',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `nickname` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '评论内容',
  `reply` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '回复内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 127 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '老师信息评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of discusslaoshixinxi
-- ----------------------------
INSERT INTO `discusslaoshixinxi` VALUES (121, '2021-04-15 14:47:38', 1, 1, '用户名1', '评论内容1', '回复内容1');
INSERT INTO `discusslaoshixinxi` VALUES (122, '2021-04-15 14:47:38', 2, 2, '用户名2', '评论内容2', '回复内容2');
INSERT INTO `discusslaoshixinxi` VALUES (123, '2021-04-15 14:47:38', 3, 3, '用户名3', '评论内容3', '回复内容3');
INSERT INTO `discusslaoshixinxi` VALUES (124, '2021-04-15 14:47:38', 4, 4, '用户名4', '评论内容4', '回复内容4');
INSERT INTO `discusslaoshixinxi` VALUES (125, '2021-04-15 14:47:38', 5, 5, '用户名5', '评论内容5', '回复内容5');
INSERT INTO `discusslaoshixinxi` VALUES (126, '2021-04-15 14:47:38', 6, 6, '用户名6', '评论内容6', '回复内容6');

-- ----------------------------
-- Table structure for discussxueshengwenti
-- ----------------------------
DROP TABLE IF EXISTS `discussxueshengwenti`;
CREATE TABLE `discussxueshengwenti`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `refid` bigint(20) NOT NULL COMMENT '关联表id',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `nickname` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '评论内容',
  `reply` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '回复内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1618469734308 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '学生问题评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of discussxueshengwenti
-- ----------------------------
INSERT INTO `discussxueshengwenti` VALUES (101, '2021-04-15 14:47:38', 1, 1, '用户名1', '评论内容1', '回复内容1');
INSERT INTO `discussxueshengwenti` VALUES (102, '2021-04-15 14:47:38', 2, 2, '用户名2', '评论内容2', '回复内容2');
INSERT INTO `discussxueshengwenti` VALUES (103, '2021-04-15 14:47:38', 3, 3, '用户名3', '评论内容3', '回复内容3');
INSERT INTO `discussxueshengwenti` VALUES (104, '2021-04-15 14:47:38', 4, 4, '用户名4', '评论内容4', '回复内容4');
INSERT INTO `discussxueshengwenti` VALUES (105, '2021-04-15 14:47:38', 5, 5, '用户名5', '评论内容5', '回复内容5');
INSERT INTO `discussxueshengwenti` VALUES (106, '2021-04-15 14:47:38', 6, 6, '用户名6', '评论内容6', '回复内容6');
INSERT INTO `discussxueshengwenti` VALUES (1618469603296, '2021-04-15 14:53:22', 1618469502652, 1618469527598, '122', '12', NULL);
INSERT INTO `discussxueshengwenti` VALUES (1618469734307, '2021-04-15 14:55:33', 1618469502652, 1618469461656, '12', '12', NULL);

-- ----------------------------
-- Table structure for forum
-- ----------------------------
DROP TABLE IF EXISTS `forum`;
CREATE TABLE `forum`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `title` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '帖子标题',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '帖子内容',
  `parentid` bigint(20) NULL DEFAULT NULL COMMENT '父节点id',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `isdone` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1747901712105 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '交流区' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum
-- ----------------------------
INSERT INTO `forum` VALUES (81, '2024-12-15 14:47:38', '链表与数组的区别？', '链表和数组都是常用的线性数据结构，但它们有很大的区别。数组是顺序存储，数据元素连续存储，而链表是链式存储，数据元素通过指针连接。数组访问元素的时间复杂度是 O(1)，而链表是 O(n)。链表的插入和删除操作比数组更高效，尤其在大数据量时。', 1, 101, '李晓明', '开放');
INSERT INTO `forum` VALUES (82, '2024-12-15 14:47:38', '什么是TCP协议中的三次握手？', 'TCP协议中的三次握手（Three-way Handshake）是为了建立可靠的连接，确保双方能够正常通信。三次握手过程如下：第一步，客户端发送SYN请求报文段给服务器；第二步，服务器回应SYN-ACK确认报文段；第三步，客户端再次发送ACK确认报文段，连接建立成功。', 2, 102, '王大海', '开放');
INSERT INTO `forum` VALUES (1736580347266, '2025-01-11 15:25:47', '这是一个交流区 测试', '<p><img src=\"../../../upload/1736580344031.jpg\" /></p>', 0, 1735879676275, '1', '开放');
INSERT INTO `forum` VALUES (1736586042128, '2025-01-11 17:00:41', '1', '<p><img src=\"../../../upload/1736586034570.jpg\" /></p>', 0, 1735879676275, '1', '开放');
INSERT INTO `forum` VALUES (1740743895138, '2025-02-28 19:58:14', 'test1', '<p>test</p>', 0, 1735879676275, '1', '开放');
INSERT INTO `forum` VALUES (1740743925530, '2025-02-28 19:58:44', NULL, '<p><img src=\"../../..//upload/1740743922668.jpg\" /></p>', 1740743895138, 1735879676275, NULL, NULL);
INSERT INTO `forum` VALUES (1747714440259, '2025-05-20 12:13:59', '1', '<p>1</p>', 0, 1747714138430, '2', '开放');
INSERT INTO `forum` VALUES (1747787004518, '2025-05-21 08:23:24', '你好', '<p><img src=\"http://182.92.211.184:8080/springboot7vkr1/upload/springboot7vkr1//upload/1747786995189.jpg\" alt=\"\" width=\"500\" height=\"500\" /></p>', 0, 1735879676275, '1', '开放');
INSERT INTO `forum` VALUES (1747787047080, '2025-05-21 08:24:06', NULL, '<p><img src=\"../../..//upload/1747787042394.jpg\" /></p>', 1747787004518, 1735879676275, NULL, NULL);
INSERT INTO `forum` VALUES (1747901712104, '2025-05-22 16:15:11', NULL, '<p><img src=\"../../..//upload/1747901709525.jpg\" /></p>', 1736580347266, 1747714312569, NULL, NULL);

-- ----------------------------
-- Table structure for guanzhuliebiao
-- ----------------------------
DROP TABLE IF EXISTS `guanzhuliebiao`;
CREATE TABLE `guanzhuliebiao`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `laoshizhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '老师账号',
  `laoshixingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '老师姓名',
  `xingbie` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '性别',
  `shoujihaoma` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `touxiang` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `xueshengzhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生账号',
  `xueshengxingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生姓名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1739780163584 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '关注列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guanzhuliebiao
-- ----------------------------
INSERT INTO `guanzhuliebiao` VALUES (1739780163583, '2025-02-17 16:16:03', 'teacher_001', '李华', '男', '13812345678', 'http://localhost:8082/springboot7vkr1/upload/1736580077347.jpg', '1', '1');

-- ----------------------------
-- Table structure for kemuleixing
-- ----------------------------
DROP TABLE IF EXISTS `kemuleixing`;
CREATE TABLE `kemuleixing`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `kemu` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '科目',
  `jianyao` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '简要',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1747897932172 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '科目类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of kemuleixing
-- ----------------------------
INSERT INTO `kemuleixing` VALUES (31, '2021-04-15 14:47:38', '计算机网络', '简要1');
INSERT INTO `kemuleixing` VALUES (32, '2021-04-15 14:47:38', '数据结构', '简要2');
INSERT INTO `kemuleixing` VALUES (34, '2021-04-15 14:47:38', '无线传感网', '简要4');
INSERT INTO `kemuleixing` VALUES (35, '2021-04-15 14:47:38', '单片机原理', '简要5');
INSERT INTO `kemuleixing` VALUES (36, '2021-04-15 14:47:38', 'javaweb', '简要6');
INSERT INTO `kemuleixing` VALUES (1747786593359, '2025-05-21 08:16:32', '硬件描述语言（HDL）', '1');
INSERT INTO `kemuleixing` VALUES (1747897932171, '2025-05-22 15:12:11', '物联网控制', '1');

-- ----------------------------
-- Table structure for laoshi
-- ----------------------------
DROP TABLE IF EXISTS `laoshi`;
CREATE TABLE `laoshi`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `laoshizhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '老师账号',
  `mima` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `laoshixingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '老师姓名',
  `xingbie` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '性别',
  `shoujihaoma` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `touxiang` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `laoshizhanghao`(`laoshizhanghao`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1747714312570 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '老师' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of laoshi
-- ----------------------------
INSERT INTO `laoshi` VALUES (21, '2021-04-15 14:47:38', 'teacher_001', '123456', '李华', '男', '13812345678', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740741940340.jpg');
INSERT INTO `laoshi` VALUES (22, '2021-04-15 14:47:38', 'teacher_002', '123456', '王丽', '女', '13998765432', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740662889054.jpg');
INSERT INTO `laoshi` VALUES (23, '2021-04-15 14:47:38', 'teacher_003', '123456', '张杰', '男', '13711223344', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740662902797.jpg');
INSERT INTO `laoshi` VALUES (24, '2021-04-15 14:47:38', 'teacher_004', '123456', '刘思敏', '女', '13655667788', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740662914856.jpg');
INSERT INTO `laoshi` VALUES (25, '2021-04-15 14:47:38', 'teacher_005', '123456', '陈东', '男', '13599887766', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740662928361.jpg');
INSERT INTO `laoshi` VALUES (26, '2021-04-15 14:47:38', 'teacher_006', '123456', '赵雅丽', '女', '13933221100', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740662938985.jpg');
INSERT INTO `laoshi` VALUES (1618469527598, '2021-04-15 14:52:07', '122', '122', '122', '男', '12346579809', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740662951676.jpg');
INSERT INTO `laoshi` VALUES (1736587395347, '2025-01-11 17:23:15', '12', '12', '12', NULL, '11111111111', '/springboot7vkr1/upload/1738993332708.png');
INSERT INTO `laoshi` VALUES (1736587467385, '2025-01-11 17:24:27', '1221', '1221', '1221', NULL, '12121212121', NULL);
INSERT INTO `laoshi` VALUES (1739529866622, '2025-02-14 18:44:26', '111', '111', '111', NULL, '11111111111', NULL);
INSERT INTO `laoshi` VALUES (1739628555582, '2025-02-15 22:09:15', '555', '555', '555', NULL, '12365478953', NULL);
INSERT INTO `laoshi` VALUES (1747714312569, '2025-05-20 12:11:52', '8', '8', '8', NULL, '12345678901', NULL);

-- ----------------------------
-- Table structure for laoshihuida
-- ----------------------------
DROP TABLE IF EXISTS `laoshihuida`;
CREATE TABLE `laoshihuida`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `wenti` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '问题',
  `fengmian` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '封面',
  `kemuleixing` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '科目类型',
  `wentimiaoshu` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '问题描述',
  `xueshengzhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生账号',
  `xueshengxingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `laoshizhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '老师账号',
  `laoshixingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '老师姓名',
  `wentijieda` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '问题解答',
  `beizhu` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `thumbsupnum` int(11) NULL DEFAULT 0 COMMENT '赞',
  `crazilynum` int(11) NULL DEFAULT 0 COMMENT '踩',
  `clicknum` int(11) NULL DEFAULT 0 COMMENT '点击次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1747896003439 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '老师回答' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of laoshihuida
-- ----------------------------
INSERT INTO `laoshihuida` VALUES (51, '2021-04-15 14:47:38', '请简述什么是链表，以及链表有哪些常见类型？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1744010144800.jpg', '数据结构', '链表是一种物理存储单元上非连续的、非顺序的线性数据结构，数据元素的逻辑顺序是通过链表中的指针链接次序实现的。链表由一系列节点组成，每个节点包含两个部分：数据域和指针域。数据域存储数据元素的值，指针域则指向下一个节点的位置。链表的常见类型包括：单链表、双链表、循环链表。', '01', '王晨', 'teacher_001', '李华', '<p>链表的常见类型包括：单链表、双链表、循环链表。</p>', '暂无备注', 0, 0, 17);
INSERT INTO `laoshihuida` VALUES (52, '2021-04-15 14:47:38', '子网掩码的作用是什么？什么是变长子网掩码？为什么使用变长子网掩码？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747752090708.jpg', '计算机网络', '子网掩码的作用是确定一个IP地址的网络部分和主机部分。它通过与IP地址相对应的二进制数来分离网络地址和主机地址。变长子网掩码允许在同一个网络中使用不同的子网掩码长度，从而更加灵活地划分子网，避免IP地址浪费。使用变长子网掩码的原因是节省IP地址、支持不同规模的子网以及提高网络的灵活性和可扩展性。', '02', '李婷', 'teacher_002', '王丽', '<p>使用变长子网掩码的原因是节省IP地址、支持不同规模的子网以及提高网络的灵活性和可扩展性。</p>', '暂无备注', 18, 1, 58);
INSERT INTO `laoshihuida` VALUES (53, '2021-04-15 14:47:38', '如何使用 MATLAB 进行无线传感网络的简单仿真', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747752110627.jpg', '无线传感网', '使用MATLAB仿真无线传感网络，可以通过设置网络拓扑、定义通信模型、模拟数据传输和计算节点的能耗等步骤来进行。首先，随机生成传感器节点的位置；然后计算节点间的路径损耗；接着模拟数据的传输和信号接收；最后计算传输过程中的能耗。', '03', '张伟', 'teacher_003', '张杰', '<p>通过MATLAB仿真无线传感网络，可以评估网络的传输情况、能耗等性能。</p>', '暂无备注', 5, 2, 16);
INSERT INTO `laoshihuida` VALUES (54, '2021-04-15 14:47:38', '请解释什么是二叉树，以及二叉树有哪些遍历方式？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747752132889.jpg', '数据结构', '二叉树是每个节点最多有两个子树的树结构，通常子树被称作“左子树”和“右子树”。二叉树的遍历方式有：前序遍历、中序遍历、后序遍历和层序遍历。', '04', '刘晓宇', 'teacher_004', '刘思敏', '<p>二叉树的遍历方式有：前序遍历、中序遍历、后序遍历和层序遍历。</p>', '暂无备注', 3, 0, 9);
INSERT INTO `laoshihuida` VALUES (1736349052324, '2025-01-08 23:10:52', '子网掩码的作用是什么？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747752154579.jpg', '无线传感网', '子网掩码的作用是什么？', '大鸟yyds', '1', '11', '11', '<p><img src=\"../../../springboot7vkr1/upload/1736349050424.jpg\"></p>', NULL, 0, 0, 14);
INSERT INTO `laoshihuida` VALUES (1736567551161, '2025-01-11 11:52:31', '请简述什么是链表，以及链表有哪些常见类型？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747752171312.jpg', '数据结构', '链表是一种物理存储单元上非连续的、非顺序的线性数据结构，数据元素的逻辑顺序是通过链表中的指针链接次序实现的。链表由一系列节点组成，每个节点包含两个部分：数据域和指针域。常见的链表类型有：单链表、双链表和循环链表。', '01', '王晨', '1', '1', '<p><img src=\"../../../springboot7vkr1/upload/1736567549247.jpg\"></p>', '1', 0, 0, 2);
INSERT INTO `laoshihuida` VALUES (1736587289539, '2025-01-11 17:21:29', '子网掩码的作用是什么？什么是变长子网掩码？为什么使用变长子网掩码？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747752198884.jpg', '计算机网络', '子网掩码用于确定一个IP地址的网络部分和主机部分。变长子网掩码（VLSM）允许在同一个网络中使用不同的子网掩码长度，目的是更有效地利用IP地址空间。', '05', '刘晓宇', '1', '1', '<p><img src=\"../../../springboot7vkr1/upload/1736587276225.jpg\"></p>', '1', 0, 0, 3);
INSERT INTO `laoshihuida` VALUES (1747714373303, '2025-05-20 12:12:52', '123', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747714265360.jpeg', '数据结构', '测试', '2', '1', '8', '8', '<p><img src=\"../../../upload/1747714371785.jpg\" /></p>', NULL, 0, 0, 1);
INSERT INTO `laoshihuida` VALUES (1747896003438, '2025-05-22 14:40:03', '测试1', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747895947461.jpg', '硬件描述语言（HDL）', '测试1', '1', '1', '8', '8', '<p><img src=\"../../../upload/1747895997510.jpg\" />11111</p>', NULL, 0, 0, 1);

-- ----------------------------
-- Table structure for laoshixinxi
-- ----------------------------
DROP TABLE IF EXISTS `laoshixinxi`;
CREATE TABLE `laoshixinxi`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `laoshizhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '老师账号',
  `laoshixingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '老师姓名',
  `xingbie` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '性别',
  `shoujihaoma` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `touxiang` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `ziwojieshao` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '自我介绍',
  `thumbsupnum` int(11) NULL DEFAULT 0 COMMENT '赞',
  `crazilynum` int(11) NULL DEFAULT 0 COMMENT '踩',
  `clicktime` datetime(0) NULL DEFAULT NULL COMMENT '最近点击时间',
  `clicknum` int(11) NULL DEFAULT 0 COMMENT '点击次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1618469566615 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '老师信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of laoshixinxi
-- ----------------------------
INSERT INTO `laoshixinxi` VALUES (1001, '2021-04-15 14:47:38', 'teacher_001', '李华', '男', '13812345678', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740742082875.jpg', '<p>自我介绍1</p>', 1, 1, '2025-02-28 19:27:55', 8);
INSERT INTO `laoshixinxi` VALUES (1002, '2021-04-15 14:47:38', 'teacher_002', '王丽', '女', '13998765432', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740742096215.jpg', '<p>自我介绍2</p>', 2, 2, '2025-02-28 19:28:10', 20);
INSERT INTO `laoshixinxi` VALUES (1003, '2021-04-15 14:47:38', 'teacher_003', '张杰', '男', '13711223344', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740742149107.jpg', '<p>自我介绍3</p>', 3, 3, '2025-02-28 19:29:01', 5);
INSERT INTO `laoshixinxi` VALUES (1004, '2021-04-15 14:47:38', 'teacher_004', '刘思敏', '女', '13655667788', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740742134968.jpg', '<p>自我介绍4</p>', 4, 4, '2025-02-28 19:28:47', 13);
INSERT INTO `laoshixinxi` VALUES (1005, '2021-04-15 14:47:38', 'teacher_005', '陈东', '男', '13599887766', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740742122331.jpg', '<p>自我介绍5</p>', 5, 5, '2025-05-22 16:25:36', 16);
INSERT INTO `laoshixinxi` VALUES (1006, '2021-04-15 14:47:38', 'teacher_006', '赵雅丽', '女', '13933221100', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740742109624.jpg', '<p>自我介绍6</p>', 6, 6, '2025-10-04 12:23:49', 15);

-- ----------------------------
-- Table structure for storeup
-- ----------------------------
DROP TABLE IF EXISTS `storeup`;
CREATE TABLE `storeup`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `refid` bigint(20) NULL DEFAULT NULL COMMENT '收藏id',
  `tablename` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '表名',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收藏名称',
  `picture` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收藏图片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1736578020545 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of storeup
-- ----------------------------
INSERT INTO `storeup` VALUES (1618469645473, '2021-04-15 14:54:05', 1618469527598, 1618469632218, 'laoshihuida', '测试问题', 'http://localhost:8080/springboot7vkr1/upload/1618469496748.jpg');
INSERT INTO `storeup` VALUES (1736349203393, '2025-01-08 23:13:23', 1736348974384, 1736348945763, 'xueshengwenti', '子网掩码的作用是什么？', 'http://localhost:8080/springboot7vkr1/upload/1736348935192.jpg');

-- ----------------------------
-- Table structure for token
-- ----------------------------
DROP TABLE IF EXISTS `token`;
CREATE TABLE `token`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `tablename` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '表名',
  `role` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色',
  `token` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '新增时间',
  `expiratedtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'token表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of token
-- ----------------------------
INSERT INTO `token` VALUES (1, 1, 'abo', 'users', '管理员', 'fzrpsjbg1xpdrw33kycoj1pezwpztyjs', '2021-04-15 14:49:52', '2025-05-22 16:11:48');
INSERT INTO `token` VALUES (2, 1618469461656, '12', 'xuesheng', '学生', 'fvec3cr9bsouwbrxzj2mhyc22563pra0', '2021-04-15 14:51:06', '2021-04-15 15:55:18');
INSERT INTO `token` VALUES (3, 1618469527598, '122', 'laoshi', '老师', 'vnk3t3utb0fhavrjjweoe66mvdjzwzel', '2021-04-15 14:52:14', '2021-04-15 15:56:59');
INSERT INTO `token` VALUES (4, 1735632090571, '大鸟yyds', 'xuesheng', '学生', 'ocfh3l5mczuier0tvnbd1heip608y3m7', '2024-12-31 16:01:36', '2025-01-09 10:45:23');
INSERT INTO `token` VALUES (5, 1735639557383, '大鸟yyds1', 'laoshi', '老师', 'ghxrahsz1iprboqjlpt3bhpxyhxkuh7s', '2024-12-31 18:06:05', '2024-12-31 19:06:05');
INSERT INTO `token` VALUES (6, 1735879676275, '1', 'xuesheng', '学生', 'de7pgn23zgqbnn1c03m35z01v4txx7nl', '2025-01-03 12:48:04', '2025-10-04 13:24:31');
INSERT INTO `token` VALUES (7, 1736347217286, '2', 'laoshi', '老师', 'nxvb4b5jbr7m4pycakem9nqmwv5qd8dm', '2025-01-08 22:40:26', '2025-01-08 23:40:27');
INSERT INTO `token` VALUES (8, 1736348974384, '11', 'laoshi', '老师', 'g3njigw1bjf466jsrce98520425qquos', '2025-01-08 23:09:44', '2025-01-09 11:06:33');
INSERT INTO `token` VALUES (9, 1736482837366, '1', 'laoshi', '老师', 'bn4ciizo8hw6uj5ie3t1vby39dop1yh9', '2025-01-10 12:20:51', '2025-01-11 12:09:41');
INSERT INTO `token` VALUES (10, 1736500989659, '111', 'xuesheng', '学生', 'w5iztiadxthokl3qwx1nmok9yqlgbo9r', '2025-01-10 17:23:16', '2025-01-10 18:23:17');
INSERT INTO `token` VALUES (11, 1736501570213, '1111', 'laoshi', '老师', 'ifisaankzsjek9ima9t9lu0mhr2wo032', '2025-01-10 17:32:58', '2025-01-10 18:32:59');
INSERT INTO `token` VALUES (12, 1736563836502, '112', 'laoshi', '老师', '1nkcf3gi00ugndf50ht0gqkms0zppk49', '2025-01-11 10:50:48', '2025-01-11 11:50:49');
INSERT INTO `token` VALUES (13, 1736587395347, '12', 'laoshi', '老师', 'jp8hyisyxz3ivy29x2vfieylnt0pu91q', '2025-01-11 17:23:22', '2025-05-13 19:35:47');
INSERT INTO `token` VALUES (14, 1736587467385, '1221', 'laoshi', '老师', 'otoz8m7v1ntzi7m6spsbrlfp8lnugfm2', '2025-01-11 17:24:41', '2025-01-11 19:03:09');
INSERT INTO `token` VALUES (15, 1739529866622, '111', 'laoshi', '老师', 'ktfhjnl4rlbzoy9gx8m7u7nob3w104lo', '2025-02-14 18:44:32', '2025-02-14 19:44:32');
INSERT INTO `token` VALUES (16, 1739628555582, '555', 'laoshi', '老师', '8lu3v04vugbqwd4yazt3t01xcjbu0y98', '2025-02-15 22:09:24', '2025-02-15 23:09:24');
INSERT INTO `token` VALUES (17, 1747714138430, '2', 'xuesheng', '学生', '68wuobkc4ht1ley8i0aw344bvj5kp1f3', '2025-05-20 12:09:12', '2025-05-20 13:09:12');
INSERT INTO `token` VALUES (18, 1747714312569, '8', 'laoshi', '老师', 'svqaou37v1h312zpv0726cphfrns185r', '2025-05-20 12:12:01', '2025-05-22 17:11:41');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `role` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '管理员' COMMENT '角色',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '新增时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '2185', '2185', '管理员', '2021-04-15 14:47:38');

-- ----------------------------
-- Table structure for xuesheng
-- ----------------------------
DROP TABLE IF EXISTS `xuesheng`;
CREATE TABLE `xuesheng`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `xueshengzhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学生账号',
  `mima` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `xueshengxingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `xingbie` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '性别',
  `shoujihaoma` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `touxiang` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `xueshengzhanghao`(`xueshengzhanghao`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1747714138431 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '学生' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xuesheng
-- ----------------------------
INSERT INTO `xuesheng` VALUES (11, '2021-04-15 14:47:38', '学生1', '123456', '学生姓名1', '男', '13823888881', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740662843471.png');
INSERT INTO `xuesheng` VALUES (12, '2021-04-15 14:47:38', '学生2', '123456', '学生姓名2', '男', '13823888882', 'http://182.92.211.184:8080/springboot7vkr1/upload/1744008830780.jpg');
INSERT INTO `xuesheng` VALUES (13, '2021-04-15 14:47:38', '学生3', '123456', '学生姓名3', '男', '13823888883', 'http://localhost:8080/springboot7vkr1/upload/xuesheng_touxiang3.jpg');
INSERT INTO `xuesheng` VALUES (14, '2021-04-15 14:47:38', '学生4', '123456', '学生姓名4', '男', '13823888884', 'http://localhost:8080/springboot7vkr1/upload/xuesheng_touxiang4.jpg');
INSERT INTO `xuesheng` VALUES (15, '2021-04-15 14:47:38', '学生5', '123456', '学生姓名5', '男', '13823888885', 'http://localhost:8080/springboot7vkr1/upload/xuesheng_touxiang5.jpg');
INSERT INTO `xuesheng` VALUES (16, '2021-04-15 14:47:38', '学生6', '123456', '学生姓名6', '男', '13823888886', 'http://localhost:8080/springboot7vkr1/upload/xuesheng_touxiang6.jpg');
INSERT INTO `xuesheng` VALUES (1618469461656, '2021-04-15 14:51:01', '12', '12', '12', '女', '12345678908', 'http://localhost:8080/springboot7vkr1/upload/1618469473442.jpg');
INSERT INTO `xuesheng` VALUES (1735632090571, '2024-12-31 16:01:30', '大鸟yyds', '2b', '1', NULL, '11111111111', NULL);
INSERT INTO `xuesheng` VALUES (1735879676275, '2025-01-03 12:47:56', '1', '1', '1', '男', '12345678971', '/springboot7vkr1/upload/1741231546598.jpg');
INSERT INTO `xuesheng` VALUES (1736500989659, '2025-01-10 17:23:09', '111', '111', '111', NULL, '11111111111', NULL);
INSERT INTO `xuesheng` VALUES (1736655011206, '2025-01-12 12:10:11', '122', '122', '122', NULL, '11111111111', NULL);
INSERT INTO `xuesheng` VALUES (1747714138430, '2025-05-20 12:08:58', '2', '1', '1', NULL, '12345678912', NULL);

-- ----------------------------
-- Table structure for xueshengwenti
-- ----------------------------
DROP TABLE IF EXISTS `xueshengwenti`;
CREATE TABLE `xueshengwenti`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `wenti` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '问题',
  `fengmian` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '封面',
  `kemuleixing` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '科目类型',
  `wentimiaoshu` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '问题描述',
  `fabushijian` datetime(0) NULL DEFAULT NULL COMMENT '发布时间',
  `xueshengzhanghao` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生账号',
  `xueshengxingming` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `thumbsupnum` int(11) NULL DEFAULT 0 COMMENT '赞',
  `crazilynum` int(11) NULL DEFAULT 0 COMMENT '踩',
  `clicktime` datetime(0) NULL DEFAULT NULL COMMENT '最近点击时间',
  `clicknum` int(11) NULL DEFAULT 0 COMMENT '点击次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1747895964339 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '学生问题' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xueshengwenti
-- ----------------------------
INSERT INTO `xueshengwenti` VALUES (41, '2024-12-15 14:47:38', '请简述什么是链表，以及链表有哪些常见类型？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663057753.jpg', '数据结构', '链表是一种物理存储单元上非连续的、非顺序的线性数据结构，数据元素的逻辑顺序是通过链表中的指针链接次序实现的。链表由一系列节点组成，每个节点包含两个部分：数据域和指针域。常见的链表类型有：单链表、双链表和循环链表。', '2024-12-15 14:47:38', '01', '王晨', 6, 2, '2025-05-22 16:11:43', 44);
INSERT INTO `xueshengwenti` VALUES (42, '2024-12-15 14:47:38', '子网掩码的作用是什么？什么是变长子网掩码？为什么使用变长子网掩码？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663069326.jpg', '计算机网络', '子网掩码用于确定一个IP地址的网络部分和主机部分。变长子网掩码（VLSM）允许在同一个网络中使用不同的子网掩码长度，目的是更有效地利用IP地址空间。', '2024-12-15 14:47:38', '05', '刘晓宇', 10, 5, '2025-03-08 10:51:31', 127);
INSERT INTO `xueshengwenti` VALUES (43, '2024-12-15 11:40:35', '如何使用 MATLAB 进行无线传感网络的简单仿真', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663083328.jpg', '无线传感网', '使用MATLAB可以通过编写脚本来仿真无线传感网络，涉及节点的布置、信号传播模型、数据传输等方面的内容，适用于学习无线网络的基本概念与原理。', '2024-12-15 11:40:35', '04', '孙琪', 3, 3, '2025-10-04 12:26:50', 31);
INSERT INTO `xueshengwenti` VALUES (44, '2024-12-15 14:47:38', '请解释什么是二叉树，以及二叉树有哪些遍历方式？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663098150.jpg', '数据结构', '二叉树是每个节点最多有两个子树的树结构，常见的遍历方式包括前序遍历、中序遍历、后序遍历和层序遍历。', '2024-12-15 14:47:38', '01', '王晨', 4, 4, '2025-02-27 21:31:31', 7);
INSERT INTO `xueshengwenti` VALUES (45, '2024-12-14 14:47:20', '请简述什么是链表，以及链表有哪些常见类型？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663109892.jpg', '数据结构', '链表是一种物理存储单元上非连续的、非顺序的线性数据结构，数据元素的逻辑顺序是通过链表中的指针链接次序实现的。链表由一系列节点组成，每个节点包含两个部分：数据域和指针域。常见的链表类型有：单链表、双链表和循环链表。', '2024-12-14 14:47:20', '03', '赵阳', 5, 5, '2025-05-22 12:05:19', 30);
INSERT INTO `xueshengwenti` VALUES (46, '2024-12-10 17:07:00', '拥塞控制的原理？流量控制的原理？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663123924.jpg', '计算机网络', '拥塞控制和流量控制是TCP协议中的重要内容，分别用于控制网络中数据流量的传输速率和避免网络拥塞。', '2024-12-10 17:07:00', '02', '胡娜', 6, 6, '2025-02-27 21:31:55', 8);
INSERT INTO `xueshengwenti` VALUES (1736348945763, '2025-01-08 23:09:04', '子网掩码的作用是什么？', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663138007.jpg', '无线传感网', '子网掩码的作用是什么？', '2025-01-08 23:08:16', '大鸟yyds', '1', 0, 0, '2025-02-27 21:32:09', 18);
INSERT INTO `xueshengwenti` VALUES (1738992113319, '2025-02-08 13:21:52', '1', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663151705.jpg', '数据结构', '1', '2025-02-08 13:21:42', '1', '1', 0, 0, '2025-05-22 12:08:07', 9);
INSERT INTO `xueshengwenti` VALUES (1739003990192, '2025-02-08 16:39:49', 't1', 'http://182.92.211.184:8080/springboot7vkr1/upload/1740663162850.jpg', 'javaweb', 't1', '2025-02-08 16:39:25', '1', '1', 7, 0, '2025-03-15 21:28:23', 3);
INSERT INTO `xueshengwenti` VALUES (1747714285839, '2025-05-20 12:11:24', '123', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747714265360.jpeg', '数据结构', '测试', '2025-05-20 12:10:42', '2', '1', 8, 0, '2025-05-20 12:12:55', 3);
INSERT INTO `xueshengwenti` VALUES (1747895964338, '2025-05-22 14:39:24', '测试1', 'http://182.92.211.184:8080/springboot7vkr1/upload/1747895947461.jpg', '硬件描述语言（HDL）', '测试1', '2025-05-22 14:39:06', '1', '1', 0, 0, '2025-05-22 14:39:45', 1);

SET FOREIGN_KEY_CHECKS = 1;
