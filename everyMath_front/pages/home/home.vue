<template>
  <view class="home-container">
    <!-- 背景数学元素 -->
    <view class="bg-elements top">
      <view class="math-symbol" :style="{top: '5%', left: '5%', animationDelay: '0s'}">+</view>
      <view class="math-symbol" :style="{top: '70%', left: '10%', animationDelay: '0.5s'}">-</view>
      <view class="math-symbol" :style="{top: '30%', left: '20%', animationDelay: '1s'}">×</view>
      <view class="math-symbol" :style="{top: '50%', left: '80%', animationDelay: '1.5s'}">÷</view>
      <view class="math-symbol" :style="{top: '15%', left: '70%', animationDelay: '2s'}">+</view>
    </view>

    <!-- 导航栏 -->
    <view class="navbar">
      <text class="navbar-title">首页</text>
      <!-- 年级选择器 - 使用原生picker组件 -->
      <picker
        class="grade-picker"
        mode="selector"
        :range="gradesOptions"
        :value="gradeIndex"
        @change="onGradeChange"
      >
        <view class="picker-view">
          <text class="grade-text">{{ selectedGrade || '年级' }}</text>
          <uni-icons type="arrowdown" size="14" color="#1E90FF"></uni-icons>
        </view>
      </picker>
    </view>

    <!-- 欢迎区域 -->
    <view class="welcome-section">
      <!-- 狐狸图片 - 绝对定位 -->
      <image 
        class="fox-welcome" 
        src="/static/fox.png" 
        mode="widthFix"
      ></image>
      
      <view class="welcome-header">
        <text class="welcome-text">{{ realName || '小朋友' }}，一起来算数学吧！</text>
      </view>
      <text class="practice-stats" v-if="!loading">
        今天已经做了 {{ todayPracticeCount }} 道题，答对了 {{ correctRate }}%！棒棒哒！
      </text>
      <text class="practice-stats" v-else>
        正在加载...
      </text>
    </view>

    <!-- 功能入口区 -->
    <view class="function-grid">
      <!-- 限时练习 -->
      <view class="function-card" @click="startTimedPractice">
        <uni-icons type="medal" size="36" color="#ff6b35"></uni-icons>
        <view class="card-text">快速挑战</view>
      </view>

            <!-- 导入作业功能 -->
      <view class="function-card" @click="importHomework">
        <uni-icons type="upload" size="36" color="#4299e1"></uni-icons>
        <view class="card-text">习题导入</view>
      </view>

      <!-- 错题复习 -->
      <view class="function-card" @click="reviewMistakes">
        <uni-icons type="closeempty" size="36" color="#ea4335"></uni-icons>
        <view class="card-text">错题复习</view>
      </view>

      <!-- 学习分析 -->
      <view class="function-card" @click="viewAnalysis">
        <uni-icons type="map" size="36" color="#34a853"></uni-icons>
        <view class="card-text">学习分析</view>
      </view>
    </view>




    <!-- 每日统计板块 -->
    <view class="daily-stats-section">
      <view class="section-header">
        <text class="section-title">今日统计</text>
      </view>
      
      <view class="stats-grid" v-if="!loading">
        <view class="stat-item">
          <view class="stat-icon">
            <uni-icons type="medal" size="24" color="#ff6b35"></uni-icons>
          </view>
          <view class="stat-content">
            <text class="stat-value">{{ dailyStats.practiceCount }}</text>
            <text class="stat-label">练习次数</text>
          </view>
        </view>
        
        <view class="stat-item">
          <view class="stat-icon">
            <uni-icons type="list" size="24" color="#4299e1"></uni-icons>
          </view>
          <view class="stat-content">
            <text class="stat-value">{{ dailyStats.questionCount }}</text>
            <text class="stat-label">题目数量</text>
          </view>
        </view>
        
        <view class="stat-item">
          <view class="stat-icon">
            <uni-icons type="checkmarkempty" size="24" color="#34a853"></uni-icons>
          </view>
          <view class="stat-content">
            <text class="stat-value">{{ dailyStats.correctRate }}%</text>
            <text class="stat-label">正确率</text>
          </view>
        </view>
        
        <view class="stat-item">
          <view class="stat-icon">
            <uni-icons type="calendar" size="24" color="#9c27b0"></uni-icons>
          </view>
          <view class="stat-content">
            <text class="stat-value">{{ dailyStats.totalTimeSpent }}</text>
            <text class="stat-label">总用时(秒)</text>
          </view>
        </view>
      </view>
      <view class="loading-placeholder" v-else>
        <text>加载中...</text>
      </view>
    </view>

    <!-- 练习记录区域 -->
    <view class="records-section">
      <view class="section-header">
        <text class="section-title">练习记录</text>
      </view>

      <view class="records-list" v-if="!loading">
        <view class="record-item" v-for="(record, index) in recentRecords" :key="index">
          <view class="record-main">
            <view class="record-info">
              <view class="record-meta">
                <text class="start-time">{{ record.startTime }}</text>
                <view class="record-details">
                  <text class="detail-text">{{ record.grade }}</text>
                  <text class="detail-text">{{ record.questionCount }}题</text>
                </view>
              </view>
              <view class="record-stats">
                <text class="stats-text">限时{{ record.timeLimit }}秒</text>
                <text class="stats-text">用时{{ record.timeSpent }}秒</text>
              </view>
            </view>
            <view class="accuracy-rate" :class="getAccuracyClass(record.correctRate)">
              <text class="rate-text">{{ record.correctRate }}%</text>
              <text class="rate-label">正确率</text>
            </view>
          </view>
        </view>
        <view class="empty-state" v-if="recentRecords.length === 0">
          <text>暂无练习记录</text>
        </view>
      </view>
      <view class="loading-placeholder" v-else>
        <text>加载中...</text>
      </view>
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
import uniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import { ref, onMounted } from 'vue';
import { updateUserGrade, getPracticeRecords } from '@/api/index.js';
import { getUserData, getUserId, formatDate, setUserData } from '@/api/utils.js';

