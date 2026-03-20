package com.qasystem.ai.rag;

import com.qasystem.config.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagStartupRunner implements ApplicationRunner {

    private final RagProperties ragProperties;
    private final RagIngestionService ragIngestionService;

    @Override
    public void run(ApplicationArguments args) {
        if (!ragProperties.isEnabled()) {
            return;
        }

        try {
            if (ragProperties.isBootstrapSchemaOnStartup()) {
                ragIngestionService.initializeKnowledgeBase(ragProperties.getDefaultKnowledgeBase());
            }

            if (ragProperties.isFullSyncOnStartup()) {
                ragIngestionService.fullSyncDefaultKnowledgeBase();
            } else if (ragProperties.isSeedOnStartup()) {
                ragIngestionService.seedDefaultKnowledgeBase();
            }
        } catch (Exception ex) {
            log.warn("RAG bootstrap skipped because initialization failed. reason={}", ex.getMessage());
        }
    }
}
