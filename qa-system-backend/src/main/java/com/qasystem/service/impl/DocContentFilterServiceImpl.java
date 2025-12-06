package com.qasystem.service.impl;

import com.qasystem.entity.DocSensitiveWord;
import com.qasystem.service.DocContentFilterService;
import com.qasystem.service.DocSensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 简单敏感词扫描实现：基于包含匹配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocContentFilterServiceImpl implements DocContentFilterService {

    private final DocSensitiveWordService docSensitiveWordService;

    @Override
    public List<String> findSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        String content = text.toLowerCase();
        List<DocSensitiveWord> words = docSensitiveWordService.listEnabled();
        Set<String> hits = new HashSet<>();
        for (DocSensitiveWord w : words) {
            if (w.getWord() == null || w.getWord().isEmpty()) {
                continue;
            }
            String target = w.getWord().toLowerCase();
            if (content.contains(target)) {
                hits.add(w.getWord());
            }
        }
        return List.copyOf(hits);
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        return !findSensitiveWords(text).isEmpty();
    }
}
