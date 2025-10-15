<template>
  <view class="reviewing-container">
    <!-- 背景数学元素 -->
    <view class="bg-elements">
      <view class="math-symbol" :style="{top: '5%', left: '5%', animationDelay: '0s'}">✗</view>
      <view class="math-symbol" :style="{top: '70%', left: '10%', animationDelay: '0.5s'}">√</view>
      <view class="math-symbol" :style="{top: '30%', left: '20%', animationDelay: '1s'}">∑</view>
      <view class="math-symbol" :style="{top: '50%', left: '80%', animationDelay: '1.5s'}">∫</view>
      <view class="math-symbol" :style="{top: '15%', left: '70%', animationDelay: '2s'}">∏</view>
    </view>

    <!-- 页面标题 -->
    <view class="navbar">
      <text class="navbar-title">错题复习</text>
    </view>

    <!-- 错题列表 -->
    <view class="mistakes-section">
      <view class="loading-placeholder" v-if="loading">
        <text>加载中...</text>
      </view>
      <view class="section-card" v-for="(mistake, index) in mistakes" :key="index" v-else>
        <view class="mistake-item" :class="{ 'corrected': mistake.corrected }">
          <view class="mistake-content" @click="mistake.corrected ? confirmDeleteMistake(index) : openMistakePopup(mistake, index)">
            <text class="mistake-question">{{ mistake.question }} = ?</text>
            <view class="mistake-meta">
              <text class="mistake-time">{{ mistake.time }}</text>
            </view>
          </view>
          <view class="mistake-actions">
            <uni-icons 
              :type="mistake.corrected ? 'checkmarkempty' : 'closeempty'" 
              :color="mistake.corrected ? '#48bb78' : '#f56565'" 
              size="24"
            ></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 错题练习弹窗 -->
    <uni-popup ref="mistakePopup" type="center">
      <view class="popup-container" v-if="currentMistake">
        <view class="popup-header">
          <text class="popup-title">错题练习</text>
          <uni-icons 
            type="closeempty" 
            size="24" 
            color="#999" 
            @click="closeMistakePopup" 
            class="close-icon"
          ></uni-icons>
        </view>

        <view class="popup-content">
          <view class="question-display">
            <text class="question-text">{{ currentMistake.question }} = ?</text>
          </view>

          <view class="answer-input-container">
            <input 
              class="answer-input" 
              type="number" 
              v-model="userAnswer" 
              placeholder="请输入答案" 
              @confirm="submitAnswer"
            />
          </view>

          <view class="popup-actions">
            <button class="action-btn submit-btn" @click="submitAnswer">提交答案</button>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 空状态 -->
    <view class="empty-state" v-if="!loading && mistakes.length === 0">
      <text>暂无错题记录</text>
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
import { ref, onMounted } from 'vue';
import uniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue';
import uniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import ButtomNav from '/components/buttom-nav.vue';
import PracticeSetup from '/components/practice-setup.vue';
import { getMistakeRecords, updateMistakeCorrectedStatus, deleteMistakeRecord } from '@/api/index.js';
import { getUserId } from '@/api/utils.js';

// 错题数据
const mistakes = ref([]);
const loading = ref(false);

// 练习设置弹窗状态
const showPracticeSetup = ref(false);

// 用户ID
const userId = ref(null);

// 当前选中的错题
const currentMistake = ref(null);
const currentMistakeIndex = ref(-1);

// 用户答案
const userAnswer = ref('');

// 弹窗引用
const mistakePopup = ref(null);


// 打开错题弹窗
const openMistakePopup = (mistake, index) => {
  // 如果已经答对，不打开弹窗
  if (mistake.corrected) return;
  
  currentMistake.value = mistake;
  currentMistakeIndex.value = index;
  userAnswer.value = '';
  mistakePopup.value.open();
};

// 关闭错题弹窗
const closeMistakePopup = () => {
  mistakePopup.value.close();
  currentMistake.value = null;
  currentMistakeIndex.value = -1;
};

// 提交答案
const submitAnswer = async () => {
  if (!userAnswer.value) {
    uni.showToast({
      title: '请输入答案',
      icon: 'none'
    });
    return;
  }

  const answer = parseInt(userAnswer.value);
  if (isNaN(answer)) {
    uni.showToast({
      title: '请输入有效数字',
      icon: 'none'
    });
    return;
  }

  // 判断答案是否正确
  if (answer === currentMistake.value.correctAnswer) {
    // 答对了，更新错题状态 - 调用真实API
    try {
      const mistakeId = currentMistake.value.id;
      console.log('更新错题纠正状态，错题ID:', mistakeId);
      
      // 调用API更新纠正状态
      await updateMistakeCorrectedStatus(mistakeId, '1');
      
      // 更新本地状态
      mistakes.value[currentMistakeIndex.value].corrected = true;
      
      uni.showToast({
        title: '回答正确！',
        icon: 'success'
      });
      
      console.log('错题状态更新成功');
      
      // 关闭弹窗
      setTimeout(() => {
        closeMistakePopup();
      }, 1000);
    } catch (error) {
      console.error('更新错题状态失败:', error);
      uni.showToast({
        title: '更新失败',
        icon: 'none'
      });
    }
  } else {
    // 答错了
    uni.showToast({
      title: '回答错误，请再试一次',
      icon: 'none'
    });
    
    // 清空输入框
    userAnswer.value = '';
  }
};

