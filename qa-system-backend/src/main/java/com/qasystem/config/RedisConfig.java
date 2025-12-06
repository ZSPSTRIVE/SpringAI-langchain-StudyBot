package com.qasystem.config;

// Jackson库 - 用于将Java对象转换为JSON格式的工具库
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
// Java 8时间API的Jackson支持（LocalDateTime等）
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// Spring的Bean注解
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Redis连接工厂
import org.springframework.data.redis.connection.RedisConnectionFactory;
// Redis操作模板
import org.springframework.data.redis.core.RedisTemplate;
// Redis序列化器
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 🗄️ Redis配置类 - 配置Redis缓存的连接和操作方式
 * 
 * 📖 功能说明：
 * Redis是一个高性能的内存数据库，本配置类负责配置Redis连接和序列化方式，
 * 为师生答疑系统提供缓存支持。主要功能包括：
 * 1. 连接配置 - 设置Redis服务器的连接参数
 * 2. 序列化配置 - 定义Java对象与Redis存储格式的转换规则
 * 3. 模板配置 - 创建RedisTemplate实例，提供便捷的Redis操作API
 * 
 * 🚀 Redis优势：
 * - 高性能：内存存储，读写速度极快（10万+QPS）
 * - 丰富数据类型：支持String、Hash、List、Set、ZSet等
 * - 持久化：支持RDB和AOF两种持久化方式
 * - 分布式：支持主从复制、哨兵模式、集群模式
 * 
 * 🎯 应用场景：
 * 1. 会话缓存 - 存储用户登录状态，提高认证效率
 * 2. 热点数据缓存 - 缓存频繁访问的问题、答案等
 * 3. 分布式锁 - 防止并发操作导致的数据不一致
 * 4. 限流控制 - 基于Redis实现API调用频率限制
 * 5. 消息队列 - 使用List或Stream实现轻量级消息队列
 * 
 * 🔧 技术实现：
 * - 使用Spring Data Redis框架简化Redis操作
 * - 采用JSON序列化存储对象，便于跨语言访问
 * - 配置连接池管理Redis连接，提高资源利用率
 * - 设置合理的序列化策略，确保数据一致性
 * 
 * 📋 配置要点：
 * - Key使用String序列化，确保可读性
 * - Value使用JSON序列化，支持复杂对象存储
 * - 启用Java 8时间模块，支持LocalDateTime等类型
 * - 激活默认类型信息，确保反序列化正确性
 * 
 * ⚠️ 注意事项：
 * - 确保Redis服务器已启动并可访问
 * - 生产环境应配置Redis密码和连接池参数
 * - 大对象存储前考虑内存占用和序列化开销
 * - 敏感数据存储前应进行加密处理
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 */
@Configuration  // 标识为Spring配置类，允许在类中定义Bean
public class RedisConfig {

    /**
     * 🏭 创建RedisTemplate Bean - Redis操作的核心工具类
     * 
     * 📖 功能说明：
     * RedisTemplate是Spring Data Redis提供的核心操作类，封装了Redis的各种操作。
     * 通过配置不同的序列化器，可以灵活地存储和读取不同类型的数据。
     * 
     * 🔧 序列化策略：
     * 1. Key序列化 - 使用StringRedisSerializer，确保Key的可读性
     * 2. Value序列化 - 使用Jackson2JsonRedisSerializer，支持复杂对象
     * 3. HashKey序列化 - 使用StringRedisSerializer，保持Hash结构Key的一致性
     * 4. HashValue序列化 - 使用Jackson2JsonRedisSerializer，支持Hash中存储对象
     * 
     * 🌐 ObjectMapper配置：
     * - 设置所有字段可见，包括private和protected字段
     * - 启用默认类型信息，确保反序列化时能正确识别类型
     * - 注册Java 8时间模块，支持LocalDateTime等时间类型
     * 
     * 📋 使用示例：
     * ```java
     * // 注入RedisTemplate
     * @Autowired
     * private RedisTemplate<String, Object> redisTemplate;
     * 
     * // 存储字符串
     * redisTemplate.opsForValue().set("key", "value");
     * 
     * // 存储对象
     * User user = new User("张三", 20);
     * redisTemplate.opsForValue().set("user:1", user);
     * 
     * // 存储Hash
     * redisTemplate.opsForHash().put("user:1:info", "name", "张三");
     * redisTemplate.opsForHash().put("user:1:info", "age", 20);
     * 
     * // 设置过期时间
     * redisTemplate.expire("key", 30, TimeUnit.MINUTES);
     * ```
     * 
     * 🔄 数据流转过程：
     * 1. 存储时：Java对象 → Jackson序列化 → JSON字符串 → Redis
     * 2. 读取时：Redis → JSON字符串 → Jackson反序列化 → Java对象
     * 
     * @param factory Redis连接工厂，由Spring自动注入，用于创建Redis连接
     * @return 配置好的RedisTemplate实例，支持Key为String、Value为Object的操作
     */
    @Bean  // 告诉Spring这个方法返回的对象需要被管理，其他地方可以注入使用
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建RedisTemplate对象
        // <String, Object>：Key的类型是String，Value的类型是Object（可以存任何类型的数据）
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        // 设置连接工厂 - 告诉RedisTemplate如何连接到Redis服务器
        template.setConnectionFactory(factory);

