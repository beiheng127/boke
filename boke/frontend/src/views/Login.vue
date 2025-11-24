<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1 class="brand-title">个人博客</h1>
          <p class="brand-subtitle">分享知识，记录生活</p>
          <div class="brand-decoration">
            <div class="decoration-circle circle-1"></div>
            <div class="decoration-circle circle-2"></div>
            <div class="decoration-circle circle-3"></div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="form-container">
          <!-- 登录表单 -->
          <div class="form-group">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">请登录您的账户</p>

            <div class="input-group">
              <input
                  class="form-input"
                  v-model="loginUsername"
                  placeholder="用户名"
                  type="text"
              />
              <span class="input-icon">👤</span>
            </div>

            <div class="input-group">
              <input
                  class="form-input"
                  v-model="loginPassword"
                  type="password"
                  placeholder="密码"
              />
              <span class="input-icon">🔒</span>
            </div>

            <!-- 滑块验证 -->
            <div v-if="showCaptcha" class="captcha-section">
              <SliderCaptcha
                  @success="onCaptchaVerified"
                  @fail="onCaptchaFail"
              />
            </div>

            <button
                class="btn-primary login-btn"
                @click="doLogin"
                :disabled="loggingIn || !canLogin"
                :class="{ 'loading': loggingIn }"
            >
              <span class="btn-content">
                <span class="btn-spinner" v-if="loggingIn"></span>
                {{ loggingIn ? '登录中...' : '登录' }}
              </span>
            </button>

            <!-- 忘记密码链接 -->
            <div class="forgot-password">
              <a href="#" @click.prevent="showForgotPassword = true">忘记密码？</a>
            </div>
          </div>

          <!-- 分割线 -->
          <div class="divider">
            <span class="divider-text">或</span>
          </div>

          <!-- 注册表单 -->
          <div class="form-group">
            <h3 class="register-title">创建新账户</h3>

            <div class="input-group">
              <input
                  class="form-input"
                  v-model="regUsername"
                  placeholder="用户名"
              />
              <span class="input-icon">👤</span>
            </div>

            <div class="input-group">
              <input
                  class="form-input"
                  v-model="regEmail"
                  placeholder="邮箱"
                  type="email"
                  @blur="validateRegEmail"
              />
              <span class="input-icon">📧</span>
              <div v-if="regEmailError" class="error-text-small">{{ regEmailError }}</div>
            </div>

            <!-- 邮箱验证码部分 - 始终显示，但初始隐藏验证码输入 -->
            <div class="verification-section">
              <div class="verification-input-group">
                <input
                    class="form-input verification-input"
                    v-model="verificationCode"
                    placeholder="请输入邮箱验证码"
                    maxlength="6"
                    :disabled="!isRegEmailValid"
                />
                <button
                    class="send-code-btn"
                    @click="sendVerificationCode"
                    :disabled="!isRegEmailValid || sendingCode || countdown > 0"
                >
                  {{ countdown > 0 ? `${countdown}s后重新发送` : (sendingCode ? '发送中...' : '发送验证码') }}
                </button>
              </div>
              <div v-if="verificationHint" class="verification-hint">
                {{ verificationHint }}
              </div>
            </div>

            <div class="input-group">
              <input
                  class="form-input"
                  v-model="regPassword"
                  type="password"
                  placeholder="密码"
              />
              <span class="input-icon">🔒</span>
            </div>

            <div class="input-group">
              <input
                  class="form-input"
                  v-model="regDisplayName"
                  placeholder="显示昵称"
              />
              <span class="input-icon">😊</span>
            </div>

            <div class="role-info">
              <div class="info-icon">ℹ️</div>
              <div class="info-content">
                <p>注册后将获得 <strong class="role-tag">普通用户</strong> 权限</p>
                <p class="info-hint">如需博主权限，请联系管理员</p>
              </div>
            </div>

            <button
                class="btn-secondary register-btn"
                @click="doRegister"
                :disabled="registering || !canRegister"
                :class="{ 'loading': registering }"
            >
            <span class="btn-content">
              <span class="btn-spinner" v-if="registering"></span>
              {{ registering ? '注册中...' : '注册并登录' }}
            </span>
            </button>
          </div>

          <!-- 错误信息 -->
          <div v-if="errorMessage" class="error-message">
            <span class="error-icon">⚠️</span>
            {{ errorMessage }}
          </div>
        </div>
      </div>
    </div>

    <!-- 忘记密码模态框 -->
    <ForgotPasswordModal
        v-if="showForgotPassword"
        @close="showForgotPassword = false"
        @success="onPasswordResetSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import SliderCaptcha from '../components/SliderCaptcha.vue'
import ForgotPasswordModal from '../components/ForgotPasswordModal.vue'
import http from '../api/http'

