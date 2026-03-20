package com.qasystem.ai.rag;

public record RagCandidate(
        String chunkId,
        String documentId,
        String title,
        String snippet,
        String knowledgePoint,
        String sourceType,
        String sourceRef,
        double score
) {
}
