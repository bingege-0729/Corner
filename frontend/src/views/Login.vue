<script setup>
import { ref } from 'vue';

const phone = ref('');
const password = ref('');

const emit = defineEmits(['login']);

const handleLogin = () => {
  if (!phone.value || phone.value.length < 11 || !password.value) return;
  emit('login', { phone: phone.value, password: password.value });
};
</script>

<template>
  <div class="login-page">
    <div class="content-wrapper">
      <div class="header-section">
        <h1 class="logo-text">Corner</h1>
        <h2 class="slogan-text">世界喧嚣，<br>替你找安静。</h2>
      </div>

      <div class="form-section">
        <!-- 手机号输入 -->
        <div class="input-group">
          <span class="prefix">+86</span>
          <input 
            type="tel" 
            v-model="phone" 
            placeholder="输入手机号" 
            maxlength="11"
            inputmode="tel"
          />
        </div>

        <!-- 密码输入 -->
        <div class="input-group">
          <div class="icon-prefix">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 11H5C3.89543 11 3 11.8954 3 13V20C3 21.1046 3.89543 22 5 22H19C20.1046 22 21 21.1046 21 20V13C21 11.8954 20.1046 11 19 11Z" stroke="var(--text-muted)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="var(--text-muted)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <input 
            type="password" 
            v-model="password" 
            placeholder="输入密码" 
          />
        </div>

        <button 
          class="btn-primary login-btn" 
          :class="{ 'dimmed': !phone || phone.length < 11 || !password }"
          @click="handleLogin"
        >
          一键注册并登录 <span class="arrow">→</span>
        </button>
      </div>
    </div>

    <div class="footer-section">
      <p class="text-muted">
        登录即代表同意 <span class="link">用户协议</span> 和 <span class="link">隐私政策</span>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 0 32px;
  background: var(--bg-gradient);
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-top: 40px;
}

.header-section {
  margin-bottom: 80px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-text {
  font-size: 4rem;
  margin-bottom: 16px;
  opacity: 0.9;
}

.slogan-text {
  font-size: 2.2rem;
  line-height: 1.3;
  color: var(--text-main);
  font-weight: 300;
  padding-left: 25px;
}

.form-section {
  width: 100%;
}

.input-group {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: var(--radius-md);
  height: 64px;
  padding: 0 1.5rem;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  transition: var(--transition);
}

.input-group:focus-within {
  border-color: var(--primary-color);
  background: #ffffff;
}

.prefix, .icon-prefix {
  color: var(--text-muted);
  font-weight: 500;
  margin-right: 16px;
  padding-right: 16px;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
}

input {
  font-size: 1.1rem;
  font-weight: 400;
  flex: 1;
  min-width: 0;
}

.btn-text {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 0.95rem;
  font-weight: 500;
  padding-left: 16px;
  transition: var(--transition);
  white-space: nowrap;
  flex-shrink: 0;
}

.btn-text.disabled {
  color: var(--text-light);
  pointer-events: none;
}

.login-btn {
  margin-top: 24px;
  height: 64px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 32px;
}

.login-btn.dimmed {
  opacity: 0.6;
  transform: none;
}

.arrow {
  font-size: 1.4rem;
  margin-left: 8px;
  transition: transform 0.3s ease;
}

.login-btn:not(.dimmed):active .arrow {
  transform: translateX(4px);
}

.footer-section {
  padding-bottom: calc(var(--safe-bottom) + 20px);
  padding-top: 12px;
  text-align: center;
}

.text-muted {
  font-size: 0.8rem;
  opacity: 0.7;
}

/* 进场动画 */
.header-section, .form-section {
  animation: slideUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
