package com.qasystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.common.util.RedisUtil;
import com.qasystem.dto.AnswerDTO;
import com.qasystem.dto.CreateAnswerRequest;
import com.qasystem.entity.Answer;
import com.qasystem.entity.Question;
import com.qasystem.entity.Teacher;
import com.qasystem.entity.User;
import com.qasystem.mapper.AnswerMapper;
import com.qasystem.mapper.QuestionMapper;
import com.qasystem.mapper.TeacherMapper;
import com.qasystem.mapper.UserMapper;
import com.qasystem.service.AnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 📝 回答服务实现类
 * 
 * 🎯 核心功能：教师回答问题、修改回答、删除回答、学生采纳回答
 * 🔒 权限控制：只有回答者可以修改/删除自己的回答；已采纳的回答不能修改
 * 💾 缓存策略：回答变化时清除问题缓存，保证数据一致性
 * ⚠️ 事务处理：创建/修改/删除/采纳回答时同时更新问题状态
 * @author 师生答疑系统开发团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    private static final String QUESTION_CACHE_KEY = "question:";

    /** 📋 根据问题ID查询所有回答 */
    @Override
    public List<AnswerDTO> getAnswersByQuestionId(Long questionId) {
        List<Answer> answers = answerMapper.findByQuestionId(questionId);
        return answers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ➕ 创建回答：验证问题状态，创建回答，更新问题状态为ANSWERED，清除缓存 */
    @Override
    @Transactional
    public AnswerDTO createAnswer(Long teacherId, CreateAnswerRequest request) {
        // 验证问题是否存在
        Question question = questionMapper.selectById(request.getQuestionId());
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        // 验证问题是否已关闭
        if ("CLOSED".equals(question.getStatus())) {
            throw new RuntimeException("问题已关闭，无法回答");
        }

        Answer answer = new Answer();
        answer.setQuestionId(request.getQuestionId());
        answer.setTeacherId(teacherId);
        answer.setContent(request.getContent());
        answer.setIsAccepted(0);
        answer.setLikeCount(0);

        // 处理图片列表
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                answer.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
            }
        }

        answerMapper.insert(answer);

        // 更新问题状态为已回答
        if ("PENDING".equals(question.getStatus())) {
            question.setStatus("ANSWERED");
            questionMapper.updateById(question);
        }

        // 清除问题缓存
        redisUtil.delete(QUESTION_CACHE_KEY + request.getQuestionId());

        log.info("创建回答成功: id={}, teacherId={}, questionId={}", 
                 answer.getId(), teacherId, request.getQuestionId());
        
        return convertToDTO(answer);
    }

    /** ✏️ 修改回答：只能修改自己的回答，已采纳的不能修改 */
    @Override
    @Transactional
    public AnswerDTO updateAnswer(Long id, Long teacherId, CreateAnswerRequest request) {
        Answer answer = answerMapper.selectById(id);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        // 验证是否是回答者本人
        if (!answer.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("只能修改自己的回答");
        }

        // 已被采纳的回答不能修改
        if (answer.getIsAccepted() == 1) {
            throw new RuntimeException("已被采纳的回答不能修改");
        }

        answer.setContent(request.getContent());

        if (request.getImages() != null) {
            try {
                answer.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
            }
        }

        answerMapper.updateById(answer);

        // 清除问题缓存
        redisUtil.delete(QUESTION_CACHE_KEY + answer.getQuestionId());

        log.info("更新回答成功: id={}", id);
        return convertToDTO(answer);
    }

    /** 🗑️ 删除回答：回答者或ADMIN可以删除，清除问题缓存 */
    @Override
    @Transactional
    public void deleteAnswer(Long id, Long userId) {
        Answer answer = answerMapper.selectById(id);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        // 验证权限（回答者本人或管理员）
        if (!answer.getTeacherId().equals(userId)) {
            User user = userMapper.selectById(userId);
            if (user == null || !"ADMIN".equals(user.getRole())) {
                throw new RuntimeException("无权删除此回答");
            }
        }

        answerMapper.deleteById(id);

        // 清除问题缓存
        redisUtil.delete(QUESTION_CACHE_KEY + answer.getQuestionId());

        log.info("删除回答成功: id={}", id);
    }

    /** ⭐ 采纳回答：只有提问者可以采纳，自动取消其他回答的采纳状态 */
    @Override
    @Transactional
    public void acceptAnswer(Long id, Long studentId) {
        Answer answer = answerMapper.selectById(id);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        // 验证问题
        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        // 验证是否是提问者本人
        if (!question.getStudentId().equals(studentId)) {
            throw new RuntimeException("只有提问者才能采纳回答");
        }

        // 取消其他回答的采纳状态
        List<Answer> answers = answerMapper.findByQuestionId(answer.getQuestionId());
        for (Answer a : answers) {
            if (a.getIsAccepted() == 1) {
                a.setIsAccepted(0);
                answerMapper.updateById(a);
            }
        }

        // 采纳当前回答
        answer.setIsAccepted(1);
        answerMapper.updateById(answer);

        // 清除问题缓存
        redisUtil.delete(QUESTION_CACHE_KEY + answer.getQuestionId());

        log.info("采纳回答成功: id={}, questionId={}", id, answer.getQuestionId());
    }

    /** 🔄 转换为DTO：关联查询教师信息，反序列化图片列表 */
    private AnswerDTO convertToDTO(Answer answer) {
        AnswerDTO dto = new AnswerDTO();
        BeanUtils.copyProperties(answer, dto);

        // 获取教师信息
        User teacher = userMapper.selectById(answer.getTeacherId());
        if (teacher != null) {
            dto.setTeacherName(teacher.getRealName());
            
            // 获取教师职称
            teacherMapper.findByUserId(answer.getTeacherId()).ifPresent(t -> {
                dto.setTeacherTitle(t.getTitle());
            });
        }

        // 解析图片列表
        if (answer.getImages() != null) {
            try {
                List<String> imageList = objectMapper.readValue(
                    answer.getImages(), 
                    new TypeReference<List<String>>() {}
                );
                dto.setImages(imageList);
            } catch (JsonProcessingException e) {
                log.error("图片列表解析失败", e);
            }
        }

        return dto;
    }
}

