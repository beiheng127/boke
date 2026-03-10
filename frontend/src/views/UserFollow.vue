<!-- src/views/UserFollow.vue -->
<template>
  <div class="user-follow-page">
    <!-- 头部导航 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
        返回
      </button>
      <h1 class="page-title">关注关系</h1>
    </div>

    <!-- 用户信息 -->
    <div class="user-info-section" v-if="userInfo">
      <div class="user-avatar">
        <img :src="userInfo.avatarUrl || '/uploads/images/default/touxiang.jpg'" :alt="userInfo.displayName" />
      </div>
      <div class="user-details">
        <h2 class="user-name">{{ userInfo.displayName || userInfo.username }}</h2>
        <p class="user-username">@{{ userInfo.username }}</p>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tab-section">
      <div class="tabs">
        <button
            class="tab-btn"
            :class="{ active: activeTab === 'following' }"
            @click="switchTab('following')"
        >
          <span class="tab-count">{{ stats.followingCount || 0 }}</span>
          关注
        </button>
        <button
            class="tab-btn"
            :class="{ active: activeTab === 'followers' }"
            @click="switchTab('followers')"
        >
          <span class="tab-count">{{ stats.followersCount || 0 }}</span>
          粉丝
        </button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <!-- 关注列表 -->
      <div v-if="activeTab === 'following'" class="list-container">
        <h3 class="list-title">关注列表</h3>
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        <div v-else-if="following.length === 0" class="empty-state">
          <div class="empty-icon">👀</div>
          <h4>还没有关注任何人</h4>
          <p>去发现有趣的用户吧</p>
        </div>
        <div v-else class="user-list">
          <div
              v-for="item in following"
              :key="item.id"
              class="user-card"
              @click="goToUserProfile(item.user.id)"
          >
            <div class="user-main">
              <img
                  :src="item.user.avatarUrl || '/uploads/images/default/touxiang.jpg'"
                  :alt="item.user.displayName"
                  class="user-avatar"
                  @error="handleImageError"
              />
              <div class="user-info">
                <h4 class="user-name">{{ item.user.displayName || item.user.username }}</h4>
                <p class="user-bio" v-if="item.user.signature">{{ item.user.signature }}</p>
                <p class="follow-date">关注于 {{ formatDate(item.createdAt) }}</p>
              </div>
            </div>
            <button
                v-if="auth.isAuthed && item.user.id !== auth.user?.id"
                class="btn small follow-btn"
                :class="{ 'unfollow': item.isFollowing }"
                @click.stop="toggleFollow(item.user.id, item.isFollowing)"
                :disabled="followLoading[item.user.id]"
            >
              {{ followLoading[item.user.id] ? '处理中...' : (item.isFollowing ? '已关注' : '关注') }}
            </button>
          </div>
        </div>
      </div>

      <!-- 粉丝列表 -->
      <div v-if="activeTab === 'followers'" class="list-container">
        <h3 class="list-title">粉丝列表</h3>
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        <div v-else-if="followers.length === 0" class="empty-state">
          <div class="empty-icon">⭐</div>
          <h4>还没有粉丝</h4>
          <p>积极互动可以获得更多关注哦</p>
        </div>
        <div v-else class="user-list">
          <div
              v-for="item in followers"
              :key="item.id"
              class="user-card"
              @click="goToUserProfile(item.user.id)"
          >
            <div class="user-main">
              <img
                  :src="item.user.avatarUrl || '/uploads/images/default/touxiang.jpg'"
                  :alt="item.user.displayName"
                  class="user-avatar"
                  @error="handleImageError"
              />
              <div class="user-info">
                <h4 class="user-name">{{ item.user.displayName || item.user.username }}</h4>
                <p class="user-bio" v-if="item.user.signature">{{ item.user.signature }}</p>
                <p class="follow-date">关注于 {{ formatDate(item.createdAt) }}</p>
              </div>
            </div>
            <button
                v-if="auth.isAuthed && item.user.id !== auth.user?.id"
                class="btn small follow-btn"
                :class="{ 'unfollow': item.isFollowing }"
                @click.stop="toggleFollow(item.user.id, item.isFollowing)"
                :disabled="followLoading[item.user.id]"
            >
              {{ followLoading[item.user.id] ? '处理中...' : (item.isFollowing ? '已关注' : '回关') }}
            </button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="!loading && (following.length > 0 || followers.length > 0)" class="pagination">
        <button class="btn ghost" :disabled="page <= 0" @click="prevPage">上一页</button>
        <span class="page-info">第 {{ page + 1 }} 页</span>
        <button class="btn ghost" :disabled="!hasMore" @click="nextPage">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import http from '../api/http'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 数据
