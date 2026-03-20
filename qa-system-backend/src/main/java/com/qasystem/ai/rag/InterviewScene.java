package com.qasystem.ai.rag;

import org.springframework.util.StringUtils;

import java.util.Locale;

public enum InterviewScene {

    FOUNDATION("foundation", "基础快问快答"),
    CODING("coding", "手撕代码"),
    SYSTEM_DESIGN("system_design", "系统设计"),
    PROJECT_DEEP_DIVE("project_deep_dive", "项目深挖"),
    DEBUGGING("debugging", "故障排查"),
    BEHAVIORAL("behavioral", "行为面试"),
    GENERAL("general", "通用问答");

    private final String code;
    private final String displayName;

    InterviewScene(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static InterviewScene fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return GENERAL;
        }

        String normalized = code.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "foundation", "basic", "concept" -> FOUNDATION;
            case "coding", "code", "algorithm", "leetcode", "live_coding" -> CODING;
            case "system_design", "design", "architecture" -> SYSTEM_DESIGN;
            case "project_deep_dive", "project", "project_detail", "project_review" -> PROJECT_DEEP_DIVE;
            case "debugging", "debug", "troubleshooting" -> DEBUGGING;
            case "behavioral", "hr", "bq", "behavior" -> BEHAVIORAL;
            default -> GENERAL;
        };
    }
}
