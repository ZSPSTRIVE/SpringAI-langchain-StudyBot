package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI模型配置实体
 */
@Data
@TableName("ai_model_config")
public class AiModelConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 厂商名称: siliconflow, deepseek, qwen, minimax, zhipu, kimi 等
     */
    private String provider;
    
    /**
     * 厂商显示名称
     */
    private String providerName;
    
    /**
     * 模型标识符
     */
    private String modelName;
    
    /**
     * 模型显示名称
     */
    private String modelDisplayName;
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * API Base URL
     */
    private String baseUrl;
    
    /**
     * 温度参数 (0.0 - 2.0)
     */
    private Double temperature;
    
    /**
     * 最大Token数
     */
    private Integer maxTokens;
    
    /**
     * 默认降重风格: ACADEMIC, FLUENCY, EXPAND, LOGIC_ENHANCE
     */
    private String defaultRewriteStyle;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 是否为当前使用的模型
     */
    private Boolean isActive;
    
    /**
     * 备注说明
     */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