const loginUsername = ref('')
const loginPassword = ref('')
const regUsername = ref('')
const regEmail = ref('')
const regPassword = ref('')
const regDisplayName = ref('')
const verificationCode = ref('')
const loggingIn = ref(false)
const registering = ref(false)
const sendingCode = ref(false)
const errorMessage = ref('')
const showCaptcha = ref(false)
const captchaVerified = ref(false)
const showForgotPassword = ref(false)
const countdown = ref(0)
const regEmailError = ref('')
const verificationHint = ref('')

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

// 计算属性
const isRegEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(regEmail.value) && !regEmailError.value
})

const canRegister = computed(() => {
  return regUsername.value &&
      regEmail.value &&
      regPassword.value &&
      regDisplayName.value &&
      verificationCode.value &&
      isRegEmailValid.value &&
      regPassword.value.length >= 6
})

// 修复：添加登录按钮可用性计算属性
const canLogin = computed(() => {
  const hasCredentials = loginUsername.value && loginPassword.value
  if (showCaptcha.value) {
    return hasCredentials && captchaVerified.value
  }
  return hasCredentials
})

onMounted(() => {
  if (route.query.redirect) {
    console.log('🔍 检测到重定向参数:', route.query.redirect)
  }
})

// 监听登录表单变化，决定是否显示验证码
watch([loginUsername, loginPassword], ([username, password]) => {
  if (username && password && !showCaptcha.value) {
    // 当用户开始输入登录信息时显示验证码
    showCaptcha.value = true
    captchaVerified.value = false
  }
}, { immediate: false })

// 验证注册邮箱
function validateRegEmail() {
  if (!regEmail.value) {
    regEmailError.value = '邮箱不能为空'
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(regEmail.value)) {
    regEmailError.value = '请输入有效的邮箱地址'
    return
  }

  regEmailError.value = ''
}

// 滑块验证相关
function onCaptchaVerified() {
  captchaVerified.value = true
  console.log('✅ 滑块验证通过，当前状态:', {
    showCaptcha: showCaptcha.value,
    captchaVerified: captchaVerified.value,
    canLogin: canLogin.value
  })
}

function onCaptchaFail() {
  captchaVerified.value = false
  errorMessage.value = '验证失败，请重试'
  console.log('❌ 滑块验证失败')
}

// 发送验证码
async function sendVerificationCode() {
  console.log('🔍 开始发送验证码，邮箱:', regEmail.value)

  if (!isRegEmailValid.value) {
    errorMessage.value = '请输入有效的邮箱地址'
    return
  }

  sendingCode.value = true
  errorMessage.value = ''

  try {
    console.log('📧 发送验证码请求:', {
      email: regEmail.value,
      type: 'REGISTER'
    })

    const { data } = await http.post('/api/auth/send-code', {
      email: regEmail.value,
      type: 'REGISTER'
    })

    console.log('✅ 验证码发送成功响应:', data)
    verificationHint.value = `验证码已发送到 ${regEmail.value}，请查收`
    startCountdown()
  } catch (error) {
    console.error('❌ 验证码发送失败:', error)
    const errorMsg = error.response?.data?.message || '发送验证码失败'
    errorMessage.value = errorMsg
    // 如果是邮箱已注册的错误，设置邮箱错误
    if (errorMsg.includes('邮箱已被注册')) {
      regEmailError.value = errorMsg
    }
  } finally {
    sendingCode.value = false
  }
}

// 倒计时
function startCountdown() {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

async function doLogin() {
  console.log('🔍 开始登录，验证状态:', {
    showCaptcha: showCaptcha.value,
    captchaVerified: captchaVerified.value,
    canLogin: canLogin.value
  })

  if (!loginUsername.value || !loginPassword.value) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  // 如果需要验证码但未验证
  if (showCaptcha.value && !captchaVerified.value) {
    errorMessage.value = '请完成验证'
    return
  }

  loggingIn.value = true
  errorMessage.value = ''

  try {
    const result = await auth.login(loginUsername.value, loginPassword.value)

    if (result.success) {
      console.log('🔍 登录成功，准备跳转')
      const redirectPath = route.query.redirect || '/'
      console.log('🔍 最终跳转路径:', redirectPath)

      setTimeout(() => {
        router.replace(redirectPath)
      }, 100)
    } else {
      errorMessage.value = result.message || '登录失败'
      // 登录失败时显示验证码并重置验证状态
      showCaptcha.value = true
      captchaVerified.value = false
      console.log('❌ 登录失败，重置验证状态')
    }
  } catch (error) {
    console.error('登录异常:', error)
    errorMessage.value = '登录失败，请检查用户名和密码'
    showCaptcha.value = true
    captchaVerified.value = false
  } finally {
    loggingIn.value = false
  }
}

async function doRegister() {
  if (!regUsername.value || !regEmail.value || !regPassword.value || !regDisplayName.value) {
    errorMessage.value = '请填写所有必填字段'
    return
  }

  if (!isRegEmailValid.value) {
    errorMessage.value = '请输入有效的邮箱地址'
    return
  }

  if (regPassword.value.length < 6) {
    errorMessage.value = '密码长度至少6位'
    return
  }

  if (!verificationCode.value) {
    errorMessage.value = '请输入验证码'
    return
  }

  registering.value = true
  errorMessage.value = ''

  try {
    const { data } = await http.post('/api/auth/register', {
      username: regUsername.value,
      email: regEmail.value,
      password: regPassword.value,
      displayName: regDisplayName.value,
      verificationCode: verificationCode.value
    })

    // 注册成功后自动登录
    const loginResult = await auth.login(regUsername.value, regPassword.value)

    if (loginResult.success) {
      console.log('🔍 注册并登录成功，准备跳转')
      const redirectPath = route.query.redirect || '/'
      router.replace(redirectPath)
    } else {
      errorMessage.value = '注册成功但自动登录失败，请手动登录'
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    registering.value = false
  }
}

function onPasswordResetSuccess() {
  showForgotPassword.value = false
  errorMessage.value = '密码重置成功，请使用新密码登录'
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  max-width: 900px;
  width: 100%;
  min-height: 600px;
  overflow: hidden;
}

/* 品牌区域 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-content {
  text-align: center;
  z-index: 2;
  position: relative;
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.brand-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 40px;
}

.brand-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 100px;
  height: 100px;
  top: 10%;
  left: 20%;
}

.circle-2 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  right: 15%;
}

.circle-3 {
  width: 80px;
  height: 80px;
  bottom: 10%;
  left: 10%;
}

/* 表单区域 */
.form-section {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
}

.form-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 40px;
}

.form-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 8px;
}

