<template>
  <img
      :src="avatarUrl"
      :alt="altText"
      @error="handleAvatarError"
      class="user-avatar"
  />
</template>

<script setup>
import { computed } from 'vue'
import { fixAvatarUrl, handleAvatarError } from '@/utils/avatar'

const props = defineProps({
  avatarPath: {
    type: String,
    default: ''
  },
  altText: {
    type: String,
    default: '用户头像'
  }
})

const avatarUrl = computed(() => {
  return fixAvatarUrl(props.avatarPath)
})
</script>

<style scoped>
.user-avatar {
  border-radius: 50%;
  object-fit: cover;
}
</style>