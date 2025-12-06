package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.DocConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * DocConfigMapper接口 - 文档查重与AI降重配置数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的doc_config表，就像一个"系统配置管理员"。
 * 管理着文档查重系统的各种配置参数，如查重阈值、AI模型选择、降重策略等。
 * 
 * 📚 配置表的作用：
 * 就像系统的“设置面板”，管理员可以调整各种参数：
 * 1. 查重阈值：设置多少相似度算作重复（如>30%）
 * 2. AI模型配置：选择使用哪个AI模型进行降重（GPT-4、通义千问等）
 * 3. 降重策略：设置降重的强度和方式
 * 4. 系统限制：文件大小限制、每天查重次数等
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<DocConfig>
 * - 只使用BaseMapper提供的基础方法
 * - 通常使用键值对形式存储配置（config_key, config_value）
 * 
 * 📊 对应数据库表: doc_config
 * 
 * 📝 常见配置项：
 * - similarity_threshold: 查重阈值（0-100）
 * - ai_model: AI模型名称（gpt-4, qwen, etc）
 * - ai_api_key: AI服务的API密钥
 * - max_file_size: 最大文件大小（MB）
 * - daily_check_limit: 每天查重次数限制
 * - rewrite_strength: 降重强度（low/medium/high）
 * 
 * 💡 使用场景：
 * 1. 系统初始化：加载配置参数
 * 2. 管理员修改配置：更新配置值
 * 3. 查重检测：读取阈值配置
 * 4. AI降重：读取AI模型配置
 * 
 * 📝 使用示例：
 * <pre>
 * // 读取查重阈值配置
 * DocConfig config = docConfigMapper.selectOne(
 *     new LambdaQueryWrapper<DocConfig>()
 *         .eq(DocConfig::getConfigKey, "similarity_threshold")
 * );
 * double threshold = Double.parseDouble(config.getConfigValue());  // 30.0
 * 
 * // 更新AI模型配置
 * config.setConfigValue("gpt-4o");
 * docConfigMapper.updateById(config);
 * </pre>
 * 
 * ⚠️ 重要提示：
 * 1. 配置通常使用单例模式，应该缓存在内存中
 * 2. API密钥等敏感信息应该加密存储
 * 3. 配置修改后应该清除缓存
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface DocConfigMapper extends BaseMapper<DocConfig> {
    // 只使用BaseMapper提供的基础方法
    // 这是文档查重系统的配置表，存储各种系统参数
}
