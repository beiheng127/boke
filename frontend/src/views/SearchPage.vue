<!-- src/views/SearchPage.vue -->
<template>
  <div class="search-page">
    <!-- 搜索栏 -->
    <div class="search-header">
      <div class="search-bar-container">
        <input
            v-model="searchKeyword"
            placeholder="搜索文章、用户..."
            @keyup.enter="performSearch"
            class="search-input"
        />
        <button @click="performSearch" class="search-btn">搜索</button>
      </div>
    </div>

    <!-- 热度推荐栏（搜索前显示） -->
    <div v-if="!hasSearched" class="hot-recommendation">
      <h3>热门推荐</h3>
      <div class="hot-articles">
        <ArticleCard
            v-for="article in hotArticles"
            :key="article.id"
            :a="article"
        />
      </div>
    </div>

    <!-- 筛选条件（搜索后显示） -->
    <div v-if="hasSearched" class="filter-section">
      <div class="filter-group">
        <label>排序方式:</label>
        <select v-model="sortBy" @change="refreshSearch">
          <option value="hot">🔥 热度排序</option>
          <option value="time">🕒 最新发布</option>
        </select>
      </div>

      <div class="filter-group">
        <label>内容类型:</label>
        <select v-model="contentType" @change="refreshSearch">
          <option value="all">全部</option>
          <option value="articles">仅文章</option>
          <option value="users">仅用户</option>
        </select>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="hasSearched" class="search-results">
      <!-- 用户搜索结果 -->
      <div v-if="userResults.length > 0 && contentType !== 'articles'" class="user-results-section">
        <h3>用户搜索结果 ({{ userResults.length }})</h3>
        <div class="user-results">
          <div
              v-for="user in userResults"
              :key="user.id"
              class="user-card"
              @click="goToUserProfile(user.id)"
          >
            <img :src="user.avatarUrl" :alt="user.displayName" class="user-avatar">
            <div class="user-info">
              <h4>{{ user.displayName || user.username }}</h4>
              <p v-if="user.signature" class="user-bio">{{ user.signature }}</p>
              <span class="user-role">{{ user.role === 'BLOGGER' ? '博主' : '读者' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 文章搜索结果 -->
      <div v-if="articleResults.length > 0 && contentType !== 'users'" class="article-results-section">
        <h3>文章搜索结果 ({{ articleResults.length }})</h3>
        <div class="article-results">
          <ArticleCard
              v-for="article in articleResults"
              :key="article.id"
              :a="article"
          />
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="hasSearched && userResults.length === 0 && articleResults.length === 0" class="empty-results">
        <div class="empty-icon">🔍</div>
        <h4>没有找到相关结果</h4>
        <p>尝试调整搜索关键词或筛选条件</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import http from '../api/http'
import ArticleCard from '../components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 搜索状态
const searchKeyword = ref('')
const hasSearched = ref(false)
const loading = ref(false)

// 筛选条件
const sortBy = ref('hot')
const contentType = ref('all')

// 数据
const hotArticles = ref([])
const userResults = ref([])
const articleResults = ref([])

// 方法
async function performSearch() {
  if (!searchKeyword.value.trim()) return

  loading.value = true
  hasSearched.value = true

  try {
    // 并行搜索用户和文章
    const [usersResponse, articlesResponse] = await Promise.all([
      searchUsers(),
      searchArticles()
    ])

    userResults.value = usersResponse
    articleResults.value = articlesResponse
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

async function searchUsers() {
  if (contentType.value === 'articles') return []

  try {
    const { data } = await http.get('/api/users/search', {
      params: {
        keyword: searchKeyword.value,
        page: 0,
        size: 10
      }
    })
    return data.content || []
  } catch (error) {
    console.error('搜索用户失败:', error)
    return []
  }
}

async function searchArticles() {
  if (contentType.value === 'users') return []

  try {
    const params = {
      keyword: searchKeyword.value,
      sortBy: sortBy.value,
      page: 0,
      size: 20
    }

    const { data } = await http.get('/api/articles', { params })
    return data.content || []
  } catch (error) {
    console.error('搜索文章失败:', error)
    return []
  }
}

async function loadHotArticles() {
  try {
    const { data } = await http.get('/api/articles', {
      params: {
        sortBy: 'hot',
        page: 0,
        size: 10
      }
    })
    hotArticles.value = data.content || []
  } catch (error) {
    console.error('加载热门文章失败:', error)
  }
}

function refreshSearch() {
  if (hasSearched.value) {
    performSearch()
  }
}

function goToUserProfile(userId) {
  router.push(`/user/${userId}`)
}

// 监听路由参数
watch(() => route.query, (newQuery) => {
  if (newQuery.keyword) {
    searchKeyword.value = newQuery.keyword
    performSearch()
  }
})

onMounted(() => {
  loadHotArticles()

  // 如果URL中有搜索关键词，自动搜索
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
    performSearch()
  }
})
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 30px;
}

.search-bar-container {
  display: flex;
  max-width: 600px;
  margin: 0 auto;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px 0 0 8px;
  font-size: 16px;
  outline: none;
}

.search-input:focus {
  border-color: #3498db;
}

.search-btn {
  padding: 12px 24px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  font-size: 16px;
}

.search-btn:hover {
  background: #2980b9;
}

.hot-recommendation {
  margin-top: 40px;
}

.hot-recommendation h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.hot-articles {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin: 30px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #2c3e50;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  border-radius: 4px;
  background: white;
}

.search-results {
  margin-top: 30px;
}

.user-results-section, .article-results-section {
  margin-bottom: 40px;
}

.user-results-section h3, .article-results-section h3 {
  margin-bottom: 20px;
  color: #2c3e50;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.user-results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-card:hover {
  border-color: #3498db;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.1);
  transform: translateY(-2px);
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

.user-info h4 {
  margin: 0 0 4px 0;
  color: #2c3e50;
}

.user-bio {
  margin: 0 0 4px 0;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.4;
}

.user-role {
  font-size: 0.8rem;
  color: #3498db;
  background: rgba(52, 152, 219, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
}

.article-results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.empty-results {
  text-align: center;
  padding: 60px 20px;
  color: #888;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-results h4 {
  margin: 0 0 8px 0;
  font-size: 1.25rem;
  color: #666;
}

.empty-results p {
  margin: 0;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-page {
    padding: 16px;
  }

  .filter-section {
    flex-direction: column;
    gap: 12px;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .hot-articles,
  .article-results {
    grid-template-columns: 1fr;
  }

  .user-results {
    grid-template-columns: 1fr;
  }
}
</style>