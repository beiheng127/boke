<template>
  <div class="message-chat">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="chat-user-info">
        <img
            :src="getOtherUser(conversation)?.avatarUrl || '/uploads/images/default/touxiang.jpg'"
            :alt="getOtherUser(conversation)?.displayName"
            class="chat-user-avatar"
        />
        <div class="chat-user-details">
          <h3 class="chat-user-name">{{ getOtherUser(conversation)?.displayName }}</h3>
          <p class="chat-user-status" v-if="getOtherUser(conversation)?.role">
            {{ getOtherUser(conversation)?.role === 'BLOGGER' ? '博主' : '读者' }}
          </p>
        </div>
      </div>
      <div class="chat-actions">
        <button class="btn ghost small" @click="loadMoreMessages" :disabled="!hasMore || loading">
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="messages-list" ref="messagesList">
      <div v-if="loading && messages.length === 0" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载消息中...</p>
      </div>

      <div v-else-if="messages.length === 0" class="empty-messages">
        <div class="empty-icon">💬</div>
        <h4>还没有消息</h4>
        <p>开始与 {{ getOtherUser(conversation)?.displayName }} 聊天吧</p>
      </div>

      <div v-else class="messages-container">
        <!-- 加载更多提示 -->
        <div v-if="hasMore" class="load-more-indicator">
          <button
              class="btn ghost small"
              @click="loadMoreMessages"
              :disabled="loading"
          >
            {{ loading ? '加载中...' : '查看更多历史消息' }}
          </button>
        </div>

        <!-- 消息项 -->
        <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{
            'sent': isSentByMe(message),
            'received': !isSentByMe(message),
            'file': message.type === 'FILE'
          }"
        >
          <div class="message-avatar">
            <img
                :src="message.sender?.avatarUrl || '/uploads/images/default/touxiang.jpg'"
                :alt="message.sender?.displayName"
            />
          </div>
          <div class="message-content">
            <!-- 文件消息 -->
            <div v-if="message.type === 'FILE'" class="file-message">
              <div class="file-info">
                <div class="file-icon">📎</div>
                <div class="file-details">
                  <p class="file-name">{{ message.fileName }}</p>
                  <p class="file-size">{{ formatFileSize(message.fileSize) }}</p>
                </div>
              </div>
              <a
                  :href="message.fileUrl"
                  target="_blank"
                  class="btn ghost small download-btn"
              >
                下载
              </a>
            </div>

            <!-- 文本消息 -->
            <div v-else class="text-message">
              <p class="message-text">{{ message.content }}</p>
            </div>

            <!-- 消息状态 -->
            <div class="message-status">
              <span class="message-time">{{ formatMessageTime(message.createdAt) }}</span>
              <span v-if="isSentByMe(message)" class="message-read-status">
                {{ message.isRead ? '已读' : '未读' }}
              </span>
              <!-- 撤回按钮（2分钟内且是自己发送的） -->
              <button
                  v-if="canRecall(message)"
                  class="recall-btn"
                  @click="recallMessage(message.id)"
                  :disabled="recallingMessageId === message.id"
              >
                {{ recallingMessageId === message.id ? '撤回中...' : '撤回' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息输入框 -->
    <div class="message-input-container">
      <!-- 文件上传区域 -->
      <div v-if="showFileUpload" class="file-upload-section">
        <div class="upload-header">
          <h4>发送文件</h4>
          <button class="close-upload" @click="showFileUpload = false">×</button>
        </div>
        <div class="upload-content">
          <input
              type="file"
              ref="fileInput"
              @change="handleFileSelect"
              class="file-input"
          />
          <div class="upload-preview" v-if="selectedFile">
            <div class="file-preview">
              <div class="file-icon">📎</div>
              <div class="file-info">
                <p class="file-name" :title="selectedFile.name">
                  {{ selectedFile.name.length > 20 ? selectedFile.name.substring(0, 20) + '...' : selectedFile.name }}
                </p>
                <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
              </div>
              <button class="remove-file" @click="removeSelectedFile">×</button>
            </div>
            <div class="upload-actions">
              <button class="btn ghost" @click="removeSelectedFile">取消</button>
              <button
                  class="btn primary"
                  @click="sendFileMessage"
                  :disabled="uploadingFile"
              >
                {{ uploadingFile ? '发送中...' : '发送' }}
              </button>
            </div>
          </div>
          <div v-else class="upload-placeholder">
            <div class="upload-icon">📎</div>
            <p>选择要发送的文件</p>
            <button class="btn primary" @click="triggerFileInput">选择文件</button>
          </div>
        </div>
      </div>

      <!-- 输入工具栏 -->
      <div class="input-toolbar">
        <button
            class="toolbar-btn"
            @click="toggleFileUpload"
            title="发送文件"
        >
          📎
        </button>
        <div class="input-wrapper">
          <textarea
              v-model="newMessage"
              placeholder="输入消息..."
              @keydown.enter.exact.prevent="sendTextMessage"
              class="message-textarea"
              rows="1"
              ref="messageInput"
          ></textarea>
          <button
              class="send-btn"
              @click="sendTextMessage"
              :disabled="!newMessage.trim() || sendingMessage"
          >
            {{ sendingMessage ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>



<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useMessageStore } from '../store/messageStore'
import { useAuthStore } from '../store/auth'

const props = defineProps({
  conversation: Object,
  messages: Array
})

const emit = defineEmits(['send-message', 'send-file', 'recall-message', 'load-more'])

const messageStore = useMessageStore()
const authStore = useAuthStore()

const messagesList = ref(null)
const messageInput = ref(null)
const fileInput = ref(null)

const newMessage = ref('')
const sendingMessage = ref(false)
const showFileUpload = ref(false)
const selectedFile = ref(null)
const uploadingFile = ref(false)
const recallingMessageId = ref(null)

// 计算属性
const hasMore = computed(() => messageStore.hasMore)
const loading = computed(() => messageStore.loading)

// 方法
function getOtherUser(conversation) {
  const currentUserId = authStore.user?.id
  return currentUserId === conversation.user1?.id ? conversation.user2 : conversation.user1
}

function isSentByMe(message) {
  return message.sender?.id === authStore.user?.id
}

function canRecall(message) {
  if (!isSentByMe(message)) return false

  // 2分钟内可以撤回
  const messageTime = new Date(message.createdAt).getTime()
  const currentTime = Date.now()
  const twoMinutes = 2 * 60 * 1000

  return (currentTime - messageTime) < twoMinutes
}

function formatMessageTime(timestamp) {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const now = new Date()
  const diffInMinutes = (now - date) / (1000 * 60)

  if (diffInMinutes < 1) {
    return '刚刚'
  } else if (diffInMinutes < 60) {
    return `${Math.floor(diffInMinutes)}分钟前`
  } else if (diffInMinutes < 1440) {
    return `${Math.floor(diffInMinutes / 60)}小时前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B'

  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

async function sendTextMessage() {
  if (!newMessage.value.trim() || sendingMessage.value) return

  try {
    sendingMessage.value = true
    await emit('send-message', newMessage.value.trim())
    newMessage.value = ''

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    sendingMessage.value = false
  }
}

function toggleFileUpload() {
  showFileUpload.value = !showFileUpload.value
  if (!showFileUpload.value) {
    removeSelectedFile()
  }
}

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileSelect(event) {
  const file = event.target.files[0]
  if (file) {
    // 文件大小限制：10MB
    const maxSize = 10 * 1024 * 1024
    if (file.size > maxSize) {
      alert('文件大小不能超过10MB')
      return
    }
    selectedFile.value = file
  }
}

function removeSelectedFile() {
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

async function sendFileMessage() {
  if (!selectedFile.value || uploadingFile.value) return

  try {
    uploadingFile.value = true
    await emit('send-file', selectedFile.value, newMessage.value.trim())

    // 重置状态
    removeSelectedFile()
    showFileUpload.value = false
    newMessage.value = ''

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送文件失败:', error)
  } finally {
    uploadingFile.value = false
  }
}

async function recallMessage(messageId) {
  try {
    recallingMessageId.value = messageId
    await emit('recall-message', messageId)
  } catch (error) {
    console.error('撤回消息失败:', error)
  } finally {
    recallingMessageId.value = null
  }
}

async function loadMoreMessages() {
  if (!loading.value && hasMore.value) {
    await emit('load-more')
    await nextTick()
    // 保持滚动位置
    maintainScrollPosition()
  }
}

function scrollToBottom() {
  if (messagesList.value) {
    messagesList.value.scrollTop = messagesList.value.scrollHeight
  }
}

function maintainScrollPosition() {
  if (messagesList.value) {
    const oldScrollHeight = messagesList.value.scrollHeight
    messagesList.value.scrollTop = messagesList.value.scrollHeight - oldScrollHeight
  }
}

// 自动调整文本域高度
function adjustTextareaHeight() {
  if (messageInput.value) {
    messageInput.value.style.height = 'auto'
    messageInput.value.style.height = Math.min(messageInput.value.scrollHeight, 120) + 'px'
  }
}

// 监听消息变化，自动滚动到底部
watch(() => props.messages.length, () => {
  if (props.messages.length > 0) {
    nextTick(() => {
      scrollToBottom()
    })
  }
})

onMounted(() => {
  // 初始滚动到底部
  nextTick(() => {
    scrollToBottom()
  })

  // 监听文本域输入
  if (messageInput.value) {
    messageInput.value.addEventListener('input', adjustTextareaHeight)
  }
})

onUnmounted(() => {
  if (messageInput.value) {
    messageInput.value.removeEventListener('input', adjustTextareaHeight)
  }
})
</script>

<style scoped>
.message-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: white;
  flex-shrink: 0;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.chat-user-details {
  flex: 1;
}

.chat-user-name {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
}

.chat-user-status {
  margin: 0;
  font-size: 0.8rem;
  color: #666;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

/* 消息列表 */
.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
  min-height: 0;
  height: 0;
}

.loading-state, .empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
  text-align: center;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-messages h4 {
  margin: 0 0 8px 0;
  color: #666;
}

.empty-messages p {
  margin: 0;
  color: #999;
  font-size: 0.9rem;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: min-content;
}

.load-more-indicator {
  text-align: center;
  padding: 16px 0;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 8px;
  max-width: 70%;
}

.message-item.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.received {
  align-self: flex-start;
}

.message-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 文本消息 */
.text-message {
  background: white;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 100%;
}

.message-item.sent .text-message {
  background: #3498db;
  color: white;
}

.message-text {
  margin: 0;
  line-height: 1.4;
  word-wrap: break-word;
}

/* 文件消息 */
.file-message {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 300px;
}

.message-item.sent .file-message {
  background: #3498db;
  color: white;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-icon {
  font-size: 1.5rem;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  margin: 0 0 4px 0;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.7;
}

.download-btn {
  flex-shrink: 0;
}

/* 消息状态 */
.message-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.75rem;
  color: #999;
  margin-top: 4px;
}

.message-item.sent .message-status {
  justify-content: flex-end;
}

.message-read-status {
  color: #27ae60;
}

.recall-btn {
  background: none;
  border: none;
  color: #e74c3c;
  cursor: pointer;
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.recall-btn:hover:not(:disabled) {
  background: rgba(231, 76, 60, 0.1);
}

.recall-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 消息输入框 */
.message-input-container {
  border-top: 1px solid #f0f0f0;
  background: white;
  flex-shrink: 0;
}

/* 文件上传区域 */
.file-upload-section {
  border-bottom: 1px solid #f0f0f0;
  background: #f8f9fa;
}

.upload-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e1e5e9;
}

.upload-header h4 {
  margin: 0;
  font-size: 1rem;
  color: #2c3e50;
}

.close-upload {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-content {
  padding: 16px;
}

.file-input {
  display: none;
}

.upload-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e1e5e9;
  min-width: 0;
}

.file-preview .file-icon {
  font-size: 1.25rem;
}

.file-preview .file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  margin: 0 0 4px 0;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.remove-file {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  color: #666;
  padding: 4px;
}

.upload-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.upload-placeholder {
  text-align: center;
  padding: 20px;
  color: #666;
}

.upload-placeholder .upload-icon {
  font-size: 2rem;
  margin-bottom: 8px;
  opacity: 0.6;
}

.upload-placeholder p {
  margin: 0 0 12px 0;
}

/* 输入工具栏 */
.input-toolbar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px;
}

.toolbar-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.toolbar-btn:hover {
  background: #f0f0f0;
}

.input-wrapper {
  flex: 1;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 8px;
}

.message-textarea {
  flex: 1;
  border: none;
  background: none;
  resize: none;
  font-family: inherit;
  font-size: 1rem;
  line-height: 1.4;
  max-height: 120px;
  padding: 8px;
}

.message-textarea:focus {
  outline: none;
}

.send-btn {
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-1px);
}

.send-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
}

.btn.primary {
  background: #3498db;
  color: white;
}

.btn.primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn.ghost {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #e1e5e9;
  color: #666;
}

.btn.ghost:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
}

.btn.small {
  padding: 4px 8px;
  font-size: 0.75rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-item {
    max-width: 85%;
  }

  .file-message {
    max-width: 250px;
  }

  .chat-header {
    padding: 12px 16px;
  }

  .messages-list {
    padding: 12px;
  }

  .input-toolbar {
    padding: 12px;
  }
}
</style>