const userInfo = ref(null)
const stats = ref({ followingCount: 0, followersCount: 0 })
const following = ref([])
const followers = ref([])
const activeTab = ref('following')
const page = ref(0)
const size = ref(20)
const hasMore = ref(true)
const loading = ref(false)
const followLoading = ref({}) // 用于跟踪每个用户的关注操作状态

// 计算属性
const currentUserId = computed(() => {
  return parseInt(route.params.userId)
})

const isOwnProfile = computed(() => {
  return currentUserId.value === auth.user?.id
})

// 方法
function goBack() {
  router.back()
}

function formatDate(dateString) {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch (e) {
    return '未知时间'
  }
}

function handleImageError(event) {
  event.target.src = '/uploads/images/default/touxiang.jpg'
}

function switchTab(tab) {
  activeTab.value = tab
  page.value = 0
  hasMore.value = true
  loadCurrentList()
}

function goToUserProfile(userId) {
  router.push(`/user/${userId}`)
}

async function loadUserInfo() {
  try {
    console.log('🔍 加载用户信息，用户ID:', currentUserId.value)
    const { data } = await http.get(`/api/users/${currentUserId.value}`)
    userInfo.value = data
    console.log('✅ 用户信息加载成功:', data)
  } catch (error) {
    console.error('❌ 加载用户信息失败:', error)
  }
}

async function loadStats() {
  try {
    console.log('🔍 加载关注统计，用户ID:', currentUserId.value)
    const { data } = await http.get(`/api/follows/${currentUserId.value}/stats`)
    stats.value = data
    console.log('✅ 关注统计加载成功:', data)
  } catch (error) {
    console.error('❌ 加载统计数据失败:', error)
    stats.value = { followingCount: 0, followersCount: 0 }
  }
}

async function loadFollowing() {
  loading.value = true
  try {
    console.log('🔍 加载关注列表，参数:', {
      userId: currentUserId.value,
      page: page.value,
      size: size.value
    })

    const { data } = await http.get(`/api/follows/${currentUserId.value}/following`, {
      params: { page: page.value, size: size.value }
    })

    console.log('✅ 关注列表API响应:', data)

    // 处理数据，检查关注状态
    const processedData = await Promise.all(
        data.content.map(async (item) => {
          let isFollowing = false
          if (auth.isAuthed) {
            try {
              const { data: followStatus } = await http.get(`/api/follows/${item.user.id}/is-following`)
              isFollowing = followStatus.isFollowing
            } catch (error) {
              console.error('检查关注状态失败:', error)
            }
          }
          return {
            ...item,
            isFollowing
          }
        })
    )

    if (page.value === 0) {
      following.value = processedData
    } else {
      following.value = [...following.value, ...processedData]
    }

    hasMore.value = !data.last
    console.log('✅ 关注列表处理完成:', processedData.length, '条数据')
  } catch (error) {
    console.error('❌ 加载关注列表失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    if (page.value === 0) {
      following.value = []
    }
  } finally {
    loading.value = false
  }
}

async function loadFollowers() {
  loading.value = true
  try {
    console.log('🔍 加载粉丝列表，参数:', {
      userId: currentUserId.value,
      page: page.value,
      size: size.value
    })

    const { data } = await http.get(`/api/follows/${currentUserId.value}/followers`, {
      params: { page: page.value, size: size.value }
    })

    console.log('✅ 粉丝列表API响应:', data)

    // 处理数据，检查是否互相关注
    const processedData = await Promise.all(
        data.content.map(async (item) => {
          let isFollowing = false
          if (auth.isAuthed) {
            try {
              const { data: followStatus } = await http.get(`/api/follows/${item.user.id}/is-following`)
              isFollowing = followStatus.isFollowing
            } catch (error) {
              console.error('检查关注状态失败:', error)
            }
          }
          return {
            ...item,
            isFollowing
          }
        })
    )

    if (page.value === 0) {
      followers.value = processedData
    } else {
      followers.value = [...followers.value, ...processedData]
    }

    hasMore.value = !data.last
    console.log('✅ 粉丝列表处理完成:', processedData.length, '条数据')
  } catch (error) {
    console.error('❌ 加载粉丝列表失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    if (page.value === 0) {
      followers.value = []
    }
  } finally {
    loading.value = false
  }
}

