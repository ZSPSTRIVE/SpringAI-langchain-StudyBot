package com.qasystem.ai.rag;

import dev.langchain4j.data.segment.TextSegment;

public record RagChunkDraft(
        String knowledgeBaseId,
        String documentId,
        String chunkId,
        String sourceType,
        String sourceRef,
        String title,
        String knowledgePoint,
        int chunkIndex,
        TextSegment segment
) {
}