// 状态管理
const showPracticeSetup = ref(false);
const realName = ref('张三');
const gradeIndex = ref(-1); // 修复变量名错误
const loading = ref(false);

// 年级选择相关
const gradesOptions = ref(['1年级', '2年级', '3年级', '4年级', '5年级', '6年级']); // 年级选项
const selectedGrade = ref(''); // 选中的年级文本

// 数据统计
const todayPracticeCount = ref(0);
const correctRate = ref(0);

// 每日统计数据
const dailyStats = ref({
  practiceCount: 0,      // 今日练习次数
  questionCount: 0,      // 今日题目数量
  correctCount: 0,       // 今日正确数量
  wrongCount: 0,         // 今日错误数量
  correctRate: 0,        // 今日正确率
  totalTimeSpent: 0      // 今日总用时(分钟)
});

// 最近练习记录
const recentRecords = ref([]);

// 根据正确率获取颜色类名
const getAccuracyClass = (rate) => {
  if (rate >= 90) return 'excellent';
  if (rate >= 80) return 'good';
  if (rate >= 70) return 'average';
  if (rate >= 60) return 'poor';
  return 'bad';
};

// 功能跳转
const startTimedPractice = () => {
  // 快速挑战：年级为当前用户年级，题目数量为5，时间为1分30秒(90秒)
  console.log('快速挑战 - gradeIndex.value:', gradeIndex.value);
  const userGrade = gradeIndex.value >= 0 ? gradeIndex.value + 1 : 1; // 默认为1年级
  console.log('快速挑战 - 使用年级:', userGrade);
  uni.navigateTo({ 
    url: `/pages/practice/practice?grade=${userGrade}&questionCount=5&timeLimit=90` 
  });
};
// 导入作业 - 跳转到导入页面
const importHomework = () => {
  uni.navigateTo({ url: '/pages/import/import' });
};

