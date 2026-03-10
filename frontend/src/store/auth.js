import { defineStore } from 'pinia'
import http from '../api/http'
import { setAuthToken } from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null
  }),
  getters: {
    isAuthed: (s) => {
      const hasValidToken = !!s.token && s.token.length > 10
      const hasUser = !!s.user
      console.log('🔍 认证检查 - token:', s.token, 'user:', s.user)
      console.log('🔍 认证检查结果:', hasValidToken && hasUser)
      return hasValidToken && hasUser
    },
    isBlogger: (s) => {
      const role = s.user?.role
      const isBlogger = role === 'BLOGGER' || role === 'ROLE_BLOGGER'
      console.log('🔍 博主检查 - 角色:', role, '结果:', isBlogger)
      return isBlogger
    }
  },
  actions: {
    // 初始化方法 - 在应用启动时调用
    initialize() {
      const token = localStorage.getItem('token')
      const user = localStorage.getItem('user')
      
      if (token && user) {
        this.token = token
        this.user = JSON.parse(user)
        setAuthToken(token)
        console.log('✅ AuthStore 初始化完成 - Token 已同步')
      } else {
        console.log('🔍 AuthStore 初始化 - 无存储的认证信息')
      }
    },
    
    setSession(token, user) {
      console.log('🔍 设置会话 - token:', token, 'user:', user)
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      
      // 同步到 http.js
      setAuthToken(token)
      console.log('✅ Token 已同步到 http.js')

      // 立即验证存储
      setTimeout(() => {
        console.log('🔍 存储验证 - localStorage token:', localStorage.getItem('token'))
        console.log('🔍 存储验证 - localStorage user:', localStorage.getItem('user'))
      }, 100)
    },
    
    logout() {
      console.log('🔍 执行登出')
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      setAuthToken(null)
    },
    
    async login(username, password) {
      console.log('🔍 开始登录:', username)
      try {
        // 先清除可能的无效Token
        this.logout()

        const {data} = await http.post('/api/auth/login', {username, password})
        console.log('🔍 登录响应数据:', data)

        // 直接使用后端返回的角色
        const userRole = data.role || 'VIEWER'

        const userData = {
          id: data.userId,
          username: data.username,
          role: userRole,
          displayName: data.displayName,
          avatarUrl: data.avatarUrl,
          homeBackgroundUrl: data.homeBackgroundUrl
        }

        this.setSession(data.token, userData)

        // 登录成功后加载背景图片
        await this.loadUserBackground()

        console.log('🔍 登录完成，当前状态:', this.user)
        return { success: true }
      } catch (error) {
        console.error('❌ 登录失败:', error)
        // 确保清除任何可能的部分登录状态
        this.logout()
        return {
          success: false,
          message: error.response?.data?.message || '登录失败'
        }
      }
    },
    
    async register(payload) {
      try {
        const { data } = await http.post('/api/auth/register', {
          username: payload.username,
          password: payload.password,
          displayName: payload.displayName,
          role: payload.role
        });

        const userRole = data.role || 'VIEWER'

        this.setSession(data.token, {
          id: data.userId,
          username: data.username,
          role: userRole,
          displayName: data.displayName,
          avatarUrl: data.avatarUrl
        });
        return { success: true, message: data.message };
      } catch (error) {
        console.error('注册失败:', error);
        return {
          success: false,
          message: error.response?.data?.message || '注册失败，请稍后重试'
        };
      }
    },
    
    // 管理员 API 方法
    async apiGet(url) {
      try {
        const response = await http.get(url)
        return response.data
      } catch (error) {
        console.error('API GET 错误:', error)
        throw error
      }
    },

    async apiPost(url, data) {
      try {
        const response = await http.post(url, data)
        return response.data
      } catch (error) {
        console.error('API POST 错误:', error)
        throw error
      }
    },

    async apiPut(url, data) {
      try {
        const response = await http.put(url, data)
        return response.data
      } catch (error) {
        console.error('API PUT 错误:', error)
        throw error
      }
    },

    async apiDelete(url) {
      try {
        const response = await http.delete(url)
        return response.data
      } catch (error) {
        console.error('API DELETE 错误:', error)
        throw error
      }
    },
    
    async loadUserBackground() {
      if (!this.isAuthed) {
        console.log('🔍 未登录，跳过加载背景图片')
        return
      }

      try {
        console.log('🔍 开始加载用户背景图片')
        const { data } = await http.get('/api/users/home-background')
        console.log('🔍 背景图片响应:', data)

        if (data.homeBackgroundUrl) {
          this.user.homeBackgroundUrl = data.homeBackgroundUrl
          localStorage.setItem('user', JSON.stringify(this.user))
          console.log('✅ 用户背景图片加载成功:', data.homeBackgroundUrl)
        } else {
          console.log('🔍 用户未设置背景图片')
        }
      } catch (error) {
        console.error('❌ 加载用户背景图片失败:', error)
      }
    }
  },
  
  persist: {
    key: 'auth-store',
    storage: localStorage,
    paths: ['token', 'user'],
    // 添加恢复后的回调
    afterRestore: (ctx) => {
      console.log('🔍 AuthStore 持久化恢复完成')
      // 在恢复后立即同步 Token 到 http.js
      if (ctx.store.token) {
        setAuthToken(ctx.store.token)
        console.log('✅ 持久化恢复后 Token 已同步')
      }
    }
  }
})