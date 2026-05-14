<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { placeApi } from '../api/index.js'
import { PlaceDetail, notifications } from '../data/constraint.js'

const router = useRouter()
const route = useRoute()

/**
 * 地点详情数据
 * 结构: { place_id, place_name, address, latitude, longitude, mood_tags, crowd_level, best_time, one_sentence, full_description, image_url, tips, your_history }
 */
const placeDetail = ref(null)
const isLoading = ref(true) // 是否正在加载数据
const hasError = ref(false) // 是否发生错误
const isSaving = ref(false) // 是否正在保存收藏

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 开始导航
 * 调用高德地图URL Scheme，若未安装则跳转Web版
 */
const startNavigation = () => {
  if (placeDetail.value) {
    const { latitude, longitude, place_name } = placeDetail.value
    // 高德地图URL Scheme
    const gaodeUrl = `amapuri://route/plan?dlat=${latitude}&dlon=${longitude}&dname=${encodeURIComponent(place_name)}&dev=1&t=0`
    // Web版备用链接
    const webUrl = `https://uri.amap.com/marker?position=${longitude},${latitude}&name=${encodeURIComponent(place_name)}&callnative=0`
    
    const startTime = Date.now()
    const link = document.createElement('a')
    link.href = gaodeUrl
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 检测是否成功打开App，若未打开则跳转Web版
    setTimeout(() => {
      if (Date.now() - startTime < 2000) {
        window.location.href = webUrl
      }
    }, 1000)
  }
}

/**
 * 保存到卡片（收藏）
 * 调用接口: POST /api/place/feedback
 * 请求参数: { place_id, action, feedback, rating }
 * action: liked(收藏), disliked(不喜欢), visited(已访问)
 */
const saveToCard = async () => {
  if (!placeDetail.value || isSaving.value) return
  
  isSaving.value = true
  
  try {
    const result = await placeApi.feedback({
      place_id: placeDetail.value.place_id,
      action: 'liked',
      feedback: '收藏此地点',
      rating: 5
    })
    
    if (result.code === 200) {
      alert(`已将「${placeDetail.value.place_name}」保存到收藏`)
    } else {
      alert('保存失败，请稍后重试')
    }
  } catch (error) {
    console.error('保存收藏失败:', error)
    alert('保存失败，请稍后重试')
  } finally {
    isSaving.value = false
  }
}

/**
 * 获取地点详情
 * 调用接口: GET /api/place/detail/{place_id}
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
  <div class="forward-page">
    <header class="header">
      <button @click="goBack" class="back-btn">←</button>
    </header>

    <main class="main-content">
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>正在加载...</p>
      </div>

      <template v-else>
        <div class="welcome-section">
          <h1 class="page-title">准备出发了吗？</h1>
          <p class="page-subtitle">你的宁静之旅在前方等待。</p>
        </div>

        <div class="image-card">
          <img :src="placeDetail?.image_url" :alt="placeDetail?.place_name" class="place-image" />
        </div>

        <div class="notifications-section">
          <div
            v-for="(notification, index) in notifications"
            :key="index"
            class="notification-item"
          >
            <div class="notification-icon">{{ notification.icon }}</div>
            <div class="notification-content">
              <span class="notification-title">{{ notification.title }}</span>
              <p class="notification-text">{{ notification.content }}</p>
            </div>
          </div>
        </div>
      </template>
    </main>

    <div class="bottom-actions">
      <button @click="startNavigation" class="primary-btn">
        ▲ 开始导航
      </button>
      <button @click="saveToCard" :disabled="isSaving" class="secondary-btn">
        {{ isSaving ? '保存中...' : '♦ 保存到卡片' }}
      </button>
      <button @click="goBack" class="text-btn">稍后再去</button>
    </div>
  </div>
</template>

<style>
.forward-page {
  min-height: 100vh;
  background-color: #FAF7F2;
  padding-bottom: calc(250px + env(safe-area-inset-bottom));
}
.header {
  padding: 32px 24px 16px;
}
.back-btn {
  width: 40px;
  height: 40px;
  background-color: white;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.main-content {
  padding: 0 24px;
}
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
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
.welcome-section {
  text-align: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}
.page-subtitle {
  font-size: 16px;
  color: #6b7280;
}
.image-card {
  border-radius: 24px;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
.place-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.notifications-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.notification-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background-color: white;
  border-radius: 20px;
}
.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background-color: #f3f4f6;
  flex-shrink: 0;
}
.notification-content {
  flex: 1;
}
.notification-title {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
  display: block;
}
.notification-text {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  margin: 0;
}
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 430px;
  background-color: white;
  border-top: 1px solid #f3f4f6;
  padding: 20px 24px;
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 100;
}
.primary-btn {
  padding: 18px;
  background-color: #5B7B5E;
  color: white;
  border-radius: 24px;
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
.secondary-btn {
  padding: 16px;
  background-color: #f3f4f6;
  color: #374151;
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
.text-btn {
  padding: 12px;
  background: none;
  color: #6b7280;
  border-radius: 24px;
  font-size: 14px;
  border: none;
  cursor: pointer;
}
</style>