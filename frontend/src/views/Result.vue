<script setup>
import { ref } from 'vue';
import { chat } from '../api/index';

const props = defineProps({
  understanding: {
    type: String,
    default: ''
  },
  places: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['select-place']);
const userInput = ref('');

const handleSend = async () => {
  if (!userInput.value.trim()) return;
  const message = userInput.value;
  userInput.value = '';
  
  console.log('Sending message:', message);
  try {
    const res = await chat({
      userInput: message,
      enableStream: false
    });
    
    if (res.code === 0) {
      console.log('AI Response:', res.data);
      // In a real app, we might update the places or add a message to a list
    }
  } catch (err) {
    console.log('Chat failed', err);
  }
};

</script>

<template>
  <div class="result-page">
    <!-- AI Message -->
    <div class="ai-message-card">
      <p>{{ understanding || '正在为你寻找最适合的角落...' }}</p>
    </div>

    <!-- Section Title -->
    <div class="section-header">
      <h3 class="section-title">记忆匹配 (根据你的记忆)</h3>
    </div>

    <!-- Results List -->
    <div class="results-list">
      <div 
        v-for="place in places" 
        :key="place.placeId" 
        class="place-card"
        :style="{ backgroundImage: `url(${place.imageUrl || '../assets/img/bg.png'})` }"
        @click="emit('select-place', place)"
      >
        <div class="card-overlay">
          <div class="tag-row">
            <span v-for="tag in place.moodTags" :key="tag" class="place-tag">{{ tag }}</span>
          </div>
          <h4 class="place-name">{{ place.placeName }}</h4>
          <div class="distance-row">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 21C16.9706 21 21 16.9706 21 12C21 7.02944 16.9706 3 12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 8V12L14 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>{{ place.distanceText || '距离未知' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Input Section -->
    <div class="chat-input-section">
      <div class="input-wrapper">
        <input 
          v-model="userInput" 
          type="text" 
          placeholder="还想去什么样的角落？"
          @keyup.enter="handleSend"
        />
        <button class="send-btn" @click="handleSend" :disabled="!userInput.trim()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 2L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.result-page {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.ai-message-card {
  background: white;
  padding: 24px;
  border-radius: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.ai-message-card p {
  font-size: 1.1rem;
  line-height: 1.5;
  color: var(--text-main);
  font-weight: 400;
}

.section-header {
  margin-top: 8px;
}

.section-title {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-weight: 400;
  letter-spacing: 0.02em;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.place-card {
  height: 360px;
  background-size: cover;
  background-position: center;
  border-radius: 28px;
  overflow: hidden;
  position: relative;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.card-overlay {
  padding: 24px;
  background: linear-gradient(0deg, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0.2) 50%, rgba(0,0,0,0) 100%);
  color: white;
}

.tag-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.place-tag {
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.place-name {
  font-size: 1.5rem;
  font-weight: 500;
  margin-bottom: 8px;
  letter-spacing: 0.02em;
}

.distance-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  opacity: 0.9;
}

/* Chat Input Section Styles */
.chat-input-section {
  position: sticky;
  bottom: 0;
  margin: 0 -24px -20px -24px;
  padding: 20px 24px 30px;
  background: linear-gradient(0deg, var(--bg-main) 70%, rgba(248, 249, 250, 0));
  backdrop-filter: blur(10px);
  z-index: 10;
}

.input-wrapper {
  background: white;
  height: 56px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  padding: 0 8px 0 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.input-wrapper input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 1rem;
  color: var(--text-main);
  background: transparent;
}

.input-wrapper input::placeholder {
  color: var(--text-muted);
}

.send-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.send-btn:disabled {
  background: #eee;
  color: #ccc;
  cursor: not-allowed;
}

.send-btn:active:not(:disabled) {
  transform: scale(0.9);
}
</style>
