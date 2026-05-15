<script setup>
import { ref, onMounted } from 'vue';
import { getBookmarks, toggleBookmark } from '../api/index';

const emit = defineEmits(['select-place']);
const memories = ref([]);
const loading = ref(false);

onMounted(() => {
  fetchMemories();
});

const fetchMemories = async () => {
  loading.value = true;
  try {
    const res = await getBookmarks();
    if (res.code === 200) {
      memories.value = res.data;
    }
  } catch (err) {
    console.log('获取收藏列表失败', err);
  } finally {
    loading.value = false;
  }
};

const handleToggleBookmark = async (e, placeId) => {
  e.stopPropagation(); // 阻止触发卡片详情跳转
  try {
    const res = await toggleBookmark(placeId);
    if (res.code === 200) {
      // 从本地列表中移除
      memories.value = memories.value.filter(m => m.placeId !== placeId);
    }
  } catch (err) {
    console.log('取消收藏失败', err);
  }
};
</script>

<template>
  <div class="memory-page">
    <div v-if="memories.length === 0 && !loading" class="empty-state">
      还没有收藏任何角落哦
    </div>
    <div class="memories-list">
      <div 
        v-for="memory in memories" 
        :key="memory.placeId" 
        class="memory-card"
        @click="emit('select-place', memory)"
      >
        <div 
          class="memory-image-box" 
          :style="{ backgroundImage: `url(${memory.imageUrl || ''})` }"
        >
          <div class="image-overlay">
            <span class="memory-date">{{ memory.lastVisited || '刚刚收藏' }}</span>
            <h2 class="memory-name">{{ memory.placeName }}</h2>
          </div>
        </div>
        <div class="memory-info">
          <div class="tag-row">
            <span v-for="tag in memory.moodTags" :key="tag" class="memory-tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.memory-page {
  padding: 20px 24px 100px;
}

.empty-state {
  text-align: center;
  padding: 100px 0;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.memories-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.memory-card {
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.memory-image-box {
  height: 240px;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.image-overlay {
  padding: 24px 20px;
  background: linear-gradient(0deg, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.3) 50%, rgba(0,0,0,0) 100%);
  color: #ffffff;
}

.memory-date {
  font-size: 0.85rem;
  opacity: 0.9;
  font-weight: 300;
}

.memory-name {
  font-size: 1.4rem;
  font-weight: 600;
  margin-top: 6px;
  letter-spacing: 0.02em;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.memory-info {
  padding: 16px 20px;
  background: white; /* 保持卡片主体白色，增加高级感 */
}

.tag-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.memory-tag {
  background: var(--text-main); /* 使用深色背景 */
  color: white; /* 文字改为白色 */
  padding: 4px 14px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.05em;
}
</style>
