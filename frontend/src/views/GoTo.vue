<script setup>
import { ref, onMounted } from 'vue';
import { getTravelTips, toggleBookmark, recordExploration } from '../api/index';

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

const isSaved = ref(false);
const showToast = ref(false);
const showMapMenu = ref(false);

const handleSave = async () => {
  try {
    const res = await toggleBookmark(props.place.placeId, props.place);
    if (res.code === 200) {
      isSaved.value = true;
      showToast.value = true;
      setTimeout(() => {
        showToast.value = false;
      }, 1500);
      emit('save-memory');
    }
  } catch (err) {
    console.log('保存失败', err);
  }
};

const handleNavigate = async (type) => {
  const { latitude, longitude, placeName } = props.place;
  
  // 记录到“发现的角落”
  try {
    await recordExploration(props.place);
  } catch (err) {
    console.log('记录探索失败', err);
  }

  let url = '';
  
  if (type === 'amap') {
    // 高德地图协议
    url = `amapuri://route/plan/?did=&dlat=${latitude}&dlon=${longitude}&dname=${placeName}&dev=0&t=0`;
  } else if (type === 'baidu') {
    // 百度地图协议
    url = `baidumap://map/direction?destination=latlng:${latitude},${longitude}|name:${placeName}&mode=driving`;
  } else if (type === 'apple') {
    // 苹果地图协议
    url = `http://maps.apple.com/?daddr=${latitude},${longitude}&dirflg=d`;
  }
  
  window.location.href = url;
  showMapMenu.value = false;
};
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
          <button class="btn-nav" @click="showMapMenu = true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 11L22 2L13 21L11 13L3 11Z" fill="currentColor"/>
            </svg>
            开始导航
          </button>
          
          <button class="btn-save" @click="handleSave" :class="{ 'is-saved': isSaved }">
            <div class="memory-icon-plus">
              <svg width="18" height="18" viewBox="0 0 24 24" :fill="isSaved ? 'var(--primary-color)' : 'none'" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L14.4 7.6L20 10L14.4 12.4L12 18L9.6 12.4L4 10L9.6 7.6L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="plus-badge" v-if="!isSaved">+</span>
            </div>
            {{ isSaved ? '已保存到记忆' : '保存到记忆' }}
          </button>
        </div>

        <div class="footer-action">
          <button class="btn-later" @click="$emit('back')">稍后再去</button>
        </div>
      </div>
    </div>

    <!-- Toast Notification -->
    <Transition name="toast">
      <div v-if="showToast" class="toast-container">
        已存入记忆 ✨
      </div>
    </Transition>

    <!-- Map Selection Menu -->
    <div v-if="showMapMenu" class="map-menu-overlay" @click="showMapMenu = false">
      <div class="map-menu-content" @click.stop>
        <div class="menu-header">选择导航地图</div>
        <button class="menu-btn" @click="handleNavigate('amap')">高德地图</button>
        <button class="menu-btn" @click="handleNavigate('baidu')">百度地图</button>
        <button class="menu-btn" @click="handleNavigate('apple')">苹果地图</button>
        <button class="menu-btn cancel" @click="showMapMenu = false">取消</button>
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

.btn-save.is-saved {
  background: #f0f7f4;
  color: #5a6b63;
}

/* Toast Styles */
.toast-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 12px 28px;
  border-radius: 20px;
  z-index: 2000;
  font-size: 0.9rem;
  backdrop-filter: blur(8px);
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -40%);
}

/* Map Menu Styles */
.map-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 3000;
  display: flex;
  align-items: flex-end;
}

.map-menu-content {
  width: 100%;
  background: white;
  border-radius: 32px 32px 0 0;
  padding: 24px;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.menu-header {
  text-align: center;
  font-size: 0.9rem;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.menu-btn {
  width: 100%;
  padding: 18px;
  border: none;
  background: #f8f9fa;
  border-radius: 16px;
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--text-main);
  margin-bottom: 12px;
  cursor: pointer;
}

.menu-btn:active {
  background: #eee;
}

.menu-btn.cancel {
  background: white;
  color: #ff4757;
  margin-top: 8px;
  margin-bottom: 0;
}
</style>
