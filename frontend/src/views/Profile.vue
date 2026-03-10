<template>
  <div class="container profile-container">
    <h2>个人资料</h2>
    <div class="card">
      <!-- 头像上传区域 -->
      <div class="avatar-section">
        <div class="avatar-preview">
          <img
              :src="avatarUrl || '/uploads/images/default/touxiang.jpg'"
              :alt="auth.user?.displayName"
              class="avatar-image"
          />
        </div>
        <div class="avatar-actions">
          <label class="file-upload-btn">
            <input type="file" accept="image/*" @change="onFile" hidden />
            📷 更换头像
          </label>
          <div class="upload-hint">支持 JPG、PNG 格式，大小不超过 2MB</div>
        </div>
      </div>

      <div class="divider"></div>

      <!-- 基本信息 -->
      <div class="form-section">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="form-value">{{ auth.user?.username }}</div>
        </div>

        <div class="form-group">
          <label class="form-label">角色</label>
          <div class="form-value">
            <span class="role-badge" :class="auth.user?.role?.toLowerCase()">
              {{ auth.user?.role === 'BLOGGER' ? '博主' : '读者' }}
            </span>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">昵称</label>
          <input
              class="form-input"
              v-model="displayName"
              placeholder="请输入昵称"
              :class="{ 'has-changed': displayNameChanged }"
          />
          <div v-if="displayNameChanged" class="change-hint">昵称已修改</div>
        </div>

        <div class="form-group">
          <label class="form-label">个性签名</label>
          <textarea
              class="form-textarea"
              rows="3"
              v-model="signature"
              placeholder="介绍一下自己..."
              :class="{ 'has-changed': signatureChanged }"
              maxlength="200"
          ></textarea>
          <div class="signature-info">
            <span v-if="signatureChanged" class="change-hint">个性签名已修改</span>
            <span class="char-count">{{ signature.length }}/200</span>
          </div>
        </div>
      </div>

      <div class="actions">
        <button
            class="btn primary-btn"
            @click="save"
            :disabled="saving || (!displayNameChanged && !signatureChanged)"
            :class="{ disabled: !displayNameChanged && !signatureChanged }"
        >
          {{ saving ? '保存中...' : '保存更改' }}
        </button>
        <button
            class="btn ghost-btn"
            @click="reset"
            :disabled="!displayNameChanged && !signatureChanged"
        >
          重置
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import http from '../api/http'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const displayName = ref('')
const signature = ref('')
const avatarUrl = ref('')
const saving = ref(false)
const loading = ref(false)

// 计算属性：检查字段是否被修改
const displayNameChanged = computed(() => {
  return displayName.value !== (auth.user?.displayName || auth.user?.username || '')
})

const signatureChanged = computed(() => {
  return signature.value !== (auth.user?.signature || '')
})

// 从服务器加载最新的用户信息
const loadUserProfile = async () => {
  loading.value = true
  try {
    const { data } = await http.get('/api/users/me')
    console.log('用户资料:', data)

    // 更新本地认证存储
    auth.user = { ...auth.user, ...data }
    localStorage.setItem('user', JSON.stringify(auth.user))

    // 初始化表单数据
    displayName.value = data.displayName || data.username || ''
    signature.value = data.signature || ''
    avatarUrl.value = data.avatarUrl || ''

  } catch (error) {
    console.error('加载用户资料失败:', error)
    // 如果API失败，使用本地存储的数据作为备选
    displayName.value = auth.user?.displayName || auth.user?.username || ''
    signature.value = auth.user?.signature || ''
    avatarUrl.value = auth.user?.avatarUrl || ''
  } finally {
    loading.value = false
  }
}

// 监听个性签名长度
watch(signature, (newValue) => {
  if (newValue.length > 200) {
    signature.value = newValue.substring(0, 200)
  }
})

// 初始化表单数据
onMounted(() => {
  loadUserProfile()
})

async function onFile(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // 文件大小检查 (2MB)
  if (file.size > 2 * 1024 * 1024) {
    alert('文件大小不能超过 2MB')
    return
  }

  // 文件类型检查
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  saving.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    const { data } = await http.post('/api/users/avatar', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    avatarUrl.value = data.avatarUrl
    // 更新本地用户信息
    auth.user = { ...auth.user, avatarUrl: data.avatarUrl }
    localStorage.setItem('user', JSON.stringify(auth.user))

    alert('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    alert('头像上传失败，请重试: ' + (error.response?.data?.message || error.message))
  } finally {
    saving.value = false
  }
}

async function save() {
  if (!displayName.value.trim()) {
    alert('昵称不能为空')
    return
  }

  saving.value = true
  try {
    const { data } = await http.put('/api/users/me', {
      displayName: displayName.value.trim(),
      signature: signature.value.trim()
    })

    // 更新本地用户信息
    auth.user = { ...auth.user, ...data }
    localStorage.setItem('user', JSON.stringify(auth.user))

    alert('资料保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败，请重试: ' + (error.response?.data?.message || error.message))
  } finally {
    saving.value = false
  }
}

function reset() {
  displayName.value = auth.user?.displayName || auth.user?.username || ''
  signature.value = auth.user?.signature || ''
}
</script>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.avatar-preview {
  flex-shrink: 0;
}

.avatar-image {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
}

.avatar-actions {
  flex: 1;
}

.file-upload-btn {
  display: inline-block;
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.file-upload-btn:hover {
  background: #2980b9;
  transform: translateY(-1px);
}

.upload-hint {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 24px 0;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.form-label {
  width: 80px;
  font-weight: 500;
  color: #333;
  padding-top: 8px;
  flex-shrink: 0;
}

.form-value {
  flex: 1;
  padding: 8px 0;
  color: #666;
}

.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.blogger {
  background: #e3f2fd;
  color: #1976d2;
}

.role-badge.viewer {
  background: #f3e5f5;
  color: #7b1fa2;
}

.form-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  border-color: #3498db;
}

.form-input.has-changed {
  border-color: #27ae60;
  background: rgba(39, 174, 96, 0.02);
}

.form-textarea {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.3s ease;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.form-textarea:focus {
  border-color: #3498db;
}

.form-textarea.has-changed {
  border-color: #27ae60;
  background: rgba(39, 174, 96, 0.02);
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.primary-btn {
  background: #3498db;
  color: white;
}

.primary-btn:hover:not(.disabled) {
  background: #2980b9;
  transform: translateY(-1px);
}

.primary-btn.disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

.ghost-btn {
  background: transparent;
  border: 1px solid #e1e5e9;
  color: #666;
}

.ghost-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #ccc;
}

.ghost-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 修改提示 */
.change-hint {
  color: #27ae60;
  font-size: 12px;
  margin-top: 4px;
}

.signature-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.char-count {
  font-size: 12px;
  color: #888;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .card {
    padding: 20px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .form-group {
    flex-direction: column;
    gap: 8px;
  }

  .form-label {
    width: auto;
    padding-top: 0;
  }

  .actions {
    flex-direction: column;
  }
}
</style>