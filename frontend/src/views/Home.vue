<template>
  <div class="home-container">
    <div class="container">
      <!-- 页面标题和搜索栏 -->
      <div class="header-section">
        <div class="welcome-section">
          <h1 class="welcome-title">探索精彩内容</h1>
          <p class="welcome-subtitle">发现优质文章，分享知识见解</p>
        </div>

        <div class="action-bar">
          <!-- 左侧按钮组 -->
          <div class="left-actions">
            <!-- 发布文章按钮 - 仅博主可见 -->
            <router-link
                v-if="auth.isBlogger && auth.isAuthed"
                to="/editor"
                class="btn primary publish-btn"
                style="text-decoration: none;"
                prefetch
            >
              <span class="btn-icon">✏️</span>
              发布文章
            </router-link>

            <!-- 更换背景按钮 -->
            <button
                v-if="auth.isAuthed"
                class="btn ghost background-btn"
                @click="triggerBackgroundUpload"
            >
              <span class="btn-icon">🎨</span>
              更换背景
            </button>
            <input
                type="file"
                ref="backgroundFileInput"
                style="display: none"
                accept="image/*"
                @change="uploadBackground"
            >
          </div>

          <!-- 搜索框和排序 -->
          <div class="search-sort-container">
            <div class="search-box">
              <div class="search-icon-wrapper">
                🔍
              </div>
              <input
                  class="search-input"
                  v-model="searchKeyword"
                  placeholder="搜索文章标题或摘要..."
                  @keyup.enter="handleSearch"
              />
              <button class="btn primary search-btn" @click="handleSearch">
                搜索
              </button>
            </div>

            <!-- 排序选项 -->
            <div class="sort-options">
              <label class="sort-label">排序:</label>
              <select v-model="sortBy" @change="handleSortChange" class="sort-select">
                <option value="hot">🔥 热度排序</option>
                <option value="time">🕐 最新发布</option>
                <option value="oldest">📜 最旧发布</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计信息 -->
      <div v-if="!loading && list.length > 0" class="stats-bar">
        <div class="stat-item">
          <span class="stat-number">{{ list.length }}</span>
          <span class="stat-label">篇文章</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ totalPages }}</span>
          <span class="stat-label">页内容</span>
        </div>
      </div>

      <div class="divider"></div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>正在加载文章...</p>
      </div>

      <!-- 文章列表 - 改为每行三篇 -->
      <div v-else-if="list.length > 0" class="article-grid-three">
        <ArticleCard
            v-for="a in list"
            :key="a.id"
            :a="a"
        />
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-illustration">
          <div class="empty-icon">📚</div>
        </div>
        <h3>{{ searchKeyword ? '没有找到相关文章' : '暂无文章' }}</h3>
        <p v-if="!searchKeyword && auth.isBlogger" class="empty-hint">
          作为博主，您可以 <router-link to="/editor" class="link">发布第一篇文章</router-link>
        </p>
        <p v-else-if="searchKeyword" class="empty-hint">
          尝试调整搜索关键词或 <a href="javascript:void(0)" @click="clearSearch" class="link">查看所有文章</a>
        </p>
        <div v-else class="empty-actions">
          <router-link to="/editor" class="btn primary" v-if="auth.isBlogger">
            ✏️ 开始写作
          </router-link>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="!loading && list.length > 0 && totalPages > 1" class="pagination">
        <button
            class="btn ghost pagination-btn"
            :disabled="page <= 0"
            @click="prevPage"
            :class="{ disabled: page <= 0 }"
        >
          <span class="pagination-icon">←</span>
          上一页
        </button>

        <div class="page-info">
          <span class="current-page">第 {{ page + 1 }} 页</span>
          <span class="page-divider">/</span>
          <span class="total-pages">共 {{ totalPages }} 页</span>
        </div>

        <button
            class="btn ghost pagination-btn"
            :disabled="page >= totalPages - 1"
            @click="nextPage"
            :class="{ disabled: page >= totalPages - 1 }"
        >
          下一页
          <span class="pagination-icon">→</span>
        </button>
      </div>
    </div>

    <!-- 背景上传加载状态 -->
    <div v-if="backgroundUploading" class="upload-overlay">
      <div class="upload-content">
        <div class="upload-spinner"></div>
        <p>正在上传背景图片...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import http from '../api/http'
