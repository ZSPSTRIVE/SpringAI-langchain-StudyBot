package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.DocSensitiveWord;
import com.qasystem.mapper.DocSensitiveWordMapper;
import com.qasystem.service.DocSensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档敏感词服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocSensitiveWordServiceImpl implements DocSensitiveWordService {

    private final DocSensitiveWordMapper docSensitiveWordMapper;

    @Override
    public IPage<DocSensitiveWord> page(Integer page, Integer size, String keyword, String category, Boolean enabled) {
        Page<DocSensitiveWord> pageParam = new Page<>(page == null || page <= 0 ? 1 : page,
                size == null || size <= 0 ? 10 : size);
        LambdaQueryWrapper<DocSensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DocSensitiveWord::getWord, keyword)
                    .or().like(DocSensitiveWord::getDescription, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(DocSensitiveWord::getCategory, category);
        }
        if (enabled != null) {
            wrapper.eq(DocSensitiveWord::getEnabled, enabled);
        }
        wrapper.orderByDesc(DocSensitiveWord::getCreatedAt);
        return docSensitiveWordMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public DocSensitiveWord create(DocSensitiveWord word) {
        LocalDateTime now = LocalDateTime.now();
        word.setId(null);
        if (word.getEnabled() == null) {
            word.setEnabled(Boolean.TRUE);
        }
        word.setCreatedAt(now);
        word.setUpdatedAt(now);
        docSensitiveWordMapper.insert(word);
        return word;
    }

    @Override
    public DocSensitiveWord update(Long id, DocSensitiveWord word) {
        DocSensitiveWord db = docSensitiveWordMapper.selectById(id);
        if (db == null) {
            throw new RuntimeException("敏感词不存在");
        }
        db.setWord(word.getWord());
        db.setCategory(word.getCategory());
        db.setLevel(word.getLevel());
        db.setEnabled(word.getEnabled());
        db.setDescription(word.getDescription());
        db.setUpdatedAt(LocalDateTime.now());
        docSensitiveWordMapper.updateById(db);
        return db;
    }

    @Override
    public void delete(Long id) {
        docSensitiveWordMapper.deleteById(id);
    }

    @Override
    public List<DocSensitiveWord> listEnabled() {
        return docSensitiveWordMapper.selectList(new LambdaQueryWrapper<DocSensitiveWord>()
                .eq(DocSensitiveWord::getEnabled, true));
    }
}
