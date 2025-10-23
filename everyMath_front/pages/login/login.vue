<template>
  <view class="container">
    <!-- 上方背景数学元素 -->
    <view class="bg-elements top">
      <view class="math-symbol" :style="{top: '5%', left: '5%', animationDelay: '0s'}">+</view>
      <view class="math-symbol" :style="{top: '70%', left: '10%', animationDelay: '0.5s'}">-</view>
      <view class="math-symbol" :style="{top: '30%', left: '20%', animationDelay: '1s'}">×</view>
      <view class="math-symbol" :style="{top: '50%', left: '80%', animationDelay: '1.5s'}">÷</view>
      <view class="math-symbol" :style="{top: '15%', left: '70%', animationDelay: '2s'}">+</view>
    </view>

    <!-- 顶部标题区域 -->
    <view class="header">
      <view class="title-decoration">
        <view class="title-circle left"></view>
        <view class="title-circle right"></view>
      </view>

      <view class="header-text">
        <text class="title">
          <span class="title-char">天</span>
          <span class="title-char" :style="{animationDelay: '0.2s'}">天</span>
          <span class="title-char" :style="{animationDelay: '0.4s'}">口</span>
          <span class="title-char" :style="{animationDelay: '0.6s'}">算</span>
        </text>

				<image src="/static/fox_header.png" mode="widthFix" class="fox-header" ></image>

        <text class="subtitle">加减乘除乐无限，数学天地任我闯！</text>
        <text class="subtitle">天天口算，欢迎超级小英雄！</text>
      </view>
    </view>

    <!-- 表单容器（包含狐狸图片） -->
    <view class="form-container">
      <!-- 狐狸图片 - 从底部上升效果 -->
      <image
        class="fox-image"
        :class="{ 'show': hasFocused }"
        src="/static/fox.png"
        mode="widthFix"
      ></image>

      <view class="login-form">
        <view class="input-group">
          <uni-icons type="person-filled" size="22" color="#1e40af" class="input-icon" />
          <input
            class="input-field"
            type="text"
            placeholder="请输入用户名"
            v-model="username"
            @focus="handleFocus"
						@blur="handleBlur"
          />
        </view>

        <view class="input-group">
          <uni-icons type="locked-filled" size="22" color="#1e40af" class="input-icon" />
          <input
            class="input-field"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            v-model="password"
            @focus="handleFocus"
						@blur="handleBlur"
          />
          <uni-icons 
            :type="showPassword ? 'eye-slash-filled' : 'eye-filled'" 
            size="20" 
            color="#94a3b8" 
            class="password-toggle-icon" 
            @click="togglePasswordVisibility"
            v-show="password.length > 0"
          />
        </view>

        <button
          class="login-btn"
          @click="handleLogin"
          :class="{ 'active': username && password }"
          :disabled="!username || !password"
        >
          登录
        </button>

        <text class="register-link" @click="handleRegister">
          还没有账号？注册
        </text>
      </view>
    </view>

    <!-- 页面下方动态元素 -->
    <view class="bottom-elements">
      <view class="wave-animation"></view>
      <view class="floating-numbers">
        <span class="number" :style="{top: '10%', left: '10%', fontSize: '60px', animationDelay: '0s'}">1</span>
        <span class="number" :style="{top: '30%', left: '30%', fontSize: '40px', animationDelay: '0.3s'}">2</span>
        <span class="number" :style="{top: '10%', left: '50%', fontSize: '50px', animationDelay: '0.6s'}">3</span>
        <span class="number" :style="{top: '20%', left: '70%', fontSize: '55px', animationDelay: '0.9s'}">4</span>
        <span class="number" :style="{top: '50%', left: '90%', fontSize: '26px', animationDelay: '1.2s'}">5</span>
        <span class="number" :style="{top: '50%', left: '20%', fontSize: '45px', animationDelay: '1.5s'}">6</span>
        <span class="number" :style="{top: '40%', left: '80%', fontSize: '35px', animationDelay: '1.8s'}">7</span>
      </view>
    </view>
		</view>
  </template>

<script setup>
import { ref } from 'vue';
import { loginUser } from '@/api/index.js';
import { showLoading, hideLoading } from '@/api/utils.js';

