package com.qasystem.ai.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RagMetadataRepository {

    private static final int RECENT_TASK_LIMIT = 10;
    private static final String DIALECT_POSTGRESQL = "postgresql";
    private static final String DIALECT_MYSQL = "mysql";
    private static final String RAG_CHUNK_TABLE = "rag_chunk_store";
    private static final String RAG_CHUNK_INDEX_KB_DOC = "idx_rag_chunk_store_kb_doc";
    private static final String RAG_CHUNK_INDEX_KB_POINT = "idx_rag_chunk_store_kb_point";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private volatile String databaseDialect;

    public RagMetadataRepository(@Qualifier("ragMetadataJdbcTemplate") JdbcTemplate jdbcTemplate,
                                 ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void initializeSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rag_knowledge_base (
                    knowledge_base_id VARCHAR(100) PRIMARY KEY,
                    name VARCHAR(200) NOT NULL,
                    description TEXT,
                    enabled BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rag_source_document (
                    document_id VARCHAR(160) PRIMARY KEY,
                    knowledge_base_id VARCHAR(100) NOT NULL,
                    source_type VARCHAR(40) NOT NULL,
                    source_ref VARCHAR(120) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    knowledge_point VARCHAR(64),
                    source_uri TEXT,
                    chunk_count INTEGER NOT NULL DEFAULT 0,
                    metadata_json TEXT,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        ensureIndex("rag_source_document", "idx_rag_document_kb_source",
                "CREATE INDEX idx_rag_document_kb_source ON rag_source_document (knowledge_base_id, source_type)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rag_chunk_store (
                    chunk_id VARCHAR(190) PRIMARY KEY,
                    vector_id VARCHAR(190),
                    knowledge_base_id VARCHAR(100) NOT NULL,
                    document_id VARCHAR(160) NOT NULL,
                    source_type VARCHAR(40) NOT NULL,
                    source_ref VARCHAR(120) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    knowledge_point VARCHAR(64),
                    chunk_index INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    metadata_json TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        ensureIndex(RAG_CHUNK_TABLE, RAG_CHUNK_INDEX_KB_DOC,
                "CREATE INDEX " + RAG_CHUNK_INDEX_KB_DOC + " ON " + RAG_CHUNK_TABLE
                        + " (knowledge_base_id, document_id, chunk_index)");
        ensureIndex(RAG_CHUNK_TABLE, RAG_CHUNK_INDEX_KB_POINT,
                "CREATE INDEX " + RAG_CHUNK_INDEX_KB_POINT + " ON " + RAG_CHUNK_TABLE
                        + " (knowledge_base_id, knowledge_point)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rag_sync_task (
                    task_id VARCHAR(64) PRIMARY KEY,
                    knowledge_base_id VARCHAR(100) NOT NULL,
                    task_type VARCHAR(40) NOT NULL,
                    source_summary TEXT,
                    status VARCHAR(32) NOT NULL,
                    source_document_count INTEGER NOT NULL DEFAULT 0,
                    document_count INTEGER NOT NULL DEFAULT 0,
                    chunk_count INTEGER NOT NULL DEFAULT 0,
                    failed_document_count INTEGER NOT NULL DEFAULT 0,
                    message TEXT,
                    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    finished_at TIMESTAMP
                )
                """);

        ensureColumn("rag_sync_task", "source_document_count",
                "ALTER TABLE rag_sync_task ADD COLUMN source_document_count INTEGER NOT NULL DEFAULT 0");
        ensureColumn("rag_sync_task", "failed_document_count",
                "ALTER TABLE rag_sync_task ADD COLUMN failed_document_count INTEGER NOT NULL DEFAULT 0");

        ensureIndex("rag_sync_task", "idx_rag_sync_task_kb_started",
                "CREATE INDEX idx_rag_sync_task_kb_started ON rag_sync_task (knowledge_base_id, started_at DESC)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rag_query_audit (
                    audit_id VARCHAR(64) PRIMARY KEY,
                    knowledge_base_id VARCHAR(100) NOT NULL,
                    query_text TEXT NOT NULL,
                    knowledge_point VARCHAR(64),
                    hit_count INTEGER NOT NULL,
                    top_titles TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private void ensureIndex(String tableName, String indexName, String createSql) {
        if (indexExists(tableName, indexName)) {
            return;
        }
        jdbcTemplate.execute(createSql);
    }

    public void ensureKnowledgeBase(String knowledgeBaseId, String description) {
        String dialect = databaseDialect();
        if (DIALECT_POSTGRESQL.equals(dialect)) {
            jdbcTemplate.update("""
                            INSERT INTO rag_knowledge_base (
                                knowledge_base_id, name, description, enabled, created_at, updated_at
                            ) VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            ON CONFLICT (knowledge_base_id) DO UPDATE
                            SET description = EXCLUDED.description,
                                updated_at = CURRENT_TIMESTAMP
                            """,
                    knowledgeBaseId,
                    knowledgeBaseId,
                    description
            );
            return;
        }

        if (DIALECT_MYSQL.equals(dialect)) {
            jdbcTemplate.update("""
                            INSERT INTO rag_knowledge_base (
                                knowledge_base_id, name, description, enabled, created_at, updated_at
                            ) VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            ON DUPLICATE KEY UPDATE
                                description = VALUES(description),
                                updated_at = CURRENT_TIMESTAMP
                            """,
                    knowledgeBaseId,
                    knowledgeBaseId,
                    description
            );
            return;
        }

        int updated = jdbcTemplate.update("""
                        UPDATE rag_knowledge_base
                        SET description = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE knowledge_base_id = ?
                        """,
                description,
                knowledgeBaseId
        );
        if (updated <= 0) {
            jdbcTemplate.update("""
                            INSERT INTO rag_knowledge_base (
                                knowledge_base_id, name, description, enabled, created_at, updated_at
                            ) VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                    knowledgeBaseId,
                    knowledgeBaseId,
                    description
            );
        }
    }

    public String startSyncTask(String knowledgeBaseId, String taskType, List<String> sourceTypes) {
        String taskId = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                        INSERT INTO rag_sync_task (
                            task_id, knowledge_base_id, task_type, source_summary, status, started_at
                        ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                taskId,
                knowledgeBaseId,
                taskType,
                String.join(",", sourceTypes),
                "RUNNING"
        );
        return taskId;
    }

    public void finishSyncTask(String taskId,
                               String status,
                               int sourceDocumentCount,
                               int documentCount,
                               int chunkCount,
                               int failedDocumentCount,
                               String message) {
        jdbcTemplate.update("""
                        UPDATE rag_sync_task
                        SET status = ?, source_document_count = ?, document_count = ?, chunk_count = ?,
                            failed_document_count = ?, message = ?, finished_at = CURRENT_TIMESTAMP
                        WHERE task_id = ?
                        """,
                status,
                sourceDocumentCount,
                documentCount,
                chunkCount,
                failedDocumentCount,
                message,
                taskId
        );
    }

    public void failSyncTask(String taskId, String message) {
        jdbcTemplate.update("""
                        UPDATE rag_sync_task
                        SET status = ?, message = ?, finished_at = CURRENT_TIMESTAMP
                        WHERE task_id = ?
                        """,
                "FAILED",
                message,
                taskId
        );
    }

    public List<String> findVectorIdsByDocument(String knowledgeBaseId, String documentId) {
        return jdbcTemplate.query("""
                        SELECT vector_id
                        FROM rag_chunk_store
                        WHERE knowledge_base_id = ? AND document_id = ? AND vector_id IS NOT NULL
                        """,
                (rs, rowNum) -> rs.getString("vector_id"),
                knowledgeBaseId,
                documentId
        );
    }

    public void deleteDocument(String knowledgeBaseId, String documentId) {
        jdbcTemplate.update(
                "DELETE FROM " + RAG_CHUNK_TABLE + " WHERE knowledge_base_id = ? AND document_id = ?",
                knowledgeBaseId,
                documentId
        );
        jdbcTemplate.update(
                "DELETE FROM rag_source_document WHERE knowledge_base_id = ? AND document_id = ?",
                knowledgeBaseId,
                documentId
        );
    }

    public void saveDocument(RagIndexDocument document, int chunkCount) {
        String metadataJson = toJson(document.metadata());
        String knowledgePoint = emptyToNull(document.knowledgePoint());
        String sourceUri = emptyToNull(document.sourceUri());
        String dialect = databaseDialect();

        if (DIALECT_POSTGRESQL.equals(dialect)) {
            jdbcTemplate.update("""
                            INSERT INTO rag_source_document (
                                document_id, knowledge_base_id, source_type, source_ref, title,
                                knowledge_point, source_uri, chunk_count, metadata_json, updated_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                            ON CONFLICT (document_id) DO UPDATE
                            SET knowledge_base_id = EXCLUDED.knowledge_base_id,
                                source_type = EXCLUDED.source_type,
                                source_ref = EXCLUDED.source_ref,
                                title = EXCLUDED.title,
                                knowledge_point = EXCLUDED.knowledge_point,
                                source_uri = EXCLUDED.source_uri,
                                chunk_count = EXCLUDED.chunk_count,
                                metadata_json = EXCLUDED.metadata_json,
                                updated_at = CURRENT_TIMESTAMP
                            """,
                    document.documentId(),
                    document.knowledgeBaseId(),
                    document.sourceType(),
                    document.sourceRef(),
                    document.title(),
                    knowledgePoint,
                    sourceUri,
                    chunkCount,
                    metadataJson
            );
            return;
        }

        if (DIALECT_MYSQL.equals(dialect)) {
            jdbcTemplate.update("""
                            INSERT INTO rag_source_document (
                                document_id, knowledge_base_id, source_type, source_ref, title,
                                knowledge_point, source_uri, chunk_count, metadata_json, updated_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                            ON DUPLICATE KEY UPDATE
                                knowledge_base_id = VALUES(knowledge_base_id),
                                source_type = VALUES(source_type),
                                source_ref = VALUES(source_ref),
                                title = VALUES(title),
                                knowledge_point = VALUES(knowledge_point),
                                source_uri = VALUES(source_uri),
                                chunk_count = VALUES(chunk_count),
                                metadata_json = VALUES(metadata_json),
                                updated_at = CURRENT_TIMESTAMP
                            """,
                    document.documentId(),
                    document.knowledgeBaseId(),
                    document.sourceType(),
                    document.sourceRef(),
                    document.title(),
                    knowledgePoint,
                    sourceUri,
                    chunkCount,
                    metadataJson
            );
            return;
        }

        int updated = jdbcTemplate.update("""
                        UPDATE rag_source_document
                        SET knowledge_base_id = ?, source_type = ?, source_ref = ?, title = ?,
                            knowledge_point = ?, source_uri = ?, chunk_count = ?, metadata_json = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE document_id = ?
                        """,
                document.knowledgeBaseId(),
                document.sourceType(),
                document.sourceRef(),
                document.title(),
                knowledgePoint,
                sourceUri,
                chunkCount,
                metadataJson,
                document.documentId()
        );
        if (updated <= 0) {
            jdbcTemplate.update("""
                            INSERT INTO rag_source_document (
                                document_id, knowledge_base_id, source_type, source_ref, title,
                                knowledge_point, source_uri, chunk_count, metadata_json, updated_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                            """,
                    document.documentId(),
                    document.knowledgeBaseId(),
                    document.sourceType(),
                    document.sourceRef(),
                    document.title(),
                    knowledgePoint,
                    sourceUri,
                    chunkCount,
                    metadataJson
            );
        }
    }

    public void saveChunks(List<RagStoredChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
                        INSERT INTO rag_chunk_store (
                            chunk_id, vector_id, knowledge_base_id, document_id, source_type,
                            source_ref, title, knowledge_point, chunk_index, content, metadata_json, created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                chunks,
                chunks.size(),
                (ps, chunk) -> {
                    ps.setString(1, chunk.chunkId());
                    ps.setString(2, chunk.vectorId());
                    ps.setString(3, chunk.knowledgeBaseId());
                    ps.setString(4, chunk.documentId());
                    ps.setString(5, chunk.sourceType());
                    ps.setString(6, chunk.sourceRef());
                    ps.setString(7, chunk.title());
                    ps.setString(8, emptyToNull(chunk.knowledgePoint()));
                    ps.setInt(9, chunk.chunkIndex());
                    ps.setString(10, chunk.content());
                    ps.setString(11, chunk.metadataJson());
                }
        );
    }

    public int countDocuments(String knowledgeBaseId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rag_source_document WHERE knowledge_base_id = ?",
                Integer.class,
                knowledgeBaseId
        );
        return count == null ? 0 : count;
    }

    public int countIndexedDocuments(String knowledgeBaseId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT document_id) FROM " + RAG_CHUNK_TABLE + " WHERE knowledge_base_id = ?",
                Integer.class,
                knowledgeBaseId
        );
        return count == null ? 0 : count;
    }

    public int countChunks(String knowledgeBaseId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + RAG_CHUNK_TABLE + " WHERE knowledge_base_id = ?",
                Integer.class,
                knowledgeBaseId
        );
        return count == null ? 0 : count;
    }

    public List<RagSyncTaskSummary> findRecentTasks(String knowledgeBaseId) {
        return jdbcTemplate.query("""
                        SELECT task_id, knowledge_base_id, task_type, status, source_document_count,
                               document_count, chunk_count, failed_document_count, message, started_at, finished_at
                        FROM rag_sync_task
                        WHERE knowledge_base_id = ?
                        ORDER BY started_at DESC
                        LIMIT ?
                        """,
                (rs, rowNum) -> mapTask(rs),
                knowledgeBaseId,
                RECENT_TASK_LIMIT
        );
    }

    public RagSyncTaskSummary findTask(String taskId) {
        List<RagSyncTaskSummary> tasks = jdbcTemplate.query("""
                        SELECT task_id, knowledge_base_id, task_type, status, source_document_count,
                               document_count, chunk_count, failed_document_count, message, started_at, finished_at
                        FROM rag_sync_task
                        WHERE task_id = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> mapTask(rs),
                taskId
        );
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    public List<RagCandidate> searchChunks(String knowledgeBaseId,
                                           List<String> keywords,
                                           String knowledgePoint,
                                           int limit) {
        List<String> safeKeywords = keywords == null ? List.of() : keywords.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toList());

        if (safeKeywords.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("""
                SELECT chunk_id, document_id, title, content, knowledge_point, source_type, source_ref
                FROM rag_chunk_store
                WHERE knowledge_base_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(knowledgeBaseId);

        if (StringUtils.hasText(knowledgePoint)) {
            sql.append(" AND knowledge_point = ? ");
            args.add(knowledgePoint.trim());
        }

        sql.append(" AND (");
        for (int i = 0; i < safeKeywords.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("(LOWER(title) LIKE ? OR LOWER(content) LIKE ?)");
            String pattern = "%" + safeKeywords.get(i) + "%";
            args.add(pattern);
            args.add(pattern);
        }
        sql.append(") ORDER BY created_at DESC LIMIT ?");
        args.add(Math.max(limit * 8, 100));

        List<RagCandidate> candidates = jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> mapCandidate(rs, safeKeywords),
                args.toArray()
        );

        return candidates.stream()
                .sorted(Comparator.comparingDouble(RagCandidate::score).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void recordSearchAudit(String knowledgeBaseId,
                                  String queryText,
                                  String knowledgePoint,
                                  List<RagCandidate> candidates) {
        jdbcTemplate.update("""
                        INSERT INTO rag_query_audit (
                            audit_id, knowledge_base_id, query_text, knowledge_point, hit_count, top_titles, created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID().toString(),
                knowledgeBaseId,
                queryText,
                emptyToNull(knowledgePoint),
                candidates == null ? 0 : candidates.size(),
                candidates == null ? null : candidates.stream()
                        .limit(5)
                        .map(RagCandidate::title)
                        .collect(Collectors.joining(" | "))
        );
    }

    public RagCandidate findChunkByVectorId(String knowledgeBaseId, String vectorId) {
        if (!StringUtils.hasText(knowledgeBaseId) || !StringUtils.hasText(vectorId)) {
            return null;
        }

        List<RagCandidate> candidates = jdbcTemplate.query("""
                        SELECT chunk_id, document_id, title, content, knowledge_point, source_type, source_ref
                        FROM rag_chunk_store
                        WHERE knowledge_base_id = ? AND vector_id = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> new RagCandidate(
                        rs.getString("chunk_id"),
                        rs.getString("document_id"),
                        rs.getString("title"),
                        trimSnippet(rs.getString("content")),
                        rs.getString("knowledge_point"),
                        rs.getString("source_type"),
                        rs.getString("source_ref"),
                        0D
                ),
                knowledgeBaseId,
                vectorId
        );
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private RagCandidate mapCandidate(ResultSet rs, List<String> keywords) throws SQLException {
        String title = rs.getString("title");
        String content = rs.getString("content");
        double score = keywordScore(title, content, rs.getString("knowledge_point"), keywords);
        return new RagCandidate(
                rs.getString("chunk_id"),
                rs.getString("document_id"),
                title,
                trimSnippet(content),
                rs.getString("knowledge_point"),
                rs.getString("source_type"),
                rs.getString("source_ref"),
                score
        );
    }

    private RagSyncTaskSummary mapTask(ResultSet rs) throws SQLException {
        return new RagSyncTaskSummary(
                rs.getString("task_id"),
                rs.getString("knowledge_base_id"),
                rs.getString("task_type"),
                rs.getString("status"),
                rs.getInt("source_document_count"),
                rs.getInt("document_count"),
                rs.getInt("chunk_count"),
                rs.getInt("failed_document_count"),
                rs.getString("message"),
                toLocalDateTime(rs.getTimestamp("started_at")),
                toLocalDateTime(rs.getTimestamp("finished_at"))
        );
    }

    private double keywordScore(String title, String content, String knowledgePoint, List<String> keywords) {
        String lowerTitle = safe(title).toLowerCase(Locale.ROOT);
        String lowerContent = safe(content).toLowerCase(Locale.ROOT);
        String lowerKnowledgePoint = safe(knowledgePoint).toLowerCase(Locale.ROOT);

        int score = 0;
        for (String keyword : keywords) {
            if (lowerTitle.contains(keyword)) {
                score += 3;
            }
            if (lowerContent.contains(keyword)) {
                score += 2;
            }
            if (lowerKnowledgePoint.contains(keyword)) {
                score += 1;
            }
        }
        return score;
    }

    private String trimSnippet(String content) {
        String normalized = safe(content)
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.length() <= 320) {
            return normalized;
        }
        return normalized.substring(0, 320) + "...";
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            return metadata.toString();
        }
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private boolean indexExists(String tableName, String indexName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                indexExists(connection, connection.getCatalog(), connection.getSchema(), tableName, indexName)
                        || indexExists(connection, connection.getCatalog(), null, tableName, indexName)
        ));
    }

    private void ensureColumn(String tableName, String columnName, String alterSql) {
        if (columnExists(tableName, columnName)) {
            return;
        }
        jdbcTemplate.execute(alterSql);
    }

    private String databaseDialect() {
        String cachedDialect = this.databaseDialect;
        if (cachedDialect != null) {
            return cachedDialect;
        }
        String resolvedDialect = jdbcTemplate.execute((ConnectionCallback<String>) connection -> {
            String productName = connection.getMetaData().getDatabaseProductName();
            if (productName == null) {
                return "";
            }
            return productName.toLowerCase(Locale.ROOT);
        });
        this.databaseDialect = resolvedDialect == null ? "" : resolvedDialect;
        return this.databaseDialect;
    }

    private boolean indexExists(Connection connection,
                                String catalog,
                                String schema,
                                String tableName,
                                String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getIndexInfo(catalog, schema, tableName, false, false)) {
            while (rs.next()) {
                String currentIndexName = rs.getString("INDEX_NAME");
                if (currentIndexName != null && indexName.equalsIgnoreCase(currentIndexName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean columnExists(String tableName, String columnName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                columnExists(connection, connection.getCatalog(), connection.getSchema(), tableName, columnName)
                        || columnExists(connection, connection.getCatalog(), null, tableName, columnName)
        ));
    }

    private boolean columnExists(Connection connection,
                                 String catalog,
                                 String schema,
                                 String tableName,
                                 String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, columnName)) {
            return rs.next();
        }
    }
}
