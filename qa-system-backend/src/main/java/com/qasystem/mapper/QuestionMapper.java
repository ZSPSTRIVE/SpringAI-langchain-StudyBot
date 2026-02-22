package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qasystem.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * QuestionMapper接口 - 问题数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的question表，是师生答疑系统的核心数据访问层。
 * 就像一个"问题库管理员"，负责管理所有学生提出的问题，包括：
 * - 存储新问题（学生提问）
 * - 查询问题列表（教师浏览待回答的问题）
 * - 更新问题状态（已回答/未回答）
 * - 删除问题（管理员删除不适合的内容）
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<Question>
 * - BaseMapper自动提供17个基础方法（insert、update、delete、select等）
 * - 我们添加了自定义的分页查询方法，因为需要关联查询学生和科目信息
 * - 对应的SQL语句写在配置文件中：resources/mapper/QuestionMapper.xml
 * 
 * 📊 对应数据库表: question
 * 
 * 🔗 关联关系：
 * - 关联student表：通过question.student_id = student.id （查询提问人信息）
 * - 关联subject表：通过question.subject_id = subject.id （查询科目信息）
 * - 与answer表为一对多关系：一个问题可以有多个答案
 * 
 * 🔧 MyBatis-Plus提供的免费方法：
 * - insert(Question q)：插入一个新问题
 * - deleteById(Long id)：删除指定问题
 * - updateById(Question q)：更新问题信息
 * - selectById(Long id)：根据ID查询问题
 * - selectList(Wrapper)：查询问题列表
 * - selectCount(Wrapper)：统计问题数量
 * 
 * 💡 使用场景：
 * 1. 学生提问：调用insert()保存问题
 * 2. 教师浏览问题：调用selectQuestionPage()分页查询
 * 3. 教师回答后：调用updateById()更新status为"已回答"
 * 4. 管理员审核：调用selectById()查看问题详情，调用deleteById()删除不合适问题
 * 5. 学生搜索：调用selectQuestionPage()根据关键词搜索历史问题
 * 
 * ⚠️ 重要提示：
 * 1. 这是一个接口，不需要编写实现类
 * 2. @Mapper注解让MyBatis自动生成实现代码
 * 3. 自定义方法selectQuestionPage的SQL实现在QuestionMapper.xml中
 * 4. @Param注解用于指定SQL中参数的名字
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 分页查询问题（带学生和科目信息）
     * 
     * 🎯 方法作用：
     * 这是一个高级查询方法，可以根据多个条件筛选问题，并且分页显示。
     * 就像在图书馆找书：
     * - 可以按分类找（科目）
     * - 可以按状态找（已借出/在架上）
     * - 可以按关键词搜索（标题或内容）
     * - 结果分页显示（每页N条）
     * 
     * 🔍 查询逻辑详解：
     * 这个方法会执行以下复杂查询：
     * 1. 从 question 表查询基础信息
     * 2. LEFT JOIN student 表，获取提问者的姓名、头像等信息
     * 3. LEFT JOIN subject 表，获取科目名称
     * 4. 根据参数进行筛选：
     *    - 如果 subjectId 不为空，只查询该科目的问题
     *    - 如果 status 不为空，只查询该状态的问题
     *    - 如果 keyword 不为空，搜索标题或内容包含关键词的问题
     * 5. 按创建时间降序排列（最新的问题在前）
     * 6. 进行分页处理（LIMIT 和 OFFSET）
     * 
     * 🔧 参数说明：
     * 
     * 1. page - 分页对象（MyBatis-Plus提供）
     *    - 包含当前页码（current）和每页大小Ｈsize）
     *    - 例如：new Page<>(1, 10) 表示查询第1页，每页显示10条
     *    - MyBatis-Plus会自动计算LIMIT和OFFSET
     * 
     * 2. subjectId - 科目ID（可选筛选条件）
     *    - 为null或空：查询所有科目的问题
     *    - 有值：只查询指定科目的问题
     *    - 示例：subjectId=1 表示只查询"数学"科目的问题
     * 
     * 3. status - 问题状态（可选筛选条件）
     *    - 为null或空：查询所有状态的问题
     *    - "pending"：只查询未回答的问题
     *    - "answered"：只查询已回答的问题
     * 
     * 4. keyword - 搜索关键词（可选筛选条件）
     *    - 为null或空：不进行关键词搜索
     *    - 有值：在问题标题和内容中模糊匹配
     *    - 示例：keyword="数学" 会查出标题或内容包含"数学"的问题
     * 
     * 📝 使用示例1 - 查询所有未回答的问题：
     * <pre>
     * // 教师想查看所有待回答的问题，每页显示10条
     * Page<Question> page = new Page<>(1, 10);  // 第1页，每页显示10条
     * IPage<Question> result = questionMapper.selectQuestionPage(
     *     page,
     *     null,        // subjectId为null，不限科目
     *     "pending",   // 只查询未回答的
     *     null         // keyword为null，不搜索关键词
     * );
     * List<Question> questions = result.getRecords();  // 获取问题列表
     * long total = result.getTotal();  // 获取总数量
     * </pre>
     * 
     * 📝 使用示例2 - 搜索指定科目的问题：
     * <pre>
     * // 学生想在"高等数学"科目中搜索关于"导数"的问题
     * Page<Question> page = new Page<>(1, 20);
     * IPage<Question> result = questionMapper.selectQuestionPage(
     *     page,
     *     5L,           // subjectId=5 （高等数学）
     *     null,         // 不限状态
     *     "导数"      // 搜索包含"导数"的问题
     * );
     * </pre>
     * 
     * 📝 使用示例3 - 查询第2页数据：
     * <pre>
     * // 用户点击“下一页”，查询第2页
     * Page<Question> page = new Page<>(2, 10);  // current=2 表示第2页
     * IPage<Question> result = questionMapper.selectQuestionPage(page, null, null, null);
     * // MyBatis-Plus会自动计算OFFSET=10（跳过前10条）
     * </pre>
     * 
     * 🎯 实际执行的SQL示例（简化版）：
     * SELECT 
     *     q.id, q.title, q.content, q.status, q.create_time,
     *     s.username AS student_name, s.avatar AS student_avatar,
     *     sub.name AS subject_name
     * FROM question q
     * LEFT JOIN student s ON q.student_id = s.id
     * LEFT JOIN subject sub ON q.subject_id = sub.id
     * WHERE 
     *     (q.subject_id = ? OR ? IS NULL)        -- 科目筛选
     *     AND (q.status = ? OR ? IS NULL)         -- 状态筛选
     *     AND (q.title LIKE ? OR q.content LIKE ? OR ? IS NULL)  -- 关键词搜索
     * ORDER BY q.create_time DESC
     * LIMIT 10 OFFSET 0
     * 
     * @param page MyBatis-Plus分页对象，包含当前页码和每页大小
     * @param subjectId 科目ID（可选），null表示不限科目
     * @param status 问题状态（可选），null表示不限状态
     * @param keyword 搜索关键词（可选），null表示不搜索
     * @return IPage<Question> 分页结果对象，包含：
     *         - records: 当前页的问题列表
     *         - total: 总记录数
     *         - size: 每页大小
     *         - current: 当前页码
     *         - pages: 总页数
     * 
     * ⚠️ 注意事项：
     * 1. 所有筛选条件都是可选的，传null表示不限制
     * 2. 返回的Question对象中包含关联查询的student和subject信息
     * 3. 分页参数应该有有效性验证：
     *    - current 应该 >= 1
     *    - size 应该在合理范围内（1-100）
     * 4. keyword搜索使用LIKE查询，性能较低，建议：
     *    - 数据量大时使用全文索引（如ElasticSearch）
     *    - 或者在数据库中创建全文索引（FULLTEXT）
     * 5. 该方法的SQL实现在 resources/mapper/QuestionMapper.xml 文件中
     * 6. @Param注解用于将Java参数名映射到XML中的SQL参数名
     */
    IPage<Question> selectQuestionPage(Page<Question> page, 
                                       @Param("subjectId") Long subjectId,
                                       @Param("status") String status,
                                       @Param("keyword") String keyword);

    /**
     * 基于 MySQL FULLTEXT 的高性能检索。
     * 注意：需要 question(title, content) 上存在 FULLTEXT 索引。
     */
    IPage<Question> selectQuestionPageByFulltext(Page<Question> page,
                                                 @Param("keyword") String keyword);
}

