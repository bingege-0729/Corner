<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { getRecommend } from '../api/index';

const props = defineProps({
  understanding: {
    type: String,
    default: ''
  },
  places: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['select-place', 'update-results']);
const userInput = ref('');
const messages = ref([]);
const chatContainer = ref(null);
const isTyping = ref(false);

// 移除全局 watch 自动滚动，改为在具体操作后手动控制
// 监听 AI 输入状态，确保“正在输入”气泡可见时平滑展示
watch(isTyping, (val) => {
  if (val) scrollToBottom();
});

// 初始化第一条 AI 消息
onMounted(() => {
  if (props.understanding) {
    messages.value.push({
      role: 'ai',
      content: props.understanding
    });
  }
  // 初始加载也滚动一次
  scrollToBottom();
});

const scrollToMessage = async (index) => {
  await nextTick();
  const el = document.getElementById(`msg-${index}`);
  if (el) {
    // 使用 scrollIntoView 并配合 CSS 的 scroll-margin-top
    el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatContainer.value) {
    chatContainer.value.scrollTo({
      top: chatContainer.value.scrollHeight,
      behavior: 'smooth'
    });
  }
};

const handleSend = async () => {
  if (!userInput.value.trim() || isTyping.value) return;
  
  const text = userInput.value;
  userInput.value = '';
  
  // 记录当前索引
  const userMsgIndex = messages.value.length;
  messages.value.push({
    role: 'user',
    content: text
  });
  
  // 发送后，精准滚动到该用户消息的顶部
  await scrollToMessage(userMsgIndex);
  
  isTyping.value = true;
  
  try {
    const res = await getRecommend({
      userInput: text
    });
    
    if (res.code === 200) {
      const aiMsgIndex = messages.value.length;
      messages.value.push({
        role: 'ai',
        content: res.data.understanding
      });
      
      // 如果 AI 返回了新的地点，通知父组件更新列表
      if (res.data.emotionMatches && res.data.emotionMatches.length > 0) {
        emit('update-results', res.data.emotionMatches);
      }

      // AI 回复完后，滚动到 AI 回复的开头，而不是最底部
      await scrollToMessage(aiMsgIndex);
    }
  } catch (err) {
    console.log('Chat failed', err);
    messages.value.push({
      role: 'ai',
      content: '抱歉，我刚才走神了，请再试一次吧。'
    });
  } finally {
    isTyping.value = false;
  }
};
</script>

<template>
  <div class="result-page">
    <!-- Main Scrollable Area -->
    <div class="chat-container" ref="chatContainer">
      <!-- 1. Chat Bubbles -->
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        :id="'msg-' + index"
        :class="['message-bubble', msg.role]"
      >
        <div class="avatar" v-if="msg.role === 'ai'">✨</div>
        <div class="bubble-content">
          {{ msg.content }}
        </div>
      </div>
      
      <!-- Typing Indicator -->
      <div v-if="isTyping" class="message-bubble ai">
        <div class="avatar">✨</div>
        <div class="bubble-content typing">
          <span>.</span><span>.</span><span>.</span>
        </div>
      </div>

      <!-- 2. Recommendations (Integrated into scroll flow) -->
      <div class="recommendations-flow" v-if="places && places.length > 0">
        <!-- AI Web Search Results Header -->
        <h2 class="section-title" v-if="places.some(p => p.matchType === 'WEB_SEARCH')">
          全网发现 <span>(AI 实时搜索)</span>
        </h2>

        <!-- Memory Match Header -->
        <h2 class="section-title" v-if="places.some(p => p.matchType === 'memory_match')">
          记忆匹配 <span>(根据你的记忆)</span>
        </h2>

        <!-- Emotion Match Header -->
        <h2 class="section-title" v-if="places.some(p => p.matchType === 'emotion_match')">
          情绪匹配 <span>(符合当前心情)</span>
        </h2>

        <!-- Results List -->
        <div class="results-list">
          <div 
            v-for="place in places" 
            :key="place.placeId" 
            class="place-card"
            :style="{ backgroundImage: `url(${place.imageUrl || '/images/place/default.jpg'})` }"
            @click="emit('select-place', place)"
          >
            <div class="card-overlay">
              <div class="tag-row">
                <span v-for="tag in place.moodTags" :key="tag" class="place-tag">{{ tag }}</span>
              </div>
              <h4 class="place-name">{{ place.placeName }}</h4>
              <div class="distance-row">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 21C16.9706 21 21 16.9706 21 12C21 7.02944 16.9706 3 12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M12 8V12L14 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>{{ place.distanceText || '距离需自行确认' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Input Section -->
    <div class="chat-input-section">
      <div class="input-wrapper">
        <input 
          v-model="userInput" 
          type="text" 
          placeholder="还想去什么样的角落？"
          @keyup.enter="handleSend"
        />
        <button class="send-btn" @click="handleSend" :disabled="!userInput.trim()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 2L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.result-page {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fcfbf9;
  position: relative;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 80px 24px 180px; /* 顶部留出 TopBar 空间，底部留出输入框空间 */
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-bubble {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message-bubble.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-bubble.ai {
  align-self: flex-start;
}

.avatar {
  width: 32px;
  height: 32px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  font-size: 14px;
  flex-shrink: 0;
}

.message-bubble {
  display: flex;
  gap: 12px;
  position: relative;
  /* 增加滚动边距，防止被顶部固定栏遮挡 */
  scroll-margin-top: 100px;
}

.user {
  justify-content: flex-end;
}

.ai {
  justify-content: flex-start;
}

.bubble-content {
  padding: 14px 18px;
  border-radius: 20px;
  font-size: 1rem;
  line-height: 1.5;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
}

.user .bubble-content {
  background: #5a6b63;
  color: white;
  border-bottom-right-radius: 4px;
}

.ai .bubble-content {
  background: white;
  color: var(--text-main);
  border-bottom-left-radius: 4px;
}

.typing span {
  animation: blink 1.4s infinite both;
  font-size: 20px;
  font-weight: bold;
}

.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes blink {
  0% { opacity: .2; }
  20% { opacity: 1; }
  100% { opacity: .2; }
}

/* Places Section */
.places-section {
  padding: 20px 0;
  flex-shrink: 0;
  margin-top: 10px;
}

.section-title {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-weight: 400;
  margin: 32px 0 16px;
  letter-spacing: 0.02em;
}

.section-title span {
  opacity: 0.6;
  font-size: 0.75rem;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.place-card {
  height: 360px;
  background-size: cover;
  background-position: center;
  /* 兜底背景：优雅的渐变 */
  background-color: #f0f2f1;
  background-image: linear-gradient(135deg, #f0f2f1 0%, #e1e6e4 100%);
  border-radius: 28px;
  overflow: hidden;
  position: relative;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.card-overlay {
  padding: 24px;
  background: linear-gradient(0deg, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0.2) 50%, rgba(0,0,0,0) 100%);
  color: white;
}

.tag-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.place-tag {
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.place-name {
  font-size: 1.5rem;
  font-weight: 500;
  margin-bottom: 8px;
  letter-spacing: 0.02em;
}

.distance-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  opacity: 0.9;
}

/* Chat Input Section Styles */
.chat-input-section {
  position: fixed;
  bottom: 100px; /* 留出底部导航栏的高度 */
  left: 0;
  right: 0;
  padding: 10px 24px 30px;
  background: transparent; /* 去掉背景和渐变 */
  z-index: 100;
}

.input-wrapper {
  background: white;
  height: 56px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  padding: 0 8px 0 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.input-wrapper input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 1rem;
  color: var(--text-main);
  background: transparent;
}

.input-wrapper input::placeholder {
  color: var(--text-muted);
}

.send-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.send-btn:disabled {
  background: #eee;
  color: #ccc;
  cursor: not-allowed;
}

.send-btn:active:not(:disabled) {
  transform: scale(0.9);
}
</style>
