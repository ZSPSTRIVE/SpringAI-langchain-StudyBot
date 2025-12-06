package com.qasystem.service;

import com.qasystem.entity.AiModelConfig;

import java.util.List;
import java.util.Map;

/**
 * 🤖 AI模型配置服务接口
 * 
 * 📖 这是什么？
 * 这个服务就像一个"AI大脑管理中心"，负责管理系统中所有可用的AI模型配置。
 * 就像手机里可以切换不同的输入法（讯飞、搜狗、百度等），这个服务让管理员可以
 * 配置和切换不同的AI模型提供商（OpenAI、通义千问、文心一言等）。
 * 
 * 🎯 核心功能：
 * 1. 查看所有已配置的AI模型（就像查看手机里安装了哪些输入法）
 * 2. 获取当前正在使用的AI模型
 * 3. 添加/编辑AI模型配置
 * 4. 切换使用哪个AI模型
 * 5. 删除不需要的AI模型配置
 * 6. 测试AI模型连接是否正常
 * 
 * 💡 使用场景：
 * - 管理员需要添加新的AI模型供系统使用
 * - 当前AI模型响应慢，想切换到另一个模型
 * - 测试新配置的AI模型是否能正常工作
 * - 某个AI模型服务到期，需要删除配置
 * 
 * @author 师生答疑系统开发团队
 */
public interface AiModelConfigService {
    
    /**
     * 📋 获取所有AI模型配置列表
     * 
     * 🎯 功能说明：
     * 获取系统中所有已配置的AI模型信息，包括正在使用的和备用的。
     * 就像查看手机里安装的所有输入法列表，能看到每个输入法的名称、版本、状态等。
     * 
     * 💬 使用场景：
     * - 管理员进入AI模型管理页面，展示所有可用的模型配置
     * - 查看每个模型的配置参数（API地址、密钥、模型名称等）
     * - 了解哪个模型正在被系统使用（isActive=true）
     * 
     * 📤 返回内容示例：
     * [
     *   {
     *     "id": 1,
     *     "providerName": "OpenAI GPT-4",    // 模型提供商名称
     *     "modelName": "gpt-4",              // 具体模型名称
     *     "apiUrl": "https://api.openai.com/v1/chat/completions",
     *     "apiKey": "sk-***",                // 脱敏显示的密钥
     *     "isActive": true,                  // 是否为当前激活的模型
     *     "temperature": 0.7,                // 生成文本的随机性
     *     "maxTokens": 2000                  // 单次回答最大字数
     *   },
     *   {
     *     "id": 2,
     *     "providerName": "通义千问",
     *     "modelName": "qwen-max",
     *     "apiUrl": "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
     *     "apiKey": "sk-***",
     *     "isActive": false,                 // 备用模型，当前未使用
     *     "temperature": 0.7,
     *     "maxTokens": 2000
     *   }
     * ]
     * 
     * @return 所有AI模型配置的列表（包括激活和未激活的）
     */
    List<AiModelConfig> listAll();
    
    /**
     * ⭐ 获取当前激活的AI模型配置
     * 
     * 🎯 功能说明：
     * 获取系统当前正在使用的AI模型配置信息。
     * 就像查看手机当前正在使用的输入法是哪一个（比如现在用的是搜狗输入法）。
     * 
     * 💡 为什么需要这个方法？
     * 系统可能配置了多个AI模型，但同一时间只能使用一个作为"默认AI大脑"。
     * 这个方法能快速获取当前正在工作的那个模型的配置，供AI助手服务调用。
     * 
     * 💬 使用场景：
     * - 学生向AI助手提问时，系统需要知道调用哪个AI模型的接口
     * - 文档降重功能需要获取AI模型的API地址和密钥
     * - 在页面上显示"当前使用的AI模型：OpenAI GPT-4"
     * 
     * 📤 返回内容示例：
     * {
     *   "id": 1,
     *   "providerName": "OpenAI GPT-4",
     *   "modelName": "gpt-4",
     *   "apiUrl": "https://api.openai.com/v1/chat/completions",
     *   "apiKey": "sk-proj-abc123...",      // 完整密钥，用于调用API
     *   "isActive": true,
     *   "temperature": 0.7,
     *   "maxTokens": 2000,
     *   "timeout": 30000                      // 超时时间（毫秒）
     * }
     * 
     * ⚠️ 注意事项：
     * - 如果系统中没有激活的模型，会抛出异常提示管理员先配置
     * - 返回的配置包含完整的API密钥，需要保护好不要泄露
     * 
     * @return 当前激活的AI模型配置对象
     * @throws BusinessException 如果没有激活的模型配置
     */
    AiModelConfig getActiveConfig();
    
