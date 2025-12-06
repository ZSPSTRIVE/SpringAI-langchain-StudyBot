package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.DocParagraph;
import org.apache.ibatis.annotations.Mapper;

/**
 * DocParagraphMapper接口 - 文档段落数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的doc_paragraph表，就像一个"文档段落管理员"。
 * 管理着每个文档被分解后的段落内容，是查重系统进行相似度对比的基础数据。
 * 
 * 📚 段落表的作用：
 * 就像把一篇文章切成一个个段落，每个段落都可以单独进行查重对比。
 * 1. 文档分段：将整篇文档按段落分解
 * 2. 段落对比：每个段落与其他文档的段落对比
 * 3. 相似度计算：计算每个段落的重复率
 * 4. 查重报告：标记哪些段落重复，重复率多少
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<DocParagraph>
 * - 只使用BaseMapper提供的基础方法，没有自定义方法
 * - 这是文档查重的详细数据表，存储每个段落的内容和查重结果
 * 
 * 📊 对应数据库表: doc_paragraph
 * 
 * 📝 表结构说明（主要字段）：
 * - id: 段落ID
 * - document_id: 所属文档ID（外键关联doc_document.id）
 * - paragraph_index: 段落序号（在文档中的位置，从1开始）
 * - content: 段落内容文本
 * - char_count: 段落字符数
 * - similarity_rate: 该段落的查重率（百分比）
 * - matched_doc_id: 匹配到的文档ID（如果有重复）
 * - matched_para_id: 匹配到的段落ID（具体重复段落）
 * - create_time: 创建时间
 * 
 * 🔗 关联关系：
 * - 关联doc_document表：paragraph.document_id = document.id （段落属于某个文档）
 * - 自关联：paragraph.matched_para_id = paragraph.id （指向重复的段落）
 * 
 * 🔧 MyBatis-Plus提供的免费方法：
 * - insert(DocParagraph para)：插入新段落
 * - insertBatch(List<DocParagraph>)：批量插入段落（文档解析时使用）
 * - deleteById(Long id)：删除段落
 * - updateById(DocParagraph para)：更新段落信息（如查重结果）
 * - selectById(Long id)：根据ID查询段落
 * - selectList(Wrapper)：查询段落列表
 * 
 * 💡 使用场景：
 * 
 * 1. 文档解析流程：
 *    - 上传文档后，后台任务读取文档内容
 *    - 按照段落分隔符（\n\n 或特殊标记）切分文档
 *    - insertBatch()批量插入所有段落
 * 
 * 2. 查重对比流程：
 *    - 逐个读取当前文档的每个段落
 *    - 与数据库中已有文档的所有段落进行对比
 *    - 使用相似度算法（如余弦相似度、编辑距离）计算重复率
 *    - updateById()更新每个段落的similarity_rate和matched_para_id
 * 
 * 3. 查看查重报告：
 *    - selectList()查询某个文档的所有段落
 *    - 按paragraph_index排序，按顺序显示
 *    - 标记重复率高的段落（如红色高亮显示）
 *    - 显示匹配的来源文档
 * 
 * 4. 统计文档总查重率：
 *    - selectList()查询文档的所有段落
 *    - 计算平均查重率：总查重字符数 / 总字符数
 *    - 更新doc_document表的similarity_rate
 * 
 * 5. 段落级别的查询：
 *    - 查询重复率最高的段落
 *    - 查询某个段落匹配到的源段落
 *    - 分析哪些段落是原创的，哪些是重复的
 * 
 * 6. 文档删除时级联删除：
 *    - 删除文档时，同时删除所有关联的段落
 *    - 使用delete(new LambdaQueryWrapper<>().eq(DocParagraph::getDocumentId, docId))
 * 
 * 📝 使用示例1 - 文档解析分段：
 * <pre>
 * // 读取文档内容并分段
 * String docContent = readDocumentContent(filePath);
 * String[] paragraphs = docContent.split("\n\n");  // 按空行分段
 * 
 * List<DocParagraph> paraList = new ArrayList<>();
 * for (int i = 0; i < paragraphs.length; i++) {
 *     DocParagraph para = new DocParagraph();
 *     para.setDocumentId(documentId);
 *     para.setParagraphIndex(i + 1);  // 从1开始
 *     para.setContent(paragraphs[i].trim());
 *     para.setCharCount(paragraphs[i].length());
 *     para.setSimilarityRate(0.0);  // 初始为0
 *     paraList.add(para);
 * }
 * 
 * // 批量插入
 * docParagraphMapper.insertBatch(paraList);
 * </pre>
 * 
 * 📝 使用示例2 - 查询文档所有段落：
 * <pre>
 * // 查询某个文档的所有段落，按序号排列
 * List<DocParagraph> paragraphs = docParagraphMapper.selectList(
 *     new LambdaQueryWrapper<DocParagraph>()
 *         .eq(DocParagraph::getDocumentId, documentId)
 *         .orderByAsc(DocParagraph::getParagraphIndex)
 * );
 * 
 * // 显示查重报告
 * for (DocParagraph para : paragraphs) {
 *     if (para.getSimilarityRate() > 50) {
 *         System.out.println("段落" + para.getParagraphIndex() + ": 重复率" + para.getSimilarityRate() + "%");
 *         System.out.println("内容: " + para.getContent());
 *         System.out.println("匹配文档: " + para.getMatchedDocId());
 *     }
 * }
 * </pre>
 * 
 * 📝 使用示例3 - 更新查重结果：
 * <pre>
 * // 查重对比后更新每个段落的查重结果
 * DocParagraph para = docParagraphMapper.selectById(paraId);
 * para.setSimilarityRate(75.5);  // 75.5%重复
 * para.setMatchedDocId(matchedDocId);  // 匹配到的文档
 * para.setMatchedParaId(matchedParaId);  // 匹配到的段落
 * docParagraphMapper.updateById(para);
 * </pre>
 * 
 * 📝 使用示例4 - 计算文档总查重率：
 * <pre>
 * // 查询所有段落
 * List<DocParagraph> paragraphs = docParagraphMapper.selectList(
 *     new LambdaQueryWrapper<DocParagraph>()
 *         .eq(DocParagraph::getDocumentId, documentId)
 * );
 * 
 * // 计算加权平均查重率
 * int totalChars = 0;
 * double totalSimilarChars = 0;
 * for (DocParagraph para : paragraphs) {
 *     totalChars += para.getCharCount();
 *     totalSimilarChars += para.getCharCount() * para.getSimilarityRate() / 100;
 * }
 * double avgSimilarityRate = (totalSimilarChars / totalChars) * 100;
 * 
 * // 更新文档总查重率
 * DocDocument doc = new DocDocument();
 * doc.setId(documentId);
 * doc.setSimilarityRate(avgSimilarityRate);
 * docDocumentMapper.updateById(doc);
 * </pre>
 * 
 * ⚠️ 重要提示：
 * 1. 每个文档会被分解为多个段落，段落数量可能很多
 * 2. paragraph_index字段非常重要，用于保持段落顺序
 * 3. 查重对比是计算密集型操作，需要在后台异步执行
 * 4. similarity_rate范围0-100，表示该段落的重复率
 * 5. matched_doc_id和matched_para_id用于追溯重复源
 * 6. 删除文档时记得级联删除所有段落
 * 7. 段落内容（content）可能很长，建议使用TEXT类型存储
 * 8. 建议在document_id和paragraph_index上创建索引，提高查询效率
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface DocParagraphMapper extends BaseMapper<DocParagraph> {
    // 只使用BaseMapper提供的基础方法，没有自定义方法
    // 这是文档查重的详细数据表，存储每个段落的内容和查重结果
}
