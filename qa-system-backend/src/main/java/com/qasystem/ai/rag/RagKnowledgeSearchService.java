package com.qasystem.ai.rag;

import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagKnowledgeSearchService {

    private final RagProperties ragProperties;
    private final RagRetriever ragRetriever;
    private final RagQueryFactory ragQueryFactory;
    private final RagMetadataRepository ragMetadataRepository;
    private final RagIngestionService ragIngestionService;

    public RagSearchResult search(String queryText, String knowledgeBaseId, String knowledgePoint, Integer limit) {
        String resolvedKnowledgeBaseId = StringUtils.hasText(knowledgeBaseId)
                ? knowledgeBaseId.trim()
                : ragProperties.getDefaultKnowledgeBase();
        if (!ragProperties.isEnabled() || !StringUtils.hasText(queryText)) {
            return new RagSearchResult(resolvedKnowledgeBaseId, knowledgePoint, List.of(), List.of());
        }

        ragIngestionService.initializeKnowledgeBase(resolvedKnowledgeBaseId);

        RagQuery query = ragQueryFactory.build(queryText, resolvedKnowledgeBaseId, knowledgePoint);
        int safeLimit = limit == null ? ragProperties.getTopK() : Math.max(1, Math.min(limit, 20));
        List<RagCandidate> candidates = ragRetriever.retrieve(query, safeLimit);
        ragMetadataRepository.recordSearchAudit(
                query.effectiveKnowledgeBaseId(ragProperties.getDefaultKnowledgeBase()),
                query.question(),
                query.effectiveKnowledgePointCode(),
                candidates
        );

        return new RagSearchResult(
                query.effectiveKnowledgeBaseId(ragProperties.getDefaultKnowledgeBase()),
                query.effectiveKnowledgePointCode(),
                query.keywords(),
                candidates
        );
    }

    public RagKnowledgeBaseOverview overview(String knowledgeBaseId) {
        String resolvedKnowledgeBaseId = StringUtils.hasText(knowledgeBaseId)
                ? knowledgeBaseId.trim()
                : ragProperties.getDefaultKnowledgeBase();
        ragIngestionService.initializeKnowledgeBase(resolvedKnowledgeBaseId);

        return new RagKnowledgeBaseOverview(
                resolvedKnowledgeBaseId,
                ragMetadataRepository.countDocuments(resolvedKnowledgeBaseId),
                ragMetadataRepository.countIndexedDocuments(resolvedKnowledgeBaseId),
                ragMetadataRepository.countChunks(resolvedKnowledgeBaseId),
                ragIngestionService.enabledSourceTypes(),
                ragMetadataRepository.findRecentTasks(resolvedKnowledgeBaseId)
        );
    }
}