.form-subtitle {
  color: #666;
  margin-bottom: 30px;
  font-size: 0.95rem;
}

.register-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

/* 输入框组 */
.input-group {
  position: relative;
  margin-bottom: 20px;
}

.form-input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 2px solid #e1e5e9;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #fafbfc;
  outline: none;
}

.form-input:focus {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: #666;
}

/* 按钮 */
.btn-primary, .btn-secondary {
  width: 100%;
  padding: 14px 20px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  outline: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover:not(:disabled) {
  background: #667eea;
  color: white;
  transform: translateY(-1px);
}

.btn-primary:disabled, .btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分割线 */
.divider {
  position: relative;
  text-align: center;
  margin: 30px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e1e5e9;
}

.divider-text {
  background: white;
  padding: 0 16px;
  color: #666;
  font-size: 0.9rem;
}

/* 角色信息 */
.role-info {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 16px;
  margin: 20px 0;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.info-icon {
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}

.info-content {
  flex: 1;
}

.info-content p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.4;
}

.role-tag {
  color: #667eea;
  font-weight: 600;
}

.info-hint {
  color: #666;
  font-size: 0.85rem !important;
  margin-top: 4px !important;
}

/* 错误信息 */
.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
}

.error-icon {
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    max-width: 400px;
  }

  .brand-section {
    padding: 40px 20px;
    min-height: 200px;
  }

  .brand-title {
    font-size: 2rem;
  }

  .form-section {
    padding: 40px 20px;
  }

  .form-container {
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 10px;
  }

  .login-card {
    border-radius: 16px;
  }

  .brand-section {
    padding: 30px 20px;
  }

  .brand-title {
    font-size: 1.75rem;
  }

  .form-section {
    padding: 30px 20px;
  }

  .form-title {
    font-size: 1.5rem;
  }
}

/* 加载状态动画 */
.loading {
  pointer-events: none;
}

.loading .btn-content {
  opacity: 0.7;
}

.captcha-section {
  margin: 20px 0;
}

.forgot-password {
  text-align: center;
  margin-top: 16px;
}

.forgot-password a {
  color: #667eea;
  text-decoration: none;
  font-size: 0.9rem;
}

.forgot-password a:hover {
  text-decoration: underline;
}

.verification-section {
  margin: 16px 0;
}

.verification-input-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.verification-input {
  flex: 1;
}

.send-code-btn {
  padding: 12px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.send-code-btn:hover:not(:disabled) {
  background: #5a6fd8;
}

.send-code-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .verification-input-group {
    flex-direction: column;
  }

  .send-code-btn {
    width: 100%;
  }
}

.error-text-small {
  color: #e74c3c;
  font-size: 0.8rem;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.error-text-small::before {
  content: "⚠️";
  font-size: 0.7rem;
}

.verification-hint {
  font-size: 0.8rem;
  color: #27ae60;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.verification-hint::before {
  content: "ℹ️";
  font-size: 0.7rem;
}

.verification-section {
  margin: 16px 0;
}

.verification-input-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.verification-input {
  flex: 1;
}

.send-code-btn {
  padding: 12px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.send-code-btn:hover:not(:disabled) {
  background: #5a6fd8;
}

.send-code-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .verification-input-group {
    flex-direction: column;
  }

  .send-code-btn {
    width: 100%;
  }
}
</style>