package com.qasystem.ai.rag.impl;

import com.qasystem.ai.rag.RagCandidate;
import com.qasystem.ai.rag.RagQuery;
import com.qasystem.ai.rag.RagRetriever;
import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Component
@RequiredArgsConstructor
public class HybridRagRetriever implements RagRetriever {

    private static final double RRF_K = 60D;

    private final MilvusRagRetriever milvusRagRetriever;
    private final KeywordRagRetriever keywordRagRetriever;
    private final RagProperties ragProperties;

    @Override
    public List<RagCandidate> retrieve(RagQuery query, int topK) {
        if (query == null || topK <= 0) {
            return List.of();
        }

        if (!query.useRag()) {
            return List.of();
        }

        if (query.retrievalMode() == com.qasystem.ai.rag.RagRetrievalMode.KEYWORD_ONLY) {
            return keywordRagRetriever.retrieve(query, Math.max(topK, ragProperties.getKeywordTopK()));
        }
        if (query.retrievalMode() == com.qasystem.ai.rag.RagRetrievalMode.MILVUS_ONLY) {
            return milvusRagRetriever.retrieve(query, Math.max(topK, ragProperties.getDenseTopK()));
        }

        List<RagCandidate> denseCandidates = milvusRagRetriever.retrieve(
                query,
                Math.max(topK, ragProperties.getDenseTopK())
        );
        List<RagCandidate> keywordCandidates = keywordRagRetriever.retrieve(
                query,
                Math.max(topK, ragProperties.getKeywordTopK())
        );

        if (denseCandidates.isEmpty() && keywordCandidates.isEmpty()) {
            return List.of();
        }

        Map<String, CandidateAccumulator> merged = new LinkedHashMap<>();
        mergeCandidates(merged, denseCandidates);
        mergeCandidates(merged, keywordCandidates);

        Set<String> queryTokens = tokenize(query.question());
        return merged.values().stream()
                .map(accumulator -> accumulator.toCandidate(queryTokens))
                .sorted(Comparator.comparingDouble(RagCandidate::score).reversed())
                .limit(topK)
                .toList();
    }

    private void mergeCandidates(Map<String, CandidateAccumulator> merged, List<RagCandidate> candidates) {
        for (int i = 0; i < candidates.size(); i++) {
            RagCandidate candidate = candidates.get(i);
            if (candidate == null) {
                continue;
            }
            String key = StringUtils.hasText(candidate.chunkId())
                    ? candidate.chunkId()
                    : candidate.documentId() + "|" + candidate.title();
            CandidateAccumulator accumulator = merged.computeIfAbsent(key, ignored -> new CandidateAccumulator(candidate));
            accumulator.merge(candidate, i);
        }
    }

    private Set<String> tokenize(String text) {
        if (!StringUtils.hasText(text)) {
            return Set.of();
        }
        return List.of(text.toLowerCase(Locale.ROOT)
                        .split("[\\s\\p{Punct}，。！？；：、“”‘’（）【】]+"))
                .stream()
                .map(String::trim)
                .filter(token -> token.length() >= 2)
                .collect(Collectors.toSet());
    }

    private static final class CandidateAccumulator {

        private RagCandidate base;
        private double fusionScore;

        private CandidateAccumulator(RagCandidate base) {
            this.base = base;
        }

        private void merge(RagCandidate candidate, int rank) {
            if (prefer(candidate, base)) {
                base = candidate;
            }
            fusionScore += 1D / (RRF_K + rank + 1D);
        }

        private RagCandidate toCandidate(Set<String> queryTokens) {
            String searchText = (safe(base.title()) + " " + safe(base.snippet())).toLowerCase(Locale.ROOT);
            long overlap = queryTokens.stream().filter(searchText::contains).count();
            double finalScore = fusionScore + (overlap * 0.01D);
            return new RagCandidate(
                    base.chunkId(),
                    base.documentId(),
                    base.title(),
                    base.snippet(),
                    base.knowledgePoint(),
                    base.sourceType(),
                    base.sourceRef(),
                    finalScore
            );
        }

        private boolean prefer(RagCandidate candidate, RagCandidate current) {
            if (current == null) {
                return true;
            }
            return candidate.score() > current.score()
                    || (StringUtils.hasText(candidate.snippet()) && !StringUtils.hasText(current.snippet()));
        }

        private String safe(String value) {
            return value == null ? "" : value;
        }
    }
}
