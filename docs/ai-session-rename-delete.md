# AI 助手会话重命名与删除功能改造说明

更新时间：2025-11-13

## 目标
- 在 AI 助手侧边栏会话项上，支持右键菜单：重命名、删除。
- 前端交互与样式统一，后端提供安全、幂等的接口。
- 数据层支持为会话设置用户自定义标题。

---

## 前端改造（qa-system-frontend）

文件：`src/views/common/AiAssistant.vue`

1) 会话项右键菜单
- 在会话项 `div.session-item` 上增加 `@contextmenu.prevent="showContextMenu($event, session)"`。
- 新增右键菜单浮层（纯前端渲染，点击页面空白处自动关闭）：
  - 菜单项：重命名、删除。
  - 采用 Element Plus 图标与卡片式样式，删除态 hover 使用红色警告配色。

2) 状态与方法
- 新增状态：`contextMenuVisible`、`contextMenuX`、`contextMenuY`、`selectedSession`。
- 新增方法：
  - `showContextMenu(event, session)`：记录坐标与选中会话，展示菜单。
  - `handleRename()`：弹出 `ElMessageBox.prompt`，调用重命名 API 成功后更新本地 `sessions`。
  - `handleDelete()`：弹出确认框，调用删除 API，成功后从 `sessions` 移除；若为当前会话则清空消息区。

3) API 增加（`src/api/ai.js`）
- `deleteSession(sessionId)` → `DELETE /ai/sessions/{sessionId}`
- `renameSession(sessionId, title)` → `PUT /ai/sessions/{sessionId}/rename`，请求体 `{ title }`

4) UI/UX 细节
- 采用简洁的玻璃态风格和阴影，融入现有风格体系。
- 菜单浮层 z-index 提升，避免被滚动容器遮挡。
- 会话列表显示优先取 `session.title`，无标题时回退至首条用户消息摘要。

5) 代理与前缀
- 前端所有 AI 接口以 `/api/ai` 为前缀（由 axios `request.js` baseURL + vite 代理统一到后端）。

---

## 后端改造（qa-system-backend）

1) 路由与控制器（`AiAssistantController.java`）
- 控制器基路径：`/api/ai`
- 新增：
  - `DELETE /api/ai/sessions/{sessionId}` → 删除会话及其所有对话记录。
  - `PUT /api/ai/sessions/{sessionId}/rename` → 重命名会话标题。
- 新增 DTO：`RenameSessionRequest`（字段 `title`，必填）。

2) 服务层（`AiAssistantService.java`）
- 新增方法：
  - `deleteSession(Long userId, String sessionId)`：
    - 仅允许删除本人会话；
    - 删除表中该 `userId+sessionId` 的全部记录；
    - 清理 Redis 缓存 `ai:conversation:{userId}:{sessionId}`。
  - `renameSession(Long userId, String sessionId, String title)`：
    - 校验非空；
    - 校验归属；
    - 为该会话的记录设置 `sessionTitle`；
    - 清理对应缓存。

3) 实体与数据库
- 实体 `AiConversation` 新增字段：`sessionTitle`（String）。
- 数据库迁移脚本：`db/migrations/add_session_title_to_ai_conversation.sql`
  - `ALTER TABLE ai_conversation ADD COLUMN session_title VARCHAR(255) ... AFTER session_id;`
  - 补充索引：`(session_id)`、`(user_id, session_id)`（若已存在，忽略建索引错误）。

4) 安全
- 所有接口依赖认证上下文获取 `userId`，仅允许操作本人数据。

---

## 联调与排错

1) 404 问题（PUT /rename）
- 原因：后端新增接口未热加载，或前后端路径不一致。
- 处理：后端 `mvn clean compile` 后需要重启 Spring Boot 服务；前端请求路径必须以 `/api/ai` 开头（vite 代理到后端）。

2) 数据库迁移
- 若执行 `CREATE INDEX idx_session_id` 报 `Duplicate key name`，说明索引已存在，可忽略。

3) 缓存一致性
- 删除/重命名会话后均清理 Redis 缓存键 `ai:conversation:{userId}:{sessionId}`，避免读取旧数据。

---

## 验收清单
- [ ] 右键会话项能弹出菜单。
- [ ] 重命名成功后列表立即显示新标题，刷新后仍生效。
- [ ] 删除成功后从列表移除；若为当前会话，消息区被清空。
- [ ] 收藏、反馈、历史加载等原功能不受影响。

---

## 操作步骤（建议）
1. 执行 SQL 迁移（已完成 `ALTER TABLE ... ADD COLUMN session_title ...`）。
2. 重新编译并重启后端服务（使新增 Controller 生效）。
3. 前端热更新即可；如遇接口 404，请确认后端已重启且前端请求路径为 `/api/ai/...`。

---

## 优化改进（已实施）

### 1. 性能优化
- ✅ **批量更新优化**：Service 层重命名改为使用 `LambdaUpdateWrapper` 单条 UPDATE 语句批量更新，替代循环 `updateById`
- ✅ **SQL 查询优化**：`getUserSessions` 返回完整会话信息（包含 `session_title` 和 `user_message`），解决刷新后标题丢失问题

### 2. 用户体验优化
- ✅ **详细审计日志**：为删除和重命名操作添加详细日志，记录旧标题、新标题、影响记录数等
- ✅ **友好错误提示**：增强错误处理，提供更具体的错误信息和操作反馈
- ✅ **Loading 状态**：使用 `ElLoading.service` 提供操作中的加载动画
- ✅ **输入验证增强**：标题长度限制（1-100字符），实时验证反馈
- ✅ **操作确认优化**：删除确认框显示会话标题，警告当前会话删除

### 3. 无障碍和键盘支持
- ✅ **ARIA 属性**：右键菜单添加 `role="menu"` 和 `aria-label`
- ✅ **键盘导航**：支持 Enter/Space 键触发菜单项，ESC 键关闭菜单
- ✅ **焦点管理**：菜单项支持 `tabindex` 焦点切换

### 4. 代码质量
- ✅ **异常处理**：完善 try-catch-finally，确保 loading 状态正确关闭
- ✅ **日志级别**：使用不同级别（info/warn/error）记录不同类型的操作
- ✅ **代码复用**：提取公共逻辑，减少重复代码

---

## 问题修复记录

### 修复1：ElMessage.loading 不是函数
**问题**：`ElMessage.loading` 方法不存在  
**解决**：改用 `ElLoading.service()` 创建全屏加载实例

### 修复2：刷新后重命名的标题丢失
**问题**：`getUserSessions` SQL 只查询 `session_id` 和 `created_at`  
**解决**：修改 SQL，返回完整的会话信息包括 `session_title`

### 修复3：编译错误 - LambdaQueryWrapper 不支持 set
**问题**：`LambdaQueryWrapper` 没有 `set` 方法  
**解决**：改用 `LambdaUpdateWrapper` 进行批量更新

---

## 数据库增强（可选）
已准备额外的数据库迁移脚本 `enhance_session_management.sql`：
- `is_pinned` - 置顶标记
- `is_archived` - 归档标记
- `session_color` - 颜色标记
- 相关复合索引

---

## 后续优化建议
- 为会话提供置顶、归档功能的 UI 实现
- 添加会话搜索和筛选功能
- 支持会话导出和分享
- 实现会话模板功能
