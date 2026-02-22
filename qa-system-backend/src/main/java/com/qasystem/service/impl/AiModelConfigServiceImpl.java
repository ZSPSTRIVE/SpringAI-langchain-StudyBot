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
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 模型配置服务实现。
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
                        .orderByDesc(AiModelConfig::getUpdatedAt)
                        .orderByDesc(AiModelConfig::getCreatedAt)
        );
    }

    @Override
    public AiModelConfig getActiveConfig() {
        List<AiModelConfig> activeConfigs = aiModelConfigMapper.selectList(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getIsActive, true)
                        .eq(AiModelConfig::getEnabled, true)
                        .orderByDesc(AiModelConfig::getUpdatedAt)
                        .orderByDesc(AiModelConfig::getId)
        );

        if (activeConfigs.size() > 1) {
            log.warn("Detected multiple active AI model configs. size={}", activeConfigs.size());
        }
        return activeConfigs.isEmpty() ? null : activeConfigs.get(0);
    }

    @Override
    @Transactional
    public AiModelConfig saveOrUpdate(AiModelConfig config) {
        if (config == null) {
            throw new RuntimeException("模型配置不能为空");
        }

        if (config.getId() == null) {
            validateRequiredFields(config);
            if (config.getEnabled() == null) {
                config.setEnabled(true);
            }
            if (config.getIsActive() == null) {
                config.setIsActive(false);
            }
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            aiModelConfigMapper.insert(config);

            if (Boolean.TRUE.equals(config.getIsActive())) {
                setActive(config.getId());
            }
            return config;
        }

        AiModelConfig existed = aiModelConfigMapper.selectById(config.getId());
        if (existed == null) {
            throw new RuntimeException("模型配置不存在");
        }

        mergeConfig(existed, config);
        validateConfigCompleteness(existed);
        existed.setUpdatedAt(LocalDateTime.now());
        aiModelConfigMapper.updateById(existed);

        if (Boolean.TRUE.equals(config.getIsActive())) {
            setActive(existed.getId());
        }
        return existed;
    }

    @Override
    @Transactional
    public void setActive(Long id) {
        if (id == null) {
            throw new RuntimeException("模型ID不能为空");
        }

        AiModelConfig target = aiModelConfigMapper.selectById(id);
        if (target == null) {
            throw new RuntimeException("模型配置不存在");
        }
        if (!Boolean.TRUE.equals(target.getEnabled())) {
            throw new RuntimeException("禁用模型不能设置为激活状态");
        }

        aiModelConfigMapper.update(null,
                new LambdaUpdateWrapper<AiModelConfig>()
                        .set(AiModelConfig::getIsActive, false)
                        .set(AiModelConfig::getUpdatedAt, LocalDateTime.now())
                        .eq(AiModelConfig::getIsActive, true)
        );

        int affected = aiModelConfigMapper.update(null,
                new LambdaUpdateWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getId, id)
                        .set(AiModelConfig::getIsActive, true)
                        .set(AiModelConfig::getUpdatedAt, LocalDateTime.now())
        );

        if (affected == 0) {
            throw new RuntimeException("激活模型失败");
        }
    }

    @Override
    public void delete(Long id) {
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }
        if (Boolean.TRUE.equals(config.getIsActive())) {
            throw new RuntimeException("不能删除当前激活的模型配置");
        }
        aiModelConfigMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> getSupportedProviders() {
        List<Map<String, Object>> providers = new ArrayList<>();

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

        Map<String, Object> deepseek = new HashMap<>();
        deepseek.put("provider", "deepseek");
        deepseek.put("providerName", "DeepSeek");
        deepseek.put("baseUrl", "https://api.deepseek.com/v1");
        deepseek.put("models", Arrays.asList(
                createModel("deepseek-chat", "DeepSeek Chat"),
                createModel("deepseek-coder", "DeepSeek Coder")
        ));
        providers.add(deepseek);

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

        Map<String, Object> minimax = new HashMap<>();
        minimax.put("provider", "minimax");
        minimax.put("providerName", "MiniMax");
        minimax.put("baseUrl", "https://api.minimax.chat/v1");
        minimax.put("models", Arrays.asList(
                createModel("abab5.5-chat", "abab5.5-chat"),
                createModel("abab6-chat", "abab6-chat")
        ));
        providers.add(minimax);

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

    @Override
    public Map<String, Object> testConnection(Long id) {
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }

        validateConfigCompleteness(config);

        Map<String, Object> result = new HashMap<>();
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .baseUrl(config.getBaseUrl())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature() == null ? 0.7 : config.getTemperature())
                    .maxTokens(config.getMaxTokens() == null ? 128 : config.getMaxTokens())
                    .timeout(Duration.ofSeconds(30))
                    .logRequests(false)
                    .logResponses(false)
                    .build();

            Response<AiMessage> response = model.generate(UserMessage.from("你好，请回复'测试成功'"));

            result.put("success", true);
            result.put("message", "连接测试成功");
            result.put("response", response.content().text());
            result.put("tokensUsed", response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0);
        } catch (Exception ex) {
            log.error("AI model connection test failed, modelId={}", id, ex);
            result.put("success", false);
            result.put("message", "连接测试失败: " + ex.getMessage());
        }

        return result;
    }

    private void validateRequiredFields(AiModelConfig config) {
        if (config == null) {
            throw new RuntimeException("模型配置不能为空");
        }
        if (!StringUtils.hasText(config.getProvider())) {
            throw new RuntimeException("provider 不能为空");
        }
        if (!StringUtils.hasText(config.getModelName())) {
            throw new RuntimeException("modelName 不能为空");
        }
        if (!StringUtils.hasText(config.getApiKey())) {
            throw new RuntimeException("apiKey 不能为空");
        }
        if (!StringUtils.hasText(config.getBaseUrl())) {
            throw new RuntimeException("baseUrl 不能为空");
        }
    }

    private void validateConfigCompleteness(AiModelConfig config) {
        if (!StringUtils.hasText(config.getApiKey())
                || !StringUtils.hasText(config.getBaseUrl())
                || !StringUtils.hasText(config.getModelName())) {
            throw new RuntimeException("模型配置不完整，请检查 apiKey/baseUrl/modelName");
        }
    }

    private void mergeConfig(AiModelConfig target, AiModelConfig source) {
        if (StringUtils.hasText(source.getProvider())) {
            target.setProvider(source.getProvider());
        }
        if (StringUtils.hasText(source.getProviderName())) {
            target.setProviderName(source.getProviderName());
        }
        if (StringUtils.hasText(source.getModelName())) {
            target.setModelName(source.getModelName());
        }
        if (StringUtils.hasText(source.getModelDisplayName())) {
            target.setModelDisplayName(source.getModelDisplayName());
        }
        if (StringUtils.hasText(source.getApiKey())) {
            target.setApiKey(source.getApiKey());
        }
        if (StringUtils.hasText(source.getBaseUrl())) {
            target.setBaseUrl(source.getBaseUrl());
        }
        if (source.getTemperature() != null) {
            target.setTemperature(source.getTemperature());
        }
        if (source.getMaxTokens() != null) {
            target.setMaxTokens(source.getMaxTokens());
        }
        if (StringUtils.hasText(source.getDefaultRewriteStyle())) {
            target.setDefaultRewriteStyle(source.getDefaultRewriteStyle());
        }
        if (source.getEnabled() != null) {
            target.setEnabled(source.getEnabled());
        }
        if (source.getIsActive() != null) {
            target.setIsActive(source.getIsActive());
        }
        if (source.getRemark() != null) {
            target.setRemark(source.getRemark());
        }
    }

    private Map<String, String> createModel(String modelName, String displayName) {
        Map<String, String> model = new HashMap<>();
        model.put("modelName", modelName);
        model.put("displayName", displayName);
        return model;
    }
}
