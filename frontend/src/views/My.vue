<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="memory-page">
    <header class="header">
      <h1 class="page-title">我的</h1>
    </header>

    <main class="main-content">
    </main>

    <nav class="bottom-nav">
      <button @click="router.push('/')" class="nav-item">
        <div class="nav-icon">♥</div>
        <span class="nav-text">心情</span>
      </button>
      <button @click="router.push('/memory')" class="nav-item">
        <div class="nav-icon">⭐</div>
        <span class="nav-text">收藏</span>
      </button>
      <button class="nav-item active">
        <div class="nav-icon">👤</div>
        <span class="nav-text">我的</span>
      </button>
    </nav>
  </div>
</template>

<style>
.memory-page {
  min-height: 100vh;
  background-color: #FAF7F2;
  padding-bottom: calc(100px + env(safe-area-inset-bottom));
}
.header {
  display: flex;
  align-items: center;
  padding: 32px 24px 24px;
}
.page-title {
  font-size: 20px;
  font-weight: 500;
  color: #1f2937;
}
.main-content {
  padding: 0 24px;
}
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 430px;
  background-color: white;
  border-top: 1px solid #f3f4f6;
  padding: 12px 24px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-around;
  z-index: 100;
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: #9ca3af;
}
.nav-item.active {
  color: #5B7B5E;
}
.nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.nav-item.active .nav-icon {
  background-color: rgba(91, 123, 94, 0.1);
}
.nav-text {
  font-size: 12px;
  font-weight: 500;
}
</style>