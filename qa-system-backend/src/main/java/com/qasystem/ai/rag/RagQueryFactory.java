package com.qasystem.ai.rag;

import com.qasystem.ai.QuestionCategory;
import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RagQueryFactory {

    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "和", "是", "在", "吗", "呢", "啊", "请", "如何", "什么", "怎么",
            "the", "is", "are", "a", "an", "to", "for", "of", "in", "on", "and"
    );

    private final RagProperties ragProperties;
    private final InterviewSceneRouter interviewSceneRouter;

    public RagQuery build(String userQuestion, String knowledgeBaseId, String requestedKnowledgePoint) {
        return build(userQuestion, knowledgeBaseId, requestedKnowledgePoint, null);
    }

    public RagQuery build(String userQuestion,
                          String knowledgeBaseId,
                          String requestedKnowledgePoint,
                          String requestedMessageType) {
        if (!StringUtils.hasText(userQuestion)) {
            return new RagQuery(
                    "",
                    List.of(),
                    knowledgeBaseId,
                    requestedKnowledgePoint,
                    InterviewKnowledgePoint.GENERAL,
                    InterviewScene.GENERAL,
                    RagRetrievalMode.NONE,
                    "empty question"
            );
        }

        String normalizedQuestion = userQuestion.trim();
        List<String> keywords = extractKeywords(normalizedQuestion, ragProperties.getMaxKeywords());
        InterviewKnowledgePoint detectedKnowledgePoint = InterviewKnowledgePoint.detect(normalizedQuestion);
        InterviewRoute route = interviewSceneRouter.route(normalizedQuestion, requestedMessageType);

        return new RagQuery(
                normalizedQuestion,
                keywords,
                knowledgeBaseId,
                requestedKnowledgePoint,
                detectedKnowledgePoint,
                route.scene(),
                route.retrievalMode(),
                route.reason()
        );
    }

    List<String> extractKeywords(String userQuestion, int limit) {
        if (limit <= 0 || !StringUtils.hasText(userQuestion)) {
            return List.of();
        }

        String lower = userQuestion.toLowerCase(Locale.ROOT);
        Set<String> keywords = new LinkedHashSet<>();

        for (InterviewKnowledgePoint knowledgePoint : InterviewKnowledgePoint.values()) {
            for (String keyword : knowledgePoint.getKeywords()) {
                if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                    keywords.add(keyword);
                }
            }
        }

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
                if (cleaned.length() < 2 || cleaned.length() > 24) {
                    continue;
                }
                if (STOP_WORDS.contains(cleaned.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                keywords.add(cleaned);
            }
        }

        return keywords.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
