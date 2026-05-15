<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { getDiscoveryPlaces } from '../api/index';

const emit = defineEmits(['explore-mood', 'select-place']);

const places = ref([]);
const loading = ref(false);
let map = null;

onMounted(() => {
  fetchPlaces();
});

const fetchPlaces = async () => {
  loading.value = true;
  try {
    const res = await getDiscoveryPlaces();
    if (res.code === 200) {
      places.value = res.data;
      // 数据加载后初始化地图
      nextTick(() => {
        initMap();
      });
    }
  } catch (err) {
    console.log('获取发现列表失败', err);
  } finally {
    loading.value = false;
  }
};

const initMap = () => {
  if (!window.BMap) {
    console.warn('百度地图 SDK 尚未加载');
    return;
  }

  // 初始化地图实例
  map = new window.BMap.Map("allmap");
  
  if (places.value.length > 0) {
    // 根据地点打点
    const points = [];
    places.value.forEach(place => {
      if (place.latitude && place.longitude) {
        const point = new window.BMap.Point(place.longitude, place.latitude);
        points.push(point);
        const marker = new window.BMap.Marker(point);
        map.addOverlay(marker);
        
        // 点击标记提示地点名和标签
        const tagsText = place.moodTags && place.moodTags.length > 0 
          ? ` [${place.moodTags.join(' ')}]` 
          : '';
        const labelContent = `${place.placeName}${tagsText}`;
        
        const label = new window.BMap.Label(labelContent, { 
          offset: new window.BMap.Size(20, -10) 
        });
        
        label.setStyle({
          border: '1px solid var(--primary-color)',
          padding: '6px 10px',
          borderRadius: '12px',
          fontSize: '12px',
          color: 'var(--text-main)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          fontWeight: '500',
          whiteSpace: 'nowrap'
        });
        marker.setLabel(label);
      }
    });

    if (points.length > 0) {
      // 自动缩放并居中到包含所有点
      map.setViewport(points);
    } else {
      // 默认中心：深圳
      map.centerAndZoom(new window.BMap.Point(114.057868, 22.543099), 13);
    }
  } else {
    // 默认中心：深圳
    map.centerAndZoom(new window.BMap.Point(114.057868, 22.543099), 13);
  }

  // 允许鼠标滚轮缩放
  map.enableScrollWheelZoom(true);
};
</script>

<template>
  <div class="discover-page">
    <!-- Map Exploration Card -->
    <div class="explore-map-card">
      <div class="map-preview" id="allmap">
        <!-- Baidu Map will render here -->
      </div>
      <button class="btn-explore-mood" @click="emit('explore-mood')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="1.5"/>
          <path d="M16.2426 7.75736L10.5858 10.5858L7.75736 16.2426L13.4142 13.4142L16.2426 7.75736Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        探索更懂你的心情
      </button>
    </div>


    <!-- Places List -->
    <div class="places-grid" v-if="places.length > 0">
      <div 
        v-for="place in places" 
        :key="place.placeId" 
        class="discover-card"
        @click="emit('select-place', place)"
      >
        <div class="card-image-wrapper">
          <img :src="place.imageUrl || '/images/place/default.jpg'" :alt="place.placeName" class="card-img" />
          <span class="status-badge" :class="{ 'status-pending': place.status === '待物探' }">
            {{ place.status }}
          </span>
        </div>
        <div class="card-footer">
          <div class="card-title-row">
            <h3 class="place-name">{{ place.placeName }}</h3>
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

    <!-- Empty State -->
    <div v-else-if="!loading" class="empty-discover">
      <div class="empty-icon">🗺️</div>
      <p>还没有发现新的角落</p>
      <button class="btn-go-recommend" @click="emit('explore-mood')">去让 AI 推荐一个</button>
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
  height: 220px;
  background: #f8f9fa;
  border-radius: 20px;
  position: relative;
  overflow: hidden;
}

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
  background: rgba(90, 107, 99, 0.8);
  backdrop-filter: blur(8px);
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  letter-spacing: 0.05em;
  z-index: 10;
}

.status-badge.status-pending {
  background: rgba(255, 165, 0, 0.8); /* 橙色表示待探索 */
}

.empty-discover {
  padding: 60px 0;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 8px;
}

.empty-discover p {
  color: var(--text-muted);
  font-size: 0.95rem;
}

.btn-go-recommend {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  margin-top: 8px;
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
