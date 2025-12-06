package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * TeacherMapper接口 - 教师数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的teacher表，就像一个"教师档案管理员"。
 * 管理着所有教师的详细信息，包括工号、姓名、部门、职称、擅长科目等。
 * 
 * 📚 系统设计逻辑：
 * 与Student表类似，用户表（user）和教师表（teacher）是分开的：
 * - user表：存储登录账号信息（用户名、密码、邮箱、角色）
 * - teacher表：存储教师业务信息（工号、姓名、部门、职称、擅长科目、头像）
 * - 通过teacher.user_id关联两张表
 * 
 * 这样设计的好处：
 * 1. 职责分离：登录认证和业务信息分开管理
 * 2. 灵活性：同一数据库可以支持多种角色（student、teacher、admin）
 * 3. 安全性：user表和teacher表可以设置不同的访问权限
 * 4. 扩展性：教师可以有特有属性（如擅长科目、科研方向）
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<Teacher>
 * - BaseMapper自动提供17个基础方法
 * - 我们添加了两个常用查询方法：
 *   1. findByUserId：根据用户ID查询教师信息（最常用）
 *   2. findByTeacherNo：根据工号查询教师信息（用于工号唯一性检查）
 * 
 * 📊 对应数据库表: teacher
 * 
 * 🔗 关联关系：
 * - 关联user表：通过teacher.user_id = user.id （获取登录账号信息）
 * - 被answer表关联：answer.teacher_id = teacher.id （教师回答）
 * - 被follow表关联：follow.teacher_id = teacher.id （学生关注教师）
 * - 关联subject表：通过中间表teacher_subject （教师擅长的科目）
 * 
 * 🔧 MyBatis-Plus提供的免费方法：
 * - insert(Teacher t)：插入新教师
 * - deleteById(Long id)：删除教师
 * - updateById(Teacher t)：更新教师信息
 * - selectById(Long id)：根据ID查询教师
 * - selectList(Wrapper)：查询教师列表
 * 
 * 💡 使用场景：
 * 1. 教师注册：先创建user记录，再创建teacher记录
 * 2. 教师登录后：根据userId查询教师信息，显示头像、姓名、职称
 * 3. 教师修改资料：更新teacher表的信息
 * 4. 学生查看回答者：根据teacher_id查询教师详情
 * 5. 学生关注教师：显示教师的详细资料、擅长科目
 * 6. 管理员管理：查询、修改、删除教师账号
 * 
 * ⚠️ 重要提示：
 * 1. 这是一个接口，不需要编写实现类
 * 2. @Mapper注解让MyBatis自动生成实现代码
 * 3. userId和teacher_id是一对一关系，一个userId只对应一个教师
 * 4. 工号（teacher_no）应该是唯一的，数据库中设置唯一索引
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    /**
     * 根据用户ID查询教师信息
     * 
     * 🎯 方法作用：
     * 通过用户ID查找对应的教师详细信息。这是系统中最常用的查询方法。
     * 就像通过身份证号查询一个人的工作证信息。
     * 
     * 🔗 使用场景：
     * 
     * 1. 教师登录后：
     *    - 教师登录成功，得到userId
     *    - 根据userId查询教师信息，在页面显示头像、姓名、职称等
     * 
     * 2. 访问个人中心：
     *    - 教师点击"个人中心"
     *    - 根据当前登录用户的userId查询教师详细信息
     * 
     * 3. 回答问题时：
     *    - 需要获取当前登录教师的teacher_id
     *    - 根据userId查询教师，获取teacher_id保存到答案表
     * 
     * 4. 权限检查：
     *    - 验证当前用户是否为教师角色
     *    - 根据userId查询，如果找到teacher记录则是教师
     * 
     * 5. 获取教师擅长科目：
     *    - 根据userId查询教师信息
     *    - 获取教师的擅长科目列表，用于问题分配
     * 
     * 🔍 查询逻辑详解：
     * 
     * 1. 创建LambdaQueryWrapper查询条件构造器
     * 
     * 2. .eq(Teacher::getUserId, userId)
     *    - 设置查询条件：user_id = ?
     *    - Lambda表达式保证类型安全，字段名错误时编译就会发现
     * 
     * 3. selectOne()执行查询
     *    - 期望只找到一条记录（user_id是唯一的）
     *    - 如果找到多条，会抛TooManyResultsException（数据库设计问题）
     *    - 如果未找到，返回null
     * 
     * 4. Optional.ofNullable()包装结果
     *    - 安全地处理可能为null的结果
     *    - 调用方可以优雅地处理找不到的情况
     * 
     * 📝 使用示例1 - 登录后获取教师信息：
     * <pre>
     * // 教师登录成功，得到当前登录用户的userId
     * Long currentUserId = 20L;  // 从 JWT token 或 Session 中获取
     * 
     * Optional<Teacher> teacherOpt = teacherMapper.findByUserId(currentUserId);
     * if (teacherOpt.isPresent()) {
     *     Teacher teacher = teacherOpt.get();
     *     // 在页面上显示教师信息
     *     System.out.println("欢迎，" + teacher.getRealName() + "老师");
     *     System.out.println("工号：" + teacher.getTeacherNo());
     *     System.out.println("职称：" + teacher.getTitle());
     *     System.out.println("部门：" + teacher.getDepartment());
     * } else {
     *     throw new BusinessException("教师信息不存在，请联系管理员");
     * }
     * </pre>
     * 
     * 📝 使用示例2 - 获取教师ID保存答案：
     * <pre>
     * // 教师回答问题，需要获取teacher_id
     * Long userId = getCurrentUserId();  // 从当前登录信息获取
     * Teacher teacher = teacherMapper.findByUserId(userId)
     *     .orElseThrow(() -> new BusinessException("教师信息不存在"));
     * 
     * // 创建答案对象
     * Answer answer = new Answer();
     * answer.setTeacherId(teacher.getId());  // 设置回答者ID
     * answer.setQuestionId(questionId);
     * answer.setContent("...");
     * answerMapper.insert(answer);
     * </pre>
     * 
     * 📝 使用示例3 - 验证用户是否为教师：
     * <pre>
     * // 检查当前登录用户是否为教师角色
     * Long userId = getCurrentUserId();
     * boolean isTeacher = teacherMapper.findByUserId(userId).isPresent();
     * 
     * if (!isTeacher) {
     *     throw new BusinessException("该功能仅对教师开放");
     * }
     * </pre>
     * 
     * 🎯 实际执行的SQL：
     * SELECT id, user_id, teacher_no, real_name, title, department, avatar, create_time, update_time
     * FROM teacher
     * WHERE user_id = 20
     * LIMIT 1
     * 
     * @param userId 用户ID（不能为null）
     * @return Optional<Teacher> 包装的教师对象
     *         - 如果找到：Optional.of(teacher)
     *         - 如果未找到：Optional.empty()
     * 
     * ⚠️ 注意事项：
     * 1. userId不能为null，否则会抛NullPointerException
     * 2. user_id和teacher是一对一关系，一个userId只应对应一个教师
     * 3. 如果找到多条记录，说明数据库设计有问题，user_id应该设置唯一索引
     * 4. 返回的Teacher对象不包含User信息，如需要，需要额外查询
     * 5. 返回的Teacher对象不包含擅长科目列表，需要通过teacher_subject中间表查询
     * 6. 建议对查询结果进行缓存，减少数据库访问（如使用Redis）
     */
    default Optional<Teacher> findByUserId(Long userId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, userId))
        );
    }

    /**
     * 根据工号查询教师信息
     * 
     * 🎯 方法作用：
     * 通过工号查找教师信息。就像在教师花名册中根据工号查找教师。
     * 主要用于注册时的工号唯一性检查、管理员搜索教师等场景。
     * 
     * 🔗 使用场景：
     * 
     * 1. 教师注册时检查工号是否已被使用：
     *    - 教师输入工号"T2021001"
     *    - 查询该工号是否已存在
     *    - 如果存在，提示"工号已被注册"
     * 
     * 2. 管理员搜索教师：
     *    - 管理员在后台输入工号查找教师
     *    - 查询该教师的详细信息、回答记录等
     * 
     * 3. 导入教师数据时去重：
     *    - 批量导入教师时，检查工号是否已存在
     *    - 避免重复创建相同工号的教师
     * 
     * 4. 学生搜索教师：
     *    - 学生输入教师工号搜索教师
     *    - 查看教师的详细资料、擅长科目、历史回答
     * 
     * 🔍 查询逻辑详解：
     * 
     * 1. 创建LambdaQueryWrapper查询条件构造器
     * 
     * 2. .eq(Teacher::getTeacherNo, teacherNo)
     *    - 设置查询条件：teacher_no = ?
     *    - 工号精确匹配，不是模糊查询
     *    - Lambda表达式保证类型安全
     * 
     * 3. selectOne()执行查询
     *    - 期望只找到一条记录（工号是唯一的）
     *    - 如果找到多条，会抛TooManyResultsException
     *    - 如果未找到，返回null
     * 
     * 4. Optional.ofNullable()包装结果
     *    - 安全地处理可能为null的结果
     * 
     * 📝 使用示例1 - 注册时检查工号：
     * <pre>
     * // 教师注册时输入工号
     * String teacherNo = "T2021001";
     * 
     * // 检查工号是否已被使用
     * Optional<Teacher> existingTeacher = teacherMapper.findByTeacherNo(teacherNo);
     * if (existingTeacher.isPresent()) {
     *     // 工号已存在，不能注册
     *     throw new BusinessException("工号" + teacherNo + "已被注册，请联系管理员");
     * }
     * 
     * // 工号未被使用，可以继续注册流程
     * Teacher newTeacher = new Teacher();
     * newTeacher.setTeacherNo(teacherNo);
     * // ...设置其他信息
     * teacherMapper.insert(newTeacher);
     * </pre>
     * 
     * 📝 使用示例2 - 管理员搜索教师：
     * <pre>
     * // 管理员在后台输入工号搜索
     * String searchTeacherNo = "T2021001";
     * 
     * Optional<Teacher> teacherOpt = teacherMapper.findByTeacherNo(searchTeacherNo);
     * teacherOpt.ifPresentOrElse(
     *     teacher -> {
     *         // 找到教师，显示详细信息
     *         System.out.println("姓名：" + teacher.getRealName());
     *         System.out.println("职称：" + teacher.getTitle());
     *         System.out.println("部门：" + teacher.getDepartment());
     *     },
     *     () -> {
     *         // 未找到教师
     *         System.out.println("未找到工号为" + searchTeacherNo + "的教师");
     *     }
     * );
     * </pre>
     * 
     * 📝 使用示例3 - 学生搜索教师：
     * <pre>
     * // 学生想关注或搜索教师
     * String teacherNo = "T2021001";
     * 
     * Optional<Teacher> teacherOpt = teacherMapper.findByTeacherNo(teacherNo);
     * if (teacherOpt.isPresent()) {
     *     Teacher teacher = teacherOpt.get();
     *     // 显示教师资料页面
     *     System.out.println("教师姓名：" + teacher.getRealName());
     *     System.out.println("擅长科目：" + getTeacherSubjects(teacher.getId()));
     *     System.out.println("回答数：" + getAnswerCount(teacher.getId()));
     * }
     * </pre>
     * 
     * 🎯 实际执行的SQL：
     * SELECT id, user_id, teacher_no, real_name, title, department, avatar, create_time, update_time
     * FROM teacher
     * WHERE teacher_no = 'T2021001'
     * LIMIT 1
     * 
     * @param teacherNo 工号（不能为null或空字符串）
     * @return Optional<Teacher> 包装的教师对象
     *         - 如果找到：Optional.of(teacher)
     *         - 如果未找到：Optional.empty()
     * 
     * ⚠️ 注意事项：
     * 1. teacherNo不能为null或空字符串，否则会抛NullPointerException
     * 2. 工号应该是唯一的，数据库中应设置唯一索引：UNIQUE KEY `uk_teacher_no` (`teacher_no`)
     * 3. 工号查询不区分大小写（取决于数据库设置）
     * 4. 建议在查询前先trim()去除首尾空格：teacherNo.trim()
     * 5. 如果需要模糊查询（如部分匹配），应使用selectList + like条件
     * 6. 对于高频查询，建议添加缓存（如Redis）
     */
    default Optional<Teacher> findByTeacherNo(String teacherNo) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getTeacherNo, teacherNo))
        );
    }
}

