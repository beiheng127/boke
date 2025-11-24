<template>
  <div class="article-card" @click="goToArticle">
    <div class="card-image" v-if="a.coverImageUrl">
      <img
          :src="a.coverImageUrl"
          :alt="a.title"
          class="cover-image"
          loading="lazy"
      />
      <div class="image-overlay"></div>
      <!-- 状态标识 - 修复逻辑 -->
      <div class="status-badge" :class="{ published: a.published }">
        {{ a.published ? '✅ 已发布' : '📝 草稿' }}
      </div>
    </div>

    <div class="card-content">
      <div class="card-header">
        <h3 class="card-title">{{ a.title || '无标题文章' }}</h3>
        <div class="hot-score" v-if="a.hotScore > 0">
          🔥 {{ Math.round(a.hotScore) }}
        </div>
      </div>

      <p class="card-summary">{{ a.summary || '暂无摘要' }}</p>

      <div class="card-meta">
        <div class="author-info">
          <img
              :src="a.author?.avatarUrl || '/uploads/images/default/touxiang.jpg'"
              :alt="a.author?.displayName || a.author?.username"
              class="author-avatar"
              loading="lazy"
          />
          <span class="author-name">{{ a.author?.displayName || a.author?.username }}</span>
        </div>

        <div class="meta-divider">•</div>

        <span class="publish-date">{{ formatDate(a.createdAt) }}</span>
      </div>

      <div class="card-stats">
        <div class="stat">
          <span class="stat-icon">👁️</span>
          <span class="stat-count">{{ a.viewCount || 0 }}</span>
        </div>
        <div class="stat">
          <span class="stat-icon">👍</span>
          <span class="stat-count">{{ a.likeCount || 0 }}</span>
        </div>
        <div class="stat">
          <span class="stat-icon">💬</span>
          <span class="stat-count">{{ a.commentCount || 0 }}</span>
        </div>
        <div class="stat">
          <span class="stat-icon">⭐</span>
          <span class="stat-count">{{ a.favCount || 0 }}</span>
        </div>
      </div>

      <div class="card-actions" v-if="auth.isAuthed">
        <button
            class="action-btn like-btn"
            :class="{ active: a.liked }"
            @click.stop="toggleLike"
        >
          {{ a.liked ? '👍 已赞' : '👍 点赞' }}
        </button>
        <button
            class="action-btn fav-btn"
            :class="{ active: a.favorited }"
            @click.stop="toggleFav"
        >
          {{ a.favorited ? '⭐ 已收藏' : '☆ 收藏' }}
        </button>
      </div>

      <!-- 文章操作按钮（作者或博主可见） -->
      <div class="article-actions" v-if="(isAuthor || isBlogger) && (!a.published || isAuthor)">
        <!-- 动态发布状态按钮 - 修复逻辑 -->
        <button
            class="btn small publish-toggle-btn"
            :class="{ 'unpublish-btn': a.published, 'publish-btn': !a.published }"
            @click.stop="togglePublish"
        >
          {{ a.published ? '⏸️ 取消发布' : '📢 发布文章' }}
        </button>
        <button class="btn small edit-btn" @click.stop="editArticle">
          ✏️ 编辑
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { computed } from 'vue'
import http from '../api/http'

const props = defineProps(['a'])
const router = useRouter()
const auth = useAuthStore()

// 计算属性：判断是否为作者或博主
const isAuthor = computed(() => {
  return auth.isAuthed && props.a.authorId === auth.user?.id
})

const isBlogger = computed(() => {
  return auth.user?.role === 'BLOGGER' || auth.user?.role === 'ROLE_BLOGGER'
})

