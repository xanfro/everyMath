<template>
  <view class="container">
    <!-- 左上角标题 -->
    <view class="navbar">
      <uni-icons type="back" size="24" color="#1E90FF" @click="navigateToHome" class="back-home-icon"></uni-icons>
      <text class="navbar-title">口算练习</text>
    </view>

    <!-- 练习组件 -->
    <practice-exercise
      v-if="questionExpressions.length > 0"
      :question-expressions="questionExpressions"
      :time-limit="practiceSettings.timeLimit"
      @complete="handlePracticeComplete"
      @exit="navigateToHome"
    />

    <!-- 加载状态 -->
    <view v-else class="loading-container">
      <text class="loading-text">正在生成题目...</text>
      </view>
  </view>

  <!-- 年级选择弹窗 -->
  <uni-popup ref="gradePopup" type="bottom">
    <view class="custom-popup">
      <view class="popup-header">
        <text class="popup-title">选择年级</text>
        <uni-icons type="closeempty" size="24" color="#999" @click="closeGradePopup" class="close-icon"></uni-icons>
      </view>
      <scroll-view class="options-container" scroll-y>
        <view
          v-for="(item, index) in gradeOptions"
          :key="item.value"
          class="option-item"
          :class="{ selected: index === practiceSetupRef?.selectedGradeIndex }"
          @click="selectGrade(index)"
        >
          {{ item.label }}
        </view>
      </scroll-view>
      <view class="popup-footer">
        <button class="confirm-btn" @click="closeGradePopup">确定</button>
      </view>
    </view>
  </uni-popup>

  <!-- 题目数量选择弹窗 -->
  <uni-popup ref="questionCountPopup" type="bottom">
    <view class="custom-popup">
      <view class="popup-header">
        <text class="popup-title">选择题目数量</text>
        <uni-icons type="closeempty" size="24" color="#999" @click="closeQuestionCountPopup" class="close-icon"></uni-icons>
      </view>
      <scroll-view class="options-container" scroll-y>
        <view
          v-for="(item, index) in questionCountOptions"
          :key="index"
          class="option-item"
          :class="{ selected: index === practiceSetupRef?.selectedQuestionCountIndex }"
          @click="selectQuestionCount(index)"
        >
          {{ item }} 题
        </view>
      </scroll-view>
      <view class="popup-footer">
        <button class="confirm-btn" @click="closeQuestionCountPopup">确定</button>
      </view>
    </view>
  </uni-popup>

  <!-- 分钟选择弹窗 -->
  <uni-popup ref="minutesPopup" type="bottom">
    <view class="custom-popup">
      <view class="popup-header">
        <text class="popup-title">选择分钟数</text>
        <uni-icons type="closeempty" size="24" color="#999" @click="closeMinutesPopup" class="close-icon"></uni-icons>
      </view>
      <scroll-view class="options-container" scroll-y>
        <view
          v-for="(item, index) in minutesOptions"
          :key="index"
          class="option-item"
          :class="{ selected: index === practiceSetupRef?.selectedMinutesIndex }"
          @click="selectMinutes(index)"
        >
          {{ item }} 分
        </view>
      </scroll-view>
      <view class="popup-footer">
        <button class="confirm-btn" @click="closeMinutesPopup">确定</button>
      </view>
    </view>
  </uni-popup>

  <!-- 秒数选择弹窗 -->
  <uni-popup ref="secondsPopup" type="bottom">
    <view class="custom-popup">
      <view class="popup-header">
        <text class="popup-title">选择秒数</text>
        <uni-icons type="closeempty" size="24" color="#999" @click="closeSecondsPopup" class="close-icon"></uni-icons>
      </view>
      <scroll-view class="options-container" scroll-y>
        <view
          v-for="(item, index) in secondsOptions"
          :key="index"
          class="option-item"
          :class="{ selected: index === practiceSetupRef?.selectedSecondsIndex }"
          @click="selectSeconds(index)"
        >
          {{ item }} 秒
        </view>
      </scroll-view>
      <view class="popup-footer">
        <button class="confirm-btn" @click="closeSecondsPopup">确定</button>
      </view>
    </view>
  </uni-popup>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import PracticeExercise from '@/components/practice-exercise.vue';
