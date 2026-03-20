package com.qasystem.ai.rag;

import org.springframework.util.StringUtils;

import java.util.List;

/**
 * RAG 上下文构建结果。
 */
public record RagContextResult(
        String context,
        List<String> citations,
        int recallCount,
        InterviewScene interviewScene,
        RagRetrievalMode retrievalMode,
        String routeReason
) {

    private static final RagContextResult EMPTY = new RagContextResult(
            "",
            List.of(),
            0,
            InterviewScene.GENERAL,
            RagRetrievalMode.NONE,
            "no question"
    );

    public static RagContextResult empty() {
        return EMPTY;
    }

    public static RagContextResult empty(RagQuery query) {
        return new RagContextResult(
                "",
                List.of(),
                0,
                query == null || query.interviewScene() == null ? InterviewScene.GENERAL : query.interviewScene(),
                query == null || query.retrievalMode() == null ? RagRetrievalMode.NONE : query.retrievalMode(),
                query == null ? "no question" : query.routeReason()
        );
    }

    public boolean hasContext() {
        return StringUtils.hasText(context);
    }

    public String sceneCode() {
        return interviewScene == null ? InterviewScene.GENERAL.getCode() : interviewScene.getCode();
    }

    public String sceneLabel() {
        return interviewScene == null ? InterviewScene.GENERAL.getDisplayName() : interviewScene.getDisplayName();
    }
}
