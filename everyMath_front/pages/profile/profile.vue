<template>
  <view class="profile-container">
    <!-- 背景数学元素 -->
    <view class="bg-elements">
      <view class="math-symbol" :style="{top: '5%', left: '5%', animationDelay: '0s'}">+</view>
      <view class="math-symbol" :style="{top: '70%', left: '10%', animationDelay: '0.5s'}">-</view>
      <view class="math-symbol" :style="{top: '30%', left: '20%', animationDelay: '1s'}">×</view>
      <view class="math-symbol" :style="{top: '50%', left: '80%', animationDelay: '1.5s'}">÷</view>
      <view class="math-symbol" :style="{top: '15%', left: '70%', animationDelay: '2s'}">+</view>
    </view>

    <!-- 导航栏 -->
    <view class="navbar">
      <text class="navbar-title">我的</text>
    </view>

    <!-- 个人信息区域 -->
    <view class="profile-header">
      <view class="avatar-container" @click="editAvatar">
        <image
          class="avatar"
          :src="avatarUrl || '/static/logo.png'"
          mode="aspectFill"
        ></image>
        <view class="camera-icon">
          <uni-icons type="camera" size="20" color="#fff"></uni-icons>
        </view>
      </view>

      <view class="user-info">
        <view class="username-row">
          <text class="username">{{ nickname || '点击设置昵称' }}</text>
          <uni-icons type="compose" size="18" color="#4299e1" @click="openNicknameModal"></uni-icons>
        </view>
        <text class="user-id">ID: {{ userId || '获取中...' }}</text>
      </view>
    </view>

    <!-- 名称设置弹窗 -->
    <uni-popup ref="nicknamePopup" type="center">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">设置昵称</text>
          <uni-icons type="close" size="24" color="#999" @click="closeNicknameModal" class="close-icon"></uni-icons>
        </view>

        <view class="input-container">
          <input
            class="nickname-input"
            v-model="newNickname"
            placeholder="请输入您的昵称"
            maxlength="20"
          >
        </view>

        <view class="button-group">
          <button class="cancel-btn" @click="closeNicknameModal">取消</button>
          <button class="confirm-btn" @click="confirmNickname">确定</button>
        </view>
      </view>
    </uni-popup>


    <!-- 设置选项 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">设置</text>
      </view>

      <view class="settings-list">
        <view class="setting-item" @click="clearCache">
          <view class="setting-main">
            <uni-icons type="trash" size="20" color="#4299e1"></uni-icons>
            <text class="setting-text">清除缓存</text>
          </view>
          <uni-icons type="forward" size="16" color="#999"></uni-icons>
        </view>

        <view class="setting-item" @click="checkVersion">
          <view class="setting-main">
            <uni-icons type="loop" size="20" color="#4299e1"></uni-icons>
            <text class="setting-text">检查更新</text>
          </view>
          <uni-icons type="forward" size="16" color="#999"></uni-icons>
        </view>

        <view class="setting-item" @click="showAbout">
          <view class="setting-main">
            <uni-icons type="info" size="20" color="#4299e1"></uni-icons>
            <text class="setting-text">关于我们</text>
          </view>
          <uni-icons type="forward" size="16" color="#999"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 退出登录按钮 -->
    <view class="logout-section">
      <button class="logout-btn" @click="logout">退出登录</button>
    </view>

    <!-- 底部导航栏 -->
    <ButtomNav @show-practice-setup="showPracticeSetup = true"/>

    <!-- 练习设置弹窗 -->
    <PracticeSetup
      :visible="showPracticeSetup"
      @close="showPracticeSetup = false"
      @confirm="startPractice"
    />
  </view>
</template>

<script setup>
import ButtomNav from '/components/buttom-nav.vue';
import PracticeSetup from '/components/practice-setup.vue';
import { ref, nextTick, onMounted } from 'vue';
import { getUserData, getUserId, setUserData, clearToken, clearUserData } from '@/api/utils.js';
import { updateUsername } from '@/api/index.js'

// 状态管理
const showPracticeSetup = ref(false);
const nicknamePopup = ref(null);
const newNickname = ref('');

// 用户信息
const avatarUrl = ref('');
const nickname = ref('');
const userId = ref('');

// 加载用户信息
const loadUserInfo = () => {
  try {
    const userData = getUserData();
    if (userData) {
      nickname.value = userData.username || '未设置昵称';
      userId.value = userData.id || '';
    } else {
      // 如果没有用户数据，跳转到登录页
      uni.reLaunch({ url: '/pages/login/login' });
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
    uni.reLaunch({ url: '/pages/login/login' });
  }
};

// 页面加载时获取用户信息
onMounted(() => {
  loadUserInfo();
});


// 编辑头像
const editAvatar = () => {
  uni.showToast({
    title: '编辑头像',
    icon: 'none'
  });
};

// 打开昵称弹窗
const openNicknameModal = () => {
  newNickname.value = nickname.value;
  nextTick(() => {
    nicknamePopup.value.open();
  });
};

// 关闭昵称弹窗
const closeNicknameModal = () => {
  nicknamePopup.value.close();
};

// 更新昵称
const confirmNickname = async () => {
  if (!newNickname.value.trim()) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    });
    return;
  }

  try {
    // 显示加载状态
    uni.showLoading({
      title: '更新中...',
      mask: true
    });

    const userId = getUserId();
    if (!userId) {
      throw new Error('用户ID获取失败');
    }

    // 调用修改用户名接口
    const response = await updateUsername(userId, newNickname.value.trim());
    
    if (response.code === 0) {
      // 更新本地显示的昵称
      nickname.value = newNickname.value.trim();
      
      // 更新本地存储的用户数据
      const userData = getUserData();
      if (userData) {
        userData.username = newNickname.value.trim();
        setUserData(userData);
      }
      
      closeNicknameModal();
      uni.hideLoading();
      uni.showToast({
        title: '昵称更新成功',
        icon: 'success'
      });
    } else {
      throw new Error(response.message || '更新失败');
    }
  } catch (error) {
    uni.hideLoading();
    console.error('更新昵称失败:', error);
    uni.showToast({
      title: error.message || '更新失败，请稍后重试',
      icon: 'none'
    });
  }
};


