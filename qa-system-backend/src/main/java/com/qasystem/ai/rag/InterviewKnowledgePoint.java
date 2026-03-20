package com.qasystem.ai.rag;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public enum InterviewKnowledgePoint {

    DATA_STRUCTURES(
            "data_structures",
            "Data Structures",
            List.of("数据结构", "data structure", "linked list", "链表", "stack", "栈", "queue", "队列", "tree", "树", "graph", "图", "hash")
    ),
    ALGORITHMS(
            "algorithms",
            "Algorithms",
            List.of("算法", "algorithm", "排序", "sort", "搜索", "search", "动态规划", "dp", "greedy", "贪心", "backtracking", "回溯")
    ),
    COMPUTER_NETWORKS(
            "computer_networks",
            "Computer Networks",
            List.of("计算机网络", "network", "tcp", "udp", "http", "https", "dns", "路由", "交换机", "osi")
    ),
    OPERATING_SYSTEMS(
            "operating_systems",
            "Operating Systems",
            List.of("操作系统", "os", "进程", "线程", "process", "thread", "memory", "调度", "死锁", "文件系统")
    ),
    DATABASES(
            "databases",
            "Databases",
            List.of("数据库", "database", "sql", "mysql", "postgresql", "索引", "事务", "acid", "join", "锁")
    ),
    COMPUTER_ARCHITECTURE(
            "computer_architecture",
            "Computer Architecture",
            List.of("计算机组成", "architecture", "cpu", "cache", "指令集", "pipeline", "存储器", "总线")
    ),
    SIGNALS_AND_SYSTEMS(
            "signals_and_systems",
            "Signals and Systems",
            List.of("信号与系统", "signal", "system", "卷积", "傅里叶", "拉普拉斯", "z变换", "采样", "线性时不变")
    ),
    DIGITAL_SIGNAL_PROCESSING(
            "digital_signal_processing",
            "Digital Signal Processing",
            List.of("数字信号处理", "dsp", "filter", "滤波器", "fft", "频谱", "采样率", "quantization", "量化")
    ),
    JAVA_BACKEND(
            "java_backend",
            "Java Backend",
            List.of("java", "spring", "spring boot", "jvm", "并发", "concurrent", "redis", "mq", "微服务")
    ),
    GENERAL(
            "general",
            "General",
            List.of()
    );

    private final String code;
    private final String displayName;
    private final List<String> keywords;

    InterviewKnowledgePoint(String code, String displayName, List<String> keywords) {
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

    public static InterviewKnowledgePoint detect(String text) {
        if (!StringUtils.hasText(text)) {
            return GENERAL;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(point -> point != GENERAL)
                .max(Comparator.comparingInt(point -> point.score(normalized)))
                .filter(point -> point.score(normalized) > 0)
                .orElse(GENERAL);
    }

    public static InterviewKnowledgePoint fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return GENERAL;
        }
        return Arrays.stream(values())
                .filter(point -> point.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(GENERAL);
    }

    private int score(String normalized) {
        int score = 0;
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                score++;
            }
        }
        return score;
    }
}
