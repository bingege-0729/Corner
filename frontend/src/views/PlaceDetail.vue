<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { placeApi } from '../api/index.js'
import { PlaceDetail } from '../data/constraint.js'

const router = useRouter()
const route = useRoute()

/**
 * 地点详情数据
 * 结构: { place_id, place_name, address, latitude, longitude, mood_tags, crowd_level, best_time, one_sentence, full_description, image_url, tips, your_history }
 */
const placeDetail = ref(null)
const isLoading = ref(true) // 是否正在加载数据
const hasError = ref(false) // 是否发生错误

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 跳转到导航页面
 */
const startNavigation = () => {
  const placeId = parseInt(route.params.id) || 1
  router.push(`/forward/${placeId}`)
}

/**
 * 获取地点详情
 * 调用接口: GET /api/place/detail/{place_id}
 * 响应: { code, message, data: { place_id, place_name, address, latitude, longitude, mood_tags, crowd_level, best_time, one_sentence, full_description, image_url, tips, your_history } }
 */
const fetchPlaceDetail = async () => {
  isLoading.value = true
  hasError.value = false
  const placeId = parseInt(route.params.id) || 1
  
  try {
    const result = await placeApi.getDetail(placeId)
    
    if (result.code === 200) {
      // 后端数据优先
      placeDetail.value = result.data
      console.log('获取地点详情成功:', placeDetail.value)
    } else {
      // 接口返回错误，使用模拟数据
      console.error('获取地点详情失败:', result.message)
      hasError.value = true
      placeDetail.value = PlaceDetail[placeId] || PlaceDetail[1]
    }
  } catch (error) {
    // 网络异常或其他错误，使用模拟数据
    console.error('获取地点详情异常:', error)
    hasError.value = true
    placeDetail.value = PlaceDetail[placeId] || PlaceDetail[1]
  } finally {
    isLoading.value = false
  }
}

// 页面加载时获取地点详情
onMounted(() => {
  fetchPlaceDetail()
})
</script>

<template>
  <div class="place-detail-page">
    <header class="fixed-header">
      <button @click="goBack" class="float-btn">←</button>
      <button class="float-btn">⊕</button>
    </header>

    <main>
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>正在加载...</p>
      </div>

      <template v-else>
        <div class="place-header">
          <img
            v-if="placeDetail"
            :src="placeDetail.image_url"
            :alt="placeDetail.place_name"
            class="header-image"
          />
        </div>

        <div class="content-area">
          <div class="info-card">
            <h1 class="place-title">{{ placeDetail?.place_name }}</h1>
            <p class="place-address">{{ placeDetail?.address }}</p>
            <div class="tag-list">
              <span
                v-for="tag in placeDetail?.mood_tags"
                :key="tag"
                class="tag"
              >
                #{{ tag }}
              </span>
            </div>
          </div>

          <div class="reason-card">
            <div class="reason-header">
              <div class="reason-icon">💬</div>
              <span class="reason-title">推荐理由</span>
            </div>
            <p class="reason-text">{{ placeDetail?.one_sentence }}</p>
          </div>

          <div class="atmosphere-card">
            <div class="atmosphere-header">
              <div class="atmosphere-icon">📊</div>
              <span class="atmosphere-title">氛围</span>
            </div>
            <p class="atmosphere-label">实时人流量</p>
            <p class="atmosphere-value">{{ placeDetail?.crowd_level || 'Low' }}</p>
          </div>

          <div class="tips-card">
            <div class="tips-header">
              <div class="tips-icon">💡</div>
              <span class="tips-title">建议</span>
            </div>
            <p class="tips-text">{{ placeDetail?.tips }}</p>
          </div>
        </div>
      </template>
    </main>

    <div class="bottom-action">
      <button @click="startNavigation" class="primary-btn">
        📍 带我去这（一键成行）
      </button>
    </div>

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
.place-detail-page {
  min-height: 100vh;
  background-color: #FAF7F2;
  padding-bottom: calc(180px + env(safe-area-inset-bottom));
}
.fixed-header {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 430px;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px 24px 16px;
}
.float-btn {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  border: none;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.place-header {
  width: 100%;
  height: 320px;
  overflow: hidden;
}
.header-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.content-area {
  padding: 0 24px;
  margin-top: -40px;
  position: relative;
  z-index: 5;
}
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
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
.info-card {
  background-color: white;
  border-radius: 24px;
  padding: 28px 24px;
  margin-bottom: 16px;
  text-align: center;
}
.place-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  line-height: 1.3;
}
.place-address {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 16px;
}
.tag-list {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}
.tag {
  padding: 6px 16px;
  background-color: #f3f4f6;
  color: #4b5563;
  font-size: 14px;
  border-radius: 16px;
}
.reason-card {
  background-color: white;
  border-radius: 24px;
  padding: 20px 24px;
  margin-bottom: 16px;
}
.reason-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.reason-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: rgba(91, 123, 94, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.reason-title {
  font-size: 14px;
  font-weight: 500;
  color: #5B7B5E;
}
.reason-text {
  font-size: 16px;
  color: #4b5563;
  line-height: 1.6;
  padding-left: 48px;
}
.atmosphere-card {
  background-color: white;
  border-radius: 24px;
  padding: 20px 24px;
  margin-bottom: 16px;
}
.atmosphere-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.atmosphere-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.atmosphere-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}
.atmosphere-label {
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 4px;
}
.atmosphere-value {
  font-size: 36px;
  font-weight: 700;
  color: #5B7B5E;
}
.tips-card {
  background-color: #e0f2fe;
  border-radius: 24px;
  padding: 20px 24px;
}
.tips-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.tips-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.tips-title {
  font-size: 14px;
  font-weight: 500;
  color: #0369a1;
}
.tips-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  padding-left: 48px;
}
.bottom-action {
  position: fixed;
  bottom: 70px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 430px;
  padding: 0 24px;
  z-index: 50;
}
.primary-btn {
  width: 100%;
  padding: 18px;
  background-color: #5B7B5E;
  color: white;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(91, 123, 94, 0.3);
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
.nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.nav-text {
  font-size: 12px;
  font-weight: 500;
}
</style>