import ArticleCard from '../components/ArticleCard.vue'

// 路由和认证
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 文章列表数据
const list = ref([])
const page = ref(0)
const size = ref(9)
const totalPages = ref(0)
const loading = ref(false)
const searchKeyword = ref('')
const sortBy = ref('hot')
const backgroundUploading = ref(false)
const backgroundFileInput = ref(null)

// 加载控制变量
let loadTimeout = null
let isInitialLoad = true
let hasLoaded = false

// 防抖加载函数
function scheduleLoad(immediate = false) {
  // 清除之前的定时器
  if (loadTimeout) {
    clearTimeout(loadTimeout)
    loadTimeout = null
  }

  if (immediate) {
    // 立即执行
    performLoad()
  } else {
    // 延迟执行，防抖
    loadTimeout = setTimeout(() => {
      performLoad()
    }, 50)
  }
}

// 实际执行加载的函数
async function performLoad() {
  // 防止重复加载
  if (loading.value) {
    console.log('⏸️ 跳过重复加载')
    return
  }

  loading.value = true
  try {
    console.log('🔍 执行文章加载，参数:', {
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value,
      sortBy: sortBy.value
    })

    const { data } = await http.get('/api/articles', {
      params: {
        page: page.value,
        size: size.value,
        keyword: searchKeyword.value,
        sortBy: sortBy.value
      }
    })

    list.value = data.content
    totalPages.value = data.totalPages
    hasLoaded = true

    console.log('✅ 加载文章成功:', {
      文章数量: list.value.length,
      总页数: totalPages.value,
      排序方式: sortBy.value
    })

  } catch (error) {
    console.error('❌ 加载文章失败:', error)
    list.value = []
    totalPages.value = 0
  } finally {
    loading.value = false
    loadTimeout = null
  }
}

// 触发背景图片上传
function triggerBackgroundUpload() {
  backgroundFileInput.value.click()
}

// 上传背景图片
async function uploadBackground(event) {
  const file = event.target.files?.[0]
  if (!file) return

  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }

  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  backgroundUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)

    const { data } = await http.post('/api/users/home-background', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    auth.user.homeBackgroundUrl = data.homeBackgroundUrl
    localStorage.setItem('user', JSON.stringify(auth.user))
    alert('背景图片上传成功！')
  } catch (error) {
    console.error('背景图片上传失败:', error)
    alert('背景图片上传失败: ' + (error.response?.data?.message || error.message))
  } finally {
    backgroundUploading.value = false
    event.target.value = ''
  }
}

// 搜索处理
function handleSearch() {
  console.log('🔍 执行搜索:', searchKeyword.value)
  page.value = 0 // 重置到第一页
  router.push({
    path: '/',
    query: {
      keyword: searchKeyword.value || undefined,
      sortBy: sortBy.value
    }
  })
}

// 排序处理
function handleSortChange() {
  console.log('🔄 排序变更:', sortBy.value)
  page.value = 0 // 重置到第一页
  router.push({
    path: '/',
    query: {
      keyword: searchKeyword.value || undefined,
      sortBy: sortBy.value
    }
  })
}

// 清空搜索
function clearSearch() {
  console.log('🗑️ 清空搜索')
  searchKeyword.value = ''
  page.value = 0
  router.push({ path: '/', query: { sortBy: sortBy.value } })
}

// 分页功能
function prevPage() {
  if (page.value > 0) {
    page.value--
    console.log('⬅️ 上一页:', page.value)
    scheduleLoad(true) // 立即加载
  }
}

function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++
    console.log('➡️ 下一页:', page.value)
    scheduleLoad(true) // 立即加载
  }
}

