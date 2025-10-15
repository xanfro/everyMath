<template>
  <view class="container">
    <!-- 背景数学元素 -->
    <view class="bg-elements">
      <view class="math-symbol" :style="{top: '10%', left: '8%', animationDelay: '0s'}">+</view>
      <view class="math-symbol" :style="{top: '60%', left: '15%', animationDelay: '0.8s'}">-</view>
      <view class="math-symbol" :style="{top: '30%', left: '85%', animationDelay: '1.2s'}">×</view>
    </view>

    <!-- 顶部标题区域 -->
    <view class="header">
      <view class="header-text">
        <text class="title">
          <span class="title-char">注</span>
          <span class="title-char" :style="{animationDelay: '0.2s'}">册</span>
          <span class="title-char" :style="{animationDelay: '0.4s'}">账</span>
          <span class="title-char" :style="{animationDelay: '0.6s'}">号</span>
        </text>
      </view>
    </view>

    <!-- 注册表单容器 -->
    <view class="form-container">
      <!-- 狐狸图片（聚焦显示） -->
      <image
        class="fox-image"
        :class="{ 'show': isFocused }"
        src="/static/fox.png"
        mode="widthFix"
      ></image>

      <view class="register-form">
        <!-- 年级选择 -->
        <view class="form-group">
          <text class="form-label">年级选择</text>
          <view class="input-group grade-select-group">
            <picker
              class="picker-select"
              mode="selector"
              :range="gradesOptions"
              :value="gradeIndex"
              @change="onGradeChange"
            >
              <text class="select-text">{{ selectedGrade || '请选择年级' }}</text>
            </picker>
          </view>
        </view>


        <!-- 用户名输入 -->
        <view class="form-group">
          <text class="form-label">用户名</text>
          <view class="input-group">
            <input
              class="input-field"
              type="text"
              placeholder="请设置登录用户名"
              v-model="username"
              @focus="handleFocus(true)"
              @blur="handleFocus(false)"
            />
          </view>
        </view>

        <!-- 密码输入 -->
        <view class="form-group">
          <text class="form-label">密码</text>
          <view class="input-group password-group">
            <input
              class="input-field"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请设置登录密码"
              v-model="password"
              @focus="handleFocus(true)"
              @blur="handleFocus(false)"
            />
            <view class="password-toggle" @click="togglePassword">
              <uni-icons 
                :type="showPassword ? 'eye-slash' : 'eye'" 
                size="20" 
                :color="showPassword ? '#3b82f6' : '#94a3b8'"
              />
            </view>
          </view>
        </view>

        <!-- 确认密码输入 -->
        <view class="form-group">
          <text class="form-label">确认密码</text>
          <view class="input-group password-group">
            <input
              class="input-field"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              v-model="confirmPassword"
              @focus="handleFocus(true)"
              @blur="handleFocus(false)"
            />
            <view class="password-toggle" @click="toggleConfirmPassword">
              <uni-icons 
                :type="showConfirmPassword ? 'eye-slash' : 'eye'" 
                size="20" 
                :color="showConfirmPassword ? '#3b82f6' : '#94a3b8'"
              />
            </view>
          </view>
        </view>

        <!-- 注册按钮 -->
        <button
          class="register-btn"
          @click="handleRegister"
          :class="{ 'active': isFormValid && !loading }"
          :disabled="!isFormValid || loading"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <!-- 已有账号链接（居中显示） -->
        <text class="login-link" @click="navigateToLogin">
          已有账号？登录
        </text>
      </view>
    </view>

    <!-- 底部动态元素 -->
    <view class="bottom-elements">
      <view class="wave-animation"></view>
      <view class="floating-numbers">
        <span class="number" :style="{top: '20%', left: '15%', fontSize: '22px'}">1</span>
        <span class="number" :style="{top: '40%', left: '40%', fontSize: '26px'}">2</span>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import uniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import { registerUser } from '../../api/index.js';

// 表单数据 - 只支持学生注册
const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const gradeIndex = ref(-1);
const selectedGrade = ref('');
const gradesOptions = ref(['1年级', '2年级', '3年级', '4年级', '5年级', '6年级']);
const isFocused = ref(false);
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);

// 年级选择处理
const onGradeChange = (e) => {
  gradeIndex.value = e.detail.value;
  selectedGrade.value = gradesOptions.value[gradeIndex.value];
};

// 密码显示切换
const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

// 表单验证
const isFormValid = computed(() => {
  if (!username.value.trim() || !password.value.trim() || !confirmPassword.value.trim()) {
    return false;
  }
  // 必须选择年级
  if (!selectedGrade.value) {
    return false;
  }
  // 密码和确认密码必须一致
  if (password.value !== confirmPassword.value) {
    return false;
  }
  return true;
});

// 聚焦状态处理
const handleFocus = (focusState) => {
  isFocused.value = focusState;
};

// 显示加载状态
const showLoading = (title = '加载中...') => {
  uni.showLoading({ title, mask: true });
};

// 隐藏加载状态
const hideLoading = () => {
  uni.hideLoading();
};

