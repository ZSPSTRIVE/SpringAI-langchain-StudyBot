package com.qasystem.controller.admin;

import com.qasystem.common.response.Result;
import com.qasystem.entity.AiModelConfig;
import com.qasystem.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🤖 AI模型配置管理控制器 - 管理AI助手使用的各种大语言模型配置
 * 
 * 📖 功能说明：
 * AI模型配置模块用于管理系统集成的各种大语言模型，如OpenAI GPT、百度文心一言、阿里通义千问等。
 * 本控制器主要功能包括：
 * 1. 模型查询 - 获取所有模型配置、当前激活模型、支持的厂商列表
 * 2. 模型管理 - 创建、更新、删除模型配置
 * 3. 模型切换 - 设置当前系统使用的AI模型
 * 4. 连接测试 - 验证模型配置是否正确、API是否可用
 * 
 * 🔒 权限控制：
 * - 所有接口：仅管理员可访问
 * - 需要管理员角色认证
 * 
 * 🌍 RESTful 设计：
 * GET    /api/v1/admin/ai-models           获取所有模型配置
 * GET    /api/v1/admin/ai-models/active     获取当前激活模型
 * GET    /api/v1/admin/ai-models/providers  获取支持的厂商列表
 * POST   /api/v1/admin/ai-models           创建或更新模型配置
 * PUT    /api/v1/admin/ai-models/{id}/active 设置为激活模型
 * DELETE /api/v1/admin/ai-models/{id}       删除模型配置
 * POST   /api/v1/admin/ai-models/{id}/test   测试模型连接
 * 
 * 📝 业务规则：
 * - 系统同时只能有一个激活的模型
 * - 删除模型前需要验证是否为当前激活模型
 * - 测试连接会实际调用API，可能产生少量费用
 * - 模型配置包含API密钥等敏感信息，需要加密存储
 * 
 * @author 师生答疑系统开发团队
 * @since 2.0.0
 */
@Slf4j  // 自动生成日志对象
@RestController  // 标识这是一个REST控制器
@RequestMapping("/api/v1/admin/ai-models")  // 定义AI模型配置接口的基础路径
@RequiredArgsConstructor  // 为final字段生成构造函数
public class AiModelConfigController {
    
    // AI模型配置服务层接口
    private final AiModelConfigService aiModelConfigService;
    
    /**
     * 📋 获取所有AI模型配置 - 管理员查看系统中配置的所有AI模型
     * 
     * 业务流程：
     * 1. 验证当前用户是否为管理员
     * 2. 查询数据库中的所有模型配置
     * 3. 过滤敏感信息（如API密钥只显示前几位）
     * 4. 按创建时间倒序排列
     * 5. 返回模型配置列表
     * 
     * 返回信息包含：
     * - 模型基本信息：ID、名称、厂商、模型类型
     * - 配置信息：API地址、版本、参数设置
     * - 状态信息：是否激活、创建时间、更新时间
     * - 统计信息：使用次数、成功率等
     * 
     * 请求示例：
     * GET /api/v1/admin/ai-models
     * Headers:
     *   Authorization: Bearer admin_token_here
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": [
     *         {
     *             "id": 1,
     *             "name": "GPT-4",
     *             "provider": "OpenAI",
     *             "model": "gpt-4",
     *             "apiUrl": "https://api.openai.com/v1/chat/completions",
     *             "apiKey": "sk-1234****5678",
     *             "isActive": true,
     *             "maxTokens": 4096,
     *             "temperature": 0.7,
     *             "createdAt": "2024-01-15T10:30:00",
     *             "updatedAt": "2024-01-15T10:30:00"
     *         },
     *         {
     *             "id": 2,
     *             "name": "文心一言",
     *             "provider": "百度",
     *             "model": "ernie-bot-4",
     *             "apiUrl": "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions",
     *             "apiKey": "abcd****efgh",
     *             "isActive": false,
     *             "maxTokens": 4096,
     *             "temperature": 0.7,
     *             "createdAt": "2024-01-15T11:00:00",
     *             "updatedAt": "2024-01-15T11:00:00"
     *         }
     *     ]
     * }
     * 
     * @return Result<List<AiModelConfig>> 统一响应对象，包含所有模型配置列表
     */
    @GetMapping  // 处理GET请求，完整路径：/api/v1/admin/ai-models
    public Result<List<AiModelConfig>> listAll() {
        // 记录查询日志
        log.info("管理员查询所有AI模型配置");
        // 调用服务层查询所有模型配置
        List<AiModelConfig> configs = aiModelConfigService.listAll();
        // 返回模型配置列表
        return Result.success(configs);
    }
    
