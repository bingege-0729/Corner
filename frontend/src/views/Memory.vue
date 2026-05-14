<script setup>
const emit = defineEmits(['select-place']);

const memories = [
  {
    id: 1,
    name: '沙河公园湖边长椅',
    date: 'Oct 12, 2023',
    tags: ['#安静', '#独处'],
    image: new URL('../assets/img/bg.png', import.meta.url).href,
    reason: "这里的气氛和'安静独处'简直绝配。",
    flow: '少',
    advice: '建议带驱蚊水。最近的厕所在公园北门。'
  },
  {
    id: 2,
    name: '旧图书馆窗边',
    date: 'Sep 05, 2023',
    tags: ['#阅读', '#午后阳光'],
    image: new URL('../assets/img/bg.png', import.meta.url).href,
    reason: "阳光洒在书页上的感觉太棒了。",
    flow: '极少',
    advice: '光线较强，注意防晒。'
  },
  {
    id: 3,
    name: '老街区深巷',
    date: 'Aug 22, 2023',
    tags: ['#漫步', '#怀旧'],
    image: new URL('../assets/img/bg.png', import.meta.url).href,
    reason: "穿越时光的静谧感。",
    flow: '一般',
    advice: '注意路滑。'
  }
];
</script>

<template>
  <div class="memory-page">
    <div class="memories-list">
      <div 
        v-for="memory in memories" 
        :key="memory.id" 
        class="memory-card"
        @click="emit('select-place', memory)"
      >
        <div 
          class="memory-image-box" 
          :style="{ backgroundImage: `url(${memory.image})` }"
        >
          <div class="image-overlay">
            <span class="memory-date">{{ memory.date }}</span>
            <h2 class="memory-name">{{ memory.name }}</h2>
          </div>
        </div>
        <div class="memory-info">
          <div class="tag-row">
            <span v-for="tag in memory.tags" :key="tag" class="memory-tag">{{ tag }}</span>
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
  padding: 20px;
  background: linear-gradient(0deg, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0) 100%);
  color: white;
}

.memory-date {
  font-size: 0.85rem;
  opacity: 0.9;
  font-weight: 300;
}

.memory-name {
  font-size: 1.4rem;
  font-weight: 500;
  margin-top: 4px;
  letter-spacing: 0.02em;
}

.memory-info {
  padding: 16px 20px;
  background: #f8f9fa;
}

.tag-row {
  display: flex;
  gap: 8px;
}

.memory-tag {
  background: #eaf2f8;
  color: var(--text-main);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}
</style>
