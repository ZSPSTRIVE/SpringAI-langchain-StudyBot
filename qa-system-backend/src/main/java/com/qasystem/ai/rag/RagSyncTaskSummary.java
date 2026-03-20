package com.qasystem.ai.rag;

import java.time.LocalDateTime;

public record RagSyncTaskSummary(
        String taskId,
        String knowledgeBaseId,
        String taskType,
        String status,
        int sourceDocumentCount,
        int documentCount,
        int chunkCount,
        int failedDocumentCount,
        String message,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
