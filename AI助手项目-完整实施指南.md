# 🤖 AI助手项目 - 完整实施指南

## 📋 项目文件清单

### ✅ 已创建的文件

1. **后端配置**
   - ✅ `pom.xml` - 添加LangChain4j依赖
   - ✅ `application.yml` - AI配置
   - ✅ `LangChain4jConfig.java` - LangChain配置类

2. **数据库**
   - ✅ `ai-schema.sql` - AI对话表结构

3. **实体和DTO**
   - ✅ `AiConversation.java` - 对话实体
   - ✅ `AiChatRequest.java` - 请求DTO
   - ✅ `AiChatResponse.java` - 响应DTO

4. **数据访问层**
   - ✅ `AiConversationMapper.java` - Mapper接口

5. **服务层**
   - ✅ `AiAssistantService.java` - 服务接口
   - ⏳ `AiAssistantServiceImpl.java` - 需要创建（代码在文档中）

6. **控制器**
   - ⏳ `AiAssistantController.java` - 需要创建（代码在文档中）

---

## 🚀 快速开始步骤

### 步骤 1: 执行数据库脚本
```sql
-- 在MySQL中执行
USE qa_system_v2;

SOURCE D:/WorkSpace/项目/毕业设计-师生答疑系统/服务器第一版 - 副本/qa-system-backend/src/main/resources/db/ai-schema.sql;
```

### 步骤 2: 创建Service实现类

创建文件: `qa-system-backend/src/main/java/com/qasystem/service/impl/AiAssistantServiceImpl.java`

由于代码太长（约500行），请参考 `AI助手完整开发文档-Phase1-2.md` 中的完整代码。

关键点：
- 集成LangChain4j的ChatLanguageModel
- 实现多轮对话上下文管理
- Redis缓存优化
- 自动问题分类
- 学习资源推荐

### 步骤 3: 创建Controller

创建文件: `qa-system-backend/src/main/java/com/qasystem/controller/AiAssistantController.java`

完整代码见文档。

### 步骤 4: 配置Spring Security

在 `SecurityConfig.java` 中添加AI助手接口的权限配置：

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        // ... 其他配置 ...
        .authorizeHttpRequests(auth -> auth
            // AI助手接口（需要认证）
            .requestMatchers("/api/v1/assistant/**").authenticated()
            // ... 其他配置 ...
        )
        .build();
}
```

### 步骤 5: 测试后端API

```bash
# 1. 启动后端
cd qa-system-backend
mvn spring-boot:run

# 2. 登录获取Token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"123zxc","password":"123456"}'

# 3. 测试AI对话
curl -X POST http://localhost:8080/api/v1/assistant/chat \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是二叉搜索树？",
    "needResources": true
  }'
```

---

## 🎨 Phase 2: 前端开发

### 前端文件结构
```
qa-system-frontend/src/
├── views/
│   └── ai/
│       ├── AiChat.vue          # AI聊天主页面
│       └── AiHistory.vue       # 对话历史
├── components/
│   └── ai/
│       ├── ChatMessage.vue     # 单条消息组件
│       ├── ChatInput.vue       # 输入框组件
│       ├── CodeBlock.vue       # 代码高亮组件
│       └── MarkdownRenderer.vue # Markdown渲染
├── api/
│   └── ai.js                   # AI API调用
└── stores/
    └── aiChat.js               # AI聊天状态管理
```

### 安装前端依赖
```bash
cd qa-system-frontend

# Markdown渲染
npm install marked highlight.js

# 代码复制功能
npm install clipboard

# 类型检查
npm install -D @types/marked
```

### 创建API接口文件

```javascript
// qa-system-frontend/src/api/ai.js
import request from '@/utils/request'

/**
 * 与AI对话
 */
export function chatWithAi(data) {
  return request({
    url: '/v1/assistant/chat',
    method: 'post',
    data
  })
}

/**
 * 获取会话历史
 */
export function getSessionHistory(sessionId) {
  return request({
    url: `/v1/assistant/history/${sessionId}`,
    method: 'get'
  })
}

/**
 * 获取最近对话
 */
export function getRecentChats(limit = 10) {
  return request({
    url: '/v1/assistant/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 标记回答是否有帮助
 */
export function markFeedback(conversationId, helpful) {
  return request({
    url: `/v1/assistant/feedback/${conversationId}`,
    method: 'put',
    params: { helpful }
  })
}

/**
 * 清除会话
 */
export function clearSession(sessionId) {
  return request({
    url: `/v1/assistant/clear/${sessionId}`,
    method: 'delete'
  })
}
```

### 创建Pinia Store

```javascript
// qa-system-frontend/src/stores/aiChat.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatWithAi, clearSession as clearSessionApi } from '@/api/ai'
import { ElMessage } from 'element-plus'

export const useAiChatStore = defineStore('aiChat', () => {
  const sessionId = ref(null)
  const messages = ref([])
  const loading = ref(false)

  // 添加消息
  const addMessage = (message) => {
    messages.value.push({
      id: Date.now() + Math.random(),
      ...message,
      timestamp: new Date()
    })
  }

  // 发送消息
  const sendMessage = async (question, needResources = false) => {
    if (!question.trim()) return

    // 添加用户消息
    addMessage({
      role: 'user',
      content: question
    })

    loading.value = true

    try {
      const response = await chatWithAi({
        sessionId: sessionId.value,
        question,
        needResources
      })

      // 更新sessionId
      if (!sessionId.value) {
        sessionId.value = response.data.sessionId
      }

      // 添加AI回复
      addMessage({
        role: 'assistant',
        content: response.data.answer,
        category: response.data.category,
        resources: response.data.recommendedResources,
        responseTime: response.data.responseTime
      })
    } catch (error) {
      console.error('AI对话失败:', error)
      ElMessage.error('AI对话失败，请稍后重试')
      
      // 添加错误消息
      addMessage({
        role: 'assistant',
        content: '抱歉，我暂时无法回答您的问题。请稍后再试。',
        error: true
      })
    } finally {
      loading.value = false
    }
  }

  // 清除会话
  const clearSession = async () => {
    if (sessionId.value) {
      try {
        await clearSessionApi(sessionId.value)
      } catch (error) {
        console.error('清除会话失败:', error)
      }
    }
    
    sessionId.value = null
    messages.value = []
  }

  return {
    sessionId,
    messages,
    loading,
    addMessage,
    sendMessage,
    clearSession
  }
})
```

---

## 📦 完整代码文件下载

由于代码量非常大，建议分步骤创建：

1. **Phase 1.1** (已完成):
   - Maven依赖 ✅
   - 配置文件 ✅
   - 实体类 ✅
   - 数据库表 ✅

2. **Phase 1.2** (需要完成):
   - Service实现 (500行代码)
   - Controller (100行代码)

3. **Phase 2** (前端开发):
   - 聊天界面 Vue组件
   - Markdown + 代码高亮
   - 响应式设计

---

## 🎯 下一步行动

### 立即执行:
1. 复制Service实现代码到项目
2. 复制Controller代码到项目
3. 执行数据库脚本
4. 启动测试后端
5. 使用Postman测试API

### 前端开发（下一阶段）:
参考 ChatGPT/DeepSeek UI风格开发聊天界面。

---

**需要我立即创建完整的Service实现和Controller文件吗？或者继续Phase 2前端开发？**

