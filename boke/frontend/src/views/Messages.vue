<template>
  <div class="messages-container">
    <div class="messages-layout">
      <!-- 会话列表侧边栏 -->
      <div class="conversations-sidebar">
        <div class="sidebar-header">
          <h2>💬 私信</h2>
          <button class="btn primary new-message-btn" @click="showNewMessageModal = true">
            ✉️ 新消息
          </button>
        </div>

        <!-- 搜索用户 -->
        <div class="search-section">
          <input
              v-model="searchKeyword"
              placeholder="搜索用户..."
              class="search-input"
              @input="searchUsers"
          />
        </div>

        <!-- 会话列表 -->
        <div class="conversations-list">
          <div v-if="messageStore.loading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>

          <div v-else-if="filteredConversations.length === 0" class="empty-state">
            <div class="empty-icon">💬</div>
            <h4>还没有对话</h4>
            <p>开始与朋友聊天吧</p>
          </div>

          <div v-else class="conversation-items">
            <div
                v-for="conversation in filteredConversations"
                :key="conversation.id"
                class="conversation-item"
                :class="{ active: isActiveConversation(conversation) }"
                @click="selectConversation(conversation)"
            >
              <div class="conversation-avatar">
                <img :src="getOtherUser(conversation).avatarUrl" :alt="getOtherUser(conversation).displayName" />
                <div v-if="getUnreadCount(conversation) > 0" class="unread-badge">
                  {{ getUnreadCount(conversation) }}
                </div>
              </div>
              <div class="conversation-content">
                <div class="conversation-header">
                  <h4 class="user-name">{{ getOtherUser(conversation).displayName }}</h4>
                  <span class="time">{{ formatTime(conversation.lastMessageAt) }}</span>
                </div>
                <p class="last-message">{{ conversation.lastMessageContent }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-area">
        <div v-if="messageStore.activeConversation" class="chat-container">
          <MessageChat
              :conversation="messageStore.activeConversation"
              :messages="messageStore.currentMessages"
              @send-message="handleSendMessage"
              @send-file="handleSendFile"
              @recall-message="handleRecallMessage"
              @load-more="handleLoadMore"
          />
        </div>
        <div v-else class="empty-chat">
          <div class="empty-icon">💬</div>
          <h3>选择对话开始聊天</h3>
          <p>或点击"新消息"开始新的对话</p>
        </div>
      </div>
    </div>

    <!-- 新建消息模态框 -->
    <NewMessageModal
        v-if="showNewMessageModal"
        :search-results="searchResults"
        :search-loading="searchLoading"
        @send="handleNewMessage"
        @search="handleSearch"
        @close="showNewMessageModal=false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useMessageStore } from '../store/messageStore'
import { useAuthStore } from '../store/auth'
import MessageChat from '../components/MessageChat.vue'
import NewMessageModal from '../components/NewMessageModal.vue'
import http from '../api/http'

const messageStore = useMessageStore()
const authStore = useAuthStore()

const showNewMessageModal = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searchLoading = ref(false)

// 计算属性
const filteredConversations = computed(() => {
  if (!searchKeyword.value) {
    return messageStore.conversations
  }

  const keyword = searchKeyword.value.toLowerCase()
  return messageStore.conversations.filter(conv => {
    const otherUser = getOtherUser(conv)
    return otherUser.displayName.toLowerCase().includes(keyword) ||
        otherUser.username.toLowerCase().includes(keyword)
  })
})

// 方法
function getOtherUser(conversation) {
  const currentUserId = authStore.user?.id
  return currentUserId === conversation.user1?.id ? conversation.user2 : conversation.user1
}

function getUnreadCount(conversation) {
  const currentUserId = authStore.user?.id
  if (currentUserId === conversation.user1?.id) {
    return conversation.unreadCountUser1 || 0
  } else {
    return conversation.unreadCountUser2 || 0
  }
}

function isActiveConversation(conversation) {
  if (!messageStore.activeConversation) return false
  const currentOtherUser = getOtherUser(messageStore.activeConversation)
  const otherUser = getOtherUser(conversation)
  return currentOtherUser.id === otherUser.id
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diffInHours = (now - date) / (1000 * 60 * 60)

  if (diffInHours < 1) {
    return '刚刚'
  } else if (diffInHours < 24) {
    return `${Math.floor(diffInHours)}小时前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

async function selectConversation(conversation) {
  messageStore.setActiveConversation(conversation)
  const otherUser = getOtherUser(conversation)
  await messageStore.loadMessages(otherUser.id)
  await messageStore.markAsRead(otherUser.id)
}

async function handleSendMessage(content) {
  if (messageStore.activeConversation) {
    const otherUser = getOtherUser(messageStore.activeConversation)
    await messageStore.sendMessage(otherUser.id, content)
  }
}

async function handleSendFile(file, content) {
  if (messageStore.activeConversation) {
    const otherUser = getOtherUser(messageStore.activeConversation)
    await messageStore.sendFileMessage(otherUser.id, file, content)
  }
}

async function handleRecallMessage(messageId) {
  await messageStore.recallMessage(messageId)
}

async function handleLoadMore() {
  if (messageStore.activeConversation) {
    const otherUser = getOtherUser(messageStore.activeConversation)
    await messageStore.loadMoreMessages(otherUser.id)
  }
}

async function handleNewMessage({ receiverId, content }) {
  // 创建新会话
  await messageStore.sendMessage(receiverId, content)
  showNewMessageModal.value = false

  // 重新加载会话列表
  await messageStore.loadConversations()
}

async function handleSearch(keyword) {
  if (!keyword.trim()) {
    searchResults.value = []
    return
  }

  try {
    searchLoading.value = true
    const { data } = await http.get('/api/users/search', {
      params: { keyword, page: 0, size: 10 }
    })
    searchResults.value = data.content.filter(user => user.id !== authStore.user?.id)
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    searchLoading.value = false
  }
}

async function searchUsers() {
  await handleSearch(searchKeyword.value)
}

// 生命周期
onMounted(async () => {
  await messageStore.loadConversations()
  await messageStore.loadUnreadCount()
})

onUnmounted(() => {
  messageStore.clearActiveConversation()
})
</script>

<style scoped>
.messages-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  height: calc(100vh - 80px);
}

.messages-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
  height: 100%;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 侧边栏样式 */
.conversations-sidebar {
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.sidebar-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.new-message-btn {
  padding: 8px 16px;
  font-size: 0.9rem;
}

.search-section {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.loading-state, .empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #666;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-state h4 {
  margin: 0 0 8px 0;
  color: #666;
}

.empty-state p {
  margin: 0;
  color: #999;
  font-size: 0.9rem;
}

.conversation-items {
  padding: 8px 0;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f8f9fa;
}

.conversation-item:hover {
  background: #f8f9fa;
}

.conversation-item.active {
  background: #e3f2fd;
  border-right: 3px solid #3498db;
}

.conversation-avatar {
  position: relative;
  margin-right: 12px;
}

.conversation-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #e74c3c;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 0.7rem;
  font-weight: 600;
  min-width: 18px;
  text-align: center;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.user-name {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  font-size: 0.8rem;
  color: #999;
  flex-shrink: 0;
  margin-left: 8px;
}

.last-message {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 聊天区域 */
.chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
  text-align: center;
}

.empty-chat .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-chat h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.empty-chat p {
  margin: 0;
  color: #999;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .messages-container {
    padding: 16px;
    height: calc(100vh - 60px);
  }

  .messages-layout {
    grid-template-columns: 1fr;
    height: 100%;
  }

  .conversations-sidebar {
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
    max-height: 300px;
  }

  .chat-area {
    height: calc(100vh - 350px);
  }
}
</style>