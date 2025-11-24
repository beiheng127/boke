<template>
  <div class="slider-captcha">
    <div class="captcha-container">
      <!-- 验证图片 -->
      <div class="captcha-image" ref="captchaImage">
        <img :src="backgroundImage" alt="验证背景" class="background" />
        <div
            class="slider-piece"
            :style="{
            backgroundImage: `url(${sliderImage})`,
            left: `${sliderPosition}px`,
            top: `${correctPosition.y}px`
          }"
        ></div>
      </div>

      <!-- 滑块轨道 -->
      <div class="slider-track" ref="sliderTrack">
        <div
            class="slider"
            ref="slider"
            :style="{ left: `${sliderValue}px` }"
            @mousedown="startDrag"
            @touchstart="startDrag"
        >
          <div class="slider-handle">
            <span class="slider-icon">→</span>
          </div>
        </div>
        <div class="slider-text">{{ sliderText }}</div>
      </div>

      <!-- 验证提示 -->
      <div v-if="showHint" class="captcha-hint" :class="hintClass">
        {{ hintText }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

// 使用标准的 Vue 事件定义
const emit = defineEmits(['success', 'fail'])

// 状态
const captchaImage = ref(null)
const sliderTrack = ref(null)
const slider = ref(null)
const isDragging = ref(false)
const sliderValue = ref(0)
const sliderPosition = ref(0)
const showHint = ref(false)
const hintText = ref('')
const isVerified = ref(false)

// 验证参数
const correctPosition = ref({ x: 0, y: 0 })
const backgroundImage = ref('')
const sliderImage = ref('')
const tolerance = 10 // 容错范围

// 计算属性
const hintClass = computed(() => {
  return isVerified.value ? 'success' : 'error'
})

// 初始化验证码
function initCaptcha() {
  // 使用本地图片，避免网络请求
  backgroundImage.value = '/uploads/Slider/slider.jpg'
  sliderImage.value = '/uploads/Slider/slider.jpg' // 使用同一张图片作为滑块

  // 随机生成正确位置
  const trackWidth = sliderTrack.value?.offsetWidth || 300
  const maxX = trackWidth - 50 // 滑块宽度
  const maxY = 150 // 最大Y偏移

  correctPosition.value = {
    x: Math.floor(Math.random() * (maxX - 50)) + 25,
    y: Math.floor(Math.random() * maxY)
  }

  sliderPosition.value = correctPosition.value.x
  resetCaptcha()
}

function resetCaptcha() {
  isDragging.value = false
  sliderValue.value = 0
  showHint.value = false
  isVerified.value = false
  sliderPosition.value = correctPosition.value.x
}

const sliderText = ref('拖动滑块完成验证')

// 开始拖动
function startDrag(e) {
  if (isVerified.value) return

  e.preventDefault()
  isDragging.value = true
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', stopDrag)
}

// 拖动中
function onDrag(e) {
  if (!isDragging.value) return

  e.preventDefault()
  const trackRect = sliderTrack.value.getBoundingClientRect()
  let clientX

  if (e.type.includes('touch')) {
    clientX = e.touches[0].clientX
  } else {
    clientX = e.clientX
  }

  let newValue = clientX - trackRect.left
  newValue = Math.max(0, Math.min(newValue, trackRect.width - 40))

  sliderValue.value = newValue
}

// 停止拖动
function stopDrag() {
  if (!isDragging.value) return

  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)

  // 验证
  validateCaptcha()
}

// 验证
function validateCaptcha() {
  const diff = Math.abs(sliderValue.value - correctPosition.value.x)

  if (diff <= tolerance) {
    // 验证成功
    isVerified.value = true
    sliderText.value = '验证成功 ✓'
    showHint.value = true
    hintText.value = '验证成功！'
    emit('success') // 使用标准事件
  } else {
    // 验证失败
    showHint.value = true
    hintText.value = '验证失败，请重试'
    sliderText.value = '验证失败，请重试'
    setTimeout(() => {
      resetCaptcha()
      emit('fail') // 使用标准事件
    }, 1000)
  }
}

onMounted(() => {
  // 延迟初始化，确保DOM已渲染
  setTimeout(() => {
    initCaptcha()
  }, 100)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
})
</script>

<style scoped>
.slider-captcha {
  margin: 20px 0;
}

.captcha-container {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e1e5e9;
}

.captcha-image {
  position: relative;
  width: 100%;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  background: #f0f0f0;
}

.background {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slider-piece {
  position: absolute;
  width: 50px;
  height: 50px;
  background-size: cover;
  border: 2px solid #3498db;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.slider-track {
  position: relative;
  width: 100%;
  height: 40px;
  background: #e9ecef;
  border-radius: 20px;
  border: 1px solid #dee2e6;
}

.slider {
  position: absolute;
  left: 0;
  top: 0;
  width: 40px;
  height: 40px;
  cursor: grab;
  z-index: 2;
}

.slider-handle {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.slider:hover .slider-handle {
  transform: scale(1.05);
}

.slider:active .slider-handle {
  cursor: grabbing;
  transform: scale(0.95);
}

.slider-icon {
  font-size: 16px;
}

.slider-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #666;
  font-size: 14px;
  pointer-events: none;
  z-index: 1;
}

.captcha-hint {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}

.captcha-hint.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.captcha-hint.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
</style>