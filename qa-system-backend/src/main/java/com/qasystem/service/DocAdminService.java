package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.entity.DocConfig;
import com.qasystem.entity.DocDocument;

import java.util.List;
import java.util.Map;

/**
 * 🛠️ 文档查重与AI降重-管理端服务接口
 * 
 * 📖 这是什么？
 * 这个服务就像一个"论文查重系统的后台管理中心"，专为管理员和老师设计。
 * 就像学校的教务管理系统，可以：
 * - 设置查重规则（比如查重率不能超过30%）
 * - 查看所有学生提交的文档
 * - 删除违规文档
 * - 监控系统使用情况
 * 
 * 🎯 核心功能：
 * 1. 配置管理：设置查重率阈值、文档大小限制、AI降重次数限制等
 * 2. 文档监管：查看所有学生提交的文档，包括查重报告
 * 3. 文档删除：删除不合规或过期的文档
 * 4. 数据分析：查看文档的查重统计、降重统计等
 * 
 * 💡 使用场景：
 * - 老师要查看学生提交的毕业论文查重报告
 * - 管理员需要调整查重系统的参数（比如放宽查重率限制）
 * - 清理过期的文档释放存储空间
 * - 查看某个学生的文档提交历史
 * 
 * @author 师生答疑系统开发团队
 */
public interface DocAdminService {

    /**
     * ⚙️ 获取文档系统配置（分组结构）
     * 
     * 🎯 功能说明：
     * 获取文档查重与AI降重系统的所有配置参数，并按照功能分组返回。
     * 就像查看学校的考试规则，分为"考试时间规定""考场纪律""成绩评定标准"等版块。
     * 
     * 💡 为什么需要分组？
     * 原始数据库中配置项是一条一条存储的，但页面展示时需要按类别分组：
     * - 查重配置：查重率阈值、引用文献阈值等
     * - 文档限制：文件大小、格式、数量限制等
     * - AI降重配置：AI调用次数、超时时间等
     * 这样前端展示更清晰，管理员修改也更方便。
     * 
     * 💬 使用场景：
     * - 管理员进入文档系统设置页面
     * - 查看当前的查重率阈值是多少
     * - 了解学生每天可以使用几次AI降重
     * 
     * 📤 返回内容示例：
     * {
     *   "plagiarismConfig": {                    // 查重配置分组
     *     "maxSimilarity": 30,                   // 最大查重率（%）
     *     "citationThreshold": 10,               // 引用文献阈值（%）
     *     "minParagraphLength": 100              // 最小段落字数
     *   },
     *   "documentLimits": {                      // 文档限制分组
     *     "maxFileSize": 10,                     // 最大文件大小（MB）
     *     "allowedFormats": ["docx", "pdf", "txt"],
     *     "maxDocumentsPerDay": 5                // 每天最多提交数
     *   },
     *   "aiRewriteConfig": {                     // AI降重配置分组
     *     "maxRewritesPerDay": 10,               // 每天最多降重次数
     *     "maxParagraphsPerRewrite": 5,          // 每次最多降重段落数
     *     "timeout": 30000                       // 超时时间（毫秒）
     *   }
     * }
     * 
     * @return 按功能分组的配置对象
     */
    Map<String, Object> getConfig();

    /**
     * 💾 保存文档系统配置
     * 
     * 🎯 功能说明：
     * 保存管理员修改的配置参数，立即生效。
     * 就像修改学校的考试规则，修改后所有学生都要遵守新规则。
     * 
     * 💡 内部逻辑：
     * 1. 解析前端发送的分组配置
     * 2. 将每个配置项保存到数据库
     * 3. 清除配置缓存，让新配置立即生效
     * 4. 记录配置修改日志
     * 
     * 💬 使用场景：
     * - 学期末，管理员把查重率阈值从30%调整为25%（更严格）
     * - 服务器空间紧张，限制每个文档最大为5MB
     * - 课程结束，增加学生每天AI降重的次数
     * 
     * 📥 参数示例：
     * {
     *   "plagiarismConfig": {
     *     "maxSimilarity": 25,                   // 把查重率阈值改为25%
     *     "citationThreshold": 10
     *   },
     *   "documentLimits": {
     *     "maxFileSize": 5,                      // 把文件大小限制改为5MB
     *     "maxDocumentsPerDay": 3                // 每天最多3篇
     *   }
     * }
     * 
     * ⚠️ 注意事项：
     * - 配置修改立即生效，影响所有用户
     * - 不要把限制设置得太低，否则学生无法正常使用
     * - 重要配置修改应提前通知用户
     * 
     * @param config 要保存的配置对象（分组结构）
     */
    void saveConfig(Map<String, Object> config);

