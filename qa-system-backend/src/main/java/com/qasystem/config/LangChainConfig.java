package com.qasystem.config;

import com.qasystem.entity.AiModelConfig;
import com.qasystem.service.AiModelConfigService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 🤖 LangChain4j配置类 - AI模型集成与动态切换支持
 * 
 * 📖 功能说明：
 * 本配置类负责集成LangChain4j框架，为师生答疑系统提供AI模型支持。
 * LangChain4j是一个Java版的AI应用开发框架，简化了与各种AI模型的交互。
 * 主要功能包括：
 * 1. 模型配置管理 - 支持从数据库或配置文件读取AI模型参数
 * 2. 动态模型切换 - 可在运行时切换不同的AI模型
 * 3. 多提供商支持 - 兼容OpenAI、Azure OpenAI等API格式
 * 4. 参数灵活配置 - 支持温度、最大令牌数等参数调整
 * 
 * 🔧 技术实现：
 * - 使用Spring的@Configuration注解标识为配置类
 * - 通过@Bean方法创建ChatLanguageModel实例
 * - 优先使用数据库配置，回退到配置文件默认值
 * - 使用OpenAI兼容API格式，支持多种AI服务提供商
 * 
 * 📋 配置优先级：
 * 1. 数据库中的激活配置（最高优先级）
 * 2. application.properties/application.yml中的配置
 * 3. 代码中的硬编码默认值（最低优先级）
 * 
 * 🌐 支持的AI提供商：
 * - OpenAI官方API
 * - SiliconFlow（国内访问友好）
 * - Azure OpenAI服务
 * - 其他OpenAI兼容的API服务
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Slf4j  // 自动生成日志对象，用于记录配置加载过程
@Configuration  // 标识为Spring配置类，允许在类中定义Bean
@RequiredArgsConstructor  // 为final字段生成构造函数，实现依赖注入
public class LangChainConfig {
    
    /**
     * AI模型配置服务接口 - 用于从数据库读取模型配置
     * 提供对AI模型配置的CRUD操作，包括：
     * - 获取当前激活的模型配置
     * - 查询所有可用的模型配置
     * - 创建、更新、删除模型配置
     */
    private final AiModelConfigService aiModelConfigService;
    
    // ==================== 默认配置参数 ====================
    // 以下参数从application.properties或application.yml文件读取
    // 当数据库中没有配置时，使用这些默认值
    
    /**
     * 🔑 OpenAI API密钥 - 用于身份验证
     * 从配置文件读取，默认为空字符串
     * 实际使用时需要配置有效的API密钥
     * 
     * 配置示例：langchain4j.open-ai.api-key=sk-xxxxxxxxxxxxxxxxxxxx
     */
    @Value("${langchain4j.open-ai.api-key:}")
    private String defaultApiKey;
    
    /**
     * 🌐 API基础URL - AI服务的访问地址
     * 默认使用SiliconFlow的服务地址，国内访问更稳定
     * 可以替换为OpenAI官方地址或其他兼容服务
     * 
     * 配置示例：
     * - OpenAI官方：https://api.openai.com/v1
     * - SiliconFlow：https://api.siliconflow.cn/v1
     * - Azure OpenAI：https://your-resource.openai.azure.com/
     */
    @Value("${langchain4j.open-ai.base-url:https://api.siliconflow.cn/v1}")
    private String defaultBaseUrl;
    
    /**
     * 🧠 AI模型名称 - 指定使用的具体模型
     * 默认使用Qwen2.5-7B-Instruct，这是阿里云通义千问的7B参数模型
     * 支持替换为其他兼容OpenAI API格式的模型
     * 
     * 常用模型示例：
     * - gpt-3.5-turbo：OpenAI的GPT-3.5模型
     * - gpt-4：OpenAI的GPT-4模型
     * - Qwen/Qwen2.5-7B-Instruct：通义千问7B指令微调模型
     * - deepseek-chat：深度求索的对话模型
     */
    @Value("${langchain4j.open-ai.model-name:Qwen/Qwen2.5-7B-Instruct}")
    private String defaultModelName;
    
    /**
     * 🌡️ 温度参数 - 控制AI回复的随机性和创造性
     * 取值范围：0.0-2.0，默认0.7
     * 
     * 参数含义：
     * - 0.0：最确定性的回答，适合事实性问答
     * - 0.7：平衡创造性和准确性，适合通用对话
     * - 1.0-2.0：更高的创造性，适合创意写作
     * 
     * 对于教育场景，建议使用0.3-0.7之间的值，确保回答准确性的同时保留一定的灵活性
     */
    @Value("${langchain4j.open-ai.temperature:0.7}")
    private Double defaultTemperature;
    
