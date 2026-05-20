<script setup>
    import { ref, watch, onMounted } from 'vue';
    import Login from './views/Login.vue';
    import Recommend from './views/Recommend.vue';
    import Result from './views/Result.vue';
    import Detail from './views/Detail.vue';
    import GoTo from './views/GoTo.vue';
    import Memory from './views/Memory.vue';
    import Profile from './views/Profile.vue';
    import Discover from './views/Discover.vue';
    import TopBar from './components/TopBar.vue';
    import BottomNav from './components/BottomNav.vue';
    import { 
      login, 
      getUserStats,
      getTags, 
      getRecommend, 
      getPlaceDetail, 
      getBookmarks, 
      toggleBookmark,
      updateLocation
    } from './api/index' 

    const currentPage = ref('login'); 
    const activeTab = ref('mood'); 
    const previousPage = ref('home'); // 记录跳转前的页面

    onMounted(() => {
        fetchTags();
    });

    const syncLocation = () => {
        if (!navigator.geolocation) {
            console.warn('浏览器不支持地理定位');
            return;
        }
        
        const options = {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
        };

        navigator.geolocation.getCurrentPosition(async (pos) => {
            const { latitude, longitude, accuracy } = pos.coords;
            console.log(`定位成功: ${latitude}, ${longitude}, 精度: ${accuracy}米`);
            try {
                await updateLocation({ latitude, longitude });
                console.log('后端位置已同步');
            } catch (err) {
                console.error('同步位置到后端失败', err);
            }
        }, (err) => {
            let errorMsg = '定位失败: ';
            switch(err.code) {
                case err.PERMISSION_DENIED: errorMsg += "用户拒绝了定位请求"; break;
                case err.POSITION_UNAVAILABLE: errorMsg += "位置信息不可用"; break;
                case err.TIMEOUT: errorMsg += "定位请求超时"; break;
                default: errorMsg += "未知错误"; break;
            }
            console.error(errorMsg);
        }, options);
    };

    // 监听标签切换，重置页面状态到入口页
    watch(activeTab, () => {
        if (currentPage.value !== 'login') {
            currentPage.value = 'home';
        }
    });

    const phone=ref('')
    const user_info=ref({})
    const getLogin=async(loginData)=>{
        try {
            const res = await login(loginData);
            if (res.code === 200) {
                user_info.value = res.data;
                localStorage.setItem('token', res.data.token);
                currentPage.value = 'home';
                syncLocation();
            } else {
                showToast(res.message || '登录失败');
            }
        } catch (err) {
            console.log('登录失败', err);
            // 显示后端返回的具体错误信息
            const errorMsg = err.response?.data?.message || '登录失败，请检查手机号和密码';
            showToast(errorMsg);
        }
    }

    const goToResult = () => {
        currentPage.value = 'result';
    }

    const toastVisible = ref(false);
    const toastMessage = ref('已存入记忆 ✨');
    
    const showToast = (message) => {
        if (message) toastMessage.value = message;
        toastVisible.value = true;
        setTimeout(() => {
            toastVisible.value = false;
        }, 2000);
    };

    const currentPlace = ref(null);
    const selectPlace = (place) => {
        previousPage.value = currentPage.value; // 保存当前页面
        currentPlace.value = place;
        fetchPlaceDetail(place.placeId); // 获取详情
        currentPage.value = 'detail';
    }

    const handleLogin = (loginData) => {
        getLogin(loginData);
    }

    const handleLogout = () => {
        localStorage.removeItem('token');
        currentPage.value = 'login';
        activeTab.value = 'mood';
    }

    const showDiscoverPage = () => {
        currentPage.value = 'discover';
    }

    const tagList=ref([])
    const fetchTags=async()=>{
        try{
            const res=await getTags()
            if(res.code===200){
                tagList.value=res.data.tags
            }
        }catch(err){
            console.log('获取标签失败',err)
        }
    }

    const understanding = ref('')
    const emotionMatches = ref([])
    const isAiLoading = ref(false)

    const fetchRecommend = async (params) => {
        isAiLoading.value = true
        try {
            const res = await getRecommend(params)
            if (res.code === 200) {
                understanding.value = res.data.understanding
                emotionMatches.value = res.data.emotionMatches
                currentPage.value = 'result'
            } else {
                showToast(res.message || 'AI 思考时走神了，请重试')
            }
        } catch (err) {
            console.log('获取推荐失败', err)
            showToast('信号好像不太好，请稍后再试')
        } finally {
            isAiLoading.value = false
        }
    }

    const togglePlaceBookmark = async (placeId) => {
        try {
            const res = await toggleBookmark(placeId)
            if (res.code === 200) {
                console.log('收藏状态切换成功')
            }
        } catch (err) {
            console.log('操作失败', err)
        }
    }

    const bookmarkList = ref([])
    const fetchBookmarks = async () => {
        try {
            const res = await getBookmarks()
            if (res.code === 200) {
                bookmarkList.value = res.data
            }
        } catch (err) {
            console.log('获取收藏列表失败', err)
        }
    }

    const placeDetailData = ref({})
    const fetchPlaceDetail = async (placeId) => {
        try {
            const res = await getPlaceDetail(placeId)
            if (res.code === 200) {
                placeDetailData.value = res.data
                currentPlace.value = res.data // 更新为详细数据
            }
        } catch (err) {
            console.log('获取地点详情失败', err)
        }
    }

    const handleSaveMemory = (newPlaceId) => {
        if (newPlaceId && currentPlace.value) {
            console.log('同步新地点ID:', newPlaceId);
            currentPlace.value.placeId = newPlaceId;
            // 同时更新详情数据
            if (placeDetailData.value) {
                placeDetailData.value.placeId = newPlaceId;
            }
        }
    };
