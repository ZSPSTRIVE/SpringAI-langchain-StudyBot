package com.qasystem.service;

import com.qasystem.dto.DocRewriteRequest;
import com.qasystem.dto.SaveDocVersionRequest;
import com.qasystem.entity.DocRewriteVersion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * DocService - 文档查重与AI降重服务接口
 * 
 * 🎯 作用：处理学生论文查重和AI降重功能
 * 就像一个“论文查重助手”，帮助学生检测论文重复率并使用AI进行降重。
 * 
 * 📝 主要功能：
 * 1. 文档查重：上传Word文档，检测重复率
 * 2. AI降重：对重复段落进行AI改写
 * 3. 版本管理：保存多个版本，方便对比和恢复
 * 4. 文档导出：生成降重后的Word文档
 * 
 * 💡 使用流程：
 * 1. 学生上传论文Word文档 → uploadAndCheck()
 * 2. 系统分析文档，显示查重报告 → getReport()
 * 3. 对重复段落进行AI降重 → rewriteText()
 * 4. 保存降重版本 → saveVersion()
 * 5. 下载降重后的文档 → downloadDocument()
 */
public interface DocService {

    /**
     * 上传Word文档并执行查重
     * 
     * 🎯 功能：学生上传论文Word文档，系统自动解析并检测重复率
     * 就像在查重网站上传论文，系统会分析每一段的重复情况。
     * 
     * @param userId 用户ID
     * @param file 上传的Word文档（.doc或.docx）
     * @return 查重结果，包含：
     *         - documentId: 文档ID
     *         - fileName: 文件名
     *         - totalRate: 总重复率
     *         - paragraphs: 每个段落的重复率和内容
     * 
     * 💬 使用场景：
     * - 学生在“论文查重”页面上传Word文档
     * - 系统解析文档并分段
     * - 计算每段的重复率
     * - 显示查重报告，高亮重复段落
     */
    Map<String, Object> uploadAndCheck(Long userId, MultipartFile file);

    /**
     * 获取文档查重报告
     * 
     * 🎯 功能：查询某个文档的完整查重结果
     * 就像再次打开之前的查重报告。
     * 
     * @param documentId 文档ID
     * @return 查重报告，包含所有段落的重复情况
     * 
     * 💬 使用场景：
     * - 学生查看之前上传的文档查重结果
     * - 显示每个段落的重复率和内容
     */
    Map<String, Object> getReport(Long documentId);

    /**
     * AI改写/降重指定文本
     * 
     * 🎯 功能：使用AI对重复段落进行改写，降低重复率
     * 就像AI帮你用不同的表达方式重写这段文字。
     * 
     * @param userId 用户ID
     * @param request 改写请求，包含原文、改写风格等
     * @return 改写结果，包含：
     *         - originalText: 原文
     *         - rewrittenText: 改写后的文字
     *         - rewriteRate: 改写程度
     * 
     * 💬 使用场景：
     * - 学生选中重复率高的段落
     * - 点击“AI降重”按钮
     * - 选择改写风格（学术风格/流畅性/扩展式等）
     * - AI生成改写后的文字
     * - 学生可以选择采用或重新生成
     */
    Map<String, Object> rewriteText(Long userId, DocRewriteRequest request);

    /**
     * 保存文档版本
     * 
     * 🎯 功能：保存当前文档状态为一个版本
     * 就像在Word中“另存为”，保留多个版本方便对比。
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     * @param request 版本保存请求，包含内容、备注等
     * @return 保存的版本信息
     * 
     * 💬 使用场景：
     * - 学生对多个段落进行AI降重后
     * - 点击“保存版本”按钮
     * - 填写版本备注，如“第二版-修改了第3章”
     * - 系统保存当前状态为一个版本
     */
    DocRewriteVersion saveVersion(Long userId, Long documentId, SaveDocVersionRequest request);

    /**
     * 获取文档的版本历史
     * 
     * 🎯 功能：查看文档的所有保存版本
     * 就像查看文件的修改历史。
     * 
     * @param documentId 文档ID
     * @return 版本列表，按时间降序
     * 
     * 💬 使用场景：
     * - 学生点击“版本历史”按钮
     * - 显示所有保存过的版本
     * - 可以查看每个版本的备注和保存时间
     */
    List<DocRewriteVersion> listVersions(Long documentId);

    /**
     * 获取单个版本详情
     * 
     * 🎯 功能：查看某个版本的完整内容
     * 
     * @param versionId 版本ID
     * @return 版本详情，包含完整文档内容
     * 
     * 💬 使用场景：
     * - 学生在版本历史中点击某个版本
     * - 查看该版本的完整内容
     * - 可以对比不同版本的差异
     */
    DocRewriteVersion getVersion(Long versionId);

    /**
     * 批量更新文档段落内容
     * 
     * 🎯 功能：一次性更新多个段落的内容
     * 就像批量应用AI降重的结果。
     * 
     * @param documentId 文档ID
     * @param paragraphs 段落列表，包含段落索引和新内容
     * 
     * 💬 使用场景：
     * - 学生对多个段落分别进行AI降重
     * - 选择满意的改写结果
     * - 点击“应用所有修改”
     * - 批量更新这些段落的内容
     */
    void batchUpdateParagraphs(Long documentId, List<Map<String, Object>> paragraphs);

    /**
     * 生成并下载Word文档
     * 
     * 🎯 功能：将降重后的文档生成Word文件供下载
     * 就像导出最终的论文文档。
     * 
     * @param documentId 文档ID
     * @return Word文件的字节数据
     * @throws Exception 文档生成失败
     * 
     * 💬 使用场景：
     * - 学生完成AI降重后
     * - 点击“下载文档”按钮
     * - 系统生成Word文档并下载到本地
     * - 学生可以用这个文档提交论文
     */
    byte[] downloadDocument(Long documentId) throws Exception;
}
