# 🤖 AI助手完整开发文档 - Phase 1 & 2

## ✅ Phase 1.1 已完成

### 1. Maven依赖添加
```xml
<!-- pom.xml -->
<properties>
    <langchain4j.version>0.35.0</langchain4j.version>
</properties>

<dependencies>
    <!-- LangChain4j - Core -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    
    <!-- LangChain4j - DashScope (阿里百练) -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-dashscope</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    
    <!-- LangChain4j - Spring Boot Starter -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-spring-boot-starter</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
</dependencies>
```

### 2. 配置文件
```yaml
# application.yml
langchain4j:
  dashscope:
    api-key: sk-d710b29d19da44178d68fce9b33db898
    model-name: qwen-plus
    temperature: 0.7
    timeout: 60s
    max-tokens: 2000
  chat-model:
    language: zh_cn
```

### 3. 配置类
已创建: `LangChain4jConfig.java`

### 4. 实体类和DTO
已创建:
- `AiConversation.java` - 对话历史实体
- `AiChatRequest.java` - 聊天请求DTO
- `AiChatResponse.java` - 聊天响应DTO

### 5. 数据库表
已创建: `ai-schema.sql`

---

## 🚀 Phase 1.2: 实现AI助手服务接口

### 步骤1: 创建Mapper接口

```java
// AiConversationMapper.java
package com.qasystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qasystem.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {

    /**
     * 根据会话ID查询对话历史
     */
    @Select("SELECT * FROM ai_conversation WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY create_time ASC")
    List<AiConversation> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID查询最近的对话
     */
    @Select("SELECT * FROM ai_conversation WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<AiConversation> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
```

### 步骤2: 创建Service接口

```java
// AiAssistantService.java
package com.qasystem.service;

import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.entity.AiConversation;

import java.util.List;

public interface AiAssistantService {

    /**
     * 与AI助手对话
     */
    AiChatResponse chat(Long userId, AiChatRequest request);

    /**
     * 获取会话历史
     */
    List<AiConversation> getSessionHistory(String sessionId);

    /**
     * 获取用户最近的对话
     */
    List<AiConversation> getUserRecentChats(Long userId, int limit);

    /**
     * 标记回答是否有帮助
     */
    void markHelpful(Long conversationId, boolean helpful);

    /**
     * 清除会话历史
     */
    void clearSession(String sessionId);
}
```

### 步骤3: 创建Service实现（核心逻辑）

