<template>
  <div>
    <div v-for="c in comments" :key="c.id" class="card">
      <div style="font-weight:600">{{ c.user }}</div>
      <div style="margin:6px 0">{{ c.content }}</div>
      <div style="color:#888; font-size:12px;">{{ new Date(c.createdAt).toLocaleString() }}</div>
      <div v-if="canDelete(c)" class="row" style="margin-top:8px;">
        <button class="btn ghost" @click="$emit('delete', c)">删除</button>
      </div>
    </div>
    <div v-if="comments.length === 0" style="color:#888;">暂无评论</div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../store/auth'
const auth = useAuthStore()
const props = defineProps({
  comments: { type: Array, default: () => [] },
  articleAuthor: { type: String, default: '' }
})
function canDelete(c) {
  if (!auth.isAuthed) return false
  return c.user === (auth.user?.username) || props.articleAuthor === (auth.user?.username) || auth.user?.role === 'BLOGGER'
}
</script>


