package com.qasystem.ai;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QuestionCategoryTest {

    @Test
    void shouldDetectComputerScienceQuestion() {
        QuestionCategory category = QuestionCategory.detect("Java 的并发编程和线程池有什么区别？");
        Assertions.assertEquals(QuestionCategory.COMPUTER_SCIENCE, category);
    }

    @Test
    void shouldDetectMathQuestion() {
        QuestionCategory category = QuestionCategory.detect("请解释一下导数和极限的关系");
        Assertions.assertEquals(QuestionCategory.ADVANCED_MATH, category);
    }

    @Test
    void shouldFallbackToGeneralWhenNoKeywordHit() {
        QuestionCategory category = QuestionCategory.detect("今天天气不错");
        Assertions.assertEquals(QuestionCategory.GENERAL, category);
    }
}
