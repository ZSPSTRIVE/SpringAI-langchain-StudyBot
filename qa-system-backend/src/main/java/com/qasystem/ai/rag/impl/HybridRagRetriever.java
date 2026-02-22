package com.qasystem.ai.rag.impl;

import com.qasystem.ai.rag.RagCandidate;
import com.qasystem.ai.rag.RagRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 混合召回器：先关键词召回，再做轻量规则重排。
 */
@Primary
@Component
@RequiredArgsConstructor
public class HybridRagRetriever implements RagRetriever {

    private final KeywordRagRetriever keywordRagRetriever;

    @Override
    public List<RagCandidate> retrieve(String userQuestion, List<String> keywords, int topK) {
        List<RagCandidate> baseCandidates = keywordRagRetriever.retrieve(userQuestion, keywords, Math.max(topK * 2, topK));
        if (baseCandidates.isEmpty()) {
            return baseCandidates;
        }

        Set<String> queryTokens = tokenize(userQuestion);
        return baseCandidates.stream()
                .map(candidate -> new RagCandidate(
                        candidate.questionId(),
                        candidate.title(),
                        candidate.snippet(),
                        candidate.score() + overlapBoost(candidate, queryTokens)
                ))
                .sorted(Comparator.comparingDouble(RagCandidate::score).reversed())
                .limit(topK)
                .toList();
    }

    private double overlapBoost(RagCandidate candidate, Set<String> queryTokens) {
        if (queryTokens.isEmpty()) {
            return 0D;
        }
        String source = (safe(candidate.title()) + " " + safe(candidate.snippet())).toLowerCase(Locale.ROOT);
        long overlap = queryTokens.stream().filter(source::contains).count();
        return overlap * 2D;
    }

    private Set<String> tokenize(String text) {
        if (!StringUtils.hasText(text)) {
            return Set.of();
        }
        return List.of(text.toLowerCase(Locale.ROOT)
                        .split("[\\s\\p{Punct}，。！？；：、“”‘’（）【】]+"))
                .stream()
                .filter(token -> token.length() >= 2)
                .collect(Collectors.toSet());
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}
