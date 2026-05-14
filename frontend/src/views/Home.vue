<script setup>import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { mood_tags } from '../data/constraint.js';
import { tagsApi } from '../api/index.js';
const router = useRouter();
const mood = ref(''); 
const energy_level = ref(3); 
const social_level = ref(2); 
const user_input = ref(''); 
const tags = ref([]); 

const fetchTags = async () => {
  try {
    const result = await tagsApi.getTags();
    if (result.code === 200) {
      tags.value = result.data.tags || [];
      console.log('获取标签列表成功:', tags.value);
    } 
  } catch (error) {
    console.error('获取标签列表异常:', error);
  }
};

const selectMood = (moodItem) => {
  mood.value = moodItem.tag_name;
  if (!user_input.value) {
    user_input.value = moodItem.tag_name;
  } else if (!user_input.value.includes(moodItem.tag_name)) {
    user_input.value += `，${moodItem.tag_name}`;
  }
};

const addTagToInput = (tagName) => {
  if (!user_input.value) {
    user_input.value = tagName;
  } else if (!user_input.value.includes(tagName)) {
    user_input.value += `，${tagName}`;
  }
};

const findCorner = () => {
 router.push({
 name: 'Recommend',
 query: {
 mood: mood.value || '烦闷',
 energy: energy_level.value,
 social: social_level.value,
 input: user_input.value
 }
 });
};

const getMoodColor = (label) => {
 const colors = {
 '烦闷': '#e5e7eb',
 '想被治愈': '#dcfce7',
 '枯槁': '#fef3c7',
 '需要烟火气': '#ffedd5',
 '安静': '#d1fae5'
 };
 return colors[label] || '#e5e7eb';
};

const getMoodTextColor = (label) => {
 const colors = {
 '烦闷': '#374151',
 '想被治愈': '#166534',
 '枯槁': '#92400e',
 '需要烟火气': '#c2410c',
 '安静': '#059669'
 };
 return colors[label] || '#374151';
};
// 页面加载时获取标签列表
onMounted(() => {
 fetchTags();
});
</script>

<template>
  <div class="mood-page">
    <header class="header">
      <div class="logo">
        <div class="logo-icon">♥</div>
        <h1 class="logo-text">Corner</h1>
      </div>
      <div class="header-actions">
        <button class="action-btn">🔍</button>
        <button class="action-btn">⚙</button>
      </div>
    </header>

    <main class="main-content">
      <div class="greeting">
        <h2 class="greeting-title">Hello, how are you feeling now?</h2>
        <p class="greeting-subtitle">花点时间让自己平静下来。</p>
      </div>

      <div class="input-section">
        <textarea
          v-model="user_input"
          placeholder="好烦，不想动脑子..."
          class="mood-input"
        ></textarea>
      </div>

      <div class="mood-tags-section">
        <p class="section-label">快速选择心情</p>
        <div class="mood-tags">
          <button
            v-for="item in mood_tags"
            :key="item.id"
            @click="selectMood(item)"
            :class="['mood-tag', { active: mood === item.tag_name }]"
            :style="{ backgroundColor: mood === item.tag_name ? getMoodColor(item.tag_name) : '#f3f4f6', color: mood === item.tag_name ? getMoodTextColor(item.tag_name) : '#4b5563' }"
          >
            {{ item.tag_name }}
          </button>
        </div>
      </div>

      <!-- 后端标签列表 -->
      <div v-if="tags.length > 0" class="backend-tags-section">
        <p class="section-label">更多标签</p>
        <div class="backend-tags">
          <button
            v-for="tag in tags"
            :key="tag.id"
            @click="addTagToInput(tag.tag_name)"
            class="backend-tag"
          >
            {{ tag.tag_name }}
          </button>
        </div>
      </div>

      <div class="sliders-section">
        <div class="slider-item">
          <div class="slider-label">
            <span class="label-text">精力值</span>
            <span class="label-hint">{{ energy_level }}</span>
          </div>
          <input
            v-model="energy_level"
            type="range"
            min="1"
            max="5"
            class="slider"
          />
        </div>

        <div class="slider-item">
          <div class="slider-label">
            <span class="label-text">社交欲</span>
            <span class="label-hint">{{ social_level }}</span>
          </div>
          <input
            v-model="social_level"
            type="range"
            min="1"
            max="5"
            class="slider"
          />
        </div>
      </div>

      <button @click="findCorner" class="primary-btn">
        寻找我的角落 →
      </button>
    </main>

    <nav class="bottom-nav">
      <button class="nav-item active">
        <div class="nav-icon">♥</div>
        <span class="nav-text">心情</span>
      </button>
      <button @click="router.push('/memory')" class="nav-item">
        <div class="nav-icon">⭐</div>
        <span class="nav-text">收藏</span>
      </button>
      <button @click="router.push('/my')" class="nav-item">
        <div class="nav-icon">👤</div>
        <span class="nav-text">我的</span>
      </button>
    </nav>
  </div>
</template>

<style>
.mood-page {
  min-height: 100vh;
  background-color: #FAF7F2;
  padding-bottom: calc(100px + env(safe-area-inset-bottom));
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px 24px 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #5B7B5E;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}
.logo-text {
  font-size: 20px;
  font-weight: 500;
  color: #1f2937;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.action-btn {
  padding: 8px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  border: none;
  cursor: pointer;
  font-size: 16px;
}
.main-content {
  padding: 0 24px;
}
.greeting {
  margin-bottom: 32px;
}
.greeting-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}
.greeting-subtitle {
  font-size: 14px;
  color: #6b7280;
}
.input-section {
  margin-bottom: 32px;
}
.mood-input {
  width: 100%;
  padding: 16px;
  border-radius: 16px;
  background-color: white;
  border: 1px solid #f3f4f6;
  font-size: 14px;
  color: #374151;
  resize: none;
  height: 80px;
  outline: none;
  box-sizing: border-box;
}
.mood-tags-section {
  margin-bottom: 40px;
}
.section-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 16px;
}
.mood-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.mood-tag {
  padding: 12px 20px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  cursor: pointer;
}
.backend-tags-section {
  margin-bottom: 40px;
}
.backend-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.backend-tag {
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #d1d5db;
  background-color: white;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s;
}
.backend-tag:hover {
  border-color: #5B7B5E;
  color: #5B7B5E;
}
.sliders-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
  margin-bottom: 40px;
}
.slider-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.slider-label {
  display: flex;
  justify-content: space-between;
}
.label-text {
  font-size: 14px;
  color: #6b7280;
}
.label-hint {
  font-size: 14px;
  color: #9ca3af;
}
.slider {
  width: 100%;
  height: 8px;
  background-color: #e5e7eb;
  border-radius: 9999px;
  -webkit-appearance: none;
  appearance: none;
  cursor: pointer;
}
.primary-btn {
  width: 100%;
  padding: 16px;
  background-color: #5B7B5E;
  color: white;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  cursor: pointer;
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