package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.DocOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * DocOperationLogMapper接口 - 文档操作日志数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的doc_operation_log表，就像一个"操作记录员"。
 * 记录着用户对文档的所有操作，就像监控摄像头记录一样。
 * 
 * 📚 日志的作用：
 * 1. 安全审计：记录谁在什么时间做了什么操作
 * 2. 问题排查：出现问题时可以回溯操作历史
 * 3. 用户行为分析：统计用户使用情况
 * 4. 数据恢复：记录删除操作，必要时可恢复
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<DocOperationLog>
 * - 只使用BaseMapper提供的基础方法
 * - 每次操作都会插入一条日志
 * 
 * 📊 对应数据库表: doc_operation_log
 * 
 * 📝 表结构说明（主要字段）：
 * - id: 日志ID
 * - user_id: 操作用户ID
 * - document_id: 操作的文档ID
 * - operation_type: 操作类型（UPLOAD/CHECK/REWRITE/DELETE/DOWNLOAD）
 * - operation_desc: 操作描述
 * - ip_address: 操作者IP地址
 * - user_agent: 浏览器信息
 * - status: 操作结果（SUCCESS/FAILED）
 * - error_message: 错误信息（如果失败）
 * - create_time: 操作时间
 * 
 * 🔗 关联关系：
 * - 关联user表：log.user_id = user.id
 * - 关联doc_document表：log.document_id = document.id
 * 
 * 💡 记录的操作类型：
 * - UPLOAD: 上传文档
 * - CHECK: 查重检测
 * - REWRITE: AI降重
 * - DELETE: 删除文档
 * - DOWNLOAD: 下载文档或报告
 * - VIEW: 查看文档详情
 * 
 * 📝 使用示例1 - 记录上传操作：
 * <pre>
 * // 用户上传文档后记录日志
 * DocOperationLog log = new DocOperationLog();
 * log.setUserId(currentUserId);
 * log.setDocumentId(documentId);
 * log.setOperationType("UPLOAD");
 * log.setOperationDesc("上传文档: " + fileName);
 * log.setIpAddress(request.getRemoteAddr());
 * log.setUserAgent(request.getHeader("User-Agent"));
 * log.setStatus("SUCCESS");
 * docOperationLogMapper.insert(log);
 * </pre>
 * 
 * 📝 使用示例2 - 记录失败操作：
 * <pre>
 * // 查重失败时记录错误
 * try {
 *     // 执行查重...
 * } catch (Exception e) {
 *     DocOperationLog log = new DocOperationLog();
 *     log.setUserId(currentUserId);
 *     log.setDocumentId(documentId);
 *     log.setOperationType("CHECK");
 *     log.setOperationDesc("查重检测");
 *     log.setStatus("FAILED");
 *     log.setErrorMessage(e.getMessage());
 *     docOperationLogMapper.insert(log);
 * }
 * </pre>
 * 
 * 📝 使用示例3 - 查询用户操作历史：
 * <pre>
 * // 查询某个用户的所有操作记录
 * List<DocOperationLog> logs = docOperationLogMapper.selectList(
 *     new LambdaQueryWrapper<DocOperationLog>()
 *         .eq(DocOperationLog::getUserId, userId)
 *         .orderByDesc(DocOperationLog::getCreateTime)
 *         .last("LIMIT 100")  // 最近100条
 * );
 * 
 * // 显示操作历史
 * for (DocOperationLog log : logs) {
 *     System.out.println(log.getCreateTime() + " - " + log.getOperationType() + ": " + log.getOperationDesc());
 * }
 * </pre>
 * 
 * 📝 使用示例4 - 统计操作数据：
 * <pre>
 * // 统计今天的查重次数
 * Long checkCount = docOperationLogMapper.selectCount(
 *     new LambdaQueryWrapper<DocOperationLog>()
 *         .eq(DocOperationLog::getOperationType, "CHECK")
 *         .ge(DocOperationLog::getCreateTime, LocalDate.now())  // 今天
 * );
 * System.out.println("今天共进行了" + checkCount + "次查重");
 * </pre>
 * 
 * ⚠️ 重要提示：
 * 1. 日志表会快速增长，建议定期清理旧数据（如保留近半年）
 * 2. 敏感操作（删除、下载）必须记录日志
 * 3. IP地址和User-Agent可用于安全分析
 * 4. 失败操作应该记录error_message，方便排查问题
 * 5. 建议在user_id、document_id、create_time上创建索引
 * 6. 可以异步记录日志，不影响主业务性能
 * 7. 日志可以用于生成用户行为报表
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface DocOperationLogMapper extends BaseMapper<DocOperationLog> {
    // 只使用BaseMapper提供的基础方法
    // 这是文档操作日志表，记录所有用户操作
}
