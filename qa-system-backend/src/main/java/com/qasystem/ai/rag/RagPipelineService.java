package com.qasystem.ai.rag;

import com.qasystem.ai.QuestionCategory;
import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RAG pipeline 编排服务：query 预处理 -> 召回 -> 上下文组装。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagPipelineService {

    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "和", "是", "在", "吗", "呢", "吧", "请", "如何", "什么", "怎么",
            "the", "is", "are", "a", "an", "to", "for", "of", "in", "on", "and"
    );

    private final RagRetriever ragRetriever;
    private final RagContextAssembler ragContextAssembler;
    private final RagProperties ragProperties;

    public RagContextResult buildContext(String userQuestion) {
        if (!ragProperties.isEnabled() || !StringUtils.hasText(userQuestion)) {
            return RagContextResult.empty();
        }

        List<String> keywords = extractKeywords(userQuestion, ragProperties.getMaxKeywords());
        if (keywords.isEmpty()) {
            return RagContextResult.empty();
        }

        try {
            List<RagCandidate> candidates = ragRetriever.retrieve(userQuestion, keywords, ragProperties.getTopK());
            return ragContextAssembler.assemble(candidates, ragProperties.getMaxContextLength());
        } catch (Exception ex) {
            log.warn("RAG pipeline execute failed, fallback to no-context. reason={}", ex.getMessage());
            return RagContextResult.empty();
        }
    }

    private List<String> extractKeywords(String userQuestion, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        String lower = userQuestion.toLowerCase(Locale.ROOT);
        Set<String> keywords = new LinkedHashSet<>();

        for (QuestionCategory category : QuestionCategory.values()) {
            for (String keyword : category.getKeywords()) {
                if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                    keywords.add(keyword);
                }
            }
        }

        if (keywords.isEmpty()) {
            String[] words = userQuestion.split("[\\s\\p{Punct}，。！？；：、“”‘’（）【】]+");
            for (String word : words) {
                String cleaned = word.trim();
                if (cleaned.length() < 2 || cleaned.length() > 15) {
                    continue;
                }
                if (STOP_WORDS.contains(cleaned.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                keywords.add(cleaned);
            }
        }

        return keywords.stream().limit(limit).collect(Collectors.toList());
    }
}