// 清除缓存
const clearCache = () => {
  uni.showModal({
    title: '清除缓存',
    content: '确定要清除应用缓存吗？这将删除临时文件和数据。',
    success: function (res) {
      if (res.confirm) {
        // 模拟清除缓存
        uni.showLoading({
          title: '清除中...',
          mask: true
        });
        
        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({
            title: '缓存清除成功',
            icon: 'success'
          });
        }, 1500);
      }
    }
  });
};

// 检查版本更新
const checkVersion = () => {
  uni.showLoading({
    title: '检查中...',
    mask: true
  });
  
  setTimeout(() => {
    uni.hideLoading();
    uni.showModal({
      title: '版本检查',
      content: '当前已是最新版本 v1.0.0',
      showCancel: false,
      confirmText: '确定'
    });
  }, 1000);
};

// 显示关于我们
const showAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: 'Powered by Xanfro',
    showCancel: false,
    confirmText: '确定'
  });
};

// 开始练习
const startPractice = (settings) => {
  showPracticeSetup.value = false;
  // 跳转到练习页面并传递设置参数
  uni.navigateTo({
    url: `/pages/practice/practice?grade=${settings.grade}&questionCount=${settings.questionCount}&timeLimit=${settings.timeLimit}`
  });
};

// 退出登录
const logout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    success: function (res) {
      if (res.confirm) {
        // 清除用户数据和token
        clearToken();
        clearUserData();
        
        // 跳转到登录页
        uni.reLaunch({
          url: '/pages/login/login'
        });
      }
    }
  });
};
</script>

<style lang="scss">
.profile-container {
  min-height: 90vh;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  padding: 20px 0 55px;
  position: relative;
  overflow: hidden;
}

// 背景元素
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


// 导航栏样式
.navbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: left;
  width: 100%;
  padding: 0 15px; // 减少左右padding
  background-color: transparent;
  margin-bottom: 15px;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

.navbar-title {
  font-size: 24px; // 稍微减小标题字体
  font-weight: bold;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #4299e1, #3182ce);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 2px 8px rgba(66, 153, 225, 0.2);
  position: relative;
  padding: 6px 0;
}

.navbar-title::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, rgba(66, 153, 225, 0.2), rgba(66, 153, 225, 0.4), rgba(66, 153, 225, 0.2));
  border-radius: 2px;
}

// 原生picker样式
.grade-picker {
  max-width: 100px; // 进一步限制最大宽度
}

.picker-view {
  display: flex;
  align-items: center;
  padding: 4px 10px; // 减小padding
  background-color: #E6F3FF; /* 浅蓝色背景 */
  border: 1px solid #1E90FF; /* 深蓝色边框 */
  border-radius: 5px; // 减小圆角
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.grade-text {
  font-size: 14px; // 减小字体大小
  color: #1E90FF; /* 深蓝色文字 */
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

// 弹窗样式
.popup-content {
  width: 300px;
  background-color: white;
  border-radius: 10px;
  padding: 20px;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.popup-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.close-icon {
  cursor: pointer;
}

.input-container {
  margin-bottom: 20px;
}

.nickname-input {
  width: 100%;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 5px;
  padding: 0 10px;
  box-sizing: border-box;
}

.button-group {
  display: flex;
  justify-content: space-between;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  height: 40px;
  border-radius: 5px;
  border: none;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
  margin-right: 10px;
}

.confirm-btn {
  background-color: #4299e1;
  color: white;
  margin-left: 10px;
}

// 个人信息区域
.profile-header {
  background-color: white;
  margin: 0 15px 15px;
  border-radius: 16px;
  padding: 30px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-container {
  position: relative;
  width: 70px;
  height: 70px;
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #f0f0f0;
}

.camera-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background-color: #4299e1;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.username {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-id {
  font-size: 14px;
  color: #999;
}

// 区块样式
.section {
  background-color: white;
  margin: 0 15px 15px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.section-header {
  padding: 15px 20px 10px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.section-content {
  padding: 0 20px 15px;
}


// 设置列表
.settings-list {
  padding: 0 20px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 0;
  cursor: pointer;

  &:not(:last-child) {
    border-bottom: 1px solid #f0f0f0;
  }
}

.setting-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.setting-text {
  font-size: 16px;
  color: #333;
}

// 退出登录
.logout-section {
  padding: 0 15px;
  margin-bottom: 70px; // 为底部导航栏留出空间
}

.logout-btn {
  width: 100%;
  height: 48px;
  background-color: #ea4335;
  color: white;
  font-size: 16px;
  border-radius: 12px;
  border: none;
  margin-top: 10px;
}

// 动画效果
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}
</style>
