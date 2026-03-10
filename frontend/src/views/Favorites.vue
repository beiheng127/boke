<template>
  <div class="favorites-container" >

    <div class="container">
      <!-- 页面标题 -->
      <div class="header-section">
        <div class="welcome-section">
          <h1 class="welcome-title">我的收藏</h1>
          <p class="welcome-subtitle">珍藏的优质内容都在这里</p>
        </div>
      </div>

      <!-- 统计信息 -->
      <div v-if="list.length > 0" class="stats-bar">
        <div class="stat-item">
          <span class="stat-number">{{ list.length }}</span>
          <span class="stat-label">篇收藏</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ totalPages }}</span>
          <span class="stat-label">页内容</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">⭐</span>
          <span class="stat-label">个人珍藏</span>
        </div>
      </div>

      <div class="divider"></div>

      <!-- 空状态 -->
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <div class="empty-illustration">
          <div class="empty-icon">⭐</div>
        </div>
        <h3>暂无收藏</h3>
        <p class="empty-hint">您还没有收藏任何文章，快去发现精彩内容吧</p>
        <div class="empty-actions">
          <router-link to="/" class="btn primary">
            🔍 探索文章
          </router-link>
        </div>
      </div>

      <!-- 收藏列表 -->
      <div v-else-if="list.length > 0" class="favorites-grid">
        <div v-for="f in list" :key="f.articleId" class="favorite-card">
          <div class="card-header">
            <router-link :to="`/article/${f.articleId}`" class="card-title-link">
              <h3 class="card-title">{{ f.title }}</h3>
            </router-link>
            <div class="card-badge">收藏</div>
          </div>

          <p class="card-summary">{{ f.summary || '这篇文章还没有摘要...' }}</p>

          <div class="card-content">
            <div class="article-stats">
              <span class="stat">
                <span class="stat-icon">👁️</span>
                {{ f.viewCount || 0 }}
              </span>
              <span class="stat">
                <span class="stat-icon">👍</span>
                {{ f.likeCount || 0 }}
              </span>
              <span class="stat">
                <span class="stat-icon">⭐</span>
                {{ f.favCount || 0 }}
              </span>
              <span class="date">{{ formatDate(f.createdAt) }}</span>
            </div>

            <div class="card-cover" v-if="f.coverImageUrl">
              <img :src="f.coverImageUrl" alt="文章封面" class="cover-image" />
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="list.length > 0 && totalPages > 1" class="pagination">
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

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>正在加载收藏列表...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../store/auth'
import http from '../api/http'

const auth = useAuthStore()
const list = ref([])
const page = ref(0)
const size = ref(9)
const totalPages = ref(0)
const loading = ref(false)
const hasMore = ref(false)


function formatDate(dateString) {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/api/interact/favorites', {
      params: { page: page.value, size: size.value }
    })
    list.value = data.content
    hasMore.value = !data.last
    totalPages.value = data.totalPages
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    list.value = []
  } finally {
    loading.value = false
  }
}

function prevPage() {
  if (page.value > 0) {
    page.value--
    load()
  }
}

function nextPage() {
  if (hasMore.value) {
    page.value++
    load()
  }
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.favorites-container {
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

.stat-icon {
  font-size: 1.2rem;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e1e5e9, transparent);
  margin: 20px 0;
}

/* 收藏网格 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

/* 收藏卡片 */
.favorite-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: #e1e5e9;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title-link {
  text-decoration: none;
  flex: 1;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.card-title:hover {
  color: #3498db;
}

.card-badge {
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
  margin-left: 12px;
}

.card-summary {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

.stat-icon {
  font-size: 0.9rem;
}

.date {
  margin-left: auto;
  font-size: 0.8rem;
  color: #999;
  font-weight: 500;
}

.card-cover {
  flex-shrink: 0;
}

.cover-image {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.btn.ghost:hover:not(.disabled) {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
  transform: translateY(-1px);
}

.btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .welcome-title {
    font-size: 2rem;
  }

  .favorites-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stats-bar {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .card-content {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .article-stats {
    order: 2;
  }

  .card-cover {
    order: 1;
    align-self: center;
  }

  .cover-image {
    width: 100%;
    max-width: 200px;
    height: 120px;
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

  .favorite-card {
    padding: 20px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .card-badge {
    margin-left: 0;
    align-self: flex-start;
  }

  .article-stats {
    flex-wrap: wrap;
    gap: 8px;
  }

  .date {
    margin-left: 0;
    width: 100%;
    text-align: right;
  }
}
</style>