const reviewMistakes = () => uni.navigateTo({ url: '/pages/reviewing/reviewing' });
const viewRecords = () => uni.navigateTo({ url: '/pages/records/history' });
const viewAnalysis = () => uni.navigateTo({ url: '/pages/analysis/analysis' });

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 从本地存储获取用户数据
    const userData = getUserData();
    
    if (!userData) {
      // 如果没有用户数据，跳转到登录页
      uni.reLaunch({ url: '/pages/login/login' });
      return;
    }
    
    // 设置用户信息
    realName.value = userData.username || '同学';
    console.log('首页加载用户数据:', userData);
    if (userData.grade) {
      gradeIndex.value = parseInt(userData.grade) - 1;
      selectedGrade.value = gradesOptions.value[gradeIndex.value] || '';
      console.log('首页设置年级:', userData.grade, '-> gradeIndex:', gradeIndex.value);
    } else {
      console.log('首页用户数据中没有年级信息');
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
    uni.reLaunch({ url: '/pages/login/login' });
  }
};


// 重置每日统计为默认值
const resetDailyStats = () => {
  dailyStats.value = {
    practiceCount: 0,
    questionCount: 0,
    correctCount: 0,
    wrongCount: 0,
    correctRate: 0,
    totalTimeSpent: 0
  };
  todayPracticeCount.value = 0;
  correctRate.value = 0;
};

// 计算今日统计数据
const calculateTodayStats = (records) => {
  const today = new Date();
  const todayStr = today.toISOString().split('T')[0]; // 获取今日日期字符串 YYYY-MM-DD
  
  // 筛选今日的练习记录
  const todayRecords = records.filter(record => {
    const recordDate = new Date(record.createdTime);
    const recordDateStr = recordDate.toISOString().split('T')[0];
    return recordDateStr === todayStr;
  });
  
  console.log(`今日练习记录数量: ${todayRecords.length}`, todayRecords);
  
  if (todayRecords.length === 0) {
    return {
      practiceCount: 0,
      questionCount: 0,
      correctCount: 0,
      wrongCount: 0,
      correctRate: 0,
      totalTimeSpent: 0
    };
  }
  
  // 计算统计数据
  let totalQuestions = 0;
  let totalCorrect = 0;
  let totalWrong = 0;
  let totalTimeSpent = 0; // 秒
  
  todayRecords.forEach(record => {
    const questionCount = parseInt(record.questionCount) || 0;
    const correctCount = parseInt(record.correctCount) || 0;
    const wrongCount = parseInt(record.wrongCount) || 0;
    const timeSpent = parseInt(record.timeSpent) || 0; // 秒
    
    totalQuestions += questionCount;
    totalCorrect += correctCount;
    totalWrong += wrongCount;
    totalTimeSpent += timeSpent;
  });
  
  const correctRate = totalQuestions > 0 ? (totalCorrect / totalQuestions * 100) : 0;
  
  return {
    practiceCount: todayRecords.length,
    questionCount: totalQuestions,
    correctCount: totalCorrect,
    wrongCount: totalWrong,
    correctRate: Math.round(correctRate * 10) / 10, // 保留一位小数
    totalTimeSpent: totalTimeSpent // 保持秒数
  };
};


// 格式化开始时间显示
const formatStartTime = (timeStr) => {
  if (!timeStr) return '';
  
  try {
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) {
      return timeStr; // 如果解析失败，返回原字符串
    }
    
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error('时间格式化失败:', error);
    return timeStr;
  }
};

