<template>
  <div class="container editor-container">
    <!-- 页面头部 -->
    <div class="editor-header">
      <div class="header-content">
        <h1 class="page-title">{{ isEdit ? '编辑文章' : '创作新文章' }}</h1>
        <p class="page-subtitle">{{ isEdit ? '完善您的文章内容' : '分享您的想法与知识' }}</p>
      </div>
      <div class="header-actions">
        <button class="btn ghost-btn preview-btn" @click="previewArticle" :disabled="loading">
          <span class="btn-icon">👁️</span>
          预览文章
        </button>
      </div>
    </div>

    <!-- 错误提示 -->
    <transition name="fade-slide">
      <div v-if="error" class="error-message">
        <span class="error-icon">⚠️</span>
        <div class="error-content">
          <strong>操作失败</strong>
          <span>{{ error }}</span>
        </div>
        <button class="error-close" @click="error = ''">×</button>
      </div>
    </transition>

    <!-- 主要内容区域 -->
    <div class="editor-main">
      <!-- 左侧表单 -->
      <div class="form-section">
        <!-- 标题 -->
        <div class="form-group">
          <label class="form-label">
            <span class="label-text">文章标题</span>
            <span class="required-star">*</span>
          </label>
          <div class="input-wrapper">
            <input
                class="form-input"
                v-model="title"
                placeholder="请输入吸引人的文章标题..."
                :class="{ 'has-error': !title.trim() && submitted }"
            />
            <div class="input-decoration"></div>
          </div>
          <div v-if="!title.trim() && submitted" class="field-error">
            <span class="error-dot">●</span>
            标题不能为空
          </div>
        </div>

        <!-- 摘要 -->
        <div class="form-group">
          <label class="form-label">
            <span class="label-text">文章摘要</span>
            <span class="optional-tag">选填</span>
          </label>
          <div class="input-wrapper">
            <textarea
                class="form-textarea"
                rows="3"
                v-model="summary"
                placeholder="简要描述文章内容，帮助读者快速了解..."
            ></textarea>
            <div class="input-decoration"></div>
          </div>
          <div class="char-count">
            <span :class="{ 'near-limit': summary.length > 250 }">{{ summary.length }}</span>/300
          </div>
        </div>

        <!-- 封面图 -->
        <div class="form-group">
          <label class="form-label">
            <span class="label-text">封面图片</span>
            <span class="optional-tag">选填</span>
          </label>

          <!-- 上传控制 -->
          <div class="upload-section">
            <div class="upload-main">
              <label class="upload-btn">
                <input type="file" @change="onFile" hidden accept="image/*" />
                <span class="upload-icon">📷</span>
                <span class="upload-text">选择图片</span>
              </label>
              <div class="upload-divider">或</div>
              <input
                  class="url-input"
                  v-model="coverImageUrl"
                  placeholder="输入图片URL地址..."
              />
            </div>
            <button class="btn ghost-btn ai-btn" @click="gen" :disabled="loading">
              <span class="ai-icon">🤖</span>
              AI 生成
            </button>
          </div>

          <!-- 封面预览 -->
          <transition name="fade">
            <div v-if="coverImageUrl" class="cover-preview">
              <div class="preview-card">
                <img :src="coverImageUrl" alt="封面预览" class="preview-image" />
                <div class="preview-overlay">
                  <button class="preview-action" @click="coverImageUrl = ''">
                    <span class="action-icon">🗑️</span>
                    移除
                  </button>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <!-- 发布选项 -->
        <div class="publish-section">
          <div class="section-header">
            <h3 class="section-title">发布设置</h3>
          </div>

          <div class="publish-options">
            <label class="option-card" :class="{ active: !publishNow }">
              <div class="option-header">
                <input
                    type="radio"
                    v-model="publishNow"
                    :value="false"
                    class="option-radio"
                />
                <span class="custom-radio"></span>
                <div class="option-icon">📝</div>
              </div>
              <div class="option-content">
                <div class="option-title">保存为草稿</div>
                <div class="option-desc">仅自己可见，可稍后发布</div>
              </div>
            </label>

            <label class="option-card" :class="{ active: publishNow }">
              <div class="option-header">
                <input
                    type="radio"
                    v-model="publishNow"
                    :value="true"
                    class="option-radio"
                />
                <span class="custom-radio"></span>
                <div class="option-icon">🚀</div>
              </div>
              <div class="option-content">
                <div class="option-title">立即发布</div>
                <div class="option-desc">所有人可见，发布到首页</div>
              </div>
            </label>
          </div>

          <!-- 状态信息 -->
          <transition name="fade">
            <div v-if="isEdit" class="status-info">
              <div class="status-content">
                <span class="status-label">当前状态:</span>
                <span class="status-badge" :class="articlePublished ? 'published' : 'draft'">
                  {{ articlePublished ? '已发布' : '草稿' }}
                </span>
              </div>
              <button class="btn ghost-btn status-toggle" @click="togglePublishStatus">
                {{ articlePublished ? '转为草稿' : '立即发布' }}
              </button>
            </div>
          </transition>
        </div>
      </div>

      <!-- 右侧编辑器 -->
      <div class="editor-section">
        <div class="editor-header">
          <label class="form-label">
            <span class="label-text">正文内容</span>
            <span class="required-star">*</span>
          </label>
          <div class="editor-tools">
            <span class="char-count">
              <span :class="{ 'near-limit': content.length > 45000 }">{{ content.length }}</span>/50000
            </span>
          </div>
        </div>

        <!-- Markdown 编辑器 -->
        <div class="editor-wrapper" :class="{ 'has-error': !content.trim() && submitted }">
          <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              class="editor-toolbar"
          />
          <Editor
              v-model="editorValue"
              :defaultConfig="editorConfig"
              class="editor-content"
              @onCreated="handleCreated"
              @onChange="handleChange"
          />
        </div>
        <div v-if="!content.trim() && submitted" class="field-error">
          <span class="error-dot">●</span>
          正文内容不能为空
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-section">
      <div class="action-buttons">
        <button
            class="btn secondary-btn cancel-btn"
            @click="cancelEdit"
            :disabled="loading"
        >
          <span class="btn-icon">❌</span>
          取消
        </button>

        <div class="primary-actions">
          <button
              class="btn draft-btn"
              @click="saveAsDraft"
              :disabled="loading"
              :class="{ loading: loading && !publishNow }"
          >
            <span v-if="loading && !publishNow" class="loading-spinner"></span>
            <span v-else class="btn-icon">💾</span>
            {{ isEdit ? '保存草稿' : '保存草稿' }}
          </button>

          <button
              class="btn publish-btn"
              @click="publishArticle"
              :disabled="loading"
              :class="{ loading: loading && publishNow }"
          >
            <span v-if="loading && publishNow" class="loading-spinner"></span>
            <span v-else class="btn-icon">🚀</span>
            {{ isEdit ? '更新发布' : '发布文章' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 加载遮罩 -->
    <transition name="fade">
      <div v-if="loading" class="loading-overlay">
        <div class="loading-content">
          <div class="loading-spinner-large"></div>
          <p class="loading-text">{{ publishNow ? '发布中...' : '保存中...' }}</p>
          <p class="loading-tip">请耐心等待，不要关闭页面</p>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch, shallowRef, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { useAuthStore } from '../store/auth'

// 引入 wangEditor-next
import { Editor, Toolbar } from '@wangeditor-next/editor-for-vue'
import '@wangeditor-next/editor/dist/css/style.css'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const id = route.params.id
const isEdit = computed(() => !!id)

// 响应式数据
const title = ref('')
const summary = ref('')
const content = ref('')
const coverImageUrl = ref('')
const loading = ref(false)
const error = ref('')
const publishNow = ref(false)
const articlePublished = ref(false)
const submitted = ref(false)

// 编辑器相关
const editorRef = shallowRef()
const editorValue = ref('')

// 工具栏配置
const toolbarConfig = {
  excludeKeys: [
    'group-video',
    'insertTable',
    'codeBlock',
    'divider'
  ]
}

// 编辑器配置
const editorConfig = {
  placeholder: '开始撰写您的文章内容...',
  MENU_CONF: {
    uploadImage: {
      server: '/api/articles/upload',
      fieldName: 'file',
      maxFileSize: 5 * 1024 * 1024,
      headers: {
        Authorization: `Bearer ${auth.token}`
      },
      onSuccess(file, res) {
        console.log(`${file.name} 上传成功`, res)
      },
      // 自定义插入，处理后端非标准格式
      customInsert(res, insertFn) {
        if (res.url) {
          // 参数：url, alt, href
          insertFn(res.url, res.url, res.url)
        }
      },
      onFailed(file, res) {
        console.error(`${file.name} 上传失败`, res)
        error.value = '图片上传失败，请重试'
      },
      onError(file, err, res) {
        console.error(`${file.name} 上传出错`, err, res)
        error.value = '图片上传过程中发生错误'
      }
    }
  }
}

// 编辑器事件处理
function handleCreated(editor) {
  editorRef.value = editor
}

function handleChange(editor) {
  content.value = editor.getHtml()
}

// 监听内容长度
watch([title, summary], () => {
  if (summary.value.length > 300) {
    summary.value = summary.value.substring(0, 300)
  }
})

// 加载文章数据
async function load() {
  if (!isEdit.value) return
  try {
    const { data } = await http.get(`/api/articles/${id}`)
    title.value = data.title
    summary.value = data.summary
    content.value = data.content
    editorValue.value = data.content
    coverImageUrl.value = data.coverImageUrl || ''
    articlePublished.value = data.published
    publishNow.value = data.published
  } catch (err) {
    error.value = '加载文章失败: ' + (err.response?.data?.message || err.message)
  }
}

// 上传文件
async function onFile(e) {
  const f = e.target.files?.[0]
  if (!f) return

  // 文件验证
  if (f.size > 5 * 1024 * 1024) {
    error.value = '文件大小不能超过 5MB'
    return
  }

  if (!f.type.startsWith('image/')) {
    error.value = '请选择图片文件'
    return
  }

  try {
    const form = new FormData()
    form.append('file', f)
    const { data } = await http.post('/api/articles/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    coverImageUrl.value = data.url
    error.value = ''
  } catch (err) {
    error.value = '上传文件失败: ' + (err.response?.data?.message || err.message)
  }
}

// 表单验证
function validateForm() {
  submitted.value = true
  if (!title.value.trim()) {
    error.value = '请填写文章标题'
    return false
  }
  if (!content.value.trim()) {
    error.value = '请填写正文内容'
    return false
  }
  error.value = ''
  return true
}

// 保存为草稿
async function saveAsDraft() {
  if (!validateForm()) return
  await submitArticle(false)
}

// 发布文章
async function publishArticle() {
  if (!validateForm()) return
  await submitArticle(true)
}

// 提交文章
async function submitArticle(shouldPublish) {
  loading.value = true

  try {
    const payload = {
      title: title.value.trim(),
      summary: summary.value.trim(),
      content: content.value.trim(),
      coverImageUrl: coverImageUrl.value.trim(),
      published: shouldPublish
    }

    let response
    if (isEdit.value) {
      response = await http.put(`/api/articles/${id}`, payload)
    } else {
      response = await http.post('/api/articles', payload)
    }

    console.log('操作成功，文章ID:', response.data.id)

    if (isEdit.value) {
      alert(shouldPublish ? '文章更新并发布成功！' : '文章保存为草稿！')
    } else {
      if (shouldPublish) {
        router.replace(`/article/${response.data.id}`)
      } else {
        router.replace('/drafts')
      }
    }
  } catch (err) {
    console.error('操作失败:', err)
    if (err.response?.status === 401) {
      error.value = '认证已过期，请重新登录'
    } else if (err.response?.status === 403) {
      error.value = '权限不足，请联系管理员'
    } else {
      error.value = '操作失败: ' + (err.response?.data?.message || err.message || '未知错误')
    }
  } finally {
    loading.value = false
  }
}

// 切换发布状态
async function togglePublishStatus() {
  if (!id) return

  try {
    const {data} = await http.post(`/api/articles/${id}/toggle-publish`)
    articlePublished.value = data.published
    publishNow.value = data.published
    alert(data.message)
  } catch (err) {
    error.value = '切换发布状态失败: ' + (err.response?.data?.message || err.message)
  }
}

// 预览文章
function previewArticle() {
  if (!title.value.trim() || !content.value.trim()) {
    error.value = '请先填写标题和正文内容'
    return
  }

  const previewContent = `
    <html>
      <head>
        <title>${title.value} - 预览</title>
        <meta charset="utf-8">
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 40px 20px;
            line-height: 1.6;
            color: #2c3e50;
            background: #f8fafc;
          }
          .preview-container {
            background: white;
            border-radius: 16px;
            padding: 40px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
          }
          h1 {
            color: #2c3e50;
            border-bottom: 3px solid #3498db;
            padding-bottom: 16px;
            margin-bottom: 24px;
            font-size: 2.2rem;
          }
          .meta {
            color: #666;
            font-size: 14px;
            margin-bottom: 32px;
            padding: 16px;
            background: #f8f9fa;
            border-radius: 8px;
            border-left: 4px solid #3498db;
          }
          .content {
            line-height: 1.8;
            font-size: 16px;
          }
          .content img {
            max-width: 100%;
            height: auto;
            border-radius: 8px;
            margin: 16px 0;
          }
          .content h2, .content h3, .content h4 {
            margin: 24px 0 16px 0;
            color: #2c3e50;
          }
          .content p {
            margin-bottom: 16px;
          }
          .content blockquote {
            border-left: 4px solid #3498db;
            padding: 16px 20px;
            margin: 20px 0;
            background: #f8f9fa;
            border-radius: 0 8px 8px 0;
          }
          .content code {
            background: #f1f3f4;
            padding: 2px 6px;
            border-radius: 4px;
            font-family: 'Monaco', 'Menlo', monospace;
            font-size: 0.9em;
          }
          .content pre {
            background: #2c3e50;
            color: white;
            padding: 20px;
            border-radius: 8px;
            overflow: auto;
            margin: 20px 0;
          }
          .content ul, .content ol {
            margin: 16px 0;
            padding-left: 24px;
          }
          .content li {
            margin: 8px 0;
          }
        </style>
      </head>
      <body>
        <div class="preview-container">
          <h1>${title.value}</h1>
          <div class="meta">
            <strong>预览模式</strong> · 文章尚未保存 · ${new Date().toLocaleString()}
          </div>
          <div class="content">${content.value}</div>
        </div>
      </body>
    </html>
  `

  const previewWindow = window.open('', '_blank')
  previewWindow.document.write(previewContent)
  previewWindow.document.close()
}

// 取消编辑
function cancelEdit() {
  if (confirm('确定要离开吗？未保存的内容将会丢失。')) {
    router.back()
  }
}

// AI生成
async function gen() {
  try {
    const {data} = await http.post('/api/ai/generate', {
      prompt: title.value || '我的新文章',
      keywords: summary.value || ''
    })
    title.value = data.title
    summary.value = data.summary
    content.value = data.content
    editorValue.value = data.content
    error.value = ''
  } catch (err) {
    error.value = 'AI生成失败: ' + (err.response?.data?.message || err.message)
  }
}

// 权限检查
onMounted(() => {
  if (!auth.isAuthed) {
    alert('请先登录')
    router.replace('/login')
    return
  }

  if (!auth.isBlogger) {
    alert('只有博主可以发布和编辑文章')
    router.replace('/')
    return
  }
  load()
})

// 组件销毁时销毁编辑器
onBeforeUnmount(() => {
  if (editorRef.value == null) return
  editorRef.value.destroy()
})
</script>

<style scoped>
.editor-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
  min-height: 100vh;
  background: #f8fafc;
}

/* 页面头部 */
.editor-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 32px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
}

.editor-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.header-content {
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #fff 0%, #e3f2fd 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 400;
}

.header-actions {
  position: relative;
  z-index: 1;
}

/* 错误提示 */
.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 16px 20px;
  border-radius: 12px;
  margin: 24px 32px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.1);
}

