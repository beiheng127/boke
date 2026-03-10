<template>
  <div class="background-container" :style="backgroundStyle">
    <!-- 遮罩层 -->
    <div class="background-overlay"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()

// 计算属性：背景样式
const backgroundStyle = computed(() => {
  const backgroundUrl = auth.user?.homeBackgroundUrl
  console.log('当前背景图片URL:', backgroundUrl) // 调试用

  if (backgroundUrl) {
    return {
      backgroundImage: `url(${backgroundUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundAttachment: 'fixed'
    }
  }
  // 默认背景渐变
  return {
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  }
})
</script>

<style scoped>
.background-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1; /* 确保背景在所有内容下面 */
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.6); /* 遮罩层颜色和透明度 */
  backdrop-filter: blur(6px); /* 毛玻璃效果 */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .background-overlay {
    background: rgba(255, 255, 255, 0.65);
    backdrop-filter: blur(4px);
  }
}

@media (max-width: 480px) {
  .background-overlay {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(2px);
  }
}
</style>