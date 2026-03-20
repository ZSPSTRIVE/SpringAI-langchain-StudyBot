package com.qasystem.ai.rag;

import java.util.List;

public record RagSearchResult(
        String knowledgeBaseId,
        String knowledgePoint,
        List<String> keywords,
        List<RagCandidate> candidates
) {
}