async function toggleFollow(userId, isCurrentlyFollowing) {
  if (!auth.isAuthed) {
    alert('请先登录')
    router.push('/login')
    return
  }

  if (userId === auth.user?.id) {
    alert('不能关注自己')
    return
  }

  // 设置加载状态
  followLoading.value[userId] = true

  try {
    if (isCurrentlyFollowing) {
      // 取消关注
      await http.delete(`/api/follows/${userId}`)
      console.log('✅ 取消关注成功:', userId)

      // 更新本地状态
      updateFollowStatus(userId, false)
    } else {
      // 关注
      await http.post(`/api/follows/${userId}`)
      console.log('✅ 关注成功:', userId)

      // 更新本地状态
      updateFollowStatus(userId, true)
    }

    // 重新加载统计
    await loadStats()

  } catch (error) {
    console.error('❌ 关注操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  } finally {
    // 清除加载状态
    followLoading.value[userId] = false
  }
}

function updateFollowStatus(userId, isFollowing) {
  // 更新关注列表中的状态
  following.value = following.value.map(item => {
    if (item.user.id === userId) {
      return { ...item, isFollowing }
    }
    return item
  })

  // 更新粉丝列表中的状态
  followers.value = followers.value.map(item => {
    if (item.user.id === userId) {
      return { ...item, isFollowing }
    }
    return item
  })
}

function prevPage() {
  if (page.value > 0) {
    page.value--
    loadCurrentList()
  }
}

function nextPage() {
  if (hasMore.value) {
    page.value++
    loadCurrentList()
  }
}

function loadCurrentList() {
  if (activeTab.value === 'following') {
    loadFollowing()
  } else {
    loadFollowers()
  }
}

// 监听路由参数
watch(() => route.params.userId, () => {
  page.value = 0
  hasMore.value = true
  loadUserInfo()
  loadStats()
  loadCurrentList()
})

// 监听标签变化
watch(activeTab, () => {
  page.value = 0
  hasMore.value = true
})

// 初始化
onMounted(() => {
  console.log('🔍 UserFollow 初始化')
  console.log('🔍 路由参数:', route.params)
  console.log('🔍 查询参数:', route.query)

  // 从查询参数设置活动标签
  if (route.query.tab && (route.query.tab === 'following' || route.query.tab === 'followers')) {
    activeTab.value = route.query.tab
  }

  loadUserInfo()
  loadStats()
  loadCurrentList()
})
</script>

<style scoped>
.user-follow-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f8f9fa;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #e9ecef;
  border-color: #3498db;
  color: #3498db;
}

.back-icon {
  font-size: 16px;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.user-username {
  color: #666;
  margin: 0;
}

.tab-section {
  margin-bottom: 30px;
}

.tabs {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tab-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
}

.tab-btn.active {
  background: #667eea;
  color: white;
}

.tab-count {
  font-size: 1.25rem;
  font-weight: 700;
}

.content-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.list-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #888;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h4 {
  margin: 0 0 8px 0;
  font-size: 1.25rem;
  color: #666;
}

.empty-state p {
  margin: 0;
  color: #999;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.user-card:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.user-main {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.user-bio {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 4px 0;
  line-height: 1.4;
}

.follow-date {
  color: #999;
  font-size: 0.8rem;
  margin: 0;
}

.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  font-size: 14px;
}

.btn.ghost {
  background: transparent;
  border: 1px solid #e1e5e9;
  color: #666;
}

.btn.ghost:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #667eea;
  color: #667eea;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.small {
  padding: 6px 12px;
  font-size: 12px;
}

.follow-btn {
  background: #667eea;
  color: white;
  border: 1px solid #667eea;
  white-space: nowrap;
}

.follow-btn:hover:not(:disabled) {
  background: #5a6fd8;
  transform: translateY(-1px);
}

.follow-btn.unfollow {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #e1e5e9;
}

.follow-btn.unfollow:hover:not(:disabled) {
  background: #e74c3c;
  color: white;
  border-color: #e74c3c;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.page-info {
  color: #666;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-follow-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .user-info-section {
    flex-direction: column;
    text-align: center;
  }

  .content-section {
    padding: 16px;
  }

  .user-card {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .user-main {
    justify-content: center;
    text-align: center;
  }
}
</style>