<template>
  <div class="chat-room">
    <!-- 左侧面板：会话列表 -->
    <div class="left-panel">
      <!-- 搜索和功能区 -->
      <div class="panel-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
        <div class="header-actions">
        
          <el-tooltip :content="wsConnected ? '已连接' : '未连接，点击重连'" placement="bottom">
            <span 
              :class="['ws-status', { connected: wsConnected }]" 
              @click="!wsConnected && connectWebSocket()"
            >●</span>
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleAction">
            <el-button circle class="action-btn">
              <el-icon><Plus /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="addFriend">
                  <el-icon><UserFilled /></el-icon> 添加好友
                </el-dropdown-item>
                <el-dropdown-item command="createGroup">
                  <el-icon><ChatDotSquare /></el-icon> 创建群聊
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- Tab 切换 -->
      <div class="tab-bar">
        <div 
          :class="['tab-item', { active: activeTab === 'chat' }]"
          @click="activeTab = 'chat'"
        >
          <el-badge :value="totalUnread" :hidden="totalUnread === 0" :max="99">
            <el-icon><ChatLineSquare /></el-icon>
          </el-badge>
          <span>消息</span>
        </div>
        <div 
          :class="['tab-item', { active: activeTab === 'contacts' }]"
          @click="activeTab = 'contacts'"
        >
          <el-badge :value="pendingRequests" :hidden="pendingRequests === 0">
            <el-icon><User /></el-icon>
          </el-badge>
          <span>通讯录</span>
        </div>
      </div>

      <!-- 会话列表 -->
      <div v-if="activeTab === 'chat'" class="conversation-list">
        <div
          v-for="conv in sortedConversations"
          :key="conv.id || conv.targetId"
          :class="['conversation-item', { active: currentConversation?.id === conv.id, pinned: conv.isTop }]"
          @click="selectConversation(conv)"
          @contextmenu.prevent="showConversationMenu($event, conv)"
        >
          <!-- 置顶标识 -->
          <div v-if="conv.isTop" class="pin-indicator">📌</div>
          <div class="avatar-wrapper">
            <el-badge :value="conv.unreadCount" :hidden="conv.unreadCount === 0" :max="99">
              <el-avatar :size="48" :src="getConversationAvatar(conv)">
                {{ getConversationName(conv)?.[0] }}
              </el-avatar>
            </el-badge>
            <!-- 私聊在线状态指示器 -->
            <span 
              v-if="conv.type === 'PRIVATE'" 
              :class="['online-dot', { online: isConversationOnline(conv) }]"
              :title="isConversationOnline(conv) ? '在线' : '离线'"
            ></span>
          </div>
          <div class="conv-info">
            <div class="conv-header">
              <span class="conv-name">{{ getConversationName(conv) }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="conv-preview">
              <span v-if="conv.type === 'GROUP'" class="group-tag">[群]</span>
              {{ conv.lastMessage || '暂无消息' }}
            </div>
          </div>
        </div>
        <el-empty v-if="sortedConversations.length === 0" description="暂无会话" />
      </div>

      <!-- 通讯录 -->
      <div v-else class="contacts-panel">
        <!-- 好友申请入口 -->
        <div class="contact-section" @click="showFriendRequests = true">
          <el-icon class="section-icon"><Bell /></el-icon>
          <span>新朋友</span>
          <el-badge :value="pendingRequests" :hidden="pendingRequests === 0" />
        </div>
        
        <!-- 群聊入口 -->
        <div class="contact-section" @click="showGroupList = true">
          <el-icon class="section-icon"><ChatDotSquare /></el-icon>
          <span>群聊</span>
        </div>

        <el-divider>好友列表 ({{ friendList.length }})</el-divider>

        <!-- 好友列表为空 -->
        <el-empty v-if="friendList.length === 0" description="暂无好友" :image-size="60">
          <el-button type="primary" size="small" @click="showAddFriend = true">添加好友</el-button>
        </el-empty>

        <!-- 好友列表 -->
        <div
          v-for="friend in friendList"
          :key="friend.userId"
          class="friend-item"
          @click="startChat(friend)"
          @contextmenu.prevent="showFriendContextMenu($event, friend)"
        >
          <div class="friend-avatar-wrapper">
            <el-avatar :size="40" :src="friend.avatar">
              {{ friend.realName?.[0] }}
            </el-avatar>
            <span :class="['online-dot', { online: friend.online }]"></span>
          </div>
          <div class="friend-info">
            <span class="friend-name">{{ friend.remark || friend.realName }}</span>
            <span :class="['online-status', { online: friend.online }]">
              {{ friend.online ? '在线' : '离线' }}
            </span>
          </div>
          <el-dropdown trigger="click" @command="(cmd) => handleFriendAction(cmd, friend)">
            <el-button link class="friend-more-btn" @click.stop>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="chat">发消息</el-dropdown-item>
                <el-dropdown-item command="detail">查看资料</el-dropdown-item>
                <el-dropdown-item command="remark">设置备注</el-dropdown-item>
                <el-dropdown-item command="group">移动分组</el-dropdown-item>
                <el-dropdown-item divided command="delete">删除好友</el-dropdown-item>
                <el-dropdown-item command="block">加入黑名单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <el-empty v-if="friendList.length === 0" description="暂无好友" />
      </div>
    </div>

    <!-- 右侧面板：聊天窗口 -->
    <div class="right-panel">
      <template v-if="currentConversation">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <span class="chat-title">{{ getConversationName(currentConversation) }}</span>
          <div class="chat-actions">
            <el-button v-if="currentConversation.type === 'GROUP'" link @click="showGroupInfo = true">
              <el-icon><More /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div ref="messageListRef" class="message-list" @scroll="handleScroll">
          <div v-if="loadingMessages" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon> 加载中...
          </div>
          
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', { 'is-self': msg.senderId === currentUserId }]"
          >
            <el-avatar :size="36" :src="msg.sender?.avatar">
              {{ msg.sender?.realName?.[0] }}
            </el-avatar>
            <div class="message-content">
              <div class="message-sender" v-if="currentConversation.type === 'GROUP'">
                <span class="sender-name">{{ msg.sender?.realName }}</span>
                <el-tag v-if="getMemberRole(msg.senderId) === 'OWNER'" type="warning" size="small" class="role-badge">群主</el-tag>
                <el-tag v-else-if="getMemberRole(msg.senderId) === 'ADMIN'" type="primary" size="small" class="role-badge">管理</el-tag>
              </div>
              <div class="message-bubble" @contextmenu.prevent="showMessageMenu($event, msg)">
                <!-- 文本消息 -->
                <template v-if="msg.type === 'TEXT'">
                  <span v-html="renderEmoji(msg.content)"></span>
                </template>
                <!-- 图片消息 -->
                <template v-else-if="msg.type === 'IMAGE'">
                  <el-image 
                    :src="getMediaUrl(msg.mediaUrl)" 
                    :preview-src-list="[getMediaUrl(msg.mediaUrl)]"
                    fit="cover"
                    class="message-image"
                    :loading="'lazy'"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>加载失败</span>
                      </div>
                    </template>
                  </el-image>
                </template>
                <!-- 视频消息 -->
                <template v-else-if="msg.type === 'VIDEO'">
                  <video 
                    :src="getMediaUrl(msg.mediaUrl)" 
                    controls 
                    :poster="msg.mediaThumbnail ? getMediaUrl(msg.mediaThumbnail) : ''"
                    class="message-video"
                    preload="metadata"
                  >
                    您的浏览器不支持视频播放
                  </video>
                </template>
                <!-- 表情消息 -->
                <template v-else-if="msg.type === 'EMOJI'">
                  <img :src="getMediaUrl(msg.mediaUrl)" class="message-emoji" @error="handleEmojiError" />
                </template>
                <!-- 撤回消息 -->
                <template v-else-if="msg.isRecalled">
                  <span class="recalled-message">消息已撤回</span>
                </template>
                <!-- 系统消息 -->
                <template v-else-if="msg.type === 'SYSTEM'">
                  <span class="system-message">{{ msg.content }}</span>
                </template>
              </div>
              <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-toolbar">
            <el-popover placement="top" trigger="click" :width="400">
              <template #reference>
                <el-button link class="toolbar-btn">
                  <el-icon><Grape /></el-icon>
                </el-button>
              </template>
              <EmojiPicker @select="insertEmoji" />
            </el-popover>
            
            <el-upload
              :show-file-list="false"
              :before-upload="handleImageUpload"
              accept="image/*"
            >
              <el-button link class="toolbar-btn">
                <el-icon><Picture /></el-icon>
              </el-button>
            </el-upload>
            
            <el-upload
              :show-file-list="false"
              :before-upload="handleVideoUpload"
              accept="video/*"
            >
              <el-button link class="toolbar-btn">
                <el-icon><VideoCamera /></el-icon>
              </el-button>
            </el-upload>
          </div>
          
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.ctrl="inputMessage += '\n'"
          />
          
          <div class="input-actions">
            <span class="input-hint">Enter 发送，Ctrl+Enter 换行</span>
            <el-button type="primary" :disabled="!inputMessage.trim()" @click="sendMessage">
              发送
            </el-button>
          </div>
        </div>
      </template>

      <!-- 未选择会话 -->
      <div v-else class="no-conversation">
        <el-empty description="选择一个会话开始聊天">
          <el-button type="primary" @click="handleAction('addFriend')">添加好友</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 添加好友对话框 -->
    <el-dialog v-model="showAddFriend" title="添加好友" width="500px">
      <el-input
        v-model="searchUserKeyword"
        placeholder="搜索用户名或姓名"
        @input="debouncedSearchUsers"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="search-results">
        <div
          v-for="user in searchResults"
          :key="user.userId"
          class="search-result-item"
        >
          <el-avatar :size="40" :src="user.avatar">{{ user.realName?.[0] }}</el-avatar>
          <div class="user-info">
            <span class="user-name">{{ user.realName }}</span>
            <span class="user-role">{{ user.role === 'TEACHER' ? '教师' : '学生' }}</span>
          </div>
          <!-- 已是好友 -->
          <el-tag v-if="user.isFriend" type="success" size="small">已是好友</el-tag>
          <!-- 我发送的申请等待中 -->
          <el-tag v-else-if="user.hasPendingRequest" type="warning" size="small">等待验证</el-tag>
          <!-- 对方发给我的申请 -->
          <el-button 
            v-else-if="user.hasReceivedRequest"
            type="success" 
            size="small"
            @click="acceptFromSearch(user)"
          >
            同意申请
          </el-button>
          <!-- 可以添加 -->
          <el-button 
            v-else
            type="primary" 
            size="small"
            @click="sendRequest(user)"
          >
            添加
          </el-button>
        </div>
        <el-empty v-if="searchResults.length === 0 && searchUserKeyword" description="未找到用户" />
      </div>
    </el-dialog>

    <!-- 好友申请列表 -->
    <el-dialog v-model="showFriendRequests" title="好友申请" width="500px">
      <div class="friend-requests">
        <div
          v-for="req in friendRequests"
          :key="req.id"
          class="request-item"
        >
          <el-avatar :size="40" :src="req.fromUser?.avatar">
            {{ req.fromUser?.realName?.[0] }}
          </el-avatar>
          <div class="request-info">
            <span class="request-name">{{ req.fromUser?.realName }}</span>
            <span class="request-message">{{ req.message || '请求添加你为好友' }}</span>
          </div>
          <div v-if="req.status === 'PENDING'" class="request-actions">
            <el-button type="primary" size="small" @click="acceptRequest(req.id)">同意</el-button>
            <el-button size="small" @click="rejectRequest(req.id)">拒绝</el-button>
          </div>
          <el-tag v-else :type="req.status === 'ACCEPTED' ? 'success' : 'info'" size="small">
            {{ req.status === 'ACCEPTED' ? '已同意' : '已拒绝' }}
          </el-tag>
        </div>
        <el-empty v-if="friendRequests.length === 0" description="暂无好友申请" />
      </div>
    </el-dialog>

    <!-- 创建群聊对话框 -->
    <el-dialog v-model="showCreateGroup" title="创建群聊" width="500px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="群名称" required>
          <el-input v-model="groupForm.name" placeholder="请输入群名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="群头像">
          <el-input v-model="groupForm.avatar" placeholder="可选，输入头像URL" />
        </el-form-item>
        <el-form-item label="选择成员">
          <div v-if="friendList.length === 0" class="no-friends-tip">
            暂无好友，请先添加好友
          </div>
          <div v-else class="member-select-list">
            <div 
              v-for="friend in friendList" 
              :key="friend.userId"
              :class="['member-item', { selected: groupForm.memberIds.includes(friend.userId) }]"
              @click="toggleMemberSelect(friend.userId)"
            >
              <el-checkbox 
                :model-value="groupForm.memberIds.includes(friend.userId)"
                @click.stop
              />
              <el-avatar :size="32" :src="friend.avatar">{{ friend.realName?.[0] }}</el-avatar>
              <span class="member-name">{{ friend.remark || friend.realName }}</span>
              <el-tag v-if="friend.online" type="success" size="small">在线</el-tag>
            </div>
          </div>
          <div v-if="groupForm.memberIds.length > 0" class="selected-count">
            已选择 {{ groupForm.memberIds.length }} 人
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateGroup = false">取消</el-button>
        <el-button type="primary" @click="createGroupChat" :disabled="!groupForm.name.trim() || groupForm.memberIds.length === 0">
          创建群聊 ({{ groupForm.memberIds.length + 1 }}人)
        </el-button>
      </template>
    </el-dialog>

    <!-- 好友详情对话框 -->
    <el-dialog v-model="showFriendDetail" title="好友资料" width="400px" class="friend-detail-dialog">
      <div v-if="currentFriend" class="friend-profile">
        <div class="profile-header">
          <el-avatar :size="80" :src="currentFriend.avatar">
            {{ currentFriend.realName?.[0] }}
          </el-avatar>
          <div class="profile-info">
            <h3>{{ currentFriend.realName }}</h3>
            <p class="username">@{{ currentFriend.username }}</p>
            <el-tag :type="currentFriend.online ? 'success' : 'info'" size="small">
              {{ currentFriend.online ? '在线' : '离线' }}
            </el-tag>
          </div>
        </div>
        <el-descriptions :column="1" border class="profile-details">
          <el-descriptions-item label="备注">
            {{ currentFriend.remark || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="分组">
            {{ currentFriend.groupName || '我的好友' }}
          </el-descriptions-item>
          <el-descriptions-item label="身份">
            <el-tag size="small">{{ currentFriend.role === 'TEACHER' ? '教师' : '学生' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ currentFriend.gender === 'M' ? '男' : currentFriend.gender === 'F' ? '女' : '保密' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ currentFriend.email || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="添加时间">
            {{ formatDate(currentFriend.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showFriendDetail = false">关闭</el-button>
        <el-button type="primary" @click="startChatWithCurrentFriend">发消息</el-button>
      </template>
    </el-dialog>

    <!-- 设置备注对话框 -->
    <el-dialog v-model="showRemarkDialog" title="设置备注" width="400px">
      <el-form>
        <el-form-item label="备注名">
          <el-input v-model="remarkForm.remark" placeholder="请输入备注名" maxlength="20" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRemarkDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>

    <!-- 移动分组对话框 -->
    <el-dialog v-model="showGroupDialog" title="移动到分组" width="400px">
      <el-form>
        <el-form-item label="选择分组">
          <el-select v-model="groupMoveForm.groupName" placeholder="选择或输入分组" filterable allow-create>
            <el-option label="我的好友" value="我的好友" />
            <el-option label="同学" value="同学" />
            <el-option label="老师" value="老师" />
            <el-option label="同事" value="同事" />
            <el-option label="家人" value="家人" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFriendGroup">确定</el-button>
      </template>
    </el-dialog>

    <!-- 群聊设置对话框 -->
    <el-dialog v-model="showGroupInfo" title="群聊设置" width="520px" class="group-info-dialog">
      <div v-if="currentConversation?.group" class="group-profile">
        <!-- 群头像和基本信息 -->
        <div class="profile-header">
          <div class="avatar-wrapper">
            <el-avatar :size="72" :src="currentConversation.group.avatar">
              {{ currentConversation.group.name?.[0] }}
            </el-avatar>
            <el-button 
              v-if="isGroupOwnerOrAdmin" 
              class="edit-avatar-btn" 
              circle 
              size="small"
              @click="showEditGroupName = true"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
          </div>
          <div class="profile-info">
            <div class="name-row">
              <h3>{{ currentConversation.group.name }}</h3>
              <el-button v-if="isGroupOwnerOrAdmin" link size="small" @click="showEditGroupName = true">
                <el-icon><Edit /></el-icon>
              </el-button>
            </div>
            <p class="group-meta">
              <span class="group-id">群号: {{ currentConversation.group.id }}</span>
              <span class="member-count">
                <el-icon><User /></el-icon>
                {{ groupMembers.length }} 人
              </span>
            </p>
          </div>
        </div>

        <!-- 群公告 -->
        <div class="group-section">
          <div class="section-header">
            <span class="section-title">
              <el-icon><Bell /></el-icon> 群公告
            </span>
            <el-button v-if="isGroupOwnerOrAdmin" link size="small" @click="showEditAnnouncement = true">
              编辑
            </el-button>
          </div>
          <div class="announcement-content">
            {{ currentConversation.group.announcement || '暂无公告' }}
          </div>
        </div>

        <!-- 群成员 -->
        <div class="group-section">
          <div class="section-header">
            <span class="section-title">
              <el-icon><UserFilled /></el-icon> 群成员 ({{ groupMembers.length }})
            </span>
            <el-button v-if="isGroupOwnerOrAdmin" link size="small" @click="showInviteMembers = true">
              <el-icon><Plus /></el-icon> 邀请
            </el-button>
          </div>
          <div class="group-members-grid">
            <div 
              v-for="member in groupMembers" 
              :key="member.userId" 
              class="member-card"
              @click="showMemberOptions(member)"
            >
              <el-avatar :size="40" :src="member.avatar || member.user?.avatar">
                {{ (member.realName || member.user?.realName)?.[0] }}
              </el-avatar>
              <span class="member-name">{{ member.realName || member.user?.realName || '未知' }}</span>
              <el-tag v-if="member.role === 'OWNER'" type="warning" size="small" class="role-tag">群主</el-tag>
              <el-tag v-else-if="member.role === 'ADMIN'" type="primary" size="small" class="role-tag">管理</el-tag>
            </div>
            <div v-if="isGroupOwnerOrAdmin" class="member-card add-member" @click="showInviteMembers = true">
              <div class="add-icon">
                <el-icon><Plus /></el-icon>
              </div>
              <span class="member-name">邀请</span>
            </div>
          </div>
        </div>

        <!-- 操作区 -->
        <div class="group-actions">
          <el-button @click="clearGroupMessages" plain>
            <el-icon><Delete /></el-icon> 清空聊天记录
          </el-button>
          <el-button v-if="isGroupOwner" type="danger" @click="dismissGroup">
            <el-icon><DeleteFilled /></el-icon> 解散群聊
          </el-button>
          <el-button v-else type="danger" plain @click="leaveGroup">
            <el-icon><SwitchButton /></el-icon> 退出群聊
          </el-button>
        </div>
      </div>
      <div v-else class="no-group-info">
        <el-icon :size="48"><Warning /></el-icon>
        <p>无法获取群信息</p>
      </div>
    </el-dialog>

    <!-- 编辑群名称对话框 -->
    <el-dialog v-model="showEditGroupName" title="编辑群信息" width="400px" append-to-body>
      <el-form :model="editGroupForm" label-width="80px">
        <el-form-item label="群名称">
          <el-input v-model="editGroupForm.name" maxlength="20" show-word-limit placeholder="请输入群名称" />
        </el-form-item>
        <el-form-item label="群头像">
          <el-input v-model="editGroupForm.avatar" placeholder="输入头像URL（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditGroupName = false">取消</el-button>
        <el-button type="primary" @click="saveGroupInfo">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑群公告对话框 -->
    <el-dialog v-model="showEditAnnouncement" title="编辑群公告" width="450px" append-to-body>
      <el-input 
        v-model="editGroupForm.announcement" 
        type="textarea" 
        :rows="4" 
        maxlength="200" 
        show-word-limit 
        placeholder="请输入群公告内容"
      />
      <template #footer>
        <el-button @click="showEditAnnouncement = false">取消</el-button>
        <el-button type="primary" @click="saveGroupAnnouncement">发布公告</el-button>
      </template>
    </el-dialog>

    <!-- 邀请成员对话框 -->
    <el-dialog 
      v-model="showInviteMembers" 
      title="邀请好友加入群聊" 
      width="450px" 
      append-to-body
      @open="onInviteDialogOpen"
    >
      <!-- 搜索框 -->
      <el-input 
        v-model="inviteSearchKeyword" 
        placeholder="搜索好友" 
        prefix-icon="Search" 
        clearable
        class="invite-search"
      />
      
      <!-- 已选择的好友 -->
      <div v-if="inviteIds.length > 0" class="selected-friends">
        <span class="label">已选择:</span>
        <el-tag 
          v-for="id in inviteIds" 
          :key="id" 
          closable 
          size="small"
          @close="toggleInvite(id)"
        >
          {{ getFriendNameById(id) }}
        </el-tag>
      </div>
      
      <!-- 好友列表 -->
      <div class="invite-member-list">
        <div 
          v-for="friend in filteredFriendsToInvite" 
          :key="friend.userId"
          :class="['invite-item', { selected: inviteIds.includes(friend.userId) }]"
          @click="toggleInvite(friend.userId)"
        >
          <el-checkbox :model-value="inviteIds.includes(friend.userId)" @click.stop />
          <div class="friend-avatar-wrapper">
            <el-avatar :size="40" :src="friend.avatar">{{ friend.realName?.[0] }}</el-avatar>
            <span v-if="friend.online" class="online-indicator"></span>
          </div>
          <div class="friend-info">
            <span class="friend-name">{{ friend.remark || friend.realName }}</span>
            <span class="friend-status">{{ friend.online ? '在线' : '离线' }}</span>
          </div>
        </div>
        <div v-if="filteredFriendsToInvite.length === 0" class="no-friends">
          <el-icon :size="32"><User /></el-icon>
          <p>{{ inviteSearchKeyword ? '未找到匹配的好友' : '没有可邀请的好友' }}</p>
        </div>
      </div>
      <template #footer>
        <div class="invite-footer">
          <span class="select-count">已选 {{ inviteIds.length }} 人</span>
          <div class="footer-buttons">
            <el-button @click="showInviteMembers = false">取消</el-button>
            <el-button type="primary" @click="inviteFriendsToGroup" :disabled="inviteIds.length === 0">
              确认邀请
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 群聊列表对话框 -->
    <el-dialog v-model="showGroupList" title="我的群聊" width="500px">
      <el-tabs v-model="groupListTab">
        <el-tab-pane label="我创建的" name="created">
          <div class="group-list">
            <div 
              v-for="group in myCreatedGroups" 
              :key="group.id" 
              class="group-item"
              @click="openGroupChat(group)"
            >
              <el-avatar :size="48" :src="group.avatar">{{ group.name?.[0] }}</el-avatar>
              <div class="group-info">
                <div class="group-name">{{ group.name }}</div>
                <div class="group-meta">{{ group.memberCount || 0 }} 人 · 群号: {{ group.id }}</div>
              </div>
              <el-tag type="warning" size="small">群主</el-tag>
            </div>
            <el-empty v-if="myCreatedGroups.length === 0" description="还没有创建群聊" :image-size="60" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="我加入的" name="joined">
          <div class="group-list">
            <div 
              v-for="group in myJoinedGroups" 
              :key="group.id" 
              class="group-item"
              @click="openGroupChat(group)"
            >
              <el-avatar :size="48" :src="group.avatar">{{ group.name?.[0] }}</el-avatar>
              <div class="group-info">
                <div class="group-name">{{ group.name }}</div>
                <div class="group-meta">{{ group.memberCount || 0 }} 人 · 群号: {{ group.id }}</div>
              </div>
              <el-tag v-if="group.myRole === 'ADMIN'" type="primary" size="small">管理员</el-tag>
            </div>
            <el-empty v-if="myJoinedGroups.length === 0" description="还没有加入群聊" :image-size="60" />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showGroupList = false">关闭</el-button>
        <el-button type="primary" @click="showCreateGroup = true; showGroupList = false">创建群聊</el-button>
      </template>
    </el-dialog>

    <!-- 成员管理对话框 -->
    <el-dialog v-model="showMemberManage" title="成员管理" width="400px" append-to-body>
      <div v-if="selectedMember" class="member-manage-content">
        <div class="member-profile">
          <el-avatar :size="64" :src="selectedMember.avatar || selectedMember.user?.avatar">
            {{ (selectedMember.realName || selectedMember.user?.realName)?.[0] }}
          </el-avatar>
          <div class="member-info">
            <h4>{{ selectedMember.realName || selectedMember.user?.realName }}</h4>
            <p class="member-role">
              <el-tag v-if="selectedMember.role === 'OWNER'" type="warning" size="small">群主</el-tag>
              <el-tag v-else-if="selectedMember.role === 'ADMIN'" type="primary" size="small">管理员</el-tag>
              <el-tag v-else type="info" size="small">成员</el-tag>
            </p>
          </div>
        </div>
        
        <el-divider />
        
        <div class="member-actions">
          <!-- 群主可以设置/取消管理员 -->
          <template v-if="isGroupOwner && selectedMember.role !== 'OWNER'">
            <el-button 
              v-if="selectedMember.role !== 'ADMIN'" 
              type="primary" 
              plain 
              @click="setMemberAdmin(selectedMember.userId, true)"
            >
              <el-icon><UserFilled /></el-icon> 设为管理员
            </el-button>
            <el-button 
              v-else 
              type="warning" 
              plain 
              @click="setMemberAdmin(selectedMember.userId, false)"
            >
              <el-icon><User /></el-icon> 取消管理员
            </el-button>
          </template>
          
          <!-- 群主可以转让群主 -->
          <el-button 
            v-if="isGroupOwner && selectedMember.role !== 'OWNER'" 
            type="warning" 
            @click="transferOwnership(selectedMember.userId)"
          >
            <el-icon><Switch /></el-icon> 转让群主
          </el-button>
          
          <!-- 群主和管理员可以踢人（管理员不能踢管理员和群主） -->
          <el-button 
            v-if="canKickMember(selectedMember)" 
            type="danger" 
            @click="kickMember(selectedMember.userId)"
          >
            <el-icon><RemoveFilled /></el-icon> 移出群聊
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 消息右键菜单 -->
    <div 
      v-show="messageContextMenu.visible" 
      class="context-menu"
      :style="{ left: messageContextMenu.x + 'px', top: messageContextMenu.y + 'px' }"
    >
      <div class="menu-item" @click="copyMessage">复制</div>
      <div class="menu-item" @click="forwardMessage">转发</div>
      <div v-if="messageContextMenu.isSelf" class="menu-item" @click="recallCurrentMessage">撤回</div>
      <div class="menu-item danger" @click="deleteMessage">删除</div>
    </div>

    <!-- 会话右键菜单 -->
    <div 
      v-show="conversationMenu.visible" 
      class="context-menu"
      :style="{ left: conversationMenu.x + 'px', top: conversationMenu.y + 'px' }"
    >
      <div class="menu-item" @click="togglePinConversation">
        {{ conversationMenu.conv?.isTop ? '取消置顶' : '置顶会话' }}
      </div>
      <div class="menu-item" @click="markConversationRead">标为已读</div>
      <div class="menu-item" @click="hideConversation">隐藏会话</div>
      <div class="menu-item danger" @click="deleteConversation">删除会话</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Plus, UserFilled, ChatDotSquare, ChatLineSquare, User,
  Bell, More, Loading, Grape, Picture, VideoCamera, Edit, Delete,
  DeleteFilled, SwitchButton, Warning, Switch, RemoveFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { debounce } from 'lodash-es'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import * as chatApi from '@/api/chat'
import { uploadFile } from '@/api/upload'
import EmojiPicker from '@/components/chat/EmojiPicker.vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const userStore = useUserStore()

// 获取当前用户ID
const currentUserId = computed(() => {
  const userInfo = userStore.userInfo
  if (userInfo?.id) return userInfo.id
  if (userInfo?.userId) return userInfo.userId
  return null
})

// 状态
const activeTab = ref('chat')
const searchKeyword = ref('')
const conversations = ref([])
const currentConversation = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loadingMessages = ref(false)
const totalUnread = ref(0)
const pendingRequests = ref(0)
const friendList = ref([])
const friendRequests = ref([])
const messageListRef = ref(null)

// 对话框
const showAddFriend = ref(false)
const showFriendRequests = ref(false)
const showCreateGroup = ref(false)
const showGroupList = ref(false)
const showGroupInfo = ref(false)
const showFriendDetail = ref(false)
const showRemarkDialog = ref(false)
const showGroupDialog = ref(false)
const showEditGroupName = ref(false)
const showEditAnnouncement = ref(false)
const showInviteMembers = ref(false)
const showMemberManage = ref(false)

// 当前操作的好友
const currentFriend = ref(null)

// 群成员列表
const groupMembers = ref([])

// 群聊列表相关
const groupListTab = ref('created')
const myGroups = ref([])
const selectedMember = ref(null)

// 邀请成员相关
const inviteIds = ref([])
const inviteSearchKeyword = ref('')

// 编辑群信息表单
const editGroupForm = reactive({
  name: '',
  avatar: '',
  announcement: ''
})

// 计算属性：是否是群主
const isGroupOwner = computed(() => {
  if (!currentConversation.value?.group?.id) return false
  const myMember = groupMembers.value.find(m => m.userId === currentUserId.value)
  return myMember?.role === 'OWNER'
})

// 计算属性：是否是群主或管理员
const isGroupOwnerOrAdmin = computed(() => {
  if (!currentConversation.value?.group?.id) return false
  const myMember = groupMembers.value.find(m => m.userId === currentUserId.value)
  return myMember?.role === 'OWNER' || myMember?.role === 'ADMIN'
})

// 计算属性：可邀请的好友（排除已在群内的）
const availableFriendsToInvite = computed(() => {
  const memberIds = new Set(groupMembers.value.map(m => m.userId))
  return friendList.value.filter(f => !memberIds.has(f.userId))
})

// 计算属性：搜索过滤后的可邀请好友
const filteredFriendsToInvite = computed(() => {
  if (!inviteSearchKeyword.value) return availableFriendsToInvite.value
  const keyword = inviteSearchKeyword.value.toLowerCase()
  return availableFriendsToInvite.value.filter(f => {
    const name = (f.remark || f.realName || '').toLowerCase()
    return name.includes(keyword)
  })
})

// 计算属性：我创建的群聊
const myCreatedGroups = computed(() => {
  return myGroups.value.filter(g => g.myRole === 'OWNER')
})

// 计算属性：我加入的群聊（非群主）
const myJoinedGroups = computed(() => {
  return myGroups.value.filter(g => g.myRole !== 'OWNER')
})

// 获取成员角色
const getMemberRole = (userId) => {
  const member = groupMembers.value.find(m => m.userId === userId)
  return member?.role || 'MEMBER'
}

// 判断是否可以踢出该成员
const canKickMember = (member) => {
  if (!member || member.userId === currentUserId.value) return false
  if (member.role === 'OWNER') return false
  if (isGroupOwner.value) return true
  if (isGroupOwnerOrAdmin.value && member.role !== 'ADMIN') return true
  return false
}

// 备注表单
const remarkForm = reactive({
  friendId: null,
  remark: ''
})

// 分组表单
const groupMoveForm = reactive({
  friendId: null,
  groupName: ''
})

// 消息右键菜单
const messageContextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  message: null,
  isSelf: false
})

// 会话右键菜单
const conversationMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  conv: null
})

// 隐藏的会话ID列表
const hiddenConversations = ref(new Set())

// 搜索
const searchUserKeyword = ref('')
const searchResults = ref([])

// 创建群聊表单
const groupForm = reactive({
  name: '',
  avatar: '',
  memberIds: []
})

// 切换成员选择
const toggleMemberSelect = (userId) => {
  console.log('切换成员选择:', userId, '当前已选:', groupForm.memberIds)
  const index = groupForm.memberIds.indexOf(userId)
  if (index > -1) {
    groupForm.memberIds.splice(index, 1)
  } else {
    groupForm.memberIds.push(userId)
  }
  console.log('更新后已选:', groupForm.memberIds)
}

// WebSocket
let ws = null

// 计算属性
const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  const keyword = searchKeyword.value.toLowerCase()
  return conversations.value.filter(conv => {
    const name = getConversationName(conv)?.toLowerCase() || ''
    return name.includes(keyword)
  })
})

// 获取会话的唯一key（在computed之前定义）
const getConvKey = (conv) => conv.id ? String(conv.id) : `${conv.type}_${conv.targetId}`

// 排序后的会话（置顶优先，隐藏的不显示）
const sortedConversations = computed(() => {
  // 过滤隐藏的会话
  const visible = filteredConversations.value.filter(conv => {
    const key = getConvKey(conv)
    return !hiddenConversations.value.has(key)
  })
  
  // 按置顶和时间排序
  return visible.sort((a, b) => {
    // 置顶优先
    if (a.isTop && !b.isTop) return -1
    if (!a.isTop && b.isTop) return 1
    // 然后按最后消息时间排序
    return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
  })
})

// 方法
const loadConversations = async () => {
  try {
    const res = await chatApi.getConversations()
    const newConversations = res.data || res || []
    
    // 保留本地创建的临时会话（id为null的）
    const tempConversations = conversations.value.filter(c => c.id === null)
    
    // 如果后端返回了数据，合并
    if (Array.isArray(newConversations) && newConversations.length > 0) {
      const serverIds = new Set(newConversations.map(c => `${c.type}_${c.targetId}`))
      const remainingTemp = tempConversations.filter(c => 
        !serverIds.has(`${c.type}_${c.targetId}`)
      )
      conversations.value = [...newConversations, ...remainingTemp]
    } else if (tempConversations.length > 0) {
      // 如果后端没有数据但有临时会话，保留临时会话
      conversations.value = tempConversations
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

const loadFriendList = async () => {
  try {
    const res = await chatApi.getFriendList()
    console.log('好友列表API响应:', res)
    // 兼容不同的响应结构
    friendList.value = res.data || res || []
    console.log('好友列表数据:', friendList.value)
  } catch (error) {
    console.error('加载好友列表失败:', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await chatApi.getUnreadCount()
    totalUnread.value = res.data || 0
  } catch (error) {
    console.error('加载未读数失败:', error)
  }
}

const loadPendingRequestCount = async () => {
  try {
    const res = await chatApi.getPendingRequestCount()
    pendingRequests.value = res.data || 0
  } catch (error) {
    console.error('加载申请数失败:', error)
  }
}

const loadFriendRequests = async () => {
  try {
    const res = await chatApi.getFriendRequests()
    friendRequests.value = res.data || []
  } catch (error) {
    console.error('加载好友申请失败:', error)
  }
}

const selectConversation = async (conv) => {
  currentConversation.value = conv
  await loadMessages()
  
  // 标记已读
  if (conv.unreadCount > 0) {
    await chatApi.markMessagesAsRead(conv.id)
    conv.unreadCount = 0
    loadUnreadCount()
  }
}

const loadMessages = async () => {
  if (!currentConversation.value) return
  
  loadingMessages.value = true
  try {
    const conv = currentConversation.value
    let res
    if (conv.type === 'PRIVATE') {
      res = await chatApi.getPrivateMessages(conv.targetId, 1, 50)
    } else {
      res = await chatApi.getGroupMessages(conv.targetId, 1, 50)
    }
    messages.value = (res.data?.records || []).reverse()
    nextTick(() => scrollToBottom())
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loadingMessages.value = false
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentConversation.value) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  try {
    const conv = currentConversation.value
    
    // 立即更新本地会话信息
    conv.lastMessage = content
    conv.lastMessageTime = new Date().toISOString()
    
    if (conv.type === 'PRIVATE') {
      const res = await chatApi.sendPrivateMessage({
        receiverId: conv.targetId,
        content,
        type: 'TEXT'
      })
      // 立即添加消息到列表
      if (res.data) {
        messages.value.push(res.data)
        nextTick(() => scrollToBottom())
      }
    } else {
      const res = await chatApi.sendGroupMessage({
        groupId: conv.targetId,
        content,
        type: 'TEXT'
      })
      if (res.data) {
        messages.value.push(res.data)
        nextTick(() => scrollToBottom())
      }
    }
    
    // 后台刷新会话列表
    loadConversations()
  } catch (error) {
    ElMessage.error('发送失败')
    console.error('发送消息失败:', error)
    // 恢复输入框内容
    inputMessage.value = content
  }
}

const handleImageUpload = async (file) => {
  if (!currentConversation.value) {
    ElMessage.warning('请先选择一个会话')
    return false
  }
  
  try {
    const res = await uploadFile(file, 'chat')
    // 兼容不同的响应结构
    const mediaUrl = res.data?.url || res.url
    
    if (!mediaUrl) {
      ElMessage.error('上传失败：未获取到文件地址')
      return false
    }
    
    const conv = currentConversation.value
    let msgRes
    if (conv.type === 'PRIVATE') {
      msgRes = await chatApi.sendPrivateMessage({
        receiverId: conv.targetId,
        type: 'IMAGE',
        mediaUrl,
        content: '[图片]'
      })
    } else {
      msgRes = await chatApi.sendGroupMessage({
        groupId: conv.targetId,
        type: 'IMAGE',
        mediaUrl,
        content: '[图片]'
      })
    }
    
    // 添加消息到列表
    if (msgRes.data) {
      messages.value.push(msgRes.data)
      nextTick(() => scrollToBottom())
    }
    
    loadConversations()
  } catch (error) {
    console.error('图片发送失败:', error)
    ElMessage.error(error.response?.data?.message || '图片发送失败')
  }
  return false
}

const handleVideoUpload = async (file) => {
  if (!currentConversation.value) {
    ElMessage.warning('请先选择一个会话')
    return false
  }
  
  // 检查文件大小 (50MB)
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('视频文件不能超过50MB')
    return false
  }
  
  try {
    ElMessage.info('正在上传视频...')
    const res = await uploadFile(file, 'video')
    const mediaUrl = res.data?.url || res.url
    
    if (!mediaUrl) {
      ElMessage.error('上传失败：未获取到文件地址')
      return false
    }
    
    const conv = currentConversation.value
    let msgRes
    if (conv.type === 'PRIVATE') {
      msgRes = await chatApi.sendPrivateMessage({
        receiverId: conv.targetId,
        type: 'VIDEO',
        mediaUrl,
        content: '[视频]'
      })
    } else {
      msgRes = await chatApi.sendGroupMessage({
        groupId: conv.targetId,
        type: 'VIDEO',
        mediaUrl,
        content: '[视频]'
      })
    }
    
    if (msgRes.data) {
      messages.value.push(msgRes.data)
      nextTick(() => scrollToBottom())
    }
    
    loadConversations()
  } catch (error) {
    console.error('视频发送失败:', error)
    ElMessage.error(error.response?.data?.message || '视频发送失败')
  }
  return false
}

const insertEmoji = (emoji) => {
  // 优先使用emoji字符，而不是代码
  inputMessage.value += emoji.unicode || emoji.text || emoji.code || `[${emoji.name}]`
}

const startChat = (friend) => {
  // 查找或创建会话
  let conv = conversations.value.find(
    c => c.type === 'PRIVATE' && c.targetId === friend.userId
  )
  
  if (!conv) {
    conv = {
      id: null,
      type: 'PRIVATE',
      targetId: friend.userId,
      targetUser: {
        id: friend.userId,
        realName: friend.realName,
        avatar: friend.avatar
      },
      lastMessage: '',
      unreadCount: 0
    }
    conversations.value.unshift(conv)
  }
  
  // 如果会话被隐藏，恢复显示
  const key = getConvKey(conv)
  if (hiddenConversations.value.has(key)) {
    const newSet = new Set(hiddenConversations.value)
    newSet.delete(key)
    hiddenConversations.value = newSet
    localStorage.setItem('hiddenConversations', JSON.stringify([...hiddenConversations.value]))
  }
  
  activeTab.value = 'chat'
  selectConversation(conv)
}

const handleAction = async (command) => {
  if (command === 'addFriend') {
    showAddFriend.value = true
    searchUserKeyword.value = ''
    searchResults.value = []
  } else if (command === 'createGroup') {
    // 重置表单
    groupForm.name = ''
    groupForm.avatar = ''
    groupForm.memberIds = []
    // 先加载好友列表，再打开对话框
    await loadFriendList()
    showCreateGroup.value = true
  }
}

const debouncedSearchUsers = debounce(async () => {
  if (!searchUserKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    const res = await chatApi.searchUsers(searchUserKeyword.value)
    searchResults.value = res.data || []
  } catch (error) {
    console.error('搜索用户失败:', error)
  }
}, 300)

const sendRequest = async (user) => {
  try {
    await chatApi.sendFriendRequest(user.userId, '')
    ElMessage.success('好友申请已发送，等待对方验证')
    // 更新状态为申请中
    user.hasPendingRequest = true
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  }
}

// 从搜索结果中同意好友申请
const acceptFromSearch = async (user) => {
  try {
    // 找到对应的申请
    const requests = await chatApi.getFriendRequests()
    const req = requests.data?.find(r => r.fromUserId === user.userId)
    if (req) {
      await chatApi.handleFriendRequest(req.id, true)
      ElMessage.success('已添加好友')
      user.isFriend = true
      user.hasReceivedRequest = false
      loadFriendList()
      loadPendingRequestCount()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const acceptRequest = async (requestId) => {
  try {
    await chatApi.handleFriendRequest(requestId, true)
    ElMessage.success('已添加好友')
    // 立即刷新
    await loadFriendRequests()
    await loadFriendList()
    await loadPendingRequestCount()
    await loadConversations()
    // 延迟再刷新一次确保数据库已更新
    setTimeout(() => {
      loadFriendList()
      loadConversations()
    }, 500)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const rejectRequest = async (requestId) => {
  try {
    await chatApi.handleFriendRequest(requestId, false)
    ElMessage.success('已拒绝')
    loadFriendRequests()
    loadPendingRequestCount()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const createGroupChat = async () => {
  if (!groupForm.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  if (groupForm.memberIds.length === 0) {
    ElMessage.warning('请选择至少一个成员')
    return
  }
  
  try {
    const res = await chatApi.createGroup(groupForm.name, groupForm.avatar || null, groupForm.memberIds)
    ElMessage.success('群聊创建成功')
    showCreateGroup.value = false
    
    // 重置表单
    groupForm.name = ''
    groupForm.avatar = ''
    groupForm.memberIds = []
    
    // 刷新会话列表
    await loadConversations()
    
    // 如果返回了群信息，直接打开该群聊
    if (res.data) {
      const newConv = conversations.value.find(c => c.type === 'GROUP' && c.targetId === res.data.id)
      if (newConv) {
        selectConversation(newConv)
      }
    }
  } catch (error) {
    console.error('创建群聊失败:', error)
    ElMessage.error(error.response?.data?.message || '创建失败')
  }
}

// 加载群成员
const loadGroupMembers = async (groupId) => {
  try {
    const res = await chatApi.getGroupMembers(groupId)
    groupMembers.value = res.data || res || []
  } catch (error) {
    console.error('加载群成员失败:', error)
    groupMembers.value = []
  }
}

// 退出群聊
const leaveGroup = async () => {
  if (!currentConversation.value?.group?.id) return
  
  try {
    await ElMessageBox.confirm('确定要退出该群聊吗？', '退出群聊', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await chatApi.leaveGroup(currentConversation.value.group.id)
    ElMessage.success('已退出群聊')
    showGroupInfo.value = false
    currentConversation.value = null
    messages.value = []
    await loadConversations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出群聊失败:', error)
      ElMessage.error(error.response?.data?.message || '退出失败')
    }
  }
}

// 解散群聊
const dismissGroup = async () => {
  if (!currentConversation.value?.group?.id) return
  
  try {
    await ElMessageBox.confirm('解散群聊后，所有聊天记录将被删除，确定要解散吗？', '解散群聊', {
      confirmButtonText: '解散',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await chatApi.dismissGroup(currentConversation.value.group.id)
    ElMessage.success('群聊已解散')
    showGroupInfo.value = false
    currentConversation.value = null
    messages.value = []
    await loadConversations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解散群聊失败:', error)
      ElMessage.error(error.response?.data?.message || '解散失败')
    }
  }
}

// 保存群信息
const saveGroupInfo = async () => {
  if (!currentConversation.value?.group?.id) return
  if (!editGroupForm.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  
  try {
    await chatApi.updateGroupInfo(currentConversation.value.group.id, {
      name: editGroupForm.name,
      avatar: editGroupForm.avatar || null
    })
    ElMessage.success('群信息已更新')
    showEditGroupName.value = false
    // 更新本地数据
    currentConversation.value.group.name = editGroupForm.name
    if (editGroupForm.avatar) {
      currentConversation.value.group.avatar = editGroupForm.avatar
    }
    await loadConversations()
  } catch (error) {
    console.error('更新群信息失败:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

// 保存群公告
const saveGroupAnnouncement = async () => {
  if (!currentConversation.value?.group?.id) return
  
  try {
    await chatApi.updateGroupInfo(currentConversation.value.group.id, {
      announcement: editGroupForm.announcement
    })
    ElMessage.success('群公告已更新')
    showEditAnnouncement.value = false
    currentConversation.value.group.announcement = editGroupForm.announcement
  } catch (error) {
    console.error('更新群公告失败:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

// 切换邀请成员
const toggleInvite = (userId) => {
  const index = inviteIds.value.indexOf(userId)
  if (index > -1) {
    inviteIds.value.splice(index, 1)
  } else {
    inviteIds.value.push(userId)
  }
}

// 邀请对话框打开时初始化
const onInviteDialogOpen = () => {
  inviteIds.value = []
  inviteSearchKeyword.value = ''
}

// 根据ID获取好友名称
const getFriendNameById = (userId) => {
  const friend = friendList.value.find(f => f.userId === userId)
  return friend?.remark || friend?.realName || '未知'
}

// 邀请好友加入群聊
const inviteFriendsToGroup = async () => {
  if (!currentConversation.value?.group?.id || inviteIds.value.length === 0) return
  
  try {
    await chatApi.inviteMembers(currentConversation.value.group.id, inviteIds.value)
    ElMessage.success('已邀请好友加入群聊')
    showInviteMembers.value = false
    inviteIds.value = []
    // 重新加载群成员
    await loadGroupMembers(currentConversation.value.group.id)
  } catch (error) {
    console.error('邀请失败:', error)
    ElMessage.error(error.response?.data?.message || '邀请失败')
  }
}

// 显示成员操作选项
const showMemberOptions = (member) => {
  // 如果点击的是自己，不显示操作
  if (member.userId === currentUserId.value) return
  // 普通成员不能操作他人
  if (!isGroupOwnerOrAdmin.value) return
  // 群主不能被操作（除了查看）
  if (member.role === 'OWNER') return
  
  selectedMember.value = member
  showMemberManage.value = true
}

// 加载我的群聊列表
const loadMyGroups = async () => {
  try {
    const res = await chatApi.getMyGroups()
    myGroups.value = res.data || res || []
  } catch (error) {
    console.error('加载群聊列表失败:', error)
    myGroups.value = []
  }
}

// 打开群聊
const openGroupChat = async (group) => {
  showGroupList.value = false
  // 查找是否已有该群的会话
  let conv = conversations.value.find(c => c.type === 'GROUP' && c.targetId === group.id)
  if (!conv) {
    // 如果没有，需要创建会话
    await loadConversations()
    conv = conversations.value.find(c => c.type === 'GROUP' && c.targetId === group.id)
  }
  if (conv) {
    selectConversation(conv)
  }
}

// 设置/取消管理员
const setMemberAdmin = async (userId, isAdmin) => {
  if (!currentConversation.value?.group?.id) return
  
  try {
    await chatApi.setGroupAdmin(currentConversation.value.group.id, userId, isAdmin)
    ElMessage.success(isAdmin ? '已设为管理员' : '已取消管理员')
    showMemberManage.value = false
    // 重新加载群成员
    await loadGroupMembers(currentConversation.value.group.id)
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 转让群主
const transferOwnership = async (userId) => {
  if (!currentConversation.value?.group?.id) return
  
  try {
    await ElMessageBox.confirm('转让群主后，你将变为普通成员，确定要转让吗？', '转让群主', {
      confirmButtonText: '确定转让',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await chatApi.transferGroupOwner(currentConversation.value.group.id, userId)
    ElMessage.success('群主已转让')
    showMemberManage.value = false
    showGroupInfo.value = false
    // 重新加载群成员
    await loadGroupMembers(currentConversation.value.group.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('转让失败:', error)
      ElMessage.error(error.response?.data?.message || '转让失败')
    }
  }
}

// 踢出成员
const kickMember = async (userId) => {
  if (!currentConversation.value?.group?.id) return
  
  try {
    await ElMessageBox.confirm('确定要将该成员移出群聊吗？', '移出成员', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await chatApi.kickGroupMember(currentConversation.value.group.id, userId)
    ElMessage.success('已将成员移出群聊')
    showMemberManage.value = false
    // 重新加载群成员
    await loadGroupMembers(currentConversation.value.group.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移出失败:', error)
      ElMessage.error(error.response?.data?.message || '移出失败')
    }
  }
}

// 清空群聊天记录（本地）
const clearGroupMessages = async () => {
  try {
    await ElMessageBox.confirm('确定要清空本地聊天记录吗？', '清空聊天记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    messages.value = []
    ElMessage.success('聊天记录已清空')
  } catch (error) {
    // 取消操作
  }
}

// 工具方法
const getConversationName = (conv) => {
  if (conv.type === 'PRIVATE') {
    return conv.targetUser?.realName || '未知用户'
  } else {
    return conv.group?.name || '未知群聊'
  }
}

const getConversationAvatar = (conv) => {
  if (conv.type === 'PRIVATE') {
    return conv.targetUser?.avatar
  } else {
    return conv.group?.avatar
  }
}

// 获取会话对方是否在线（私聊）
const isConversationOnline = (conv) => {
  if (conv.type !== 'PRIVATE') return false
  const targetId = Number(conv.targetId || conv.targetUser?.id)
  const friend = friendList.value.find(f => Number(f.userId) === targetId)
  return friend?.online || false
}

const formatTime = (time) => {
  if (!time) return ''
  const d = dayjs(time)
  if (d.isSame(dayjs(), 'day')) {
    return d.format('HH:mm')
  } else if (d.isSame(dayjs().subtract(1, 'day'), 'day')) {
    return '昨天'
  } else {
    return d.format('MM/DD')
  }
}

const formatMessageTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

const renderEmoji = (content) => {
  if (!content) return ''
  // 直接显示内容，emoji字符会正常显示
  return content
}

// 获取媒体文件URL
const getMediaUrl = (url) => {
  if (!url) return ''
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 确保URL以/开头
  if (!url.startsWith('/')) {
    url = '/' + url
  }
  return url
}

// 表情加载失败处理
const handleEmojiError = (e) => {
  e.target.style.display = 'none'
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const showMessageMenu = (event, msg) => {
  messageContextMenu.visible = true
  messageContextMenu.x = event.clientX
  messageContextMenu.y = event.clientY
  messageContextMenu.message = msg
  messageContextMenu.isSelf = msg.senderId === currentUserId.value
}

// 关闭消息右键菜单
const closeMessageMenu = () => {
  messageContextMenu.visible = false
}

// 复制消息
const copyMessage = () => {
  if (messageContextMenu.message?.content) {
    navigator.clipboard.writeText(messageContextMenu.message.content)
    ElMessage.success('已复制')
  }
  closeMessageMenu()
}

// 撤回消息
const recallCurrentMessage = async () => {
  try {
    await chatApi.recallMessage(messageContextMenu.message.id)
    ElMessage.success('消息已撤回')
    loadMessages()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '撤回失败')
  }
  closeMessageMenu()
}

// 转发消息
const forwardMessage = () => {
  ElMessage.info('转发功能开发中')
  closeMessageMenu()
}

// 删除消息（本地删除）
const deleteMessage = () => {
  const index = messages.value.findIndex(m => m.id === messageContextMenu.message.id)
  if (index > -1) {
    messages.value.splice(index, 1)
  }
  closeMessageMenu()
}

// 好友操作处理
const handleFriendAction = async (command, friend) => {
  switch (command) {
    case 'chat':
      startChat(friend)
      break
    case 'detail':
      await loadFriendDetail(friend.userId)
      break
    case 'remark':
      remarkForm.friendId = friend.userId
      remarkForm.remark = friend.remark || ''
      showRemarkDialog.value = true
      break
    case 'group':
      groupMoveForm.friendId = friend.userId
      groupMoveForm.groupName = friend.groupName || '我的好友'
      showGroupDialog.value = true
      break
    case 'delete':
      await confirmDeleteFriend(friend)
      break
    case 'block':
      await confirmBlockFriend(friend)
      break
  }
}

// 加载好友详情
const loadFriendDetail = async (friendId) => {
  try {
    const res = await chatApi.getFriendDetail(friendId)
    currentFriend.value = res.data
    showFriendDetail.value = true
  } catch (error) {
    ElMessage.error('加载好友信息失败')
  }
}

// 从好友详情发起聊天
const startChatWithCurrentFriend = () => {
  if (currentFriend.value) {
    startChat({
      userId: currentFriend.value.userId,
      realName: currentFriend.value.realName,
      avatar: currentFriend.value.avatar
    })
    showFriendDetail.value = false
  }
}

// 保存备注
const saveRemark = async () => {
  try {
    await chatApi.updateFriendRemark(remarkForm.friendId, remarkForm.remark)
    ElMessage.success('备注已保存')
    showRemarkDialog.value = false
    loadFriendList()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 保存好友分组
const saveFriendGroup = async () => {
  try {
    await chatApi.setFriendGroup(groupMoveForm.friendId, groupMoveForm.groupName)
    ElMessage.success('已移动到分组')
    showGroupDialog.value = false
    loadFriendList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 确认删除好友
const confirmDeleteFriend = async (friend) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除好友 "${friend.remark || friend.realName}" 吗？删除后将清空聊天记录。`,
      '删除好友',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await chatApi.deleteFriend(friend.userId)
    ElMessage.success('已删除好友')
    // 立即刷新
    await loadFriendList()
    await loadConversations()
    // 关闭好友详情对话框
    showFriendDetail.value = false
    // 延迟再刷新一次
    setTimeout(() => {
      loadFriendList()
      loadConversations()
    }, 500)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 确认拉黑好友
const confirmBlockFriend = async (friend) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 "${friend.remark || friend.realName}" 加入黑名单吗？`,
      '加入黑名单',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await chatApi.blockFriend(friend.userId)
    ElMessage.success('已加入黑名单')
    loadFriendList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// ==================== 会话管理 ====================

// 显示会话右键菜单
const showConversationMenu = (event, conv) => {
  conversationMenu.visible = true
  conversationMenu.x = event.clientX
  conversationMenu.y = event.clientY
  conversationMenu.conv = conv
}

// 关闭会话菜单
const closeConversationMenu = () => {
  conversationMenu.visible = false
}

// 置顶/取消置顶会话
const togglePinConversation = async () => {
  const conv = conversationMenu.conv
  if (!conv) return
  
  try {
    await chatApi.toggleConversationTop(conv.id, !conv.isTop)
    conv.isTop = !conv.isTop
    ElMessage.success(conv.isTop ? '已置顶' : '已取消置顶')
  } catch (error) {
    // 如果后端接口不存在，用本地状态
    conv.isTop = !conv.isTop
    ElMessage.success(conv.isTop ? '已置顶' : '已取消置顶')
  }
  closeConversationMenu()
}

// 标记会话已读
const markConversationRead = async () => {
  const conv = conversationMenu.conv
  if (!conv) return
  
  try {
    if (conv.id) {
      await chatApi.markMessagesAsRead(conv.id)
    }
    conv.unreadCount = 0
    loadUnreadCount()
    ElMessage.success('已标为已读')
  } catch (error) {
    console.error('标记已读失败:', error)
  }
  closeConversationMenu()
}

// 隐藏会话（不删除消息）
const hideConversation = () => {
  const conv = conversationMenu.conv
  if (!conv) return
  
  const key = getConvKey(conv)
  const newSet = new Set(hiddenConversations.value)
  newSet.add(key)
  hiddenConversations.value = newSet
  
  // 保存到本地存储
  localStorage.setItem('hiddenConversations', JSON.stringify([...hiddenConversations.value]))
  
  // 如果当前正在查看这个会话，清空当前会话
  if (currentConversation.value && getConvKey(currentConversation.value) === key) {
    currentConversation.value = null
    messages.value = []
  }
  
  ElMessage.success('会话已隐藏，点击好友可恢复')
  closeConversationMenu()
}

// 删除会话
const deleteConversation = async () => {
  const conv = conversationMenu.conv
  if (!conv) return
  
  try {
    await ElMessageBox.confirm(
      '确定要删除此会话吗？聊天记录将被清空。',
      '删除会话',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    
    // 从列表中移除
    const index = conversations.value.findIndex(c => c.id === conv.id)
    if (index > -1) {
      conversations.value.splice(index, 1)
    }
    
    // 如果当前正在查看此会话，清空
    if (currentConversation.value?.id === conv.id) {
      currentConversation.value = null
      messages.value = []
    }
    
    ElMessage.success('会话已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error)
    }
  }
  closeConversationMenu()
}

// 好友右键菜单
const showFriendContextMenu = (event, friend) => {
  // 使用下拉菜单代替
}

// 格式化日期
const formatDate = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const handleScroll = (e) => {
  // TODO: 实现上拉加载更多
}

// WebSocket 连接状态
const wsConnected = ref(false)
let wsHeartbeatTimer = null
let wsReconnectTimer = null
let wsReconnectAttempts = 0
const WS_MAX_RECONNECT_ATTEMPTS = 10

// 刷新在线状态
const refreshOnlineStatus = async () => {
  try {
    const res = await chatApi.getOnlineUsers()
    const rawIds = res.data || res || []
    // 转换为数字类型的Set，避免类型不匹配
    const onlineIds = new Set(rawIds.map(id => Number(id)))
    
    // 更新好友列表的在线状态
    friendList.value.forEach(friend => {
      const friendId = Number(friend.userId)
      friend.online = onlineIds.has(friendId)
    })
    
    // 强制刷新会话列表
    conversations.value = [...conversations.value]
  } catch (error) {
    console.error('获取在线状态失败:', error)
  }
}

// 发送心跳
const sendHeartbeat = () => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'PING' }))
  }
}

// 启动心跳
const startHeartbeat = () => {
  stopHeartbeat()
  // 每30秒发送一次心跳
  wsHeartbeatTimer = setInterval(sendHeartbeat, 30000)
}

// 停止心跳
const stopHeartbeat = () => {
  if (wsHeartbeatTimer) {
    clearInterval(wsHeartbeatTimer)
    wsHeartbeatTimer = null
  }
}

// WebSocket 连接
const connectWebSocket = () => {
  if (!currentUserId.value) {
    console.error('无法连接WebSocket: 用户ID未定义')
    return
  }
  
  // 清除重连定时器
  if (wsReconnectTimer) {
    clearTimeout(wsReconnectTimer)
    wsReconnectTimer = null
  }
  
  // 关闭旧连接
  if (ws) {
    if (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING) {
      ws.close()
    }
    ws = null
  }
  
  // 开发环境直接连接后端，生产环境使用当前host
  const isDev = import.meta.env.DEV
  const wsHost = isDev ? 'localhost:8080' : location.host
  const wsProtocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${wsProtocol}//${wsHost}/ws/chat?userId=${currentUserId.value}`
  
  console.log('正在连接WebSocket:', wsUrl)
  
  try {
    ws = new WebSocket(wsUrl)
  } catch (e) {
    console.error('WebSocket 创建失败:', e)
    scheduleReconnect()
    return
  }
  
  ws.onopen = () => {
    console.log('WebSocket 连接成功')
    wsConnected.value = true
    wsReconnectAttempts = 0
    startHeartbeat()
    // 连接成功后刷新在线状态
    refreshOnlineStatus()
  }
  
  ws.onmessage = (event) => {
    // 忽略心跳响应的日志
    if (!event.data.includes('PONG')) {
      console.log('📩 WebSocket 收到消息:', event.data)
    }
    try {
      const data = JSON.parse(event.data)
      handleWebSocketMessage(data)
    } catch (e) {
      console.error('WebSocket 消息解析失败:', e)
    }
  }
  
  ws.onclose = (event) => {
    console.log('❌ WebSocket 连接关闭, code:', event.code, 'reason:', event.reason)
    wsConnected.value = false
    stopHeartbeat()
    // 非正常关闭才重连
    if (event.code !== 1000) {
      scheduleReconnect()
    }
  }
  
  ws.onerror = (error) => {
    console.error('⚠️ WebSocket 错误:', error)
    wsConnected.value = false
  }
}

// 计划重连
const scheduleReconnect = () => {
  if (wsReconnectAttempts >= WS_MAX_RECONNECT_ATTEMPTS) {
    console.error('WebSocket 重连次数已达上限')
    ElMessage.error('连接已断开，请刷新页面重试')
    return
  }
  
  // 指数退避重连，最长30秒
  const delay = Math.min(1000 * Math.pow(2, wsReconnectAttempts), 30000)
  console.log(`${delay/1000}秒后重连...`)
  
  wsReconnectTimer = setTimeout(() => {
    wsReconnectAttempts++
    connectWebSocket()
  }, delay)
}

// 播放消息提示音
const playNotificationSound = () => {
  try {
    // 使用 Web Audio API 生成简单提示音
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    
    oscillator.frequency.value = 800
    oscillator.type = 'sine'
    gainNode.gain.value = 0.1
    
    oscillator.start()
    setTimeout(() => {
      oscillator.stop()
      audioContext.close()
    }, 100)
  } catch (e) {
    // 忽略音频播放错误
  }
}

// 显示浏览器通知
const showBrowserNotification = (title, body) => {
  // 检查是否支持通知
  if (!('Notification' in window)) return
  
  // 请求通知权限
  if (Notification.permission === 'granted') {
    new Notification(title, { body, icon: '/favicon.ico' })
  } else if (Notification.permission !== 'denied') {
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        new Notification(title, { body, icon: '/favicon.ico' })
      }
    })
  }
}

const handleWebSocketMessage = (data) => {
  switch (data.type) {
    case 'CONNECTED':
      // 连接成功确认
      console.log('WebSocket 连接已确认, userId:', data.userId)
      break
    case 'PONG':
      // 心跳响应，不需要处理
      break
    case 'PRIVATE_MESSAGE':
      handleNewPrivateMessage(data.data)
      break
    case 'GROUP_MESSAGE':
      handleNewGroupMessage(data.groupId, data.data)
      break
    case 'FRIEND_REQUEST':
      pendingRequests.value++
      ElMessage.info('收到新的好友申请')
      // 刷新好友申请列表
      loadFriendRequests()
      break
    case 'FRIEND_REQUEST_RESULT':
      // 好友申请被处理
      if (data.accepted) {
        ElMessage.success(`${data.data?.realName || '对方'} 已同意你的好友申请`)
        // 刷新好友列表和会话列表
        loadFriendList()
        loadConversations()
      } else {
        ElMessage.info(`${data.data?.realName || '对方'} 拒绝了你的好友申请`)
      }
      break
    case 'FRIEND_ADDED':
      // 有新好友添加
      ElMessage.success(`已成功添加好友`)
      loadFriendList()
      loadConversations()
      break
    case 'FRIEND_DELETED':
      // 被好友删除
      loadFriendList()
      loadConversations()
      break
    case 'ONLINE_STATUS':
      // 更新好友在线状态
      console.log('在线状态变化:', data.userId, data.online ? '上线' : '离线')
      const onlineFriend = friendList.value.find(f => f.userId === data.userId)
      if (onlineFriend) {
        onlineFriend.online = data.online
      }
      // 强制触发会话列表的重新渲染（因为在线状态是通过方法计算的）
      conversations.value = [...conversations.value]
      break
    case 'MESSAGE_RECALLED':
      handleMessageRecalled(data.messageId)
      break
  }
}

const handleNewPrivateMessage = (msg) => {
  console.log('收到新私聊消息:', msg.senderId, msg.content || msg.type)
  
  // 播放提示音
  playNotificationSound()
  
  // 如果当前正在查看这个会话
  if (currentConversation.value?.type === 'PRIVATE' && 
      currentConversation.value?.targetId === msg.senderId) {
    messages.value.push(msg)
    nextTick(() => scrollToBottom())
    // 标记消息已读
    if (currentConversation.value.id) {
      chatApi.markMessagesAsRead(currentConversation.value.id).catch(() => {})
    }
  } else {
    // 不在当前会话，显示浏览器通知
    showBrowserNotification(msg.sender?.realName || '新消息', getMessagePreview(msg))
  }
  
  // 查找或创建会话
  let conv = conversations.value.find(c => 
    c.type === 'PRIVATE' && c.targetId === msg.senderId
  )
  
  // 如果会话被隐藏，收到新消息时自动恢复显示
  if (conv) {
    const key = getConvKey(conv)
    if (hiddenConversations.value.has(key)) {
      const newSet = new Set(hiddenConversations.value)
      newSet.delete(key)
      hiddenConversations.value = newSet
      localStorage.setItem('hiddenConversations', JSON.stringify([...hiddenConversations.value]))
      console.log('会话已恢复显示:', key)
    }
  }
  
  if (conv) {
    // 更新现有会话
    conv.lastMessage = getMessagePreview(msg)
    conv.lastMessageTime = msg.createTime || new Date().toISOString()
    // 如果不是当前查看的会话，增加未读数
    if (currentConversation.value?.targetId !== msg.senderId) {
      conv.unreadCount = (conv.unreadCount || 0) + 1
    }
  } else {
    // 创建新会话
    const senderInfo = msg.sender || {}
    const newConv = {
      id: null,
      type: 'PRIVATE',
      targetId: msg.senderId,
      targetUser: {
        id: msg.senderId,
        realName: senderInfo.realName || msg.senderName || '新用户',
        avatar: senderInfo.avatar || msg.senderAvatar || '',
        username: senderInfo.username || ''
      },
      lastMessage: getMessagePreview(msg),
      lastMessageTime: msg.createTime || new Date().toISOString(),
      unreadCount: 1,
      isTop: false
    }
    
    // 如果这个会话之前被隐藏，恢复显示
    const key = getConvKey(newConv)
    if (hiddenConversations.value.has(key)) {
      const newSet = new Set(hiddenConversations.value)
      newSet.delete(key)
      hiddenConversations.value = newSet
      localStorage.setItem('hiddenConversations', JSON.stringify([...hiddenConversations.value]))
      console.log('新会话恢复显示:', key)
    }
    
    conversations.value.unshift(newConv)
    console.log('创建新会话:', newConv.targetUser.realName)
    ElMessage.info(`收到 ${newConv.targetUser.realName} 的新消息`)
  }
  
  // 立即更新未读数
  loadUnreadCount()
  
  // 延迟刷新会话列表，确保后端已创建会话
  setTimeout(() => {
    loadConversations()
  }, 300)
}

// 获取消息预览文本
const getMessagePreview = (msg) => {
  if (!msg) return ''
  switch (msg.type) {
    case 'IMAGE': return '[图片]'
    case 'VIDEO': return '[视频]'
    case 'FILE': return '[文件]'
    case 'VOICE': return '[语音]'
    case 'EMOJI': return '[表情]'
    default: return msg.content || ''
  }
}

const handleNewGroupMessage = (groupId, msg) => {
  // 播放提示音
  playNotificationSound()
  
  if (currentConversation.value?.type === 'GROUP' && 
      currentConversation.value?.targetId === groupId) {
    messages.value.push(msg)
    nextTick(() => scrollToBottom())
  } else {
    // 不在当前会话，显示浏览器通知
    const senderName = msg.sender?.realName || '群消息'
    showBrowserNotification(senderName, getMessagePreview(msg))
  }
  
  // 查找群会话，如果被隐藏则恢复
  const conv = conversations.value.find(c => c.type === 'GROUP' && c.targetId === groupId)
  if (conv) {
    const key = getConvKey(conv)
    if (hiddenConversations.value.has(key)) {
      const newSet = new Set(hiddenConversations.value)
      newSet.delete(key)
      hiddenConversations.value = newSet
      localStorage.setItem('hiddenConversations', JSON.stringify([...hiddenConversations.value]))
      console.log('群会话恢复显示:', key)
    }
  }
  
  loadConversations()
  loadUnreadCount()
}

const handleMessageRecalled = (messageId) => {
  const msg = messages.value.find(m => m.id === messageId)
  if (msg) {
    msg.isRecalled = true
    msg.content = '消息已撤回'
  }
}

// 点击其他地方关闭右键菜单
const handleDocumentClick = () => {
  messageContextMenu.visible = false
  conversationMenu.visible = false
}

// 加载隐藏的会话
const loadHiddenConversations = () => {
  const saved = localStorage.getItem('hiddenConversations')
  if (saved) {
    try {
      const arr = JSON.parse(saved)
      hiddenConversations.value = new Set(arr)
    } catch (e) {
      console.error('加载隐藏会话失败:', e)
    }
  }
}

// 自动刷新定时器
let refreshTimer = null

// 开始自动刷新（每30秒刷新好友列表和在线状态）
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadFriendList()
    loadPendingRequestCount()
    // 如果WebSocket已连接，刷新在线状态
    if (wsConnected.value) {
      refreshOnlineStatus()
    }
  }, 30000)
}

// 生命周期
onMounted(async () => {
  // 加载隐藏的会话记录
  loadHiddenConversations()
  
  try {
    await Promise.all([
      loadConversations(),
      loadFriendList(),
      loadUnreadCount(),
      loadPendingRequestCount()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
  }
  
  // 确保 WebSocket 连接
  if (currentUserId.value) {
    connectWebSocket()
  }
  
  // 添加全局点击事件监听
  document.addEventListener('click', handleDocumentClick)
  
  // 开始自动刷新
  startAutoRefresh()
})

// 监听用户信息变化，当用户ID可用时自动连接 WebSocket
watch(() => userStore.userInfo, (newUserInfo) => {
  if (newUserInfo?.id && !wsConnected.value && !ws) {
    connectWebSocket()
  }
}, { immediate: true })

// 监听群设置对话框打开，加载群成员并初始化表单
watch(showGroupInfo, async (visible) => {
  if (visible && currentConversation.value?.type === 'GROUP' && currentConversation.value?.group?.id) {
    await loadGroupMembers(currentConversation.value.group.id)
    // 初始化编辑表单
    editGroupForm.name = currentConversation.value.group.name || ''
    editGroupForm.avatar = currentConversation.value.group.avatar || ''
    editGroupForm.announcement = currentConversation.value.group.announcement || ''
  }
})

// 监听群聊列表对话框打开，加载群聊列表
watch(showGroupList, async (visible) => {
  if (visible) {
    await loadMyGroups()
  }
})

onUnmounted(() => {
  // 关闭WebSocket连接
  stopHeartbeat()
  if (wsReconnectTimer) {
    clearTimeout(wsReconnectTimer)
  }
  if (ws) {
    ws.close(1000, 'Component unmounted')
  }
  
  // 移除全局点击事件监听
  document.removeEventListener('click', handleDocumentClick)
  
  // 清除自动刷新定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 监听好友申请弹窗
watch(showFriendRequests, (val) => {
  if (val) {
    loadFriendRequests()
  }
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.chat-room {
  display: flex;
  height: calc(100vh - 140px);
  background: $bg-glass;
  backdrop-filter: blur($blur-lg);
  -webkit-backdrop-filter: blur($blur-lg);
  border: 1px solid $glass-border;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-glass;
}

// 左侧面板
.left-panel {
  width: 320px;
  border-right: 1px solid $glass-border;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.3);
}

.panel-header {
  padding: $spacing-md;
  display: flex;
  gap: $spacing-sm;
  border-bottom: 1px solid $glass-border;
  
  .search-input {
    flex: 1;
    :deep(.el-input__wrapper) {
      background: $glass-white-light;
      box-shadow: none;
      border: 1px solid $glass-border;
      border-radius: $radius-md;
      
      &:hover, &.is-focus {
        background: $glass-white;
        box-shadow: $shadow-sm;
      }
    }
  }
  
  .action-btn {
    background: $glass-white;
    border: 1px solid $glass-border;
    border-radius: $radius-md;
    box-shadow: $shadow-sm;
    color: $text-primary;
    
    &:hover {
      background: $primary-color;
      color: #fff;
      transform: translateY(-1px);
      box-shadow: $shadow-md;
    }
  }
  
  .ws-status {
    font-size: 12px;
    color: $danger-color;
    cursor: pointer;
    margin-right: 8px;
    
    &.connected {
      color: $success-color;
      cursor: default;
    }
  }
}

.tab-bar {
  display: flex;
  border-bottom: 1px solid $glass-border;
  background: rgba(255, 255, 255, 0.2);
  
  .tab-item {
    flex: 1;
    padding: $spacing-md;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    font-size: 12px;
    font-weight: 600;
    transition: all 150ms;
    color: $text-secondary;
    
    &:hover {
      background: $bg-hover;
      color: $text-primary;
    }
    
    &.active {
      background: transparent;
      color: $primary-color;
      position: relative;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 20px;
        height: 3px;
        background: $primary-color;
        border-radius: $radius-full;
      }
    }
    
    .el-icon {
      font-size: 20px;
    }
  }
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  
  .conversation-item {
    display: flex;
    align-items: center;
    padding: $spacing-md;
    gap: $spacing-md;
    cursor: pointer;
    border-bottom: 1px solid $glass-border;
    transition: all 150ms;
    position: relative;
    margin: $spacing-xs $spacing-sm;
    border-radius: $radius-lg;
    
    &:hover {
      background: $bg-hover;
    }
    
    &.active {
      background: $glass-white;
      box-shadow: $shadow-sm;
    }
    
    &.pinned {
      background: rgba($ios-yellow, 0.1);
    }
    
    .pin-indicator {
      position: absolute;
      top: 4px;
      right: 4px;
      font-size: 10px;
    }
    
    .conv-info {
      flex: 1;
      min-width: 0;
      
      .conv-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;
        
        .conv-name {
          font-weight: 600;
          font-size: 14px;
          color: $text-primary;
        }
        
        .conv-time {
          font-size: 12px;
          color: $text-secondary;
        }
      }
      
      .conv-preview {
        font-size: 12px;
        color: $text-secondary;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        
        .group-tag {
          color: $primary-color;
          margin-right: 4px;
          font-weight: 600;
        }
      }
    }
    
    .avatar-wrapper {
      position: relative;
      flex-shrink: 0;
      display: inline-block;
      
      .online-dot {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: #c0c4cc;
        border: 2px solid #fff;
        z-index: 10;
        box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1);
        
        &.online {
          background: $success-color;
          box-shadow: 0 0 4px rgba($success-color, 0.5);
        }
      }
    }
  }
}

// 通讯录
.contacts-panel {
  flex: 1;
  overflow-y: auto;
  
  .contact-section {
    display: flex;
    align-items: center;
    padding: $spacing-md;
    gap: $spacing-md;
    cursor: pointer;
    border-bottom: 1px solid $glass-border;
    margin: $spacing-xs $spacing-sm;
    border-radius: $radius-lg;
    
    &:hover {
      background: $bg-hover;
    }
    
    .section-icon {
      font-size: 24px;
      color: $primary-color;
    }
  }
}

.friend-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  gap: $spacing-md;
  cursor: pointer;
  border-bottom: 1px solid $glass-border;
  margin: $spacing-xs $spacing-sm;
  border-radius: $radius-lg;
  
  &:hover {
    background: $bg-hover;
  }
  
  .friend-avatar-wrapper {
    position: relative;
    flex-shrink: 0;
    display: inline-block;
    
    .online-dot {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: #c0c4cc;
      border: 2px solid #fff;
      z-index: 10;
      box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1);
      
      &.online {
        background: $success-color;
        box-shadow: 0 0 4px rgba($success-color, 0.5);
      }
    }
  }
  
  .friend-info {
    flex: 1;
    
    .friend-name {
      font-weight: 600;
      display: block;
      color: $text-primary;
    }
    
    .online-status {
      font-size: 12px;
      color: $text-secondary;
      
      &.online {
        color: $success-color;
      }
    }
  }
}

// 右侧面板
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
}

.chat-header {
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid $glass-border;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  
  .chat-title {
    font-size: 16px;
    font-weight: 700;
    color: $text-primary;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-lg;
  
  .loading-more {
    text-align: center;
    padding: $spacing-md;
    color: $text-secondary;
  }
}

.message-item {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
  
  .message-content {
    display: flex;
    flex-direction: column;
    max-width: 60%;
    
    .message-sender {
      font-size: 12px;
      color: $text-secondary;
      margin-bottom: 4px;
    }
    
    .message-bubble {
      padding: $spacing-sm $spacing-md;
      background: $glass-white;
      border: 1px solid $glass-border;
      border-radius: $radius-lg;
      word-break: break-word;
      box-shadow: $shadow-sm;
      backdrop-filter: blur(5px);
      color: $text-primary;
    }
  }
  
  // 自己发送的消息样式 - 放在后面以确保覆盖
  &.is-self {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
      
      .message-bubble {
        background: $primary-color;
        color: white;
        border: none;
        box-shadow: 0 2px 8px rgba($primary-color, 0.3);
      }
    }
  }
  
  // 消息内容中的媒体元素
  .message-image {
    max-width: 200px;
    max-height: 200px;
    border-radius: $radius-md;
    cursor: pointer;
  }
  
  .image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100px;
    height: 100px;
    background: rgba(0, 0, 0, 0.05);
    border-radius: $radius-md;
    color: #999;
    font-size: 12px;
    gap: 4px;
  }
  
  .message-video {
    max-width: 300px;
    max-height: 200px;
    border-radius: $radius-md;
  }
  
  .message-emoji {
    width: 80px;
    height: 80px;
    object-fit: contain;
  }
  
  .recalled-message,
  .system-message {
    color: $text-secondary;
    font-style: italic;
  }
  
  .message-time {
    font-size: 11px;
    color: $text-placeholder;
    margin-top: 4px;
  }
}

// 输入区域
.input-area {
  border-top: 1px solid $glass-border;
  padding: $spacing-md;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  
  .input-toolbar {
    display: flex;
    flex-direction: row;
    gap: $spacing-md;
    margin-bottom: $spacing-sm;
    
    .toolbar-btn {
      font-size: 20px;
      color: $text-secondary;
      cursor: pointer;
      padding: 6px;
      border-radius: $radius-sm;
      transition: all 0.2s;
      
      &:hover {
        color: $primary-color;
        background: $bg-hover;
      }
      
      .el-icon {
        font-size: 20px;
      }
    }
    
    // 确保 el-upload 不破坏横向布局
    :deep(.el-upload) {
      display: inline-flex;
    }
  }
  
  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: $spacing-sm;
    
    .input-hint {
      font-size: 12px;
      color: $text-secondary;
    }
  }
  
  :deep(.el-textarea__inner) {
    background: $glass-white-light;
    border: 1px solid $glass-border;
    border-radius: $radius-lg;
    resize: none;
    box-shadow: none;
    transition: all 0.2s;
    
    &:hover, &:focus {
      background: $glass-white;
      box-shadow: $shadow-sm;
      border-color: $primary-color;
    }
  }
}

// 群成员选择
.member-select-list {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid $glass-border;
  border-radius: $radius-lg;
  padding: $spacing-xs;
  background: $glass-white-light;
}

.member-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  cursor: pointer;
  transition: all 150ms;
  border-radius: $radius-md;
  margin-bottom: 2px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &:hover {
    background: $bg-hover;
  }
  
  &.selected {
    background: rgba($primary-color, 0.15);
    border: 1px solid rgba($primary-color, 0.3);
    
    .member-name {
      color: $primary-color;
      font-weight: 500;
    }
  }
  
  .el-checkbox {
    margin-right: 0;
  }
  
  .member-name {
    flex: 1;
    font-size: 14px;
  }
}

.no-friends-tip {
  color: $text-secondary;
  text-align: center;
  padding: $spacing-lg;
}

.selected-count {
  margin-top: $spacing-sm;
  color: $primary-color;
  font-size: 12px;
}

// 好友申请
.friend-requests {
  max-height: 400px;
  overflow-y: auto;
}

.request-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1px solid $glass-border;
  
  .request-info {
    flex: 1;
    
    .request-name {
      font-weight: 600;
      display: block;
      color: $text-primary;
    }
    
    .request-message {
      font-size: 12px;
      color: $text-secondary;
    }
  }
  
  .request-actions {
    display: flex;
    gap: $spacing-xs;
  }
}

// 好友更多按钮
.friend-more-btn {
  opacity: 0;
  transition: opacity 150ms;
  color: $text-secondary;
  
  &:hover {
    color: $primary-color;
  }
}

.friend-item:hover .friend-more-btn {
  opacity: 1;
}

// 好友详情
.friend-profile {
  .profile-header {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    margin-bottom: $spacing-lg;
    
    .profile-info {
      h3 {
        margin: 0 0 4px;
        font-size: 18px;
        color: $text-primary;
      }
      
      .username {
        margin: 0 0 8px;
        color: $text-secondary;
        font-size: 14px;
      }
    }
  }
  
  .profile-details {
    :deep(.el-descriptions__label) {
      width: 80px;
      font-weight: 600;
      color: $text-secondary;
    }
    :deep(.el-descriptions__content) {
      color: $text-primary;
    }
  }
}

// 群设置对话框
.group-profile {
  .profile-header {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    padding-bottom: $spacing-md;
    border-bottom: 1px solid $glass-border;
    margin-bottom: $spacing-md;
    
    .avatar-wrapper {
      position: relative;
      
      .edit-avatar-btn {
        position: absolute;
        bottom: -4px;
        right: -4px;
        width: 24px;
        height: 24px;
        background: white;
        border: 1px solid $glass-border;
        box-shadow: $shadow-sm;
      }
    }
    
    .profile-info {
      flex: 1;
      
      .name-row {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        
        h3 {
          margin: 0;
          font-size: 18px;
          color: $text-primary;
        }
      }
      
      .group-meta {
        margin: 8px 0 0;
        display: flex;
        align-items: center;
        gap: $spacing-md;
        color: $text-secondary;
        font-size: 13px;
        
        .member-count {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }
  
  .group-section {
    margin-bottom: $spacing-md;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-sm;
      
      .section-title {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        font-weight: 600;
        color: $text-primary;
        font-size: 14px;
      }
    }
    
    .announcement-content {
      padding: $spacing-sm $spacing-md;
      background: $glass-white-light;
      border-radius: $radius-md;
      color: $text-secondary;
      font-size: 13px;
      line-height: 1.6;
      min-height: 40px;
    }
  }
  
  .group-members-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-sm;
    max-height: 200px;
    overflow-y: auto;
    padding: $spacing-sm;
    background: $glass-white-light;
    border-radius: $radius-lg;
  }
  
  .member-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-sm;
    border-radius: $radius-md;
    cursor: pointer;
    transition: all 150ms;
    position: relative;
    
    &:hover {
      background: $bg-hover;
    }
    
    .member-name {
      font-size: 12px;
      color: $text-primary;
      margin-top: 4px;
      text-align: center;
      max-width: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .role-tag {
      position: absolute;
      top: 2px;
      right: 2px;
      font-size: 10px;
      padding: 0 4px;
      height: 16px;
      line-height: 16px;
    }
    
    &.add-member {
      .add-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        border: 2px dashed $glass-border;
        display: flex;
        align-items: center;
        justify-content: center;
        color: $text-secondary;
        font-size: 20px;
      }
      
      &:hover .add-icon {
        border-color: $primary-color;
        color: $primary-color;
      }
    }
  }
  
  .group-actions {
    display: flex;
    justify-content: center;
    gap: $spacing-md;
    padding-top: $spacing-md;
    border-top: 1px solid $glass-border;
    margin-top: $spacing-md;
  }
}

.no-group-info {
  text-align: center;
  padding: $spacing-xl;
  color: $text-secondary;
  
  .el-icon {
    color: $text-placeholder;
    margin-bottom: $spacing-sm;
  }
  
  p {
    margin: 0;
  }
}

// 邀请成员对话框
.invite-search {
  margin-bottom: $spacing-md;
}

.selected-friends {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-md;
  padding: $spacing-sm;
  background: $glass-white-light;
  border-radius: $radius-md;
  
  .label {
    font-size: 12px;
    color: $text-secondary;
  }
}

.invite-member-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid $glass-border;
  border-radius: $radius-lg;
  padding: $spacing-xs;
  
  .invite-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    cursor: pointer;
    border-radius: $radius-md;
    transition: all 150ms;
    
    &:hover {
      background: $bg-hover;
    }
    
    &.selected {
      background: rgba($primary-color, 0.1);
      border: 1px solid rgba($primary-color, 0.2);
    }
    
    .friend-avatar-wrapper {
      position: relative;
      
      .online-indicator {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 10px;
        height: 10px;
        background: $success-color;
        border: 2px solid white;
        border-radius: 50%;
      }
    }
    
    .friend-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .friend-name {
        font-size: 14px;
        color: $text-primary;
      }
      
      .friend-status {
        font-size: 12px;
        color: $text-secondary;
      }
    }
  }
  
  .no-friends {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: $spacing-xl;
    color: $text-secondary;
    
    .el-icon {
      margin-bottom: $spacing-sm;
      color: $text-placeholder;
    }
    
    p {
      margin: 0;
      font-size: 14px;
    }
  }
}

.invite-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  
  .select-count {
    font-size: 13px;
    color: $text-secondary;
  }
  
  .footer-buttons {
    display: flex;
    gap: $spacing-sm;
  }
}

// 消息右键菜单
.context-menu {
  position: fixed;
  background: $glass-white-strong;
  backdrop-filter: blur(20px);
  border: 1px solid $glass-border;
  border-radius: $radius-lg;
  box-shadow: $shadow-glass;
  z-index: 1000;
  min-width: 120px;
  overflow: hidden;
  padding: 4px;
  
  .menu-item {
    padding: 8px 12px;
    cursor: pointer;
    font-size: 14px;
    transition: all 150ms;
    border-radius: $radius-sm;
    color: $text-primary;
    
    &:hover {
      background: $primary-color;
      color: white;
    }
    
    &.danger {
      color: $danger-color;
      
      &:hover {
        background: $danger-color;
        color: white;
      }
    }
  }
}

// 消息已读状态
.message-status {
  font-size: 11px;
  color: $text-placeholder;
  margin-top: 2px;
  
  &.read {
    color: $primary-color;
  }
}

// 群聊列表
.group-list {
  max-height: 350px;
  overflow-y: auto;
}

.group-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all 150ms;
  
  &:hover {
    background: $bg-hover;
  }
  
  .group-info {
    flex: 1;
    
    .group-name {
      font-weight: 600;
      color: $text-primary;
      font-size: 15px;
    }
    
    .group-meta {
      font-size: 12px;
      color: $text-secondary;
      margin-top: 4px;
    }
  }
}

// 成员管理对话框
.member-manage-content {
  .member-profile {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    
    .member-info {
      h4 {
        margin: 0 0 4px;
        font-size: 16px;
        color: $text-primary;
      }
      
      .member-role {
        margin: 0;
      }
    }
  }
  
  .member-actions {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
    
    .el-button {
      justify-content: flex-start;
    }
  }
}

// 消息发送者角色标签
.message-sender {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  
  .sender-name {
    font-size: 12px;
    color: $text-secondary;
  }
  
  .role-badge {
    font-size: 10px;
    padding: 0 4px;
    height: 16px;
    line-height: 14px;
  }
}
</style>