import { getUserData } from '@/api/utils.js';

// 获取用户年级作为默认值
const getUserGrade = () => {
  try {
    // 直接从存储中获取，避免工具函数可能的问题
    const userDataStr = uni.getStorageSync('userData');
    console.log('原始用户数据字符串:', userDataStr);
    
    if (userDataStr) {
      const userData = JSON.parse(userDataStr);
      console.log('解析后的用户数据:', userData);
      
      if (userData && userData.grade) {
        const grade = parseInt(userData.grade) || 1;
        console.log('解析的年级:', userData.grade, '-> ', grade);
        return grade;
      } else {
        console.log('用户数据中没有年级信息');
      }
    } else {
      console.log('本地存储中没有用户数据');
    }
  } catch (error) {
    console.error('获取用户年级失败:', error);
  }
  console.log('使用默认年级: 1');
  return 1; // 默认1年级
};

// 练习设置
const practiceSettings = ref({
  grade: 1, // 临时默认值，将在onLoad中重新设置
  questionCount: 10, // 默认题目数量
  timeLimit: 180 // 默认时间限制(秒)
});

console.log('初始化练习设置:', practiceSettings.value);

// 题目表达式数组
const questionExpressions = ref([]);

// 弹窗引用（保留用于设置弹窗）
const gradePopup = ref(null);
const questionCountPopup = ref(null);
const minutesPopup = ref(null);
const secondsPopup = ref(null);

// 年级选项 (1-6年级)
const gradeOptions = ref([
  { label: '一年级', value: 1 },
  { label: '二年级', value: 2 },
  { label: '三年级', value: 3 },
  { label: '四年级', value: 4 },
  { label: '五年级', value: 5 },
  { label: '六年级', value: 6 }
]);

// 题目数量选项
const questionCountOptions = ref([5, 10, 15, 20, 25, 30]);

// 时间选项
const minutesOptions = ref(Array.from({ length: 16 }, (_, i) => i)); // 0-15分钟
const secondsOptions = ref(Array.from({ length: 60 }, (_, i) => i)); // 0-59秒