    /**
     * 📊 最大令牌数 - 限制AI回复的最大长度
     * 默认2000，约等于1500-2000个中文字符
     * 
     * 参数说明：
     * - 1个令牌约等于0.75个英文单词或0.5个中文字符
     * - 包括输入和输出的总令牌数
     * - 设置过小可能导致回答不完整
     * - 设置过大会增加API调用成本
     * 
     * 教育场景建议：
     * - 简单问答：500-1000
     * - 详细解释：1000-2000
     * - 代码示例：1500-2500
     */
    @Value("${langchain4j.open-ai.max-tokens:2000}")
    private Integer defaultMaxTokens;
    
    /**
     * 🏭 创建ChatLanguageModel Bean - AI聊天模型的Spring Bean
     * 
     * 业务流程：
     * 1. 尝试从数据库获取当前激活的AI模型配置
     * 2. 如果获取失败或没有配置，使用默认配置
     * 3. 根据配置参数构建OpenAiChatModel实例
     * 4. 设置超时时间、日志等附加参数
     * 5. 返回配置好的ChatLanguageModel Bean
     * 
     * 为什么使用OpenAiChatModel？
     * - OpenAI API已成为事实上的行业标准
     * - 多家AI服务提供商提供兼容接口
     * - LangChain4j对OpenAI格式支持最完善
     * - 便于在不同提供商之间切换
     * 
     * @return 配置好的ChatLanguageModel实例，用于AI对话服务
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // 优先从数据库读取激活的模型配置
        AiModelConfig activeConfig = null;
        try {
            activeConfig = aiModelConfigService.getActiveConfig();
        } catch (Exception e) {
            // 记录警告日志，但不中断Bean创建过程
            // 这样即使数据库配置有问题，系统仍能使用默认配置启动
            log.warn("获取数据库AI模型配置失败，使用默认配置: {}", e.getMessage());
        }
        
        // 初始化配置变量为默认值
        String apiKey = defaultApiKey;
        String baseUrl = defaultBaseUrl;
        String modelName = defaultModelName;
        Double temperature = defaultTemperature;
        Integer maxTokens = defaultMaxTokens;
        
        // 如果数据库中有激活配置，则使用数据库配置覆盖默认值
        if (activeConfig != null) {
            log.info("使用数据库AI模型配置: {} - {}", 
                    activeConfig.getProviderName(), activeConfig.getModelDisplayName());
            apiKey = activeConfig.getApiKey();
            baseUrl = activeConfig.getBaseUrl();
            modelName = activeConfig.getModelName();
            temperature = activeConfig.getTemperature();
            maxTokens = activeConfig.getMaxTokens();
        } else {
            log.info("使用默认AI模型配置: {}", defaultModelName);
        }
        
        // 使用建造者模式创建OpenAiChatModel实例
        // 建造者模式提供了清晰的配置方式，易于理解和维护
        return OpenAiChatModel.builder()
                .apiKey(apiKey)  // 设置API密钥
                .baseUrl(baseUrl)  // 设置API基础URL
                .modelName(modelName)  // 设置模型名称
                .temperature(temperature)  // 设置温度参数
                .maxTokens(maxTokens)  // 设置最大令牌数
                .timeout(Duration.ofSeconds(60))  // 设置请求超时时间为60秒
                .logRequests(false)  // 关闭请求日志，避免敏感信息泄露
                .logResponses(false)  // 关闭响应日志，避免敏感信息泄露
                .build();
    }
    
    /**
     * 🌊 创建StreamingChatLanguageModel Bean - 流式AI聊天模型
     * 
     * 用于SSE流式对话，实现打字机效果的实时响应。
     * 配置与ChatLanguageModel保持一致，但使用流式API。
     * 
     * @return 配置好的StreamingChatLanguageModel实例
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        // 优先从数据库读取激活的模型配置
        AiModelConfig activeConfig = null;
        try {
            activeConfig = aiModelConfigService.getActiveConfig();
        } catch (Exception e) {
            log.warn("获取数据库AI模型配置失败，使用默认配置: {}", e.getMessage());
        }
        
        // 初始化配置变量为默认值
        String apiKey = defaultApiKey;
        String baseUrl = defaultBaseUrl;
        String modelName = defaultModelName;
        Double temperature = defaultTemperature;
        Integer maxTokens = defaultMaxTokens;
        
        // 如果数据库中有激活配置，则使用数据库配置覆盖默认值
        if (activeConfig != null) {
            log.info("使用数据库AI模型配置(流式): {} - {}", 
                    activeConfig.getProviderName(), activeConfig.getModelDisplayName());
            apiKey = activeConfig.getApiKey();
            baseUrl = activeConfig.getBaseUrl();
            modelName = activeConfig.getModelName();
            temperature = activeConfig.getTemperature();
            maxTokens = activeConfig.getMaxTokens();
        } else {
            log.info("使用默认AI模型配置(流式): {}", defaultModelName);
        }
        
        // 创建流式聊天模型
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(120))  // 流式响应需要更长超时
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}

