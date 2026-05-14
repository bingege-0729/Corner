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
      getTags, 
      getRecommend, 
      getPlaceDetail, 
      getBookmarks, 
      toggleBookmark,
      chat 
    } from './api/index' 

    const currentPage = ref('home'); 
    const activeTab = ref('mood'); 
    const previousPage = ref('home'); // 记录跳转前的页面

    onMounted(() => {
        fetchTags();
    });

    // 监听标签切换，重置页面状态到入口页
    watch(activeTab, () => {
        if (currentPage.value !== 'login') {
            currentPage.value = 'home';
        }
    });

    const phone=ref('')
    const user_info=ref({})
    const getLogin=async(phoneNum)=>{
        try {
            const res = await login({ phone: phoneNum });
            if (res.code === 0) {
                user_info.value = res.data;
                currentPage.value = 'home';
            }
        } catch (err) {
            console.log('登录失败', err);
        }
    }

    const goToResult = () => {
        currentPage.value = 'result';
    }

    const currentPlace = ref(null);
    const selectPlace = (place) => {
        previousPage.value = currentPage.value; // 保存当前页面
        currentPlace.value = place;
        currentPage.value = 'detail';
    }

    const handleLogin = (phoneNum) => {
        getLogin(phoneNum);
    }

    const handleLogout = () => {
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
            if(res.code===0){
                tagList.value=res.data.tags
            }
        }catch(err){
            console.log('获取标签失败',err)
        }
    }

    const understanding = ref('')
    const emotionMatches = ref([])

    const fetchRecommend = async (params) => {
        try {
            const res = await getRecommend(params)
            if (res.code === 0) {
                understanding.value = res.data.understanding
                emotionMatches.value = res.data.emotionMatches
                currentPage.value = 'result'
            }
        } catch (err) {
            console.log('获取推荐失败', err)
        }
    }

    const togglePlaceBookmark = async (placeId) => {
        try {
            const res = await toggleBookmark(placeId)
            if (res.code === 0) {
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
            if (res.code === 0) {
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
            if (res.code === 0) {
                placeDetailData.value = res.data
            }
        } catch (err) {
            console.log('获取地点详情失败', err)
        }
    }
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
          <Recommend @submit="fetchRecommend" />
        </div>
        <div v-if="currentPage === 'discover'" class="tab-page">
          <Discover @explore-mood="activeTab = 'mood'; currentPage = 'home';" @select-place="selectPlace" />
        </div>
        <div v-if="currentPage === 'result'" class="tab-page">
          <Result 
            :understanding="understanding" 
            :places="emotionMatches" 
            @select-place="selectPlace" 
          />
        </div>
        <div v-if="currentPage === 'detail'" class="tab-page">
          <Detail :place="currentPlace" @back="currentPage = previousPage" @go-to="currentPage = 'goto'" />
        </div>
        <div v-if="currentPage === 'goto'" class="tab-page">
          <GoTo :place="currentPlace" @back="currentPage = 'detail'" @save-memory="currentPage = 'home'; activeTab = 'memory';" />
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
</style>
