<template>
  <div class="user-home-container">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="header-background"></div>
      <div class="user-info-card">
        <div class="avatar-section">
          <img
              :src="userInfo.avatarUrl || '/uploads/images/default/touxiang.jpg'"
              :alt="userInfo.displayName"
              class="user-avatar-large"
          />

        </div>

        <div class="user-details">
          <div class="user-name-section">
            <h1 class="user-display-name">{{ userInfo.displayName || userInfo.username }}</h1>
            <div class="user-badges">
              <span class="badge role" :class="userInfo.role?.toLowerCase()">
                {{ userInfo.role === 'BLOGGER' ? '博主' : '读者' }}
              </span>
              <span class="badge follower" @click="goToFollowPage('followers')">
                👥 {{ userStats.followersCount || 0 }} 粉丝
              </span>
              <span class="badge following" @click="goToFollowPage('following')">
                👤 {{ userStats.followingCount || 0 }} 关注
              </span>
            </div>
          </div>

          <p class="user-bio" v-if="userInfo.signature">
            {{ userInfo.signature }}
          </p>
          <p class="user-bio placeholder" v-else>
            这个人很懒，还没有写个性签名...
          </p>

          <div class="user-stats" v-if="isBlogger">
            <div class="stat-item">
              <span class="stat-number">{{ userStats.articleCount || 0 }}</span>
              <span class="stat-label">文章</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userStats.likeCount || 0 }}</span>
              <span class="stat-label">获赞</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userStats.viewCount || 0 }}</span>
              <span class="stat-label">阅读</span>
            </div>
          </div>

          <div class="user-actions">
            <!-- 关注按钮 -->
            <button
                v-if="!isOwnProfile && auth.isAuthed"
                class="btn primary follow-btn"
                :class="{ 'unfollow': isFollowing }"
                @click="toggleFollow"
                :disabled="followLoading"
            >
              <span class="btn-icon" v-if="!followLoading">
                {{ isFollowing ? '✅' : '👤' }}
              </span>
              {{ followLoading ? '处理中...' : (isFollowing ? '已关注' : '关注') }}
            </button>

            <router-link v-if="isOwnProfile && isBlogger" to="/editor" class="btn primary">
              <span class="btn-icon">✏️</span>
              写新文章
            </router-link>

            <router-link
                v-if="!isOwnProfile && auth.isAuthed"
                :to="`/messages?userId=${userInfo.id}`"
                class="btn ghost"
            >
              <span class="btn-icon">💬</span>
              私信
            </router-link>

            <router-link v-if="!auth.isAuthed" to="/login" class="btn primary">
              <span class="btn-icon">🔐</span>
              登录后操作
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 博主文章区域 -->
    <div v-if="isBlogger" class="articles-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">📝</span>
          {{ isOwnProfile ? '我的文章' : `${userInfo.displayName || userInfo.username}的文章` }}
        </h2>
        <div class="section-actions" v-if="isOwnProfile && isBlogger">
          <router-link to="/editor" class="btn primary">
            <span class="btn-icon">✏️</span>
            写新文章
          </router-link>
        </div>
      </div>

      <!-- 文章列表 -->
      <div v-if="articles.length > 0" class="articles-grid">
        <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :a="article"
            :show-author="false"
        />
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-articles">
        <div class="empty-illustration">
          <div class="empty-icon">📚</div>
        </div>
        <h3>{{ isOwnProfile ? '还没有发布文章' : '该用户还没有发布文章' }}</h3>
        <p v-if="isOwnProfile" class="empty-hint">
          开始您的创作之旅，分享知识和见解
        </p>
        <div v-if="isOwnProfile" class="empty-actions">
          <router-link to="/editor" class="btn primary">
            ✏️ 开始写作
          </router-link>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="articles.length > 0 && totalPages > 1" class="pagination">
        <button
            class="btn ghost pagination-btn"
            :disabled="currentPage <= 1"
            @click="prevPage"
        >
          <span class="pagination-icon">←</span>
          上一页
        </button>

        <div class="page-info">
          <span class="current-page">第 {{ currentPage }} 页</span>
          <span class="page-divider">/</span>
          <span class="total-pages">共 {{ totalPages }} 页</span>
        </div>

        <button
            class="btn ghost pagination-btn"
            :disabled="currentPage >= totalPages"
            @click="nextPage"
        >
          下一页
          <span class="pagination-icon">→</span>
        </button>
      </div>
    </div>

    <!-- 普通用户提示 -->
    <div v-else-if="isOwnProfile" class="reader-section">
      <div class="reader-info">
        <div class="info-icon">👀</div>
        <h3>读者模式</h3>
        <p>您当前是读者身份，可以浏览和收藏文章</p>
        <div class="reader-actions">
          <router-link to="/" class="btn primary">
            🔍 探索文章
          </router-link>
          <router-link to="/favorites" class="btn ghost">
            ⭐ 我的收藏
          </router-link>
        </div>
      </div>
    </div>

    <!-- 查看他人主页且不是博主时的提示 -->
    <div v-else-if="!isBlogger" class="reader-section">
      <div class="reader-info">
        <div class="info-icon">👤</div>
        <h3>读者主页</h3>
        <p>这是一个读者用户的主页</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, computed, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useAuthStore} from '../store/auth'
