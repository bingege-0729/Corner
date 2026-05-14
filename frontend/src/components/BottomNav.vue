<script setup>
defineProps({
  modelValue: {
    type: String,
    default: 'mood'
  }
});

defineEmits(['update:modelValue', 'tab-click']);
</script>

<template>
  <nav class="bottom-nav">
    <div 
      class="nav-item" 
      :class="{ active: modelValue === 'mood' }"
      @click="$emit('update:modelValue', 'mood'); $emit('tab-click', 'mood')"
    >
      <div class="icon-wrapper">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 21.35L10.55 20.03C5.4 15.36 2 12.28 2 8.5C2 5.42 4.42 3 7.5 3C9.24 3 10.91 3.81 12 5.09C13.09 3.81 14.76 3 16.5 3C19.58 3 22 5.42 22 8.5C22 12.28 18.6 15.36 13.45 20.04L12 21.35Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <span class="label">心情</span>
    </div>

    <div 
      class="nav-item" 
      :class="{ active: modelValue === 'memory' }"
      @click="$emit('update:modelValue', 'memory'); $emit('tab-click', 'memory')"
    >
      <div class="icon-wrapper">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L14.4 7.6L20 10L14.4 12.4L12 18L9.6 12.4L4 10L9.6 7.6L12 2Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M19 15L20.2 17.8L23 19L20.2 20.2L19 23L17.8 20.2L15 19L17.8 17.8L19 15Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <span class="label">记忆</span>
    </div>

    <div 
      class="nav-item" 
      :class="{ active: modelValue === 'me' }"
      @click="$emit('update:modelValue', 'me'); $emit('tab-click', 'me')"
    >
      <div class="icon-wrapper">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <span class="label">我的</span>
    </div>
  </nav>
</template>

<style scoped>
.bottom-nav {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 84px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  padding-bottom: env(safe-area-inset-bottom);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
  cursor: pointer;
  transition: var(--transition);
  padding: 8px 16px;
  border-radius: 20px;
}

.nav-item.active {
  color: var(--text-main);
}

.icon-wrapper {
  margin-bottom: 4px;
  width: 44px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  transition: var(--transition);
}

.active .icon-wrapper {
  background: rgba(0, 0, 0, 0.05);
  color: var(--primary-color);
}

.label {
  font-size: 0.75rem;
  font-weight: 500;
}
</style>
