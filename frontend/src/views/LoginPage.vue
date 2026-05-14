<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/index.js'

const router = useRouter()
const phone = ref('')                    
const verificationCode = ref('')         // 验证码
const isSendingCode = ref(false)         // 是否正在发送验证码
const codeCountdown = ref(0)             // 验证码倒计时
const isLoading = ref(false)             // 是否正在登录

const sendVerificationCode = async () => {
  if (!phone.value || phone.value.length !== 11) {
    alert('请输入正确的手机号')
    return
  }
  
  isSendingCode.value = true
  codeCountdown.value = 60
  
  const countdown = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdown)
      isSendingCode.value = false
    }
  }, 1000)
  
  console.log('发送验证码到:', phone.value)
}

const handleLogin = async () => {
  if (!phone.value || phone.value.length !== 11) {
    alert('请输入正确的手机号')
    return
  }
  
  isLoading.value = true
  
  try {
    // 调用登录接口
    const result = await userApi.login(phone.value)
    
    if (result.code === 200) {
      const { user_id, nickname, token } = result.data
      
      localStorage.setItem('user_id', user_id)
      localStorage.setItem('nickname', nickname)
      localStorage.setItem('token', token)
      
      // 跳转到首页
      router.push('/')
    } else {
      alert(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录请求失败:', error)
    alert(error.message || '登录失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="logo-section">
      <h1 class="app-name">Corner</h1>
      <p class="app-slogan">世界喧嚣，</p>
      <p class="app-slogan">替你找安静。</p>
    </div>
    
    <div class="form-section">
      <div class="input-group">
        <div class="phone-prefix">+86</div>
        <input
          v-model="phone"
          type="tel"
          maxlength="11"
          placeholder="输入手机号"
          class="phone-input"
        />
      </div>
      
      <div class="input-group">
        <input
          v-model="verificationCode"
          type="text"
          maxlength="6"
          placeholder="输入验证码"
          class="code-input"
        />
        <button
          @click="sendVerificationCode"
          :disabled="isSendingCode"
          class="send-code-btn"
        >
          {{ isSendingCode ? `${codeCountdown}s` : '获取验证码' }}
        </button>
      </div>
      
      <button
        @click="handleLogin"
        :disabled="isLoading"
        class="login-btn"
      >
        {{ isLoading ? '登录中...' : '手机号登录' }}
      </button>
    </div>
    
    <div class="footer">
      <span class="footer-text">登录即代表同意</span>
      <span class="footer-link">用户协议</span>
      <span class="footer-text">和</span>
      <span class="footer-link">隐私政策</span>
    </div>
  </div>
</template>

<style>
.login-page {
  min-height: 100vh;
  background-color: #FAF7F2;
  display: flex;
  flex-direction: column;
  padding: 0 24px;
  box-sizing: border-box;
}

.logo-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding-top: 80px;
}

.app-name {
  font-size: 48px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 32px;
}

.app-slogan {
  font-size: 20px;
  color: #6b7280;
  margin: 0;
  line-height: 1.8;
}

.form-section {
  margin-bottom: 40px;
}

.input-group {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 12px;
  padding: 0 16px;
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
}

.phone-prefix {
  font-size: 16px;
  color: #6b7280;
  padding-right: 16px;
  border-right: 1px solid #e5e7eb;
  margin-right: 16px;
}

.phone-input,
.code-input {
  flex: 1;
  padding: 16px 0;
  font-size: 16px;
  color: #374151;
  border: none;
  outline: none;
  background: transparent;
}

.phone-input::placeholder,
.code-input::placeholder {
  color: #9ca3af;
}

.send-code-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: #5B7B5E;
  background: none;
  border: 1px solid #5B7B5E;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.send-code-btn:hover:not(:disabled) {
  background-color: rgba(91, 123, 94, 0.1);
}

.send-code-btn:disabled {
  color: #9ca3af;
  border-color: #e5e7eb;
  cursor: not-allowed;
}

.login-btn {
  width: 100%;
  padding: 16px;
  background-color: #5B7B5E;
  color: white;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  margin-top: 8px;
  transition: background-color 0.2s;
}

.login-btn:hover:not(:disabled) {
  background-color: #4a6a4e;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.footer {
  text-align: center;
  padding-bottom: 32px;
  font-size: 12px;
  color: #9ca3af;
}

.footer-link {
  color: #5B7B5E;
}
</style>