.error-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.error-content strong {
  font-size: 0.9rem;
}

.error-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #dc2626;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.error-close:hover {
  background: rgba(220, 38, 38, 0.1);
}

/* 主要内容布局 */
.editor-main {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
  padding: 24px 32px;
  max-width: 1400px;
  margin: 0 auto;
}

.form-section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  height: fit-content;
  position: sticky;
  top: 24px;
}

.editor-section {
  background: white;
  border-radius: 16px;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.editor-header {
  padding: 24px 32px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 表单样式 */
.form-group {
  margin-bottom: 32px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #374151;
  font-size: 15px;
}

.label-text {
  font-weight: 600;
}

.required-star {
  color: #ef4444;
  font-size: 14px;
}

.optional-tag {
  background: #f3f4f6;
  color: #6b7280;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-sizing: border-box;
  font-family: inherit;
  background: white;
  position: relative;
  z-index: 1;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.form-input.has-error, .form-textarea.has-error {
  border-color: #ef4444;
}

.input-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(135deg, #3498db, #667eea);
  border-radius: 0 0 12px 12px;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.form-input:focus ~ .input-decoration,
.form-textarea:focus ~ .input-decoration {
  transform: scaleX(1);
}

.field-error {
  color: #ef4444;
  font-size: 14px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.error-dot {
  font-size: 8px;
}

.char-count {
  text-align: right;
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
  font-weight: 500;
}

.near-limit {
  color: #f59e0b;
  font-weight: 600;
}

/* 上传区域 */
.upload-section {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.upload-main {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8fafc;
  padding: 12px;
  border-radius: 12px;
  border: 2px dashed #cbd5e1;
}

.upload-btn {
  padding: 12px 20px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #475569;
  white-space: nowrap;
  flex-shrink: 0;
}

.upload-btn:hover {
  background: #3498db;
  border-color: #3498db;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.upload-divider {
  color: #9ca3af;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
}

.url-input {
  flex: 1;
  min-width: 120px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
}

.ai-btn {
  white-space: nowrap;
  flex-shrink: 0;
}

.ai-icon {
  font-size: 16px;
}

/* 封面预览 */
.cover-preview {
  margin-top: 16px;
}

.preview-card {
  position: relative;
  display: inline-block;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.preview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.preview-image {
  width: 200px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}

.preview-card:hover .preview-overlay {
  opacity: 1;
}

.preview-action {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.preview-action:hover {
  background: white;
  transform: scale(1.05);
}

.action-icon {
  font-size: 14px;
}

/* 发布选项 */
.publish-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
  margin-top: 32px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.publish-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  cursor: pointer;
  padding: 20px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: white;
}

.option-card:hover {
  border-color: #cbd5e1;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.option-card.active {
  border-color: #3498db;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f3ff 100%);
  box-shadow: 0 4px 15px rgba(52, 152, 219, 0.15);
}

.option-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.option-radio {
  display: none;
}

.custom-radio {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 50%;
  position: relative;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.option-card.active .custom-radio {
  border-color: #3498db;
  background: #3498db;
}

.option-card.active .custom-radio::after {
  content: '';
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.option-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.option-card.active .option-icon {
  background: #3498db;
  color: white;
}

.option-content {
  flex: 1;
}

.option-title {
  font-weight: 600;
  color: #374151;
  margin-bottom: 4px;
  font-size: 15px;
}

.option-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
}

/* 状态信息 */
.status-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  margin-top: 20px;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.published {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.draft {
  background: #fef3c7;
  color: #92400e;
}

.status-toggle {
  font-size: 13px;
  padding: 8px 16px;
}

/* 编辑器样式 */
.editor-toolbar {
  border-bottom: 1px solid #e5e7eb !important;
  padding: 12px 20px !important;
}

.editor-content {
  height: 600px !important;
}

.editor-wrapper.has-error {
  border: 2px solid #ef4444;
  border-radius: 12px;
  overflow: hidden;
}

/* 操作按钮区域 */
.action-section {
  padding: 24px 32px;
  background: white;
  border-top: 1px solid #f1f5f9;
  margin-top: 24px;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
}

.primary-actions {
  display: flex;
  gap: 16px;
}

.btn {
  padding: 14px 28px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 120px;
  position: relative;
  overflow: hidden;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.btn:hover::before {
  left: 100%;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn:disabled::before {
  display: none;
}

.secondary-btn {
  background: #6b7280;
  color: white;
}

.secondary-btn:hover:not(:disabled) {
  background: #4b5563;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(107, 114, 128, 0.3);
}

.draft-btn {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
}

.draft-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 158, 11, 0.4);
}

.publish-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.publish-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

.ghost-btn {
  background: transparent;
  border: 2px solid #e5e7eb;
  color: #6b7280;
}

.ghost-btn:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #3498db;
  color: #3498db;
  transform: translateY(-1px);
}

.preview-btn {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}

.preview-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.btn-icon {
  font-size: 16px;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.loading-content {
  background: white;
  padding: 40px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 300px;
  width: 90%;
}

.loading-spinner-large {
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

.loading-text {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.loading-tip {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 动画 */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .editor-main {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .form-section {
    position: static;
    order: 2;
  }

  .editor-section {
    order: 1;
  }
}

@media (max-width: 768px) {
  .editor-container {
    padding: 0;
  }

  .editor-header {
    padding: 30px 20px;
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
    text-align: center;
  }

  .editor-main {
    padding: 20px;
    gap: 16px;
  }

  .form-section {
    padding: 24px;
  }

  .upload-main {
    flex-direction: column;
    gap: 12px;
  }

  .upload-divider {
    display: none;
  }

  .upload-section {
    flex-direction: column;
  }

  .publish-options {
    flex-direction: column;
  }

  .action-buttons {
    flex-direction: column;
    gap: 16px;
  }

  .primary-actions {
    width: 100%;
  }

  .primary-actions .btn {
    flex: 1;
  }

  .status-info {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    text-align: center;
  }

  .error-message {
    margin: 16px 20px;
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }

  .error-content {
    align-items: center;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 2rem;
  }

  .form-section {
    padding: 20px;
  }

  .btn {
    padding: 12px 20px;
    font-size: 14px;
    min-width: 100px;
  }

  .editor-header {
    padding: 20px 16px;
  }

  .editor-main {
    padding: 16px;
  }
}

/* wangEditor 样式调整 */
:deep(.w-e-text-container) {
  padding: 20px !important;
  font-size: 15px !important;
  line-height: 1.7 !important;
}

:deep(.w-e-bar) {
  background: #f8fafc !important;
  border-bottom: 1px solid #e5e7eb !important;
}

:deep(.w-e-bar-item) {
  border-radius: 6px !important;
}

:deep(.w-e-bar-item button) {
  border-radius: 6px !important;
}

:deep(.w-e-bar-divider) {
  margin: 0 8px !important;
}
</style>