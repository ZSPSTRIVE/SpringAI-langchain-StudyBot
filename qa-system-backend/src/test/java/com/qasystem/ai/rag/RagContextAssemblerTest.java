package com.qasystem.ai.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class RagContextAssemblerTest {

    @Test
    void shouldBuildContextWithCitations() {
        RagContextAssembler assembler = new RagContextAssembler();
        RagQuery query = new RagQuery(
                "请解释线程池原理",
                List.of("线程池"),
                "intern-rag-playbook",
                null,
                InterviewKnowledgePoint.JAVA_BACKEND,
                InterviewScene.FOUNDATION,
                RagRetrievalMode.HYBRID,
                "基础面试问题"
        );
        List<RagCandidate> candidates = List.of(
                new RagCandidate(
                        "chunk-1",
                        "doc-1",
                        "Java 线程池使用方式",
                        "线程池可以复用线程并控制并发数量",
                        InterviewKnowledgePoint.JAVA_BACKEND.getCode(),
                        "seed_internet",
                        "java-thread-pool",
                        12
                ),
                new RagCandidate(
                        "chunk-2",
                        "doc-2",
                        "并发编程常见问题",
                        "并发问题主要包括可见性、有序性与原子性",
                        InterviewKnowledgePoint.OPERATING_SYSTEMS.getCode(),
                        "seed_internet",
                        "concurrency-common-issues",
                        10
                )
        );

        RagContextResult result = assembler.assemble(query, candidates, 1000);
        Assertions.assertTrue(result.hasContext());
        Assertions.assertEquals(2, result.citations().size());
        Assertions.assertEquals(InterviewScene.FOUNDATION, result.interviewScene());
        Assertions.assertTrue(result.context().contains("参考片段"));
    }

    @Test
    void shouldReturnEmptyWhenInputInvalid() {
        RagContextAssembler assembler = new RagContextAssembler();
        RagQuery query = new RagQuery(
                "请做一道手撕代码题",
                List.of("手撕", "代码"),
                "intern-rag-playbook",
                null,
                InterviewKnowledgePoint.ALGORITHMS,
                InterviewScene.CODING,
                RagRetrievalMode.KEYWORD_ONLY,
                "手撕代码"
        );
        RagContextResult result = assembler.assemble(query, List.of(), 100);
        Assertions.assertFalse(result.hasContext());
        Assertions.assertEquals(0, result.recallCount());
        Assertions.assertEquals(RagRetrievalMode.KEYWORD_ONLY, result.retrievalMode());
    }
}
