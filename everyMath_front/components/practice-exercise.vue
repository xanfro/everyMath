<template>
  <view class="practice-container">
    <!-- 进度和时间区域 -->
    <view class="progress-timer-bar">
      <view class="progress-container">
        <text class="progress-text">第 {{ currentQuestionIndex + 1 }} / {{ totalQuestions }} 题</text>
        <view class="progress-bar">
          <view
            class="progress-fill"
            :style="{ width: (currentQuestionIndex + 1) / totalQuestions * 100 + '%' }"
          ></view>
        </view>
      </view>

      <view class="timer">
        <view class="timer-icon">⏱</view>
        <text class="timer-text">{{ formatTime(remainingTime) }}</text>
      </view>
    </view>

    <!-- 动画容器 -->
    <view class="animation-viewport">
      <!-- 题目+选项统一动画容器 -->
      <transition name="slide">
        <view class="question-options-group" :key="currentQuestionIndex" :data-direction="direction">
          <!-- 精简的题目卡片 -->
          <view class="question-card">
            <view class="question-header">
              <text class="question-tag">题目</text>
            </view>

            <!-- 题目内容 -->
            <view class="question-content">
              <text class="question-text">{{ currentQuestion.question }}</text>
            </view>
          </view>

          <!-- 选项区域 -->
          <view class="options-container">
            <view
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              class="option-item"
              :class="{
                selected: selectedOption === index,
                correct: isAnswered && index === currentQuestion.correctAnswer,
                incorrect: isAnswered && selectedOption === index && index !== currentQuestion.correctAnswer
              }"
              @click="handleOptionClick(index)"
              :disabled="isAnswered || isViewingPrevious"
              :style="{ animationDelay: (index * 80) + 'ms' }"
            >
              <view class="option-marker">{{ String.fromCharCode(65 + index) }}</view>
              <text class="option-text">{{ option }}</text>
              <view
                class="option-feedback"
                v-if="isAnswered && index === currentQuestion.correctAnswer"
              >✓</view>
              <view
                class="option-feedback incorrect"
                v-if="isAnswered && selectedOption === index && index !== currentQuestion.correctAnswer"
              >✗</view>
            </view>
          </view>
        </view>
      </transition>
    </view>

    <!-- 底部导航区 -->
    <view class="bottom-nav">
      <button
        class="nav-btn prev-btn"
        @click="handlePrevQuestion"
        :disabled="currentQuestionIndex === 0"
      >
        <view class="nav-icon">←</view>
        <text class="nav-text">上一题</text>
      </button>

      <button
        class="nav-btn next-btn"
        @click="handleNextQuestion"
        :disabled="!isAnswered"
      >
        <text class="nav-text">
          {{ currentQuestionIndex === totalQuestions - 1 ? '完成练习' : '下一题' }}
        </text>
        <view class="nav-icon" v-if="currentQuestionIndex < totalQuestions - 1">→</view>
      </button>
    </view>

    <!-- 装饰元素 -->
    <view class="decor decor-1"></view>
    <view class="decor decor-2"></view>
    <view class="decor decor-3"></view>

    <!-- 超时弹窗 -->
    <view v-if="showTimeoutModal" class="timeout-modal">
      <view class="timeout-content">
        <view class="timeout-icon">⏰</view>
        <text class="timeout-title">时间到了！</text>
        <text class="timeout-message">练习时间已结束，请返回主页重新开始</text>
        <button class="timeout-btn" @click="handleTimeoutConfirm">返回主页</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { saveMistakeRecord } from '@/api/index.js';
import { getUserId } from '@/api/utils.js';

// 定义组件props
const props = defineProps({
  // 题目组 - 字符串数组，每个字符串代表一个多项式
  questionExpressions: {
    type: Array,
    required: true,
    default: () => []
  },
  // 时间限制（秒）
  timeLimit: {
    type: Number,
    required: true,
    default: 300
  }
});

// 定义组件事件
const emit = defineEmits(['complete', 'exit']);