    /**
     * 🔍 获取当前激活的模型配置 - 获取系统当前正在使用的AI模型
     * 
     * 业务流程：
     * 1. 验证当前用户是否为管理员
     * 2. 查询当前激活状态的模型配置
     * 3. 如果没有激活模型，返回默认错误
     * 4. 过滤敏感信息（API密钥部分隐藏）
     * 5. 返回激活模型的完整配置
     * 
     * 使用场景：
     * - 管理员查看当前系统使用的AI模型
     * - 前端显示当前AI助手的模型信息
     * - 系统初始化时加载默认模型
     * 
     * 请求示例：
     * GET /api/v1/admin/ai-models/active
     * Headers:
     *   Authorization: Bearer admin_token_here
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": {
     *         "id": 1,
     *         "name": "GPT-4",
     *         "provider": "OpenAI",
     *         "model": "gpt-4",
     *         "apiUrl": "https://api.openai.com/v1/chat/completions",
     *         "apiKey": "sk-1234****5678",
     *         "isActive": true,
     *         "maxTokens": 4096,
     *         "temperature": 0.7,
     *         "createdAt": "2024-01-15T10:30:00",
     *         "updatedAt": "2024-01-15T10:30:00"
     *     }
     * }
     * 
     * 错误响应示例（无激活模型）：
     * {
     *     "code": 404,
     *     "message": "未找到激活的AI模型配置",
     *     "data": null
     * }
     * 
     * @return Result<AiModelConfig> 统一响应对象，包含当前激活的模型配置
     */
    @GetMapping("/active")  // 处理GET请求，完整路径：/api/v1/admin/ai-models/active
    public Result<AiModelConfig> getActive() {
        // 记录查询日志
        log.info("管理员查询当前激活的AI模型配置");
        // 调用服务层查询激活的模型配置
        AiModelConfig config = aiModelConfigService.getActiveConfig();
        // 返回激活的模型配置
        return Result.success(config);
    }
    
    /**
     * 🏭 获取支持的厂商列表 - 获取系统支持的所有AI模型厂商信息
     * 
     * 业务流程：
     * 1. 验证当前用户是否为管理员
     * 2. 获取系统预定义的支持厂商列表
     * 3. 组装厂商信息：名称、标识、支持的模型列表
     * 4. 返回厂商列表
     * 
     * 支持的厂商包括：
     * - OpenAI：GPT-3.5, GPT-4, GPT-4-Turbo等
     * - 百度：文心一言系列
     * - 阿里：通义千问系列
     * - 腾讯：混元系列
     * - 科大讯飞：星火系列
     * 
     * 请求示例：
     * GET /api/v1/admin/ai-models/providers
     * Headers:
     *   Authorization: Bearer admin_token_here
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": [
     *         {
     *             "id": "openai",
     *             "name": "OpenAI",
     *             "description": "OpenAI官方提供的大语言模型",
     *             "models": [
     *                 {"id": "gpt-3.5-turbo", "name": "GPT-3.5 Turbo"},
     *                 {"id": "gpt-4", "name": "GPT-4"},
     *                 {"id": "gpt-4-turbo", "name": "GPT-4 Turbo"}
     *             ]
     *         },
     *         {
     *             "id": "baidu",
     *             "name": "百度",
     *             "description": "百度文心一言大语言模型",
     *             "models": [
     *                 {"id": "ernie-bot", "name": "文心一言"},
     *                 {"id": "ernie-bot-4", "name": "文心一言4.0"},
     *                 {"id": "ernie-bot-turbo", "name": "文心一言Turbo"}
     *             ]
     *         }
     *     ]
     * }
     * 
     * @return Result<List<Map<String, Object>>> 统一响应对象，包含支持的厂商列表
     */
    @GetMapping("/providers")  // 处理GET请求，完整路径：/api/v1/admin/ai-models/providers
    public Result<List<Map<String, Object>>> getSupportedProviders() {
        // 记录查询日志
        log.info("管理员查询支持的AI厂商列表");
        // 调用服务层查询支持的厂商列表
        List<Map<String, Object>> providers = aiModelConfigService.getSupportedProviders();
        // 返回支持的厂商列表
        return Result.success(providers);
    }
    
