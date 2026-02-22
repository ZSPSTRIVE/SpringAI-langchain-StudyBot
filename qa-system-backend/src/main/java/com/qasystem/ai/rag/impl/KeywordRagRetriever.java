package com.qasystem.ai.rag.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.ai.rag.RagCandidate;
import com.qasystem.ai.rag.RagRetriever;
import com.qasystem.config.RagProperties;
import com.qasystem.entity.Answer;
import com.qasystem.entity.Question;
import com.qasystem.mapper.AnswerMapper;
import com.qasystem.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 关键词召回器（MySQL LIKE 分页检索）。
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class KeywordRagRetriever implements RagRetriever {

    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final RagProperties ragProperties;

    @Override
    public List<RagCandidate> retrieve(String userQuestion, List<String> keywords, int topK) {
        if (keywords == null || keywords.isEmpty() || topK <= 0) {
            return List.of();
        }

        Map<Long, RagCandidate> candidateMap = new LinkedHashMap<>();
        int pageSize = Math.max(topK, 3);

        for (String keyword : keywords) {
            if (!StringUtils.hasText(keyword) || keyword.length() < 2) {
                continue;
            }

            IPage<Question> page = searchQuestionPage(keyword, pageSize);
            if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
                continue;
            }

            for (Question question : page.getRecords()) {
                if (question == null || question.getId() == null || candidateMap.containsKey(question.getId())) {
                    continue;
                }
                RagCandidate candidate = buildCandidate(question, keyword, userQuestion);
                if (candidate != null) {
                    candidateMap.put(question.getId(), candidate);
                }
            }
        }

        return candidateMap.values().stream()
                .sorted(Comparator.comparingDouble(RagCandidate::score).reversed())
                .limit(topK)
                .toList();
    }

    private IPage<Question> searchQuestionPage(String keyword, int pageSize) {
        if (ragProperties.isPreferFulltext()) {
            try {
                IPage<Question> fulltext = questionMapper.selectQuestionPageByFulltext(new Page<>(1, pageSize), keyword);
                if (fulltext != null && fulltext.getRecords() != null && !fulltext.getRecords().isEmpty()) {
                    return fulltext;
                }
            } catch (Exception ex) {
                log.debug("fulltext query failed, fallback LIKE. keyword={}, reason={}", keyword, ex.getMessage());
            }
        }
        return questionMapper.selectQuestionPage(new Page<>(1, pageSize), null, null, keyword);
    }

    private RagCandidate buildCandidate(Question question, String keyword, String userQuestion) {
        List<Answer> answers = answerMapper.findByQuestionId(question.getId());
        if (answers == null || answers.isEmpty()) {
            return null;
        }

        Answer bestAnswer = pickBestAnswer(answers);
        if (bestAnswer == null || !StringUtils.hasText(bestAnswer.getContent())) {
            return null;
        }

        String snippet = extractSnippet(bestAnswer.getContent(), ragProperties.getSnippetLength());
        if (!StringUtils.hasText(snippet)) {
            return null;
        }

        double score = 0D;
        if (bestAnswer.getIsAccepted() != null && bestAnswer.getIsAccepted() == 1) {
            score += 30;
        }
        if (bestAnswer.getLikeCount() != null) {
            score += Math.min(bestAnswer.getLikeCount(), 20);
        }
        if (question.getViewCount() != null) {
            score += Math.min(question.getViewCount() / 50.0, 15);
        }
        score += keywordMatchScore(question, bestAnswer, keyword);
        score += keywordMatchScore(question, bestAnswer, userQuestion);
        score += recencyScore(bestAnswer.getCreateTime());

        return new RagCandidate(
                question.getId(),
                safeText(question.getTitle()),
                snippet,
                score
        );
    }

    private Answer pickBestAnswer(List<Answer> answers) {
        List<Answer> validAnswers = new ArrayList<>();
        for (Answer answer : answers) {
            if (answer != null && StringUtils.hasText(answer.getContent())) {
                validAnswers.add(answer);
            }
        }
        if (validAnswers.isEmpty()) {
            return null;
        }
        return validAnswers.stream()
                .sorted(Comparator
                        .comparing((Answer answer) -> answer.getIsAccepted() != null && answer.getIsAccepted() == 1)
                        .reversed()
                        .thenComparing(answer -> answer.getLikeCount() == null ? 0 : answer.getLikeCount(), Comparator.reverseOrder())
                        .thenComparing(Answer::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .findFirst()
                .orElse(null);
    }

    private double keywordMatchScore(Question question, Answer answer, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return 0D;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        int hit = 0;
        if (containsIgnoreCase(question.getTitle(), lowerKeyword)) {
            hit += 3;
        }
        if (containsIgnoreCase(question.getContent(), lowerKeyword)) {
            hit += 2;
        }
        if (containsIgnoreCase(answer.getContent(), lowerKeyword)) {
            hit += 1;
        }
        return hit;
    }

    private boolean containsIgnoreCase(String source, String lowerKeyword) {
        return StringUtils.hasText(source) && source.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    private double recencyScore(LocalDateTime createTime) {
        if (createTime == null) {
            return 0D;
        }
        LocalDateTime now = LocalDateTime.now();
        if (createTime.isAfter(now.minusDays(7))) {
            return 10D;
        }
        if (createTime.isAfter(now.minusDays(30))) {
            return 5D;
        }
        return 0D;
    }

    private String extractSnippet(String content, int maxLength) {
        String clean = safeText(content)
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (!StringUtils.hasText(clean)) {
            return "";
        }
        if (maxLength <= 0 || clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, maxLength) + "...";
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
