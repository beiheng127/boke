<template>
  <div class="card">
    <textarea class="input" rows="4" v-model="content" placeholder="说点什么吧..."></textarea>
    <div class="space"></div>
    <button class="btn" @click="submit" :disabled="loading || !content.trim()">发表评论</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({ onSubmit: Function })
const loading = ref(false)
const content = ref('')

async function submit() {
  if (!content.value.trim()) return
  loading.value = true
  try {
    await props.onSubmit?.(content.value.trim())
    content.value = ''
  } catch (error) {
    console.error('发表评论失败:', error)
    alert('发表评论失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}
</script>


