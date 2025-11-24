<template>
  <div class="article-detail-container" v-if="article">
    <!-- 文章头部 -->
    <div class="article-header">
      <div class="header-content">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <div class="meta-left">
            <div class="author-info">
              <div class="author-avatar">
                <img :src="article.authorAvatar || '/uploads/images/default/touxiang.jpg'" alt="作者头像" />
              </div>
              <div class="author-details">
                <span class="author-name">{{ article.author }}</span>
                <span class="publish-time">{{ formatDate(article.createdAt) }}</span>
              </div>
            </div>
            <div v-if="article.updatedAt !== article.createdAt" class="update-info">
              <span class="update-text">更新于 {{ formatRelativeTime(article.updatedAt) }}</span>
            </div>
          </div>
          <div class="meta-right">
            <div class="article-stats">
              <!-- 浏览量 -->
              <div class="stat-item view-count">
                <span class="stat-icon">👁️</span>
                <span class="stat-number">{{ article.viewCount || 0 }}</span>
                <span class="stat-label">浏览</span>
              </div>

              <!-- 点赞按钮 -->
              <button class="stat-btn like-btn" @click="toggleLike" :class="{ active: article.liked }">
                <span class="btn-icon">👍</span>
                <span class="btn-number">{{ article.likeCount || 0 }}</span>
                <span class="btn-label">{{ article.liked ? '已赞' : '点赞' }}</span>
              </button>

              <!-- 收藏按钮 -->
              <button class="stat-btn fav-btn" @click="toggleFav" :class="{ active: article.favorited }">
                <span class="btn-icon">{{ article.favorited ? '⭐' : '☆' }}</span>
                <span class="btn-number">{{ article.favCount || 0 }}</span>
                <span class="btn-label">{{ article.favorited ? '已收藏' : '收藏' }}</span>
              </button>
            </div>

            <!-- 作者操作按钮 -->
            <div v-if="isAuthor || isBlogger" class="author-actions">
              <router-link class="action-btn edit-btn" :to="`/editor/${article.id}`">
                <span class="action-icon">✏️</span>
                编辑文章
              </router-link>
              <button class="action-btn delete-btn" @click="delArticle">
                <span class="action-icon">🗑️</span>
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 封面图片 -->
    <div v-if="article.coverImageUrl" class="cover-image-section">
      <div class="cover-container">
        <img :src="article.coverImageUrl" alt="文章封面" class="cover-image" />
      </div>
    </div>

    <!-- 文章摘要 -->
    <div v-if="article.summary" class="article-summary">
      <div class="summary-content">
        <p>{{ article.summary }}</p>
      </div>
    </div>

    <!-- 文章内容 -->
    <div class="article-content-section">
      <div class="content-container">
        <!-- Markdown 内容渲染 -->
        <div class="markdown-content" v-html="renderedContent"></div>
      </div>
    </div>

    <!-- 文章标签和分类 -->
    <div v-if="article.tags && article.tags.length > 0" class="article-tags">
      <div class="tags-container">
        <span class="tags-label">标签：</span>
        <span v-for="tag in article.tags" :key="tag" class="tag-item">
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- 文章底部操作栏 -->
    <div class="article-actions-bar">
      <div class="actions-container">
        <button class="action-btn secondary" @click="scrollToComments">
          <span class="action-icon">💬</span>
          查看评论
        </button>
        <button class="action-btn secondary" @click="shareArticle">
          <span class="action-icon">📤</span>
          分享文章
        </button>
        <button class="action-btn secondary" @click="scrollToTop">
          <span class="action-icon">⬆️</span>
          回到顶部
        </button>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comments-section" ref="commentsSection">
      <div class="comments-header">
        <h2 class="comments-title">
          <span class="title-icon">💬</span>
          评论
          <span class="comments-count">({{ comments.length }})</span>
        </h2>
        <div class="comments-stats">
          <span class="stat-text">欢迎留下您的想法</span>
        </div>
      </div>

      <!-- 评论表单 -->
      <div class="comment-form-wrapper">
        <CommentForm :onSubmit="createComment" />
      </div>

      <!-- 评论列表 -->
      <div class="comments-list-wrapper">
        <CommentList :comments="comments" :articleAuthor="article.author" @delete="deleteComment" />
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="!article" class="loading-state">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p>加载文章中...</p>
      </div>
    </div>
  </div>

  <!-- 文章不存在 -->
  <div v-else-if="article === null" class="not-found-state">
    <div class="not-found-content">
      <div class="not-found-icon">📝</div>
      <h2>文章未找到</h2>
      <p>您要查看的文章可能已被删除或不存在</p>
      <router-link to="/" class="back-home-btn">
        <span class="btn-icon">🏠</span>
        返回首页
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import CommentList from '../components/CommentList.vue'
import CommentForm from '../components/CommentForm.vue'
import { useAuthStore } from '../store/auth'

