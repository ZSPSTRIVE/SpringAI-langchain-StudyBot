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
 * 问题服务实现
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

    private static final String QUESTION_CACHE_KEY = "question:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    public IPage<QuestionDTO> getQuestionPage(Integer page, Integer size, Long subjectId, String status, String keyword) {
        Page<Question> questionPage = new Page<>(page, size);
        IPage<Question> resultPage = questionMapper.selectQuestionPage(questionPage, subjectId, status, keyword);
        
        return resultPage.convert(this::convertToDTO);
    }

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