// 题目数据
const questions = ref([]);
const currentQuestionIndex = ref(0);
const isViewingPrevious = ref(false);
const remainingTime = ref(props.timeLimit);
const direction = ref('forward');
const practiceStartTime = ref(null); // 记录练习开始时间

// 用户ID
const userId = ref(null);

// 超时弹窗状态
const showTimeoutModal = ref(false);

// 初始化题目
const initializeQuestions = () => {
  questions.value = props.questionExpressions.map((expression, index) => {
    // 计算表达式的正确答案
    const correctAnswer = evaluateExpression(expression);
    
    // 生成选项
    const options = generateOptions(correctAnswer);
    
    // 随机打乱选项
    shuffleArray(options);
    
    // 找到正确答案在选项中的索引
    const correctAnswerIndex = options.indexOf(correctAnswer);
    
    return {
      id: index + 1,
      question: expression,
      options: options,
      correctAnswer: correctAnswerIndex,
      userAnswer: -1,
      answered: false
    };
  });
  
  // 重置状态
  currentQuestionIndex.value = 0;
  remainingTime.value = props.timeLimit;
};

// 计算表达式的值
const evaluateExpression = (expression) => {
  try {
    // 将数学符号转换为JavaScript可识别的符号
    const jsExpression = expression
      .replace(/×/g, '*')
      .replace(/÷/g, '/')
      .replace(/（/g, '(')
      .replace(/）/g, ')');
    
    // 安全地计算表达式
    return Function(`"use strict"; return (${jsExpression})`)();
  } catch (error) {
    console.error('表达式计算错误:', expression, error);
    return 0;
  }
};

// 移除难度判断函数

// 生成选项
const generateOptions = (correctAnswer) => {
  const options = [correctAnswer];
  const offset = 5; // 固定偏移量

  while (options.length < 4) {
    const delta = getRandomInt(1, offset);
    const sign = Math.random() > 0.5 ? 1 : -1;
    const option = correctAnswer + sign * delta;

    if (option > 0 && !options.includes(option)) {
      options.push(option);
    }
  }

  return options;
};

// 获取随机整数
const getRandomInt = (min, max) => {
  return Math.floor(Math.random() * (max - min + 1)) + min;
};

// 随机打乱数组
const shuffleArray = (array) => {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
};

// 保存错题记录
const saveMistakeToDatabase = async (question) => {
  try {
    if (!userId.value) {
      console.warn('用户ID未找到，无法保存错题记录');
      return;
    }

    const mistakeData = {
      userId: userId.value,
      question: question.question, // 添加 = ? 后缀
      correctAnswer: question.options[question.correctAnswer].toString(),
      corrected: '0' // 0表示未纠正
    };

    console.log('保存错题记录:', mistakeData);
    await saveMistakeRecord(mistakeData);
    console.log('错题记录保存成功');
  } catch (error) {
    console.error('保存错题记录失败:', error);
    // 不阻断用户练习流程，只记录错误
  }
};

// 计算属性
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || {});
const totalQuestions = computed(() => questions.value.length);
const selectedOption = computed({
  get: () => currentQuestion.value.userAnswer,
  set: (value) => { 
    if (currentQuestion.value) {
      currentQuestion.value.userAnswer = value; 
    }
  }
});
const isAnswered = computed({
  get: () => currentQuestion.value.answered || false,
  set: (value) => { 
    if (currentQuestion.value) {
      currentQuestion.value.answered = value; 
    }
  }
});

// 移除难度相关的计算属性

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 选项点击处理
const handleOptionClick = async (index) => {
  if (!isAnswered.value) {
    selectedOption.value = index;
    isAnswered.value = true;
    isViewingPrevious.value = false;
    
    // 检查答案是否正确，如果错误则保存错题记录
    const currentQ = currentQuestion.value;
    if (currentQ && index !== currentQ.correctAnswer) {
      console.log('答案错误，保存错题记录...');
      await saveMistakeToDatabase(currentQ);
    }
  }
};

