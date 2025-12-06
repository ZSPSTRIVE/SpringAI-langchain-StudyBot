package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文档查重与AI降重配置表
 */
@Data
@TableName("doc_config")
public class DocConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键，例如 similarity.minhash.threshold
     */
    private String configKey;

    /**
     * 配置值（字符串形式，具体解析由业务层处理）
     */
    private String configValue;

    /**
     * 配置说明
     */
    private String description;
}