```java
// AiAssistantServiceImpl.java
package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qasystem.common.util.RedisUtil;
import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.entity.AiConversation;
import com.qasystem.entity.User;
import com.qasystem.mapper.AiConversationMapper;
import com.qasystem.mapper.UserMapper;
import com.qasystem.service.AiAssistantService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final ChatLanguageModel chatLanguageModel;
    private final AiConversationMapper conversationMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            你是一个专业的计算机专业课程学习助手，专门帮助学生学习以下计算机科学课程：
            - 数据结构与算法
            - 操作系统
            - 计算机网络
            - 数据库系统
            - 编程语言（Java、C++、Python等）
            - 软件工程
            
            你的职责是：
            1. 准确回答学生关于计算机专业课程的问题
            2. 提供清晰的解释和示例代码
            3. 推荐相关的学习资源
            4. 引导学生深入思考，而不是直接给出答案
            5. 使用中文回答，语言简洁专业
            
            请以友好、耐心的方式帮助学生学习。
            """;

    private static final String CACHE_KEY_PREFIX = "ai:chat:";
    private static final long CACHE_EXPIRE_MINUTES = 60;

    @Override
    @Transactional
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 生成或使用现有sessionId
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        // 3. 检查缓存
        String cacheKey = CACHE_KEY_PREFIX + sessionId + ":" + request.getQuestion().hashCode();
        AiChatResponse cachedResponse = redisUtil.get(cacheKey, AiChatResponse.class);
        if (cachedResponse != null) {
            log.info("✅ 从缓存返回答案: sessionId={}", sessionId);
            return cachedResponse;
        }

        // 4. 构建对话上下文
        List<ChatMessage> messages = buildConversationContext(sessionId, request.getQuestion());

        // 5. 调用AI模型
        log.info("🤖 调用AI模型: sessionId={}, question={}", sessionId, request.getQuestion());
        Response<AiMessage> aiResponse = chatLanguageModel.generate(messages);
        String answer = aiResponse.content().text();

        // 6. 分析问题类别
        String category = analyzeQuestionCategory(request.getQuestion());

        // 7. 生成学习资源推荐
        List<AiChatResponse.ResourceRecommendation> resources = null;
        if (request.getNeedResources()) {
            resources = generateResourceRecommendations(category, request.getQuestion());
        }

        // 8. 保存对话历史
        AiConversation conversation = new AiConversation();
        conversation.setSessionId(sessionId);
        conversation.setUserId(userId);
        conversation.setUsername(user.getUsername());
        conversation.setRole(user.getRole());
        conversation.setQuestion(request.getQuestion());
        conversation.setAnswer(answer);
        conversation.setCategory(category);
        conversation.setResponseTime(System.currentTimeMillis() - startTime);

        if (resources != null && !resources.isEmpty()) {
            try {
                conversation.setRecommendedResources(objectMapper.writeValueAsString(resources));
            } catch (Exception e) {
                log.warn("资源推荐序列化失败", e);
            }
        }

        conversationMapper.insert(conversation);

        // 9. 构建响应
        AiChatResponse response = AiChatResponse.builder()
                .sessionId(sessionId)
                .answer(answer)
                .category(category)
                .recommendedResources(resources)
                .responseTime(System.currentTimeMillis() - startTime)
                .build();

        // 10. 缓存结果
        redisUtil.set(cacheKey, response, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("✅ AI回答完成: sessionId={}, responseTime={}ms", sessionId, response.getResponseTime());
        return response;
    }

    /**
     * 构建对话上下文（支持多轮对话）
     */
    private List<ChatMessage> buildConversationContext(String sessionId, String currentQuestion) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统提示
        messages.add(SystemMessage.from(SYSTEM_PROMPT));

        // 获取历史对话（最近5轮）
        List<AiConversation> history = conversationMapper.findBySessionId(sessionId);
        if (!history.isEmpty()) {
            // 只保留最近5轮对话
            List<AiConversation> recentHistory = history.stream()
                    .skip(Math.max(0, history.size() - 5))
                    .collect(Collectors.toList());

            for (AiConversation conv : recentHistory) {
                messages.add(UserMessage.from(conv.getQuestion()));
                messages.add(AiMessage.from(conv.getAnswer()));
            }
        }

        // 添加当前问题
        messages.add(UserMessage.from(currentQuestion));

        return messages;
    }

    /**
     * 分析问题类别
     */
    private String analyzeQuestionCategory(String question) {
        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("数据结构") || lowerQuestion.contains("链表") || 
            lowerQuestion.contains("树") || lowerQuestion.contains("图") || 
            lowerQuestion.contains("栈") || lowerQuestion.contains("队列")) {
            return "数据结构";
        } else if (lowerQuestion.contains("算法") || lowerQuestion.contains("排序") || 
                   lowerQuestion.contains("查找") || lowerQuestion.contains("递归")) {
            return "算法";
        } else if (lowerQuestion.contains("操作系统") || lowerQuestion.contains("进程") || 
                   lowerQuestion.contains("线程") || lowerQuestion.contains("内存")) {
            return "操作系统";
        } else if (lowerQuestion.contains("数据库") || lowerQuestion.contains("sql") || 
                   lowerQuestion.contains("事务") || lowerQuestion.contains("索引")) {
            return "数据库";
        } else if (lowerQuestion.contains("网络") || lowerQuestion.contains("tcp") || 
                   lowerQuestion.contains("http") || lowerQuestion.contains("协议")) {
            return "计算机网络";
        } else if (lowerQuestion.contains("java") || lowerQuestion.contains("python") || 
                   lowerQuestion.contains("c++") || lowerQuestion.contains("编程")) {
            return "编程语言";
        }

        return "通用";
    }

    /**
     * 生成学习资源推荐
     */
    private List<AiChatResponse.ResourceRecommendation> generateResourceRecommendations(
            String category, String question) {
        
        List<AiChatResponse.ResourceRecommendation> resources = new ArrayList<>();

        // 根据类别推荐资源（这里是示例，实际可以从数据库或配置文件读取）
        switch (category) {
            case "数据结构":
                resources.add(AiChatResponse.ResourceRecommendation.builder()
                        .title("《数据结构与算法分析（Java语言描述）》")
                        .type("书籍")
                        .description("经典的数据结构教材，Java实现")
                        .build());
                resources.add(AiChatResponse.ResourceRecommendation.builder()
                        .title("LeetCode 数据结构专题")
                        .type("在线练习")
                        .url("https://leetcode.cn/problemset/")
                        .description("大量数据结构相关练习题")
                        .build());
                break;

            case "算法":
                resources.add(AiChatResponse.ResourceRecommendation.builder()
                        .title("《算法导论》")
                        .type("书籍")
                        .description("算法领域的经典著作")
                        .build());
                resources.add(AiChatResponse.ResourceRecommendation.builder()
                        .title("可视化算法")
                        .type("在线工具")
                        .url("https://visualgo.net/zh")
                        .description("算法可视化学习工具")
                        .build());
                break;

            case "操作系统":
                resources.add(AiChatResponse.ResourceRecommendation.builder()
                        .title("《深入理解计算机系统》")
                        .type("书籍")
                        .description("CSAPP，操作系统经典教材")
                        .build());
                break;

            case "数据库":
                resources.add(AiChatResponse.ResourceRecommendation.builder()
                        .title("《MySQL必知必会》")
                        .type("书籍")
                        .description("MySQL入门经典")
                        .build());
                break;

            case "计算机网络":
                resources.add(AiChatResponse.ResourceRecommendation.builder()
                        .title("《计算机网络：自顶向下方法》")
                        .type("书籍")
                        .description("网络原理经典教材")
                        .build());
                break;
        }

        return resources;
    }

    @Override
    public List<AiConversation> getSessionHistory(String sessionId) {
        return conversationMapper.findBySessionId(sessionId);
    }

    @Override
    public List<AiConversation> getUserRecentChats(Long userId, int limit) {
        return conversationMapper.findRecentByUserId(userId, limit);
    }

    @Override
    @Transactional
    public void markHelpful(Long conversationId, boolean helpful) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            conversation.setHelpful(helpful);
            conversationMapper.updateById(conversation);
        }
    }

    @Override
    @Transactional
    public void clearSession(String sessionId) {
        conversationMapper.delete(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getSessionId, sessionId));
        
        log.info("🗑️ 已清除会话历史: sessionId={}", sessionId);
    }
}
```

