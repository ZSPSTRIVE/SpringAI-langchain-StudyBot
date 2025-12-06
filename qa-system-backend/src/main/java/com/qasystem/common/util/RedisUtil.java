package com.qasystem.common.util;

// Lombok的注解，自动生成包含所有final字段的构造函数
import lombok.RequiredArgsConstructor;
// Spring Data Redis的核心类，提供Redis操作的高级抽象
import org.springframework.data.redis.core.RedisTemplate;
// 标记为Spring组件
import org.springframework.stereotype.Component;

// Java集合框架
import java.util.Collection;
// 时间单位枚举，用于设置过期时间
import java.util.concurrent.TimeUnit;

/**
 * 🗄️ Redis工具类 - 提供Redis操作的便捷方法
 * 
 * 📖 功能说明：
 * 本工具类封装了RedisTemplate的常用操作，提供简单易用的API接口，
 * 用于缓存数据、会话管理、计数器、分布式锁等场景。
 * 通过Spring依赖注入获取RedisTemplate实例，简化Redis操作代码。
 * 
 * 🚀 Redis是什么？
 * Redis（Remote Dictionary Server）是一个开源的内存数据结构存储系统，
 * 可以用作数据库、缓存和消息中间件。它支持多种数据结构，如字符串、哈希、
 * 列表、集合、有序集合等，并提供了丰富的操作命令。
 * 
 * 💡 Redis的优势：
 * 1. 高性能：基于内存操作，读写速度极快（10万次/秒）
 * 2. 丰富的数据结构：支持多种数据类型，适用于不同场景
 * 3. 持久化：支持RDB和AOF两种持久化方式
 * 4. 高可用：支持主从复制、哨兵模式和集群模式
 * 5. 事务支持：支持MULTI/EXEC事务操作
 * 
 * 🎯 应用场景：
 * 1. 缓存：热点数据缓存，减轻数据库压力
 * 2. 会话管理：存储用户会话信息，支持分布式会话
 * 3. 计数器：文章阅读量、点赞数等计数场景
 * 4. 排行榜：使用有序集合实现排行榜功能
 * 5. 分布式锁：实现分布式环境下的互斥访问
 * 6. 消息队列：使用列表实现简单的消息队列
 * 
 * 🛠️ 本工具类提供的功能：
 * 1. 基本键值操作：设置、获取、删除缓存
 * 2. 过期时间管理：设置和获取键的过期时间
 * 3. 计数器操作：递增、递减计数器
 * 4. 批量操作：批量删除键
 * 5. 存在性检查：检查键是否存在
 * 
 * ⚙️ 技术实现：
 * - 使用Spring Data Redis的RedisTemplate作为底层实现
 * - 支持泛型，可以存储任意类型的对象
 * - 自动处理序列化和反序列化
 * - 提供线程安全的操作方法
 * 
 * 📋 配置要求：
 * 在application.yml中配置Redis连接信息：
 * ```yaml
 * spring:
 *   redis:
 *     host: localhost
 *     port: 6379
 *     password: your-password
 *     database: 0
 *     timeout: 3000ms
 *     lettuce:
 *       pool:
 *         max-active: 8
 *         max-wait: -1ms
 *         max-idle: 8
 *         min-idle: 0
 * ```
 * 
 * ⚠️ 注意事项：
 * - Redis是基于内存的，服务器重启后数据会丢失（除非开启持久化）
 * - 应合理设置过期时间，避免内存溢出
 * - 大量数据存储应考虑内存使用情况
 * - 生产环境应配置密码和访问控制
 * - 敏感数据不应明文存储在Redis中
 * 
 * @author 师生答疑系统开发团队
 * @since 1.0.0
 * @see <a href="https://redis.io/documentation">Redis官方文档</a>
 */
// @Component：告诉Spring这是一个组件，会被Spring管理，其他地方可以注入使用
@Component
// @RequiredArgsConstructor：Lombok注解，自动生成包含所有final字段的构造函数
// 这里会自动生成：public RedisUtil(RedisTemplate<String, Object> redisTemplate) { this.redisTemplate = redisTemplate; }
@RequiredArgsConstructor
public class RedisUtil {

