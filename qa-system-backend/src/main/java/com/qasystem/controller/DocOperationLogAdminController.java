package com.qasystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.common.response.Result;
import com.qasystem.entity.DocOperationLog;
import com.qasystem.service.DocOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 文档操作日志审计 - 管理端接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/doc/logs")
@RequiredArgsConstructor
public class DocOperationLogAdminController {

    private final DocOperationLogService docOperationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    public Result<IPage<DocOperationLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("分页查询文档操作日志: page={}, size={}, userId={}, userRole={}, operationType={}, documentId={}, startTime={}, endTime={}",
                page, size, userId, userRole, operationType, documentId, startTime, endTime);
        IPage<DocOperationLog> result = docOperationLogService.page(page, size, userId, userRole, operationType, documentId, startTime, endTime);
        return Result.success(result);
    }
}