// 格式化日期
const formatDate = (dateString) => {
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

// 跳转到文章详情
const goToArticle = () => {
  // 如果是草稿且不是作者/博主，不允许查看 - 修复逻辑
  if (!props.a.published && !isAuthor.value && !isBlogger.value) {
    alert('该文章尚未发布，无法查看')
    return
  }
  router.push(`/article/${props.a.id}`)
}

// 点赞功能
const toggleLike = async () => {
  if (!auth.isAuthed) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    const { data } = await http.post(`/api/interact/like/${props.a.id}`)
    props.a.liked = data.liked
    props.a.likeCount = data.likeCount
  } catch (error) {
    console.error('点赞操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  }
}

// 收藏功能
const toggleFav = async () => {
  if (!auth.isAuthed) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    const { data } = await http.post(`/api/interact/fav/${props.a.id}`)
    props.a.favorited = data.favorited
    props.a.favCount = data.favCount
  } catch (error) {
    console.error('收藏操作失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  }
}

// 切换发布状态
const togglePublish = async () => {
  if (!auth.isAuthed) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    const action = props.a.published ? '取消发布' : '发布'
    const confirmMessage = props.a.published
        ? '确定要取消发布这篇文章吗？取消后其他人将无法看到这篇文章。'
        : '确定要发布这篇文章吗？发布后所有人都可以看到这篇文章。'

    if (!confirm(confirmMessage)) {
      return
    }

    const { data } = await http.post(`/api/articles/${props.a.id}/toggle-publish`)
    props.a.published = data.published

    const message = data.published ? '文章已发布成功！' : '文章已转为草稿'
    alert(message)

    // 触发父组件刷新列表
    window.dispatchEvent(new CustomEvent('article-published', {
      detail: {
        id: props.a.id,
        published: data.published
      }
    }))
  } catch (error) {
    console.error('切换发布状态失败:', error)
    alert('操作失败: ' + (error.response?.data?.message || error.message))
  }
}

// 编辑文章
const editArticle = () => {
  router.push(`/editor/${props.a.id}`)
}
</script>

<style scoped>
.article-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .cover-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.1));
}

/* 状态标识 */
.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
  z-index: 2;
}

.status-badge:not(.published) {
  background: rgba(108, 117, 125, 0.9); /* 草稿 - 灰色 */
}

.status-badge.published {
  background: rgba(40, 167, 69, 0.9); /* 已发布 - 绿色 */
}

.card-content {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  line-height: 1.4;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-score {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.card-summary {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 0.85rem;
  color: #888;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-weight: 500;
  color: #555;
}

.meta-divider {
  color: #ddd;
}

.publish-date {
  color: #999;
}

.card-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: #666;
}

.stat-icon {
  font-size: 0.9rem;
}

.stat-count {
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.action-btn {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  background: white;
  color: #666;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
}

.action-btn.active {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

.like-btn.active {
  background: #e74c3c;
  border-color: #e74c3c;
}

.fav-btn.active {
  background: #f39c12;
  border-color: #f39c12;
}

/* 文章操作按钮 */
.article-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.btn.small {
  padding: 6px 12px;
  font-size: 0.8rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  text-align: center;
}

/* 发布按钮样式 - 绿色（当文章是草稿状态时显示） */
.publish-btn {
  background: #28a745;
  color: white;
}

.publish-btn:hover {
  background: #218838;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
}

/* 取消发布按钮样式 - 红色/警告色（当文章是已发布状态时显示） */
.unpublish-btn {
  background: #dc3545;
  color: white;
}

.unpublish-btn:hover {
  background: #c82333;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.edit-btn {
  background: #17a2b8;
  color: white;
}

.edit-btn:hover {
  background: #138496;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(23, 162, 184, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-content {
    padding: 20px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .hot-score {
    align-self: flex-start;
  }

  .card-stats {
    gap: 12px;
  }

  .card-actions {
    flex-direction: column;
  }

  .article-actions {
    flex-direction: column;
  }
}

/* 草稿文章的特殊样式 */
.article-card:has(.status-badge:not(.published)) {
  border: 2px solid #6c757d;
}

.article-card:has(.status-badge:not(.published)):hover {
  border-color: #495057;
}

/* 已发布文章的特殊样式 */
.article-card:has(.status-badge.published) {
  border: 2px solid #28a745;
}

.article-card:has(.status-badge.published):hover {
  border-color: #218838;
}
</style>