import http from '../api/http'
import ArticleCard from '../components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 当前查看的用户ID
const currentUserId = computed(() => {
  return route.params.userId ? parseInt(route.params.userId) : auth.user?.id
})

// 用户信息
const userInfo = ref({
  id: '',
  username: '',
  displayName: '',
  signature: '',
  avatarUrl: '',
  role: ''
})

// 用户统计 - 初始化为null，等待API加载
const userStats = ref({
  articleCount: null,
  likeCount: null,
  viewCount: null,
  followersCount: null,
  followingCount: null
})

// 文章数据
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(9)
const totalPages = ref(0)
const loading = ref(false)

// 关注相关状态 - 初始化为null，等待API检查
const isFollowing = ref(null)
const followLoading = ref(false)

// 计算属性
const isBlogger = computed(() => userInfo.value.role === 'BLOGGER' || userInfo.value.role === 'ROLE_BLOGGER')
const isOwnProfile = computed(() => {
  return currentUserId.value === auth.user?.id
})

function goToFollowPage(tab) {
  if (currentUserId.value) {
    router.push(`/user/${currentUserId.value}/follow?tab=${tab}`)
  }
}

// 检查关注状态
async function checkFollowStatus() {
  // 如果是自己的主页或者未登录，不需要检查关注状态
  if (isOwnProfile.value || !auth.isAuthed) {
    isFollowing.value = false
    return
  }

  try {
    console.log('🔍 检查关注状态，目标用户ID:', currentUserId.value)
    const { data } = await http.get(`/api/follows/${currentUserId.value}/is-following`)
    isFollowing.value = data.isFollowing
    console.log('✅ 关注状态检查结果:', data.isFollowing)
  } catch (error) {
    console.error('❌ 检查关注状态失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    // 403 错误只是权限不足，设置为未关注状态
    isFollowing.value = false
  }
}
// 关注/取消关注
async function toggleFollow() {
  if (isOwnProfile.value) {
    alert('不要关注自己啊')
    return
  }

  if (!auth.isAuthed) {
    alert('请先登录')
    router.push('/login')
    return
  }

  followLoading.value = true
  try {
    if (isFollowing.value) {
      // 取消关注
      await http.delete(`/api/follows/${currentUserId.value}`)
      isFollowing.value = false
      // 更新本地粉丝数
      if (userStats.value.followersCount !== null) {
        userStats.value.followersCount = Math.max(0, userStats.value.followersCount - 1)
      }
      console.log('✅ 取消关注成功')
    } else {
      // 关注
      await http.post(`/api/follows/${currentUserId.value}`)
      isFollowing.value = true
      // 更新本地粉丝数
      if (userStats.value.followersCount !== null) {
        userStats.value.followersCount = userStats.value.followersCount + 1
      } else {
        userStats.value.followersCount = 1
      }
      console.log('✅ 关注成功')
    }
  } catch (error) {
    console.error('❌ 关注操作失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  } finally {
    followLoading.value = false
  }
}

// 加载用户信息
async function loadUserInfo() {
  try {
    if (!currentUserId.value) {
      console.error('用户ID不存在')
      return
    }

    const {data} = await http.get(`/api/users/${currentUserId.value}`)
    userInfo.value = data
    console.log('加载用户信息成功:', data)

    await Promise.all([
      loadUserStats(),
      checkFollowStatus()
    ])
  } catch (error) {
    console.error('❌ 加载用户信息失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    // 如果用户不存在，跳转到首页
    if (error.response?.status === 404) {
      router.push('/')
    }
  }
}

// 加载用户统计
async function loadUserStats() {
  try {
    // 并行加载文章统计和关注统计
    const [statsResponse, followStatsResponse] = await Promise.all([
      http.get(`/api/users/${currentUserId.value}/stats`).catch(err => {
        console.error('加载文章统计失败:', err)
        return { data: { articleCount: 0, likeCount: 0, viewCount: 0 } }
      }),
      http.get(`/api/follows/${currentUserId.value}/stats`).catch(err => {
        console.error('加载关注统计失败:', err)
        // 403 错误只是权限不足，不执行登出，使用默认值
        return { data: { followersCount: 0, followingCount: 0 } }
      })
    ])

    userStats.value = {
      articleCount: statsResponse.data.articleCount || 0,
      likeCount: statsResponse.data.likeCount || 0,
      viewCount: statsResponse.data.viewCount || 0,
      followersCount: followStatsResponse.data.followersCount || 0,
      followingCount: followStatsResponse.data.followingCount || 0
    }

    console.log('✅ 用户统计加载成功:', userStats.value)
  } catch (error) {
    console.error('加载用户统计失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    userStats.value = {
      articleCount: 0,
      likeCount: 0,
      viewCount: 0,
      followersCount: 0,
      followingCount: 0
    }
  }
}

// 加载文章列表
async function loadArticles() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      authorId: currentUserId.value
    }

    console.log('🔍 加载文章参数:', params)
    const {data} = await http.get('/api/articles', {params})
    console.log('🔍 文章API响应:', data)

    articles.value = data.content
    totalPages.value = data.totalPages
    console.log('✅ 加载文章列表成功:', data.content.length, '篇')
  } catch (error) {
    console.error('❌ 加载文章失败:', error)
    console.error('❌ 错误详情:', error.response?.data)
    articles.value = []
  } finally {
    loading.value = false
  }
}

