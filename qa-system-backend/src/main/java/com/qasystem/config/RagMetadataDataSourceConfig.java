package com.qasystem.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class RagMetadataDataSourceConfig {

    @Bean(name = "dataSourceProperties")
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(
            @Qualifier("dataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "jdbcTemplate")
    @Primary
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "ragMetadataDataSourceProperties")
    @ConfigurationProperties("qa.rag.metadata.datasource")
    public DataSourceProperties ragMetadataDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "ragMetadataDataSource")
    @ConfigurationProperties("qa.rag.metadata.datasource.hikari")
    public DataSource ragMetadataDataSource(
            @Qualifier("ragMetadataDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "ragMetadataJdbcTemplate")
    public JdbcTemplate ragMetadataJdbcTemplate(
            @Qualifier("ragMetadataDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
