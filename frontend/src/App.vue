<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-container">
    <RouterView />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
}

body {
  background-color: #1a1a1a;
}

#app {
  height: 100%;
}

.app-container {
  max-width: 430px;
  height: 100vh;
  max-height: 100vh;
  margin: 0 auto;
  background-color: #FAF7F2;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  position: relative;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
}

.app-container::-webkit-scrollbar {
  display: none;
}

@supports (padding-bottom: env(safe-area-inset-bottom)) {
  .app-container {
    padding-bottom: env(safe-area-inset-bottom);
  }
}
</style>