// 上一题处理
const handlePrevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    direction.value = 'backward';
    currentQuestionIndex.value--;
    isViewingPrevious.value = true;
  }
};

// 下一题处理
const handleNextQuestion = () => {
  if (isAnswered.value && currentQuestionIndex.value < questions.value.length - 1) {
    direction.value = 'forward';
    currentQuestionIndex.value++;
    isViewingPrevious.value = false;
  } else if (isAnswered.value && currentQuestionIndex.value === questions.value.length - 1) {
    // 完成练习，统计结果
    completePractice();
  }
};

// 完成练习
const completePractice = () => {
  const total = questions.value.length;
  let correct = 0;

  questions.value.forEach(q => {
    if (q.userAnswer === q.correctAnswer) {
      correct++;
    }
  });

  const practiceResult = {
    totalQuestions: total,
    correctCount: correct,
    startTime: practiceStartTime.value || new Date().toISOString(), // 使用记录的开始时间
    timeSpent: formatTime(props.timeLimit - remainingTime.value),
    timeLimit: formatTime(props.timeLimit),
    timeLimitSeconds: props.timeLimit, // 添加秒数格式的时间限制
    timeSpentSeconds: props.timeLimit - remainingTime.value, // 添加秒数格式的用时
    questions: questions.value
  };

  // 触发完成事件
  emit('complete', practiceResult);
};

// 倒计时逻辑
let timer = null;
const startTimer = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--;
    } else if (remainingTime.value === 0 && !showTimeoutModal.value) {
      // 时间到了，显示超时弹窗
      console.log('练习时间到，显示超时弹窗');
      showTimeoutModal.value = true;
      stopTimer(); // 停止计时器
    }
  }, 1000);
};

const stopTimer = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

// 处理超时确认
const handleTimeoutConfirm = () => {
  console.log('用户确认超时，返回主页');
  showTimeoutModal.value = false;
  
  // 触发退出事件，让父组件处理返回主页
  emit('exit', { reason: 'timeout', message: '练习时间已结束' });
};

// 监听props变化
watch(() => props.questionExpressions, () => {
  initializeQuestions();
}, { immediate: true });

watch(() => props.timeLimit, (newLimit) => {
  remainingTime.value = newLimit;
});

// 组件挂载和卸载
onMounted(() => {
  // 记录练习开始时间
  practiceStartTime.value = new Date().toISOString();
  
  // 获取用户ID
  try {
    userId.value = getUserId();
    console.log('获取到用户ID:', userId.value);
  } catch (error) {
    console.error('获取用户ID失败:', error);
  }
  
  initializeQuestions();
  startTimer();
});

onUnmounted(() => {
  stopTimer();
});

// 暴露方法给父组件
defineExpose({
  resetPractice: initializeQuestions,
  getCurrentProgress: () => ({
    current: currentQuestionIndex.value + 1,
    total: totalQuestions.value,
    timeRemaining: remainingTime.value
  })
});
</script>

<style lang="scss" scoped>
* {
  box-sizing: border-box;
  max-width: 100%;
}

.practice-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f7f9fc;
  position: relative;
  overflow: hidden;
  width: 100%;
  max-width: 100vw;
  box-sizing: border-box;
  padding-bottom: 100px; // 为固定底部按钮留出空间
}

// 进度和时间区域
.progress-timer-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 18px;
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
  max-width: 100%;
  padding: 0;
  position: relative;
  flex: 1;
  min-height: 450px;
  box-sizing: border-box;
  overflow: hidden;
}

// 题目+选项统一动画容器
.question-options-group {
  position: absolute;
  top: 0;
  left: 16px;
  right: 16px;
  width: calc(100% - 32px);
  box-sizing: border-box;
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

// 题目卡片
.question-card {
  background-color: #fff;
  border-radius: 16px;
  padding: 18px 20px;
  margin-bottom: 22px;
  position: relative;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.07);

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
    margin-bottom: 15px;
  }

  .question-tag {
    font-size: 13px;
    color: #4299e1;
    background-color: #ebf8ff;
    padding: 3px 10px;
    border-radius: 15px;
    font-weight: 500;
  }

  // 移除难度相关样式

  .question-content {
    min-height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px 0;
  }

  .question-text {
    font-size: 32px;
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
  gap: 16px;
  width: 100%;
  max-width: 100%;
  margin-bottom: 25px;
  box-sizing: border-box;
}