// 页面加载时获取数据
onMounted(async () => {
  loading.value = true;
  try {
    // 先加载用户信息
    await loadUserInfo();
    
    // 然后加载练习数据（优化：只调用一次API，同时计算统计和记录）
    await loadPracticeData();
  } catch (error) {
    console.error('页面数据加载失败:', error);
    uni.showToast({
      title: '数据加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
});

// 加载练习数据（统一加载，避免重复API调用）
const loadPracticeData = async () => {
  try {
    const userId = getUserId();
    if (!userId) {
      console.warn('用户未登录，无法获取练习数据');
      resetDailyStats();
      recentRecords.value = [];
      return;
    }
    
    console.log('正在获取用户练习数据，用户ID:', userId);
    
    // 只调用一次API获取所有练习记录
    const response = await getPracticeRecords(userId);
    console.log('获取到的练习记录:', response);
    
    if (response && response.data && Array.isArray(response.data)) {
      const allRecords = response.data;
      
      // 1. 计算今日统计
      const todayStats = calculateTodayStats(allRecords);
      dailyStats.value = todayStats;
      todayPracticeCount.value = dailyStats.value.questionCount;
      correctRate.value = Math.round(dailyStats.value.correctRate);
      
      // 2. 处理最近练习记录（显示所有记录）
      recentRecords.value = allRecords
        .map(record => ({
          startTime: formatStartTime(record.createdTime),
          grade: `${record.grade}年级`,
          questionCount: parseInt(record.questionCount) || 0,
          correctRate: Math.round(parseFloat(record.correctRate) || 0),
          timeLimit: parseInt(record.timeLimit) || 0,  // 保持秒数
          timeSpent: parseInt(record.timeSpent) || 0   // 保持秒数
        }));
      
      console.log('今日统计:', dailyStats.value);
      console.log('最近练习记录:', recentRecords.value);
    } else {
      console.log('没有获取到练习数据');
      resetDailyStats();
      recentRecords.value = [];
    }
  } catch (error) {
    console.error('加载练习数据失败:', error);
    resetDailyStats();
    recentRecords.value = [];
  }
};

// 开始练习
const startPractice = (settings) => {
  showPracticeSetup.value = false;
  // 跳转到练习页面并传递设置参数
  uni.navigateTo({
    url: `/pages/practice/practice?grade=${settings.grade}&questionCount=${settings.questionCount}&timeLimit=${settings.timeLimit}`
  });
};

// 年级选择变更处理函数
const onGradeChange = async (e) => {
  const newGradeIndex = e.detail.value;
  const newGrade = newGradeIndex + 1; // 转换为1-6的年级值
  
  try {
    // 显示加载提示
    uni.showLoading({
      title: '更新年级中...',
      mask: true
    });
    
    const userId = getUserId();
    if (!userId) {
      throw new Error('用户ID获取失败');
    }
    
    // 调用API更新年级
    const response = await updateUserGrade(userId, newGrade);
    
    if (response.code === 0) {
      // 更新本地状态
      gradeIndex.value = newGradeIndex;
      selectedGrade.value = gradesOptions.value[newGradeIndex];
      
      // 更新本地存储的用户数据
      const userData = getUserData();
      if (userData) {
        userData.grade = newGrade;
        setUserData(userData);
      }
      
      uni.hideLoading();
      uni.showToast({
        title: '年级更新成功',
        icon: 'success'
      });
    } else {
      throw new Error(response.message || '年级更新失败');
    }
  } catch (error) {
    uni.hideLoading();
    console.error('年级更新失败:', error);
    uni.showToast({
      title: error.message || '年级更新失败，请稍后重试',
      icon: 'none'
    });
    
    // 恢复原来的选择
    // 不更新gradeIndex和selectedGrade，保持原状态
  }
};
</script>

<style lang="scss" scoped>
.home-container {
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

// 导航栏样式
.navbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 20px;
  background-color: transparent;
  margin-bottom: 15px;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  gap: 10px; // 添加最小间距
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
  flex-shrink: 0; // 防止收缩
  min-width: 80px; // 设置最小宽度
  max-width: 120px; // 设置最大宽度
}

.picker-view {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  background-color: #E6F3FF; /* 浅蓝色背景 */
  border: 1px solid #1E90FF; /* 深蓝色边框 */
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 32px;
  box-sizing: border-box;
}

.grade-text {
  font-size: 16px;
  color: #318eff; /* 深蓝色文字 */
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
	text-align: center;
  margin-right: 6px;
  flex-shrink: 0;
}

// 欢迎区域
.welcome-section {
  padding: 25px 20px;
  background-color: white;
  margin: 0 15px 15px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;
  overflow: hidden;

  .welcome-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .welcome-text {
    font-size: 20px;
    color: #333;
    display: block;
  }

  .user-type-tag {
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
  }

  .practice-stats {
    font-size: 14px;
    color: #666;
  }
}

// 狐狸图片样式 - 绝对定位
.fox-welcome {
  position: absolute;
  bottom: 0px;
  right: 30px;
  width: 60px;
  opacity: 1;
  transition:all 0.3s ease;
  z-index: 2;
  pointer-events: none;
}

// 功能网格
.function-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 0 15px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;

  .function-card {
    background-color: white;
    border-radius: 12px;
    padding: 25px 10px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    gap: 10px;

    &:active {
      transform: scale(0.98);
    }

    .card-text {
      font-size: 16px;
      color: #333;
    }
  }
}




// 每日统计板块
.daily-stats-section {
  background-color: white;
  margin: 0 15px 15px;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2px;

    .section-title {
      font-size: 16px;
      color: #333;
      font-weight: 500;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;

    .stat-item {
      display: flex;
      align-items: center;
      padding: 15px;
      background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
      border-radius: 10px;
      border: 1px solid #e6f0ff;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        margin-right: 12px;
        padding: 8px;
        background-color: rgba(255, 255, 255, 0.8);
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .stat-content {
        flex: 1;
        display: flex;
        flex-direction: column;

        .stat-value {
          font-size: 20px;
          font-weight: bold;
          color: #333;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 12px;
          color: #666;
          margin-top: 2px;
        }
      }
    }
  }
}

// 练习记录区域
.records-section {
  background-color: white;
  margin: 0 15px 20px;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2px;

    .section-title {
      font-size: 16px;
      color: #333;
      font-weight: 500;
    }
  }

  .records-list {
    .record-item {
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-main {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .record-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 8px;

          .record-meta {
            display: flex;
            flex-direction: column;

            .start-time {
              font-size: 15px;
              color: #333;
              font-weight: 500;
              margin-bottom: 4px;
            }

            .record-details {
              display: flex;
              gap: 12px;

              .detail-text {
                font-size: 13px;
                color: #666;
                background-color: #f5f7fa;
                padding: 3px 8px;
                border-radius: 4px;
              }
            }
          }

          .record-stats {
            display: flex;
            gap: 12px;

            .stats-text {
              font-size: 12px;
              color: #999;
              background-color: #f5f5f5;
              padding: 2px 8px;
              border-radius: 3px;
            }
          }
        }

        .accuracy-rate {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          min-width: 70px;
          padding: 8px 12px;
          border-radius: 10px;
          font-weight: bold;

          &.excellent {
            background-color: #e8f5e9;
            color: #2e7d32;
          }

          &.good {
            background-color: #fff8e1;
            color: #f57f17;
          }

          &.average {
            background-color: #ffe0b2;
            color: #ef6c00;
          }

          &.poor {
            background-color: #ffebee;
            color: #c62828;
          }

          &.bad {
            background-color: #ffcdd2;
            color: #b71c1c;
          }

          .rate-text {
            font-size: 20px;
          }

          .rate-label {
            font-size: 12px;
            margin-top: 2px;
            opacity: 0.8;
          }
        }
      }
    }
  }
}

// 适配小屏手机
@media (max-width: 375px) {
  .navbar {
    padding: 0 15px;
    gap: 8px;
  }
  
  .navbar-title {
    font-size: 20px;
    flex-shrink: 1;
  }

  .grade-picker {
    max-width: 90px;
    min-width: 70px;
  }

  .picker-view {
    padding: 4px 8px;
    min-height: 28px;
  }

  .grade-text {
    font-size: 14px;
    margin-right: 4px;
  }
}

/* 加载状态样式 */
.loading-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}
</style>
