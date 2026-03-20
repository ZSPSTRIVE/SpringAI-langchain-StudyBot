package com.qasystem.ai.rag;

import java.util.Map;

public record RagIndexDocument(
        String knowledgeBaseId,
        String documentId,
        String sourceType,
        String sourceRef,
        String title,
        String content,
        String knowledgePoint,
        String sourceUri,
        Map<String, Object> metadata
) {
}
