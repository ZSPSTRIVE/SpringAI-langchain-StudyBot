package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.DocSensitiveWord;
import org.apache.ibatis.annotations.Mapper;

/**
 * DocSensitiveWordMapper接口 - 敏感词库数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的doc_sensitive_word表，就像一个"敏感词字典管理员"。
 * 管理着系统的敏感词库，用于检测和过滤文档中的不合适内容。
 * 
 * 📚 敏感词检测功能：
 * 就像论坛的"敏感词过滤"，防止用户上传不合适的文档：
 * 1. 文档上传时：扫描文档内容，检测是否包含敏感词
 * 2. 发现敏感词：给出警告，或直接拒绝上传
 * 3. 敏感词管理：管理员可以添加、修改、删除敏感词
 * 4. 分类管理：敏感词分为不同级别（严重、中等、轻微）
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<DocSensitiveWord>
 * - 只使用BaseMapper提供的基础方法
 * - 敏感词通常缓存在内存中，提高检测速度
 * 
 * 📊 对应数据库表: doc_sensitive_word
 * 
 * 📝 表结构说明（主要字段）：
 * - id: 敏感词ID
 * - word: 敏感词内容
 * - level: 严重程度（HIGH/MEDIUM/LOW）
 * - category: 分类（政治、暴力、色情、违禁物等）
 * - action: 处理动作（REJECT/WARN/REPLACE）
 * - replacement: 替换词（如果action为REPLACE）
 * - status: 状态（ACTIVE/INACTIVE）
 * - create_time: 创建时间
 * 
 * 💡 使用场景：
 * 
 * 1. 系统启动加载敏感词库：
 *    - selectList()查询所有启用的敏感词
 *    - 加载到内存中（如Redis或HashMap）
 *    - 构建AC自动机或Trie树，快速匹配
 * 
 * 2. 文档上传时检测：
 *    - 读取文档内容
 *    - 使用敏感词库扫描文本
 *    - 发现敏感词后根据action处理
 * 
 * 3. 管理员维护敏感词：
 *    - insert()添加新的敏感词
 *    - updateById()修改敏感词级别或处理方式
 *    - deleteById()删除过时的敏感词
 * 
 * 4. 分级处理：
 *    - HIGH级别：直接拒绝上传
 *    - MEDIUM级别：给出警告，用户确认后可上传
 *    - LOW级别：自动替换为***或其他字符
 * 
 * 📝 使用示例1 - 加载敏感词库：
 * <pre>
 * // 系统启动时加载所有启用的敏感词
 * List<DocSensitiveWord> words = docSensitiveWordMapper.selectList(
 *     new LambdaQueryWrapper<DocSensitiveWord>()
 *         .eq(DocSensitiveWord::getStatus, "ACTIVE")
 * );
 * 
 * // 加载到敏感词检测器
 * SensitiveWordFilter filter = new SensitiveWordFilter();
 * for (DocSensitiveWord word : words) {
 *     filter.addWord(word.getWord(), word.getLevel());
 * }
 * </pre>
 * 
 * 📝 使用示例2 - 文档敏感词检测：
 * <pre>
 * // 检测文档内容中的敏感词
 * String content = readDocumentContent(file);
 * List<String> foundWords = sensitiveWordFilter.check(content);
 * 
 * if (!foundWords.isEmpty()) {
 *     // 发现敏感词，查询处理策略
 *     for (String word : foundWords) {
 *         DocSensitiveWord config = docSensitiveWordMapper.selectOne(
 *             new LambdaQueryWrapper<DocSensitiveWord>()
 *                 .eq(DocSensitiveWord::getWord, word)
 *         );
 *         if ("REJECT".equals(config.getAction())) {
 *             throw new BusinessException("文档包含禁止词汇，不能上传");
 *         }
 *     }
 * }
 * </pre>
 * 
 * 📝 使用示例3 - 添加敏感词：
 * <pre>
 * // 管理员添加新的敏感词
 * DocSensitiveWord word = new DocSensitiveWord();
 * word.setWord("某敏感词");
 * word.setLevel("HIGH");
 * word.setCategory("政治");
 * word.setAction("REJECT");
 * word.setStatus("ACTIVE");
 * docSensitiveWordMapper.insert(word);
 * 
 * // 更新内存缓存
 * sensitiveWordFilter.reload();
 * </pre>
 * 
 * ⚠️ 重要提示：
 * 1. 敏感词库应该缓存在内存中，避免每次检测都查数据库
 * 2. 使用AC自动机或Trie树等高效算法进行匹配
 * 3. 敏感词更新后需要重新加载缓存
 * 4. 建议支持正则表达式匹配，更灵活
 * 5. 注意过度检测可能导致误抦，需要平衡
 * 6. 敏感词库需要定期更新维护
 * 7. 建议分级处理，不要一刀切全部拒绝
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface DocSensitiveWordMapper extends BaseMapper<DocSensitiveWord> {
    // 只使用BaseMapper提供的基础方法
    // 这是敏感词库表，用于内容审核和过滤
}