    /**
     * ➕ 创建或更新模型配置 - 管理员添加新的AI模型或修改现有模型配置
     * 
     * 业务流程：
     * 1. 验证当前用户是否为管理员
     * 2. 验证请求参数：名称、厂商、API地址等必填字段
     * 3. 如果是更新操作，验证模型是否存在
     * 4. 如果是创建操作，验证名称是否重复
     * 5. 加密存储API密钥等敏感信息
     * 6. 保存或更新模型配置
     * 7. 记录操作日志
     * 8. 返回保存后的模型配置
     * 
     * 创建 vs 更新：
     * - 创建：请求体中不包含id字段，系统自动生成新ID
     * - 更新：请求体中包含id字段，更新现有记录
     * 
     * 请求示例（创建）：
     * POST /api/v1/admin/ai-models
     * Headers:
     *   Authorization: Bearer admin_token_here
     *   Content-Type: application/json
     * Body:
     * {
     *     "name": "Claude-3",
     *     "provider": "Anthropic",
     *     "model": "claude-3-opus-20240229",
     *     "apiUrl": "https://api.anthropic.com/v1/messages",
     *     "apiKey": "sk-ant-api03-xxxx",
     *     "maxTokens": 4096,
     *     "temperature": 0.7,
     *     "isActive": false
     * }
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "保存成功",
     *     "data": {
     *         "id": 3,
     *         "name": "Claude-3",
     *         "provider": "Anthropic",
     *         "model": "claude-3-opus-20240229",
     *         "apiUrl": "https://api.anthropic.com/v1/messages",
     *         "apiKey": "sk-ant-api03-****",
     *         "maxTokens": 4096,
     *         "temperature": 0.7,
     *         "isActive": false,
     *         "createdAt": "2024-01-15T14:30:00",
     *         "updatedAt": "2024-01-15T14:30:00"
     *     }
     * }
     * 
     * @param config AI模型配置对象
     *               @RequestBody - 从请求体中反序列化JSON数据
     *               包含模型名称、厂商、API地址、密钥等配置信息
     * @return Result<AiModelConfig> 统一响应对象，包含保存后的模型配置
//     * @throws ValidationException 当参数校验失败时抛出
//     * @throws BusinessException 当模型名称已存在时抛出
     */
    @PostMapping  // 处理POST请求，完整路径：/api/v1/admin/ai-models
    public Result<AiModelConfig> saveOrUpdate(@RequestBody AiModelConfig config) {
        // 记录操作日志，包含模型名称和厂商
        log.info("管理员{}AI模型配置: name={}, provider={}", 
                config.getId() == null ? "创建" : "更新", config.getModelName(), config.getProvider());
        // 调用服务层保存或更新模型配置
        AiModelConfig saved = aiModelConfigService.saveOrUpdate(config);
        // 返回保存后的模型配置
        return Result.success("保存成功", saved);
    }
    
    /**
     * 🔄 设置为激活模型 - 切换系统当前使用的AI模型
     * 
     * 业务流程：
     * 1. 验证当前用户是否为管理员
     * 2. 根据ID查询模型是否存在
     * 3. 测试新模型的连接是否正常
     * 4. 如果测试通过，将当前激活模型设置为非激活状态
     * 5. 将指定模型设置为激活状态
     * 6. 清除相关缓存
     * 7. 记录切换日志
     * 8. 返回操作结果
     * 
     * 切换规则：
     * - 系统同时只能有一个激活的模型
     * - 切换前会测试新模型的连接
     * - 如果测试失败，不允许切换
     * - 切换后，所有新的AI请求将使用新模型
     * 
     * 请求示例：
     * PUT /api/v1/admin/ai-models/2/active
     * Headers:
     *   Authorization: Bearer admin_token_here
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "切换模型成功",
     *     "data": "文心一言已设置为当前激活模型"
     * }
     * 
     * 错误响应示例（测试失败）：
     * {
     *     "code": 400,
     *     "message": "模型连接测试失败，无法切换",
     *     "data": null
     * }
     * 
     * @param id 模型ID，从URL路径获取
     *           @PathVariable - 从URL路径中获取参数
     *           示例：/ai-models/2/active 中的 2
     * @return Result<String> 统一响应对象，包含操作结果信息
//     * @throws ResourceNotFoundException 当模型不存在时抛出
//     * @throws BusinessException 当模型连接测试失败时抛出
     */
    @PutMapping("/{id}/active")  // 处理PUT请求，完整路径：/api/v1/admin/ai-models/{id}/active
    public Result<String> setActive(@PathVariable Long id) {
        // 记录切换操作日志，包含模型ID
        log.info("管理员切换激活AI模型: id={}", id);
        // 调用服务层设置激活模型
        aiModelConfigService.setActive(id);
        // 返回成功响应
        return Result.success("切换模型成功");
    }
    