</script>

<template>
  <div class="app-container">
    <Login v-if="currentPage === 'login'" @login="getLogin" />
    
    <template v-else>
      <TopBar 
        v-if="currentPage !== 'detail' && currentPage !== 'goto'" 
        :title="activeTab === 'memory' ? 'Memory' : (currentPage === 'discover' ? '发现的角落' : 'Corner')"
        :show-back="currentPage === 'discover'"
        @back="currentPage === 'discover' ? (currentPage = 'home') : null"
      />
      
      <main class="main-content">
        <!-- 根据 currentPage 和 activeTab 显示不同内容 -->
        <div v-if="currentPage === 'home' && activeTab === 'mood'" class="tab-page">
          <Recommend :tags="tagList" @submit="fetchRecommend" />
        </div>
        <div v-if="currentPage === 'discover'" class="tab-page">
          <Discover @explore-mood="activeTab = 'mood'; currentPage = 'home';" @select-place="selectPlace" />
        </div>
        <div v-if="currentPage === 'result'" class="tab-page">
          <Result 
            :understanding="understanding" 
            :places="emotionMatches" 
            @select-place="selectPlace" 
            @update-results="emotionMatches = $event"
          />
        </div>
        <div v-if="currentPage === 'detail'" class="tab-page">
          <Detail :place="currentPlace" @back="currentPage = previousPage" @go-to="currentPage = 'goto'" @save-memory="handleSaveMemory" />
        </div>
        <div v-if="currentPage === 'goto'" class="tab-page">
          <GoTo :place="currentPlace" @back="currentPage = 'detail'" @save-memory="handleSaveMemory" />
        </div>
        <div v-if="currentPage === 'home' && activeTab === 'memory'" class="tab-page">
          <Memory @select-place="selectPlace" />
        </div>
        <div v-if="currentPage === 'home' && activeTab === 'me'" class="tab-page">
          <Profile @logout="handleLogout" @show-discover="showDiscoverPage" />
        </div>
      </main>

      <BottomNav v-if="currentPage !== 'goto'" v-model="activeTab" @tab-click="currentPage = 'home'" />
    </template>

    <!-- Global AI Loading Overlay -->
    <Transition name="fade">
      <div v-if="isAiLoading" class="ai-loading-overlay">
        <div class="loading-content">
          <div class="ai-sphere">
            <div class="ring"></div>
            <div class="ring"></div>
            <div class="ring"></div>
          </div>
          <div class="loading-text">
            <h3>Corner AI 正在思考</h3>
            <p class="status-msg">正在全网为你搜寻最安静的角落...</p>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-bottom: 90px; /* 为底部栏留出空间 */
}

.tab-page {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* AI Loading Overlay Styles */
.ai-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-content {
  text-align: center;
}

.ai-sphere {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 30px;
}

.ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 2px solid var(--primary-color);
  border-radius: 50%;
  opacity: 0;
  animation: pulse 2s infinite;
}

.ring:nth-child(2) { animation-delay: 0.6s; }
.ring:nth-child(3) { animation-delay: 1.2s; }

@keyframes pulse {
  0% { transform: scale(0.5); opacity: 0; }
  50% { opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}

.loading-text h3 {
  font-size: 1.4rem;
  color: var(--text-main);
  margin-bottom: 8px;
  font-weight: 500;
}

.status-msg {
  color: var(--text-muted);
  font-size: 0.9rem;
  animation: breathe 2s infinite ease-in-out;
}

@keyframes breathe {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
