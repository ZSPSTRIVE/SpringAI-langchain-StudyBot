package com.qasystem.ai;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 问题分类统一枚举，避免业务逻辑中出现字符串漂移。
 */
public enum QuestionCategory {

    COMPUTER_SCIENCE(
            "computer_science",
            "计算机科学",
            List.of("java", "python", "c++", "javascript", "sql", "编程", "代码", "算法", "数据结构", "数据库", "网络")
    ),
    ADVANCED_MATH(
            "advanced_math",
            "高等数学",
            List.of("微积分", "导数", "积分", "极限", "级数", "微分方程")
    ),
    LINEAR_ALGEBRA(
            "linear_algebra",
            "线性代数",
            List.of("矩阵", "行列式", "向量", "线性", "特征值", "特征向量")
    ),
    PROBABILITY_STATS(
            "probability_stats",
            "概率统计",
            List.of("概率", "统计", "随机", "分布", "期望", "方差")
    ),
    COLLEGE_PHYSICS(
            "college_physics",
            "大学物理",
            List.of("物理", "力学", "电磁", "热学", "光学", "量子")
    ),
    COLLEGE_ENGLISH(
            "college_english",
            "大学英语",
            List.of("英语", "english", "语法", "翻译", "四级", "六级")
    ),
    ACADEMIC_ADVICE(
            "academic_advice",
            "学业咨询",
            List.of("课程", "学分", "考试", "选课", "成绩", "挂科")
    ),
    GENERAL(
            "general",
            "一般咨询",
            List.of()
    );

    private final String code;
    private final String displayName;
    private final List<String> keywords;

    QuestionCategory(String code, String displayName, List<String> keywords) {
        this.code = code;
        this.displayName = displayName;
        this.keywords = keywords;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public static QuestionCategory detect(String message) {
        if (message == null || message.isBlank()) {
            return GENERAL;
        }
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(category -> category != GENERAL)
                .max(Comparator.comparingInt(category -> hitScore(category, lowerMessage)))
                .filter(category -> hitScore(category, lowerMessage) > 0)
                .orElse(GENERAL);
    }

    public static QuestionCategory fromCode(String code) {
        if (code == null || code.isBlank()) {
            return GENERAL;
        }
        return Arrays.stream(values())
                .filter(category -> Objects.equals(category.code, code))
                .findFirst()
                .orElse(GENERAL);
    }

    private static int hitScore(QuestionCategory category, String lowerMessage) {
        int score = 0;
        for (String keyword : category.keywords) {
            if (lowerMessage.contains(keyword.toLowerCase(Locale.ROOT))) {
                score++;
            }
        }
        return score;
    }
}