// 页面加载时获取参数
onLoad((options) => {
  console.log('页面加载参数:', options);
  
  // 重新获取用户年级（确保用户数据已加载）
  const currentUserGrade = getUserGrade();
  console.log('页面加载时的用户年级:', currentUserGrade);
  
  // 首先设置默认年级
  practiceSettings.value.grade = currentUserGrade;
  console.log('设置默认年级后的练习设置:', practiceSettings.value);
  
  if (options && options.fromImport === 'true') {
    // 从导入页面进入，使用导入的题目数据
    const importedQuestions = uni.getStorageSync('practiceQuestions');
    const practiceTitle = uni.getStorageSync('practiceTitle');
    const timeLimit = uni.getStorageSync('practiceTimeLimit') || 300;
    
    if (importedQuestions && importedQuestions.length > 0) {
      // 将导入的题目转换为表达式数组
      questionExpressions.value = importedQuestions.map(q => q.question || q);
      practiceSettings.value = {
        grade: currentUserGrade, // 使用页面加载时获取的用户年级
        questionCount: importedQuestions.length,
        timeLimit: timeLimit
      };
      console.log('导入练习设置:', practiceSettings.value);
      
      // 更新页面标题
      if (practiceTitle) {
        console.log('练习标题：', practiceTitle);
      }
    } else {
      uni.showToast({
        title: '题目数据加载失败',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  } else if (options && Object.keys(options).length > 0) {
    // 正常进入，根据参数生成题目
    const grade = parseInt(options.grade) || currentUserGrade; // 优先使用URL参数，否则使用用户年级
    const questionCount = parseInt(options.questionCount) || 10;
    const timeLimit = parseInt(options.timeLimit) || 180;

    practiceSettings.value = { grade, questionCount, timeLimit };
    console.log('URL参数练习设置:', practiceSettings.value);

    // 根据设置生成题目表达式
    generateQuestionExpressions();
  } else {
    // 没有参数时，使用用户默认年级（已在上面设置）
    console.log('无参数时的练习设置:', practiceSettings.value);
    generateQuestionExpressions();
  }
});

// 生成题目表达式函数
const generateQuestionExpressions = () => {
  const { grade, questionCount } = practiceSettings.value;
  console.log('生成题目 - 使用年级:', grade, '题目数量:', questionCount);
  questionExpressions.value = [];

  // 根据年级确定运算范围和类型
  let numberRange, operators;
  switch (grade) {
    case 1:
      // 一年级：10以内加法
      numberRange = { min: 1, max: 10 };
      operators = ['+'];
      break;
    case 2:
      // 二年级：20以内加减法
      numberRange = { min: 1, max: 20 };
      operators = ['+', '-'];
      break;
    case 3:
      // 三年级：100以内加减法和表内乘法
      numberRange = { min: 1, max: 100 };
      operators = ['+', '-', '×'];
      break;
    case 4:
      // 四年级：100以内四则运算
      numberRange = { min: 1, max: 100 };
      operators = ['+', '-', '×', '÷'];
      break;
    case 5:
      // 五年级：1000以内四则运算和小数运算
      numberRange = { min: 1, max: 1000 };
      operators = ['+', '-', '×', '÷'];
      break;
    case 6:
      // 六年级：更大范围四则运算和分数运算
      numberRange = { min: 1, max: 10000 };
      operators = ['+', '-', '×', '÷'];
      break;
    default:
      // 默认：三年级水平
      numberRange = { min: 1, max: 100 };
      operators = ['+', '-', '×'];
  }

  // 生成指定数量的题目表达式
  for (let i = 0; i < questionCount; i++) {
    const expression = generateSingleExpression(numberRange, operators, grade);
    questionExpressions.value.push(expression);
  }
};

// 生成单个表达式
const generateSingleExpression = (numberRange, operators, grade) => {
  // 根据年级决定题目类型
  const questionTypes = getQuestionTypes(grade);
  const questionType = questionTypes[Math.floor(Math.random() * questionTypes.length)];
  
  let questionStr;

  switch (questionType) {
    case 'simple':
      questionStr = generateSimpleExpression(numberRange, operators, grade);
      break;
    case 'bracket':
      questionStr = generateBracketExpression(numberRange, operators, grade);
      break;
    case 'multi_step':
      questionStr = generateMultiStepExpression(numberRange, operators, grade);
      break;
    case 'mixed':
      questionStr = generateMixedExpression(numberRange, operators, grade);
      break;
    default:
      questionStr = generateSimpleExpression(numberRange, operators, grade);
  }

  return questionStr;
};

// 根据年级获取题目类型
const getQuestionTypes = (grade) => {
  switch (grade) {
    case 1:
      return ['simple']; // 一年级：只有简单加法
    case 2:
      return ['simple']; // 二年级：简单加减法
    case 3:
      return ['simple', 'bracket']; // 三年级：简单运算和简单括号题
    case 4:
      return ['simple', 'bracket', 'multi_step']; // 四年级：增加多步运算
    case 5:
      return ['simple', 'bracket', 'multi_step', 'mixed']; // 五年级：包含混合运算
    case 6:
      return ['simple', 'bracket', 'multi_step', 'mixed']; // 六年级：所有类型，更复杂
    default:
      return ['simple', 'bracket']; // 默认：三年级水平
  }
};

// 生成简单表达式
const generateSimpleExpression = (numberRange, operators, grade = 3) => {
  const operator = operators[Math.floor(Math.random() * operators.length)];
  let questionStr;

  switch (operator) {
    case '+':
      const a1 = getRandomInt(numberRange.min, numberRange.max);
      const b1 = getRandomInt(numberRange.min, numberRange.max);
      questionStr = `${a1} + ${b1}`;
      break;

    case '-':
      const a2 = getRandomInt(numberRange.min, numberRange.max);
      const b2 = getRandomInt(numberRange.min, a2);
      questionStr = `${a2} - ${b2}`;
      break;

    case '×':
      // 根据年级调整乘法范围
      let maxMultiplier;
      if (grade <= 3) {
        maxMultiplier = 9; // 三年级及以下：九九乘法表
      } else if (grade <= 4) {
        maxMultiplier = 12; // 四年级：扩展到12
      } else {
        maxMultiplier = Math.min(20, Math.floor(numberRange.max / 10)); // 五六年级：更大范围
      }
      
      const a3 = getRandomInt(1, maxMultiplier);
      const b3 = getRandomInt(1, maxMultiplier);
      questionStr = `${a3} × ${b3}`;
      break;

    case '÷':
      // 根据年级调整除法范围
      let maxDivisor;
      if (grade <= 4) {
        maxDivisor = 9; // 四年级及以下：表内除法
      } else if (grade <= 5) {
        maxDivisor = 12; // 五年级：扩展范围
      } else {
        maxDivisor = Math.min(20, Math.floor(numberRange.max / 10)); // 六年级：更大范围
      }
      
      let divisor = getRandomInt(2, maxDivisor);
      let quotient = getRandomInt(1, Math.min(maxDivisor, Math.floor(numberRange.max / divisor)));
      let dividend = divisor * quotient;
      questionStr = `${dividend} ÷ ${divisor}`;
      break;

    default:
      const a = getRandomInt(numberRange.min, numberRange.max);
      const b = getRandomInt(numberRange.min, numberRange.max);
      questionStr = `${a} + ${b}`;
  }

  return questionStr;
};

// 生成带括号的表达式
const generateBracketExpression = (numberRange, operators, grade) => {
  const bracketTypes = [
    'addition_in_bracket',    // (a + b) × c
    'subtraction_in_bracket', // (a - b) × c
    'multiplication_first',   // a × (b + c)
    'division_first'         // a ÷ (b + c)
  ];
  
  const bracketType = bracketTypes[Math.floor(Math.random() * bracketTypes.length)];
  let questionStr;
  
  const maxNum = Math.min(numberRange.max, grade <= 4 ? 50 : 100);
  
  switch (bracketType) {
    case 'addition_in_bracket':
      const a1 = getRandomInt(1, maxNum / 5);
      const b1 = getRandomInt(1, maxNum / 5);
      const c1 = getRandomInt(2, 5);
      questionStr = `(${a1} + ${b1}) × ${c1}`;
      break;
      
    case 'subtraction_in_bracket':
      const a2 = getRandomInt(10, maxNum / 3);
      const b2 = getRandomInt(1, a2 - 1);
      const c2 = getRandomInt(2, 5);
      questionStr = `(${a2} - ${b2}) × ${c2}`;
      break;
      
    case 'multiplication_first':
      const a3 = getRandomInt(2, 8);
      const b3 = getRandomInt(1, maxNum / 10);
      const c3 = getRandomInt(1, maxNum / 10);
      questionStr = `${a3} × (${b3} + ${c3})`;
      break;
      
    case 'division_first':
      const divisor = getRandomInt(2, 6);
      const b4 = getRandomInt(1, 8);
      const c4 = getRandomInt(1, 8);
      const dividend = divisor * (b4 + c4);
      questionStr = `${dividend} ÷ (${b4} + ${c4})`;
      break;
      
    default:
      return generateSimpleExpression(numberRange, operators);
  }
  
  return questionStr;
};

// 生成多步骤计算表达式
const generateMultiStepExpression = (numberRange, operators, grade) => {
  const multiStepTypes = [
    'three_numbers',      // a + b - c
    'mixed_operations',   // a × b + c
    'division_addition',  // a ÷ b + c
    'complex_bracket'     // (a + b) × c - d
  ];
  
  const stepType = multiStepTypes[Math.floor(Math.random() * multiStepTypes.length)];
  let questionStr;
  
  const maxNum = Math.min(numberRange.max, grade <= 4 ? 30 : 80);
  
  switch (stepType) {
    case 'three_numbers':
      const a1 = getRandomInt(10, maxNum);
      const b1 = getRandomInt(1, maxNum / 2);
      const c1 = getRandomInt(1, Math.min(b1, maxNum / 3));
      questionStr = `${a1} + ${b1} - ${c1}`;
      break;
      
    case 'mixed_operations':
      const a2 = getRandomInt(2, 8);
      const b2 = getRandomInt(2, 8);
      const c2 = getRandomInt(1, maxNum / 4);
      questionStr = `${a2} × ${b2} + ${c2}`;
      break;
      
    case 'division_addition':
      const divisor = getRandomInt(2, 6);
      const quotient = getRandomInt(3, 10);
      const dividend = divisor * quotient;
      const addend = getRandomInt(1, maxNum / 4);
      questionStr = `${dividend} ÷ ${divisor} + ${addend}`;
      break;
      
    case 'complex_bracket':
      const a4 = getRandomInt(1, 10);
      const b4 = getRandomInt(1, 10);
      const c4 = getRandomInt(2, 5);
      const d4 = getRandomInt(1, 15);
      questionStr = `(${a4} + ${b4}) × ${c4} - ${d4}`;
      break;
      
    default:
      return generateSimpleExpression(numberRange, operators);
  }
  
  return questionStr;
};

// 生成混合运算表达式
const generateMixedExpression = (numberRange, operators, grade) => {
  const mixedTypes = [
    'fraction_like',      // 12 + 8 ÷ 4
    'bracket_mixed',      // 15 - (3 + 2) × 2
    'double_bracket',     // (8 + 4) ÷ (6 - 3)
    'chain_operations'    // 2 × 3 + 4 × 5
  ];
  
  const mixedType = mixedTypes[Math.floor(Math.random() * mixedTypes.length)];
  let questionStr;
  
  switch (mixedType) {
    case 'fraction_like':
      const a1 = getRandomInt(10, 50);
      const divisor1 = getRandomInt(2, 6);
      const dividend1 = divisor1 * getRandomInt(2, 8);
      questionStr = `${a1} + ${dividend1} ÷ ${divisor1}`;
      break;
      
    case 'bracket_mixed':
      const a2 = getRandomInt(15, 30);
      const b2 = getRandomInt(1, 5);
      const c2 = getRandomInt(1, 5);
      const d2 = getRandomInt(2, 4);
      questionStr = `${a2} - (${b2} + ${c2}) × ${d2}`;
      break;
      
    case 'double_bracket':
      const a3 = getRandomInt(8, 20);
      const b3 = getRandomInt(1, a3 / 2);
      const c3 = getRandomInt(4, 10);
      const d3 = getRandomInt(1, c3 - 1);
      questionStr = `(${a3} + ${b3}) ÷ (${c3} - ${d3})`;
      break;
      
    case 'chain_operations':
      const a4 = getRandomInt(2, 5);
      const b4 = getRandomInt(2, 5);
      const c4 = getRandomInt(2, 6);
      const d4 = getRandomInt(2, 6);
      questionStr = `${a4} × ${b4} + ${c4} × ${d4}`;
      break;
      
    default:
      return generateBracketExpression(numberRange, operators, grade);
  }
  
  return questionStr;
};

// 处理练习完成
const handlePracticeComplete = (practiceResult) => {
  // 添加年级信息到练习结果中
  const resultWithGrade = {
    ...practiceResult,
    grade: practiceSettings.value.grade
  };
  
  // 跳转到结果页面并传递数据
  uni.redirectTo({
    url: `/pages/result/result?data=${encodeURIComponent(JSON.stringify(resultWithGrade))}`
  });
};

// 获取随机整数
const getRandomInt = (min, max) => {
  return Math.floor(Math.random() * (max - min + 1)) + min;
};

// 返回首页
const navigateToHome = (exitData) => {
  console.log('练习退出事件:', exitData);
  
  if (exitData && exitData.reason === 'timeout') {
    console.log('练习超时，返回主页');
    // 超时情况下，显示提示并返回主页
    uni.showToast({
      title: exitData.message || '练习时间已结束',
      icon: 'none',
      duration: 2000
    });
  }
  
  // 使用 reLaunch 确保清除页面栈，避免返回问题
  uni.reLaunch({ 
    url: '/pages/home/home' 
  });
};

</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f7f9fc;
  padding-top: 20px;
  position: relative;
  overflow: hidden;
  width: 100%;
  max-width: 100vw;
}

// 加载状态
.loading-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-text {
  font-size: 16px;
  color: #666;
  text-align: center;
}

// 左上角标题
.navbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  position: relative;
  z-index: 10;
  width: 100%;
  padding: 0 20px;
  gap: 8px;
}

.navbar-title {
  font-size: 26px;
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

.back-home-icon {
  cursor: pointer;
}

// 进度和时间区域
.progress-timer-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 18px; // 减少间距
  gap: 12px;
  padding: 0 16px;
  flex-wrap: nowrap;
}

.progress-container {
  flex: 1;
  min-width: 0;
}

.progress-text {
  font-size: 14px;
  color: #5a6474;
  margin-bottom: 6px;
  display: inline-block;
  white-space: nowrap;
}

.progress-bar {
  height: 7px;
  width: 100%;
  background-color: #edf2f7;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.05);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #48bb78, #38a169);
  border-radius: 4px;
  transition: width 0.5s ease;
  box-shadow: 0 0 5px rgba(72, 187, 120, 0.4);
}

.timer {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e53e3e;
  font-weight: 500;
  white-space: nowrap;
  padding: 6px 10px;
  background-color: rgba(229, 62, 62, 0.05);
  border-radius: 20px;
}

.timer-icon {
  font-size: 16px;
}

.timer-text {
  font-size: 15px;
}

// 动画视口
.animation-viewport {
  width: 100%;
  padding: 0 16px;
  position: relative;
  flex: 1;
  min-height: 450px; // 适当降低最小高度
}

// 题目+选项统一动画容器
.question-options-group {
  width: 100%;
  position: absolute;
  top: 0;
  left: 16px;
  width: calc(100% - 32px);
}

// 核心同步动画
.slide-enter-active,
.slide-leave-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-100%) scale(0.95);
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.95);
}