// 注册处理
const handleRegister = async () => {
  if (!isFormValid.value) {
    uni.showToast({
      title: '请填写所有必填项',
      icon: 'none'
    });
    return;
  }

  loading.value = true;

  try {
    showLoading('注册中...');

    const gradeValue = gradeIndex.value + 1; // 年级值从1开始

    const response = await registerUser({
      username: username.value,
      password: password.value,
      grade: gradeValue
    });

    hideLoading();

    if (response.code === 0) {
      uni.showToast({
        title: '注册成功',
        icon: 'success'
      });
      setTimeout(() => navigateToLogin(), 1500);
    } else {
      uni.showToast({
        title: response.message || '注册失败',
        icon: 'none'
      });
    }
  } catch (error) {
    hideLoading();
    console.error('注册失败:', error);
    uni.showToast({
      title: error.message || '注册失败，请稍后重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 跳转到登录页
const navigateToLogin = () => {
  uni.redirectTo({ url: '/pages/login/login' });
};
</script>

<style lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

// 页面容器
.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 60px 20px 0;
  position: relative;
  overflow: hidden;
  align-items: center;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
}

// 背景数学元素
.bg-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.math-symbol {
  position: absolute;
  color: rgba(30, 64, 175, 0.1);
  font-size: 80px;
  font-weight: bold;
  transform: rotate(-15deg);
  animation: float 12s infinite ease-in-out;
}

// 顶部标题区域
.header {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
  z-index: 10;
  padding: 5px 0;
  width: 100%;
  max-width: 500px;
}

.title {
  font-size: 38px;
  font-weight: bold;
  letter-spacing: 2px;
  display: inline-block;
  margin-bottom: 8px;
  padding: 8px 15px;
  position: relative;
}

.title-char {
  display: inline-block;
  background: linear-gradient(90deg, #1e3a8a, #1e40af);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 2px 10px rgba(30, 64, 175, 0.4);
  animation: bounce 0.6s ease-out forwards;
  transform: translateY(20px);
  opacity: 0;
}

// 表单容器
.form-container {
  width: 100%;
  max-width: 380px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  padding: 30px 25px;
  box-shadow: 0 8px 30px rgba(30, 64, 175, 0.15);
  position: relative;
  z-index: 10;
  margin-bottom: 60px;
  overflow: hidden;
}

// 狐狸图片
.fox-image {
  position: absolute;
  right: 15px;
  bottom: -60px;
  width: 70px;
  opacity: 0;
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);
  z-index: 15;
  pointer-events: none;
}

.fox-image.show {
  opacity: 1;
  bottom: -15px;
  transform: rotate(5deg);
}

// 表单样式
.register-form {
  width: 100%;
}

.form-group {
  margin-bottom: 22px;
}

.form-label {
  display: block;
  font-size: 15px;
  color: #1e40af;
  font-weight: 500;
  margin-bottom: 8px;
  padding-left: 2px;
}

// 年级选择器样式
.picker-select {
  width: 100%;
  height: 100%;
  padding: 0;
  background-color: transparent;
  border: none;
  outline: none;
  font-size: 17px;
  color: #1e3a8a;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.select-text {
  color: #1e3a8a;
  text-align: left;
  width: 100%;
}

.select-text:empty::before {
  content: '请选择年级';
  color: #94a3b8;
}

// 年级选择器特殊样式
.grade-select-group {
  width: 40%;
  max-width: 200px;
}

// 输入框样式
.input-group {
  background-color: white;
  border-radius: 12px;
  padding: 0 16px;
  height: 52px;
  box-shadow: 0 3px 10px rgba(30, 64, 175, 0.08);
  border: 2px solid rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  position: relative;
}

.input-group:hover {
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.12);
}

.input-group:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 3px 12px rgba(30, 64, 175, 0.2);
}

.input-field {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  font-size: 17px;
  color: #1e3a8a;
  background: transparent;
}

.input-field::placeholder {
  color: #94a3b8;
  font-size: 16px;
}

// 密码输入框特殊样式
.password-group {
  padding-right: 50px; // 为眼睛按钮留出空间
}

.password-toggle {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.password-toggle:hover {
  opacity: 0.7;
}

// 注册按钮
.register-btn {
  width: 100%;
  height: 50px;
  background: linear-gradient(90deg, #60a5fa, #93c5fd);
  color: white;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 500;
  border: none;
  box-shadow: 0 4px 15px rgba(96, 165, 250, 0.3);
  transition: all 0.3s ease;
  margin-bottom: 15px;
}

.register-btn.active {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

.register-btn:active.active {
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  transform: scale(0.98);
}

// 登录链接（居中显示）
.login-link {
  display: block;
  text-align: center;
  color: #3b82f6;
  font-size: 14px;
  cursor: pointer;
  padding: 5px 0;
}

.login-link:hover {
  color: #1e40af;
  text-decoration: underline;
}

// 底部动态元素
.bottom-elements {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120px;
  z-index: 5;
  overflow: hidden;
}

.wave-animation {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 60px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='rgba(96, 165, 250, 0.1)' fill-opacity='1' d='M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,229.3C1248,224,1344,160,1392,128L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
  background-size: 50% 100%;
  animation: wave 15s linear infinite;
}

.number {
  position: absolute;
  color: rgba(30, 64, 175, 0.2);
  font-weight: bold;
  animation: float-random 8s infinite ease-in-out;
}

// 动画效果
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}

@keyframes bounce {
  0% { transform: translateY(20px); opacity: 0; }
  60% { transform: translateY(-5px); opacity: 1; }
  100% { transform: translateY(0); opacity: 1; }
}

@keyframes wave {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

@keyframes float-random {
  0%, 100% { transform: translateY(0); opacity: 0.6; }
  50% { transform: translateY(-25px); opacity: 1; }
}

// 响应式调整
@media (max-width: 375px) {
  .title {
    font-size: 32px;
  }

  .input-group, .register-btn {
    height: 48px;
  }
}
</style>
