<template>
  <div class="comment-form-card">
    <textarea
      class="comment-input"
      rows="4"
      v-model="content"
      placeholder="分享你的观点..."
      :class="{ 'input-focus': isFocused }"
      @focus="isFocused = true"
      @blur="isFocused = false"
    ></textarea>
    <div class="form-actions">
      <button class="submit-btn" @click="submit" :disabled="loading || !content.trim()">
        <span v-if="loading" class="spinner"></span>
        <span v-else>发表评论</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  onSubmit: Function,
});

const loading = ref(false);
const content = ref('');
const isFocused = ref(false); // 用于处理输入框 focus 状态

async function submit() {
  if (!content.value.trim()) return;
  
  loading.value = true;
  try {
    await props.onSubmit?.(content.value.trim());
    content.value = ''; // 提交成功后清空输入框
  } catch (error) {
    console.error('发表评论失败:', error);
    // 使用更友好的方式提示错误，而不是 alert
    // 这里可以替换为项目中的全局通知组件
    alert('发表评论失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
/* 表单卡片容器 */
.comment-form-card {
  background-color: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s ease-in-out;
}

.comment-form-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.08);
}

/* 评论输入框 */
.comment-input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #d1d5db; /* 稍深一点的边框 */
  border-radius: 6px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical; /* 允许用户垂直 resize */
  transition: border-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.comment-input::placeholder {
  color: #9ca3af; /* 占位符颜色 */
}

/* 输入框聚焦状态 */
.comment-input:focus {
  outline: none;
  border-color: #3b82f6; /* 蓝色边框 */
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); /* 蓝色阴影 */
}

/* 表单操作区 (按钮) */
.form-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end; /* 按钮靠右 */
}

/* 提交按钮 */
.submit-btn {
  background-color: #3b82f6; /* 主色调：蓝色 */
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out, transform 0.1s ease-in-out;
  display: flex;
  align-items: center;
  gap: 8px; /* 图标和文字间距 */
}

/* 按钮 hover 状态 */
.submit-btn:hover:not(:disabled) {
  background-color: #2563eb; /* 更深的蓝色 */
}

/* 按钮 active 状态 */
.submit-btn:active:not(:disabled) {
  transform: scale(0.98);
}

/* 按钮 disabled 状态 */
.submit-btn:disabled {
  background-color: #9ca3af; /* 灰色 */
  color: #f3f4f6;
  cursor: not-allowed;
  transform: none; /* 取消 active 状态的缩放 */
}

/* 加载中动画 */
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #ffffff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>