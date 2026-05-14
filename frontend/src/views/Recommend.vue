<script setup>
import { ref } from 'vue';

const userInput = ref('');
const energyLevel = ref(4);
const socialLevel = ref(7);
const selectedTags = ref(['想被治愈']); // 改为数组支持多选

const moodTags = [
  { name: '烦闷', color: '#f2f2f2', top: '10%', left: '5%' },
  { name: '想被治愈', color: '#eaf2f8', top: '40%', left: '15%' },
  { name: '枯竭', color: '#f2f2f2', top: '15%', left: '60%' },
  { name: '需要烟火气', color: '#e8e8e6', top: '70%', left: '10%' },
  { name: '安静', color: '#ecf0ef', top: '65%', left: '55%' }
];

const toggleTag = (name) => {
  const index = selectedTags.value.indexOf(name);
  if (index > -1) {
    selectedTags.value.splice(index, 1);
  } else {
    selectedTags.value.push(name);
  }
};

const removeTag = (name) => {
  selectedTags.value = selectedTags.value.filter(t => t !== name);
};

const emit = defineEmits(['submit']);

const handleRecommend = () => {
  const params = {
    mood: selectedTags.value.join(','),
    energyLevel: parseInt(energyLevel.value),
    socialLevel: parseInt(socialLevel.value),
    userInput: userInput.value,
    userLat: 0, // Should be fetched from geolocation if possible
    userLng: 0,
    enableStream: false
  };
  emit('submit', params);
  console.log('Finding corners...', params);
};
</script>

<template>
  <div class="recommend-container">
    <!-- Header Text -->
    <div class="intro-section">
      <h2 class="title-serif">Hello, how are you feeling now?</h2>
      <p class="subtitle">花点时间让自己平静下来。</p>
    </div>

    <!-- Scattered Word Cloud Section -->
    <div class="mood-section">
      <div class="cloud-container">
        <button 
          v-for="tag in moodTags" 
          :key="tag.name"
          class="cloud-tag"
          :style="{ 
            backgroundColor: tag.color,
            top: tag.top,
            left: tag.left
          }"
          :class="{ active: selectedTags.includes(tag.name) }"
          @click="toggleTag(tag.name)"
        >
          {{ tag.name }} <span class="tag-arrow">→</span>
        </button>
      </div>
    </div>

    <!-- Selected Tags + Input Card -->
    <div class="input-wrapper">
      <!-- Selected Tags Pill List -->
      <div class="selected-tags-bar" v-if="selectedTags.length > 0">
        <span 
          v-for="tag in selectedTags" 
          :key="tag" 
          class="selected-pill"
          @click="removeTag(tag)"
        >
          {{ tag }} <i class="close-icon">×</i>
        </span>
      </div>

      <div class="card input-card">
        <textarea 
          v-model="userInput" 
          placeholder="还有什么想说的吗..."
          rows="3"
        ></textarea>
      </div>
    </div>

    <!-- Sliders Section -->
    <div class="sliders-section">
      <div class="slider-group">
        <div class="slider-header">
          <span>精力值</span>
          <span class="level-label">{{ energyLevel }}</span>
        </div>
        <input type="range" v-model="energyLevel" min="1" max="10" class="custom-slider" />
      </div>

      <div class="slider-group">
        <div class="slider-header">
          <span>社交欲</span>
          <span class="level-label">{{ socialLevel }}</span>
        </div>
        <input type="range" v-model="socialLevel" min="1" max="10" class="custom-slider" />
      </div>
    </div>

    <!-- Main Button -->
    <div class="action-section">
      <button class="btn-recommend" @click="handleRecommend">
        寻找我的角落 <span class="arrow">→</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.recommend-container {
  padding: 20px 32px 100px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 450px;
  margin: 0 auto;
}

.intro-section {
  text-align: center;
  margin-bottom: 0;
}

.title-serif {
  font-family: var(--font-serif);
  font-size: 1.6rem;
  color: var(--text-main);
  margin-bottom: 8px;
}

.mood-section {
  height: 220px;
  position: relative;
  margin: 0 -10px;
}

.cloud-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.cloud-tag {
  position: absolute;
  border: none;
  padding: 10px 20px;
  border-radius: 25px;
  font-size: 0.95rem;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
  white-space: nowrap;
}

.cloud-tag.active {
  transform: scale(1.1);
  background-color: var(--primary-color) !important;
  color: white !important;
  z-index: 10;
  box-shadow: 0 8px 20px rgba(83, 102, 94, 0.2);
}

.cloud-tag.active .tag-arrow {
  color: white;
  opacity: 0.8;
}

.tag-arrow {
  opacity: 0.3;
  font-size: 0.8rem;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selected-tags-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.selected-pill {
  background: #f0f2f1;
  color: var(--primary-color);
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  animation: popIn 0.3s ease-out;
}

@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.close-icon {
  font-style: normal;
  font-size: 1.1rem;
  opacity: 0.5;
}

.input-card {
  padding: 16px 20px;
  border-radius: var(--radius-md);
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.03);
  box-shadow: var(--shadow-sm);
}

textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: 1rem;
  color: var(--text-main);
  resize: none;
  font-family: inherit;
}

.sliders-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.slider-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  color: var(--text-main);
  font-size: 0.95rem;
}

.level-label {
  color: var(--primary-color);
  font-weight: 600;
}

.custom-slider {
  -webkit-appearance: none;
  width: 100%;
  height: 4px;
  background: #ebebeb;
  border-radius: 2px;
}

.custom-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  background: var(--primary-color);
  border: 3px solid white;
  border-radius: 50%;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.btn-recommend {
  width: 100%;
  height: 60px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
</style>
