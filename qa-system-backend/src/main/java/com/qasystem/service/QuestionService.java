package com.qasystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qasystem.dto.CreateQuestionRequest;
import com.qasystem.dto.QuestionDTO;

/**
 * QuestionService - 问题服务接口
 * 
 * 🎯 作用：处理所有与问题相关的业务逻辑
 * 就像一个“问题管理员”，负责处理学生提问、查看问题、编辑问题等所有操作。
 * 
 * 📝 主要功能：
 * 1. 问题查询：分页、筛选、搜索、查看详情
 * 2. 问题管理：创建、编辑、删除、关闭
 * 3. 数据统计：浏览数、回答数
 * 
 * 💡 使用流程：
 * 1. 学生访问问题列表页 → 调用 getQuestionPage()
 * 2. 学生点击某个问题 → 调用 getQuestionById() + incrementViewCount()
 * 3. 学生发布新问题 → 调用 createQuestion()
 * 4. 学生编辑自己的问题 → 调用 updateQuestion()
 * 5. 学生删除自己的问题 → 调用 deleteQuestion()
 * 6. 学生关闭已解决的问题 → 调用 closeQuestion()
 */
public interface QuestionService {

    /**
     * 分页查询问题列表
     * 
     * 🎯 功能：根据条件分页查询问题，支持多种筛选和搜索
     * 就像在图书馆搜索书籍，可以按分类、状态、关键词筛选。
     * 
     * @param page 页码，从1开始，第1页、第2页...
     * @param size 每页数量，如每页显示10个问题
     * @param subjectId 科目ID，用于筛选某个科目的问题，为null则不限制
     * @param status 问题状态，PENDING/RESOLVED/CLOSED，为null则不限制
     * @param keyword 搜索关键词，在标题和内容中搜索，为null则不搜索
     * @return 分页结果，包含总数、当前页数据等
     * 
     * 💬 使用场景：
     * - 首页显示所有问题：getQuestionPage(1, 10, null, null, null)
     * - 查看数据结构科目的问题：getQuestionPage(1, 10, 1L, null, null)
     * - 搜索包含“二叉树”的问题：getQuestionPage(1, 10, null, null, "二叉树")
     * - 查看已解决的问题：getQuestionPage(1, 10, null, "RESOLVED", null)
     */
    IPage<QuestionDTO> getQuestionPage(Integer page, Integer size, Long subjectId, String status, String keyword);

    /**
     * 根据ID获取问题详情
     * 
     * 🎯 功能：查询某个问题的完整信息
     * 包括问题内容、科目、提问学生、回答数、浏览数等。
     * 
     * @param id 问题ID
     * @return 问题详情，如果不存在则抛出异常
     * 
     * 💬 使用场景：
     * - 用户点击问题列表中的某个问题
     * - 进入问题详情页，显示完整内容和所有回答
     */
    QuestionDTO getQuestionById(Long id);

    /**
     * 创建新问题
     * 
     * 🎯 功能：学生发布一个新的问题
     * 就像学生在答疑平台填写提问表单并提交。
     * 
     * @param studentId 提问的学生ID，从当前登录用户获取
     * @param request 问题请求数据，包含科目、标题、内容、图片等
     * @return 创建成功的问题详情，包含自动生成的ID和时间
     * 
     * 💬 使用场景：
     * - 学生在“提问”页面填写表单
     * - 选择科目、输入标题和内容、上传图片
     * - 点击“发布问题”按钮，调用此方法
     */
    QuestionDTO createQuestion(Long studentId, CreateQuestionRequest request);

    /**
     * 更新问题
     * 
     * 🎯 功能：学生修改自己发布的问题
     * 只有问题的作者（提问的学生）才能编辑，其他人无权限。
     * 
     * @param id 要更新的问题ID
     * @param studentId 当前操作的学生ID，用于权限验证
     * @param request 新的问题数据
     * @return 更新后的问题详情
     * 
     * 💬 使用场景：
     * - 学生发现自己的问题有错别字或表达不清
     * - 在问题详情页点击“编辑”按钮
     * - 修改内容后提交，调用此方法
     */
    QuestionDTO updateQuestion(Long id, Long studentId, CreateQuestionRequest request);

    /**
     * 删除问题
     * 
     * 🎯 功能：删除某个问题
     * 只有问题作者或管理员才能删除，删除后相关的回答也会被删除。
     * 
     * @param id 要删除的问题ID
     * @param userId 当前操作的用户ID，用于权限验证
     * 
     * 💬 使用场景：
     * - 学生想删除自己的问题（可能是重复或不再需要）
     * - 管理员删除违规问题
     */
    void deleteQuestion(Long id, Long userId);

    /**
     * 增加问题浏览次数
     * 
     * 🎯 功能：记录问题被查看的次数
     * 就像文章的阅读量，每次打开问题详情页时+1。
     * 
     * @param id 问题ID
     * 
     * 💬 使用场景：
     * - 用户点击问题进入详情页时自动调用
     * - 通常与getQuestionById()一起使用
     * - 浏览数可以用来显示问题的热度
     */
    void incrementViewCount(Long id);

    /**
     * 关闭问题
     * 
     * 🎯 功能：学生主动关闭自己的问题
     * 当问题已经解决或不再需要时，学生可以关闭问题，状态变为CLOSED。
     * 
     * @param id 问题ID
     * @param studentId 学生ID，只有问题作者才能关闭
     * 
     * 💬 使用场景：
     * - 学生的问题已经得到满意的回答
     * - 或者问题已经自己解决，不再需要回答
     * - 点击“关闭问题”按钮，调用此方法
     */
    void closeQuestion(Long id, Long studentId);
}