.slide-enter-from[data-direction="backward"] {
  transform: translateX(-100%) scale(0.95);
}

.slide-leave-to[data-direction="backward"] {
  transform: translateX(100%) scale(0.95);
}

// 精简的题目卡片（移除下方元素）
.question-card {
  background-color: #fff;
  border-radius: 16px;
  padding: 18px 20px; // 减少内边距
  margin-bottom: 22px; // 减少与选项的间距
  position: relative;
  width: 100%;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.07);

  // 顶部装饰线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(90deg, #4299e1, #63b3ed);
    border-top-left-radius: 16px;
    border-top-right-radius: 16px;
  }

  .question-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 15px; // 减少标题与题目间距
  }

  .question-tag {
    font-size: 13px;
    color: #4299e1;
    background-color: #ebf8ff;
    padding: 3px 10px;
    border-radius: 15px;
    font-weight: 500;
  }

  .question-difficulty {
    font-size: 13px;
    padding: 3px 10px;
    border-radius: 15px;
    font-weight: 500;
  }

  .question-difficulty.easy {
    color: #38a169;
    background-color: #f0fff4;
  }

  .question-difficulty.medium {
    color: #dd6b20;
    background-color: #fffaf0;
  }

  .question-difficulty.hard {
    color: #e53e3e;
    background-color: #fff5f5;
  }

  // 增大字号的题目内容
  .question-content {
    min-height: 80px; // 适当降低高度
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px 0;
  }

  .question-text {
    font-size: 42px;
    color: #2d3748;
    font-weight: 600;
    text-align: center;
    word-break: keep-all;
    letter-spacing: 1px;
    padding: 12px 24px;
    background-color: rgba(66, 153, 225, 0.03);
    border-radius: 12px;
  }
}

