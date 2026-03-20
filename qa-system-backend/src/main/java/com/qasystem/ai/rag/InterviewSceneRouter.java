package com.qasystem.ai.rag;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Component
public class InterviewSceneRouter {

    private static final List<String> CODING_KEYWORDS = List.of(
            "手撕", "写代码", "实现", "编程题", "代码题", "leetcode", "复杂度",
            "数组", "链表", "二叉树", "哈希", "dp", "dfs", "bfs", "排序", "双指针"
    );
    private static final List<String> SYSTEM_DESIGN_KEYWORDS = List.of(
            "系统设计", "架构", "高并发", "高可用", "扩展性", "限流", "缓存", "mq", "消息队列",
            "一致性", "分库分表", "秒杀", "短链接", "推荐系统", "怎么设计", "如何设计"
    );
    private static final List<String> PROJECT_KEYWORDS = List.of(
            "项目", "实习", "你做过", "你负责", "难点", "亮点", "项目经历", "项目中",
            "为什么这样设计", "收益", "指标", "压测", "优化前后"
    );
    private static final List<String> DEBUGGING_KEYWORDS = List.of(
            "排查", "定位", "故障", "异常", "慢查询", "oom", "死锁", "线上问题",
            "问题分析", "怎么排", "怎么查", "为什么挂了", "内存泄漏"
    );
    private static final List<String> BEHAVIORAL_KEYWORDS = List.of(
            "自我介绍", "为什么选择", "为什么来", "职业规划", "优点", "缺点",
            "冲突", "挫折", "压力", "团队协作", "最有成就感", "最大的挑战"
    );
    private static final List<String> FOUNDATION_PATTERNS = List.of(
            "什么是", "区别", "原理", "为什么", "流程", "特点", "优缺点", "如何理解", "怎么理解"
    );

    public InterviewRoute route(String message, String requestedMessageType) {
        InterviewScene requestedScene = InterviewScene.fromCode(requestedMessageType);
        if (requestedScene != InterviewScene.GENERAL) {
            return buildRoute(requestedScene, "按照 messageType 指定的面试场景执行");
        }

        String normalized = normalize(message);
        if (!StringUtils.hasText(normalized)) {
            return buildRoute(InterviewScene.GENERAL, "未识别到有效问题，按通用问答处理");
        }

        if (containsAny(normalized, BEHAVIORAL_KEYWORDS)) {
            return buildRoute(InterviewScene.BEHAVIORAL, "命中行为面试表达，关闭知识库检索");
        }
        if (containsAny(normalized, PROJECT_KEYWORDS)) {
            return buildRoute(InterviewScene.PROJECT_DEEP_DIVE, "命中项目深挖表达，优先走 Milvus 语义检索");
        }
        if (containsAny(normalized, SYSTEM_DESIGN_KEYWORDS)) {
            return buildRoute(InterviewScene.SYSTEM_DESIGN, "命中系统设计表达，走混合检索");
        }
        if (containsAny(normalized, DEBUGGING_KEYWORDS)) {
            return buildRoute(InterviewScene.DEBUGGING, "命中故障排查表达，走混合检索");
        }
        if (containsAny(normalized, CODING_KEYWORDS)) {
            return buildRoute(InterviewScene.CODING, "命中手撕代码表达，走关键词检索");
        }
        if (containsAny(normalized, FOUNDATION_PATTERNS)) {
            return buildRoute(InterviewScene.FOUNDATION, "命中基础知识提问表达，走混合检索");
        }

        return buildRoute(InterviewScene.GENERAL, "未命中特定面试场景，按通用技术问答走混合检索");
    }

    public String buildAnswerDirective(InterviewScene scene) {
        return switch (scene) {
            case FOUNDATION -> "按基础面试回答：先给定义，再讲核心原理，再补充常见追问和易错点。";
            case CODING -> "按手撕代码回答：先确认题意，再说思路、复杂度、边界条件，最后给出 Java 实现。";
            case SYSTEM_DESIGN -> "按系统设计回答：先澄清需求，再拆模块、数据流、瓶颈、权衡和扩展性。";
            case PROJECT_DEEP_DIVE -> "按项目深挖回答：突出背景、职责、关键难点、技术取舍、结果指标和复盘。";
            case DEBUGGING -> "按故障排查回答：按现象、假设、排查步骤、根因、修复和预防顺序展开。";
            case BEHAVIORAL -> "按行为面试回答：使用 STAR 结构，强调真实行动、结果和个人反思。";
            case GENERAL -> "按技术面试回答：结构化、简洁，优先解释原理和面试官可能继续追问的点。";
        };
    }

    private InterviewRoute buildRoute(InterviewScene scene, String reason) {
        RagRetrievalMode mode = switch (scene) {
            case FOUNDATION, SYSTEM_DESIGN, DEBUGGING, GENERAL -> RagRetrievalMode.HYBRID;
            case CODING -> RagRetrievalMode.KEYWORD_ONLY;
            case PROJECT_DEEP_DIVE -> RagRetrievalMode.MILVUS_ONLY;
            case BEHAVIORAL -> RagRetrievalMode.NONE;
        };
        return new InterviewRoute(scene, mode, reason);
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
