package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * StudentMapper接口 - 学生数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的student表，就像一个"学生档案管理员"。
 * 管理着所有学生的详细信息，包括学号、姓名、班级、专业等。
 * 
 * 📚 系统设计逻辑：
 * 在我们的系统中，用户表（user）和学生表（student）是分开的：
 * - user表：存储登录账号信息（用户名、密码、邮箱、角色）
 * - student表：存储学生业务信息（学号、姓名、班级、专业、头像）
 * - 通过student.user_id关联两张表
 * 
 * 这样设计的好处：
 * 1. 职责分离：登录认证和业务信息分开管理
 * 2. 扩展性：未来可以添加teacher表、admin表等不同角色
 * 3. 安全性：user表和student表可以设置不同的访问权限
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<Student>
 * - BaseMapper自动提供17个基础方法
 * - 我们添加了两个常用查询方法：
 *   1. findByUserId：根据用户ID查询学生信息（最常用）
 *   2. findByStudentNo：根据学号查询学生信息（用于学号唯一性检查）
 * 
 * 📊 对应数据库表: student
 * 
 * 🔗 关联关系：
 * - 关联user表：通过student.user_id = user.id （获取登录账号信息）
 * - 被question表关联：question.student_id = student.id （学生提问）
 * - 裫follow表关联：follow.student_id = student.id （学生关注教师）
 * - 裫collection表关联：collection.student_id = student.id （学生收藏）
 * 
 * 🔧 MyBatis-Plus提供的免费方法：
 * - insert(Student s)：插入新学生
 * - deleteById(Long id)：删除学生
 * - updateById(Student s)：更新学生信息
 * - selectById(Long id)：根据ID查询学生
 * - selectList(Wrapper)：查询学生列表
 * 
 * 💡 使用场景：
 * 1. 学生注册：先创建user记录，再创建student记录
 * 2. 学生登录后：根据userId查询学生信息，显示头像、姓名
 * 3. 学生修改资料：更新student表的信息
 * 4. 教师查看提问者：根据student_id查询学生详情
 * 5. 管理员管理：查询、修改、删除学生账号
 * 
 * ⚠️ 重要提示：
 * 1. 这是一个接口，不需要编写实现类
 * 2. @Mapper注解让MyBatis自动生成实现代码
 * 3. userId和student_id是一对一关系，一个userId只对应一个学生
 * 4. 学号（student_no）应该是唯一的，数据库中设置唯一索引
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 根据用户ID查询学生信息
     * 
     * 🎯 方法作用：
     * 通过用户ID查找对应的学生详细信息。这是系统中最常用的查询方法。
     * 就像通过身份证号查询一个人的学籍信息。
     * 
     * 🔗 使用场景：
     * 1. 学生登录后：
     *    - 用户登录成功，得到userId
     *    - 根据userId查询学生信息，在页面显示头像、姓名、班级等
     * 
     * 2. 访问个人中心：
     *    - 用户点击"个人中心"
     *    - 根据当前登录用户的userId查询学生详细信息
     * 
     * 3. 提交问题时：
     *    - 需要获取当前登录学生的student_id
     *    - 根据userId查询学生，获取student_id保存到问题表
     * 
     * 4. 权限检查：
     *    - 验证当前用户是否为学生角色
     *    - 根据userId查询，如果找到student记录则是学生
     * 
     * 🔍 查询逻辑详解：
     * 
     * 1. 创建LambdaQueryWrapper查询条件构造器
     * 
     * 2. .eq(Student::getUserId, userId)
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
     * 📝 使用示例1 - 登录后获取学生信息：
     * <pre>
     * // 学生登录成功，得到当前登录用户的userId
     * Long currentUserId = 10L;  // 从 JWT token 或 Session 中获取
     * 
     * Optional<Student> studentOpt = studentMapper.findByUserId(currentUserId);
     * if (studentOpt.isPresent()) {
     *     Student student = studentOpt.get();
     *     // 在页面上显示学生信息
     *     System.out.println("欢迎，" + student.getRealName());
     *     System.out.println("学号：" + student.getStudentNo());
     *     System.out.println("班级：" + student.getClassName());
     * } else {
     *     // 理论上不应该出现，因为学生登录时应该已创建学生记录
     *     throw new BusinessException("学生信息不存在，请联系管理员");
     * }
     * </pre>
     * 
     * 📝 使用示例2 - 获取学生ID保存问题：
     * <pre>
     * // 学生提交问题，需要获取student_id
     * Long userId = getCurrentUserId();  // 从当前登录信息获取
     * Student student = studentMapper.findByUserId(userId)
     *     .orElseThrow(() -> new BusinessException("学生信息不存在"));
     * 
     * // 创建问题对象
     * Question question = new Question();
     * question.setStudentId(student.getId());  // 设置提问者ID
     * question.setTitle("...");
     * question.setContent("...");
     * questionMapper.insert(question);
     * </pre>
     * 
     * 📝 使用示例3 - 验证用户是否为学生：
     * <pre>
     * // 检查当前登录用户是否为学生角色
     * Long userId = getCurrentUserId();
     * boolean isStudent = studentMapper.findByUserId(userId).isPresent();
     * 
     * if (!isStudent) {
     *     throw new BusinessException("该功能仅对学生开放");
     * }
     * </pre>
     * 
     * 🎯 实际执行的SQL：
     * SELECT id, user_id, student_no, real_name, class_name, major, avatar, create_time, update_time
     * FROM student
     * WHERE user_id = 10
     * LIMIT 1
     * 
     * @param userId 用户ID（不能为null）
     * @return Optional<Student> 包装的学生对象
     *         - 如果找到：Optional.of(student)
     *         - 如果未找到：Optional.empty()
     * 
     * ⚠️ 注意事项：
     * 1. userId不能为null，否则会抛NullPointerException
     * 2. user_id和student是一对一关系，一个userId只应对应一个学生
     * 3. 如果找到多条记录，说明数据库设计有问题，user_id应该设置唯一索引
     * 4. 返回的Student对象不包含User信息，如需要，需要额外查询
     * 5. 建议对查询结果进行缓存，减少数据库访问（如使用Redis）
     */
    default Optional<Student> findByUserId(Long userId) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId))
        );
    }

    /**
     * 根据学号查询学生信息
     * 
     * 🎯 方法作用：
     * 通过学号查找学生信息。就像在学生花名册中根据学号查找学生。
     * 主要用于注册时的学号唯一性检查、管理员搜索学生等场景。
     * 
     * 🔗 使用场景：
     * 
     * 1. 学生注册时检查学号是否已被使用：
     *    - 学生输入学号"2021001"
     *    - 查询该学号是否已存在
     *    - 如果存在，提示"学号已被注册"
     * 
     * 2. 管理员搜索学生：
     *    - 管理员在后台输入学号查找学生
     *    - 查询该学生的详细信息、提问记录等
     * 
     * 3. 导入学生数据时去重：
     *    - 批量导入学生时，检查学号是否已存在
     *    - 避免重复创建相同学号的学生
     * 
     * 4. 学生信息校验：
     *    - 校验学生输入的学号是否有效
     *    - 确认学生身份
     * 
     * 🔍 查询逻辑详解：
     * 
     * 1. 创建LambdaQueryWrapper查询条件构造器
     * 
     * 2. .eq(Student::getStudentNo, studentNo)
     *    - 设置查询条件：student_no = ?
     *    - 学号精确匹配，不是模糊查询
     *    - Lambda表达式保证类型安全
     * 
     * 3. selectOne()执行查询
     *    - 期望只找到一条记录（学号是唯一的）
     *    - 如果找到多条，会抛TooManyResultsException
     *    - 如果未找到，返回null
     * 
     * 4. Optional.ofNullable()包装结果
     *    - 安全地处理可能为null的结果
     * 
     * 📝 使用示例1 - 注册时检查学号：
     * <pre>
     * // 学生注册时输入学号
     * String studentNo = "2021001";
     * 
     * // 检查学号是否已被使用
     * Optional<Student> existingStudent = studentMapper.findByStudentNo(studentNo);
     * if (existingStudent.isPresent()) {
     *     // 学号已存在，不能注册
     *     throw new BusinessException("学号" + studentNo + "已被注册，请联系管理员");
     * }
     * 
     * // 学号未被使用，可以继续注册流程
     * Student newStudent = new Student();
     * newStudent.setStudentNo(studentNo);
     * // ...设置其他信息
     * studentMapper.insert(newStudent);
     * </pre>
     * 
     * 📝 使用示例2 - 管理员搜索学生：
     * <pre>
     * // 管理员在后台输入学号搜索
     * String searchStudentNo = "2021001";
     * 
     * Optional<Student> studentOpt = studentMapper.findByStudentNo(searchStudentNo);
     * studentOpt.ifPresentOrElse(
     *     student -> {
     *         // 找到学生，显示详细信息
     *         System.out.println("姓名：" + student.getRealName());
     *         System.out.println("班级：" + student.getClassName());
     *         System.out.println("专业：" + student.getMajor());
     *     },
     *     () -> {
     *         // 未找到学生
     *         System.out.println("未找到学号为" + searchStudentNo + "的学生");
     *     }
     * );
     * </pre>
     * 
     * 📝 使用示例3 - 批量导入时去重：
     * <pre>
     * // 从 Excel 文件导入学生数据
     * List<StudentExcelDTO> excelData = readExcel("students.xlsx");
     * 
     * for (StudentExcelDTO dto : excelData) {
     *     // 检查学号是否已存在
     *     Optional<Student> existing = studentMapper.findByStudentNo(dto.getStudentNo());
     *     
     *     if (existing.isPresent()) {
     *         // 学号已存在，跳过或更新
     *         System.out.println("学号" + dto.getStudentNo() + "已存在，跳过");
     *         continue;
     *     }
     *     
     *     // 学号不存在，创建新学生
     *     Student newStudent = convertToStudent(dto);
     *     studentMapper.insert(newStudent);
     * }
     * </pre>
     * 
     * 🎯 实际执行的SQL：
     * SELECT id, user_id, student_no, real_name, class_name, major, avatar, create_time, update_time
     * FROM student
     * WHERE student_no = '2021001'
     * LIMIT 1
     * 
     * @param studentNo 学号（不能为null或空字符串）
     * @return Optional<Student> 包装的学生对象
     *         - 如果找到：Optional.of(student)
     *         - 如果未找到：Optional.empty()
     * 
     * ⚠️ 注意事项：
     * 1. studentNo不能为null或空字符串，否则会抛NullPointerException
     * 2. 学号应该是唯一的，数据库中应设置唯一索引：UNIQUE KEY `uk_student_no` (`student_no`)
     * 3. 学号查询不区分大小写（取决于数据库设置）
     * 4. 建议在查询前先trim()去除首尾空格：studentNo.trim()
     * 5. 如果需要模糊查询（如部分匹配），应使用selectList + like条件
     * 6. 对于高频查询，建议添加缓存（如Redis）
     */
    default Optional<Student> findByStudentNo(String studentNo) {
        return Optional.ofNullable(
            selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo))
        );
    }
}