// 选项区域
.options-container {
  display: flex;
  flex-direction: column;
  gap: 16px; // 减少选项间距
  width: 100%;
  margin-bottom: 25px; // 减少与底部按钮的间距
}

.option-item {
  background-color: #fff;
  border-radius: 14px;
  padding: 16px 20px; // 减少内边距
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  position: relative;
  width: 100%;
  height: 64px;
  border: 2px solid transparent;
  animation: optionFadeIn 0.4s ease forwards;
  opacity: 0;
  transform: translateY(10px);
  z-index: 1;
}

@keyframes optionFadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.option-item:disabled {
  opacity: 0.9;
  cursor: default;
}

.option-item:active:not(:disabled) {
  transform: scale(0.985);
}

.option-marker {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #edf2f7;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-right: 16px;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.option-text {
  font-size: 22px;
  color: #334155;
  flex: 1;
  word-break: keep-all;
}

.option-feedback {
  color: #38a169;
  font-size: 24px;
  font-weight: bold;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s ease 0.2s;
  margin-left: 10px;
}

.option-feedback.incorrect {
  color: #e53e3e;
}

.option-item.selected {
  border-color: #4299e1;
  background-color: #f0f9ff;
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.15);
}

.option-item.selected .option-marker {
  background-color: #4299e1;
  color: white;
}

