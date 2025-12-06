package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AnswerMapper接口 - 答案数据访问层
 * 
 * 🎯 作用说明：
 * 这个接口负责操作数据库中的answer表，是师生答疑系统的核心功能之一。
 * 就像一个"答案集管理员"，负责管理教师和学生对问题的所有回答，包括：
 * - 保存新答案（教师或学生回答问题）
 * - 查询答案列表（显示某个问题下的所有答案）
 * - 更新答案（修改内容、设置为最佳答案、更新点赞数）
 * - 删除答案（管理员删除不适合的内容）
 * 
 * 🏗️ 技术架构：
 * - 继承自MyBatis-Plus的BaseMapper<Answer>
 * - BaseMapper自动提供17个基础方法（增删改查）
 * - 我们添加了findByQuestionId方法，用于查询某个问题下的所有答案
 * - 答案会按照智能排序：最佳答案 > 热门答案 > 最新答案
 * 
 * 📊 对应数据库表: answer
 * 
 * 🔗 关联关系：
 * - 属于question表：通过answer.question_id = question.id （每个答案属于一个问题）
 * - 属于teacher表：通过answer.teacher_id = teacher.id （回答者信息）
 * - 一个问题可以有多个答案，但只有一个最佳答案（is_accepted=1）
 * 
 * 🔧 MyBatis-Plus提供的免费方法：
 * - insert(Answer a)：插入一个新答案
 * - deleteById(Long id)：删除指定答案
 * - updateById(Answer a)：更新答案信息
 * - selectById(Long id)：根据ID查询答案
 * - selectList(Wrapper)：查询答案列表
 * - selectCount(Wrapper)：统计答案数量
 * 
 * 💡 使用场景：
 * 1. 教师回答问题：调用insert()保存答案
 * 2. 显示问题详情：调用findByQuestionId()查询所有答案
 * 3. 学生点赞：调用updateById()增加like_count
 * 4. 学生采纳答案：调用updateById()设置is_accepted=1
 * 5. 管理员审核：调用deleteById()删除不合适答案
 * 
 * ⚠️ 重要提示：
 * 1. 这是一个接口，不需要编写实现类
 * 2. @Mapper注解让MyBatis自动生成实现代码
 * 3. default方法可以在接口中直接编写实现代码
 * 4. 每个问题只能有一个最佳答案，设置新的最佳答案前要取消旧的
 * 
 * @author QA System Team
 * @version 1.0
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {

    /**
     * 根据问题ID查询所有答案（智能排序）
     * 
     * 🎯 方法作用：
     * 查询某个问题下的所有答案，并按照智能排序，确保最有价值的答案显示在最前面。
     * 就像在问答网站（如知乎、百度知道）看问题一样，好的答案会自动排在前面。
     * 
     * 🏆 排序逻辑（优先级从高到低）：
     * 
     * 1. 第一优先级：是否为最佳答案（is_accepted）
     *    - is_accepted = 1 的答案排在最前面
     *    - 这是提问者认可的最佳答案，就像老师批改作业给的“优秀”
     *    - 一般情况下一个问题只有一个最佳答案
     * 
     * 2. 第二优先级：点赞数（like_count）
     *    - 点赞数多的答案排在前面
     *    - 表示这个答案获得了更多人的认可，质量较高
     *    - 就像商品评价里的“有用”数量
     * 
     * 3. 第三优先级：创建时间（create_time）
     *    - 如果点赞数相同，较新的答案排在前面
     *    - 因为新答案可能包含更新的信息或解决方案
     * 
     * 🔍 查询逻辑详解：
     * 
     * 1. 创建LambdaQueryWrapper查询条件构造器
     * 
     * 2. .eq(Answer::getQuestionId, questionId)
     *    - 设置查询条件：question_id = ?
     *    - 只查询属于指定问题的答案
     *    - 例如：questionId=100，就查询ID为100的问题下的所有答案
     * 
     * 3. .orderByDesc(Answer::getIsAccepted)
     *    - 第一次排序：按is_accepted降序
     *    - is_accepted=1的排在前，is_accepted=0的排在后
     *    - SQL：ORDER BY is_accepted DESC
     * 
     * 4. .orderByDesc(Answer::getLikeCount)
     *    - 第二次排序：在is_accepted相同的情况下，按like_count降序
     *    - 点赞数多的排在前
     *    - SQL：, like_count DESC
     * 
     * 5. .orderByDesc(Answer::getCreateTime)
     *    - 第三次排序：在前两个条件相同的情况下，按创建时间降序
     *    - 新答案排在前
     *    - SQL：, create_time DESC
     * 
     * 6. selectList()执行查询，返回答案列表
     * 
     * 📝 使用示例1 - 显示问题详情页面：
     * <pre>
     * // 学生点击查看问题ID为100的问题详情
     * Long questionId = 100L;
     * List<Answer> answers = answerMapper.findByQuestionId(questionId);
     * 
     * // 返回的答案列表已按智能排序：
     * // 1. 最佳答案（is_accepted=1）在最前面
     * // 2. 然后是点赞多的普通答案
     * // 3. 最后是较新的答案
     * 
     * // 在页面上显示
     * for (Answer answer : answers) {
     *     System.out.println("回答者: " + answer.getTeacherName());
     *     System.out.println("内容: " + answer.getContent());
     *     System.out.println("点赞数: " + answer.getLikeCount());
     *     if (answer.getIsAccepted() == 1) {
     *         System.out.println("[最佳答案]");  // 显示标记
     *     }
     * }
     * </pre>
     * 
     * 📝 使用示例2 - 统计问题回答数：
     * <pre>
     * // 在问题列表中显示每个问题有几个回答
     * List<Answer> answers = answerMapper.findByQuestionId(questionId);
     * int answerCount = answers.size();
     * System.out.println("该问题有 " + answerCount + " 个回答");
     * </pre>
     * 
     * 📝 使用示例3 - 检查是否有最佳答案：
     * <pre>
     * // 检查这个问题是否已有最佳答案
     * List<Answer> answers = answerMapper.findByQuestionId(questionId);
     * boolean hasAccepted = answers.stream()
     *     .anyMatch(answer -> answer.getIsAccepted() == 1);
     * 
     * if (hasAccepted) {
     *     System.out.println("该问题已有最佳答案");
     * } else {
     *     System.out.println("该问题还没有最佳答案，欢迎回答");
     * }
     * </pre>
     * 
     * 🎯 实际执行的SQL：
     * SELECT 
     *     id, question_id, teacher_id, content, is_accepted, 
     *     like_count, create_time, update_time
     * FROM answer
     * WHERE question_id = 100
     * ORDER BY 
     *     is_accepted DESC,    -- 最佳答案在最前
     *     like_count DESC,     -- 点赞多的排在前
     *     create_time DESC     -- 新答案排在前
     * 
     * @param questionId 问题ID（不能为null）
     * @return List<Answer> 答案列表（已排序）
     *         - 如果没有答案，返回空列表（不是null）
     *         - 最佳答案在列表第一位（如果有）
     *         - 每个Answer对象包含完整的答案信息
     * 
     * ⚠️ 注意事项：
     * 1. questionId不能为null，否则会抛NullPointerException
     * 2. 返回的列表不会null，但可能为空（size=0）
     * 3. 排序逻辑是多级排序，优先级为：最佳 > 点赞 > 时间
     * 4. 一个问题理论上只应有一个is_accepted=1的答案
     * 5. 如果需要分页，建议使用MyBatis-Plus的Page对象
     * 6. 如果需要同时查询回答者（Teacher）信息，需要额外的关联查询
     * 7. 点赞数（like_count）可能需要缓存优化，避免频繁更新数据库
     */
    default List<Answer> findByQuestionId(Long questionId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Answer>()
                .eq(Answer::getQuestionId, questionId)
                .orderByDesc(Answer::getIsAccepted)
                .orderByDesc(Answer::getLikeCount)
                .orderByDesc(Answer::getCreateTime));
    }
}

