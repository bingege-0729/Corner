<script setup>
import { ref, onMounted } from 'vue';
import { getTravelTips } from '../api/index';

const emit = defineEmits(['back', 'save-memory']);

const props = defineProps({
  place: {
    type: Object,
    required: true
  }
});

const travelTips = ref({
  weatherTip: '正在获取天气...',
  preparationTip: '正在获取建议...',
  aiMessage: ''
});

onMounted(() => {
  fetchTips();
});

const fetchTips = async () => {
  try {
    const res = await getTravelTips(props.place.placeId);
    if (res.code === 200) {
      travelTips.value = res.data;
    }
  } catch (err) {
    console.log('获取建议失败', err);
  }
};

const bgImage = props.place.imageUrl || new URL('../assets/img/bg.png', import.meta.url).href;
</script>

<template>
  <div class="goto-page" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="overlay">
      <div class="content-container">
        <!-- Header -->
        <div class="header-text">
          <h1 class="title">准备出发了吗？</h1>
          <p class="subtitle">你的宁静之旅在前方等待。</p>
        </div>

        <!-- Map Card -->
        <div class="map-card">
          <img :src="place.imageUrl || bgImage" alt="Map View" class="map-image" />
        </div>

        <!-- Reminders -->
        <div class="reminders-list">
          <div class="reminder-item">
            <div class="reminder-icon weather-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2V4M12 20V22M4.22 4.22L5.64 5.64M18.36 18.36L19.78 19.78M2 12H4M20 12H22M4.22 19.78L5.64 18.36M18.36 5.64L19.78 4.22M17 12C17 14.7614 14.7614 17 12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="reminder-content">
              <h4>天气提醒</h4>
              <p>{{ travelTips.weatherTip }}</p>
            </div>
          </div>

          <div class="reminder-item easter-egg">
            <div class="reminder-icon egg-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="reminder-content">
              <h4>出行建议</h4>
              <p>{{ travelTips.preparationTip }}</p>
            </div>
          </div>
        </div>

        <!-- Buttons -->
        <div class="actions">
          <button class="btn-nav">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 11L22 2L13 21L11 13L3 11Z" fill="currentColor"/>
            </svg>
            开始导航
          </button>
          
          <button class="btn-save" @click="$emit('save-memory')">
            <div class="memory-icon-plus">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L14.4 7.6L20 10L14.4 12.4L12 18L9.6 12.4L4 10L9.6 7.6L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="plus-badge">+</span>
            </div>
            保存到记忆
          </button>
        </div>

        <div class="footer-action">
          <button class="btn-later" @click="$emit('back')">稍后再去</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.goto-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  z-index: 1000;
}

.overlay {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
}

.content-container {
  width: 100%;
  max-width: 400px;
  background: rgba(245, 242, 238, 0.95);
  border-radius: 40px;
  padding: 40px 24px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.header-text .title {
  font-size: 1.8rem;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--text-main);
}

.header-text .subtitle {
  color: var(--text-muted);
  font-size: 0.95rem;
}

.map-card {
  height: 240px;
  background: #7a8c82;
  border-radius: 24px;
  overflow: hidden;
  position: relative;
}

.map-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.8;
}

.map-pin {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
}

.pin-inner {
  width: 12px;
  height: 12px;
  background: #5a6b63;
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(0,0,0,0.2);
}

.reminders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reminder-item {
  background: white;
  padding: 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  text-align: left;
}

.reminder-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eaf2f8;
  color: var(--primary-color);
  flex-shrink: 0;
}

.easter-egg {
  background: rgba(234, 242, 248, 0.5);
}

.reminder-content h4 {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 4px;
  font-weight: 500;
}

.reminder-content p {
  font-size: 0.95rem;
  color: var(--text-main);
  line-height: 1.4;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.btn-nav {
  height: 60px;
  background: #5a6b63;
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 1.1rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-save {
  height: 60px;
  background: #e8eceb;
  color: var(--text-main);
  border: none;
  border-radius: 30px;
  font-size: 1.1rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.memory-icon-plus {
  position: relative;
  display: flex;
  align-items: center;
}

.plus-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 14px;
  font-weight: bold;
  color: var(--primary-color);
}

.footer-action {
  margin-top: -8px;
  display: flex;
  justify-content: center;
}

.btn-later {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1rem;
  cursor: pointer;
  padding: 8px 16px;
  transition: var(--transition);
}

.btn-later:hover {
  color: var(--text-main);
}
</style>