.option-item {
  background-color: #fff;
  border-radius: 14px;
  padding: 16px 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  position: relative;
  width: 100%;
  max-width: 100%;
  height: 64px;
  border: 2px solid transparent;
  animation: optionFadeIn 0.4s ease forwards;
  opacity: 0;
  transform: translateY(10px);
  z-index: 1;
  box-sizing: border-box;

  &:disabled {
    opacity: 0.9;
    cursor: default;
  }

  &:active:not(:disabled) {
    transform: scale(0.985);
  }

  &.selected {
    border-color: #4299e1;
    background-color: #f0f9ff;
    box-shadow: 0 4px 12px rgba(66, 153, 225, 0.15);

    .option-marker {
      background-color: #4299e1;
      color: white;
    }
  }

  &.correct {
    border-color: #38a169;
    background-color: #f0fff4;
    box-shadow: 0 4px 12px rgba(56, 161, 105, 0.15);

    .option-marker {
      background-color: #38a169;
      color: white;
    }

    .option-feedback {
      opacity: 1;
      transform: scale(1);
    }
  }

  &.incorrect {
    border-color: #e53e3e;
    background-color: #fff5f5;
    box-shadow: 0 4px 12px rgba(229, 62, 62, 0.15);

    .option-marker {
      background-color: #e53e3e;
      color: white;
    }

    .option-feedback {
      opacity: 1;
      transform: scale(1);
    }
  }
}

@keyframes optionFadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

  &.incorrect {
    color: #e53e3e;
  }
}

// 底部导航区
.bottom-nav {
  display: flex;
  gap: 14px;
  width: 100%;
  max-width: 100vw;
  padding: 20px 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: #f7f9fc;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
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

  &:disabled {
    opacity: 0.5;
    cursor: default;
  }
}

.prev-btn {
  background-color: #f8fafc;
  color: #475569;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);

  &:active:not(:disabled) {
    background-color: #e2e8f0;
    transform: scale(0.98);
  }
}

.next-btn {
  background: linear-gradient(90deg, #4299e1, #3182ce);
  color: white;
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.3);

  &:active:not(:disabled) {
    transform: scale(0.98);
    background: linear-gradient(90deg, #3182ce, #2b6cb0);
  }

  &:disabled {
    box-shadow: 0 4px 15px rgba(66, 153, 225, 0.1);
  }
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

// 超时弹窗样式
.timeout-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.timeout-content {
  background-color: #fff;
  border-radius: 20px;
  padding: 32px 24px;
  margin: 0 20px;
  max-width: 320px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  animation: timeoutModalShow 0.3s ease-out;
}

@keyframes timeoutModalShow {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.timeout-icon {
  font-size: 48px;
  margin-bottom: 16px;
  animation: timeoutIconPulse 1s ease-in-out infinite;
}

@keyframes timeoutIconPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.timeout-title {
  font-size: 24px;
  font-weight: 600;
  color: #e53e3e;
  margin-bottom: 12px;
  display: block;
}

.timeout-message {
  font-size: 16px;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 24px;
  display: block;
}

.timeout-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(90deg, #e53e3e, #c53030);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(229, 62, 62, 0.3);

  &:active {
    transform: scale(0.98);
    background: linear-gradient(90deg, #c53030, #9c2626);
  }
}

// 适配小屏手机
@media (max-width: 375px) {
  .question-text {
    font-size: 28px !important;
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

  .timeout-content {
    margin: 0 16px;
    padding: 28px 20px;
  }

  .timeout-icon {
    font-size: 40px;
  }

  .timeout-title {
    font-size: 20px;
  }

  .timeout-message {
    font-size: 14px;
  }
}
</style>
