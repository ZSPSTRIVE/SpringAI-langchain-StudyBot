package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.entity.DocOperationLog;

import java.time.LocalDateTime;

/**
 * 📝 文档操作日志服务
 * 
 * 📖 这是什么？
 * 这个服务就像一个"监控摄像头"，记录用户对文档的所有操作行为。
 * 就像银行的交易记录，记录了“谁、在什么时间、做了什么操作”。
 * 
 * 🎯 核心功能：
 * 1. 记录日志：保存用户的每一次操作记录
 * 2. 查询日志：支持多维度筛选和分页查询
 * 
 * 💡 为什么需要操作日志？
 * 1. 安全审计：发现问题时可以追溯谁做了什么
 * 2. 行为分析：了解用户使用习惯，优化功能设计
 * 3. 问题排查：系统出错时可以查看用户操作流程
 * 4. 数据恢复：误删除数据时可以根据日志恢复
 * 
 * 💬 记录的操作类型：
 * - 文档上传：学生上传了新文档
 * - 查重检测：系统对文档进行查重
 * - AI降重：学生使用AI降重功能
 * - 文档下载：用户下载了文档
 * - 文档删除：文档被删除
 * - 版本保存：保存了文档的新版本
 * 
 * @author 师生答疑系统开发团队
 */
public interface DocOperationLogService {

    /**
     * 📝 记录一条操作日志
     * 
     * 🎯 功能说明：
     * 向日志表中插入一条操作记录。
     * 就像监控摄像头拍下一帧画面并保存，记录下“谁在什么时间做了什么”。
     * 
     * 💡 什么时候调用？
     * 在用户执行重要操作后，立即调用这个方法记录日志：
     * - 学生上传文档成功后
     * - 文档查重完成后
     * - 学生AI降重成功后
     * - 用户下载或删除文档后
     * 
     * 💬 使用场景：
     * - 学生上传文档后，记录：“学生张三(ID:1001)于2024-01-15 10:30上传了文档123”
     * - 系统查重完成后，记录：“系统于2024-01-15 10:31对文档123完成查重，查重率18.5%”
     * - 学生使用AI降重，记录：“学生张三对段落456使用AI降重”
     * 
     * 📥 参数说明：
     * @param userId 操作用户ID（比如：1001）
     * @param userRole 用户角色（如："student"、"teacher"、"admin"、"system"）
     * @param operationType 操作类型（如："upload"、"check"、"rewrite"、"download"、"delete"）
     * @param documentId 文档ID，可为null（如果操作与文档相关）
     * @param paragraphId 段落ID，可为null（如果操作与段落相关）
     * @param detail 详细描述（如："查重率18.5%"、"降重段落5个"）
     * 
     * 📝 记录示例：
     * // 学生上传文档
     * log(1001, "student", "upload", 123L, null, "上传文档:network_design.docx");
     * 
     * // 系统查重
     * log(null, "system", "check", 123L, null, "查重完成，查重率18.5%");
     * 
     * // 学生AI降重
     * log(1001, "student", "rewrite", 123L, 456L, "AI降重段落");
     * 
     * // 管理员删除文档
     * log(2001, "admin", "delete", 123L, null, "管理员删除违规文档");
     * 
     * ⚠️ 注意事项：
     * - 日志记录应在操作成功后调用，避免记录失败的操作
     * - detail字段应简洁明了，避免存储大量文本
     * - 系统自动操作（如查重）的userRole可以设为"system"
     */
    void log(Long userId,
             String userRole,
             String operationType,
             Long documentId,
             Long paragraphId,
             String detail);

    /**
     * 🔍 分页查询操作日志
     * 
     * 🎯 功能说明：
     * 查询系统中的操作日志，支持多维度筛选和分页。
     * 就像查看监控回放，可以按人员、时间、事件类型筛选。
     * 
     * 💡 支持的筛选条件：
     * - 按用户ID：查看某个学生的所有操作
     * - 按角色：只查看学生或老师的操作
     * - 按操作类型：只查看上传或删除操作
     * - 按文档ID：查看某个文档的操作历史
     * - 按时间范围：查看某个时间段的操作
     * 
     * 💬 使用场景：
     * - 管理员查看某个学生的所有操作记录
     * - 查看今天有多少学生上传了文档
     * - 排查文档为什么被删除，查看删除日志
     * - 统计AI降重功能的使用情况
     * - 寻找异常操作（如短时间内大量上传）
     * 
     * 📥 参数说明：
     * @param page 页码，从1开始
     * @param size 每页条数
     * @param userId 用户ID，可为null（为null则不按用户筛选）
     * @param userRole 用户角色，可为null（如："student"、"teacher"）
     * @param operationType 操作类型，可为null（如："upload"、"rewrite"）
     * @param documentId 文档ID，可为null（查看某个文档的操作历史）
     * @param startTime 开始时间，可为null
     * @param endTime 结束时间，可为null
     * 
     * 📤 返回内容示例：
     * {
     *   "total": 328,                            // 总记录数
     *   "pages": 33,                             // 总页数
     *   "current": 1,                            // 当前页
     *   "size": 10,                              // 每页条数
     *   "records": [
     *     {
     *       "id": 1001,
     *       "userId": 1001,
     *       "userName": "张三",
     *       "userRole": "student",
     *       "operationType": "upload",
     *       "operationName": "上传文档",
     *       "documentId": 123,
     *       "documentTitle": "计算机网络课程设计",
     *       "paragraphId": null,
     *       "detail": "上传文档:network_design.docx",
     *       "operationTime": "2024-01-15 10:30:25",
     *       "ipAddress": "192.168.1.100"        // IP地址（如果记录了）
     *     },
     *     {
     *       "id": 1002,
     *       "userId": null,
     *       "userName": "系统",
     *       "userRole": "system",
     *       "operationType": "check",
     *       "operationName": "查重检测",
     *       "documentId": 123,
     *       "detail": "查重完成，查重率18.5%",
     *       "operationTime": "2024-01-15 10:31:30"
     *     }
     *   ]
     * }
     * 
     * ⚠️ 性能提示：
     * - 日志表数据量很大，建议设置索引提高查询速度
     * - 查询时尽量指定时间范围，避免全表扫描
     * - 历史日志可以定期归档或清理
     * 
     * @return 分页后的操作日志列表
     */
    IPage<DocOperationLog> page(Integer page,
                                Integer size,
                                Long userId,
                                String userRole,
                                String operationType,
                                Long documentId,
                                LocalDateTime startTime,
                                LocalDateTime endTime);
}
