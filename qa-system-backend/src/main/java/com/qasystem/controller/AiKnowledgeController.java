package com.qasystem.controller;

import com.qasystem.ai.rag.InterviewKnowledgePoint;
import com.qasystem.ai.rag.RagCandidate;
import com.qasystem.ai.rag.RagKnowledgeBaseOverview;
import com.qasystem.ai.rag.RagKnowledgeSearchService;
import com.qasystem.ai.rag.RagSearchResult;
import com.qasystem.ai.rag.RagSyncResult;
import com.qasystem.ai.rag.RagSyncTaskSummary;
import com.qasystem.ai.rag.RagIngestionService;
import com.qasystem.common.response.Result;
import com.qasystem.dto.KnowledgeOverviewResponse;
import com.qasystem.dto.KnowledgePointOptionResponse;
import com.qasystem.dto.KnowledgeSearchRequest;
import com.qasystem.dto.KnowledgeSearchResponse;
import com.qasystem.dto.KnowledgeSyncRequest;
import com.qasystem.dto.KnowledgeSyncResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai/knowledge")
@RequiredArgsConstructor
public class AiKnowledgeController {

    private final RagKnowledgeSearchService ragKnowledgeSearchService;
    private final RagIngestionService ragIngestionService;

    @PostMapping("/search")
    public Result<KnowledgeSearchResponse> search(@Valid @RequestBody KnowledgeSearchRequest request,
                                                  Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("User {} search knowledge base. query={}, kb={}", userId, request.getQuery(), request.getKnowledgeBaseId());

        RagSearchResult result = ragKnowledgeSearchService.search(
                request.getQuery(),
                request.getKnowledgeBaseId(),
                request.getKnowledgePoint(),
                request.getLimit()
        );

        return Result.success(KnowledgeSearchResponse.builder()
                .knowledgeBaseId(result.knowledgeBaseId())
                .knowledgePoint(result.knowledgePoint())
                .keywords(result.keywords())
                .hits(result.candidates().stream().map(this::toSearchHit).toList())
                .build());
    }

    @PostMapping("/sync")
    public Result<KnowledgeSyncResponse> sync(@RequestBody KnowledgeSyncRequest request,
                                              Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("User {} start knowledge sync. kb={}, sources={}", userId, request.getKnowledgeBaseId(), request.getSourceTypes());

        RagSyncResult result = ragIngestionService.syncKnowledgeBase(
                request.getKnowledgeBaseId(),
                request.getSourceTypes()
        );

        return Result.success(KnowledgeSyncResponse.builder()
                .taskId(result.taskId())
                .knowledgeBaseId(result.knowledgeBaseId())
                .sourceTypes(result.sourceTypes())
                .status(result.status())
                .sourceDocumentCount(result.sourceDocumentCount())
                .documentCount(result.documentCount())
                .chunkCount(result.chunkCount())
                .failedDocumentCount(result.failedDocumentCount())
                .message(result.message())
                .build());
    }

    @GetMapping("/overview")
    public Result<KnowledgeOverviewResponse> overview(@RequestParam(required = false) String knowledgeBaseId,
                                                      Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("User {} query knowledge overview. kb={}", userId, knowledgeBaseId);

        RagKnowledgeBaseOverview overview = ragKnowledgeSearchService.overview(knowledgeBaseId);
        return Result.success(KnowledgeOverviewResponse.builder()
                .knowledgeBaseId(overview.knowledgeBaseId())
                .sourceDocumentCount(overview.sourceDocumentCount())
                .indexedDocumentCount(overview.indexedDocumentCount())
                .chunkCount(overview.chunkCount())
                .enabledSourceTypes(overview.enabledSourceTypes())
                .recentTasks(overview.recentTasks().stream().map(this::toSyncTask).toList())
                .build());
    }

    @GetMapping("/points")
    public Result<List<KnowledgePointOptionResponse>> knowledgePoints() {
        return Result.success(Arrays.stream(InterviewKnowledgePoint.values())
                .map(point -> KnowledgePointOptionResponse.builder()
                        .code(point.getCode())
                        .displayName(point.getDisplayName())
                        .build())
                .toList());
    }

    private KnowledgeSearchResponse.SearchHit toSearchHit(RagCandidate candidate) {
        InterviewKnowledgePoint knowledgePoint = InterviewKnowledgePoint.fromCode(candidate.knowledgePoint());
        return KnowledgeSearchResponse.SearchHit.builder()
                .chunkId(candidate.chunkId())
                .documentId(candidate.documentId())
                .title(candidate.title())
                .snippet(candidate.snippet())
                .knowledgePoint(candidate.knowledgePoint())
                .knowledgePointLabel(knowledgePoint.getDisplayName())
                .sourceType(candidate.sourceType())
                .sourceRef(candidate.sourceRef())
                .score(candidate.score())
                .build();
    }

    private KnowledgeOverviewResponse.SyncTask toSyncTask(RagSyncTaskSummary task) {
        return KnowledgeOverviewResponse.SyncTask.builder()
                .taskId(task.taskId())
                .taskType(task.taskType())
                .status(task.status())
                .sourceDocumentCount(task.sourceDocumentCount())
                .documentCount(task.documentCount())
                .chunkCount(task.chunkCount())
                .failedDocumentCount(task.failedDocumentCount())
                .message(task.message())
                .startedAt(task.startedAt())
                .finishedAt(task.finishedAt())
                .build();
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        return Long.parseLong(authentication.getName());
    }
}
