package com.qasystem.ai.rag;

import java.util.List;

public record RagKnowledgeBaseOverview(
        String knowledgeBaseId,
        int sourceDocumentCount,
        int indexedDocumentCount,
        int chunkCount,
        List<String> enabledSourceTypes,
        List<RagSyncTaskSummary> recentTasks
) {
}