        // ==================== 配置序列化器 ====================
        
        // 步骤1：创建 ObjectMapper（Jackson的核心工具，负责Java对象和JSON互转）
        ObjectMapper mapper = new ObjectMapper();
        
        // 设置可见性 - 允许序列化所有的字段（包括private、protected等）
        // PropertyAccessor.ALL：所有属性
        // JsonAutoDetect.Visibility.ANY：任何可见性的字段都可以访问
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 启用默认类型 - 在JSON中包含类型信息
        // 作用：反序列化时能知道原始的Java类型
        // 例如：{"@class":"com.qasystem.entity.User", "name":"zhangsan"}
        // NON_FINAL：除了final类以外的所有类
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        
        // 注册Java 8时间模块 - 支持LocalDateTime、LocalDate等类型的序列化
        // 没有这个，保存LocalDateTime会报错
        mapper.registerModule(new JavaTimeModule());
        
        // 步骤2：创建 JSON 序列化器
        // 这个序列化器会把Java对象转换成JSON字符串存入Redis
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // 步骤3：创建 String 序列化器
        // 用于序列化Key（Key一般都是字符串类型）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // ==================== 设置各种数据类型的序列化方式 ====================
        
        // 设置普通的Key序列化方式：使用String序列化
        // 例如：redisTemplate.opsForValue().set("name", "zhangsan")
        // Key "name" 会用String方式序列化
        template.setKeySerializer(stringSerializer);
        
        // 设置Hash结构的Key序列化方式：使用String序列化
        // Hash是Redis的一种数据结构，就像Java的Map：Map<String, Object>
        // 例如：redisTemplate.opsForHash().put("user:1", "name", "zhangsan")
        // "user:1" 是Key，"name" 是HashKey
        template.setHashKeySerializer(stringSerializer);
        
        // 设置普通的Value序列化方式：使用JSON序列化
        // Value可能是对象，所以用JSON方式保存，方便阅读和反序列化
        template.setValueSerializer(serializer);
        
        // 设置Hash结构的Value序列化方式：使用JSON序列化
        // Hash的Value也用JSON方式保存
        template.setHashValueSerializer(serializer);

        // 初始化RedisTemplate，应用所有的配置
        // 必须调用这个方法，否则配置不会生效
        template.afterPropertiesSet();
        
        // 返回配置好的RedisTemplate
        // 其他地方可以通过 @Autowired 注入使用
        return template;
    }
    
    // ==================== 使用示例 ====================
    // 在Service类中注入并使用：
    // @Autowired
    // private RedisTemplate<String, Object> redisTemplate;
    //
    // // 存储数据
    // redisTemplate.opsForValue().set("user:1", userObject, 30, TimeUnit.MINUTES);
    //
    // // 获取数据
    // User user = (User) redisTemplate.opsForValue().get("user:1");
    //
    // // 删除数据
    // redisTemplate.delete("user:1");
}

