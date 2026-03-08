<template>
  <div class="comments-container">
    <!-- 遍历评论列表 -->
    <div v-for="c in comments" :key="c.id" class="comment-card">
      <!-- 用户名 -->
      <div class="comment-author">{{ c.user }}</div>
      
      <!-- 评论内容 -->
      <div class="comment-content">{{ c.content }}</div>
      
      <!-- 评论时间和删除按钮 -->
      <div class="comment-meta">
        <span class="comment-time">{{ new Date(c.createdAt).toLocaleString() }}</span>
        
        <!-- 删除按钮，通过 canDelete 控制显示 -->
        <button 
          v-if="canDelete(c)" 
          class="delete-btn" 
          @click="$emit('delete', c)"
        >
          删除
        </button>
      </div>
    </div>

    <!-- 没有评论时的提示 -->
    <div v-if="comments.length === 0" class="no-comments">
      暂无评论，快来抢沙发吧~
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../store/auth'
const auth = useAuthStore()

// 定义组件属性
const props = defineProps({
  comments: { type: Array, default: () => [] },
  articleAuthor: { type: String, default: '' }
})

// 判断当前用户是否有权删除评论
function canDelete(c) {
  if (!auth.isAuthed) return false
  // 评论作者、文章作者或管理员可以删除
  return c.user === (auth.user?.username) || 
         props.articleAuthor === (auth.user?.username) || 
         auth.user?.role === 'BLOGGER'
}
</script>

<style scoped>
/* 评论列表容器 */
.comments-container {
  width: 100%;
  box-sizing: border-box;
}

/* 单个评论卡片 */
.comment-card {
  background-color: #fff;
  border: 1px solid #e5e7eb; /* 淡灰色边框 */
  border-radius: 8px; /* 圆角 */
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
  transition: box-shadow 0.2s ease-in-out;
}

/* 鼠标悬停在卡片上时增强阴影 */
.comment-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.08);
}

/* 评论作者 */
.comment-author {
  font-size: 15px;
  font-weight: 600;
  color: #111827; /* 深灰色文字 */
  margin-bottom: 8px;
}

/* 评论内容 */
.comment-content {
  font-size: 14px;
  color: #374151; /* 中灰色文字 */
  line-height: 1.6; /* 行高，提升可读性 */
  margin-bottom: 12px;
  word-break: break-word; /* 防止长单词或URL破坏布局 */
}

/* 评论元信息（时间和删除按钮） */
.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #6b7280; /* 浅灰色文字 */
}

/* 删除按钮 */
.delete-btn {
  background-color: transparent;
  color: #ef4444; /* 红色，突出删除操作 */
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
}

.delete-btn:hover {
  background-color: #fee2e2; /*  hover 时显示淡红色背景 */
}

/* 暂无评论提示 */
.no-comments {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af; /* 更淡的灰色 */
  font-size: 14px;
  background-color: #f9fafb; /* 非常淡的灰色背景 */
  border-radius: 8px;
  border: 1px dashed #e5e7eb; /* 虚线边框 */
}
</style>