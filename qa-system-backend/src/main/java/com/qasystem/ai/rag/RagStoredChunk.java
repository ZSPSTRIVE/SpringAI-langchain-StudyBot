package com.qasystem.ai.rag;

public record RagStoredChunk(
        String knowledgeBaseId,
        String documentId,
        String chunkId,
        String vectorId,
        String sourceType,
        String sourceRef,
        String title,
        String content,
        String knowledgePoint,
        int chunkIndex,
        String metadataJson
) {
}
