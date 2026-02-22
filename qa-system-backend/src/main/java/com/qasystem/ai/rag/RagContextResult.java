package com.qasystem.ai.rag;

import org.springframework.util.StringUtils;

import java.util.List;

/**
 * RAG 上下文构建结果。
 */
public record RagContextResult(
        String context,
        List<String> citations,
        int recallCount
) {

    private static final RagContextResult EMPTY = new RagContextResult("", List.of(), 0);

    public static RagContextResult empty() {
        return EMPTY;
    }

    public boolean hasContext() {
        return StringUtils.hasText(context);
    }
}
