import axios from 'axios'

const http = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 15000
})

// 定义不需要Token的公开API路径
const publicApis = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/auth/send-code',
    '/api/auth/send-reset-code',
    '/api/auth/verify-code',
    '/api/auth/reset-password'
]

// Token 管理
let currentToken = null

// 设置 Token 的方法
export const setAuthToken = (token) => {
    currentToken = token
    console.log('✅ Token 已设置:', token ? `长度 ${token.length}` : 'null')
}

// 获取 Token 的方法
const getAuthToken = () => {
    return currentToken || localStorage.getItem('token')
}

// 请求拦截器
http.interceptors.request.use(
    (config) => {
        // 检查是否为公开API
        const isPublicApi = publicApis.some(api => config.url.includes(api))

        console.log('🔍 请求拦截器 - 请求URL:', config.url)
        console.log('🔍 请求拦截器 - 请求方法:', config.method)
        console.log('🔍 请求拦截器 - 是否为公开API:', isPublicApi)

        if (isPublicApi) {
            console.log('✅ 公开API，跳过Token检查')
            return config
        }

        // 对于非公开API，检查Token
        const token = getAuthToken()
        console.log('🔍 请求拦截器 - 当前Token:', token ? `长度 ${token.length}` : 'null')

        if (token) {
            config.headers.Authorization = `Bearer ${token}`
            console.log('✅ 请求拦截器 - 已设置Authorization头')
        } else {
            console.warn('⚠️ 请求拦截器 - 未找到Token，但继续请求')
        }

        return config
    },
    (error) => {
        console.error('❌ 请求拦截器错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
http.interceptors.response.use(
    (response) => {
        console.log('✅ 响应成功:', response.config.url, response.status)
        return response
    },
    (error) => {
        const errorInfo = {
            url: error.config?.url || '未知URL',
            status: error.response?.status || '无状态码',
            data: error.response?.data || '无响应数据',
            message: error.message
        }

        console.error('❌ 响应错误:', errorInfo)

        if (!error.response) {
            console.error('❌ 网络错误或请求未发出:', error.message)
            return Promise.reject({
                message: '网络错误，请检查网络连接',
                code: 'NETWORK_ERROR'
            })
        }
        if (error.response?.status === 403 || error.response?.status === 401) {
            console.log('🔍 Token无效，执行登出')
            setAuthToken(null)
            localStorage.removeItem('token')
            localStorage.removeItem('user')

            if (window.location.pathname !== '/login') {
                window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
            }
        }
        return Promise.reject(error)
    }
)

export default http



