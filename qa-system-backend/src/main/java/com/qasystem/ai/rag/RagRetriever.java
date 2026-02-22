package com.qasystem.ai.rag;

import java.util.List;

/**
 * RAG 召回器接口。
 */
public interface RagRetriever {

    /**
     * 根据用户问题和关键词召回候选知识。
     */
    List<RagCandidate> retrieve(String userQuestion, List<String> keywords, int topK);
}
