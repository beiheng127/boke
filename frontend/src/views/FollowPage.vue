<template>
  <div class="follow-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">关注关系</h1>
      <div class="user-info" v-if="userInfo">
        <div class="avatar-wrapper" @click="goToUserProfile(userInfo.id)">
          <img :src="userInfo.avatarUrl" :alt="userInfo.displayName" class="user-avatar" />
          <div class="avatar-overlay">
            <span>查看主页</span>
          </div>
        </div>
        <div class="user-details">
          <h2 class="user-name" @click="goToUserProfile(userInfo.id)">{{ userInfo.displayName }}</h2>
          <p class="user-username">@{{ userInfo.username }}</p>
        </div>
      </div>
    </div>

    <!-- 统计标签 -->
    <div class="stats-tabs">
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
    <div class="content-area">
      <!-- 关注列表 -->
      <div v-if="activeTab === 'following'" class="list-section">
        <h3 class="section-title">关注列表</h3>
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
                v-if="!isOwnProfile && auth.isAuthed"
                class="btn small follow-btn"
                :class="{ 'unfollow': item.isFollowing }"
                @click.stop="toggleFollow(item.user.id, item.isFollowing)"
            >
              {{ item.isFollowing ? '已关注' : '关注' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 粉丝列表 -->
      <div v-else-if="activeTab === 'followers'" class="list-section">
        <h3 class="section-title">粉丝列表</h3>
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
            >
              {{ item.isFollowing ? '已关注' : '回关' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="(activeTab === 'following' && following.length > 0) || (activeTab === 'followers' && followers.length > 0)"
           class="pagination">
        <button class="btn ghost" :disabled="page <= 0" @click="prevPage">
          上一页
        </button>
        <span class="page-info">第 {{ page + 1 }} 页</span>
        <button class="btn ghost" :disabled="!hasMore" @click="nextPage">
          下一页
        </button>
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

// 计算属性
const currentUserId = computed(() => {
  return route.params.userId ? parseInt(route.params.userId) : auth.user?.id
})

const isOwnProfile = computed(() => {
  return currentUserId.value === auth.user?.id
})

// 方法
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

function goToUserProfile(userId) {
  router.push(`/user/${userId}`)
}

async function switchTab(tab) {
  if (activeTab.value === tab) return

  activeTab.value = tab
  page.value = 0
  hasMore.value = true
  await loadCurrentList()
}

async function loadUserInfo() {
  try {
    console.log('🔍 加载用户信息，用户ID:', currentUserId.value)
    const {data} = await http.get(`/api/users/${currentUserId.value}`)
    userInfo.value = data
    console.log('✅ 用户信息加载成功:', data)
  } catch (error) {
    console.error('❌ 加载用户信息失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
  }
}

async function loadStats() {
  try {
    console.log('🔍 加载关注统计，用户ID:', currentUserId.value)
    const {data} = await http.get(`/api/follows/${currentUserId.value}/stats`)
    stats.value = data
    console.log('✅ 关注统计加载成功:', data)
  } catch (error) {
    console.error('❌ 加载统计数据失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    // 设置默认值
    stats.value = {followingCount: 0, followersCount: 0}
  }
}

async function loadFollowing() {
  if (loading.value) return

  loading.value = true
  try {
    console.log('🔍 加载关注列表，参数:', {
      userId: currentUserId.value,
      page: page.value,
      size: size.value
    })

    const {data} = await http.get(`/api/follows/${currentUserId.value}/following`, {
      params: {page: page.value, size: size.value}
    })

    console.log('✅ 关注列表API响应:', data)

    // 处理数据，添加关注状态
    const processedData = data.content.map(item => ({
      ...item,
      isFollowing: true // 关注列表中的用户都是已关注的
    }))

    following.value = processedData
    hasMore.value = !data.last

    console.log('✅ 关注列表处理完成:', processedData.length, '条数据')
  } catch (error) {
    console.error('❌ 加载关注列表失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    following.value = []
  } finally {
    loading.value = false
  }
}

async function loadFollowers() {
  if (loading.value) return

  loading.value = true
  try {
    console.log('🔍 加载粉丝列表，参数:', {
      userId: currentUserId.value,
      page: page.value,
      size: size.value
    })

    const {data} = await http.get(`/api/follows/${currentUserId.value}/followers`, {
      params: {page: page.value, size: size.value}
    })

    console.log('✅ 粉丝列表API响应:', data)

    // 处理数据，检查是否互相关注
    const processedData = await Promise.all(
        data.content.map(async (item) => {
          let isFollowing = false
          if (auth.isAuthed) {
            try {
              const {data: followStatus} = await http.get(`/api/follows/${item.user.id}/is-following`, {
                params: {followerId: auth.user?.id}
              })
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

    followers.value = processedData
    hasMore.value = !data.last

    console.log('✅ 粉丝列表处理完成:', processedData.length, '条数据')
  } catch (error) {
    console.error('❌ 加载粉丝列表失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    followers.value = []
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

  try {
    if (isCurrentlyFollowing) {
      // 取消关注
      await http.delete(`/api/follows/${userId}`)
      console.log('✅ 取消关注成功:', userId)
    } else {
      // 关注
      await http.post(`/api/follows/${userId}`)
      console.log('✅ 关注成功:', userId)
    }

    // 重新加载数据
    await loadStats()
    await loadCurrentList()

  } catch (error) {
    console.error('❌ 关注操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  }
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

// 监听
watch(() => route.params.userId, () => {
  page.value = 0
  hasMore.value = true
  loadUserInfo()
  loadStats()
  loadCurrentList()
})

// 生命周期
onMounted(() => {
  console.log('🔍 FollowPage 初始化')
  console.log('🔍 当前用户ID:', currentUserId.value)
  console.log('🔍 认证状态:', auth.isAuthed)
  console.log('🔍 登录用户:', auth.user)

  loadUserInfo()
  loadStats()
  loadCurrentList()
})
</script>

<style scoped>
.follow-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 30px;
  position: relative;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 25px;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(102, 126, 234, 0.8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}

.avatar-overlay span {
  color: white;
  font-size: 0.8rem;
  font-weight: 600;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.user-details {
  text-align: left;
}

.user-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 5px 0;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-block;
}

.user-name:hover {
  color: #667eea;
  transform: translateY(-2px);
}

.user-username {
  color: #666;
  margin: 0;
  font-size: 1rem;
}

.stats-tabs {
  margin-bottom: 40px;
}

.content-area {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.content-area::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  transition: all 0.4s ease;
  background: white;
  position: relative;
  overflow: hidden;
}

.user-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.05), transparent);
  transition: left 0.6s ease;
}

.user-card:hover::before {
  left: 100%;
}

.user-card:hover {
  border-color: #667eea;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.user-main {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-main:hover {
  transform: translateX(5px);
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 6px 0;
  transition: all 0.3s ease;
}

.user-card .user-name:hover {
  color: #667eea;
}

.user-bio {
  color: #666;
  font-size: 0.95rem;
  margin: 0 0 6px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.follow-date {
  color: #999;
  font-size: 0.85rem;
  margin: 0;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #888;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

.empty-state h4 {
  margin: 0 0 12px 0;
  font-size: 1.5rem;
  color: #666;
  font-weight: 600;
}

.empty-state p {
  margin: 0;
  color: #999;
  font-size: 1rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
  padding-top: 25px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.4s ease;
  cursor: pointer;
  border: none;
  font-size: 14px;
  position: relative;
  overflow: hidden;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.btn:hover::before {
  left: 100%;
}

.btn.ghost {
  background: transparent;
  border: 2px solid #e1e5e9;
  color: #666;
  position: relative;
}

.btn.ghost:hover:not(:disabled) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.btn.small {
  padding: 10px 20px;
  font-size: 13px;
  border-radius: 10px;
}

.follow-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 2px solid transparent;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.follow-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  border-radius: inherit;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.follow-btn:hover::after {
  opacity: 1;
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.follow-btn.unfollow {
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e1e5e9;
}

.follow-btn.unfollow:hover {
  background: #e74c3c;
  color: white;
  border-color: #e74c3c;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(231, 76, 60, 0.3);
}

.page-info {
  color: #666;
  font-size: 1rem;
  font-weight: 600;
  padding: 10px 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.loading-spinner {
  text-align: center;
}

.loading-spinner p {
  color: #667eea;
  font-weight: 600;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .follow-page {
    padding: 16px;
  }

  .page-title {
    font-size: 2rem;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .content-area {
    padding: 25px;
  }

  .user-card {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
    text-align: center;
  }

  .user-main {
    justify-content: center;
    flex-direction: column;
    gap: 15px;
  }

  .pagination {
    flex-direction: column;
    gap: 15px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.75rem;
  }

  .user-avatar {
    width: 60px;
    height: 60px;
  }

  .content-area {
    padding: 20px;
  }

  .section-title {
    font-size: 1.25rem;
  }
}

/* 标签页样式 */
.stats-tabs {
  margin-bottom: 30px;
}

.tabs {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tab-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 20px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  color: #666;
}

.tab-btn:hover {
  background: #f8f9fa;
}

.tab-btn.active {
  background: #667eea;
  color: white;
}

.tab-count {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.tab-btn.active .tab-count {
  color: white;
}

/* 简化用户卡片悬停效果 */
.user-card:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
  transform: none; /* 移除上浮效果 */
}

.user-main:hover {
  transform: none; /* 移除平移效果 */
}

/* 简化按钮效果 */
.btn:hover {
  transform: none; /* 移除按钮上浮效果 */
}

.btn::before {
  display: none; /* 移除按钮光扫效果 */
}

/* 简化加载状态 */
.loading-overlay {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: none; /* 移除模糊效果 */
}

</style>