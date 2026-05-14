import { ref, onMounted } from 'vue';
import { getUserStats, uploadAvatar } from '../api/index';

const emit = defineEmits(['logout', 'show-discover']);

const stats = ref({
  discovered: 0,
  moods: {}
});

const loading = ref(false);

onMounted(() => {
  fetchStats();
});

const userName = ref('旅人');

const fetchStats = async () => {
  loading.value = true;
  try {
    const res = await getUserStats();
    if (res.code === 200) {
      stats.value.discovered = res.data.visitedPlacesCount;
      stats.value.moods = res.data.moodStats || {};
      
      // 更新用户信息
      userName.value = res.data.nickname || '旅人';
      if (res.data.avatarUrl) {
        avatarUrl.value = res.data.avatarUrl.startsWith('http') 
          ? res.data.avatarUrl 
          : `http://localhost:8080${res.data.avatarUrl}`;
      }
    }
  } catch (err) {
    console.log('获取统计数据失败', err);
  } finally {
    loading.value = false;
  }
};

const showLogoutConfirm = ref(false);

const confirmLogout = () => {
  showLogoutConfirm.value = true;
};

const handleLogout = () => {
  showLogoutConfirm.value = false;
  emit('logout');
};

const avatarUrl = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=Corner');
const fileInput = ref(null);

const triggerFileInput = () => {
  fileInput.value.click();
};

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const res = await uploadAvatar(formData);
    if (res.code === 200) {
      // 这里的 res.data 应该是后端返回的新头像 URL
      // 注意：如果是相对路径，需要拼上 baseURL
      const newUrl = res.data.startsWith('http') ? res.data : `http://localhost:8080${res.data}`;
      avatarUrl.value = newUrl;
    }
  } catch (err) {
    console.log('头像上传失败', err);
  }
};
</script>

<template>
  <div class="profile-page">
    <!-- User Header -->
    <div class="user-header">
      <div class="avatar-wrapper">
        <div class="avatar-container">
          <img :src="avatarUrl" alt="User Avatar" class="avatar-img" />
        </div>
        <button class="avatar-edit-btn" @click="triggerFileInput">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M23 19C23 19.5304 22.7893 20.0391 22.4142 20.4142C22.0391 20.7893 21.5304 21 21 21H3C2.46957 21 1.96086 20.7893 1.58579 20.4142C1.21071 20.0391 1 19.5304 1 19V8C1 7.46957 1.21071 6.96086 1.58579 6.58579C1.96086 6.21071 2.46957 6 3 6H7L9 3H15L17 6H21C21.5304 6 22.0391 6.21071 22.4142 6.58579C22.7893 6.96086 23 7.46957 23 8V19Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 17C14.2091 17 16 15.2091 16 13C16 10.7909 14.2091 9 12 9C9.79086 9 8 10.7909 8 13C8 15.2091 9.79086 17 12 17Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <input 
          type="file" 
          ref="fileInput" 
          style="display: none" 
          accept="image/*" 
          @change="handleAvatarUpload" 
        />
      </div>
      <h2 class="user-name">{{ userName }}</h2>
      <p class="user-motto">在这里，找回内心的安静</p>
    </div>

    <!-- Stats Cards -->
    <div class="stats-container">
      <div class="stats-card-main" @click="emit('show-discover')">
        <span class="stats-number">{{ stats.discovered }}</span>
        <span class="stats-label">发现的角落</span>
      </div>
      
      <div class="stats-card-group" v-if="Object.keys(stats.moods).length > 0">
        <div v-for="(count, mood) in stats.moods" :key="mood" class="stats-sub-card">
          <span class="stats-number">{{ count }}</span>
          <span class="stats-label">{{ mood }}</span>
        </div>
      </div>
      <div v-else class="stats-card-group empty-stats">
        <p>还没有心情记录哦</p>
      </div>
    </div>

    <!-- Menu List -->
    <div class="menu-list">
      <div class="menu-item">
        <div class="menu-left">
          <svg class="menu-svg" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 21L10.55 19.705C5.4 15.035 2 11.95 2 8.175C2 5.095 4.42 2.675 7.5 2.675C9.24 2.675 10.91 3.485 12 4.765C13.09 3.485 14.76 2.675 16.5 2.675C19.58 2.675 22 5.095 22 8.175C22 11.95 18.6 15.035 13.45 19.715L12 21Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>给个好评</span>
        </div>
        <span class="chevron">›</span>
      </div>
      
      <div class="menu-item">
        <div class="menu-left">
          <svg class="menu-svg" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 16V12M12 8H12.01M22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>关于我们</span>
        </div>
        <span class="chevron">›</span>
      </div>
    </div>

    <!-- Logout Button -->
    <div class="logout-section">
      <button class="btn-logout" @click="confirmLogout">
        退出登录
      </button>
    </div>

    <!-- Logout Confirmation Modal -->
    <Teleport to="body">
      <div v-if="showLogoutConfirm" class="modal-overlay">
        <div class="confirm-modal">
          <div class="modal-icon">👋</div>
          <h3>确定要退出吗？</h3>
          <p>退出后将需要重新登录手机号以查看你的记忆</p>
          <div class="modal-actions">
            <button class="btn-modal-cancel" @click="showLogoutConfirm = false">取消</button>
            <button class="btn-modal-confirm" @click="handleLogout">确定退出</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 40px 24px 100px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.user-header {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 20px;
}