    /**
     * 🗑️ 删除模型配置 - 管理员移除不再使用的AI模型配置
     * 
     * 业务流程：
     * 1. 验证当前用户是否为管理员
     * 2. 根据ID查询模型是否存在
     * 3. 检查模型是否为当前激活模型
     * 4. 如果是激活模型，不允许删除
     * 5. 执行软删除（设置deleted标志，不是物理删除）
     * 6. 清除相关缓存
     * 7. 记录删除日志
     * 8. 返回操作结果
     * 
     * 删除规则：
     * - 使用软删除，不物理删除数据
     * - 当前激活的模型不能删除
     * - 删除后模型配置不再显示在列表中
     * - 保留数据用于审计和历史记录
     * 
     * 请求示例：
     * DELETE /api/v1/admin/ai-models/3
     * Headers:
     *   Authorization: Bearer admin_token_here
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "删除成功",
     *     "data": null
     * }
     * 
     * 错误响应示例（删除激活模型）：
     * {
     *     "code": 400,
     *     "message": "不能删除当前激活的模型",
     *     "data": null
     * }
     * 
     * @param id 模型ID，从URL路径获取
     *           @PathVariable - 从URL路径中获取参数
     *           示例：/ai-models/3 中的 3
     * @return Result<String> 统一响应对象，包含操作结果信息
//     * @throws ResourceNotFoundException 当模型不存在时抛出
//     * @throws BusinessException 当尝试删除激活模型时抛出
     */
    @DeleteMapping("/{id}")  // 处理DELETE请求，完整路径：/api/v1/admin/ai-models/{id}
    public Result<String> delete(@PathVariable Long id) {
        // 记录删除操作日志，包含模型ID
        log.info("管理员删除AI模型配置: id={}", id);
        // 调用服务层删除模型配置
        aiModelConfigService.delete(id);
        // 返回成功响应
        return Result.success("删除成功");
    }
    
    /**
     * 🔌 测试模型连接 - 验证AI模型配置是否正确，API是否可用
     * 
     * 业务流程：
     * 1. 验证当前用户是否为管理员
     * 2. 根据ID查询模型配置
     * 3. 使用模型配置发送测试请求到AI厂商API
     * 4. 解析API响应，验证连接是否成功
     * 5. 记录测试结果：响应时间、成功率、错误信息等
     * 6. 返回测试结果
     * 
     * 测试内容：
     * - API地址是否可达
     * - API密钥是否有效
     * - 模型名称是否正确
     * - 请求参数格式是否正确
     * - 响应时间是否符合预期
     * 
     * 注意事项：
     * - 测试连接会实际调用AI厂商API，可能产生少量费用
     * - 测试请求使用简单的"Hello"消息，避免消耗过多token
     * - 测试结果会记录到数据库，用于统计分析
     * 
     * 请求示例：
     * POST /api/v1/admin/ai-models/2/test
     * Headers:
     *   Authorization: Bearer admin_token_here
     * 
     * 成功响应示例：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": {
     *         "success": true,
     *         "responseTime": 1250,
     *         "message": "连接测试成功",
     *         "testTime": "2024-01-15T15:30:00",
     *         "response": "Hello! I'm an AI assistant."
     *     }
     * }
     * 
     * 失败响应示例：
     * {
     *     "code": 200,
     *     "message": "success",
     *     "data": {
     *         "success": false,
     *         "responseTime": null,
     *         "message": "API密钥无效",
     *         "testTime": "2024-01-15T15:30:00",
     *         "error": "Invalid API key"
     *     }
     * }
     * 
     * @param id 模型ID，从URL路径获取
     *           @PathVariable - 从URL路径中获取参数
     *           示例：/ai-models/2/test 中的 2
     * @return Result<Map<String, Object>> 统一响应对象，包含测试结果
//     * @throws ResourceNotFoundException 当模型不存在时抛出
     */
    @PostMapping("/{id}/test")  // 处理POST请求，完整路径：/api/v1/admin/ai-models/{id}/test
    public Result<Map<String, Object>> testConnection(@PathVariable Long id) {
        // 记录测试操作日志，包含模型ID
        log.info("管理员测试AI模型连接: id={}", id);
        // 调用服务层测试模型连接
        Map<String, Object> result = aiModelConfigService.testConnection(id);
        // 返回测试结果
        return Result.success(result);
    }
}
