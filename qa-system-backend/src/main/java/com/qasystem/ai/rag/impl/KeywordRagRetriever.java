package com.qasystem.ai.rag.impl;

import com.qasystem.ai.rag.RagCandidate;
import com.qasystem.ai.rag.RagMetadataRepository;
import com.qasystem.ai.rag.RagQuery;
import com.qasystem.ai.rag.RagRetriever;
import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(20)
@RequiredArgsConstructor
public class KeywordRagRetriever implements RagRetriever {

    private final RagMetadataRepository ragMetadataRepository;
    private final RagProperties ragProperties;

    @Override
    public List<RagCandidate> retrieve(RagQuery query, int topK) {
        if (query == null || topK <= 0 || query.keywords() == null || query.keywords().isEmpty()) {
            return List.of();
        }
        return ragMetadataRepository.searchChunks(
                query.effectiveKnowledgeBaseId(ragProperties.getDefaultKnowledgeBase()),
                query.keywords(),
                query.effectiveKnowledgePointCode(),
                topK
        );
    }
}