.avatar-container {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  padding: 4px;
  background: white;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  overflow: hidden;
}

.avatar-edit-btn {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 32px;
  height: 32px;
  background: #5a6b63;
  border: 2px solid white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
  transition: transform 0.2s ease;
}

.avatar-edit-btn:active {
  transform: scale(0.9);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 1.8rem;
  font-weight: 500;
  color: var(--text-main);
  margin-bottom: 8px;
}

.user-motto {
  color: var(--text-muted);
  font-size: 0.95rem;
}

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-card-main, .stats-card-group {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0, 0, 0, 0.01);
}

.stats-card-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.stats-card-group {
  display: flex;
  padding: 24px 0;
}

.stats-sub-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.divider {
  width: 1px;
  background: #f1f1f1;
  height: 40px;
  align-self: center;
}

.stats-number {
  font-family: var(--font-serif);
  font-size: 2rem;
  color: var(--primary-color);
  line-height: 1;
}

.stats-label {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.menu-list {
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f8f9fa;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1rem;
  color: var(--text-main);
}

.menu-svg {
  color: var(--text-muted);
}

.chevron {
  color: #ccc;
  font-size: 1.5rem;
  line-height: 1;
}

.logout-section {
  margin-top: 8px;
}

.btn-logout {
  width: 100%;
  padding: 16px;
  border-radius: 20px;
  border: 1px solid #eee;
  background: white;
  color: #ff4757;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
}

.btn-logout:active {
  background: #fffafa;
  transform: scale(0.98);
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 40px;
}

.confirm-modal {
  background: white;
  width: 100%;
  max-width: 320px;
  border-radius: 32px;
  padding: 32px;
  text-align: center;
  animation: modalPop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes modalPop {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.modal-icon {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.confirm-modal h3 {
  font-size: 1.4rem;
  color: var(--text-main);
  margin-bottom: 12px;
}

.confirm-modal p {
  font-size: 0.95rem;
  color: var(--text-muted);
  line-height: 1.5;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn-modal-confirm {
  width: 100%;
  padding: 14px;
  border-radius: 16px;
  border: none;
  background: #ff4757;
  color: white;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
}

.btn-modal-confirm:active {
  background: #e04050;
  transform: scale(0.98);
}

.btn-modal-cancel {
  width: 100%;
  padding: 14px;
  border-radius: 16px;
  border: none;
  background: #f8f9fa;
  color: var(--text-muted);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
}
</style>
