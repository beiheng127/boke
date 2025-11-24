<template>
  <div>
    <Background />
    <Navbar />
    <router-view />
  </div>
  </template>

<script setup>
import Navbar from './components/Navbar.vue'
import Background from './components/Background.vue'
</script>

<style>
:root { --primary: #2563eb; }
a { text-decoration: none; color: inherit; }
.container { max-width: 960px; margin: 0 auto; padding: 16px; }
.btn { background: var(--primary); color: #fff; padding: 8px 14px; border: 0; border-radius: 6px; cursor: pointer; }
.btn.ghost { background: transparent; color: var(--primary); border: 1px solid var(--primary); }
.input { width: 100%; box-sizing: border-box; padding: 10px; border: 1px solid #ddd; border-radius: 6px; }
.card { border: 1px solid #eee; border-radius: 10px; padding: 16px; margin-bottom: 16px; }
.row { display: flex; gap: 12px; align-items: center; }
.space { height: 16px; }
</style>


