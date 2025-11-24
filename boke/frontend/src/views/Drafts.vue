<!-- 优化后的 Drafts.vue -->
<template>
  <div class="container">
    <div class="header-section">
      <div class="title-section">
        <h1 class="page-title">📝 草稿箱</h1>
        <p class="page-subtitle">管理您的未发布文章</p>
      </div>

      <div class="action-bar">
        <router-link to="/editor" class="btn primary new-draft-btn">
          <span class="btn-icon">✏️</span>
          写新文章
        </router-link>

        <router-link to="/" class="btn ghost">
          <span class="btn-icon">📰</span>
          返回首页
        </router-link>
      </div>
    </div>

    <div class="stats-card" v-if="drafts.length > 0">
      <div class="stat-item">
        <div class="stat-icon">📄</div>
        <div class="stat-content">
          <div class="stat-number">{{ drafts.length }}</div>
          <div class="stat-label">篇草稿</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <div class="stat-number">{{ latestUpdate }}</div>
          <div class="stat-label">最近更新</div>
        </div>
      </div>
    </div>

    <div class="divider"></div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>正在加载草稿...</p>
    </div>

    <!-- 草稿列表 -->
    <div v-else-if="drafts.length > 0" class="drafts-container">
      <div class="drafts-grid">
        <div
            v-for="draft in drafts"
            :key="draft.id"
            class="draft-card"
            :class="{ 'has-cover': draft.coverImageUrl }"
        >
          <div class="draft-cover" v-if="draft.coverImageUrl">
            <img :src="draft.coverImageUrl" :alt="draft.title" />
            <div class="cover-overlay"></div>
            <div class="draft-badge">草稿</div>
          </div>

          <div class="draft-content">
            <h3 class="draft-title">{{ draft.title || '无标题文章' }}</h3>

            <p class="draft-summary">{{ draft.summary || '暂无摘要' }}</p>

            <div class="draft-meta">
              <div class="meta-item">
                <span class="meta-icon">📅</span>
                <span class="meta-text">{{ formatDate(draft.createdAt) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-icon">🔄</span>
                <span class="meta-text">{{ formatDate(draft.updatedAt) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-icon">📝</span>
                <span class="meta-text">{{ getWordCount(draft.content) }}</span>
              </div>
            </div>

            <div class="draft-preview">
              {{ getPreview(draft.content) }}
            </div>

            <div class="draft-actions">
              <button class="btn primary publish-btn" @click="publishDraft(draft.id)">
                <span class="btn-icon">📢</span>
                立即发布
              </button>
              <router-link :to="`/editor/${draft.id}`" class="btn ghost edit-btn">
                <span class="btn-icon">✏️</span>
                继续编辑
              </router-link>
              <button class="btn danger delete-btn" @click="deleteDraft(draft.id)">
                <span class="btn-icon">🗑️</span>
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-illustration">
        <div class="empty-icon">📄</div>
      </div>
      <h3>暂无草稿</h3>
      <p class="empty-hint">
        您还没有保存任何草稿，开始创作您的第一篇文章吧！
      </p>
      <div class="empty-actions">
        <router-link to="/editor" class="btn primary">
          <span class="btn-icon">✏️</span>
          开始写作
        </router-link>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="!loading && drafts.length > 0 && totalPages > 1" class="pagination">
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
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import http from '../api/http'

const router = useRouter()
const auth = useAuthStore()

const drafts = ref([])
const page = ref(0)
const size = ref(8)
const totalPages = ref(0)
const loading = ref(false)

// 计算最近更新时间
const latestUpdate = computed(() => {
  if (drafts.value.length === 0) return '暂无'
  const latest = drafts.value.reduce((latest, draft) => {
    return new Date(draft.updatedAt) > new Date(latest) ? draft.updatedAt : latest
  }, drafts.value[0].updatedAt)
  return formatDate(latest)
})

// 加载草稿列表
async function loadDrafts() {
  loading.value = true
  try {
    const { data } = await http.get('/api/articles/drafts', {
      params: {
        page: page.value,
        size: size.value
      }
    })
    drafts.value = data.content
    totalPages.value = data.totalPages
    console.log('加载草稿成功:', drafts.value.length)
  } catch (error) {
    console.error('加载草稿失败:', error)
    drafts.value = []
    totalPages.value = 0
  } finally {
    loading.value = false
  }
}

// 发布草稿
async function publishDraft(draftId) {
  try {
    const { data } = await http.post(`/api/articles/${draftId}/toggle-publish`)
    alert(data.message)
    // 从列表中移除已发布的草稿
    drafts.value = drafts.value.filter(draft => draft.id !== draftId)
  } catch (error) {
    console.error('发布草稿失败:', error)
    alert('发布失败: ' + (error.response?.data?.message || error.message))
  }
}

// 删除草稿
async function deleteDraft(draftId) {
  if (!confirm('确定要删除这个草稿吗？此操作不可逆。')) {
    return
  }

  try {
    await http.delete(`/api/articles/${draftId}`)
    drafts.value = drafts.value.filter(draft => draft.id !== draftId)
    alert('草稿删除成功')
  } catch (error) {
    console.error('删除草稿失败:', error)
    alert('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

// 格式化日期
function formatDate(dateString) {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now - date)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 1) return '今天'
  if (diffDays === 2) return '昨天'
  if (diffDays <= 7) return `${diffDays - 1}天前`

  return date.toLocaleDateString('zh-CN')
}

// 获取字数
function getWordCount(content) {
  if (!content) return '0字'
  const words = content.replace(/\s/g, '').length
  return words > 1000 ? `${(words / 1000).toFixed(1)}k字` : `${words}字`
}

// 获取内容预览
function getPreview(content) {
  if (!content) return '暂无内容'
  const cleanContent = content.replace(/#{1,6}\s?/g, '').replace(/\*\*/g, '')
  return cleanContent.length > 120 ? cleanContent.substring(0, 120) + '...' : cleanContent
}

// 分页功能
function prevPage() {
  if (page.value > 0) {
    page.value--
    loadDrafts()
  }
}

function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++
    loadDrafts()
  }
}

onMounted(() => {
  // 权限检查
  if (!auth.isAuthed) {
    alert('请先登录')
    router.replace('/login')
    return
  }

  if (!auth.isBlogger) {
    alert('只有博主可以访问草稿箱')
    router.replace('/')
    return
  }

  loadDrafts()
})
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.title-section {
  flex: 1;
}

.page-title {
  font-size: 2.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.action-bar {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.new-draft-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.new-draft-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 统计卡片 */
.stats-card {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  font-size: 2rem;
  opacity: 0.8;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  margin-top: 4px;
}

/* 草稿容器 */
.drafts-container {
  margin-bottom: 40px;
}

.drafts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.draft-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.draft-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.draft-card.has-cover .draft-content {
  padding-top: 0;
}

.draft-cover {
  position: relative;
  height: 300px;
  overflow: hidden;
}

.draft-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.1));
}

.draft-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(108, 117, 125, 0.9);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.draft-content {
  padding: 24px;
}

.draft-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.draft-summary {
  color: #666;
  font-size: 0.95rem;
  margin: 0 0 16px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.draft-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #666;
}

.meta-icon {
  font-size: 0.9rem;
  width: 16px;
  text-align: center;
}

.draft-preview {
  color: #888;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 20px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
  border-left: 3px solid #3498db;
}

.draft-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.publish-btn {
  background: #28a745;
  border: none;
  color: white;
  flex: 1;
  min-width: 120px;
}

.publish-btn:hover {
  background: #218838;
}

.edit-btn {
  flex: 1;
  min-width: 100px;
}

.delete-btn {
  background: #dc3545;
  border: none;
  color: white;
  min-width: 80px;
}

.delete-btn:hover {
  background: #c82333;
}

/* 其他样式与首页保持一致 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e1e5e9, transparent);
  margin: 20px 0;
}

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

/* 响应式设计 */
@media (max-width: 1024px) {
  .drafts-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .header-section {
    flex-direction: column;
    gap: 20px;
  }

  .page-title {
    font-size: 1.75rem;
  }

  .action-bar {
    width: 100%;
    justify-content: stretch;
  }

  .action-bar .btn {
    flex: 1;
    justify-content: center;
  }

  .stats-card {
    grid-template-columns: 1fr;
  }

  .drafts-grid {
    grid-template-columns: 1fr;
  }

  .draft-actions {
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
  .draft-content {
    padding: 20px;
  }

  .draft-meta {
    padding: 12px;
  }
}
</style>