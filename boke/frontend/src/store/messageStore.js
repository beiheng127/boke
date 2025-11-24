// src/store/messageStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import http from '../api/http'

export const useMessageStore = defineStore('message', () => {
    const conversations = ref([])
    const currentMessages = ref([])
    const activeConversation = ref(null)
    const loading = ref(false)
    const hasMore = ref(true)
    const currentPage = ref(0)
    const serverUnreadCount = ref(0)

    // 计算属性
    const totalUnreadCount = computed(() => {
        return conversations.value.reduce((total, conv) => {
            const currentUserId = JSON.parse(localStorage.getItem('user'))?.id
            if (currentUserId === conv.user1?.id) {
                return total + (conv.unreadCountUser1 || 0)
            } else {
                return total + (conv.unreadCountUser2 || 0)
            }
        }, 0)
    })


    // 获取未读消息总数 - 从服务器获取
    async function loadUnreadCount() {
        try {
            const { data } = await http.get('/api/messages/unread-count')
            serverUnreadCount.value = data.count
            console.log('✅ 获取服务器未读消息数:', data.count)
            return data.count
        } catch (error) {
            console.error('❌ 获取未读消息数失败:', error)
            return 0
        }
    }

    // 加载会话列表
    async function loadConversations() {
        try {
            loading.value = true
            const { data } = await http.get('/api/messages/conversations')
            conversations.value = data.content
            console.log('✅ 加载会话列表成功:', data.content.length, '个会话')
        } catch (error) {
            console.error('❌ 加载会话列表失败:', error)
            throw error
        } finally {
            loading.value = false
        }
    }

    // 加载聊天记录
    async function loadMessages(userId, page = 0) {
        try {
            loading.value = true
            const { data } = await http.get(`/api/messages/with/${userId}`, {
                params: { page, size: 20 }
            })

            if (page === 0) {
                currentMessages.value = data.content.reverse()
            } else {
                currentMessages.value = [...data.content.reverse(), ...currentMessages.value]
            }

            hasMore.value = !data.last
            currentPage.value = page

            console.log('✅ 加载消息成功:', data.content.length, '条消息')
        } catch (error) {
            console.error('❌ 加载消息失败:', error)
            throw error
        } finally {
            loading.value = false
        }
    }

    // 加载更多消息
    async function loadMoreMessages(userId) {
        if (hasMore.value && !loading.value) {
            await loadMessages(userId, currentPage.value + 1)
        }
    }

    // 发送文本消息
    async function sendMessage(receiverId, content) {
        try {
            const { data } = await http.post('/api/messages', {
                receiverId,
                content
            })

            // 添加到当前消息列表
            currentMessages.value.push(data)

            // 更新会话列表
            await loadConversations()

            console.log('✅ 发送消息成功:', data)
            return data
        } catch (error) {
            console.error('❌ 发送消息失败:', error)
            throw error
        }
    }

    // 发送文件消息
    async function sendFileMessage(receiverId, file, content = '') {
        try {
            const formData = new FormData()
            formData.append('receiverId', receiverId.toString())
            formData.append('file', file)
            if (content) {
                formData.append('content', content)
            }

            const { data } = await http.post('/api/messages/file', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            })

            // 添加到当前消息列表
            currentMessages.value.push(data)

            // 更新会话列表
            await loadConversations()

            console.log('✅ 发送文件消息成功:', data)
            return data
        } catch (error) {
            console.error('❌ 发送文件消息失败:', error)
            throw error
        }
    }

    // 标记消息为已读
    async function markAsRead(senderId) {
        try {
            await http.put('/api/messages/read', { senderId })

            // 更新本地未读计数
            const conversation = conversations.value.find(conv =>
                conv.user1?.id === senderId || conv.user2?.id === senderId
            )
            if (conversation) {
                const currentUserId = JSON.parse(localStorage.getItem('user'))?.id
                if (currentUserId === conversation.user1?.id) {
                    conversation.unreadCountUser1 = 0
                } else {
                    conversation.unreadCountUser2 = 0
                }
            }

            console.log('✅ 标记消息已读成功')
        } catch (error) {
            console.error('❌ 标记消息已读失败:', error)
        }
    }

    // 撤回消息
    async function recallMessage(messageId) {
        try {
            await http.delete(`/api/messages/${messageId}`)

            // 从当前消息列表中移除
            const index = currentMessages.value.findIndex(msg => msg.id === messageId)
            if (index !== -1) {
                currentMessages.value.splice(index, 1)
            }

            // 更新会话列表
            await loadConversations()

            console.log('✅ 撤回消息成功')
        } catch (error) {
            console.error('❌ 撤回消息失败:', error)
            throw error
        }
    }

    // 获取未读消息总数
    async function loadUnreadCount() {
        try {
            const { data } = await http.get('/api/messages/unread-count')
            serverUnreadCount.value = data.count
            console.log('✅ 获取未读消息数成功:', data.count)
            return data.count
        } catch (error) {
            console.error('❌ 获取未读消息数失败:', error)
            return 0
        }
    }

    // 设置当前活跃会话
    function setActiveConversation(conversation) {
        activeConversation.value = conversation
        currentMessages.value = []
        currentPage.value = 0
        hasMore.value = true
    }

    // 清空当前会话
    function clearActiveConversation() {
        activeConversation.value = null
        currentMessages.value = []
    }

    return {
        conversations,
        currentMessages,
        activeConversation,
        loading,
        unreadCount: totalUnreadCount,
        loadConversations,
        loadMessages,
        loadMoreMessages,
        sendMessage,
        sendFileMessage,
        markAsRead,
        recallMessage,
        loadUnreadCount,
        setActiveConversation,
        clearActiveConversation
    }
},{
    persist: {
      key: 'message-store', // 存储键名
      storage: localStorage, // 存储到 localStorage
      // 只持久化会话列表和未读计数（按需调整）
      paths: ['conversations',  'activeConversation']
      // 说明：currentMessages 通常不需要持久化（可重新加载），loading/hasMore 是临时状态，无需持久化
    }
  }
)