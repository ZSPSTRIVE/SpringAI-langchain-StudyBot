package com.qasystem.ai.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class RagContextAssemblerTest {

    @Test
    void shouldBuildContextWithCitations() {
        RagContextAssembler assembler = new RagContextAssembler();
        List<RagCandidate> candidates = List.of(
                new RagCandidate(1L, "Java 线程池使用方式", "线程池可以复用线程并控制并发数量", 12),
                new RagCandidate(2L, "并发编程常见问题", "并发问题主要包括可见性、有序性与原子性", 10)
        );

        RagContextResult result = assembler.assemble(candidates, 1000);
        Assertions.assertTrue(result.hasContext());
        Assertions.assertEquals(2, result.citations().size());
        Assertions.assertTrue(result.context().contains("参考片段"));
    }

    @Test
    void shouldReturnEmptyWhenInputInvalid() {
        RagContextAssembler assembler = new RagContextAssembler();
        RagContextResult result = assembler.assemble(List.of(), 100);
        Assertions.assertFalse(result.hasContext());
        Assertions.assertEquals(0, result.recallCount());
    }
}
