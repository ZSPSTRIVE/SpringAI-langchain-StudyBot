package com.qasystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化器 - 在应用启动时检查并创建必要的表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // 检查forum表是否存在
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'forum'",
                Integer.class
            );
            
            if (count == null || count == 0) {
                log.info("📋 检测到forum表不存在，正在创建...");
                createForumTable();
                log.info("✅ forum表创建成功！");
            } else {
                log.info("✅ forum表已存在");
            }
        } catch (Exception e) {
            log.error("❌ 数据库初始化失败", e);
        }
    }

    private void createForumTable() {
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
        
        jdbcTemplate.execute(createTableSql);
        
        // 插入一些初始数据
        insertInitialData();
    }

    private void insertInitialData() {
        try {
            // 检查是否已有数据
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM forum", Integer.class);
            
            if (count == null || count == 0) {
                log.info("📝 插入初始论坛数据...");
                
                String insertSql = """
                    INSERT INTO `forum` (`title`, `content`, `parentid`, `userid`, `username`, `isdone`, `addtime`) VALUES
                    (?, ?, ?, ?, ?, ?, NOW()),
                    (?, ?, ?, ?, ?, ?, NOW()),
                    (?, ?, ?, ?, ?, ?, NOW())
                    """;
                
                jdbcTemplate.update(insertSql,
                    "欢迎来到师生答疑系统交流区", "<p>这里是师生交流互助的平台，大家可以自由讨论学习问题。</p>", 0, 1, "系统管理员", "开放",
                    "Java学习交流", "<p>欢迎大家交流Java学习心得！</p>", 0, 1, "系统管理员", "开放",
                    "前端技术讨论", "<p>Vue、React等前端技术讨论专区</p>", 0, 1, "系统管理员", "开放"
                );
                
                log.info("✅ 初始论坛数据插入成功！");
            }
        } catch (Exception e) {
            log.warn("⚠️ 插入初始数据失败（可能已存在）: {}", e.getMessage());
        }
    }
}