const username = ref('');
const password = ref('');
const hasFocused = ref(false); // 控制狐狸图片显示状态，一旦聚焦过就保持显示
const showPassword = ref(false); // 控制密码显示/隐藏状态

// 处理输入框聚焦 - 一旦聚焦就永久显示
const handleFocus = () => {
  hasFocused.value = true;
};

const handleBlur = () =>{
	hasFocused.value= false;
}

// 切换密码显示/隐藏状态
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const handleLogin = async () => {
  if (!username.value || !password.value) {
    uni.showToast({
      title: '请输入用户名和密码',
      icon: 'none'
    });
    return;
  }

  try {
    showLoading('登录中...');

    const response = await loginUser({
      username: username.value,
      password: password.value
    });

    hideLoading();

    if (response.code === 0) {
      console.log("登录成功，响应数据:", response);
      
      // 保存用户数据到本地存储
      if (response.data && response.data.user) {
        console.log("保存后端返回的用户数据:", response.data.user);
        uni.setStorageSync('userData', JSON.stringify(response.data.user));
      } else {
        // 如果后端没有返回用户数据，创建一个默认的
        const defaultUser = {
          id: Date.now(),
          username: username.value,
          grade: '3' // 默认3年级
        };
        console.log("创建默认用户数据:", defaultUser);
        uni.setStorageSync('userData', JSON.stringify(defaultUser));
      }
      
      // 保存token
      if (response.data && response.data.token) {
        uni.setStorageSync('token', response.data.token);
      }
      
      // 验证数据是否保存成功
      const savedData = uni.getStorageSync('userData');
      console.log("验证保存的用户数据:", savedData);
      
      uni.showToast({
        title: '登录成功',
        icon: 'success'
      });
			console.log("登录成功");

      // 延迟跳转，确保数据保存完成
      setTimeout(() => {
        console.log("开始跳转到首页");
        uni.redirectTo({ 
          url: '/pages/home/home',
          success: (res) => {
            console.log("跳转成功:", res);
          },
          fail: (err) => {
            console.error("跳转失败:", err);
            // 如果redirectTo失败，尝试navigateTo
            uni.navigateTo({
              url: '/pages/home/home'
            });
          }
        });
      }, 1000);

			console.log("跳转指令已发出");
    } else {
      uni.showToast({
        title: response.message || '登录失败',
        icon: 'none'
      });
    }
  } catch (error) {
    hideLoading();
    console.error('登录失败:', error);
    uni.showToast({
      title: error.message || '登录失败，请稍后重试',
      icon: 'none'
    });
  }
};

const handleRegister = () => {
  uni.navigateTo({ url: '/pages/register/register' });
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

// 上方背景元素
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
  margin-bottom: 40px;
  z-index: 10;
  padding: 10px 0;
  width: 100%;
  max-width: 500px;
}

.title-decoration {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  transform: translateY(-50%);
  z-index: 1;
  pointer-events: none;
}