    /**
     * 💾 创建或更新AI模型配置
     * 
     * 🎯 功能说明：
     * 保存AI模型的配置信息，如果配置已存在则更新，不存在则新建。
     * 就像在手机里添加新的输入法，或者修改现有输入法的设置（比如改键盘布局、字体大小）。
     * 
     * 💡 为什么用"SaveOrUpdate"？
     * 这是一种智能保存方式：
     * - 如果config.id为null → 新建一条配置记录
     * - 如果config.id有值 → 更新这条配置记录
     * 这样前端只需调用一个接口，不用区分"新增"还是"编辑"。
     * 
     * 💬 使用场景：
     * - 管理员添加新的AI模型（比如接入了百度文心一言）
     * - 修改现有模型的参数（比如调整temperature让回答更稳定）
     * - 更新模型的API密钥（原密钥过期了）
     * 
     * 📥 参数示例：
     * // 新建配置（id为null）
     * {
     *   "id": null,
     *   "providerName": "文心一言",
     *   "modelName": "ERNIE-Bot-4",
     *   "apiUrl": "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro",
     *   "apiKey": "your-api-key-here",
     *   "secretKey": "your-secret-key-here",
     *   "isActive": false,                   // 先配置，暂不激活
     *   "temperature": 0.8,
     *   "maxTokens": 2000
     * }
     * 
     * // 更新配置（id有值）
     * {
     *   "id": 2,
     *   "providerName": "文心一言",
     *   "temperature": 0.7,                   // 降低随机性，让回答更稳定
     *   "maxTokens": 3000                     // 增加最大字数
     * }
     * 
     * ⚠️ 注意事项：
     * - apiKey和secretKey必须保密，不能泄露
     * - 如果设置isActive=true，会自动把其他模型设为非激活状态
     * - 建议保存后立即调用testConnection()测试连接
     * 
     * @param config 要保存的AI模型配置对象
     * @return 保存后的配置对象（包含生成的id和创建时间）
     */
    AiModelConfig saveOrUpdate(AiModelConfig config);
    
    /**
     * 🔄 切换激活的AI模型
     * 
     * 🎯 功能说明：
     * 将指定的AI模型设置为当前激活状态，系统后续将使用这个模型回答问题。
     * 就像切换手机输入法：从搜狗输入法切换到讯飞输入法。
     * 
     * 💡 内部逻辑：
     * 1. 将所有模型的isActive设为false（关闭其他输入法）
     * 2. 将指定id的模型的isActive设为true（开启选中的输入法）
     * 3. 系统立即生效，下次AI调用就会使用新模型
     * 
     * 💬 使用场景：
     * - OpenAI接口响应慢，临时切换到国内的通义千问
     * - 测试不同AI模型的回答质量，选择最适合的
     * - 某个模型API额度用完了，切换到备用模型
     * 
     * 📥 参数说明：
     * @param id 要激活的模型配置ID（比如：2）
     * 
     * 💭 执行流程：
     * 步骤1: 检查id=2的配置是否存在
     * 步骤2: 将所有配置的isActive改为false
     * 步骤3: 将id=2的配置的isActive改为true
     * 步骤4: 清除AI服务的配置缓存（让新配置立即生效）
     * 
     * ⚠️ 注意事项：
     * - 如果id不存在，会抛出异常
     * - 切换后立即生效，所有AI功能都会使用新模型
     * - 建议切换前先测试新模型连接是否正常
     */
    void setActive(Long id);
    
    /**
     * 🗑️ 删除AI模型配置
     * 
     * 🎯 功能说明：
     * 删除指定的AI模型配置记录。
     * 就像卸载手机上的某个输入法。
     * 
     * 💬 使用场景：
     * - 某个AI服务到期了，不再使用
     * - 测试配置用完了，需要清理
     * - 误添加了重复的配置
     * 
     * 📥 参数说明：
     * @param id 要删除的模型配置ID
     * 
     * ⚠️ 重要限制：
     * - 不能删除当前激活的模型（isActive=true）
     *   就像不能卸载当前正在使用的输入法
     * - 删除前要先切换到其他模型，或者确保系统中至少还有一个可用模型
     * 
     * @throws BusinessException 如果尝试删除激活的模型
     */
    void delete(Long id);
    