.option-item.correct {
  border-color: #38a169;
  background-color: #f0fff4;
  box-shadow: 0 4px 12px rgba(56, 161, 105, 0.15);
}

.option-item.correct .option-marker {
  background-color: #38a169;
  color: white;
}

.option-item.correct .option-feedback {
  opacity: 1;
  transform: scale(1);
}

.option-item.incorrect {
  border-color: #e53e3e;
  background-color: #fff5f5;
  box-shadow: 0 4px 12px rgba(229, 62, 62, 0.15);
}

.option-item.incorrect .option-marker {
  background-color: #e53e3e;
  color: white;
}

.option-item.incorrect .option-feedback {
  opacity: 1;
  transform: scale(1);
}

// 底部导航区
.bottom-nav {
  display: flex;
  gap: 14px;
  width: 100%;
  padding: 0 16px 20px; // 减少底部间距
  position: relative;
  z-index: 5;
  margin-top: auto;
}

.nav-btn {
  flex: 1;
  height: 54px;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 500;
  transition: all 0.25s ease;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.prev-btn {
  background-color: #f8fafc;
  color: #475569;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

.prev-btn:active:not(:disabled) {
  background-color: #e2e8f0;
  transform: scale(0.98);
}

.prev-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.next-btn {
  background: linear-gradient(90deg, #4299e1, #3182ce);
  color: white;
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.3);
}

.next-btn:active:not(:disabled) {
  transform: scale(0.98);
  background: linear-gradient(90deg, #3182ce, #2b6cb0);
}

.next-btn:disabled {
  opacity: 0.5;
  cursor: default;
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.1);
}

.nav-icon {
  font-size: 19px;
  transition: transform 0.2s ease;
}

.prev-btn:active .nav-icon:not(:disabled) {
  transform: translateX(-3px);
}

.next-btn:active .nav-icon:not(:disabled) {
  transform: translateX(3px);
}

// 装饰元素
.decor {
  position: absolute;
  border-radius: 50%;
  z-index: 0;
  filter: blur(60px);
  opacity: 0.1;
}

.decor-1 {
  width: 300px;
  height: 300px;
  top: 15%;
  left: -150px;
  background: linear-gradient(135deg, #4299e1, #81c3fd);
}

.decor-2 {
  width: 250px;
  height: 250px;
  bottom: 10%;
  right: -120px;
  background: linear-gradient(135deg, #48bb78, #81e6d9);
}

.decor-3 {
  width: 200px;
  height: 200px;
  top: 60%;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #f687b3, #f72585);
}

// 适配小屏手机
@media (max-width: 375px) {
  .navbar-title {
    font-size: 24px;
  }

  .question-text {
    font-size: 36px !important;
  }

  .option-text {
    font-size: 19px !important;
  }

  .nav-btn {
    height: 50px;
    font-size: 16px;
  }

  .options-container {
    gap: 14px;
  }

  .option-item {
    height: 58px;
  }

  .animation-viewport {
    min-height: 400px;
  }
}

// 自定义弹窗样式
.custom-popup {
  background-color: #fff;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.custom-popup .popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.custom-popup .popup-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.custom-popup .close-icon {
  padding: 5px;
}

.custom-popup .options-container {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px 0;
}

.custom-popup .option-item {
  padding: 15px 20px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  color: #333;

  &:last-child {
    border-bottom: none;
  }

  &.selected {
    color: #4299e1;
    font-weight: 500;
    background-color: #f0f9ff;
  }
}

.custom-popup .popup-footer {
  padding: 20px;
  padding-top: 10px;
}

.custom-popup .confirm-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(90deg, #4299e1, #3182ce);
  color: white;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.3);
}
</style>
