<template>
  <div class="admin-container">

    <!-- 添加调试工具栏 -->
    <div class="debug-toolbar">
      <button @click="debugToken" class="btn btn-sm debug-btn">
        🔧 调试Token状态
      </button>
      <button @click="testAdminAPI" class="btn btn-sm debug-btn">
        🧪 测试管理员API
      </button>
      <button @click="forceRelogin" class="btn btn-sm debug-btn" style="background: #e74c3c;">
        🔄 强制重新登录
      </button>
    </div>

    <!-- 页面头部 -->
    <div class="page-header">
      <h1>博主管理面板</h1>
      <p class="page-description">管理用户、文章和博主账号</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <h3>总用户数</h3>
          <p class="stat-number">{{ stats.totalUsers }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-info">
          <h3>总文章数</h3>
          <p class="stat-number">{{ stats.totalArticles }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👑</div>
        <div class="stat-info">
          <h3>博主数量</h3>
          <p class="stat-number">{{ stats.bloggerCount }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <h3>活跃用户</h3>
          <p class="stat-number">{{ stats.activeUsers || '-' }}</p>
        </div>
      </div>
    </div>

    <!-- 标签页导航 -->
    <div class="tabs-container">
      <div class="tabs-header">
        <button
            v-for="tab in tabs"
            :key="tab.id"
            :class="['tab-button', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          {{ tab.name }}
        </button>
      </div>

      <div class="tab-content">
        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'" class="management-section">
          <div class="section-header">
            <h2>👥 用户管理</h2>
            <div class="search-box">
              <input
                  v-model="userSearch"
                  placeholder="搜索用户..."
                  class="search-input"
              />
              <span class="search-icon">🔍</span>
            </div>
          </div>

          <div class="table-container">
            <table class="data-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>用户信息</th>
                <th>角色</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="user in filteredUsers" :key="user.id">
                <td class="user-id">{{ user.id }}</td>
                <td class="user-info">
                  <img
                      :src="user.avatarUrl || '/uploads/images/default/touxiang.jpg'"
                      :alt="user.displayName"
                      class="user-avatar"
                  />
                  <div class="user-details">
                    <div class="user-name">{{ user.displayName || user.username }}</div>
                    <div class="user-username">@{{ user.username }}</div>
                  </div>
                </td>
                <td>
                  <select
                      :value="user.role"
                      @change="updateUserRole(user.id, $event.target.value)"
                      :class="['role-select', user.role.toLowerCase()]"
                      :disabled="user.id === currentUser.id"
                  >
                    <option value="VIEWER">👀 浏览者</option>
                    <option value="BLOGGER">👑 博主</option>
                  </select>
                </td>
                <td class="date-cell">{{ formatDate(user.createdAt) }}</td>
                <td class="actions-cell">
                  <button
                      @click="deleteUser(user.id)"
                      class="btn btn-danger btn-sm"
                      :disabled="user.id === currentUser.id"
                      :title="user.id === currentUser.id ? '不能删除自己' : '删除用户'"
                  >
                    🗑️ 删除
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div v-if="filteredUsers.length === 0" class="empty-state">
            <div class="empty-icon">👥</div>
            <h3>没有找到用户</h3>
            <p v-if="userSearch">尝试调整搜索关键词</p>
          </div>

          <div v-if="userTotalPages > 1" class="pagination">
            <button
                @click="changeUserPage(currentUserPage - 1)"
                :disabled="currentUserPage === 0"
                class="btn btn-ghost"
            >
              ◀ 上一页
            </button>
            <span class="page-info">
              第 {{ currentUserPage + 1 }} 页，共 {{ userTotalPages }} 页
            </span>
            <button
                @click="changeUserPage(currentUserPage + 1)"
                :disabled="currentUserPage >= userTotalPages - 1"
                class="btn btn-ghost"
            >
              下一页 ▶
            </button>
          </div>
        </div>

        <!-- 文章管理 -->
        <div v-if="activeTab === 'articles'" class="management-section">
          <div class="section-header">
            <h2>📝 文章管理</h2>
            <div class="search-box">
              <input
                  v-model="articleSearch"
                  placeholder="搜索文章..."
                  class="search-input"
              />
              <span class="search-icon">🔍</span>
            </div>
          </div>

          <div class="table-container">
            <table class="data-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>文章标题</th>
                <th>作者</th>
                <th>状态</th>
                <th>发布时间</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="article in filteredArticles" :key="article.id">
                <td class="article-id">{{ article.id }}</td>
                <td class="article-title">
                  <router-link
                      :to="`/article/${article.id}`"
                      class="article-link"
                      target="_blank"
                  >
                    {{ article.title }}
                  </router-link>
                </td>
                <td class="author-info">
                  <span>{{ article.author?.displayName || article.author?.username }}</span>
                </td>
                <td>
                    <span :class="['status-badge', article.published ? 'published' : 'draft']">
                      {{ article.published ? '✅ 已发布' : '📝 草稿' }}
                    </span>
                </td>
                <td class="date-cell">{{ formatDate(article.createdAt) }}</td>
                <td class="actions-cell">
                  <div class="action-buttons">
                    <button
                        @click="toggleArticlePublish(article.id)"
                        :class="['btn', 'btn-sm', article.published ? 'btn-warning' : 'btn-success']"
                    >
                      {{ article.published ? '⏸️ 取消发布' : '🚀 发布' }}
                    </button>
                    <button
                        @click="deleteArticle(article.id)"
                        class="btn btn-danger btn-sm"
                    >
                      🗑️ 删除
                    </button>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div v-if="filteredArticles.length === 0" class="empty-state">
            <div class="empty-icon">📝</div>
            <h3>没有找到文章</h3>
            <p v-if="articleSearch">尝试调整搜索关键词</p>
          </div>

          <div v-if="articleTotalPages > 1" class="pagination">
            <button
                @click="changeArticlePage(currentArticlePage - 1)"
                :disabled="currentArticlePage === 0"
                class="btn btn-ghost"
            >
              ◀ 上一页
            </button>
            <span class="page-info">
              第 {{ currentArticlePage + 1 }} 页，共 {{ articleTotalPages }} 页
            </span>
            <button
                @click="changeArticlePage(currentArticlePage + 1)"
                :disabled="currentArticlePage >= articleTotalPages - 1"
                class="btn btn-ghost"
            >
              下一页 ▶
            </button>
          </div>
        </div>

        <!-- 创建博主 -->
        <div v-if="activeTab === 'create-blogger'" class="management-section">
          <div class="create-blogger-section">
            <h2>👑 创建博主账号</h2>
            <p class="section-description">创建新的博主账号，授予管理权限</p>

            <div class="create-form">
              <div class="form-group">
                <label>用户名 *</label>
                <input
                    v-model="newBlogger.username"
                    placeholder="输入用户名"
                    class="input"
                    @blur="validateUsername"
                />
                <div v-if="usernameError" class="error-text">{{ usernameError }}</div>
              </div>

              <div class="form-group">
                <label>邮箱 *</label>
                <input
                    v-model="newBlogger.email"
                    type="email"
                    placeholder="输入邮箱地址"
                    class="input"
                    @blur="validateEmail"
                />
                <div v-if="emailError" class="error-text">{{ emailError }}</div>
              </div>

              <div class="form-group">
                <label>密码 *</label>
                <input
                    v-model="newBlogger.password"
                    type="password"
                    placeholder="输入密码（至少6位）"
                    class="input"
                    @blur="validatePassword"
                />
                <div v-if="passwordError" class="error-text">{{ passwordError }}</div>
              </div>

              <div class="form-group">
                <label>显示名称 *</label>
                <input
                    v-model="newBlogger.displayName"
                    placeholder="输入显示名称"
                    class="input"
                />
              </div>

              <button
                  @click="createBlogger"
                  :disabled="!canCreateBlogger"
                  :class="['btn', 'btn-primary', 'create-btn', { disabled: !canCreateBlogger }]"
              >
                👑 创建博主账号
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="message" :class="['message-toast', messageType]">
      <span class="toast-icon">{{ messageType === 'success' ? '✅' : '❌' }}</span>
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

// 标签页配置
const tabs = [
  { id: 'users', name: '用户管理', icon: '👥' },
  { id: 'articles', name: '文章管理', icon: '📝' },
  { id: 'create-blogger', name: '创建博主', icon: '👑' }
]

// 状态管理
const activeTab = ref('users')
const loading = ref(false)
const message = ref('')
const messageType = ref('success')

// 用户管理状态
const users = ref([])
const userSearch = ref('')
const currentUserPage = ref(0)
const userTotalPages = ref(0)

// 文章管理状态
const articles = ref([])
const articleSearch = ref('')
const currentArticlePage = ref(0)
const articleTotalPages = ref(0)

// 验证状态
const usernameError = ref('')
const emailError = ref('')
const passwordError = ref('')

// 创建博主状态
const newBlogger = ref({
  username: '',
  email: '',
  password: '',
  displayName: ''
})

// 统计信息
const stats = ref({
  totalUsers: 0,
  totalArticles: 0,
  bloggerCount: 0,
  activeUsers: 0
})

// 计算属性 - 简化验证逻辑
const canCreateBlogger = computed(() => {
  return newBlogger.value.username &&
      newBlogger.value.email &&
      newBlogger.value.password &&
      newBlogger.value.displayName &&
      !usernameError.value &&
      !emailError.value &&
      !passwordError.value
})

// 邮箱验证
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(newBlogger.value.email)
})

// 验证用户名
const validateUsername = async () => {
  if (!newBlogger.value.username) {
    usernameError.value = '用户名不能为空'
    return
  }

  if (newBlogger.value.username.length < 3) {
    usernameError.value = '用户名至少3个字符'
    return
  }

  // 移除用户名唯一性检查，因为后端会检查
  usernameError.value = ''
}

// 验证邮箱
const validateEmail = () => {
  if (!newBlogger.value.email) {
    emailError.value = '邮箱不能为空'
    return
  }

  if (!isEmailValid.value) {
    emailError.value = '请输入有效的邮箱地址'
    return
  }

  emailError.value = ''
}

// 验证密码
const validatePassword = () => {
  if (!newBlogger.value.password) {
    passwordError.value = '密码不能为空'
    return
  }

  if (newBlogger.value.password.length < 6) {
    passwordError.value = '密码至少6个字符'
    return
  }

  passwordError.value = ''
}

// 计算属性
const currentUser = computed(() => authStore.user || {})
const filteredUsers = computed(() => {
  if (!userSearch.value) return users.value
  const searchLower = userSearch.value.toLowerCase()
  return users.value.filter(user =>
      user.username.toLowerCase().includes(searchLower) ||
      user.displayName?.toLowerCase().includes(searchLower)
  )
})

const filteredArticles = computed(() => {
  if (!articleSearch.value) return articles.value
  return articles.value.filter(article =>
      article.title.toLowerCase().includes(articleSearch.value.toLowerCase())
  )
})

// 方法
const showMessage = (text, type = 'success') => {
  message.value = text
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 4000)
}

