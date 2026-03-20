package com.qasystem.ai.rag.impl;

import com.qasystem.ai.rag.InterviewKnowledgePoint;
import com.qasystem.ai.rag.RagCandidate;
import com.qasystem.ai.rag.RagMetadataRepository;
import com.qasystem.ai.rag.RagQuery;
import com.qasystem.ai.rag.RagRetriever;
import com.qasystem.ai.rag.RagVectorStoreProvider;
import com.qasystem.config.RagProperties;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class MilvusRagRetriever implements RagRetriever {

    private final RagProperties ragProperties;
    private final RagVectorStoreProvider ragVectorStoreProvider;
    private final RagMetadataRepository ragMetadataRepository;

    @Lazy
    private final EmbeddingModel ragEmbeddingModel;

    @Override
    public List<RagCandidate> retrieve(RagQuery query, int topK) {
        if (query == null || topK <= 0 || !StringUtils.hasText(query.question())) {
            return List.of();
        }

        String knowledgeBaseId = query.effectiveKnowledgeBaseId(ragProperties.getDefaultKnowledgeBase());
        try {
            Embedding queryEmbedding = ragEmbeddingModel.embed(query.question()).content();
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(topK)
                    .minScore(ragProperties.getMinVectorScore())
                    .filter(buildFilter(knowledgeBaseId, query.effectiveKnowledgePointCode()))
                    .build();

            EmbeddingSearchResult<TextSegment> result = ragVectorStoreProvider.getStore().search(request);
            if (result == null || result.matches() == null || result.matches().isEmpty()) {
                return List.of();
            }

            return result.matches().stream()
                    .map(match -> toCandidate(knowledgeBaseId, match))
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            log.warn("Dense retrieval failed, fallback to keyword only. reason={}", ex.getMessage());
            return List.of();
        }
    }

    private Filter buildFilter(String knowledgeBaseId, String knowledgePoint) {
        Filter filter = metadataKey("knowledgeBaseId").isEqualTo(knowledgeBaseId);
        if (StringUtils.hasText(knowledgePoint)) {
            filter = Filter.and(filter, metadataKey("knowledgePoint").isEqualTo(knowledgePoint.trim()));
        }
        return filter;
    }

    private RagCandidate toCandidate(String knowledgeBaseId, EmbeddingMatch<TextSegment> match) {
        if (match == null) {
            return null;
        }

        TextSegment segment = match.embedded();
        if (segment == null || !StringUtils.hasText(segment.text())) {
            return ragMetadataRepository.findChunkByVectorId(knowledgeBaseId, match.embeddingId());
        }

        Metadata metadata = segment.metadata();
        String chunkId = firstNonBlank(metadata.getString("chunkId"), match.embeddingId());
        String knowledgePoint = firstNonBlank(
                metadata.getString("knowledgePoint"),
                InterviewKnowledgePoint.GENERAL.getCode()
        );

        return new RagCandidate(
                chunkId,
                metadata.getString("documentId"),
                firstNonBlank(metadata.getString("title"), "Knowledge Segment"),
                trimSnippet(segment.text()),
                knowledgePoint,
                metadata.getString("sourceType"),
                metadata.getString("sourceRef"),
                match.score() == null ? 0D : match.score()
        );
    }

    private String trimSnippet(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= ragProperties.getSnippetLength()) {
            return normalized;
        }
        return normalized.substring(0, ragProperties.getSnippetLength()) + "...";
    }

    private String firstNonBlank(String first, String fallback) {
        return StringUtils.hasText(first) ? first.trim() : fallback;
    }
}
