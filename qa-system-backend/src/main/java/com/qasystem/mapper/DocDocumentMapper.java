package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.DocDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * DocDocumentMapper接口 - 文档查重主表数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的doc_document表，就像一个"文档档案管理员"。
 * 管理着系统中所有上传的文档信息，是文档查重功能的核心数据表。
 * 
 * 📚 文档查重系统介绍：
 * 文档查重是一个类似“知网”的功能，用于检测文档的重复率。系统包括：
 * 1. 文档上传：学生上传论文、作业等文档
 * 2. 文档解析：将文档分段（paragraph），分句分析
 * 3. 查重检测：与数据库中已有文档对比，计算相似度
 * 4. 查重报告：生成详细的查重报告，标注重复部分
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<DocDocument>
 * - 只使用BaseMapper提供的基础方法，没有自定义方法
 * - 这是文档查重功能的主表，存储文档基本信息
 * 
 * 📊 对应数据库表: doc_document
 * 
 * 📝 表结构说明（主要字段）：
 * - id: 文档主ID
 * - user_id: 上传用户ID
 * - title: 文档标题
 * - file_name: 原始文件名
 * - file_path: 文件存储路径
 * - file_size: 文件大小（字节）
 * - total_chars: 总字符数
 * - similarity_rate: 查重率（百分比）
 * - status: 处理状态（PENDING/PROCESSING/COMPLETED/FAILED）
 * - create_time: 创建时间
 * - update_time: 更新时间
 * 
 * 🔗 关联关系：
 * - 关联user表：doc_document.user_id = user.id （文档上传者）
 * - 与doc_paragraph表为一对多关系：一个文档包含多个段落
 * - 与doc_rewrite_version表为一对多关系：一个文档可有多个改写版本
 * 
 * 🔧 MyBatis-Plus提供的免费方法：
 * - insert(DocDocument doc)：插入新文档记录
 * - deleteById(Long id)：删除文档
 * - updateById(DocDocument doc)：更新文档信息（如查重率、状态）
 * - selectById(Long id)：根据ID查询文档
 * - selectList(Wrapper)：查询文档列表
 * - selectCount(Wrapper)：统计文档数量
 * 
 * 💡 使用场景：
 * 
 * 1. 文档上传流程：
 *    - 用户上传文档 → 保存文件 → insert()创建doc_document记录
 *    - 状态设置为PENDING（等待处理）
 * 
 * 2. 文档解析流程：
 *    - 后台任务读取文档内容 → 分段分句 → 保存到doc_paragraph表
 *    - updateById()更新状态为PROCESSING（处理中）
 * 
 * 3. 查重检测流程：
 *    - 将当前文档的每个段落与数据库中已有文档对比
 *    - 计算相似度，生成查重结果
 *    - updateById()更新similarity_rate（查重率）和status为COMPLETED
 * 
 * 4. 查看查重报告：
 *    - selectById()查询文档详情
 *    - 获取查重率、标题、上传者等信息
 *    - 关联doc_paragraph表查询详细的重复内容
 * 
 * 5. 文档列表查询：
 *    - selectList()查询用户的所有文档
 *    - 可按查重率、上传时间等排序
 *    - 支持分页显示
 * 
 * 6. 文档删除：
 *    - deleteById()删除文档记录
 *    - 同时需要删除关联的doc_paragraph记录
 *    - 删除服务器上的文件
 * 
 * 📝 使用示例：
 * <pre>
 * // 上传文档
 * DocDocument doc = new DocDocument();
 * doc.setUserId(currentUserId);
 * doc.setTitle("我的毕业论文");
 * doc.setFileName("thesis.docx");
 * doc.setFilePath("/uploads/2024/01/xxx.docx");
 * doc.setFileSize(102400L);  // 100KB
 * doc.setStatus("PENDING");
 * docDocumentMapper.insert(doc);
 * 
 * // 更新查重结果
 * doc.setSimilarityRate(15.6);  // 15.6%
 * doc.setStatus("COMPLETED");
 * docDocumentMapper.updateById(doc);
 * 
 * // 查询用户的所有文档
 * List<DocDocument> docs = docDocumentMapper.selectList(
 *     new LambdaQueryWrapper<DocDocument>()
 *         .eq(DocDocument::getUserId, userId)
 *         .orderByDesc(DocDocument::getCreateTime)
 * );
 * </pre>
 * 
 * ⚠️ 重要提示：
 * 1. 这是文档查重功能的核心表，存储文档的基本信息
 * 2. 文档的详细内容（段落）存储在doc_paragraph表中
 * 3. 文档处理是异步的，需要通过status字段跟踪处理状态
 * 4. similarity_rate表示查重率，范围0-100
 * 5. 支持的文件格式通常包括：docx, pdf, txt等
 * 6. 删除文档时需要级联删除相关的paragraph记录
 * 7. 文件路径应该安全存储，避免路径遍历攻击
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface DocDocumentMapper extends BaseMapper<DocDocument> {
    // 只使用BaseMapper提供的基础方法，没有自定义方法
    // 这是文档查重功能的主表，存储文档基本信息
}
