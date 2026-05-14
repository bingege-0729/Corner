<script setup>
import { ref } from 'vue';

const emit = defineEmits(['explore-mood', 'select-place']);

const categories = ['全部', '安静', '治愈', '想独处'];
const activeCategory = ref('全部');

const places = [
  {
    id: 1,
    name: '南头古城后院',
    distance: '1.2km',
    image: new URL('../assets/img/bg.png', import.meta.url).href,
    status: '记忆中',
    tags: ['#隐秘', '#绿意'],
    reason: "藏在深巷里的院子，满墙的绿植能瞬间让你平静下来。",
    flow: '极少',
    advice: '巷子较深，建议开启地图导航，注意防蚊。'
  },
  {
    id: 2,
    name: '华侨城旧书店',
    distance: '3.5km',
    image: new URL('../assets/img/bg.png', import.meta.url).href,
    status: '记忆中',
    tags: ['#安静', '#独处'],
    reason: "这里的旧书香和木质桌椅营造了极佳的阅读氛围，适合一个人安静呆着。",
    flow: '少',
    advice: '建议自带水杯，书店内的座位比较紧俏。'
  },
  {
    id: 3,
    name: '盐田海滨栈道',
    distance: '8.7km',
    image: new URL('../assets/img/bg.png', import.meta.url).href,
    status: '待物探',
    tags: ['#开阔', '#海风'],
    reason: "面对大海，所有的烦恼都会烟消云散。",
    flow: '多',
    advice: '风大注意保暖，建议带个充电宝。'
  },
  {
    id: 4,
    name: '蛇口老街巷弄',
    distance: '4.2km',
    image: new URL('../assets/img/bg.png', import.meta.url).href,
    status: '记忆中',
    tags: ['#怀旧', '#烟火'],
    reason: "老深圳的味道，这里的慢节奏很治愈。",
    flow: '一般',
    advice: '适合下午漫步，有很多好喝的咖啡馆。'
  }
];
</script>

<template>
  <div class="discover-page">
    <!-- Map Exploration Card -->
    <div class="explore-map-card">
      <div class="map-preview">
        <div class="map-tag tag-1">安静</div>
        <div class="map-tag tag-2">放空</div>
        <div class="map-tag tag-3">烟火气</div>
      </div>
      <button class="btn-explore-mood" @click="emit('explore-mood')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="1.5"/>
          <path d="M16.2426 7.75736L10.5858 10.5858L7.75736 16.2426L13.4142 13.4142L16.2426 7.75736Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        探索更懂你的心情
      </button>
    </div>

    <!-- Category Filters -->
    <div class="filter-tabs">
      <button 
        v-for="cat in categories" 
        :key="cat"
        class="filter-tab"
        :class="{ active: activeCategory === cat }"
        @click="activeCategory = cat"
      >
        {{ cat }}
      </button>
    </div>

    <!-- Places List -->
    <div class="places-grid">
      <div 
        v-for="place in places" 
        :key="place.id" 
        class="discover-card"
        @click="emit('select-place', place)"
      >
        <div class="card-image-wrapper">
          <img :src="place.image" :alt="place.name" class="card-img" />
          <span class="status-badge">{{ place.status }}</span>
        </div>
        <div class="card-footer">
          <div class="card-title-row">
            <h3 class="place-name">{{ place.name }}</h3>
            <button class="btn-icon-only">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M19 21L12 16L5 21V5C5 4.46957 5.21071 3.96086 5.58579 3.58579C5.96086 3.21071 6.46957 3 7 3H17C17.5304 3 18.0391 3.21071 18.4142 3.58579C18.7893 3.96086 19 4.46957 19 5V21Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div class="place-meta">
            <div class="meta-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>{{ place.distance }}</span>
            </div>
            <span class="meta-divider">|</span>
            <div class="meta-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L14.4 7.6L20 10L14.4 12.4L12 18L9.6 12.4L4 10L9.6 7.6L12 2Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>12</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.discover-page {
  padding: 0 24px 100px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.explore-map-card {
  background: #f0f0f0;
  border-radius: 28px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 10px;
}

.map-preview {
  height: 180px;
  background: #e5e5e5;
  border-radius: 20px;
  position: relative;
  overflow: hidden;
  background-image: radial-gradient(circle, #ddd 1px, transparent 1px);
  background-size: 20px 20px;
}

.map-tag {
  position: absolute;
  background: white;
  padding: 6px 14px;
  border-radius: 12px;
  font-size: 0.75rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  color: var(--text-main);
}

.tag-1 { top: 30%; left: 40%; }
.tag-2 { top: 60%; left: 30%; }
.tag-3 { top: 65%; left: 55%; }

.btn-explore-mood {
  background: white;
  border: none;
  padding: 12px;
  border-radius: 16px;
  font-size: 0.85rem;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: var(--shadow-sm);
}

.filter-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.filter-tab {
  background: #f5f5f5;
  border: none;
  padding: 8px 20px;
  border-radius: 12px;
  font-size: 0.85rem;
  color: var(--text-muted);
  white-space: nowrap;
  transition: var(--transition);
}

.filter-tab.active {
  background: #5a6b63;
  color: white;
}

.places-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.discover-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-image-wrapper {
  position: relative;
  height: 380px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  letter-spacing: 0.05em;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.place-name {
  font-size: 1.4rem;
  font-weight: 500;
  color: var(--text-main);
  letter-spacing: 0.02em;
}

.btn-icon-only {
  background: transparent;
  border: none;
  color: #ccc;
  padding: 4px;
  cursor: pointer;
}

.place-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-divider {
  opacity: 0.2;
}
</style>