    /**
     * 📋 获取原始配置列表
     * 
     * 🎯 功能说明：
     * 获取所有配置项的原始数据，不分组。
     * 就像查看所有规则条文的列表，每一条单独显示。
     * 
     * 💡 与getConfig()的区别：
     * - getConfig()：返回的是按功能分组的配置，适合页面展示
     * - listAllConfigs()：返回的是原始数据列表，适合数据导出或开发调试
     * 
     * 💬 使用场景：
     * - 开发者调试时需要查看所有配置的具体值
     * - 导出配置数据做备份
     * - 系统迁移时需要复制配置
     * 
     * 📤 返回内容示例：
     * [
     *   {
     *     "id": 1,
     *     "configKey": "plagiarism.maxSimilarity",    // 配置项键名
     *     "configValue": "30",                        // 配置值
     *     "description": "最大查重率阈值（%）",
     *     "dataType": "integer",                      // 数据类型
     *     "category": "plagiarismConfig"              // 所属分组
     *   },
     *   {
     *     "id": 2,
     *     "configKey": "document.maxFileSize",
     *     "configValue": "10",
     *     "description": "最大文件大小（MB）",
     *     "dataType": "integer",
     *     "category": "documentLimits"
     *   }
     * ]
     * 
     * @return 所有配置项的原始列表
     */
    List<DocConfig> listAllConfigs();

    /**
     * 📑 分页查询文档列表
     * 
     * 🎯 功能说明：
     * 查询学生提交的所有文档，支持分页、筛选和搜索。
     * 就像学校教务系统查看所有学生提交的作业，可以按班级、学生、提交时间筛选。
     * 
     * 💡 支持的筛选条件：
     * - 按用户ID筛选：查看某个学生的所有文档
     * - 按标题搜索：模糊搜索文档标题
     * - 按状态筛选：查看"查重中"/"已完成"/"查重失败"的文档
     * 
     * 💬 使用场景：
     * - 老师查看所有学生提交的毕业论文
     * - 管理员查找某个学生的所有查重记录
     * - 搜索标题包含"计算机网络"的所有文档
     * - 查看有多少文档查重失败了
     * 
     * 📥 参数说明：
     * @param page 页码，从1开始（比如：1）
     * @param size 每页条数（比如：10）
     * @param userId 用户ID，可为null（为null时查询所有用户）
     * @param title 标题关键词，可为null（支持模糊搜索）
     * @param status 文档状态，可为null（如："completed", "processing", "failed"）
     * 
     * 📤 返回内容示例：
     * {
     *   "total": 156,                            // 总记录数
     *   "pages": 16,                             // 总页数
     *   "current": 1,                            // 当前页
     *   "size": 10,                              // 每页条数
     *   "records": [
     *     {
     *       "id": 123,
     *       "userId": 1001,
     *       "userName": "张三",
     *       "title": "计算机网络课程设计",
     *       "fileName": "network_design.docx",
     *       "fileSize": 2048576,                 // 文件大小（字节）
     *       "status": "completed",               // 查重完成
     *       "similarityRate": 18.5,              // 查重率
     *       "uploadTime": "2024-01-15 09:30:00",
     *       "checkTime": "2024-01-15 09:31:25"
     *     }
     *   ]
     * }
     * 
     * @return 分页后的文档列表
     */
    IPage<DocDocument> pageDocuments(Integer page, Integer size, Long userId, String title, String status);

