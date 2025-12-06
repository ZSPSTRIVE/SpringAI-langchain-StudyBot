package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qasystem.entity.AiModelConfig;
import com.qasystem.mapper.AiModelConfigMapper;
import com.qasystem.service.AiModelConfigService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI模型配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelConfigServiceImpl implements AiModelConfigService {
    
    private final AiModelConfigMapper aiModelConfigMapper;
    
    @Override
    public List<AiModelConfig> listAll() {
        return aiModelConfigMapper.selectList(
                new LambdaQueryWrapper<AiModelConfig>()
                        .orderByDesc(AiModelConfig::getIsActive)
                        .orderByDesc(AiModelConfig::getCreatedAt)
        );
    }
    
    @Override
    public AiModelConfig getActiveConfig() {
        return aiModelConfigMapper.selectOne(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getIsActive, true)
                        .eq(AiModelConfig::getEnabled, true)
        );
    }
    
    @Override
    @Transactional
    public AiModelConfig saveOrUpdate(AiModelConfig config) {
        if (config.getId() == null) {
            // 新增
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            if (config.getEnabled() == null) {
                config.setEnabled(true);
            }
            if (config.getIsActive() == null) {
                config.setIsActive(false);
            }
            aiModelConfigMapper.insert(config);
        } else {
            // 更新
            config.setUpdatedAt(LocalDateTime.now());
            aiModelConfigMapper.updateById(config);
        }
        return config;
    }
    
    @Override
    @Transactional
    public void setActive(Long id) {
        // 先将所有模型设为非激活
        aiModelConfigMapper.update(null,
                new LambdaUpdateWrapper<AiModelConfig>()
                        .set(AiModelConfig::getIsActive, false)
        );
        
        // 设置指定模型为激活
        aiModelConfigMapper.update(null,
                new LambdaUpdateWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getId, id)
                        .set(AiModelConfig::getIsActive, true)
                        .set(AiModelConfig::getUpdatedAt, LocalDateTime.now())
        );
    }
    
    @Override
    public void delete(Long id) {
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config != null && Boolean.TRUE.equals(config.getIsActive())) {
            throw new RuntimeException("不能删除当前激活的模型配置");
        }
        aiModelConfigMapper.deleteById(id);
    }
    
    @Override
    public List<Map<String, Object>> getSupportedProviders() {
        List<Map<String, Object>> providers = new ArrayList<>();
        
        // 硅基流动
        Map<String, Object> siliconflow = new HashMap<>();
        siliconflow.put("provider", "siliconflow");
        siliconflow.put("providerName", "硅基流动");
        siliconflow.put("baseUrl", "https://api.siliconflow.cn/v1");
        siliconflow.put("models", Arrays.asList(
                createModel("Qwen/Qwen2.5-7B-Instruct", "通义千问 2.5-7B"),
                createModel("Qwen/Qwen2-72B-Instruct", "通义千问 2-72B"),
                createModel("deepseek-ai/DeepSeek-V2.5", "DeepSeek V2.5")
        ));
        providers.add(siliconflow);
        
        // DeepSeek
        Map<String, Object> deepseek = new HashMap<>();
        deepseek.put("provider", "deepseek");
        deepseek.put("providerName", "DeepSeek");
        deepseek.put("baseUrl", "https://api.deepseek.com/v1");
        deepseek.put("models", Arrays.asList(
                createModel("deepseek-chat", "DeepSeek Chat"),
                createModel("deepseek-coder", "DeepSeek Coder")
        ));
        providers.add(deepseek);
        
        // 阿里通义千问
        Map<String, Object> qwen = new HashMap<>();
        qwen.put("provider", "qwen");
        qwen.put("providerName", "阿里通义千问");
        qwen.put("baseUrl", "https://dashscope.aliyuncs.com/compatible-mode/v1");
        qwen.put("models", Arrays.asList(
                createModel("qwen-turbo", "通义千问 Turbo"),
                createModel("qwen-plus", "通义千问 Plus"),
                createModel("qwen-max", "通义千问 Max")
        ));
        providers.add(qwen);
        
        // MiniMax
        Map<String, Object> minimax = new HashMap<>();
        minimax.put("provider", "minimax");
        minimax.put("providerName", "MiniMax");
        minimax.put("baseUrl", "https://api.minimax.chat/v1");
        minimax.put("models", Arrays.asList(
                createModel("abab5.5-chat", "abab5.5-chat"),
                createModel("abab6-chat", "abab6-chat")
        ));
        providers.add(minimax);
        
        // 智谱 AI
        Map<String, Object> zhipu = new HashMap<>();
        zhipu.put("provider", "zhipu");
        zhipu.put("providerName", "智谱 AI");
        zhipu.put("baseUrl", "https://open.bigmodel.cn/api/paas/v4");
        zhipu.put("models", Arrays.asList(
                createModel("glm-4", "GLM-4"),
                createModel("glm-4-flash", "GLM-4-Flash"),
                createModel("glm-3-turbo", "GLM-3-Turbo")
        ));
        providers.add(zhipu);
        
        // Moonshot (Kimi)
        Map<String, Object> kimi = new HashMap<>();
        kimi.put("provider", "kimi");
        kimi.put("providerName", "月之暗面 (Kimi)");
        kimi.put("baseUrl", "https://api.moonshot.cn/v1");
        kimi.put("models", Arrays.asList(
                createModel("moonshot-v1-8k", "Kimi 8K"),
                createModel("moonshot-v1-32k", "Kimi 32K"),
                createModel("moonshot-v1-128k", "Kimi 128K")
        ));
        providers.add(kimi);
        
        return providers;
    }
    
    private Map<String, String> createModel(String modelName, String displayName) {
        Map<String, String> model = new HashMap<>();
        model.put("modelName", modelName);
        model.put("displayName", displayName);
        return model;
    }
    
    @Override
    public Map<String, Object> testConnection(Long id) {
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 创建临时模型实例进行测试
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .baseUrl(config.getBaseUrl())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature())
                    .maxTokens(config.getMaxTokens())
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            // 发送测试消息
            Response<AiMessage> response = model.generate(
                    UserMessage.from("你好，请回复'测试成功'")
            );
            
            result.put("success", true);
            result.put("message", "连接测试成功");
            result.put("response", response.content().text());
            result.put("tokensUsed", response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0);
        } catch (Exception e) {
            log.error("AI模型连接测试失败", e);
            result.put("success", false);
            result.put("message", "连接测试失败: " + e.getMessage());
        }
        
        return result;
    }
}
