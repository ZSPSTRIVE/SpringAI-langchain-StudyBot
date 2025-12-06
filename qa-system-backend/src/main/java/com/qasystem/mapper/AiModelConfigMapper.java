package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AiModelConfigMapper接口 - AI模型配置数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的ai_model_config表，就像一个"AI模型管理员"。
 * 管理着系统支持的各种AI模型的配置信息，就像一个AI服务目录。
 * 
 * 📚 AI模型配置表的作用：
 * 系统可能支持多种AI模型，每个模型有不同的配置：
 * 1. 模型信息：模型名称、版本、提供商
 * 2. API配置：API地址、API Key、请求参数
 * 3. 费用信息：价格、使用额度、余额
 * 4. 性能参数：超时时间、重试次数、并发限制
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<AiModelConfig>
 * - 只使用BaseMapper提供的基础方法
 * - 系统可以配置多个AI模型，灵活切换
 * 
 * 📊 对应数据库表: ai_model_config
 * 
 * 📝 表结构说明（主要字段）：
 * - id: 配置ID
 * - model_name: 模型名称（gpt-4o, qwen-max, claude-3等）
 * - model_version: 模型版本
 * - provider: 提供商（OpenAI, Alibaba, Anthropic等）
 * - api_endpoint: API请求地址
 * - api_key: API密钥（加密存储）
 * - max_tokens: 最大token数
 * - temperature: 温度参数（0-1）
 * - timeout: 超时时间（秒）
 * - retry_times: 重试次数
 * - price_per_1k_tokens: 每1000 tokens价格
 * - status: 状态（ACTIVE/INACTIVE）
 * - is_default: 是否为默认模型
 * - create_time: 创建时间
 * 
 * 💡 使用场景：
 * 
 * 1. 系统初始化加载AI模型：
 *    - selectList()查询所有启用的模型配置
 *    - 初始化AI服务客户端
 *    - 缓存到内存中
 * 
 * 2. AI降重时选择模型：
 *    - 查询默认模型或用户选择的模型
 *    - 获取API配置进行调用
 *    - 记录使用情况和费用
 * 
 * 3. 模型切换：
 *    - 当一个模型不可用时，自动切换到备用模型
 *    - 根据成本选择最优模型
 * 
 * 4. 管理员配置模型：
 *    - insert()添加新的AI模型
 *    - updateById()修改模型参数或API Key
 *    - 启用/禁用某个模型
 * 
 * 5. 成本统计：
 *    - 根据price_per_1k_tokens计算使用成本
 *    - 生成费用报表
 * 
 * 📝 使用示例1 - 查询默认模型：
 * <pre>
 * // 获取默认的AI模型配置
 * AiModelConfig defaultModel = aiModelConfigMapper.selectOne(
 *     new LambdaQueryWrapper<AiModelConfig>()
 *         .eq(AiModelConfig::getIsDefault, true)
 *         .eq(AiModelConfig::getStatus, "ACTIVE")
 * );
 * 
 * // 使用配置调用AI服务
 * AiClient client = new AiClient(defaultModel.getApiEndpoint(), defaultModel.getApiKey());
 * String result = client.rewrite(content, defaultModel.getTemperature());
 * </pre>
 * 
 * 📝 使用示例2 - 查询所有可用模型：
 * <pre>
 * // 查询所有启用的AI模型
 * List<AiModelConfig> models = aiModelConfigMapper.selectList(
 *     new LambdaQueryWrapper<AiModelConfig>()
 *         .eq(AiModelConfig::getStatus, "ACTIVE")
 *         .orderByAsc(AiModelConfig::getPricePer1kTokens)  // 按价格排序
 * );
 * 
 * // 让用户选择模型
 * for (AiModelConfig model : models) {
 *     System.out.println(model.getModelName() + " - 价格: " + model.getPricePer1kTokens() + "元/1000 tokens");
 * }
 * </pre>
 * 
 * 📝 使用示例3 - 添加新模型：
 * <pre>
 * // 管理员添加新的AI模型配置
 * AiModelConfig config = new AiModelConfig();
 * config.setModelName("gpt-4o");
 * config.setModelVersion("2024-05-13");
 * config.setProvider("OpenAI");
 * config.setApiEndpoint("https://api.openai.com/v1/chat/completions");
 * config.setApiKey(encryptApiKey("sk-..."));  // 加密存储
 * config.setMaxTokens(4096);
 * config.setTemperature(0.7);
 * config.setTimeout(60);
 * config.setRetryTimes(3);
 * config.setPricePer1kTokens(0.03);  // 3分钱/1000 tokens
 * config.setStatus("ACTIVE");
 * config.setIsDefault(false);
 * aiModelConfigMapper.insert(config);
 * </pre>
 * 
 * 📝 使用示例4 - 计算使用成本：
 * <pre>
 * // 计算AI降重的成本
 * AiModelConfig model = aiModelConfigMapper.selectById(modelId);
 * int tokensUsed = 2500;  // 实际使用的token数
 * double cost = (tokensUsed / 1000.0) * model.getPricePer1kTokens();
 * System.out.println("本次降重成本: " + cost + "元");
 * </pre>
 * 
 * ⚠️ 重要提示：
 * 1. API Key必须加密存储，不能明文保存
 * 2. 建议配置多个模型作为备用，提高可用性
 * 3. 模型配置应该缓存在内存中，减少数据库查询
 * 4. timeout和retry_times很重要，避免请求卡死
 * 5. 建议设置一个默认模型，方便系统自动选择
 * 6. price_per_1k_tokens用于成本控制和费用统计
 * 7. 不同模型的效果和价格不同，需要平衡选择
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
    // 只使用BaseMapper提供的基础方法
    // 这是AI模型配置表，管理各种可用的AI模型
}
