<script setup>
defineProps({
  title: {
    type: String,
    default: 'Corner'
  },
  showBack: {
    type: Boolean,
    default: false
  }
});

defineEmits(['back']);
</script>

<template>
  <header class="top-bar">
    <div class="left-action" @click="$emit('back')">
      <template v-if="showBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </template>
      <template v-else>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 22C12 22 12 18 17 13C19.5 10.5 19.5 7 17 4.5C14.5 2 11 2 8.5 4.5C6 7 6 10.5 8.5 13C10 14.5 12 16 12 22Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M12 22C12 22 12 18 7 13C4.5 10.5 4.5 7 7 4.5C9.5 2 13 2 15.5 4.5C18 7 18 10.5 15.5 13C14 14.5 12 16 12 22Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </template>
    </div>
    <h1 class="logo-text">{{ title }}</h1>
    <div class="right-action">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 15 3 17 3 17H21C21 17 18 15 18 8Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M10.33 21C10.5054 21.3031 10.757 21.5547 11.0601 21.7301C11.3632 21.9054 11.7067 21.9973 12.056 21.9973C12.4054 21.9973 12.7488 21.9054 13.0519 21.7301C13.355 21.5547 13.6067 21.3031 13.782 21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
  </header>
</template>

<style scoped>
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
}

.logo-text {
  font-size: 1.8rem;
  margin: 0;
  font-weight: 500;
}

.left-action, .right-action {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  cursor: pointer;
}

.left-action:active, .right-action:active {
  opacity: 0.6;
}
</style>
