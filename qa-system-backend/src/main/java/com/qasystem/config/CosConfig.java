package com.qasystem.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 对象存储配置类
 *
 * 从 application.yml 中读取 cos.client.* 配置，
 * 如果配置完整则初始化 COSClient Bean，否则跳过（打印警告日志）。
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "cos.client")
public class CosConfig {

    private String host;
    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String appId;

    /**
     * 创建 COSClient Bean
     * 仅当 secretId 和 bucket 都配置了时才创建
     */
    @Bean
    public COSClient cosClient() {
        // 检查必要配置
        if (secretId == null || secretId.isBlank()
                || secretKey == null || secretKey.isBlank()
                || bucket == null || bucket.isBlank()) {
            log.warn("  [COS配置] secretId/secretKey/bucket 未配置，COS 对象存储不可用，文件将存储到本地");
            return null;
        }

        try {
            COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            clientConfig.setHttpProtocol(HttpProtocol.https);
            COSClient client = new COSClient(credentials, clientConfig);

            log.info("  [COS配置] 腾讯云 COS 客户端初始化成功 | region={} | bucket={}", region, bucket);
            return client;
        } catch (Exception e) {
            log.error("  [COS配置] 腾讯云 COS 客户端初始化失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
