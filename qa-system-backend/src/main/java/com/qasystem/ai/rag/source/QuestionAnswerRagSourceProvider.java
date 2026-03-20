package com.qasystem.ai.rag.source;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.ai.rag.InterviewKnowledgePoint;
import com.qasystem.ai.rag.RagIndexDocument;
import com.qasystem.config.RagProperties;
import com.qasystem.entity.Answer;
import com.qasystem.entity.Question;
import com.qasystem.mapper.AnswerMapper;
import com.qasystem.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionAnswerRagSourceProvider implements RagSourceProvider {

    private final RagProperties ragProperties;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;

    @Override
    public String sourceType() {
        return "question_answer";
    }

    @Override
    public boolean enabled() {
        return ragProperties.getSources().isQuestionAnswerEnabled();
    }

    @Override
    public List<RagIndexDocument> load(String knowledgeBaseId) {
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .orderByDesc(Question::getViewCount)
                .orderByDesc(Question::getCreateTime)
                .last("LIMIT " + ragProperties.getSources().getQuestionLimit()));

        List<RagIndexDocument> documents = new ArrayList<>();
        for (Question question : questions) {
            List<Answer> answers = answerMapper.findByQuestionId(question.getId());
            if (answers == null || answers.isEmpty()) {
                continue;
            }

            Answer answer = pickBestAnswer(answers);
            if (answer == null || !StringUtils.hasText(answer.getContent())) {
                continue;
            }

            String content = buildContent(question, answer);
            String knowledgePoint = InterviewKnowledgePoint.detect(
                    question.getTitle() + " " + question.getContent() + " " + answer.getContent()
            ).getCode();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("questionId", question.getId());
            metadata.put("answerId", answer.getId());
            metadata.put("viewCount", question.getViewCount());
            metadata.put("accepted", answer.getIsAccepted());

            documents.add(new RagIndexDocument(
                    knowledgeBaseId,
                    "qa:" + question.getId(),
                    sourceType(),
                    String.valueOf(question.getId()),
                    question.getTitle(),
                    content,
                    knowledgePoint,
                    null,
                    metadata
            ));
        }
        log.info("Loaded {} question-answer seed documents for RAG", documents.size());
        return documents;
    }

    private Answer pickBestAnswer(List<Answer> answers) {
        return answers.stream()
                .filter(answer -> StringUtils.hasText(answer.getContent()))
                .findFirst()
                .orElse(null);
    }

    private String buildContent(Question question, Answer answer) {
        StringBuilder builder = new StringBuilder();
        builder.append("问题标题: ").append(safe(question.getTitle())).append('\n');
        if (StringUtils.hasText(question.getContent())) {
            builder.append("问题描述: ").append(question.getContent().trim()).append('\n');
        }
        builder.append("参考答案: ").append(answer.getContent().trim());
        return builder.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