    /**
     * 🔧 RedisTemplate实例 - Redis操作的核心类
     * 
     * 📖 功能说明：
     * RedisTemplate是Spring Data Redis提供的核心类，封装了Redis的各种操作。
     * 它提供了高级抽象，自动处理序列化和反序列化，支持多种Redis数据结构。
     * 
     * 🔧 技术细节：
     * - 泛型参数<String, Object>表示键为String类型，值为Object类型
     * - 自动处理Java对象与Redis存储格式之间的转换
     * - 支持多种序列化策略（默认使用JdkSerializationRedisSerializer）
     * - 提供线程安全的操作方法
     * 
     * 🔄 工作原理：
     * 1. 接收Java对象作为输入
     * 2. 使用序列化器将对象转换为字节数组
     * 3. 发送命令到Redis服务器
     * 4. 接收Redis服务器的响应
     * 5. 使用反序列化器将字节数组转换为Java对象
     * 
     * 📋 配置示例：
     * 在RedisConfig.java中配置RedisTemplate：
     * ```java
     * @Bean
     * public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
     *     RedisTemplate<String, Object> template = new RedisTemplate<>();
     *     template.setConnectionFactory(connectionFactory);
     *     // 设置序列化器
     *     template.setKeySerializer(new StringRedisSerializer());
     *     template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
     *     return template;
     * }
     * ```
     * 
     * ⚠️ 注意事项：
     * - 不同类型的RedisTemplate实例应共享同一个连接工厂
     * - 序列化策略的选择会影响性能和兼容性
     * - 大对象序列化可能消耗较多CPU和内存
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 💾 设置缓存 - 存储键值对到Redis
     * 
     * 📖 功能说明：
     * 将指定的键值对存储到Redis中，如果键已存在则覆盖原有值。
     * 此方法不设置过期时间，数据将永久保存（直到被删除或Redis重启）。
     * 
     * 🔄 工作流程：
     * 1. 接收键和值作为参数
     * 2. 使用RedisTemplate的opsForValue()获取值操作对象
     * 3. 调用set()方法将键值对存储到Redis
     * 4. Redis返回操作结果
     * 
     * 📝 参数说明：
     * @param key Redis键，用于唯一标识存储的数据，建议使用有意义的命名规则
     * @param value 要存储的值，可以是任意Java对象（需实现序列化接口）
     * 
     * 🔄 使用示例：
     * ```java
     * // 存储用户信息
     * User user = new User(1L, "张三", "STUDENT");
     * redisUtil.set("user:1", user);
     * 
     * // 存储配置信息
     * redisUtil.set("config:system", Map.of("maxUsers", 1000, "version", "1.0.0"));
     * 
     * // 存储简单字符串
     * redisUtil.set("welcome:message", "欢迎使用师生答疑系统");
     * ```
     * 
     * ⚠️ 注意事项：
     * - 键的命名建议使用冒号分隔的层级结构，如"user:1"、"config:system"
     * - 此方法不会设置过期时间，数据将永久保存
     * - 存储大对象时注意内存使用情况
     * - 键应避免使用特殊字符和空格
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * ⏰ 设置缓存并指定过期时间 - 存储带过期时间的键值对
     * 
     * 📖 功能说明：
     * 将指定的键值对存储到Redis中，并设置过期时间。
     * 过期时间到达后，Redis会自动删除该键值对，释放内存空间。
     * 这是最常用的缓存存储方式，适用于临时数据存储。
     * 
     * 🔄 工作流程：
     * 1. 接收键、值和过期时间参数
     * 2. 使用RedisTemplate的opsForValue()获取值操作对象
     * 3. 调用set(key, value, timeout, unit)方法存储数据并设置过期时间
     * 4. Redis返回操作结果
     * 5. 启动过期计时器，到期自动删除数据
     * 
     * 📝 参数说明：
     * @param key Redis键，用于唯一标识存储的数据，建议使用有意义的命名规则
     * @param value 要存储的值，可以是任意Java对象（需实现序列化接口）
     * @param timeout 过期时间的数值，与unit参数配合使用
     * @param unit 时间单位，如TimeUnit.SECONDS（秒）、TimeUnit.MINUTES（分钟）等
     * 
     * 🔄 使用示例：
     * ```java
     * // 存储验证码，5分钟过期
     * redisUtil.set("verify:code:123456", "ABC123", 5, TimeUnit.MINUTES);
     * 
     * // 存储用户会话，2小时过期
     * redisUtil.set("session:user:123", sessionData, 2, TimeUnit.HOURS);
     * 
     * // 存储热点数据，10分钟过期
     * redisUtil.set("hot:question:456", questionData, 10, TimeUnit.MINUTES);
     * 
     * // 存储临时令牌，30秒过期
     * redisUtil.set("temp:token:abc", tokenData, 30, TimeUnit.SECONDS);
     * ```
     * 
     * 📊 常见过期时间设置：
     * - 验证码：5-10分钟
     * - 用户会话：1-24小时
     * - 热点数据：5-30分钟
     * - 临时令牌：30秒-5分钟
     * - 配置缓存：1-24小时
     * 
     * ⚠️ 注意事项：
     * - 过期时间设置应根据业务需求合理选择
     * - 过短可能导致频繁缓存失效，影响性能
     * - 过长可能导致数据不一致，占用过多内存
     * - 过期时间是相对时间，从设置时刻开始计算
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 🔍 获取缓存 - 从Redis中获取指定键的值
     * 
     * 📖 功能说明：
     * 从Redis中获取指定键对应的值，并自动反序列化为指定的Java类型。
     * 如果键不存在，返回null。这是缓存读取的核心方法。
     * 
     * 🔄 工作流程：
     * 1. 接收键和目标类型作为参数
     * 2. 使用RedisTemplate的opsForValue()获取值操作对象
     * 3. 调用get(key)方法从Redis获取字节数据
     * 4. 使用反序列化器将字节数据转换为Java对象
     * 5. 将结果强制转换为指定类型并返回
     * 
     * 📝 参数说明：
     * @param <T> 泛型类型，表示返回值的类型
     * @param key Redis键，用于标识要获取的数据
     * @param clazz 目标类型的Class对象，用于类型转换
     * 
     * @return 键对应的值，如果键不存在则返回null
     * 
     * 🔄 使用示例：
     * ```java
     * // 获取用户信息
     * User user = redisUtil.get("user:1", User.class);
     * if (user != null) {
     *     System.out.println("用户名: " + user.getUsername());
     * }
     * 
     * // 获取配置信息
     * Map<String, Object> config = redisUtil.get("config:system", Map.class);
     * 
     * // 获取简单字符串
     * String message = redisUtil.get("welcome:message", String.class);
     * 
     * // 获取列表数据
     * List<Question> questions = redisUtil.get("hot:questions", List.class);
     * ```
     * 
     * 🚨 异常处理：
     * - 如果键不存在，返回null
     * - 如果数据无法反序列化为指定类型，可能抛出ClassCastException
     * - 如果Redis连接失败，可能抛出RedisConnectionException
     * 
     * ⚠️ 注意事项：
     * - 获取大对象时注意内存使用情况
     * - 频繁获取相同数据应考虑本地缓存
     * - 类型参数clazz应与存储时的类型匹配
     * - 获取后建议检查返回值是否为null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 🗑️ 删除缓存 - 从Redis中删除指定的键
     * 
     * 📖 功能说明：
     * 从Redis中删除指定的键及其对应的值，释放内存空间。
     * 这是最常用的删除操作，适用于清理过期或无效的缓存数据。
     * 
     * 🔄 工作流程：
     * 1. 接收键作为参数
     * 2. 使用RedisTemplate的delete()方法删除键
     * 3. Redis执行删除操作并释放内存
     * 4. 返回删除结果（true表示成功删除，false表示键不存在）
     * 
     * 📝 参数说明：
     * @param key 要删除的Redis键
     * 
     * @return 删除结果，true表示成功删除键，false表示键不存在
     * 
     * 🔄 使用示例：
     * ```java
     * // 删除用户信息
     * boolean deleted = redisUtil.delete("user:1");
     * if (deleted) {
     *     System.out.println("用户信息已删除");
     * }
     * 
     * // 删除验证码
     * redisUtil.delete("verify:code:123456");
     * 
     * // 删除过期的会话
     * redisUtil.delete("session:user:123");
     * 
     * // 删除临时数据
     * redisUtil.delete("temp:data:abc");
     * ```
     * 
     * 🚨 返回值说明：
     * - true：键存在且成功删除
     * - false：键不存在，无需删除
     * - null：Redis连接失败或执行错误
     * 
     * ⚠️ 注意事项：
     * - 删除操作不可逆，请谨慎使用
     * - 删除不存在的键不会报错，但会返回false
     * - 大量删除操作可能影响Redis性能
     * - 建议在删除前检查键是否存在
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 🗑️ 批量删除缓存 - 一次性删除多个键
     * 
     * 📖 功能说明：
     * 一次性删除多个键及其对应的值，提高删除效率。
     * 适用于清理一批相关的缓存数据，如清理用户的所有相关缓存。
     * 
     * 🔄 工作流程：
     * 1. 接收键集合作为参数
     * 2. 使用RedisTemplate的delete(keys)方法批量删除
     * 3. Redis执行批量删除操作并释放内存
     * 4. 返回实际删除的键数量
     * 
     * 📝 参数说明：
     * @param keys 要删除的Redis键集合
     * 
     * @return 实际删除的键数量，如果键不存在则不计入
     * 
     * 🔄 使用示例：
     * ```java
     * // 删除用户的所有相关缓存
     * List<String> userKeys = Arrays.asList("user:1", "user:1:profile", "user:1:settings");
     * Long deletedCount = redisUtil.delete(userKeys);
     * System.out.println("删除了 " + deletedCount + " 个键");
     * 
     * // 清理过期的验证码
     * List<String> expiredCodes = Arrays.asList("verify:code:123", "verify:code:456", "verify:code:789");
     * redisUtil.delete(expiredCodes);
     * 
     * // 清理临时数据
     * Set<String> tempKeys = new HashSet<>();
     * tempKeys.add("temp:data:1");
     * tempKeys.add("temp:data:2");
     * redisUtil.delete(tempKeys);
     * ```
     * 
     * 📊 性能考虑：
     * - 批量删除比多次单独删除效率更高
     * - 一次删除大量键（如数千个）可能阻塞Redis服务器
     * - 建议分批删除大量键，避免影响Redis性能
     * 
     * ⚠️ 注意事项：
     * - 空集合不会执行任何操作，返回0
     * - 只删除存在的键，不存在的键不计入返回值
     * - 大量删除操作可能影响Redis性能
     * - 建议在删除前检查集合是否为空
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 🔍 判断key是否存在 - 检查Redis中是否存在指定键
     * 
     * 📖 功能说明：
     * 检查Redis中是否存在指定的键，但不获取键对应的值。
     * 适用于条件判断、缓存存在性检查等场景，比获取值更高效。
     * 
     * 🔄 工作流程：
     * 1. 接收键作为参数
     * 2. 使用RedisTemplate的hasKey()方法检查键是否存在
     * 3. Redis执行EXISTS命令检查键
     * 4. 返回检查结果
     * 
     * 📝 参数说明：
     * @param key 要检查的Redis键
     * 
     * @return 检查结果，true表示键存在，false表示键不存在
     * 
     * 🔄 使用示例：
     * ```java
     * // 检查用户信息是否已缓存
     * if (redisUtil.hasKey("user:1")) {
     *     User user = redisUtil.get("user:1", User.class);
     * } else {
     *     // 从数据库加载并缓存
     *     User user = userService.getById(1L);
     *     redisUtil.set("user:1", user, 30, TimeUnit.MINUTES);
     * }
     * 
     * // 检查验证码是否有效
     * if (!redisUtil.hasKey("verify:code:123456")) {
     *     throw new BusinessException("验证码已过期或不存在");
     * }
     * 
     * // 检查会话是否有效
     * if (redisUtil.hasKey("session:user:123")) {
     *     // 会话有效，继续处理
     * } else {
     *     // 会话过期，要求重新登录
     *     throw new UnauthorizedException("会话已过期，请重新登录");
     * }
     * ```
     * 
     * 📊 性能优势：
     * - 只检查键的存在性，不传输值数据，网络开销小
     * - 时间复杂度为O(1)，性能高
     * - 适用于缓存穿透防护、条件判断等场景
     * 
     * ⚠️ 注意事项：
     * - 此操作只检查键的存在性，不关心键的类型
     * - 即使键对应的是空字符串，也返回true
     * - 频繁检查可能影响性能，建议合理使用
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * ⏰ 设置过期时间 - 为已存在的键设置过期时间
     * 
     * 📖 功能说明：
     * 为已存在的键设置过期时间，过期后Redis会自动删除该键。
     * 适用于将永久存储的数据转换为临时数据，或延长临时数据的有效期。
     * 
     * 🔄 工作流程：
     * 1. 接收键、过期时间和时间单位作为参数
     * 2. 使用RedisTemplate的expire()方法设置过期时间
     * 3. Redis执行EXPIRE命令设置过期时间
     * 4. 启动过期计时器，到期自动删除数据
     * 5. 返回设置结果
     * 
     * 📝 参数说明：
     * @param key 要设置过期时间的Redis键
     * @param timeout 过期时间的数值，与unit参数配合使用
     * @param unit 时间单位，如TimeUnit.SECONDS（秒）、TimeUnit.MINUTES（分钟）等
     * 
     * @return 设置结果，true表示成功设置过期时间，false表示键不存在或设置失败
     * 
     * 🔄 使用示例：
     * ```java
     * // 为用户信息设置30分钟过期时间
     * boolean result = redisUtil.expire("user:1", 30, TimeUnit.MINUTES);
     * if (result) {
     *     System.out.println("过期时间设置成功");
     * }
     * 
     * // 延长验证码的有效期
     * redisUtil.expire("verify:code:123456", 5, TimeUnit.MINUTES);
     * 
     * // 为会话设置2小时过期时间
     * redisUtil.expire("session:user:123", 2, TimeUnit.HOURS);
     * 
     * // 为临时数据设置1小时过期时间
     * redisUtil.expire("temp:data:abc", 1, TimeUnit.HOURS);
     * ```
     * 
     * 🔄 应用场景：
     * - 将永久数据转换为临时数据
     * - 延长临时数据的有效期
     * - 实现滑动过期机制（每次访问延长过期时间）
     * - 批量设置过期时间
     * 
     * ⚠️ 注意事项：
     * - 只能为已存在的键设置过期时间
     * - 过期时间设置会覆盖原有的过期时间
     * - 过期时间是相对时间，从设置时刻开始计算
     * - 设置成功后，键会在过期时间到达时自动删除
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * ⏰ 获取过期时间 - 查询键的剩余过期时间
     * 
     * 📖 功能说明：
     * 查询键的剩余过期时间，用于监控缓存状态、实现缓存预热等。
     * 返回值为键的剩余生存时间（TTL, Time To Live）。
     * 
     * 🔄 工作流程：
     * 1. 接收键作为参数
     * 2. 使用RedisTemplate的getExpire()方法查询过期时间
     * 3. Redis执行TTL命令查询剩余时间
     * 4. 返回查询结果
     * 
     * 📝 参数说明：
     * @param key 要查询的Redis键
     * 
     * @return 剩余过期时间（秒），有以下几种情况：
     *         - 正数：键存在且设置了过期时间，返回剩余秒数
     *         - -1：键存在但未设置过期时间（永久存储）
     *         - -2：键不存在
     * 
     * 🔄 使用示例：
     * ```java
     * // 检查用户信息的剩余过期时间
     * Long ttl = redisUtil.getExpire("user:1");
     * if (ttl > 0) {
     *     System.out.println("用户信息将在 " + ttl + " 秒后过期");
     * } else if (ttl == -1) {
     *     System.out.println("用户信息永不过期");
     * } else {
     *     System.out.println("用户信息不存在");
     * }
     * 
     * // 检查验证码是否即将过期（剩余时间少于1分钟）
     * Long codeTtl = redisUtil.getExpire("verify:code:123456");
     * if (codeTtl != null && codeTtl > 0 && codeTtl < 60) {
     *     System.out.println("验证码即将过期，请尽快使用");
     * }
     * 
     * // 实现缓存预热：在缓存即将过期时提前刷新
     * Long dataTtl = redisUtil.getExpire("hot:data:456");
     * if (dataTtl != null && dataTtl > 0 && dataTtl < 300) { // 剩余时间少于5分钟
     *     // 提前刷新缓存
     *     refreshCache("hot:data:456");
     * }
     * ```
     * 
     * 📊 返回值说明：
     * - 正数：剩余生存时间（秒）
     * - -1：键存在但未设置过期时间
     * - -2：键不存在
     * - null：Redis连接失败或执行错误
     * 
     * 🔄 应用场景：
     * - 监控缓存状态
     * - 实现缓存预热机制
     * - 检查数据是否即将过期
     * - 调试缓存问题
     * 
     * ⚠️ 注意事项：
     * - 返回值是秒为单位，不是毫秒
     * - 频繁查询TTL可能影响Redis性能
     * - 返回值可能为null，需要做空值检查
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 🔢 递增 - 将键对应的数值加1
     * 
     * 📖 功能说明：
     * 将键对应的数值加1，并返回递增后的值。
     * 如果键不存在，会先创建键并初始化为0，然后执行递增操作。
     * 适用于计数器、序列号生成、访问量统计等场景。
     * 
     * 🔄 工作流程：
     * 1. 接收键作为参数
     * 2. 使用RedisTemplate的opsForValue()获取值操作对象
     * 3. 调用increment(key)方法执行递增操作
     * 4. Redis执行INCR命令
     * 5. 返回递增后的值
     * 
     * 📝 参数说明：
     * @param key 要递增的Redis键，对应的值必须是数字类型
     * 
     * @return 递增后的值，如果键不存在则返回1
     * 
     * 🔄 使用示例：
     * ```java
     * // 文章访问量计数
     * Long viewCount = redisUtil.increment("article:123:views");
     * System.out.println("文章访问量: " + viewCount);
     * 
     * // 生成唯一序列号
     * Long sequence = redisUtil.increment("sequence:order");
     * String orderNo = "ORD" + System.currentTimeMillis() + String.format("%04d", sequence);
     * 
     * // 统计在线用户数
     * Long onlineCount = redisUtil.increment("stats:online:users");
     * 
     * // 记录API调用次数
     * Long apiCallCount = redisUtil.increment("api:calls:today");
     * ```
     * 
     * 📊 应用场景：
     * - 访问量统计：文章浏览量、页面访问量
     * - 计数器：点赞数、评论数、收藏数
     * - 序列号生成：订单号、流水号
     * - 限流器：API调用次数限制
     * - 统计数据：日活用户、在线用户数
     * 
     * ⚠️ 注意事项：
     * - 键对应的值必须是数字类型，否则会抛出异常
     * - 递增操作是原子性的，不会出现并发问题
     * - 长期递增可能导致数值溢出（Redis支持大整数）
     * - 递增操作的性能很高，适合高并发场景
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * ➕ 递增指定值 - 将键对应的数值增加指定值
     * 
     * 📖 功能说明：
     * 将键对应的数值增加指定的值，并返回递增后的值。
     * 如果键不存在，会先创建键并初始化为0，然后执行递增操作。
     * 支持正数递增和负数递减，适用于各种计数场景。
     * 
     * 🔄 工作流程：
     * 1. 接收键和增量值作为参数
     * 2. 使用RedisTemplate的opsForValue()获取值操作对象
     * 3. 调用increment(key, delta)方法执行递增操作
     * 4. Redis执行INCRBY命令
     * 5. 返回递增后的值
     * 
     * 📝 参数说明：
     * @param key 要递增的Redis键，对应的值必须是数字类型
     * @param delta 要增加的值，可以是正数（递增）或负数（递减）
     * 
     * @return 递增后的值
     * 
     * 🔄 使用示例：
     * ```java
     * // 文章点赞数增加
     * Long likeCount = redisUtil.increment("article:123:likes", 1);
     * 
     * // 批量增加库存（减少库存用负数）
     * Long stock = redisUtil.increment("product:456:stock", -5);
     * 
     * // 增加用户积分
     * Long points = redisUtil.increment("user:789:points", 100);
     * 
     * // 批量处理计数
     * Long processedCount = redisUtil.increment("batch:processed", 50);
     * 
     * // 减少操作（使用负数）
     * Long remaining = redisUtil.increment("quota:api:daily", -1);
     * ```
     * 
     * 📊 应用场景：
     * - 点赞/取消点赞：+1/-1
     * - 库存管理：增加/减少库存
     * - 积分系统：增加/扣除积分
     * - 配额管理：增加/减少配额
     * - 批量处理：记录处理数量
     * 
     * ⚠️ 注意事项：
     * - 键对应的值必须是数字类型，否则会抛出异常
     * - 递增操作是原子性的，不会出现并发问题
     * - delta可以是负数，实现递减功能
     * - 大数值递增可能导致数值溢出（Redis支持大整数）
     * - 递增操作的性能很高，适合高并发场景
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * ➖ 递减 - 将键对应的数值减1
     * 
     * 📖 功能说明：
     * 将键对应的数值减1，并返回递减后的值。
     * 如果键不存在，会先创建键并初始化为0，然后执行递减操作（结果为-1）。
     * 适用于库存扣减、配额消耗、倒计时等场景。
     * 
     * 🔄 工作流程：
     * 1. 接收键作为参数
     * 2. 使用RedisTemplate的opsForValue()获取值操作对象
     * 3. 调用decrement(key)方法执行递减操作
     * 4. Redis执行DECR命令
     * 5. 返回递减后的值
     * 
     * 📝 参数说明：
     * @param key 要递减的Redis键，对应的值必须是数字类型
     * 
     * @return 递减后的值，如果键不存在则返回-1
     * 
     * 🔄 使用示例：
     * ```java
     * // 库存扣减
     * Long stock = redisUtil.decrement("product:123:stock");
     * if (stock >= 0) {
     *     System.out.println("库存扣减成功，剩余: " + stock);
     * } else {
     *     System.out.println("库存不足");
     *     // 回滚操作
     *     redisUtil.increment("product:123:stock");
     * }
     * 
     * // API调用配额消耗
     * Long remainingQuota = redisUtil.decrement("api:quota:daily");
     * if (remainingQuota < 0) {
     *     System.out.println("API调用次数已达上限");
     *     // 回滚操作
     *     redisUtil.increment("api:quota:daily");
     * }
     * 
     * // 倒计时功能
     * Long remainingTime = redisUtil.decrement("countdown:event");
     * if (remainingTime <= 0) {
     *     System.out.println("倒计时结束");
     * }
     * ```
     * 
     * 📊 应用场景：
     * - 库存管理：商品库存扣减
     * - 配额管理：API调用次数限制
     * - 倒计时：活动倒计时、限时优惠
     * - 资源管理：连接池、线程池资源计数
     * - 限流器：请求频率控制
     * 
     * ⚠️ 注意事项：
     * - 键对应的值必须是数字类型，否则会抛出异常
     * - 递减操作是原子性的，不会出现并发问题
     * - 递减可能导致负值，需要业务逻辑处理
     * - 长期递减可能导致数值下溢（Redis支持大整数）
     * - 递减操作的性能很高，适合高并发场景
     * - 建议在递减后检查结果是否为负值，必要时进行回滚
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
}