    /**
     * 🔍 获取文档详细信息
     * 
     * 🎯 功能说明：
     * 查看单个文档的详细信息，包括查重报告、降重记录、版本历史等。
     * 就像点开一篇作业，能看到学生什么时候提交的、查重率是多少、
     * 哪些段落被标记为相似、学生修改了几次等等。
     * 
     * 💡 包含的信息：
     * - 文档基本信息：标题、作者、上传时间、文件大小
     * - 查重统计：总查重率、引用率、相似段落数
     * - 降重统计：AI降重次数、降重段落数
     * - 版本历史：修改记录、每个版本的查重率变化
     * 
     * 💬 使用场景：
     * - 老师点开某篇论文查看详细的查重报告
     * - 管理员排查为什么某个文档查重失败
     * - 查看学生对这篇文档进行了几次AI降重
     * - 对比文档的不同版本，查看修改后查重率是否降低
     * 
     * 📥 参数说明：
     * @param documentId 文档ID
     * 
     * 📤 返回内容示例：
     * {
     *   "document": {                            // 基本信息
     *     "id": 123,
     *     "title": "计算机网络课程设计",
     *     "fileName": "network_design.docx",
     *     "fileSize": 2048576,
     *     "uploadTime": "2024-01-15 09:30:00",
     *     "status": "completed"
     *   },
     *   "plagiarismStats": {                     // 查重统计
     *     "totalSimilarity": 18.5,               // 总查重率
     *     "citationRate": 5.2,                   // 引用率
     *     "similarParagraphs": 8,                // 相似段落数
     *     "totalParagraphs": 45                  // 总段落数
     *   },
     *   "rewriteStats": {                        // 降重统计
     *     "totalRewrites": 3,                    // AI降重次数
     *     "rewrittenParagraphs": 5               // 已降重段落数
     *   },
     *   "versions": [                            // 版本历史
     *     {
     *       "versionNumber": 1,
     *       "createTime": "2024-01-15 09:30:00",
     *       "similarityRate": 25.3,              // 这个版本的查重率
     *       "description": "初始版本"
     *     },
     *     {
     *       "versionNumber": 2,
     *       "createTime": "2024-01-15 10:15:00",
     *       "similarityRate": 18.5,              // 修改后降低了
     *       "description": "AI降重后的版本"
     *     }
     *   ]
     * }
     * 
     * @return 文档的详细信息（包含查重和降重统计）
     */
    Map<String, Object> getDocumentDetail(Long documentId);
    
    /**
     * 🗑️ 删除文档（级联删除）
     * 
     * 🎯 功能说明：
     * 删除指定的文档，并自动删除关联的所有数据（段落、版本、查重报告等）。
     * 就像删除一篇作业，不仅要删除作业本身，还要删除老师的批改记录、成绩记录等。
     * 
     * 💡 什么是级联删除？
     * 文档在系统中会产生很多关联数据：
     * - 文档段落：文档分成的各个段落
     * - 查重报告：每个段落的相似度数据
     * - 版本记录：文档的历史版本
     * - 操作日志：用户对文档的操作记录
     * 级联删除会一次性删除这些所有相关数据，避免留下垃圾数据。
     * 
     * 💬 使用场景：
     * - 学生上传错了文档，请求删除
     * - 课程结束，清理过期的文档数据
     * - 文档查重出错，需要删除后重新上传
     * - 释放服务器存储空间
     * 
     * 📥 参数说明：
     * @param documentId 要删除的文档ID
     * 
     * 💭 删除过程：
     * 步骤1: 删除文档的所有段落记录
     * 步骤2: 删除文档的所有版本记录
     * 步骤3: 删除文档的查重报告数据
     * 步骤4: 删除文档的操作日志
     * 步骤5: 删除服务器上的文档文件
     * 步骤6: 最后删除文档主记录
     * 
     * ⚠️ 重要警告：
     * - 删除操作不可恢复！所有相关数据都会被永久删除
     * - 建议删除前先备份重要文档
     * - 只有管理员和文档作者本人才能删除
     */
    void deleteDocument(Long documentId);
}