// 监听路由参数变化 - 简化逻辑
watch(() => route.query, (newQuery) => {
  console.log('🔄 路由查询参数变化:', newQuery)

  // 更新本地状态
  if (newQuery.keyword !== undefined) {
    searchKeyword.value = newQuery.keyword
  }
  if (newQuery.sortBy) {
    sortBy.value = newQuery.sortBy
  }

  // 重置页码并调度加载
  page.value = 0
  scheduleLoad()
}, { deep: true })

// 页面加载时初始化
onMounted(async () => {
  console.log('🚀 主页初始化开始')

  // 防止重复初始化
  if (hasLoaded) {
    console.log('⏭️ 已加载过，跳过初始化')
    return
  }

  console.log('认证状态:', auth.isAuthed)
  console.log('用户角色:', auth.user?.role)
  console.log('是否为博主:', auth.isBlogger)

  // 如果认证状态有问题，强制重新登录
  if (auth.isAuthed && (!auth.user || !auth.user.role)) {
    console.log('❌ 认证状态异常，强制登出')
    auth.logout()
    router.replace('/login')
    return
  }

  // 等待下一个tick确保DOM就绪
  await nextTick()

  // 初始化搜索关键词和排序方式
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }
  if (route.query.sortBy) {
    sortBy.value = route.query.sortBy
  }

  console.log('📝 初始化参数:', {
    keyword: searchKeyword.value,
    sortBy: sortBy.value,
    page: page.value
  })

  // 使用 setTimeout 确保只执行一次加载
  setTimeout(() => {
    if (!hasLoaded) {
      scheduleLoad(true)
    }
  }, 100)

  console.log('✅ 主页初始化完成')
})

// 组件卸载时清理
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (loadTimeout) {
    clearTimeout(loadTimeout)
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  position: relative;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 1;
  min-height: 80vh;
}

/* 头部区域 */
.header-section {
  margin-bottom: 32px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.left-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.publish-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.background-btn {
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #e1e5e9;
  color: #666;
  padding: 12px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.background-btn:hover {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
  transform: translateY(-1px);
}

/* 搜索和排序容器 */
.search-sort-container {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid #e1e5e9;
}

.search-icon-wrapper {
  padding: 0 16px;
  color: #666;
}

.search-input {
  padding: 12px 0;
  border: none;
  outline: none;
  width: 300px;
  font-size: 14px;
  background: transparent;
}

.search-btn {
  padding: 12px 20px;
  border: none;
  border-radius: 0;
  background: #3498db;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: #2980b9;
}

/* 排序选项 */
.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  font-weight: 500;
}

.sort-select {
  padding: 10px 16px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  background: white;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.sort-select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.sort-select:hover {
  border-color: #3498db;
}

/* 统计信息 */
.stats-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e1e5e9, transparent);
  margin: 20px 0;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 80px 20px;
  color: #666;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 文章网格布局 - 每行三篇 */
.article-grid-three {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
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

.empty-state h3 {
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

.link {
  color: #3498db;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin-top: 60px;
  padding: 20px 0;
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

.pagination-btn:not(.disabled):hover {
  background: #3498db;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
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

.btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

/* 上传加载状态 */
.upload-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.upload-content {
  background: white;
  padding: 40px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.upload-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .article-grid-three {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .welcome-title {
    font-size: 2rem;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .left-actions {
    justify-content: center;
  }

  .search-sort-container {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .search-box {
    order: -1;
  }

  .search-input {
    width: 100%;
  }

  .sort-options {
    justify-content: center;
  }

  .article-grid-three {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stats-bar {
    flex-direction: column;
    gap: 12px;
    text-align: center;
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
  .welcome-title {
    font-size: 1.75rem;
  }

  .welcome-subtitle {
    font-size: 1rem;
  }

  .search-input {
    min-width: auto;
  }

  .sort-select {
    width: 100%;
  }

  .left-actions {
    flex-direction: column;
    width: 100%;
  }

  .left-actions .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>