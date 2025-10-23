<template>
  <view class="result-container">
    <!-- 背景数学元素 -->
    <view class="bg-elements">
      <view class="math-symbol" :style="{top: '5%', left: '5%', animationDelay: '0s'}">√</view>
      <view class="math-symbol" :style="{top: '70%', left: '10%', animationDelay: '0.5s'}">∞</view>
      <view class="math-symbol" :style="{top: '30%', left: '20%', animationDelay: '1s'}">∑</view>
      <view class="math-symbol" :style="{top: '50%', left: '80%', animationDelay: '1.5s'}">∫</view>
      <view class="math-symbol" :style="{top: '15%', left: '70%', animationDelay: '2s'}">∏</view>
    </view>

    <!-- 页面标题 -->
    <view class="navbar">
      <text class="navbar-title">练习结果</text>
    </view>

    <!-- 练习结果概览 -->
    <view class="result-overview">
      <view class="overview-card">
        <view class="overview-header">
          <text class="overview-title">本次练习</text>
          <text class="overview-time">{{ practiceTime }}</text>
        </view>

        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{ totalQuestions }}</text>
            <text class="stat-label">题目数量</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ correctCount }}</text>
            <text class="stat-label">答对题数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ wrongCount }}</text>
            <text class="stat-label">答错题数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value" :class="accuracyClass">{{ accuracyRate }}%</text>
            <text class="stat-label">正确率</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 详细统计 -->
    <view class="detail-section">
      <view class="section-card">
        <view class="section-header">
          <text class="section-title">详细统计</text>
        </view>

        <view class="detail-grid">
          <view class="detail-item">
            <text class="detail-label">年级</text>
            <text class="detail-value">{{ gradeText }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">开始时间</text>
            <text class="detail-value">{{ startTime }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">用时</text>
            <text class="detail-value">{{ timeSpent }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">限时</text>
            <text class="detail-value">{{ timeLimit }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 移除难度分布部分 -->

    <!-- 操作按钮 -->
    <view class="action-section">
      <view class="button-row">
        <button class="action-btn primary" @click="goToHome">
          <text class="btn-text">返回首页</text>
        </button>
        <button class="action-btn secondary" @click="reviewMistakes" :disabled="wrongCount === 0">
          <text class="btn-text">查看错题</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { savePracticeRecord } from '@/api/index.js';
import ButtomNav from '/components/buttom-nav.vue';

// 练习数据
const practiceData = ref({
  totalQuestions: 10,
  correctCount: 8,
  startTime: '2025-10-04 15:30',
  timeSpent: '15:22',
  timeLimit: '30分钟',
  grade: 3
});

// 页面加载时获取参数并保存练习记录
onLoad(async (options) => {
  if (options && options.data) {
    try {
      const data = JSON.parse(decodeURIComponent(options.data));
      practiceData.value = {
        ...practiceData.value,
        ...data
      };
      
      // 自动保存练习记录到数据库
      await savePracticeRecordToDatabase();
      
    } catch (e) {
      console.error('解析练习数据失败:', e);
    }
  }
});

// 保存练习记录到数据库
const savePracticeRecordToDatabase = async () => {
  try {
    // 获取当前用户信息
    const userData = uni.getStorageSync('userData');
    if (!userData) {
      console.warn('用户未登录，无法保存练习记录');
      return;
    }
    
    const user = typeof userData === 'string' ? JSON.parse(userData) : userData;
    
    // 计算练习数据
    const wrongCount = practiceData.value.totalQuestions - practiceData.value.correctCount;
    const correctRate = (practiceData.value.correctCount / practiceData.value.totalQuestions * 100).toFixed(2);
    
    // 转换时间格式 - 优先使用秒数格式，否则转换字符串格式
    const timeSpentInSeconds = practiceData.value.timeSpentSeconds || convertTimeToSeconds(practiceData.value.timeSpent);
    const timeLimitInSeconds = practiceData.value.timeLimitSeconds || convertTimeToSeconds(practiceData.value.timeLimit);
    const startTime = practiceData.value.startTime && practiceData.value.startTime.includes('T') 
      ? practiceData.value.startTime 
      : convertToISOString(practiceData.value.startTime);
    
    // 构建保存数据
    const practiceRecordData = {
      userId: user.id,
      grade: String(practiceData.value.grade),
      questionCount: practiceData.value.totalQuestions,
      correctCount: practiceData.value.correctCount,
      wrongCount: wrongCount,
      correctRate: parseFloat(correctRate),
      timeLimit: timeLimitInSeconds,
      timeSpent: timeSpentInSeconds,
      startTime: startTime
    };
    
    console.log('准备保存练习记录:', practiceRecordData);
    
    // 调用API保存
    const response = await savePracticeRecord(practiceRecordData);
    console.log('练习记录保存成功:', response);
    
    // 可选：显示保存成功提示
    // uni.showToast({
    //   title: '练习记录已保存',
    //   icon: 'success',
    //   duration: 1500
    // });
    
  } catch (error) {
    console.error('保存练习记录失败:', error);
    // 可选：显示错误提示
    // uni.showToast({
    //   title: '保存失败: ' + error.message,
    //   icon: 'none',
    //   duration: 2000
    // });
  }
};

// 将时间字符串转换为秒数
const convertTimeToSeconds = (timeStr) => {
  if (!timeStr) return 0;
  
  // 处理 "15:22" 格式
  if (timeStr.includes(':')) {
    const parts = timeStr.split(':');
    const minutes = parseInt(parts[0]) || 0;
    const seconds = parseInt(parts[1]) || 0;
    return minutes * 60 + seconds;
  }
  
  // 处理 "30分钟" 格式
  if (timeStr.includes('分钟')) {
    const minutes = parseInt(timeStr.replace('分钟', '')) || 0;
    return minutes * 60;
  }
  
  // 处理纯数字
  const num = parseInt(timeStr);
  return isNaN(num) ? 0 : num;
};

// 将时间字符串转换为ISO格式
const convertToISOString = (timeStr) => {
  if (!timeStr) return new Date().toISOString();
  
  try {
    // 尝试解析不同格式的时间字符串
    let date;
    
    // 处理 "2025-10-04 15:30" 格式
    if (timeStr.includes('-') && timeStr.includes(' ')) {
      date = new Date(timeStr);
    }
    // 处理 "10月4日 15:30" 格式
    else if (timeStr.includes('月') && timeStr.includes('日')) {
      const year = new Date().getFullYear();
      const regex = /(\d+)月(\d+)日\s+(\d+):(\d+)/;
      const match = timeStr.match(regex);
      if (match) {
        const [, month, day, hour, minute] = match;
        date = new Date(year, parseInt(month) - 1, parseInt(day), parseInt(hour), parseInt(minute));
      }
    }
    
    // 如果解析失败，使用当前时间
    if (!date || isNaN(date.getTime())) {
      date = new Date();
    }
    
    return date.toISOString();
  } catch (error) {
    console.error('时间转换失败:', error);
    return new Date().toISOString();
  }
};

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 计算属性
const totalQuestions = computed(() => practiceData.value.totalQuestions);
const correctCount = computed(() => practiceData.value.correctCount);
const wrongCount = computed(() => totalQuestions.value - correctCount.value);
const accuracyRate = computed(() => Math.round((correctCount.value / totalQuestions.value) * 100));
const accuracyClass = computed(() => {
  const rate = accuracyRate.value;
  if (rate >= 90) return 'excellent';
  if (rate >= 80) return 'good';
  if (rate >= 70) return 'average';
  if (rate >= 60) return 'poor';
  return 'bad';
});

const gradeText = computed(() => {
  const grades = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级'];
  return grades[practiceData.value.grade - 1] || '未知年级';
});

const practiceTime = computed(() => {
  const date = new Date(practiceData.value.startTime);
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
});

const startTime = computed(() => practiceData.value.startTime);
const timeSpent = computed(() => practiceData.value.timeSpent);
const timeLimit = computed(() => practiceData.value.timeLimit);

// 移除难度相关的计算属性

// 方法
const goToHome = () => {
  uni.redirectTo({ url: '/pages/home/home' });
};

const reviewMistakes = () => {
  // 如果没有错题，禁用按钮
  if (wrongCount.value === 0) return;

  // 跳转到错题复习页面
  uni.navigateTo({ url: '/pages/reviewing/reviewing' });
};
</script>

<style lang="scss">
.result-container {
  min-height: 100vh;
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

.navbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  padding: 0 15px;
  background-color: transparent;
  margin-bottom: 15px;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

.navbar-title {
  font-size: 24px;
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

.result-overview {
  padding: 0 15px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.overview-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.overview-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.overview-time {
  font-size: 14px;
  color: #666;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  background-color: #f8fafc;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #4285F4;
  margin-bottom: 5px;
}

.stat-value.excellent {
  color: #2e7d32;
}

.stat-value.good {
  color: #f57f17;
}

.stat-value.average {
  color: #ef6c00;
}

.stat-value.poor {
  color: #c62828;
}

.stat-value.bad {
  color: #b71c1c;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.detail-section {
  padding: 0 15px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.section-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 15px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.detail-value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

// 移除难度相关样式

.action-section {
  padding: 0 15px;
  position: relative;
  z-index: 1;
}

.button-row {
  display: flex;
  gap: 15px;
}

.action-btn {
  flex: 1;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.action-btn.primary {
  background: linear-gradient(90deg, #4299e1, #3182ce);
  color: white;
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.3);
}

.action-btn.secondary {
  background-color: white;
  color: #4299e1;
  border: 1px solid #4299e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}
</style>
