<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="new-message-modal">
      <div class="modal-header">
        <h2>新建消息</h2>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="modal-content">
        <!-- 搜索区域 -->
        <div class="search-section">
          <div class="search-input-wrapper">
            <input
                v-model="searchKeyword"
                placeholder="搜索用户..."
                @input="handleSearchInput"
                class="search-input"
            />
            <div class="search-icon">🔍</div>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div class="search-results">
          <div v-if="searchLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>搜索中...</p>
          </div>

          <div v-else-if="searchResults.length === 0 && searchKeyword" class="empty-results">
            <div class="empty-icon">👤</div>
            <h4>没有找到用户</h4>
            <p>请尝试其他关键词</p>
          </div>

          <div v-else-if="searchResults.length > 0" class="results-list">
            <div
                v-for="user in searchResults"
                :key="user.id"
                class="user-result-item"
                @click="selectUser(user)"
            >
              <div class="user-avatar">
                <img :src="user.avatarUrl || '/uploads/images/default/touxiang.jpg'" :alt="user.displayName" />
              </div>
              <div class="user-info">
                <h4 class="user-name">{{ user.displayName }}</h4>
                <p class="user-username">@{{ user.username }}</p>
                <p class="user-role">{{ user.role === 'BLOGGER' ? '博主' : '读者' }}</p>
              </div>
              <div class="user-action">
                <button class="btn primary small">选择</button>
              </div>
            </div>
          </div>

          <div v-else class="search-placeholder">
            <div class="placeholder-icon">💬</div>
            <h4>开始新的对话</h4>
            <p>搜索用户并开始聊天</p>
          </div>
        </div>

        <!-- 消息输入区域 -->
        <div v-if="selectedUser" class="message-input-section">
          <div class="selected-user">
            <div class="selected-user-info">
              <img
                  :src="selectedUser.avatarUrl || '/uploads/images/default/touxiang.jpg'"
                  :alt="selectedUser.displayName"
                  class="selected-user-avatar"
              />
              <div class="selected-user-details">
                <h4>{{ selectedUser.displayName }}</h4>
                <p>@{{ selectedUser.username }}</p>
              </div>
            </div>
            <button class="change-user-btn" @click="selectedUser = null">更改</button>
          </div>

          <div class="message-composer">
            <textarea
                v-model="messageContent"
                placeholder="输入消息内容..."
                rows="4"
                class="message-textarea"
                ref="messageTextarea"
            ></textarea>
            <div class="composer-actions">
              <button
                  class="btn ghost"
                  @click="$emit('close')"
              >
                取消
              </button>
              <button
                  class="btn primary"
                  @click="sendMessage"
                  :disabled="!messageContent.trim() || sendingMessage"
              >
                {{ sendingMessage ? '发送中...' : '发送' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const emit = defineEmits(['send', 'search', 'close'])

const props = defineProps({
  searchResults: {
    type: Array,
    default: () => []
  },
  searchLoading: {
    type: Boolean,
    default: false
  }
})

const searchKeyword = ref('')
const selectedUser = ref(null)
const messageContent = ref('')
const sendingMessage = ref(false)
const messageTextarea = ref(null)

// 防抖搜索
let searchTimeout = null

function handleSearchInput() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    if (searchKeyword.value.trim()) {
      emit('search', searchKeyword.value.trim())
    }
  }, 300)
}

function selectUser(user) {
  selectedUser.value = user
  // 自动聚焦到消息输入框
  nextTick(() => {
    messageTextarea.value?.focus()
  })
}

async function sendMessage() {
  if (!selectedUser.value || !messageContent.value.trim() || sendingMessage.value) {
    return
  }

  try {
    sendingMessage.value = true
    await emit('send', {
      receiverId: selectedUser.value.id,
      content: messageContent.value.trim()
    })

    // 重置状态
    messageContent.value = ''
    selectedUser.value = null
    searchKeyword.value = ''
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    sendingMessage.value = false
  }
}

// 监听搜索关键词变化
watch(searchKeyword, (newVal) => {
  if (!newVal.trim()) {
    // 清空搜索结果
    emit('search', '')
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.new-message-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.3s ease;
}

.close-btn:hover {
  background: #f0f0f0;
}

.modal-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 搜索区域 */
.search-section {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.search-input-wrapper {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 40px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
}

/* 搜索结果 */
.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
}

.loading-state, .empty-results, .search-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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
  margin-bottom: 16px;
}

.empty-icon, .placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-results h4, .search-placeholder h4 {
  margin: 0 0 8px 0;
  color: #666;
}

.empty-results p, .search-placeholder p {
  margin: 0;
  color: #999;
  font-size: 0.9rem;
}

.results-list {
  padding: 16px 0;
}

.user-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-result-item:hover {
  background: #f8f9fa;
  border-color: #e1e5e9;
}

.user-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-name {
  margin: 0 0 4px 0;
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
}

.user-username {
  margin: 0 0 4px 0;
  font-size: 0.9rem;
  color: #666;
}

.user-role {
  margin: 0;
  font-size: 0.8rem;
  color: #999;
  background: #f8f9fa;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.user-action {
  flex-shrink: 0;
}

/* 消息输入区域 */
.message-input-section {
  border-top: 1px solid #f0f0f0;
  padding: 24px;
}

.selected-user {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.selected-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.selected-user-details h4 {
  margin: 0 0 4px 0;
  font-size: 1rem;
  color: #2c3e50;
}

.selected-user-details p {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
}

.change-user-btn {
  background: none;
  border: 1px solid #e1e5e9;
  color: #666;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.3s ease;
}

.change-user-btn:hover {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
}

.message-composer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-textarea {
  width: 100%;
  padding: 16px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
  min-height: 100px;
  transition: border-color 0.3s ease;
}

.message-textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
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
  transform: translateY(-1px);
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
  padding: 6px 12px;
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
  .modal-overlay {
    padding: 16px;
  }

  .new-message-modal {
    max-height: 90vh;
  }

  .modal-header {
    padding: 20px;
  }

  .search-section {
    padding: 16px 20px;
  }

  .search-results {
    padding: 0 20px;
  }

  .message-input-section {
    padding: 20px;
  }

  .selected-user {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .change-user-btn {
    align-self: flex-end;
  }
}
</style>