package com.qasystem.ai.rag;

public record InterviewRoute(
        InterviewScene scene,
        RagRetrievalMode retrievalMode,
        String reason
) {

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