// 确认删除已做对的错题
const confirmDeleteMistake = (index) => {
  uni.showModal({
    title: '移除题目',
    content: '确定要移除这道已掌握的题目吗？',
    success: async function (res) {
      if (res.confirm) {
        try {
          const mistakeId = mistakes.value[index].id;
          console.log('删除错题记录，错题ID:', mistakeId);
          
          // 调用API删除错题记录
          await deleteMistakeRecord(mistakeId);
          
          // 从本地列表中移除
          mistakes.value.splice(index, 1);
          
          uni.showToast({
            title: '已移除',
            icon: 'success'
          });
          
          console.log('错题记录删除成功');
        } catch (error) {
          console.error('删除错题记录失败:', error);
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 加载错题数据 - 使用真实API
const loadMistakes = async () => {
  try {
    if (!userId.value) {
      console.warn('用户ID未找到，无法加载错题记录');
      return;
    }

    console.log('开始加载用户错题记录，用户ID:', userId.value);
    const response = await getMistakeRecords(userId.value);
    
    if (response && response.data) {
      // 处理API返回的数据，转换为页面需要的格式
      mistakes.value = response.data.map(record => ({
        id: record.id,
        question: record.question.replace(' = ?', ''), // 移除 = ? 后缀，因为模板会自动添加
        time: formatMistakeTime(record.createdTime),
        correctAnswer: parseFloat(record.correctAnswer),
        corrected: record.corrected === '1' // 转换为布尔值
      }));
      
      console.log('错题记录加载成功:', mistakes.value);
    } else {
      console.log('没有获取到错题数据');
      mistakes.value = [];
    }
  } catch (error) {
    console.error('加载错题数据失败:', error);
    uni.showToast({
      title: '数据加载失败',
      icon: 'none'
    });
    mistakes.value = [];
  }
};

// 格式化错题时间
const formatMistakeTime = (timeString) => {
  try {
    const date = new Date(timeString);
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch (error) {
    console.error('时间格式化失败:', error);
    return timeString;
  }
};

// 计算答案
const calculateAnswer = (num1, operator, num2) => {
  const n1 = parseFloat(num1);
  const n2 = parseFloat(num2);
  switch (operator) {
    case '+': return n1 + n2;
    case '-': return n1 - n2;
    case '*': return n1 * n2;
    case '/': return n2 !== 0 ? n1 / n2 : 0;
    default: return 0;
  }
};

// 页面加载时获取数据
onMounted(async () => {
  loading.value = true;
  try {
    // 获取用户ID
    userId.value = getUserId();
    console.log('获取到用户ID:', userId.value);
    
    if (userId.value) {
      await loadMistakes();
    } else {
      console.warn('用户ID未找到，无法加载错题记录');
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('页面初始化失败:', error);
    uni.showToast({
      title: '页面加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
});

// 开始练习
const startPractice = (settings) => {
  // 跳转到练习页面，传递练习设置参数
  const query = new URLSearchParams({
    grade: settings.grade,
    questionCount: settings.questionCount,
    timeLimit: settings.timeLimit,
    practiceType: settings.practiceType
  }).toString();
  
  uni.navigateTo({
    url: `/pages/practice/practice?${query}`
  });
};
</script>

<style lang="scss">
.reviewing-container {
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

.mistakes-section {
  padding: 0 15px;
  position: relative;
  z-index: 1;
}

.section-card {
  background-color: white;
  border-radius: 12px;
  margin-bottom: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.section-card:hover {
  border-color: #cbd5e0;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}


.mistake-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.mistake-item:hover {
  background-color: #f8fafc;
}



.mistake-item.corrected .mistake-question {
  text-decoration: line-through;
  color: #718096;
}

.mistake-content {
  flex: 1;
  cursor: pointer;
}

.mistake-question {
  font-size: 18px;
  color: #2d3748;
  font-weight: 500;
  margin-bottom: 8px;
  display: block;
}

.mistake-meta {
  display: flex;
  justify-content: flex-start;
}

.mistake-time {
  font-size: 12px;
  color: #718096;
  background-color: rgba(113, 128, 150, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}



.mistake-actions {
  display: flex;
  align-items: center;
  margin-left: 15px;
}


.footer-tip {
  padding: 0 15px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.tip-text {
  font-size: 14px;
  color: #718096;
  background-color: rgba(255, 255, 255, 0.7);
  padding: 10px 15px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

// 弹窗样式
.popup-container {
  width: 300px;
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.popup-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.close-icon {
  padding: 5px;
  cursor: pointer;
}

.popup-content {
  padding: 20px;
}

.question-display {
  text-align: center;
  margin-bottom: 25px;
  padding: 15px;
  background-color: #f8fafc;
  border-radius: 12px;
}

.question-text {
  font-size: 24px;
  color: #2d3748;
  font-weight: 600;
}

.answer-input-container {
  margin-bottom: 25px;
}

.answer-input {
  width: 100%;
  height: 50px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0 15px;
  font-size: 18px;
  text-align: center;
  box-sizing: border-box;
}

.answer-input:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.popup-actions {
  display: flex;
  justify-content: center;
}

.action-btn {
  height: 45px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.submit-btn {
  background: linear-gradient(90deg, #4299e1, #3182ce);
  color: white;
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.3);
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}

/* 加载状态和空状态样式 */
.loading-placeholder, .empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}
</style>