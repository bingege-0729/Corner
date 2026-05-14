<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { memoryApi } from '../api/index.js'
import { MemoryList } from '../data/constraint.js'

const router = useRouter()

/**
 * 记忆列表数据
 * 结构: { memory_id, place_id, place_name, image_url, interaction_type, rating, feedback, visited_at }
 */
const memoryData = ref([])
const isLoading = ref(true) // 是否正在加载数据
const hasError = ref(false) // 是否发生错误

/**
 * 跳转到地点详情页
 * @param {number} placeId - 地点ID
 */
const goToDetail = (placeId) => {
  router.push(`/place/${placeId}`)
}

/**
 * 格式化日期
 * @param {string} dateStr - 日期字符串 (YYYY-MM-DD)
 * @returns {string} - 格式化后的日期 (YYYY.MM.DD)
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
}

/**
 * 获取记忆列表
 * 调用接口: GET /api/memory/list
 * 请求参数: { type } - type可选: visited / bookmarked
 * 响应: { code, message, data: { list: [...] } }
 */
const fetchMemoryList = async () => {
  isLoading.value = true
  hasError.value = false
  
  try {
    // 调用记忆列表接口，不传type返回全部
    const result = await memoryApi.getList()
    
    if (result.code === 200) {
      // 后端数据优先
      memoryData.value = result.data.list || []
      console.log('获取记忆列表成功:', memoryData.value)
    } else {
      // 接口返回错误，使用模拟数据
      console.error('获取记忆列表失败:', result.message)
      hasError.value = true
      memoryData.value = MemoryList.data.list
    }
  } catch (error) {
    // 网络异常或其他错误，使用模拟数据
    console.error('获取记忆列表异常:', error)
    hasError.value = true
    memoryData.value = MemoryList.data.list
  } finally {
    isLoading.value = false
  }
}

// 页面加载时获取记忆列表
onMounted(() => {
  fetchMemoryList()
})
</script>

<template>
  <div class="memory-page">
    <header class="header">
      <h1 class="page-title">Memory</h1>
    </header>

    <main class="main-content">
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>正在加载记忆...</p>
      </div>

      <template v-else>
        <!-- 错误提示（如果有错误） -->
        <div v-if="hasError" class="error">
          <p>加载记忆失败，使用模拟数据</p>
        </div>

        <!-- 记忆列表 -->
        <div class="memory-list">
          <div
            v-for="item in memoryData"
            :key="item.memory_id"
            @click="goToDetail(item.place_id)"
            class="memory-card"
          >
            <div class="card-image">
              <img :src="item.image_url" :alt="item.place_name" class="image" />
            </div>
            <div class="card-info">
              <span class="card-date">{{ formatDate(item.visited_at) }}</span>
              <h3 class="card-title">{{ item.place_name }}</h3>
              <div class="card-tags">
                <span class="tag">#{{ item.interaction_type === 'VISITED' ? '已访问' : '已收藏' }}</span>
                <span v-if="item.rating" class="tag">评分: {{ item.rating }}星</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </main>

    <nav class="bottom-nav">
      <button @click="router.push('/')" class="nav-item">
        <div class="nav-icon">♥</div>
        <span class="nav-text">心情</span>
      </button>
      <button class="nav-item active">
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
.memory-page {
  min-height: 100vh;
  background-color: #FAF7F2;
  padding-bottom: calc(100px + env(safe-area-inset-bottom));
}
.header {
  display: flex;
  align-items: center;
  padding: 32px 24px 24px;
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
.memory-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.memory-card {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
}
.card-image {
  width: 100%;
  aspect-ratio: 3 / 2;
  background-color: #f3f4f6;
  overflow: hidden;
}
.card-image .image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.card-info {
  padding: 16px;
}
.card-date {
  font-size: 12px;
  color: #9ca3af;
}
.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin: 8px 0;
}
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag {
  padding: 4px 12px;
  background-color: #f3f4f6;
  color: #6b7280;
  font-size: 12px;
  border-radius: 12px;
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