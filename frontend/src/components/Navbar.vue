<!-- 修改 Navbar.vue，在导航栏添加草稿箱链接 -->
<template>
  <div class="navbar">
    <div class="nav-container">
      <div class="nav-left">
        <router-link to="/" class="brand">
          <strong>北恒的个人博客</strong>
        </router-link>
        <div class="nav-spacer"></div>

        <!-- 搜索框 -->
        <div class="search-container">
          <input
              class="search-input"
              v-model="kw"
              placeholder="搜索..."
              @keyup.enter="goSearch"
          />
          <button class="btn ghost search-btn" @click="goSearch">
            <span class="search-icon">🔍</span>
          </button>
        </div>
      </div>

      <div class="nav-right">
        <router-link to="/" class="nav-link">首页</router-link>

        <!-- 博主专属功能 -->
        <template v-if="isBlogger">
          <router-link to="/editor" class="btn primary-btn nav-icon-link">
            <span class="btn-icon">✏️</span>
            写文章
          </router-link>

          <!-- 添加草稿箱链接 -->
          <router-link to="/drafts" class="nav-link draft-link nav-icon-link">
            <span class="nav-icon">📝</span>
            草稿箱
          </router-link>

          <!-- 管理面板入口 -->
          <router-link to="/admin" class="nav-link admin-link nav-icon-link">
            <span class="nav-icon">⚙️</span>
            管理
          </router-link>
        </template>

        <!-- 收藏链接 -->
        <router-link to="/favorites" class="nav-link nav-icon-link">
          <span class="nav-icon">⭐</span>
          我的收藏
        </router-link>

        <!-- 私信链接 -->
        <router-link v-if="auth.isAuthed" to="/messages" class="nav-link message-link nav-icon-link">
          <span class="nav-icon">💬</span>
          私信
          <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </router-link>

        <!-- 关注链接 -->
        <router-link to="/follow" class="nav-link nav-icon-link">
          <span class="nav-icon">👥</span>
          关注
        </router-link>

        <!-- 未登录状态 -->
        <div v-if="!auth.isAuthed" class="auth-buttons">
          <router-link to="/login" class="btn primary-btn">登录</router-link>
        </div>

        <!-- 已登录状态 -->
        <div v-else class="user-section">
          <!-- 用户头像 -->
          <div class="user-avatar-container">
            <img
                :src="auth.user?.avatarUrl || '/uploads/images/default/touxiang.jpg'"
                :alt="auth.user?.displayName"
                class="user-avatar"
                @click="toggleDropdown"
            />
            <!-- 下拉菜单 -->
            <div v-if="showDropdown" class="dropdown-menu">
              <div class="dropdown-item user-info">
                <img
                    :src="auth.user?.avatarUrl || '/uploads/images/default/touxiang.jpg'"
                    class="dropdown-avatar"
                />
                <div class="user-details">
                  <div class="user-name">{{ auth.user?.displayName || auth.user?.username }}</div>
                  <div class="user-role">{{ isBlogger ? '博主' : '读者' }}</div>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <router-link to="/user" class="dropdown-item" @click="showDropdown = false">
                <span class="dropdown-icon">🏠</span>
                个人主页
              </router-link>
              <router-link to="/profile" class="dropdown-item" @click="showDropdown = false">
                <span class="dropdown-icon">👤</span>
                个人资料
              </router-link>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item logout-btn" @click="doLogout">
                <span class="dropdown-icon">🚪</span>
                退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useMessageStore } from '../store/messageStore'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const messageStore = useMessageStore()
const kw = ref(route.query.keyword || '')
const showDropdown = ref(false)
const unreadCount = computed(() => useMessageStore.unreadCount)

const isBlogger = computed(() => auth.user?.role === 'BLOGGER' || auth.user?.role === 'ROLE_BLOGGER')

// 在 Navbar.vue 的搜索方法中
function goSearch() {
  if (kw.value.trim()) {
    router.push({
      path: '/search',
      query: { keyword: kw.value }
    })
  }
}

function doLogout() {
  auth.logout()
  router.push('/login')
  showDropdown.value = false
}

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

// 点击其他地方关闭下拉菜单
function handleClickOutside(event) {
  const userAvatar = document.querySelector('.user-avatar-container')
  if (userAvatar && !userAvatar.contains(event.target)) {
    showDropdown.value = false
  }

}


