package com.qasystem.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 🗃️ MyBatis-Plus配置类 - 数据库访问层框架配置
 * 
 * 📖 功能说明：
 * 本配置类负责MyBatis-Plus框架的核心配置，解决Spring Boot 3兼容性问题。
 * MyBatis-Plus是MyBatis的增强工具，在MyBatis的基础上只做增强不做改变，
 * 为简化开发、提高效率而生。主要功能包括：
 * 1. 内置分页插件 - 基于MyBatis物理分页，开发者无需关心具体操作
 * 2. 代码生成器 - 减少大量重复代码编写
 * 3. 条件构造器 - 通过Lambda表达式轻松构建复杂查询条件
 * 4. 自动填充功能 - 自动处理创建时间、更新时间等公共字段
 * 5. 逻辑删除 - 实现数据恢复、数据保护等需求
 * 
 * 🔧 技术实现：
 * - 手动配置SqlSessionFactory解决Spring Boot 3兼容性问题
 * - 使用MybatisSqlSessionFactoryBean替代默认的SqlSessionFactoryBean
 * - 配置分页插件、逻辑删除、自动填充等增强功能
 * - 设置驼峰命名转换，提高代码可读性
 * 
 * 📋 配置要点：
 * - @Primary注解确保此SqlSessionFactory优先级高于自动配置
 * - 分页插件限制最大查询条数为500，防止大查询影响性能
 * - 逻辑删除使用deleted字段，1表示已删除，0表示未删除
 * - 自动填充createTime和updateTime字段，简化业务代码
 * 
 * ⚠️ 注意事项：
 * - 此配置是为了解决Spring Boot 3与MyBatis-Plus的兼容性问题
 * - 如果升级MyBatis-Plus到兼容Spring Boot 3的版本，可以简化此配置
 * - 分页查询最大限制可根据业务需求调整
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Configuration  // 标识为Spring配置类，允许在类中定义Bean
public class MyBatisPlusConfig {

    /**
     * 🏭 创建SqlSessionFactory Bean - MyBatis-Plus核心会话工厂
     * 
     * 🔍 问题背景：
     * Spring Boot 3与旧版本MyBatis-Plus存在兼容性问题，
     * 自动配置的SqlSessionFactory可能导致启动失败或功能异常。
     * 手动配置可以确保MyBatis-Plus的所有功能正常工作。
     * 
     * 🛠️ 解决方案：
     * 使用MybatisSqlSessionFactoryBean替代默认的SqlSessionFactoryBean，
     * 并手动配置所有必要的组件，确保与Spring Boot 3兼容。
     * 
     * 📋 配置详情：
     * 1. 数据源配置 - 使用注入的DataSource
     * 2. Mapper XML位置 - 扫描classpath*:/mapper/** /.xml
     * 3. 类型别名包 - 自动扫描com.qasystem.entity包下的实体类
     * 4. 分页插件 - 配置MySQL分页，限制最大查询条数
     * 5. 全局配置 - 设置ID自增、逻辑删除、自动填充
     * 6. MyBatis配置 - 启用驼峰命名转换，禁用缓存
     * 
     * @param dataSource 数据源，由Spring自动注入
     * @return 配置好的SqlSessionFactory实例
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean
    @Primary  // 设置为主要Bean，优先级高于Spring Boot自动配置
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        DbType dbType = resolveDbType(dataSource);
        // 使用MybatisSqlSessionFactoryBean替代默认的SqlSessionFactoryBean
        // 这是解决Spring Boot 3兼容性问题的关键
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        
        // 配置Mapper XML文件位置
        // classpath*:/mapper/**/*.xml 表示扫描所有jar包和classes目录下的mapper目录
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath*:/mapper/**/*.xml"));
        
        // 配置实体类别名包
        // 设置后，在Mapper XML中可以直接使用类名，无需写全限定名
        sqlSessionFactoryBean.setTypeAliasesPackage("com.qasystem.entity");
        
