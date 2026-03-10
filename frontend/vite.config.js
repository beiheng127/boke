import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 允许访问项目根目录外的文件
    fs: {
      allow: ['..', '../..', path.resolve(__dirname, '../../uploads')]
    },
    proxy: {
      // 开发时代理 API 请求到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 代理上传的文件到后端
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    // 构建优化配置
    target: 'es2015',
    minify: 'terser',
    cssCodeSplit: true,
    sourcemap: false,
    // 代码分割配置
    rollupOptions: {
      output: {
        manualChunks: {
          // 将 Vue 相关库单独打包
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // 将 markdown 相关库单独打包
          'markdown': ['marked', 'dompurify'],
          // 将工具库单独打包
          'utils': ['axios', 'lodash-es']
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
      }
    },
    // 压缩配置
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    // 分块策略
    chunkSizeWarningLimit: 1000
  },
  // 预览服务器配置
  preview: {
    port: 4173
  }
})