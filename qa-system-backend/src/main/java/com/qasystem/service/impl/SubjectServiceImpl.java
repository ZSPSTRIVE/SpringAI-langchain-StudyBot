package com.qasystem.service.impl;

import com.qasystem.common.util.RedisUtil;
import com.qasystem.dto.SubjectDTO;
import com.qasystem.entity.Subject;
import com.qasystem.mapper.SubjectMapper;
import com.qasystem.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 科目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final RedisUtil redisUtil;

    private static final String SUBJECT_LIST_CACHE_KEY = "subjects:list";
    private static final String SUBJECT_CACHE_KEY = "subject:";
    private static final long CACHE_EXPIRE_DAYS = 7;

    @Override
    public List<SubjectDTO> getAllActiveSubjects() {
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<SubjectDTO> cachedList = redisUtil.get(SUBJECT_LIST_CACHE_KEY, List.class);
        if (cachedList != null && !cachedList.isEmpty()) {
            log.debug("从缓存获取科目列表");
            return cachedList;
        }

        // 查询数据库
        List<Subject> subjects = subjectMapper.findAllActive();
        List<SubjectDTO> dtoList = subjects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 缓存结果
        if (!dtoList.isEmpty()) {
            redisUtil.set(SUBJECT_LIST_CACHE_KEY, dtoList, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        }

        return dtoList;
    }

    @Override
    public SubjectDTO getSubjectById(Long id) {
        // 先从缓存获取
        String cacheKey = SUBJECT_CACHE_KEY + id;
        SubjectDTO cachedDTO = redisUtil.get(cacheKey, SubjectDTO.class);
        if (cachedDTO != null) {
            return cachedDTO;
        }

        // 查询数据库
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new RuntimeException("科目不存在");
        }

        SubjectDTO dto = convertToDTO(subject);
        
        // 缓存结果
        redisUtil.set(cacheKey, dto, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);

        return dto;
    }

    @Override
    @Transactional
    public SubjectDTO createSubject(Subject subject) {
        subject.setStatus("ACTIVE");
        subjectMapper.insert(subject);
        
        // 清除列表缓存
        redisUtil.delete(SUBJECT_LIST_CACHE_KEY);
        
        log.info("创建科目成功: id={}, name={}", subject.getId(), subject.getName());
        return convertToDTO(subject);
    }

    @Override
    @Transactional
    public SubjectDTO updateSubject(Long id, Subject subject) {
        Subject existSubject = subjectMapper.selectById(id);
        if (existSubject == null) {
            throw new RuntimeException("科目不存在");
        }

        subject.setId(id);
        subjectMapper.updateById(subject);
        
        // 清除缓存
        redisUtil.delete(SUBJECT_LIST_CACHE_KEY);
        redisUtil.delete(SUBJECT_CACHE_KEY + id);
        
        log.info("更新科目成功: id={}", id);
        return convertToDTO(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        subjectMapper.deleteById(id);
        
        // 清除缓存
        redisUtil.delete(SUBJECT_LIST_CACHE_KEY);
        redisUtil.delete(SUBJECT_CACHE_KEY + id);
        
        log.info("删除科目成功: id={}", id);
    }

    private SubjectDTO convertToDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        BeanUtils.copyProperties(subject, dto);
        return dto;
    }
}

