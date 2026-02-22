package com.qasystem.ai.rag;

/**
 * RAG 候选知识片段。
 */
public record RagCandidate(
        Long questionId,
        String title,
        String snippet,
        double score
) {
}
