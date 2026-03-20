package com.qasystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private static final String USER_TABLE = "user";
    private static final String STUDENT_TABLE = "student";
    private static final String TEACHER_TABLE = "teacher";
    private static final String FORUM_TABLE = "forum";
    private static final String AI_MODEL_CONFIG_TABLE = "ai_model_config";

    private static final String DEFAULT_PASSWORD_HASH =
            "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKfqg.aO";

    private static final List<SeedUser> DEMO_USERS = List.of(
            new SeedUser("admin", "System Admin", "ADMIN", "admin@qasystem.local"),
            new SeedUser("student1", "Student One", "STUDENT", "student1@qasystem.local"),
            new SeedUser("teacher1", "Teacher One", "TEACHER", "teacher1@qasystem.local")
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        DatabaseDialect dialect = detectDialect();
        log.info("Database initializer detected dialect={}", dialect.name().toLowerCase(Locale.ROOT));

        ensureCoreAuthTables(dialect);
        ensureDemoUsers(dialect);
        ensureDemoProfiles(dialect);
        ensureForumTable(dialect);
        ensureAiModelConfigTable(dialect);
    }

    private void ensureCoreAuthTables(DatabaseDialect dialect) {
        ensureTable(USER_TABLE, userTableStatements(dialect));
        ensureTable(STUDENT_TABLE, studentTableStatements(dialect));
        ensureTable(TEACHER_TABLE, teacherTableStatements(dialect));
    }

    private void ensureForumTable(DatabaseDialect dialect) {
        try {
            if (tableExists(FORUM_TABLE)) {
                log.info("{} table already exists", FORUM_TABLE);
                insertInitialForumData(dialect);
                return;
            }

            executeStatements(forumTableStatements(dialect));
            insertInitialForumData(dialect);
            log.info("{} table created successfully", FORUM_TABLE);
        } catch (Exception ex) {
            log.error("{} table initialization failed", FORUM_TABLE, ex);
        }
    }

    private void ensureAiModelConfigTable(DatabaseDialect dialect) {
        try {
            if (tableExists(AI_MODEL_CONFIG_TABLE)) {
                log.info("{} table already exists", AI_MODEL_CONFIG_TABLE);
                return;
            }

            executeStatements(aiModelConfigStatements(dialect));
            log.info("{} table created successfully", AI_MODEL_CONFIG_TABLE);
        } catch (Exception ex) {
            log.error("{} table initialization failed", AI_MODEL_CONFIG_TABLE, ex);
        }
    }

    private void ensureTable(String tableName, List<String> statements) {
        try {
            if (tableExists(tableName)) {
                log.info("{} table already exists", tableName);
                return;
            }

            executeStatements(statements);
            log.info("{} table created successfully", tableName);
        } catch (Exception ex) {
            log.error("{} table initialization failed", tableName, ex);
        }
    }

    private void ensureDemoUsers(DatabaseDialect dialect) {
        if (!tableExists(USER_TABLE)) {
            return;
        }

        String tableRef = tableRef(USER_TABLE, dialect);
        String countSql = "SELECT COUNT(*) FROM " + tableRef + " WHERE username = ?";
        String insertSql = """
                INSERT INTO %s
                (username, password, real_name, role, email, phone, avatar, gender, status, create_time, update_time, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """.formatted(tableRef);

        for (SeedUser seedUser : DEMO_USERS) {
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, seedUser.username());
            if (count != null && count > 0) {
                continue;
            }

            jdbcTemplate.update(
                    insertSql,
                    seedUser.username(),
                    DEFAULT_PASSWORD_HASH,
                    seedUser.realName(),
                    seedUser.role(),
                    seedUser.email(),
                    null,
                    null,
                    "U",
                    "ACTIVE"
            );
            log.info("Seeded demo user {}", seedUser.username());
        }
    }

    private void ensureDemoProfiles(DatabaseDialect dialect) {
        ensureStudentProfile(dialect);
        ensureTeacherProfile(dialect);
    }

    private void ensureStudentProfile(DatabaseDialect dialect) {
        Optional<Long> studentUserId = findUserId(dialect, "student1");
        if (studentUserId.isEmpty() || profileExists(STUDENT_TABLE, dialect, studentUserId.get())) {
            return;
        }

        String insertSql = """
                INSERT INTO %s
                (user_id, student_no, major, class_name, grade, college, create_time, update_time, deleted)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """.formatted(tableRef(STUDENT_TABLE, dialect));

        jdbcTemplate.update(
                insertSql,
                studentUserId.get(),
                "S20240001",
                "Computer Science",
                "CS-1",
                2024,
                "School of Computer Science"
        );
        log.info("Seeded demo student profile for student1");
    }

    private void ensureTeacherProfile(DatabaseDialect dialect) {
        Optional<Long> teacherUserId = findUserId(dialect, "teacher1");
        if (teacherUserId.isEmpty() || profileExists(TEACHER_TABLE, dialect, teacherUserId.get())) {
            return;
        }

        String insertSql = """
                INSERT INTO %s
                (user_id, teacher_no, title, college, research, office, bio, create_time, update_time, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """.formatted(tableRef(TEACHER_TABLE, dialect));

        jdbcTemplate.update(
                insertSql,
                teacherUserId.get(),
                "T20240001",
                "Lecturer",
                "School of Computer Science",
                "Distributed Systems",
                "B-301",
                "Demo teacher for local startup"
        );
        log.info("Seeded demo teacher profile for teacher1");
    }

    private Optional<Long> findUserId(DatabaseDialect dialect, String username) {
        String sql = "SELECT id FROM " + tableRef(USER_TABLE, dialect) + " WHERE username = ?";
        List<Long> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong(1), username);
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ids.get(0));
    }

    private boolean profileExists(String tableName, DatabaseDialect dialect, Long userId) {
        if (!tableExists(tableName)) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM " + tableRef(tableName, dialect) + " WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    private void insertInitialForumData(DatabaseDialect dialect) {
        String forumRef = tableRef(FORUM_TABLE, dialect);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + forumRef, Integer.class);
        if (count != null && count > 0) {
            return;
        }

        Long adminUserId = findUserId(dialect, "admin").orElse(1L);
        String insertSql = """
                INSERT INTO %s (title, content, parentid, userid, username, isdone, addtime)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """.formatted(forumRef);

        jdbcTemplate.update(
                insertSql,
                "Welcome to QA System",
                "<p>This is the collaboration area for teachers and students.</p>",
                0L,
                adminUserId,
                "admin",
                "OPEN"
        );
        jdbcTemplate.update(
                insertSql,
                "Java learning discussion",
                "<p>Share Java interview prep and backend notes here.</p>",
                0L,
                adminUserId,
                "admin",
                "OPEN"
        );
        jdbcTemplate.update(
                insertSql,
                "Frontend discussion",
                "<p>Discuss Vue, React and browser topics here.</p>",
                0L,
                adminUserId,
                "admin",
                "OPEN"
        );
        log.info("Seeded forum records");
    }

    private void executeStatements(List<String> statements) {
        for (String statement : statements) {
            jdbcTemplate.execute(statement);
        }
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                tableExists(connection, connection.getCatalog(), connection.getSchema(), tableName)
                        || tableExists(connection, connection.getCatalog(), null, tableName)
        ));
    }

    private boolean tableExists(Connection connection, String catalog, String schema, String tableName)
            throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(catalog, schema, null, new String[]{"TABLE"})) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private DatabaseDialect detectDialect() {
        return jdbcTemplate.execute((ConnectionCallback<DatabaseDialect>) connection ->
                DatabaseDialect.fromProductName(connection.getMetaData().getDatabaseProductName())
        );
    }

    private String tableRef(String tableName, DatabaseDialect dialect) {
        return switch (dialect) {
            case MYSQL -> "`" + tableName + "`";
            case POSTGRESQL -> "\"" + tableName + "\"";
            case OTHER -> tableName;
        };
    }

    private List<String> userTableStatements(DatabaseDialect dialect) {
        return switch (dialect) {
            case MYSQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS `user` (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        username VARCHAR(50) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        real_name VARCHAR(100),
                        role VARCHAR(20) NOT NULL,
                        email VARCHAR(100),
                        phone VARCHAR(30),
                        avatar VARCHAR(255),
                        gender VARCHAR(10) NOT NULL DEFAULT 'U',
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted TINYINT NOT NULL DEFAULT 0,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_user_username (username),
                        UNIQUE KEY uk_user_email (email),
                        KEY idx_user_role (role),
                        KEY idx_user_status (status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """
            );
            case POSTGRESQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS "user" (
                        id BIGSERIAL PRIMARY KEY,
                        username VARCHAR(50) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        real_name VARCHAR(100),
                        role VARCHAR(20) NOT NULL,
                        email VARCHAR(100),
                        phone VARCHAR(30),
                        avatar VARCHAR(255),
                        gender VARCHAR(10) NOT NULL DEFAULT 'U',
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted INTEGER NOT NULL DEFAULT 0,
                        CONSTRAINT uk_user_username UNIQUE (username),
                        CONSTRAINT uk_user_email UNIQUE (email)
                    )
                    """,
                    "CREATE INDEX idx_user_role ON \"user\" (role)",
                    "CREATE INDEX idx_user_status ON \"user\" (status)"
            );
            case OTHER -> throw new IllegalStateException("Unsupported database dialect for user bootstrap");
        };
    }

    private List<String> studentTableStatements(DatabaseDialect dialect) {
        return switch (dialect) {
            case MYSQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS student (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        student_no VARCHAR(50),
                        major VARCHAR(100),
                        class_name VARCHAR(100),
                        grade INT,
                        college VARCHAR(100),
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted TINYINT NOT NULL DEFAULT 0,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_student_user_id (user_id),
                        UNIQUE KEY uk_student_no (student_no),
                        KEY idx_student_college (college),
                        KEY idx_student_grade (grade)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """
            );
            case POSTGRESQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS student (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        student_no VARCHAR(50),
                        major VARCHAR(100),
                        class_name VARCHAR(100),
                        grade INTEGER,
                        college VARCHAR(100),
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted INTEGER NOT NULL DEFAULT 0,
                        CONSTRAINT uk_student_user_id UNIQUE (user_id),
                        CONSTRAINT uk_student_no UNIQUE (student_no)
                    )
                    """,
                    "CREATE INDEX idx_student_college ON student (college)",
                    "CREATE INDEX idx_student_grade ON student (grade)"
            );
            case OTHER -> throw new IllegalStateException("Unsupported database dialect for student bootstrap");
        };
    }

    private List<String> teacherTableStatements(DatabaseDialect dialect) {
        return switch (dialect) {
            case MYSQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS teacher (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        teacher_no VARCHAR(50),
                        title VARCHAR(100),
                        college VARCHAR(100),
                        research VARCHAR(255),
                        office VARCHAR(100),
                        bio TEXT,
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted TINYINT NOT NULL DEFAULT 0,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_teacher_user_id (user_id),
                        UNIQUE KEY uk_teacher_no (teacher_no),
                        KEY idx_teacher_college (college),
                        KEY idx_teacher_title (title)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """
            );
            case POSTGRESQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS teacher (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        teacher_no VARCHAR(50),
                        title VARCHAR(100),
                        college VARCHAR(100),
                        research VARCHAR(255),
                        office VARCHAR(100),
                        bio TEXT,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted INTEGER NOT NULL DEFAULT 0,
                        CONSTRAINT uk_teacher_user_id UNIQUE (user_id),
                        CONSTRAINT uk_teacher_no UNIQUE (teacher_no)
                    )
                    """,
                    "CREATE INDEX idx_teacher_college ON teacher (college)",
                    "CREATE INDEX idx_teacher_title ON teacher (title)"
            );
            case OTHER -> throw new IllegalStateException("Unsupported database dialect for teacher bootstrap");
        };
    }

    private List<String> forumTableStatements(DatabaseDialect dialect) {
        return switch (dialect) {
            case MYSQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS forum (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        title VARCHAR(200),
                        content LONGTEXT NOT NULL,
                        parentid BIGINT DEFAULT 0,
                        userid BIGINT NOT NULL,
                        username VARCHAR(200),
                        isdone VARCHAR(200),
                        addtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        KEY idx_forum_parentid (parentid),
                        KEY idx_forum_userid (userid),
                        KEY idx_forum_addtime (addtime)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """
            );
            case POSTGRESQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS forum (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(200),
                        content TEXT NOT NULL,
                        parentid BIGINT DEFAULT 0,
                        userid BIGINT NOT NULL,
                        username VARCHAR(200),
                        isdone VARCHAR(200),
                        addtime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """,
                    "CREATE INDEX idx_forum_parentid ON forum (parentid)",
                    "CREATE INDEX idx_forum_userid ON forum (userid)",
                    "CREATE INDEX idx_forum_addtime ON forum (addtime)"
            );
            case OTHER -> throw new IllegalStateException("Unsupported database dialect for forum bootstrap");
        };
    }

    private List<String> aiModelConfigStatements(DatabaseDialect dialect) {
        return switch (dialect) {
            case MYSQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS ai_model_config (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        provider VARCHAR(64) NOT NULL DEFAULT '',
                        provider_name VARCHAR(100) NOT NULL DEFAULT '',
                        model_name VARCHAR(120) NOT NULL DEFAULT '',
                        model_display_name VARCHAR(120),
                        api_key VARCHAR(512) NOT NULL DEFAULT '',
                        base_url VARCHAR(512) NOT NULL DEFAULT '',
                        temperature DECIMAL(4,2) DEFAULT 0.70,
                        max_tokens INT DEFAULT 2000,
                        default_rewrite_style VARCHAR(50),
                        enabled TINYINT(1) NOT NULL DEFAULT 1,
                        is_active TINYINT(1) NOT NULL DEFAULT 0,
                        remark VARCHAR(500),
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        KEY idx_ai_model_enabled_active (enabled, is_active),
                        KEY idx_ai_model_provider (provider),
                        KEY idx_ai_model_updated_at (updated_at)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """
            );
            case POSTGRESQL -> List.of(
                    """
                    CREATE TABLE IF NOT EXISTS ai_model_config (
                        id BIGSERIAL PRIMARY KEY,
                        provider VARCHAR(64) NOT NULL DEFAULT '',
                        provider_name VARCHAR(100) NOT NULL DEFAULT '',
                        model_name VARCHAR(120) NOT NULL DEFAULT '',
                        model_display_name VARCHAR(120),
                        api_key VARCHAR(512) NOT NULL DEFAULT '',
                        base_url VARCHAR(512) NOT NULL DEFAULT '',
                        temperature NUMERIC(4,2) DEFAULT 0.70,
                        max_tokens INTEGER DEFAULT 2000,
                        default_rewrite_style VARCHAR(50),
                        enabled BOOLEAN NOT NULL DEFAULT TRUE,
                        is_active BOOLEAN NOT NULL DEFAULT FALSE,
                        remark VARCHAR(500),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """,
                    "CREATE INDEX idx_ai_model_enabled_active ON ai_model_config (enabled, is_active)",
                    "CREATE INDEX idx_ai_model_provider ON ai_model_config (provider)",
                    "CREATE INDEX idx_ai_model_updated_at ON ai_model_config (updated_at)"
            );
            case OTHER -> throw new IllegalStateException("Unsupported database dialect for ai model config bootstrap");
        };
    }

    private enum DatabaseDialect {
        MYSQL,
        POSTGRESQL,
        OTHER;

        private static DatabaseDialect fromProductName(String productName) {
            String normalized = productName == null ? "" : productName.toLowerCase(Locale.ROOT);
            if (normalized.contains("mysql")) {
                return MYSQL;
            }
            if (normalized.contains("postgresql")) {
                return POSTGRESQL;
            }
            return OTHER;
        }
    }

    private record SeedUser(String username, String realName, String role, String email) {
    }
}
