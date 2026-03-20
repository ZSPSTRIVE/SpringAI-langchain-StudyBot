package com.qasystem.ai.rag.source;

import com.qasystem.ai.rag.RagIndexDocument;

import java.util.List;

public interface RagSourceProvider {

    String sourceType();

    boolean enabled();

    List<RagIndexDocument> load(String knowledgeBaseId);
}
