package com.qasystem.ai.rag;

import java.util.List;

public record RagSyncResult(
        String taskId,
        String knowledgeBaseId,
        List<String> sourceTypes,
        String status,
        int sourceDocumentCount,
        int documentCount,
        int chunkCount,
        int failedDocumentCount,
        String message
) {
}
