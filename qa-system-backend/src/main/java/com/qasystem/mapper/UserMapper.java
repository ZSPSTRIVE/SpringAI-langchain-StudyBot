package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * UserMapper接口 - 用户数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的user表，相当于一个"图书管理员"，专门负责从数据库"图书馆"中
 * 查找、添加、修改、删除用户信息。所有对用户表的数据库操作都通过这个接口进行。
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<User>，就像继承了一套"标准图书管理工具"
 * - BaseMapper自动提供了常用的增删改查方法（insert、update、delete、select等）
 * - 我们只需要添加特殊的查询方法（比如根据用户名查找、根据邮箱查找）
 * 
 * 📊 对应数据库表: user
 * 
 * 🔧 MyBatis-Plus提供的免费方法（无需编写SQL）：
 * - insert(User user)：插入一条用户记录
 * - deleteById(Long id)：根据ID删除用户
 * - updateById(User user)：根据ID更新用户信息
 * - selectById(Long id)：根据ID查询用户
 * - selectList(Wrapper)：根据条件查询用户列表
 * - selectCount(Wrapper)：统计用户数量
 * 等等...共有17个基础方法
 * 
 * 💡 使用场景：
 * 1. 用户登录时：根据用户名查找用户信息，验证密码
 * 2. 用户注册时：检查用户名/邮箱是否已存在，插入新用户
 * 3. 找回密码时：根据邮箱查找用户，发送重置链接
 * 4. 用户管理：管理员查询、修改、删除用户
 * 
 * ⚠️ 重要提示：
 * 1. 这是一个接口（interface），不需要编写实现类，MyBatis-Plus会自动生成实现代码
 * 2. @Mapper注解告诉Spring：这是一个数据访问接口，请自动创建实现
 * 3. default方法可以在接口中直接编写实现代码（Java 8+特性）
 * 4. Optional包装返回值，表示"可能找到，也可能找不到"，避免空指针异常
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * 
     * 🎯 方法作用：
     * 在user表中查找指定用户名的用户，就像在学生花名册中根据姓名查找学生信息。
     * 主要用于用户登录验证 - 用户输入用户名和密码，我们先根据用户名找到用户记录，
     * 然后再比对密码是否正确。
     * 
     * 🔍 查询逻辑详解：
     * 1. 创建LambdaQueryWrapper查询条件构造器
     *    - Lambda方式的好处：使用User::getUsername而不是字符串"username"，更安全
     *    - 如果字段名写错，编译时就能发现，而不是运行时才报错
     * 
     * 2. .eq(User::getUsername, username)
     *    - eq表示"equals"，即相等条件
     *    - 生成SQL：WHERE username = ?
     *    - 参数username会自动替换到?位置，MyBatis会防止SQL注入
     * 
     * 3. selectOne()查询单条记录
     *    - 期望只找到一条记录（用户名是唯一的）
     *    - 如果找到多条会抛异常（数据库设计问题）
     *    - 如果找不到返回null
     * 
     * 4. Optional.ofNullable()包装结果
     *    - 将可能为null的结果包装成Optional对象
     *    - 调用方可以优雅地处理"找不到"的情况
     * 
     * 📝 使用示例：
     * <pre>
     * // 在Service层调用
     * Optional<User> userOpt = userMapper.findByUsername("zhangsan");
     * if (userOpt.isPresent()) {
     *     User user = userOpt.get();
     *     // 找到了用户，进行密码验证
     *     if (passwordEncoder.matches(password, user.getPassword())) {
     *         // 密码正确，登录成功
     *     }
     * } else {
     *     // 用户不存在，返回错误提示
     * }
     * </pre>
     * 
     * 🎯 实际执行的SQL：
     * SELECT id, username, password, email, role, create_time, update_time 
     * FROM user 
     * WHERE username = 'zhangsan'
     * LIMIT 1
     * 
     * @param username 要查询的用户名（不能为null或空字符串）
     * @return Optional<User> 包装的用户对象
     *         - 如果找到用户：Optional.of(user)
     *         - 如果未找到：Optional.empty()
     * 
     * ⚠️ 注意事项：
     * 1. 用户名区分大小写（取决于数据库表的字符集设置）
     * 2. 调用前应先验证username不为null，否则会抛NullPointerException
     * 3. 用户名应该在数据库中设置唯一索引，保证不会查出多条记录
     * 4. 查询前后会自动去除username的首尾空格（可选，需要在调用前处理）
     */
    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username))
        );
    }

    /**
     * 根据邮箱查询用户
     * 
     * 🎯 方法作用：
     * 在user表中查找指定邮箱的用户，就像通过电子邮箱地址查找联系人。
     * 主要用于以下场景：
     * 1. 用户注册时检查邮箱是否已被使用
     * 2. 找回密码时根据邮箱查找用户
     * 3. 用户通过邮箱登录（如果系统支持）
     * 4. 发送邮件通知时查找接收者
     * 
     * 🔍 查询逻辑详解：
     * 1. 使用LambdaQueryWrapper构建查询条件
     *    - User::getEmail是Lambda表达式，引用User类的getEmail方法
     *    - 编译时类型安全，重构时IDE会自动更新
     * 
     * 2. .eq(User::getEmail, email)
     *    - 生成SQL条件：WHERE email = ?
     *    - email参数会自动转义，防止SQL注入攻击
     *    - 邮箱匹配是精确匹配，不是模糊查询
     * 
     * 3. selectOne()执行查询
     *    - 返回单个User对象或null
     *    - 如果数据库中有多条相同邮箱的记录会抛TooManyResultsException
     *    - 这说明数据完整性有问题，邮箱应该是唯一的
     * 
     * 4. Optional.ofNullable()安全包装
     *    - 避免调用方直接处理null值
     *    - 提供链式调用的便利性
     * 
     * 📝 使用示例1 - 注册时检查邮箱：
     * <pre>
     * // 用户尝试用邮箱"zhangsan@qq.com"注册
     * Optional<User> existingUser = userMapper.findByEmail("zhangsan@qq.com");
     * if (existingUser.isPresent()) {
     *     // 邮箱已被使用，返回错误
     *     throw new BusinessException("该邮箱已被注册，请使用其他邮箱");
     * }
     * // 邮箱未被使用，可以继续注册流程
     * </pre>
     * 
     * 📝 使用示例2 - 找回密码：
     * <pre>
     * // 用户输入邮箱找回密码
     * Optional<User> userOpt = userMapper.findByEmail("zhangsan@qq.com");
     * userOpt.ifPresentOrElse(
     *     user -> {
     *         // 找到用户，发送重置密码邮件
     *         String resetToken = generateResetToken();
     *         emailService.sendResetEmail(user.getEmail(), resetToken);
     *     },
     *     () -> {
     *         // 未找到用户，为了安全也显示"邮件已发送"（防止邮箱探测）
     *         // 实际不发送邮件
     *     }
     * );
     * </pre>
     * 
     * 🎯 实际执行的SQL：
     * SELECT id, username, password, email, role, create_time, update_time 
     * FROM user 
     * WHERE email = 'zhangsan@qq.com'
     * LIMIT 1
     * 
     * @param email 要查询的邮箱地址（应该是有效的邮箱格式）
     * @return Optional<User> 包装的用户对象
     *         - 如果找到：Optional.of(user)
     *         - 如果未找到：Optional.empty()
     * 
     * ⚠️ 注意事项：
     * 1. 邮箱查询不区分大小写（根据数据库配置，建议统一转小写后存储和查询）
     * 2. 调用前应验证email格式是否有效（使用正则或javax.validation）
     * 3. 邮箱字段应在数据库中设置唯一索引：UNIQUE KEY `uk_email` (`email`)
     * 4. 处理找回密码时要注意安全：
     *    - 不要泄露用户是否存在
     *    - 重置链接要有时效性（如30分钟）
     *    - 使用安全的随机token
     */
    default Optional<User> findByEmail(String email) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email))
        );
    }
}

