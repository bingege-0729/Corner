<script setup>
import { ref, onMounted } from 'vue';
import { getTravelTips, toggleBookmark, getPlaceDetail } from '../api/index';

const emit = defineEmits(['back', 'go-to', 'save-memory']);

const props = defineProps({
  place: {
    type: Object,
    required: true
  }
});

const travelTips = ref(null);
const loadingTips = ref(false);
const isBookmarked = ref(false);

onMounted(() => {
  fetchAITips();
  fetchFullDetail();
});

const fetchFullDetail = async () => {
  if (!props.place.placeId || props.place.placeId === -1) return;
  try {
    const res = await getPlaceDetail(props.place.placeId);
    if (res.code === 200 && res.data.yourHistory) {
      isBookmarked.value = !!res.data.yourHistory.isBookmarked;
    }
  } catch (err) {
    console.log('获取地点详情失败', err);
  }
};

const fetchAITips = async () => {
  if (!props.place.placeId) return;
  loadingTips.ref = true;
  try {
    const res = await getTravelTips(props.place.placeId);
    if (res.code === 200) {
      travelTips.value = res.data;
    }
  } catch (err) {
    console.log('获取温馨提示失败', err);
  } finally {
    loadingTips.value = false;
  }
};

const showToast = ref(false);
const toastMessage = ref('');

const handleBookmark = async () => {
  try {
    const res = await toggleBookmark(props.place.placeId, props.place);
    if (res.code === 200) {
      isBookmarked.value = !isBookmarked.value;
      toastMessage.value = isBookmarked.value ? '已存入记忆 ✨' : '已取消收藏';
      showToast.value = true;
      setTimeout(() => {
        showToast.value = false;
      }, 1500);

      // 如果是第一次从外部地点转为入库地点，同步 ID
      if (props.place.placeId === -1 && isBookmarked.value) {
        emit('save-memory', res.data);
      }
    }
  } catch (err) {
    console.log('收藏操作失败', err);
  }
};
</script>

<template>
  <div class="detail-page">
    <!-- Hero Image Section -->
    <div class="hero-section" :style="{ backgroundImage: `url(${place.imageUrl || '/images/default-place.jpg'})` }">
      <div class="header-actions">
        <button class="icon-btn" @click="$emit('back')">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <button class="icon-btn" @click.stop="handleBookmark" :class="{ active: isBookmarked }">
          <svg width="24" height="24" viewBox="0 0 24 24" :fill="isBookmarked ? 'currentColor' : 'none'" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 21L12 16L5 21V5C5 4.46957 5.21071 3.96086 5.58579 3.58579C5.96086 3.21071 6.46957 3 7 3H17C17.5304 3 18.0391 3.21071 18.4142 3.58579C18.7893 3.96086 19 4.46957 19 5V21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Content Card -->
    <div class="content-card">
      <div class="title-section">
        <h1 class="place-name">{{ place.placeName }}</h1>
        <div class="tag-row">
          <span v-for="tag in place.moodTags" :key="tag" class="detail-tag">{{ tag }}</span>
        </div>
      </div>

      <!-- Recommendation Reason -->
      <div class="info-section">
        <div class="section-label">
          <span class="icon">💬</span>
          <span>一句话推荐</span>
        </div>
        <p class="reason-text">{{ place.oneSentence }}</p>
      </div>

      <!-- AI Travel Tips -->
      <div v-if="travelTips" class="sub-card ai-tips-card">
        <div class="section-label">
          <span class="icon">✨</span>
          <span>AI 出行建议</span>
        </div>
        <p class="ai-msg">{{ travelTips.aiMessage }}</p>
        <div class="tip-grid">
          <div class="tip-item">
            <span class="tip-label">天气提示</span>
            <span class="tip-val">{{ travelTips.weatherTip }}</span>
          </div>
          <div class="tip-item">
            <span class="tip-label">准备建议</span>
            <span class="tip-val">{{ travelTips.preparationTip }}</span>
          </div>
        </div>
      </div>

      <!-- Atmosphere Card -->
      <div class="sub-card atmosphere-card">
        <div class="section-label">
          <span class="icon">🌊</span>
          <span>氛围</span>
        </div>
        <div class="flow-info">
          <span class="flow-label">实时人流量</span>
          <span class="flow-value">{{ place.crowdLevel }}</span>
        </div>
      </div>

      <!-- Suggestion Card -->
      <div class="sub-card suggestion-card">
        <div class="section-label">
          <span class="icon">🍃</span>
          <span>地点贴士</span>
        </div>
        <p class="suggestion-text">{{ place.tips }}</p>
      </div>
    </div>

    <!-- Bottom Action Button -->
    <div class="bottom-action">
      <button class="btn-go" @click="emit('go-to')">
        <span class="icon">🚶</span>
        带我去这 (一键成行)
      </button>
    </div>

    <!-- Toast Notification -->
    <Transition name="toast">
      <div v-if="showToast" class="toast-container">
        {{ toastMessage }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.detail-page {
  background: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 120px;
}

.hero-section {
  height: 400px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  padding: 20px;
  padding-top: 50px; /* Safe area */
}

.icon-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-main);
  cursor: pointer;
}

.content-card {
  margin-top: -30px;
  background: white;
  border-radius: 40px 40px 0 0;
  padding: 40px 28px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  position: relative;
}

.title-section {
  text-align: center;
}

.place-name {
  font-size: 2.2rem;
  font-weight: 500;
  color: var(--text-main);
  margin-bottom: 16px;
  line-height: 1.2;
}

.tag-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.detail-tag {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 0.9rem;
  background: #eaf2f8; /* Default tag bg */
}

.detail-tag:nth-child(2) {
  background: #ecf0ef;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.reason-text {
  font-size: 1.4rem;
  color: var(--text-main);
  line-height: 1.4;
  border-left: 2px solid #ddd;
  padding-left: 16px;
}

.sub-card {
  padding: 24px;
  border-radius: 24px;
  background: #f8f9fa;
}

.atmosphere-card {
  background: #fcfcfc;
}

.flow-info {
  margin-top: 12px;
}

.flow-label {
  display: block;
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.flow-value {
  font-family: var(--font-serif);
  font-size: 2rem;
  color: var(--text-main);
}

.suggestion-card {
  background: #eaf2f8;
}

.suggestion-text {
  font-size: 1.1rem;
  color: var(--text-main);
  line-height: 1.5;
}

.bottom-action {
  position: fixed;
  bottom: 100px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  padding: 0 40px;
  z-index: 10;
}

.btn-go {
  width: 100%;
  height: 64px;
  background: #5a6b63;
  color: white;
  border: none;
  border-radius: 32px;
  font-size: 1.1rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  box-shadow: 0 8px 20px rgba(90, 107, 99, 0.3);
}

/* Toast Styles */
.toast-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 12px 24px;
  border-radius: 20px;
  z-index: 1000;
  font-size: 0.9rem;
  backdrop-filter: blur(5px);
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
</style>