// 分页功能
function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--
    loadArticles()
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadArticles()
  }
}

// 监听路由参数变化
watch(() => route.params.userId, () => {
  currentPage.value = 1
  // 重置状态
  isFollowing.value = null
  userStats.value = {
    articleCount: null,
    likeCount: null,
    viewCount: null,
    followersCount: null,
    followingCount: null
  }
  loadUserInfo()
  loadArticles()
})

onMounted(() => {
  console.log('UserHome mounted, currentUserId:', currentUserId.value)
  console.log('Auth user:', auth.user)
  loadUserInfo()
  loadArticles()
})
</script>

<style scoped>
/* 原有样式保持不变，只添加关注按钮相关样式 */
.user-home-container {
  max-width: 1200px;
  margin: -100px auto;
  padding: 0 20px;
}

.user-header {
  position: relative;
  margin-bottom: 40px;
}

.header-background {
  position: absolute;
  top: 0;
  left: -20px;
  right: -20px;
  height: 200px;
  border-radius: 0 0 20px 20px;
  overflow: hidden;
}

.user-info-card {
  position: relative;
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-top: 100px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.avatar-section {
  text-align: center;
  flex-shrink: 0;
}

.user-avatar-large {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.avatar-actions {
  margin-top: 12px;
}

.user-details {
  flex: 1;
}

.user-name-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.user-display-name {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.user-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.badge.role {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: default;
}

.badge.follower, .badge.following {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #e1e5e9;
}

.badge.follower:hover, .badge.following:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
  transform: translateY(-1px);
}

.user-bio {
  font-size: 1.1rem;
  color: #666;
  line-height: 1.6;
  margin: 0 0 24px 0;
}

.user-bio.placeholder {
  color: #999;
  font-style: italic;
}

.user-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #666;
  margin-top: 4px;
}

.user-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 关注按钮样式 */
.follow-btn {
  background: #667eea;
  color: white;
  border: 1px solid #667eea;
  transition: all 0.3s ease;
}

.follow-btn:hover:not(:disabled) {
  background: #5a6fd8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
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

.follow-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.articles-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 1.25rem;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.empty-articles {
  text-align: center;
  padding: 60px 20px;
  color: #888;
}

.empty-illustration {
  margin-bottom: 24px;
}

.empty-icon {
  font-size: 80px;
  opacity: 0.5;
  margin-bottom: 16px;
}

.empty-articles h3 {
  margin: 0 0 16px 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #666;
}

.empty-hint {
  margin: 0 0 24px 0;
  font-size: 16px;
  color: #999;
  line-height: 1.6;
}

.empty-actions {
  margin-top: 24px;
}

.reader-section {
  background: white;
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.reader-info {
  max-width: 400px;
  margin: 0 auto;
}

.info-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.7;
}

.reader-info h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 12px 0;
}

.reader-info p {
  color: #666;
  line-height: 1.6;
  margin: 0 0 30px 0;
}

.reader-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #f0f0f0;
}

.pagination-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.pagination-btn:not(:disabled):hover {
  background: #3498db;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.pagination-icon {
  font-size: 16px;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  padding: 0 20px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 8px 16px;
}

.current-page {
  font-weight: 600;
  color: #3498db;
}

.page-divider {
  color: #ddd;
}

.total-pages {
  color: #999;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn.ghost {
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #e1e5e9;
  color: #666;
}

.btn.ghost:hover {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
  transform: translateY(-1px);
}

.btn.small {
  padding: 6px 12px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-home-container {
    padding: 0 16px;
  }

  .user-info-card {
    flex-direction: column;
    text-align: center;
    padding: 20px;
    margin-top: 80px;
  }

  .user-name-section {
    justify-content: center;
  }

  .user-stats {
    justify-content: center;
    gap: 20px;
  }

  .user-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .articles-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .reader-actions {
    flex-direction: column;
  }

  .pagination {
    flex-direction: column;
    gap: 16px;
  }

  .page-info {
    order: -1;
  }
}

@media (max-width: 480px) {
  .user-display-name {
    font-size: 1.5rem;
  }

  .user-avatar-large {
    width: 100px;
    height: 100px;
  }

  .user-stats {
    gap: 16px;
  }

  .stat-number {
    font-size: 1.25rem;
  }

  .articles-section {
    padding: 20px;
  }
}
</style>