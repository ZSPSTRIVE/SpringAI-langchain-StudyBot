package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.DocOperationLog;
import com.qasystem.mapper.DocOperationLogMapper;
import com.qasystem.service.DocOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 文档操作日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocOperationLogServiceImpl implements DocOperationLogService {

    private final DocOperationLogMapper docOperationLogMapper;

    @Override
    public void log(Long userId, String userRole, String operationType, Long documentId, Long paragraphId, String detail) {
        DocOperationLog logEntity = new DocOperationLog();
        logEntity.setUserId(userId);
        logEntity.setUserRole(userRole);
        logEntity.setOperationType(operationType);
        logEntity.setDocumentId(documentId);
        logEntity.setParagraphId(paragraphId);
        logEntity.setDetail(detail);
        logEntity.setClientIp(null);
        logEntity.setUserAgent(null);
        logEntity.setCreatedAt(LocalDateTime.now());
        docOperationLogMapper.insert(logEntity);
    }

    @Override
    public IPage<DocOperationLog> page(Integer page, Integer size, Long userId, String userRole, String operationType, Long documentId, LocalDateTime startTime, LocalDateTime endTime) {
        Page<DocOperationLog> pageParam = new Page<>(page == null || page <= 0 ? 1 : page,
                size == null || size <= 0 ? 10 : size);
        LambdaQueryWrapper<DocOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(DocOperationLog::getUserId, userId);
        }
        if (userRole != null && !userRole.isEmpty()) {
            wrapper.eq(DocOperationLog::getUserRole, userRole);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(DocOperationLog::getOperationType, operationType);
        }
        if (documentId != null) {
            wrapper.eq(DocOperationLog::getDocumentId, documentId);
        }
        if (startTime != null) {
            wrapper.ge(DocOperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(DocOperationLog::getCreatedAt, endTime);
        }
        wrapper.orderByDesc(DocOperationLog::getCreatedAt);
        return docOperationLogMapper.selectPage(pageParam, wrapper);
    }
}