.title-circle {
  width: 12px;
  height: 12px;
  background: linear-gradient(90deg, #1e40af, #3b82f6);
  border-radius: 50%;
  position: absolute;
}

.title-circle.left {
  left: 15%;
  top: 30%;
  box-shadow: 0 0 15px rgba(30, 64, 175, 0.5);
  animation: pulse 3s infinite;
}

.title-circle.right {
  right: 15%;
  bottom: 30%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  box-shadow: 0 0 15px rgba(96, 165, 250, 0.5);
  animation: pulse 4s infinite;
  animation-delay: 1s;
}

// 标题样式
.title {
  font-size: 42px;
  font-weight: bold;
  letter-spacing: 2px;
  display: inline-block;
  margin-bottom: 15px;
  padding: 10px 20px;
  position: relative;
	background-image: linear-gradient(to left, #2f71ff, #58cafe);
	-webkit-background-clip: text; /* 关键：将背景裁剪为文字形状 */
	background-clip: text;
	-webkit-text-fill-color: transparent; /* 关键：将文字本身颜色设为透明，以显示背景 */
	color: transparent; /* 非Webkit内核浏览器的备用方案 */
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

.title::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 30%;
  background-color: rgba(30, 64, 175, 0.1);
  border-radius: 50%;
  transform: translateY(-50%);
  z-index: -1;
}

// 副标题
.subtitle {
  font-size: 15px;
  color: #6cadfb;
  letter-spacing: 0.3px;
  line-height: 1.6;
  display: block;
  padding: 2px 0;
}

// 表单容器
.form-container {
  width: 100%;
  max-width: 380px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 30px 25px;
  box-shadow: 0 8px 30px rgba(30, 64, 175, 0.15);
  position: relative;
  z-index: 10;
  margin-bottom: 60px;
  overflow: hidden; // 确保超出容器的部分被隐藏
}

// 狐狸图片样式 - 从底部上升效果
.fox-image {
  position: absolute;
  right: 15px; // 右侧固定位置
  bottom: -60px; // 初始隐藏在容器底部外（图片高度的80%左右）
  width: 70px; // 图片宽度
  opacity: 0; // 初始透明
  transform: translateY(0) rotate(0deg); // 初始位置
  transition: all 0.8s cubic-bezier(0.22, 1, 0.36, 1); // 缓动曲线，上升更自然
  z-index: 15; // 确保在表单上方显示
  pointer-events: none; // 不影响交互
}

// 聚焦后显示并上升（不完全显示）
.fox-image.show {
  opacity: 1; // 完全显示
  bottom: -15px; // 上升后位置（约20%仍在容器外）
  transform: translateY(0) rotate(5deg); // 轻微旋转增加生动感
}

// 输入框样式
.input-group {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 12px;
  padding: 0 16px;
  margin-bottom: 20px;
  height: 52px;
  box-shadow: 0 3px 10px rgba(30, 64, 175, 0.08);
  transition: all 0.3s ease;
  border: 2px solid rgba(59, 130, 246, 0.3);
}

.input-group:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 3px 12px rgba(30, 64, 175, 0.2);
}

.input-group:hover {
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.12);
}

.input-icon {
  margin-right: 14px;
  width: 24px;
}

.password-toggle-icon {
  margin-left: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 2px;
  border-radius: 4px;
  
  &:hover {
    background-color: rgba(148, 163, 184, 0.1);
    color: #64748b;
  }
  
  &:active {
    transform: scale(0.95);
    background-color: rgba(148, 163, 184, 0.2);
  }
}

.input-field {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  font-size: 17px;
  color: #1e3a8a;
  background-color: transparent;
  padding: 0;
}

.input-field::placeholder {
  color: #94a3b8;
  font-size: 16px;
}

// 登录按钮样式
.login-btn {
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
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
}

.login-btn.active {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

.login-btn:active.active {
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  transform: scale(0.98);
}

.login-btn:disabled {
  background: linear-gradient(90deg, #bfdbfe, #dbeafe);
  color: #94a3b8;
  box-shadow: 0 3px 8px rgba(30, 64, 175, 0.1);
  cursor: default;
}

// 注册链接
.register-link {
  display: block;
  text-align: center;
  color: #3b82f6;
  font-size: 14px;
  cursor: pointer;
  padding-right: 4px;
  transition: all 0.2s ease;
}

.register-link:hover {
  color: #1e40af;
  text-decoration: underline;
}

// 页面下方动态元素
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

.floating-numbers {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.number {
  position: absolute;
  color: rgba(30, 64, 175, 0.2);
  font-weight: bold;
  transform-origin: center;
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

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.3); opacity: 1; }
}

@keyframes wave {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

@keyframes float-random {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-25px) rotate(10deg);
    opacity: 1;
  }
}

@keyframes fox-header{
	0%, 100%{
		opacity: 0;
	}
	70%{
		transform: rotate(50deg) ;
		opacity: 1;
	}
}

// 响应式调整
@media (max-width: 375px) {
  .title {
    font-size: 36px;
  }

  .subtitle {
    font-size: 14px;
  }

  .form-container {
    padding: 25px 20px;
    margin-bottom: 40px;
  }

  .input-group,
  .login-btn {
    height: 48px;
  }

  .math-symbol {
    font-size: 60px;
  }

  .bottom-elements {
    height: 100px;
  }

  .number {
    font-size: clamp(16px, 4vw, 24px) !important;
  }

  .fox-image {
    width: 60px; // 小屏幕缩小图片
    right: 10px;
  }
}

.fox-header{
	position: absolute;
	right: 160rpx;
	width: 120rpx;
	transform-origin: right bottom;
	z-index: -1;
	transition: all 0.3s ease;
	animation: fox-header 4s infinite ease-in-out;
}

</style>
