package com.qasystem.ai.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InterviewSceneRouterTest {

    private final InterviewSceneRouter router = new InterviewSceneRouter();

    @Test
    void shouldRouteCodingQuestionsToKeywordSearch() {
        InterviewRoute route = router.route("请你现场手撕一道链表反转，顺便说下时间复杂度", null);

        Assertions.assertEquals(InterviewScene.CODING, route.scene());
        Assertions.assertEquals(RagRetrievalMode.KEYWORD_ONLY, route.retrievalMode());
        Assertions.assertTrue(route.useKeyword());
        Assertions.assertFalse(route.useMilvus());
    }

    @Test
    void shouldRouteProjectQuestionsToMilvus() {
        InterviewRoute route = router.route("讲讲你实习项目里最难的优化点，为什么这么设计", null);

        Assertions.assertEquals(InterviewScene.PROJECT_DEEP_DIVE, route.scene());
        Assertions.assertEquals(RagRetrievalMode.MILVUS_ONLY, route.retrievalMode());
        Assertions.assertTrue(route.useMilvus());
    }

    @Test
    void shouldAllowMessageTypeOverride() {
        InterviewRoute route = router.route("随便问一句", "behavioral");

        Assertions.assertEquals(InterviewScene.BEHAVIORAL, route.scene());
        Assertions.assertEquals(RagRetrievalMode.NONE, route.retrievalMode());
    }
}
