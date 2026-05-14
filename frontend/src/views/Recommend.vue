<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { recommendApi } from '../api/index.js'
import { Recommend } from '../data/constraint.js'

const router = useRouter()
const route = useRoute()

const recommendData = ref(null)
const isLoading = ref(true) // 是否正在加载数据
const hasError = ref(false) // 是否发生错误

const goBack = () => {
  router.back()
}

/**
 * 跳转到地点详情页
 * @param {number} placeId - 地点ID
 */
const goToDetail = (placeId) => {
  router.push(`/place/${placeId}`)
}

/**
 * 获取推荐数据
 * 调用接口: POST /api/recommend
 * 请求参数: { mood, energy_level, social_level, user_input, user_lat, user_lng }
 * 响应: { code, message, data: { understanding, memory_matches, emotion_matches } }
 */
const fetchRecommend = async () => {
  isLoading.value = true
  hasError.value = false
  
  try {
    // 从URL参数获取心情、精力值、社交欲
    const mood = route.query.mood || '烦闷'
    const energy_level = parseInt(route.query.energy) || 3
    const social_level = parseInt(route.query.social) || 2
    const user_input = route.query.input || null
    
    // 调用推荐接口
    const result = await recommendApi.getRecommend({
      mood,
      energy_level,
      social_level,
      user_input,
      user_lat: 22.5431,  // 模拟用户纬度
      user_lng: 113.9526  // 模拟用户经度
    })
    
    if (result.code === 200) {
      // 后端数据优先
      recommendData.value = result.data
      console.log('获取推荐数据成功:', recommendData.value)
    } else {
      // 接口返回错误，使用模拟数据
      console.error('获取推荐数据失败:', result.message)
      hasError.value = true
      recommendData.value = Recommend.data
    }
  } catch (error) {
    // 网络异常或其他错误，使用模拟数据
    console.error('获取推荐数据异常:', error)
    hasError.value = true
    recommendData.value = Recommend.data
  } finally {
    isLoading.value = false
  }
}

// 页面加载时获取推荐数据
onMounted(() => {
  fetchRecommend()
})
</script>

<template>
  <div class="recommend-page">
    <header class="header">
      <button @click="goBack" class="back-btn">←</button>
      <h1 class="page-title">Corner</h1>
    </header>

    <main class="main-content">
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>正在为你寻找角落...</p>
      </div>

      <template v-else>
        <!-- 错误提示（如果有错误） -->
        <div v-if="hasError" class="error">
          <p>获取推荐失败，使用模拟数据</p>
        </div>

        <!-- 推荐内容 -->
        <template v-if="recommendData">
          <!-- 记忆匹配 -->
          <div v-if="recommendData.memory_matches && recommendData.memory_matches.length > 0" class="memory-section">
            <p class="section-label">记忆匹配 (根据你的记忆)</p>
            <div
              @click="goToDetail(recommendData.memory_matches[0].place_id)"
              class="memory-card"
            >
              <div class="card-header">
                <h3 class="place-name">{{ recommendData.memory_matches[0].place_name }}</h3>
                <div class="distance-info">
                  <span class="distance-text">📍 {{ recommendData.memory_matches[0].distance_text }}</span>
                </div>
              </div>
              <div class="tag-list">
                <span
                  v-for="tag in recommendData.memory_matches[0].mood_tags"
                  :key="tag"
                  class="tag"
                >
                  #{{ tag }}
                </span>
              </div>
              <div class="match-reason">
                <div class="reason-icon">♥</div>
                <span class="reason-text">{{ recommendData.memory_matches[0].match_reason }}</span>
              </div>
            </div>
          </div>

          <!-- 情绪地图 -->
          <div v-if="recommendData.emotion_matches && recommendData.emotion_matches.length > 0" class="emotion-section">
            <p class="section-label">情绪地图 (或者试试新的)</p>
            <div class="emotion-cards">
              <div
                v-for="place in recommendData.emotion_matches"
                :key="place.place_id"
                @click="goToDetail(place.place_id)"
                class="emotion-card"
              >
                <div class="card-image">
                  <img
                    :src="place.image_url"
                    :alt="place.place_name"
                    class="image"
                  />
                </div>
                <div class="card-content">
                  <div class="tag-list">
                    <span
                      v-for="tag in place.mood_tags"
                      :key="tag"
                      class="tag"
                    >
                      #{{ tag }}
                    </span>
                  </div>
                  <h3 class="place-name">{{ place.place_name }}</h3>
                  <div class="distance-info">
                    <span class="distance-text">📍 {{ place.distance_text }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>
    </main>

    <nav class="bottom-nav">
      <button @click="router.push('/')" class="nav-item">
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
.recommend-page {
  min-height: 100vh;
  background-color: #FAF7F2;
  padding-bottom: calc(100px + env(safe-area-inset-bottom));
}
.header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 32px 24px 24px;
}
.back-btn {
  padding: 8px 12px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  border: none;
  cursor: pointer;
  font-size: 16px;
}
.page-title {
  font-size: 20px;
  font-weight: 500;
  color: #1f2937;
}
.main-content {
  padding: 0 24px;
}
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top-color: #5B7B5E;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.loading p {
  margin-top: 16px;
  color: #6b7280;
}
.error {
  padding: 24px;
  text-align: center;
  color: #ef4444;
}
.memory-section {
  margin-bottom: 32px;
}
.section-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 16px;
}
.memory-card {
  background-color: white;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}
.place-name {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}
.distance-info {
  display: flex;
  align-items: center;
  gap: 4px;
}
.distance-text {
  font-size: 14px;
  color: #6b7280;
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.tag {
  padding: 4px 12px;
  background-color: #E8F0E8;
  color: #5B7B5E;
  font-size: 12px;
  border-radius: 12px;
}
.match-reason {
  display: flex;
  align-items: center;
  gap: 8px;
}
.reason-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(91, 123, 94, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5B7B5E;
}
.reason-text {
  font-size: 14px;
  color: #4b5563;
}
.emotion-section {
  margin-bottom: 24px;
}
.emotion-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.emotion-card {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
}
.card-image {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #f3f4f6;
  overflow: hidden;
}
.card-image .image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.card-content {
  padding: 16px;
}
.card-content .place-name {
  margin: 8px 0;
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