const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// API 调用方法 - 修改所有路径，添加 /api 前缀
const loadUsers = async (page = 0) => {
  try {
    loading.value = true
    // 修改路径：从 /admin/users 改为 /api/admin/users
    const response = await authStore.apiGet(`/api/admin/users?page=${page}&size=10`)
    users.value = response.users || response.content || []
    userTotalPages.value = response.totalPages || 1

    // 更新统计信息
    stats.value.totalUsers = response.totalItems || users.value.length
    stats.value.bloggerCount = users.value.filter(user =>
        user.role === 'BLOGGER' || user.role === 'ROLE_BLOGGER'
    ).length
  } catch (error) {
    showMessage('加载用户失败: ' + (error.response?.data?.message || error.message), 'error')
    console.error('加载用户失败:', error)
  } finally {
    loading.value = false
  }
}

const loadArticles = async (page = 0) => {
  try {
    loading.value = true
    // 修改路径：从 /admin/articles 改为 /api/admin/articles
    const response = await authStore.apiGet(`/api/admin/articles?page=${page}&size=10`)
    articles.value = response.articles || response.content || []
    articleTotalPages.value = response.totalPages || 1

    // 更新统计信息
    stats.value.totalArticles = response.totalItems || articles.value.length
  } catch (error) {
    showMessage('加载文章失败: ' + (error.response?.data?.message || error.message), 'error')
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

const updateUserRole = async (userId, role) => {
  try {
    loading.value = true
    // 修改路径：从 /admin/users/${userId}/role 改为 /api/admin/users/${userId}/role
    await authStore.apiPut(`/api/admin/users/${userId}/role`, { role })
    showMessage('用户角色更新成功')
    await loadUsers(currentUserPage.value)
  } catch (error) {
    showMessage('更新用户角色失败: ' + (error.response?.data?.message || error.message), 'error')
    console.error('更新用户角色失败:', error)
  } finally {
    loading.value = false
  }
}

const deleteUser = async (userId) => {
  if (userId === currentUser.value.id) {
    showMessage('不能删除自己的账号', 'error')
    return
  }

  if (!confirm('确定要删除这个用户吗？此操作不可逆。')) {
    return
  }

  try {
    loading.value = true
    // 修改路径：从 /admin/users/${userId} 改为 /api/admin/users/${userId}
    await authStore.apiDelete(`/api/admin/users/${userId}`)
    showMessage('用户删除成功')
    await loadUsers(currentUserPage.value)
  } catch (error) {
    showMessage('删除用户失败: ' + (error.response?.data?.message || error.message), 'error')
    console.error('删除用户失败:', error)
  } finally {
    loading.value = false
  }
}

const deleteArticle = async (articleId) => {
  if (!confirm('确定要删除这篇文章吗？此操作不可逆。')) {
    return
  }

  try {
    loading.value = true
    // 修改路径：从 /admin/articles/${articleId} 改为 /api/admin/articles/${articleId}
    await authStore.apiDelete(`/api/admin/articles/${articleId}`)
    showMessage('文章删除成功')
    await loadArticles(currentArticlePage.value)
  } catch (error) {
    showMessage('删除文章失败: ' + (error.response?.data?.message || error.message), 'error')
    console.error('删除文章失败:', error)
  } finally {
    loading.value = false
  }
}

const toggleArticlePublish = async (articleId) => {
  try {
    loading.value = true
    // 修改路径：从 /admin/articles/${articleId}/toggle-publish 改为 /api/admin/articles/${articleId}/toggle-publish
    await authStore.apiPost(`/api/admin/articles/${articleId}/toggle-publish`)
    showMessage('文章状态更新成功')
    await loadArticles(currentArticlePage.value)
  } catch (error) {
    showMessage('更新文章状态失败: ' + (error.response?.data?.message || error.message), 'error')
    console.error('更新文章状态失败:', error)
  } finally {
    loading.value = false
  }
}

const createBlogger = async () => {
  // 重新验证所有字段
  validateUsername()
  validateEmail()
  validatePassword()

  if (usernameError.value || emailError.value || passwordError.value) {
    showMessage('请修正表单错误', 'error')
    return
  }

  try {
    loading.value = true

    // 直接创建博主账号，不验证验证码
    const createResponse = await authStore.apiPost('/api/admin/create-blogger', {
      username: newBlogger.value.username,
      email: newBlogger.value.email,
      password: newBlogger.value.password,
      displayName: newBlogger.value.displayName
    })

    showMessage('博主账号创建成功')

    // 重置表单
    newBlogger.value = {
      username: '',
      email: '',
      password: '',
      displayName: ''
    }

    await loadUsers(currentUserPage.value) // 刷新用户列表
  } catch (error) {
    const errorMessage = error.response?.data?.message || '创建博主账号失败'
    showMessage(errorMessage, 'error')
    console.error('创建博主账号失败:', error)

    // 如果是用户名或邮箱已存在，设置对应的错误信息
    if (errorMessage.includes('用户名')) {
      usernameError.value = errorMessage
    } else if (errorMessage.includes('邮箱')) {
      emailError.value = errorMessage
    }
  } finally {
    loading.value = false
  }
}

const changeUserPage = (page) => {
  if (page >= 0 && page < userTotalPages.value) {
    currentUserPage.value = page
    loadUsers(page)
  }
}

const changeArticlePage = (page) => {
  if (page >= 0 && page < articleTotalPages.value) {
    currentArticlePage.value = page
    loadArticles(page)
  }
}

// 检查用户权限
const checkPermission = () => {
  const userRole = currentUser.value.role
  const isBlogger = userRole === 'BLOGGER' || userRole === 'ROLE_BLOGGER'

  if (!isBlogger) {
    showMessage('您没有权限访问管理面板', 'error')
    router.push('/')
    return false
  }
  return true
}

// 监听标签页变化
watch(activeTab, (newTab) => {
  if (newTab === 'users' && users.value.length === 0) {
    loadUsers()
  } else if (newTab === 'articles' && articles.value.length === 0) {
    loadArticles()
  }
})


// 两个调试方法
const debugToken = async () => {
  console.log('=== 🔧 Token调试信息 ===')
  const token = localStorage.getItem('token')
  console.log('1. 本地存储Token:', token)
  console.log('2. Token长度:', token?.length)
  console.log('3. Store Token:', authStore.token)
  console.log('4. 当前用户:', authStore.user)
  console.log('5. 用户ID:', authStore.user?.id)
  console.log('6. 认证状态:', authStore.isAuthed)
  console.log('7. 博主状态:', authStore.isBlogger)

  // 测试Token验证
  try {
    console.log('8. 测试Token验证API...')
    const response = await authStore.apiGet('/api/auth/validate')
    console.log('✅ Token验证成功:', response.data)
  } catch (error) {
    console.error('❌ Token验证失败:', {
      status: error.response?.status,
      message: error.response?.data?.message || error.message
    })
  }
}

const testAdminAPI = async () => {
  console.log('=== 🧪 测试管理员API ===')
  try {
    console.log('1. 测试 /api/articles (普通用户API)...')
    const articleResponse = await authStore.apiGet('/api/articles?page=0&size=5')
    console.log('✅ 普通API成功:', articleResponse.status)

    console.log('2. 测试 /api/admin/users (管理员API)...')
    const adminResponse = await authStore.apiGet('/api/admin/users?page=0&size=5')
    console.log('✅ 管理员API成功:', adminResponse.status)
    console.log('✅ 管理员API数据:', adminResponse.data)
  } catch (error) {
    console.error('❌ API测试失败:', {
      url: error.config?.url,
      status: error.response?.status,
      message: error.response?.data?.message || error.message
    })
  }
}

const forceRelogin = () => {
  console.log('🔄 强制重新登录...')
  authStore.logout()
  router.push({
    path: '/login',
    query: { redirect: '/admin' }
  })
}


onMounted(async() => {
  console.log('🔍 管理页面加载 - 认证状态:', authStore.isAuthed)
  console.log('🔍 管理页面加载 - 用户角色:', authStore.user?.role)
  console.log('🔍 管理页面加载 - 是否为博主:', authStore.isBlogger)

  // 先测试API连接
  await testAdminAPI()

  const token = localStorage.getItem('token')
  if (token && token.length < 20) { // 假设有效Token长度大于20
    console.log('检测到可能无效的Token，执行登出')
    authStore.logout()
    router.push('/login')
    return
  }

  // 双重检查权限
  if (!authStore.isAuthed) {
    console.log('🔍 用户未登录，跳转到登录页')
    router.push({
      path: '/login',
      query: { redirect: '/admin' }
    })
    return
  }

  if (!authStore.isBlogger) {
    console.log('🔍 用户不是博主，没有权限')
    showMessage('您没有权限访问管理面板', 'error')
    router.push('/')
    return
  }

  console.log('🔍 权限检查通过，加载数据')
  if (checkPermission()) {
    loadUsers()
  }
})
</script>

<style scoped>
.admin-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: #f8f9fa;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 8px;
  font-weight: 700;
}

.page-description {
  color: #666;
  font-size: 1.1rem;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-info h3 {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 0.9rem;
  font-weight: 500;
}

.stat-number {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
}

/* 标签页 */
.tabs-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.tabs-header {
  display: flex;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.tab-button {
  flex: 1;
  padding: 16px 24px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.tab-button:hover {
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
}

.tab-button.active {
  background: white;
  color: #3498db;
  border-bottom: 3px solid #3498db;
}

.tab-content {
  padding: 0;
}

/* 管理区域 */
.management-section {
  padding: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-header h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  padding: 10px 16px 10px 40px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  width: 280px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #666;
  font-size: 14px;
}

/* 表格样式 */
.table-container {
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.data-table th {
  background: #f8f9fa;
  padding: 16px 12px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 1px solid #e9ecef;
  font-size: 0.9rem;
}

.data-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #f1f3f4;
  vertical-align: middle;
}

.data-table tr:hover {
  background: #f8f9fa;
}

/* 用户信息列 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e9ecef;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
}

.user-username {
  font-size: 0.8rem;
  color: #666;
}

/* 文章标题 */
.article-title {
  max-width: 300px;
}

.article-link {
  color: #2c3e50;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.article-link:hover {
  color: #3498db;
}

/* 角色选择 */
.role-select {
  padding: 6px 12px;
  border: 1px solid #e1e5e9;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.role-select.viewer {
  background: #e3f2fd;
  border-color: #2196f3;
  color: #1976d2;
}

.role-select.blogger {
  background: #fff3e0;
  border-color: #ff9800;
  color: #f57c00;
}

.role-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 状态标签 */
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.published {
  background: #d4edda;
  color: #155724;
}

.status-badge.draft {
  background: #fff3cd;
  color: #856404;
}

/* 按钮样式 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.8rem;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(.disabled) {
  background: #2980b9;
  transform: translateY(-1px);
}

.btn-success {
  background: #27ae60;
  color: white;
}

.btn-success:hover {
  background: #219a52;
}

.btn-warning {
  background: #f39c12;
  color: white;
}

.btn-warning:hover {
  background: #e67e22;
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn-danger:hover {
  background: #c0392b;
}

.btn-ghost {
  background: transparent;
  border: 1px solid #e1e5e9;
  color: #666;
}

.btn-ghost:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

/* 操作按钮组 */
.actions-cell {
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

/* 创建博主表单 */
.create-blogger-section {
  max-width: 500px;
  margin: 0 auto;
  text-align: center;
}

.section-description {
  color: #666;
  margin-bottom: 30px;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.create-btn {
  padding: 14px 28px;
  font-size: 1.1rem;
  margin-top: 10px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.3;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  color: #666;
}

.empty-state p {
  margin: 0;
  color: #999;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  padding: 20px 0;
}

.page-info {
  color: #666;
  font-size: 0.9rem;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 消息提示 */
.message-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 16px 20px;
  border-radius: 8px;
  z-index: 1001;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease;
}

.message-toast.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message-toast.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.toast-icon {
  font-size: 1.2rem;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-container {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .tabs-header {
    flex-direction: column;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .data-table {
    font-size: 0.8rem;
  }

  .action-buttons {
    flex-direction: column;
  }

  .pagination {
    flex-direction: column;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 2rem;
  }

  .management-section {
    padding: 20px 16px;
  }

  .data-table th,
  .data-table td {
    padding: 12px 8px;
  }

  .user-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}

.debug-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.debug-btn {
  background: #666;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.debug-btn:hover {
  background: #555;
}

.error-text {
  color: #e74c3c;
  font-size: 0.8rem;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.error-text::before {
  content: "⚠️";
  font-size: 0.7rem;
}
</style>