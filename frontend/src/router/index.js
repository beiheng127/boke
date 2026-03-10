import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

// 添加加载组件
const LoadingSpinner = {
  template: `
    <div class="loading-container">
      <div class="spinner"></div>
      <p>页面加载中...</p>
    </div>
  `
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../views/Home.vue'),
      // 预加载相关路由
      meta: { preload: ['article', 'search'] }
    },
    {
      path: '/login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/article/:id',
      component: () => import('../views/ArticleDetail.vue'),
      props: true
    },
    {
      path: '/editor/:id?',
      component: () => import('../views/Editor.vue'),
      meta: { requiresAuth: true, requiresBlogger: true }
    },
    {
      path: '/favorites',
      component: () => import('../views/Favorites.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('../views/AdminPage.vue'),
      meta: { requiresAuth: true, requiresBlogger: true }
    },
    {
      path: '/drafts',
      name: 'Drafts',
      component: () => import('../views/Drafts.vue'),
      meta: { requiresAuth: true, requiresBlogger: true }
    },
    {
      path: '/user/:userId?',
      name: 'UserHome',
      component: () => import('../views/UserHome.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/follow',
      name: 'Follow',
      component: () => import('../views/FollowPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/search',
      name: 'Search',
      component: () => import('../views/SearchPage.vue')
    },
    {
      path: '/user/:userId/follow',
      name: 'UserFollow',
      component: () => import('../views/UserFollow.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/messages',
      name: 'Messages',
      component: () => import('../views/Messages.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由预加载功能
const preloadMap = {
  'article': () => import('../views/ArticleDetail.vue'),
  'search': () => import('../views/SearchPage.vue'),
  'user': () => import('../views/UserHome.vue')
}

// 预加载关键路由
function preloadCriticalRoutes() {
  // 预加载首页可能用到的路由
  if (typeof window !== 'undefined') {
    setTimeout(() => {
      preloadMap.article()
      preloadMap.search()
    }, 2000)
  }
}

// 合并为一个路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  console.log('🔍 路由守卫 - 目标路径:', to.path)
  console.log('🔍 路由守卫 - 认证状态:', authStore.isAuthed)
  console.log('🔍 路由守卫 - 用户角色:', authStore.user?.role)
  console.log('🔍 路由守卫 - 是否为博主:', authStore.isBlogger)

  // 预加载相关路由
  if (to.meta.preload) {
    to.meta.preload.forEach(route => {
      if (preloadMap[route]) {
        preloadMap[route]()
      }
    })
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth || to.meta.auth) {
    if (!authStore.isAuthed) {
      console.log('🔍 需要认证，跳转到登录页，携带重定向参数:', to.fullPath)
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 检查是否需要博主权限
    if ((to.meta.requiresBlogger || to.meta.bloggerOnly) && !authStore.isBlogger) {
      console.log('🔍 需要博主权限，但用户不是博主')
      alert('只有博主可以访问此页面')
      next('/')
      return
    }
  }

  // 已登录用户访问登录页，重定向到首页或目标页面
  if (to.path === '/login' && authStore.isAuthed) {
    console.log('🔍 已登录用户访问登录页，重定向到首页')
    next('/')
    return
  }

  next()
})

// 初始化预加载
if (typeof window !== 'undefined') {
  window.addEventListener('load', preloadCriticalRoutes)
}

export default router