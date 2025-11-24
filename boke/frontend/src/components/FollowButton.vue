<template>
  <button
      class="follow-btn"
      :class="buttonClass"
      @click="handleFollow"
      :disabled="loading"
  >
    <span class="btn-content">
      <span v-if="loading" class="btn-spinner"></span>
      {{ buttonText }}
    </span>
  </button>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '../store/auth'
import http from '../api/http'

const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  size: {
    type: String,
    default: 'medium'
  },
  variant: {
    type: String,
    default: 'primary'
  }
})

const auth = useAuthStore()
const isFollowing = ref(false)
const loading = ref(false)
const checking = ref(true)

const isOwnProfile = computed(() => props.userId === auth.user?.id)
const canFollow = computed(() => auth.isAuthed && !isOwnProfile.value)

const buttonText = computed(() => {
  if (!auth.isAuthed) return '请登录'
  if (isOwnProfile.value) return '自己'
  return isFollowing.value ? '已关注' : '关注'
})

const buttonClass = computed(() => {
  const classes = ['follow-btn']
  classes.push(`size-${props.size}`)
  classes.push(`variant-${props.variant}`)
  if (isFollowing.value) classes.push('following')
  if (loading.value) classes.push('loading')
  if (!canFollow.value) classes.push('disabled')
  return classes
})

async function checkFollowStatus() {
  if (!auth.isAuthed || isOwnProfile.value) {
    checking.value = false
    return
  }

  try {
    const { data } = await http.get(`/api/follows/${props.userId}/is-following`)
    isFollowing.value = data.isFollowing
  } catch (error) {
    console.error('检查关注状态失败:', error)
  } finally {
    checking.value = false
  }
}

async function handleFollow() {
  if (!canFollow.value || loading.value) return

  loading.value = true
  try {
    if (isFollowing.value) {
      await http.delete(`/api/follows/${props.userId}`)
      isFollowing.value = false
    } else {
      await http.post(`/api/follows/${props.userId}`)
      isFollowing.value = true
    }
  } catch (error) {
    console.error('操作失败:', error)
    alert(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkFollowStatus()
})

watch(() => props.userId, () => {
  checkFollowStatus()
})
</script>

<style scoped>
.follow-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
  position: relative;
}

.size-small {
  padding: 6px 12px;
  font-size: 12px;
  min-width: 60px;
}

.size-medium {
  padding: 8px 16px;
  font-size: 14px;
  min-width: 80px;
}

.size-large {
  padding: 10px 20px;
  font-size: 16px;
  min-width: 100px;
}

.variant-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.variant-primary:not(.following):not(.disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.variant-outline {
  background: transparent;
  border: 2px solid #667eea;
  color: #667eea;
}

.variant-outline:not(.following):not(.disabled):hover {
  background: #667eea;
  color: white;
}

.following {
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e1e5e9;
}

.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading {
  pointer-events: none;
  opacity: 0.7;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-spinner {
  width: 12px;
  height: 12px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>