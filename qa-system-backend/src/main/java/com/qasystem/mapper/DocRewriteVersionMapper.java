package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.DocRewriteVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * DocRewriteVersionMapper接口 - 文档降重版本数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的doc_rewrite_version表，就像一个"文档版本管理员"。
 * 管理着文档经过AI降重后的各个版本，就像Git的版本控制一样。
 * 
 * 📚 AI降重功能介绍：
 * AI降重就是使用AI对重复内容进行改写，降低查重率：
 * 1. 用户上传文档 → 查重检测 → 发现重复率高
 * 2. 点击AI降重按钮 → AI改写重复段落 → 生成新版本
 * 3. 可以多次降重，每次生成一个新版本
 * 4. 用户可以对比不同版本，选择最优版本
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<DocRewriteVersion>
 * - 只使用BaseMapper提供的基础方法
 * - 一个文档可以有多个降重版本
 * 
 * 📊 对应数据库表: doc_rewrite_version
 * 
 * 📝 表结构说明（主要字段）：
 * - id: 版本ID
 * - document_id: 原始文档ID
 * - version_number: 版本号（1, 2, 3...）
 * - rewritten_content: 降重后的内容
 * - similarity_rate: 降重后的查重率
 * - ai_model: 使用的AI模型
 * - rewrite_strategy: 降重策略（mild/moderate/aggressive）
 * - create_time: 创建时间
 * 
 * 🔗 关联关系：
 * - 关联doc_document表：rewrite_version.document_id = document.id
 * - 一个文档可有多个降重版本
 * 
 * 💡 使用场景：
 * 
 * 1. 第一次AI降重：
 *    - 用户查重后发现重复率60%
 *    - 点击“AI降重”按钮
 *    - AI改写重复段落，生成版本1
 *    - insert()保存版本1，查重率降到35%
 * 
 * 2. 第二次降重：
 *    - 用户对版本1不满意，再次点击降重
 *    - AI再次改写，生成版本2
 *    - insert()保存版本2，查重率降到20%
 * 
 * 3. 查看版本历史：
 *    - selectList()查询文档的所有版本
 *    - 按version_number排序显示
 *    - 用户可以对比不同版本的差异
 * 
 * 4. 选择最优版本：
 *    - 用户对比后选择最优版本
 *    - 下载或替换原文档
 * 
 * 5. 删除版本：
 *    - 用户可以删除不需要的版本
 *    - deleteById()删除指定版本
 * 
 * 📝 使用示例1 - 生成降重版本：
 * <pre>
 * // AI降重后保存新版本
 * DocRewriteVersion version = new DocRewriteVersion();
 * version.setDocumentId(documentId);
 * version.setVersionNumber(1);  // 第1个版本
 * version.setRewrittenContent("降重后的内容...");
 * version.setSimilarityRate(35.0);  // 降到35%
 * version.setAiModel("gpt-4o");
 * version.setRewriteStrategy("moderate");
 * docRewriteVersionMapper.insert(version);
 * </pre>
 * 
 * 📝 使用示例2 - 查询版本历史：
 * <pre>
 * // 查询文档的所有降重版本
 * List<DocRewriteVersion> versions = docRewriteVersionMapper.selectList(
 *     new LambdaQueryWrapper<DocRewriteVersion>()
 *         .eq(DocRewriteVersion::getDocumentId, documentId)
 *         .orderByAsc(DocRewriteVersion::getVersionNumber)
 * );
 * 
 * // 显示版本列表
 * for (DocRewriteVersion ver : versions) {
 *     System.out.println("版本" + ver.getVersionNumber() + ": 查重率" + ver.getSimilarityRate() + "%");
 * }
 * </pre>
 * 
 * 📝 使用示例3 - 统计最优版本：
 * <pre>
 * // 查找查重率最低的版本
 * DocRewriteVersion bestVersion = docRewriteVersionMapper.selectOne(
 *     new LambdaQueryWrapper<DocRewriteVersion>()
 *         .eq(DocRewriteVersion::getDocumentId, documentId)
 *         .orderByAsc(DocRewriteVersion::getSimilarityRate)
 *         .last("LIMIT 1")
 * );
 * System.out.println("最优版本：版本" + bestVersion.getVersionNumber() + ", 查重率" + bestVersion.getSimilarityRate() + "%");
 * </pre>
 * 
 * ⚠️ 重要提示：
 * 1. 每次AI降重都会生成一个新版本，version_number自增
 * 2. rewritten_content可能很长，建议使用LONGTEXT类型
 * 3. AI降重是付费服务，需要控制使用次数
 * 4. 建议保留原始文档，不要直接覆盖
 * 5. 版本太多时可以设置保留数量限制（如最多5个）
 * 6. 删除文档时需要级联删除所有版本
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface DocRewriteVersionMapper extends BaseMapper<DocRewriteVersion> {
    // 只使用BaseMapper提供的基础方法
    // 这是文档降重版本表，记录AI降重后的各个版本
}
