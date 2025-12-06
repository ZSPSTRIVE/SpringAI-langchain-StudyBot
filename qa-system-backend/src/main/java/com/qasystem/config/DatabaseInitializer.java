package com.qasystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 🗄️ 数据库初始化器 - 在应用启动时检查并创建必要的表结构
 * 
 * 📖 功能说明：
 * 数据库初始化器负责在应用启动时自动检查和创建必要的数据库表结构，
 * 确保应用能够正常运行。主要功能包括：
 * 1. 表结构检查 - 检查关键表是否存在
 * 2. 自动建表 - 如果表不存在则自动创建
 * 3. 初始数据 - 插入系统运行所需的基础数据
 * 4. 错误处理 - 记录初始化过程中的错误
 * 
 * 🔧 技术实现：
 * - 实现CommandLineRunner接口，在Spring容器初始化完成后自动执行
 * - 使用JdbcTemplate执行原生SQL语句
 * - 通过information_schema检查表是否存在，兼容多种数据库
 * 
 * 📋 初始化流程：
 * 1. 应用启动 → Spring容器初始化完成
 * 2. 执行run方法 → 检查forum表是否存在
 * 3. 表不存在 → 创建表结构 → 插入初始数据
 * 4. 表已存在 → 跳过创建，继续启动
 * 5. 记录日志 → 完成初始化
 * 
 * ⚠️ 注意事项：
 * - 只在开发环境使用，生产环境应使用数据库迁移工具
 * - 初始化过程是幂等的，多次运行不会产生副作用
 * - 如果初始化失败，应用仍会继续启动，但功能可能受限
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录初始化过程
@Component  // 标识为Spring组件，确保被Spring容器扫描和管理
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class DatabaseInitializer implements CommandLineRunner {

    /**
     * JDBC模板对象 - 用于执行原生SQL语句
     * Spring提供的简化数据库访问的工具类，封装了JDBC操作的复杂性
     * 相比直接使用Connection/Statement，JdbcTemplate提供了：
     * - 自动资源管理（连接、语句、结果集）
     * - 异常转换（将SQLException转换为Spring的DataAccessException）
     * - 简化的查询和更新方法
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 🚀 应用启动时执行的方法 - 检查并初始化数据库表结构
     * 
     * 业务流程：
     * 1. 查询information_schema.tables表，检查forum表是否存在
     * 2. 如果表不存在，调用createForumTable()方法创建表
     * 3. 如果表已存在，记录日志并跳过创建
     * 4. 捕获并记录任何异常，确保应用能够继续启动
     * 
     * 为什么使用information_schema？
     * - 标准SQL接口，兼容MySQL、PostgreSQL等多种数据库
     * - 不依赖特定数据库的SHOW TABLES语法
     * - 可以精确判断表是否存在，避免异常
     * 
     * @param args 命令行参数，由Spring Boot传入，本方法中未使用
     * @throws Exception 当初始化过程中发生严重错误时抛出
     */
    @Override
    public void run(String... args) {
        try {
            // 查询information_schema.tables表，检查forum表是否存在
            // DATABASE()函数获取当前数据库名称
            // COUNT(*)返回匹配的记录数，0表示表不存在
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'forum'",
                Integer.class
            );
            
            // 根据查询结果决定是否需要创建表
            if (count == null || count == 0) {
                log.info("📋 检测到forum表不存在，正在创建...");
                createForumTable();
                log.info("✅ forum表创建成功！");
            } else {
                log.info("✅ forum表已存在");
            }
        } catch (Exception e) {
            // 记录错误日志，但不阻止应用启动
            // 这样即使数据库初始化失败，应用仍可以继续启动
            // 管理员可以通过日志排查问题并手动修复
            log.error("❌ 数据库初始化失败", e);
        }
    }

    /**
     * 🔨 创建forum表 - 论坛/互助区功能的数据表
     * 
     * 表结构设计：
     * - id: 主键，自增长，确保每条记录有唯一标识
     * - title: 帖子标题，用于列表展示和搜索
     * - content: 帖子内容，使用LONGTEXT支持长文本，支持HTML格式
     * - parentid: 父节点ID，实现无限层级评论结构
     *   - 0表示顶级帖子（主题）
     *   - 非0表示评论或回复
     * - userid: 发帖用户ID，关联用户表
     * - username: 发帖用户名，冗余存储避免关联查询
     * - isdone: 状态标识，如"开放"、"关闭"等
     * - addtime: 创建时间，默认为当前时间
     * 
     * 索引设计：
     * - 主键索引：id字段，自动创建
     * - idx_parentid：parentid字段，加速查询某个帖子的所有评论
     * - idx_userid：userid字段，加速查询某个用户的所有帖子
     * - idx_addtime：addtime字段，支持按时间排序和分页查询
     * 
     * 字符集和排序规则：
     * - utf8mb4：支持完整的UTF-8字符集，包括emoji表情
     * - utf8mb4_unicode_ci：不区分大小写的Unicode排序规则
     */
    private void createForumTable() {
        // 使用多行字符串定义建表SQL，提高可读性
        // CREATE TABLE IF NOT EXISTS确保幂等性，多次执行不会报错
        String createTableSql = """
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互助区/论坛'
            """;
        
        // 执行建表SQL
        jdbcTemplate.execute(createTableSql);
        
        // 建表完成后，插入初始数据
        insertInitialData();
    }

    /**
     * 📝 插入初始数据 - 为论坛功能提供基础数据
     * 
     * 初始数据包括：
     * 1. 欢迎帖子 - 介绍论坛用途和使用方法
     * 2. Java学习交流 - 为Java学习者提供讨论区
     * 3. 前端技术讨论 - 为前端开发者提供交流平台
     * 
     * 设计原则：
     * - 幂等性：检查是否已有数据，避免重复插入
     * - 容错性：插入失败不影响应用启动
     * - 实用性：提供有价值的初始内容，引导用户使用
     * 
     * 数据内容：
     * - 使用HTML格式的内容，支持富文本展示
     * - 用户ID设为1，假设系统管理员ID为1
     * - 状态设为"开放"，表示可以回复和讨论
     * - parentid设为0，表示这些都是顶级帖子
     */
    private void insertInitialData() {
        try {
            // 先检查表中是否已有数据
            // 避免重复插入初始数据
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM forum", Integer.class);
            
            // 只有当表为空时才插入初始数据
            if (count == null || count == 0) {
                log.info("📝 插入初始论坛数据...");
                
                // 使用批量插入SQL，一次性插入多条记录
                // VALUES占位符与后面的参数数组一一对应
                String insertSql = """
                    INSERT INTO `forum` (`title`, `content`, `parentid`, `userid`, `username`, `isdone`, `addtime`) VALUES
                    (?, ?, ?, ?, ?, ?, NOW()),
                    (?, ?, ?, ?, ?, ?, NOW()),
                    (?, ?, ?, ?, ?, ?, NOW())
                    """;
                
                // 执行批量插入，参数按顺序与SQL中的占位符对应
                jdbcTemplate.update(insertSql,
                    "欢迎来到师生答疑系统交流区", "<p>这里是师生交流互助的平台，大家可以自由讨论学习问题。</p>", 0, 1, "系统管理员", "开放",
                    "Java学习交流", "<p>欢迎大家交流Java学习心得！</p>", 0, 1, "系统管理员", "开放",
                    "前端技术讨论", "<p>Vue、React等前端技术讨论专区</p>", 0, 1, "系统管理员", "开放"
                );
                
                log.info("✅ 初始论坛数据插入成功！");
            }
        } catch (Exception e) {
            // 使用warn级别记录插入失败，而不是error
            // 因为插入失败通常是因为数据已存在，不是严重错误
            // 记录详细错误信息便于排查问题
            log.warn("⚠️ 插入初始数据失败（可能已存在）: {}", e.getMessage());
        }
    }
}