const loadUnreadCount = async () => {
  try {
    console.log('🔍 开始加载未读消息数')
    
    // 确保 store 和方法存在
    if (messageStore && typeof messageStore.loadUnreadCount === 'function') {
      await messageStore.loadUnreadCount()
      console.log('✅ 未读消息数加载完成')
    } else {
      console.error('❌ messageStore 或 loadUnreadCount 方法不存在')
    }
  } catch (error) {
    console.error('加载未读消息数失败:', error)
  }
}
// async function loadUnreadCount() {
//   if (auth.isAuthed) {
//     await useMessageStore.loadUnreadCount()
//   }
// }

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  setTimeout(() => {
    loadUnreadCount()
  }, 0)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.navbar {
  border-bottom: 1px solid #e1e5e9;
  background: white;
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand {
  text-decoration: none;
  font-size: 1.25rem;
  color: #2c3e50;
  font-weight: 700;
  white-space: nowrap;
}

.brand:hover {
  color: #3498db;
}

.nav-spacer {
  width: 24px;
  flex-shrink: 0;
}

.search-container {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e1e5e9;
  transition: all 0.3s ease;
}

.search-container:focus-within {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.search-input {
  padding: 8px 16px;
  border: none;
  background: transparent;
  outline: none;
  width: 200px;
  font-size: 14px;
}

.search-btn {
  padding: 8px 12px;
  border: none;
  background: transparent;
  border-left: 1px solid #e1e5e9;
  border-radius: 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: #e9ecef;
}

.search-icon {
  font-size: 12px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
  gap: 6px;
}

.ghost {
  background: transparent;
  border: 1px solid #e1e5e9;
  color: #666;
}

.ghost:hover {
  background: #f8f9fa;
  border-color: #ccc;
}

.primary-btn {
  background: #3498db;
  color: white;
  box-shadow: 0 2px 4px rgba(52, 152, 219, 0.2);
}

.primary-btn:hover {
  background: #2980b9;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(52, 152, 219, 0.3);
}

.nav-link {
  padding: 8px 16px;
  text-decoration: none;
  color: #666;
  border-radius: 6px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.nav-link:hover {
  color: #3498db;
  background: rgba(52, 152, 219, 0.1);
}

.nav-icon, .btn-icon {
  font-size: 12px;
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 用户头像样式 */
.user-avatar-container {
  position: relative;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e1e5e9;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-avatar:hover {
  border-color: #3498db;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 下拉菜单样式 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  min-width: 240px;
  z-index: 1000;
  animation: dropdownFade 0.2s ease;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  text-decoration: none;
  color: #333;
  border: none;
  background: none;
  width: 100%;
  cursor: pointer;
  transition: background 0.2s ease;
  font-size: 14px;
  gap: 10px;
}

.dropdown-item:hover {
  background: #f8f9fa;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fafbfc;
}

.dropdown-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e1e5e9;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 15px;
}

.user-role {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.dropdown-divider {
  height: 1px;
  background: #f0f0f0;
}

.dropdown-icon {
  font-size: 14px;
  width: 16px;
  text-align: center;
}

.logout-btn {
  color: #e74c3c;
}

.logout-btn:hover {
  background: #fdf2f2;
}

@keyframes dropdownFade {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 特殊链接样式 */
.draft-link {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.draft-link:hover {
  background: rgba(255, 193, 7, 0.2);
  color: #ff9800;
}

.admin-link {
  background: rgba(155, 89, 182, 0.1);
  color: #9b59b6;
}

.admin-link:hover {
  background: rgba(155, 89, 182, 0.2);
  color: #8e44ad;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
  }

  .search-container {
    display: none;
  }

  .nav-link {
    padding: 6px 12px;
    font-size: 13px;
  }

  .btn {
    padding: 6px 12px;
    font-size: 13px;
  }

  .nav-right {
    gap: 8px;
  }

  .brand {
    font-size: 1.1rem;
  }

  .unread-badge {
    top: -4px;
    right: -4px;
    font-size: 0.6rem;
    min-width: 16px;
    padding: 1px 4px;
  }
}

@media (max-width: 480px) {
  .nav-container {
    height: 56px;
  }

  .nav-link span:not(.nav-icon) {
    display: none;
  }

  .primary-btn span:not(.btn-icon) {
    display: none;
  }

  .nav-spacer {
    width: 12px;
  }
  
  /* 在小屏幕上，导航链接只显示图标 */
  .nav-icon-link span:not(.nav-icon):not(.unread-badge):not(.btn-icon),
  .primary-btn span:not(.btn-icon),
  .nav-link span:not(.nav-icon):not(.unread-badge) {
    display: none;
  }

  .unread-badge {
    top: 2px;
    right: 2px;
  }

  /* 调整按钮和链接的间距 */
  .nav-right {
    gap: 4px;
  }

  .nav-link, .btn {
    padding: 6px 8px;
    min-width: auto;
  }

  /* 确保图标居中显示 */
  .nav-link, .primary-btn {
    justify-content: center;
  }
}

.message-link {
  position: relative;
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
}

.message-link:hover {
  background: rgba(52, 152, 219, 0.2);
  color: #2980b9;
}

/* 未读消息徽章 */
.unread-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #e74c3c;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 0.7rem;
  font-weight: 600;
  min-width: 18px;
  text-align: center;
  line-height: 1;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  animation: pulse 2s infinite;
}

/* 徽章呼吸动画 */
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}


</style>