    /**
     * 🏢 获取支持的AI厂商列表
     * 
     * 🎯 功能说明：
     * 返回系统支持接入的AI模型提供商列表。
     * 就像查看手机应用商店里有哪些输入法可以下载（搜狗、讯飞、百度、谷歌等）。
     * 
     * 💡 这是什么？
     * 这是一个"预设模板列表"，帮助管理员快速配置常见的AI模型。
     * 每个模板包含该厂商的默认配置参数，减少手动填写的工作量。
     * 
     * 💬 使用场景：
     * - 管理员点击"添加AI模型"，展示可选的厂商列表
     * - 选择"OpenAI"后，自动填充API地址、默认模型名等
     * - 管理员只需填写API密钥就能快速完成配置
     * 
     * 📤 返回内容示例：
     * [
     *   {
     *     "code": "openai",                     // 厂商标识
     *     "name": "OpenAI",                      // 显示名称
     *     "logo": "/images/providers/openai.png",
     *     "models": ["gpt-4", "gpt-3.5-turbo"],  // 可选模型
     *     "defaultApiUrl": "https://api.openai.com/v1/chat/completions",
     *     "configTemplate": {                    // 配置模板
     *       "temperature": 0.7,
     *       "maxTokens": 2000
     *     }
     *   },
     *   {
     *     "code": "qwen",
     *     "name": "阿里通义千问",
     *     "logo": "/images/providers/qwen.png",
     *     "models": ["qwen-max", "qwen-plus", "qwen-turbo"],
     *     "defaultApiUrl": "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
     *     "configTemplate": {
     *       "temperature": 0.7,
     *       "maxTokens": 2000
     *     }
     *   },
     *   {
     *     "code": "ernie",
     *     "name": "百度文心一言",
     *     "models": ["ERNIE-Bot-4", "ERNIE-Bot-turbo"],
     *     "defaultApiUrl": "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro"
     *   }
     * ]
     * 
     * @return 支持的AI厂商信息列表（包含配置模板）
     */
    List<Map<String, Object>> getSupportedProviders();
    
    /**
     * 🔍 测试AI模型连接
     * 
     * 🎯 功能说明：
     * 测试指定AI模型的配置是否正确，API连接是否正常。
     * 就像测试新安装的输入法能不能正常打字，网络连接是否畅通。
     * 
     * 💡 测试内容：
     * 1. 检查API地址是否可访问
     * 2. 验证API密钥是否有效
     * 3. 发送一个简单的测试问题（如："你好"）
     * 4. 检查返回结果是否正常
     * 5. 记录响应时间
     * 
     * 💬 使用场景：
     * - 添加新模型配置后，测试是否能正常工作
     * - API密钥更新后，验证新密钥是否有效
     * - 排查AI功能异常，确认是否是模型连接问题
     * 
     * 📥 参数说明：
     * @param id 要测试的模型配置ID
     * 
     * 📤 返回结果示例：
     * // 测试成功
     * {
     *   "success": true,
     *   "message": "连接成功",
     *   "responseTime": 1250,                   // 响应时间（毫秒）
     *   "testQuestion": "你好",
     *   "testAnswer": "你好！我是AI助手，很高兴为你服务。",
     *   "timestamp": "2024-01-15 10:30:25"
     * }
     * 
     * // 测试失败
     * {
     *   "success": false,
     *   "message": "API密钥无效",
     *   "errorCode": "INVALID_API_KEY",
     *   "errorDetail": "The API key is invalid or has expired",
     *   "timestamp": "2024-01-15 10:30:25"
     * }
     * 
     * ⚠️ 注意事项：
     * - 测试会调用真实的AI接口，可能消耗API额度
     * - 如果API限流，测试可能失败
     * - 建议配置后立即测试，避免实际使用时才发现问题
     * 
     * @return 测试结果信息（包含成功状态、响应时间、错误信息等）
     */
    Map<String, Object> testConnection(Long id);
}
