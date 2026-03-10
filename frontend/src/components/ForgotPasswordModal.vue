<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="forgot-password-modal">
      <div class="modal-header">
        <h2>重置密码</h2>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="modal-content">
        <!-- 步骤1：输入邮箱 -->
        <div v-if="currentStep === 1" class="step step-1">
          <div class="step-icon">📧</div>
          <h3>输入您的邮箱</h3>
          <p>我们将向该邮箱发送验证码</p>

          <div class="input-group">
            <input
                class="form-input"
                v-model="email"
                placeholder="请输入注册时使用的邮箱"
                type="email"
            />
            <span class="input-icon">📧</span>
          </div>

          <button
              class="btn-primary next-btn"
              @click="sendResetCode"
              :disabled="!isEmailValid || sendingCode"
          >
            {{ sendingCode ? '发送中...' : '发送验证码' }}
          </button>
        </div>

        <!-- 步骤2：输入验证码和新密码 -->
        <div v-else-if="currentStep === 2" class="step step-2">
          <div class="step-icon">🔐</div>
          <h3>设置新密码</h3>
          <p>我们已向 {{ email }} 发送了验证码，请填写验证码和新密码</p>

          <!-- 验证码输入 -->
          <div class="input-group">
            <input
                class="form-input"
                v-model="verificationCode"
                placeholder="请输入6位验证码"
                maxlength="6"
            />
            <span class="input-icon">🔢</span>
          </div>

          <!-- 新密码输入 -->
          <div class="input-group">
            <input
                class="form-input"
                v-model="newPassword"
                type="password"
                placeholder="新密码"
            />
            <span class="input-icon">🔒</span>
          </div>

          <!-- 确认密码输入 -->
          <div class="input-group">
            <input
                class="form-input"
                v-model="confirmPassword"
                type="password"
                placeholder="确认新密码"
            />
            <span class="input-icon">🔒</span>
          </div>

          <!-- 密码要求 -->
          <div class="password-requirements">
            <p>密码要求：</p>
            <ul>
              <li :class="{ 'valid': newPassword.length >= 6 }">至少6个字符</li>
              <li :class="{ 'valid': newPassword && confirmPassword && newPassword === confirmPassword }">两次输入的密码一致</li>
            </ul>
          </div>

          <!-- 重新发送验证码 -->
          <div class="resend-code">
            <span>没有收到验证码？</span>
            <button
                class="resend-btn"
                @click="sendResetCode"
                :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}s后重新发送` : '重新发送' }}
            </button>
          </div>

          <div class="step-actions">
            <button class="btn-ghost" @click="currentStep = 1">上一步</button>
            <button
                class="btn-primary"
                @click="resetPassword"
                :disabled="!isFormValid || resetting"
            >
              {{ resetting ? '重置中...' : '重置密码' }}
            </button>
          </div>
        </div>

        <!-- 步骤3：完成 -->
        <div v-else class="step step-3">
          <div class="step-icon success">✅</div>
          <h3>密码重置成功</h3>
          <p>您的密码已成功重置，现在可以使用新密码登录</p>

          <button class="btn-primary" @click="completeReset">
            立即登录
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
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const emit = defineEmits(['close', 'success'])

const router = useRouter()

const currentStep = ref(1)
const email = ref('')
const verificationCode = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const sendingCode = ref(false)
const resetting = ref(false)
const errorMessage = ref('')
const countdown = ref(0)

// 计算属性
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email.value)
})

const isFormValid = computed(() => {
  return verificationCode.value.length === 6 &&
      newPassword.value.length >= 6 &&
      newPassword.value === confirmPassword.value
})

// 发送重置验证码
async function sendResetCode() {
  if (!isEmailValid.value) {
    errorMessage.value = '请输入有效的邮箱地址'
    return
  }

  sendingCode.value = true
  errorMessage.value = ''

  try {
    const { data } = await http.post('/api/auth/send-code', {
      email: email.value,
      type: 'RESET_PASSWORD'
    })

    // 发送验证码成功后直接进入第二步
    currentStep.value = 2
    startCountdown()
    console.log('✅ 重置验证码发送成功')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '发送验证码失败'
  } finally {
    sendingCode.value = false
  }
}

// 重置密码（一次性验证验证码和重置密码）
async function resetPassword() {
  if (!isFormValid.value) {
    errorMessage.value = '请确保所有信息填写正确'
    return
  }

  resetting.value = true
  errorMessage.value = ''

  try {
    console.log('🔍 发送重置密码请求:', {
      email: email.value,
      code: verificationCode.value,
      newPassword: newPassword.value,
      newPasswordLength: newPassword.value.length
    })

    const { data } = await http.post('/api/auth/reset-password', {
      email: email.value,
      code: verificationCode.value,
      newPassword: newPassword.value
    })

    console.log('✅ 重置密码成功响应:', data)
    currentStep.value = 3
    console.log('✅ 密码重置成功')
  } catch (error) {
    console.error('❌ 重置密码失败:', error)
    const errorMsg = error.response?.data?.message || '密码重置失败'
    errorMessage.value = errorMsg
    console.log('❌ 错误详情:', {
      status: error.response?.status,
      data: error.response?.data
    })
  } finally {
    resetting.value = false
  }
}

// 完成重置
function completeReset() {
  emit('success')
  emit('close')
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
</script>

<style scoped>
/* 样式保持不变，与之前相同 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.forgot-password-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 450px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.3s ease;
}

.close-btn:hover {
  background: #f0f0f0;
}

.modal-content {
  padding: 24px;
}

.step {
  text-align: center;
}

.step-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.step-icon.success {
  color: #27ae60;
}

.step h3 {
  margin: 0 0 8px 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
}

.step p {
  margin: 0 0 24px 0;
  color: #666;
  line-height: 1.5;
}

.input-group {
  position: relative;
  margin-bottom: 20px;
}

.form-input {
  width: 80%;
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

.btn-primary {
  width: 100%;
  padding: 14px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-ghost {
  padding: 12px 20px;
  background: transparent;
  color: #666;
  border: 2px solid #e1e5e9;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
}

.btn-ghost:hover {
  background: #f8f9fa;
  border-color: #667eea;
  color: #667eea;
}

.resend-code {
  margin: 16px 0;
  font-size: 0.9rem;
  color: #666;
}

.resend-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 0.9rem;
  margin-left: 8px;
}

.resend-btn:hover:not(:disabled) {
  text-decoration: underline;
}

.resend-btn:disabled {
  color: #999;
  cursor: not-allowed;
}

.step-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.password-requirements {
  text-align: left;
  margin: 16px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.password-requirements p {
  margin: 0 0 8px 0;
  font-weight: 500;
  color: #2c3e50;
}

.password-requirements ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.password-requirements li {
  margin-bottom: 4px;
}

.password-requirements li.valid {
  color: #27ae60;
}

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
@media (max-width: 480px) {
  .modal-overlay {
    padding: 16px;
  }

  .forgot-password-modal {
    max-height: 90vh;
  }

  .modal-header {
    padding: 20px;
  }

  .modal-content {
    padding: 20px;
  }

  .step-actions {
    flex-direction: column;
  }
}
</style>