### 步骤4: 创建Controller

```java
// AiAssistantController.java
package com.qasystem.controller;

import com.qasystem.common.response.Result;
import com.qasystem.dto.AiChatRequest;
import com.qasystem.dto.AiChatResponse;
import com.qasystem.entity.AiConversation;
import com.qasystem.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI助手控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    /**
     * 与AI助手对话
     */
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(
            Authentication authentication,
            @Valid @RequestBody AiChatRequest request) {
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("🤖 AI对话请求: userId={}, question={}", userId, request.getQuestion());

        AiChatResponse response = aiAssistantService.chat(userId, request);
        return Result.success("对话成功", response);
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history/{sessionId}")
    public Result<List<AiConversation>> getHistory(
            @PathVariable String sessionId) {
        
        List<AiConversation> history = aiAssistantService.getSessionHistory(sessionId);
        return Result.success(history);
    }

    /**
     * 获取最近对话
     */
    @GetMapping("/recent")
    public Result<List<AiConversation>> getRecent(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        
        Long userId = (Long) authentication.getPrincipal();
        List<AiConversation> recent = aiAssistantService.getUserRecentChats(userId, limit);
        return Result.success(recent);
    }

    /**
     * 标记回答是否有帮助
     */
    @PutMapping("/feedback/{conversationId}")
    public Result<Void> feedback(
            @PathVariable Long conversationId,
            @RequestParam boolean helpful) {
        
        aiAssistantService.markHelpful(conversationId, helpful);
        return Result.success("反馈成功", null);
    }

    /**
     * 清除会话历史
     */
    @DeleteMapping("/clear/{sessionId}")
    public Result<Void> clearSession(@PathVariable String sessionId) {
        aiAssistantService.clearSession(sessionId);
        return Result.success("会话已清除", null);
    }
}
```

---

## ✅ 完成Phase 1.2后的测试

### 1. 启动后端
```bash
cd qa-system-backend
mvn clean compile
mvn spring-boot:run
```

### 2. 测试API（使用Postman或curl）

```bash
# 登录获取Token
POST http://localhost:8080/api/v1/auth/login
{
  "username": "123zxc",
  "password": "123456"
}

# 与AI对话
POST http://localhost:8080/api/v1/assistant/chat
Headers:
  Authorization: Bearer YOUR_TOKEN
Body:
{
  "question": "请解释一下什么是二叉搜索树？",
  "needResources": true
}

# 获取会话历史
GET http://localhost:8080/api/v1/assistant/history/{sessionId}
Headers:
  Authorization: Bearer YOUR_TOKEN

# 标记有帮助
PUT http://localhost:8080/api/v1/assistant/feedback/1?helpful=true
Headers:
  Authorization: Bearer YOUR_TOKEN
```

---

## 📝 下一步：Phase 2 前端开发

Phase 2将在另一个文档中详细说明，包括：
- ChatGPT风格的聊天界面
- 实时对话流式传输
- 代码高亮显示
- Markdown渲染
- 响应式设计

**Phase 1完成！继续Phase 2开发...**