        // 配置MyBatis-Plus插件
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件配置
        PaginationInnerInterceptor paginationInnerInterceptor =
                dbType == DbType.OTHER ? new PaginationInnerInterceptor() : new PaginationInnerInterceptor(dbType);
        paginationInnerInterceptor.setOverflow(false);  // 禁用分页溢出处理（查询第100页只有10页数据时，不返回最后一页数据）
        paginationInnerInterceptor.setMaxLimit(500L);  // 限制最大单次查询条数为500，防止大查询影响性能
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        sqlSessionFactoryBean.setPlugins(interceptor);
        
        // 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setBanner(false);  // 关闭MyBatis-Plus启动横幅
        
        // 数据库配置
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        applyIdentifierFormat(dbConfig, dbType);
        dbConfig.setIdType(com.baomidou.mybatisplus.annotation.IdType.AUTO);  // 设置ID自增策略为数据库自增
        dbConfig.setLogicDeleteField("deleted");  // 设置逻辑删除字段名
        dbConfig.setLogicDeleteValue("1");  // 设置已删除值
        dbConfig.setLogicNotDeleteValue("0");  // 设置未删除值
        globalConfig.setDbConfig(dbConfig);
        
        // 设置自动填充处理器
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        
        // MyBatis核心配置
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);  // 开启驼峰命名转换，如user_name自动映射为userName
        configuration.setCacheEnabled(false);  // 禁用二级缓存，避免分布式环境下的数据一致性问题
        sqlSessionFactoryBean.setConfiguration(configuration);
        
        // 返回配置好的SqlSessionFactory
        return sqlSessionFactoryBean.getObject();
    }

    private DbType resolveDbType(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            String normalized = productName == null ? "" : productName.toLowerCase(Locale.ROOT);
            if (normalized.contains("postgresql")) {
                return DbType.POSTGRE_SQL;
            }
            if (normalized.contains("mysql")) {
                return DbType.MYSQL;
            }
            return DbType.OTHER;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to detect database dialect for MyBatis-Plus", ex);
        }
    }

    private void applyIdentifierFormat(GlobalConfig.DbConfig dbConfig, DbType dbType) {
        if (dbType == DbType.POSTGRE_SQL) {
            dbConfig.setTableFormat("\"%s\"");
            dbConfig.setColumnFormat("\"%s\"");
            return;
        }
        if (dbType == DbType.MYSQL) {
            dbConfig.setTableFormat("`%s`");
            dbConfig.setColumnFormat("`%s`");
        }
    }

    /**
     * 🔄 自动填充处理器 - 自动处理公共字段
     * 
     * 📖 功能说明：
     * 实现MetaObjectHandler接口，在插入和更新操作时自动填充公共字段，
     * 减少重复代码，提高开发效率。主要功能：
     * 1. 插入时自动填充createTime和updateTime
     * 2. 更新时自动填充updateTime
     * 
     * 🔧 使用方法：
     * 在实体类字段上添加@TableField注解：
     * @TableField(fill = FieldFill.INSERT)  // 插入时填充
     * @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时填充
     * 
     * 📋 填充策略：
     * - createTime：仅在插入时填充，记录创建时间
     * - updateTime：插入和更新时都填充，记录最后修改时间
     * - 使用LocalDateTime类型，精确到秒
     * 
     * ⚠️ 注意事项：
     * - 实体类中必须有对应字段，否则填充无效
     * - 字段名必须与填充方法中的字段名完全一致
     * - 如果手动设置了值，自动填充不会覆盖
     * 
     * @author 师生答疑系统开发团队
     * @since 1.0.0
     */
    public static class MyMetaObjectHandler implements MetaObjectHandler {
        
        /**
         * 📝 插入操作自动填充
         * 
         * 在执行插入操作时，自动填充createTime和updateTime字段为当前时间。
         * 使用strictInsertFill方法确保只在字段为null时填充，避免覆盖已有值。
         * 
         * @param metaObject 元对象，包含实体对象的元信息
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            // 填充创建时间为当前时间
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            // 填充更新时间为当前时间
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        /**
         * 🔄 更新操作自动填充
         * 
         * 在执行更新操作时，自动填充updateTime字段为当前时间。
         * 使用strictUpdateFill方法确保只在字段为null时填充，避免覆盖已有值。
         * 
         * @param metaObject 元对象，包含实体对象的元信息
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            // 填充更新时间为当前时间
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}

