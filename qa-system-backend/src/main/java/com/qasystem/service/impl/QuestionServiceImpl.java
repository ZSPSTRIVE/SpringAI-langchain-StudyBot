package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.common.util.RedisUtil;
import com.qasystem.dto.CreateQuestionRequest;
import com.qasystem.dto.QuestionDTO;
import com.qasystem.entity.Answer;
import com.qasystem.entity.Question;
import com.qasystem.entity.Subject;
import com.qasystem.entity.User;
import com.qasystem.mapper.AnswerMapper;
import com.qasystem.mapper.QuestionMapper;
import com.qasystem.mapper.SubjectMapper;
import com.qasystem.mapper.UserMapper;
import com.qasystem.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ❓ 问题服务实现类
 * 
 * 📖 这是什么？
 * 这是问题模块的核心业务实现，处理学生提问、修改问题、删除问题等操作。
 * 就像一个“问题管理系统”，学生可以发布问题、管理自己的问题、查看问题详情。
 * 
 * 🎯 核心功能实现：
 * 1. 问题查询：分页查询、单个查询，支持按科目、状态、关键词筛选
 * 2. 问题创建：学生发布新问题，支持上传图片
 * 3. 问题修改：只有提问者本人可以修改，且只能修改待回答的问题
 * 4. 问题删除：提问者或管理员可以删除
 * 5. 浏览计数：每次查看问题时自动增加浏览量
 * 6. 关闭问题：提问者可以主动关闭问题
 * 
 * 💾 缓存策略：
 * - 缓存Key："question:{id}"
 * - 缓存时间：24小时
 * - 查询时先从Redis获取，击中则直接返回，未命中再查数据库
 * - 更新/删除/关闭问题时自动清除缓存，保证数据一致性
 * 
 * 🔒 权限控制：
 * - 修改问题：只有提问者本人可以修改，且只能修改PENDING状态的问题
 * - 删除问题：提问者本人或ADMIN管理员可以删除
 * - 关闭问题：只有提问者本人可以关闭
 * 
 * 📝 数据转换：
 * - 图片列表使用JSON字符串存储，存入时序列化，读取时反序列化
 * - 转换为DTO时会关联查询科目名、学生姓名、回答数量
 * 
 * ⚠️ 事务处理：
 * - 创建、修改、删除、关闭、浏览计数等方法都使用@Transactional保证数据一致性
 * 
 * @author 师生答疑系统开发团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;
    private final UserMapper userMapper;
    private final AnswerMapper answerMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    /** 问题缓存Key前缀 */
    private static final String QUESTION_CACHE_KEY = "question:";
    /** 缓存过期时间：24小时 */
    private static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 📋 分页查询问题列表
     * 支持按科目、状态、关键词筛选，返回分页结果。
     */
    @Override
    public IPage<QuestionDTO> getQuestionPage(Integer page, Integer size, Long subjectId, String status, String keyword) {
        Page<Question> questionPage = new Page<>(page, size);
        IPage<Question> resultPage = questionMapper.selectQuestionPage(questionPage, subjectId, status, keyword);
        
        return resultPage.convert(this::convertToDTO);
    }

    /**
     * 🔍 根据ID查询问题详情
     * 先从Redis缓存获取，未命中再查数据库，并缓存24小时。
     */
    @Override
    public QuestionDTO getQuestionById(Long id) {
        // 先从缓存获取
        String cacheKey = QUESTION_CACHE_KEY + id;
        QuestionDTO cachedDTO = redisUtil.get(cacheKey, QuestionDTO.class);
        if (cachedDTO != null) {
            return cachedDTO;
        }

        // 查询数据库
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        QuestionDTO dto = convertToDTO(question);
        
        // 缓存结果
        redisUtil.set(cacheKey, dto, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return dto;
    }

    /**
     * ➕ 创建新问题
     * 
     * 执行流程：
     * 1. 验证科目是否存在
     * 2. 创建问题对象，默认状态为PENDING（待回答）
     * 3. 处理图片列表：将List<String>序列化为JSON字符串存储
     * 4. 插入数据库，返回DTO
     */
    @Override
    @Transactional
    public QuestionDTO createQuestion(Long studentId, CreateQuestionRequest request) {
        // 验证科目是否存在
        Subject subject = subjectMapper.selectById(request.getSubjectId());
        if (subject == null) {
            throw new RuntimeException("科目不存在");
        }

        Question question = new Question();
        question.setSubjectId(request.getSubjectId());
        question.setStudentId(studentId);
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setStatus("PENDING");
        question.setViewCount(0);
        question.setIsTop(0);
        question.setIsFeatured(0);

        // 处理图片列表
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                question.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
            }
        }

        questionMapper.insert(question);
        
        log.info("创建问题成功: id={}, studentId={}", question.getId(), studentId);
        return convertToDTO(question);
    }

    /**
     * ✏️ 修改问题
     * 
     * 权限限制：
     * 1. 只有提问者本人可以修改
     * 2. 只能修改PENDING（待回答）状态的问题
     * 3. 已有回答或已关闭的问题不能修改
     * 4. 修改后自动清除Redis缓存
     */
    @Override
    @Transactional
    public QuestionDTO updateQuestion(Long id, Long studentId, CreateQuestionRequest request) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        // 验证是否是提问者本人
        if (!question.getStudentId().equals(studentId)) {
            throw new RuntimeException("只能修改自己的问题");
        }

        // 只能修改待回答的问题
        if (!"PENDING".equals(question.getStatus())) {
            throw new RuntimeException("只能修改待回答的问题");
        }

        question.setTitle(request.getTitle());
        question.setContent(request.getContent());

        if (request.getImages() != null) {
            try {
                question.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
            }
        }

        questionMapper.updateById(question);
        
        // 清除缓存
        redisUtil.delete(QUESTION_CACHE_KEY + id);
        
        log.info("更新问题成功: id={}", id);
        return convertToDTO(question);
    }

    /**
     * 🗑️ 删除问题
     * 
     * 权限检查：
     * - 提问者本人可以删除自己的问题
     * - ADMIN管理员可以删除任意问题
     * - 删除后清除Redis缓存
     */
    @Override
    @Transactional
    public void deleteQuestion(Long id, Long userId) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        // 验证权限（提问者本人或管理员）
        if (!question.getStudentId().equals(userId)) {
            User user = userMapper.selectById(userId);
            if (user == null || !"ADMIN".equals(user.getRole())) {
                throw new RuntimeException("无权删除此问题");
            }
        }

        questionMapper.deleteById(id);
        
        // 清除缓存
        redisUtil.delete(QUESTION_CACHE_KEY + id);
        
        log.info("删除问题成功: id={}", id);
    }

    /**
     * 👁️ 增加浏览次数
     * 每次查看问题详情时调用，浏览量+1，并清除缓存。
     */
    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        Question question = questionMapper.selectById(id);
        if (question != null) {
            question.setViewCount(question.getViewCount() + 1);
            questionMapper.updateById(question);
            
            // 清除缓存
            redisUtil.delete(QUESTION_CACHE_KEY + id);
        }
    }

    /**
     * 🚪 关闭问题
     * 提问者可以主动关闭问题，状态变为CLOSED，不再接受新回答。
     */
    @Override
    @Transactional
    public void closeQuestion(Long id, Long studentId) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        if (!question.getStudentId().equals(studentId)) {
            throw new RuntimeException("只能关闭自己的问题");
        }

        question.setStatus("CLOSED");
        questionMapper.updateById(question);
        
        // 清除缓存
        redisUtil.delete(QUESTION_CACHE_KEY + id);
        
        log.info("关闭问题成功: id={}", id);
    }

    /**
     * 🔄 将Question实体转换为QuestionDTO
     * 
     * 转换过程：
     * 1. 复制基本属性
     * 2. 关联查询科目名称
     * 3. 关联查询学生姓名
     * 4. 反序列化JSON图片列表
     * 5. 统计回答数量
     */
    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        BeanUtils.copyProperties(question, dto);

        // 获取科目名称
        Subject subject = subjectMapper.selectById(question.getSubjectId());
        if (subject != null) {
            dto.setSubjectName(subject.getName());
        }

        // 获取学生姓名
        User student = userMapper.selectById(question.getStudentId());
        if (student != null) {
            dto.setStudentName(student.getRealName());
        }

        // 解析图片列表
        if (question.getImages() != null) {
            try {
                List<String> imageList = objectMapper.readValue(
                    question.getImages(), 
                    new TypeReference<List<String>>() {}
                );
                dto.setImages(imageList);
            } catch (JsonProcessingException e) {
                log.error("图片列表解析失败", e);
            }
        }

        // 获取回答数量
        List<Answer> answers = answerMapper.findByQuestionId(question.getId());
        dto.setAnswerCount(answers.size());

        return dto;
    }
}