// 引入 Markdown 解析库
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const id = Number(route.params.id)
const article = ref(null)
const comments = ref([])
const commentsSection = ref(null)

// 配置 marked
marked.setOptions({
  highlight: function(code, lang) {
    // 这里可以集成代码高亮库，如 highlight.js
    return code
  },
  breaks: true,
  gfm: true
})

// 计算属性
const isAuthor = computed(() => auth.isAuthed && article.value && auth.user?.id === article.value.authorId)
const isBlogger = computed(() => {
  const userRole = auth.user?.role
  return userRole === 'BLOGGER' || userRole === 'ROLE_BLOGGER'
})

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  // 使用 DOMPurify 对 Markdown 渲染的 HTML 进行消毒
  return DOMPurify.sanitize(marked.parse(article.value.content))
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 格式化相对时间
const formatRelativeTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diffInSeconds = Math.floor((now - date) / 1000)

  if (diffInSeconds < 60) return '刚刚'
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}分钟前`
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}小时前`
  if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)}天前`

  return formatDate(dateString)
}

async function loadDetail() {
  try {
    const { data } = await http.get(`/api/articles/${id}`)
    article.value = data
    console.log('文章详情:', data)


    // 加载交互状态（点赞、收藏）
    await loadInteractionStatus()
  } catch (error) {
    console.error('加载文章详情失败:', error)
    if (error.response?.status === 404) {
      article.value = null // 设置为 null 表示文章不存在
    }
  }
}


async function loadInteractionStatus() {
  if (!auth.isAuthed) {
    // 未登录时设置默认状态
    if (article.value) {
      article.value.liked = false
      article.value.favorited = false
      // 从文章数据中获取计数
      const { data: stats } = await http.get(`/api/interact/status/${id}`)
      article.value.likeCount = stats.likeCount
      article.value.favCount = stats.favCount
    }
    return
  }
  try {
    const { data } = await http.get(`/api/interact/status/${id}`)
    // 更新交互状态和计数
    article.value.liked = data.liked
    article.value.favorited = data.favorited
    article.value.likeCount = data.likeCount
    article.value.favCount = data.favCount
  } catch (error) {
    console.error('加载交互状态失败:', error)
  }
}

async function loadComments() {
  try {
    const {data} = await http.get(`/api/comments/by-article/${id}`,
        {params: {page: 0, size: 50}})
    comments.value = data.content
  } catch (error) {
    console.error('加载评论失败:', error)
    comments.value = [] // 出错时设为空数组
  }
}

async function delArticle() {
  if (!confirm('确定要删除这篇文章吗？此操作不可逆。')) {
    return
  }
  try {
    await http.delete(`/api/articles/${id}`)
    router.replace('/')
  } catch (error) {
    console.error('删除文章失败:', error)
    alert('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

async function toggleLike() {
  if (!auth.isAuthed) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    const { data } = await http.post(`/api/interact/like/${id}`)
    // 更新本地状态
    article.value.liked = data.liked
    article.value.likeCount = data.likeCount
    article.value.favCount = data.favCount
  } catch (error) {
    console.error('点赞操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  }
}

async function toggleFav() {
  if (!auth.isAuthed) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    const { data } = await http.post(`/api/interact/fav/${id}`)
    // 更新本地状态
    article.value.favorited = data.favorited
    article.value.likeCount = data.likeCount
    article.value.favCount = data.favCount
  } catch (error) {
    console.error('收藏操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  }
}

async function createComment(content) {
  if (!auth.isAuthed) {
    alert('请先登录')
    return
  }

  try {
    await http.post('/api/comments', { articleId: id, content })
    await loadComments()
    // 滚动到最新评论
    setTimeout(() => {
      scrollToComments()
    }, 100)
  } catch (error) {
    console.error('发表评论失败:', error)
    alert('发表评论失败: ' + (error.response?.data?.message || error.message))
  }
}

async function deleteComment(c) {
  try {
    await http.delete(`/api/comments/${c.id}`)
    await loadComments()
  } catch (error) {
    console.error('删除评论失败:', error)
    alert('删除评论失败: ' + (error.response?.data?.message || error.message))
  }
}

function scrollToComments() {
  if (commentsSection.value) {
    commentsSection.value.scrollIntoView({ behavior: 'smooth' })
  }
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function shareArticle() {
  if (navigator.share) {
    navigator.share({
      title: article.value.title,
      text: article.value.summary,
      url: window.location.href
    })
  } else {
    // 降级方案：复制链接
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板！')
  }
}

onMounted(async () => {
  await loadDetail()
  await loadComments()
})
</script>

<style scoped>
.article-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
  min-height: 100vh;
}

/* 文章头部 */
.article-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  border-radius: 0 0 20px 20px;
  margin: 0 -20px 40px -20px;
  position: relative;
  overflow: hidden;
}

.article-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.header-content {
  position: relative;
  z-index: 1;
}

.article-title {
  font-size: 2.8rem;
  font-weight: 700;
  line-height: 1.2;
  margin: 0 0 30px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 20px;
}

.meta-left {
  flex: 1;
  min-width: 300px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.author-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 1.1rem;
  font-weight: 600;
}

.publish-time {
  font-size: 0.9rem;
  opacity: 0.9;
}

.update-info {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  display: inline-block;
}

.update-text {
  font-size: 0.85rem;
  opacity: 0.9;
}

.meta-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-end;
}

.article-stats {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stat-item, .stat-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  font-size: 0.9rem;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.stat-btn {
  cursor: pointer;
}

.stat-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.stat-btn.active {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

.stat-icon, .btn-icon {
  font-size: 1.1rem;
}

.stat-number, .btn-number {
  font-weight: 600;
  font-size: 1rem;
}

.stat-label, .btn-label {
  font-size: 0.85rem;
}

.author-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 25px;
  background: transparent;
  color: white;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.5);
}

.edit-btn:hover {
  background: rgba(52, 152, 219, 0.3);
}

.delete-btn:hover {
  background: rgba(231, 76, 60, 0.3);
}

.action-icon {
  font-size: 1rem;
}

/* 封面图片 */
.cover-image-section {
  margin: 0 -20px 40px -20px;
}

.cover-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.cover-image {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 文章摘要 */
.article-summary {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 30px;
  border-radius: 16px;
  margin-bottom: 40px;
  border-left: 4px solid #667eea;
}

.summary-content {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #4a5568;
  font-style: italic;
}

/* 文章内容 */
.article-content-section {
  margin-bottom: 40px;
}

.content-container {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* Markdown 内容样式 */
.markdown-content {
  line-height: 1.8;
  color: #2d3748;
  font-size: 1.05rem;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  margin: 2rem 0 1rem 0;
  color: #2c3e50;
  font-weight: 600;
  line-height: 1.3;
}

.markdown-content h1 {
  font-size: 2.2rem;
  border-bottom: 3px solid #3498db;
  padding-bottom: 0.5rem;
}

.markdown-content h2 {
  font-size: 1.8rem;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0.3rem;
}

.markdown-content h3 {
  font-size: 1.5rem;
}

.markdown-content p {
  margin-bottom: 1.5rem;
}

.markdown-content blockquote {
  border-left: 4px solid #3498db;
  padding: 1rem 1.5rem;
  margin: 1.5rem 0;
  background: #f8f9fa;
  border-radius: 0 8px 8px 0;
  font-style: italic;
  color: #4a5568;
}

.markdown-content code {
  background: #f7fafc;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
  color: #e53e3e;
  border: 1px solid #e2e8f0;
}

.markdown-content pre {
  background: #2d3748;
  color: #e2e8f0;
  padding: 1.5rem;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1.5rem 0;
  border: 1px solid #4a5568;
}

.markdown-content pre code {
  background: none;
  padding: 0;
  border: none;
  color: inherit;
  font-size: 0.9rem;
}

.markdown-content ul,
.markdown-content ol {
  margin: 1rem 0;
  padding-left: 2rem;
}

.markdown-content li {
  margin: 0.5rem 0;
}

.markdown-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 1.5rem 0;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.markdown-content th,
.markdown-content td {
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  text-align: left;
}

.markdown-content th {
  background: #f7fafc;
  font-weight: 600;
  color: #4a5568;
}

.markdown-content tr:nth-child(even) {
  background: #f8fafc;
}

.markdown-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1.5rem 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.markdown-content a {
  color: #3498db;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.markdown-content a:hover {
  color: #2980b9;
  border-bottom-color: #3498db;
}

/* 文章标签 */
.article-tags {
  margin-bottom: 30px;
}

.tags-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.tags-label {
  font-weight: 600;
  color: #4a5568;
  font-size: 0.9rem;
}

.tag-item {
  padding: 6px 12px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #4a5568;
  transition: all 0.3s ease;
}

.tag-item:hover {
  background: #3498db;
  color: white;
  border-color: #3498db;
  transform: translateY(-1px);
}

/* 文章底部操作栏 */
.article-actions-bar {
  background: white;
  padding: 20px 0;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 40px;
}

.actions-container {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.action-btn.secondary {
  padding: 12px 24px;
  background: white;
  border: 2px solid #e2e8f0;
  color: #4a5568;
  border-radius: 25px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn.secondary:hover {
  background: #3498db;
  color: white;
  border-color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

/* 评论区 */
.comments-section {
  margin-bottom: 60px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e2e8f0;
}

.comments-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  color: #2c3e50;
  font-size: 1.8rem;
}

.title-icon {
  font-size: 1.5rem;
}

.comments-count {
  color: #3498db;
  font-weight: 600;
}

.comments-stats {
  color: #718096;
  font-size: 0.9rem;
}

.comment-form-wrapper {
  margin-bottom: 40px;
}

.comments-list-wrapper {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 加载状态 */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
  padding: 40px;
}

.loading-content {
  text-align: center;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

.loading-content p {
  color: #718096;
  font-size: 1.1rem;
  margin: 0;
}

/* 文章不存在状态 */
.not-found-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 40px;
  text-align: center;
}

.not-found-content {
  max-width: 400px;
}

.not-found-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.7;
}

.not-found-content h2 {
  color: #2c3e50;
  margin: 0 0 16px 0;
  font-size: 1.8rem;
}

.not-found-content p {
  color: #718096;
  margin: 0 0 30px 0;
  font-size: 1.1rem;
  line-height: 1.6;
}

.back-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #3498db;
  color: white;
  text-decoration: none;
  border-radius: 25px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

/* 动画 */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-detail-container {
    padding: 0 16px;
  }

  .article-header {
    padding: 40px 24px;
    margin: 0 -16px 30px -16px;
  }

  .article-title {
    font-size: 2rem;
  }

  .article-meta {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
  }

  .meta-left, .meta-right {
    min-width: auto;
  }

  .meta-right {
    align-items: stretch;
  }

  .article-stats {
    justify-content: space-between;
  }

  .author-actions {
    justify-content: center;
  }

  .cover-image-section {
    margin: 0 -16px 30px -16px;
  }

  .cover-container {
    padding: 0 16px;
  }

  .content-container {
    padding: 24px;
  }

  .markdown-content h1 {
    font-size: 1.8rem;
  }

  .markdown-content h2 {
    font-size: 1.5rem;
  }

  .actions-container {
    flex-direction: column;
    align-items: stretch;
  }

  .comments-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .article-title {
    font-size: 1.6rem;
  }

  .article-stats {
    flex-direction: column;
    align-items: stretch;
  }

  .stat-item, .stat-btn {
    justify-content: center;
  }

  .author-actions {
    flex-direction: column;
  }

  .content-container {
    padding: 20px;
  }

  .markdown-content {
    font-size: 1rem;
  }
}
</style>