package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.DocConfig;
import com.qasystem.entity.DocDocument;
import com.qasystem.entity.DocParagraph;
import com.qasystem.mapper.DocConfigMapper;
import com.qasystem.mapper.DocDocumentMapper;
import com.qasystem.mapper.DocParagraphMapper;
import com.qasystem.service.DocAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档查重与AI降重-管理端服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocAdminServiceImpl implements DocAdminService {

    private final DocConfigMapper docConfigMapper;
    private final DocDocumentMapper docDocumentMapper;
    private final DocParagraphMapper docParagraphMapper;

    @Override
    public Map<String, Object> getConfig() {
        List<DocConfig> configs = docConfigMapper.selectList(null);
        Map<String, Object> grouped = new HashMap<>();
        for (DocConfig cfg : configs) {
            if (cfg.getConfigKey() == null) {
                continue;
            }
            String[] parts = cfg.getConfigKey().split("\\.", 2);
            String group = parts.length > 1 ? parts[0] : "default";
            String key = parts.length > 1 ? parts[1] : cfg.getConfigKey();
            Map<String, String> groupMap = (Map<String, String>) grouped.computeIfAbsent(group, k -> new HashMap<>());
            groupMap.put(key, cfg.getConfigValue());
        }
        return grouped;
    }

    @Override
    public void saveConfig(Map<String, Object> config) {
        if (config == null) {
            return;
        }
        // 简单实现：将 group.key 展开为 configKey，覆盖写入
        for (Map.Entry<String, Object> groupEntry : config.entrySet()) {
            String group = groupEntry.getKey();
            Object value = groupEntry.getValue();
            if (!(value instanceof Map<?, ?> groupMap)) {
                continue;
            }
            for (Map.Entry<?, ?> entry : groupMap.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String fullKey = group + "." + key;
                String val = String.valueOf(entry.getValue());

                DocConfig existing = docConfigMapper.selectOne(new LambdaQueryWrapper<DocConfig>()
                        .eq(DocConfig::getConfigKey, fullKey));
                if (existing == null) {
                    DocConfig cfg = new DocConfig();
                    cfg.setConfigKey(fullKey);
                    cfg.setConfigValue(val);
                    cfg.setDescription(null);
                    docConfigMapper.insert(cfg);
                } else {
                    existing.setConfigValue(val);
                    docConfigMapper.updateById(existing);
                }
            }
        }
    }

    @Override
    public List<DocConfig> listAllConfigs() {
        return docConfigMapper.selectList(null);
    }

    @Override
    public IPage<DocDocument> pageDocuments(Integer page, Integer size, Long userId, String title, String status) {
        Page<DocDocument> pageParam = new Page<>(page == null || page <= 0 ? 1 : page,
                size == null || size <= 0 ? 10 : size);
        LambdaQueryWrapper<DocDocument> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(DocDocument::getUserId, userId);
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like(DocDocument::getTitle, title);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DocDocument::getStatus, status);
        }
        wrapper.orderByDesc(DocDocument::getCreatedAt);
        return docDocumentMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getDocumentDetail(Long documentId) {
        DocDocument doc = docDocumentMapper.selectById(documentId);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }
        List<DocParagraph> paragraphs = docParagraphMapper.selectList(
                new LambdaQueryWrapper<DocParagraph>()
                        .eq(DocParagraph::getDocumentId, documentId)
                        .orderByAsc(DocParagraph::getParagraphIndex)
        );

        double avgSimilarity = paragraphs.stream()
                .map(DocParagraph::getSimilarity)
                .filter(s -> s != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("document", doc);
        result.put("paragraphs", paragraphs);
        result.put("avgSimilarity", avgSimilarity);
        result.put("paragraphCount", paragraphs.size());
        result.put("highRiskParagraphs", paragraphs.stream()
                .filter(p -> p.getSimilarity() != null && p.getSimilarity() >= 70.0)
                .collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public void deleteDocument(Long documentId) {
        // 删除文档记录
        docDocumentMapper.deleteById(documentId);
        
        // 级联删除所有段落
        docParagraphMapper.delete(new LambdaQueryWrapper<DocParagraph>()
                .eq(DocParagraph::getDocumentId, documentId));
        
        log.info("已删除文档：documentId={}", documentId);
    }
}
