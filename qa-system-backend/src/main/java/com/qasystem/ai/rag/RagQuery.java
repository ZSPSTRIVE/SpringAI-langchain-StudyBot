package com.qasystem.ai.rag;

import org.springframework.util.StringUtils;

import java.util.List;

public record RagQuery(
        String question,
        List<String> keywords,
        String knowledgeBaseId,
        String requestedKnowledgePoint,
        InterviewKnowledgePoint detectedKnowledgePoint,
        InterviewScene interviewScene,
        RagRetrievalMode retrievalMode,
        String routeReason
) {

    public String effectiveKnowledgeBaseId(String defaultKnowledgeBaseId) {
        return StringUtils.hasText(knowledgeBaseId) ? knowledgeBaseId.trim() : defaultKnowledgeBaseId;
    }

    public String effectiveKnowledgePointCode() {
        if (StringUtils.hasText(requestedKnowledgePoint)) {
            return requestedKnowledgePoint.trim();
        }
        return detectedKnowledgePoint == null || detectedKnowledgePoint == InterviewKnowledgePoint.GENERAL
                ? null
                : detectedKnowledgePoint.getCode();
    }

    public boolean useRag() {
        return retrievalMode != RagRetrievalMode.NONE;
    }

    public boolean useMilvus() {
        return retrievalMode == RagRetrievalMode.MILVUS_ONLY || retrievalMode == RagRetrievalMode.HYBRID;
    }

    public boolean useKeyword() {
        return retrievalMode == RagRetrievalMode.KEYWORD_ONLY || retrievalMode == RagRetrievalMode.HYBRID;
    }
}
