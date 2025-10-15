<template>
  <!-- 练习设置主弹窗 -->
  <uni-popup ref="mainPopup" type="center" :mask-click="false">
    <view class="popup-container">
      <view class="popup-header">
        <text class="popup-title">练习设置</text>
        <uni-icons type="closeempty" size="24" color="#999" @click="closePopup" class="close-icon"></uni-icons>
      </view>

      <view class="popup-content">
        <view class="setting-item">
          <text class="setting-label">年级</text>
          <view class="picker-selector" @click="openGradePopup">
            <text>{{ gradeOptions[selectedGradeIndex].label }}</text>
            <uni-icons type="arrowdown" size="16" color="#999"></uni-icons>
          </view>
        </view>

        <view class="setting-item">
          <text class="setting-label">题目数量</text>
          <view class="picker-selector" @click="openQuestionCountPopup">
            <text>{{ questionCountOptions[selectedQuestionCountIndex] }} 题</text>
            <uni-icons type="arrowdown" size="16" color="#999"></uni-icons>
          </view>
        </view>

        <view class="setting-item">
          <text class="setting-label">练习时间</text>
          <view class="time-picker-container">
            <view class="time-picker" @click="openMinutesPopup">
              <text>{{ minutesOptions[selectedMinutesIndex] }} 分</text>
            </view>
            <view class="time-picker" @click="openSecondsPopup">
              <text>{{ secondsOptions[selectedSecondsIndex] }} 秒</text>
            </view>
          </view>
        </view>
      </view>

      <view class="popup-footer">
        <button class="confirm-btn" @click="confirmSettings">开始练习</button>
      </view>
    </view>
  </uni-popup>

  <!-- 年级选择弹窗 -->
  <uni-popup ref="gradePopup" type="center">
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
          :class="{ selected: index === selectedGradeIndex }"
          @click="selectGrade(index)"
        >
          {{ item.label }}
        </view>
      </scroll-view>
    </view>
  </uni-popup>

  <!-- 题目数量选择弹窗 -->
  <uni-popup ref="questionCountPopup" type="center">
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
          :class="{ selected: index === selectedQuestionCountIndex }"
          @click="selectQuestionCount(index)"
        >
          {{ item }} 题
        </view>
      </scroll-view>
    </view>
  </uni-popup>

  <!-- 分钟选择弹窗 -->
  <uni-popup ref="minutesPopup" type="center">
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
          :class="{ selected: index === selectedMinutesIndex }"
          @click="selectMinutes(index)"
        >
          {{ item }} 分
        </view>
      </scroll-view>
    </view>
  </uni-popup>

  <!-- 秒数选择弹窗 -->
  <uni-popup ref="secondsPopup" type="center">
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
          :class="{ selected: index === selectedSecondsIndex }"
          @click="selectSeconds(index)"
        >
          {{ item }} 秒
        </view>
      </scroll-view>
    </view>
  </uni-popup>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { getUserData } from '@/api/utils.js';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close', 'confirm']);

// 弹窗引用
const mainPopup = ref(null);
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

// 获取用户年级作为默认值
const getUserGrade = () => {
  try {
    const userData = getUserData();
    if (userData && userData.grade) {
      const grade = parseInt(userData.grade) || 1;
      return grade - 1; // 转换为索引（0-5）
    }
  } catch (error) {
    console.error('练习设置组件获取用户年级失败:', error);
  }
  return 0; // 默认一年级索引
};

// 默认值
const selectedGradeIndex = ref(getUserGrade()); // 使用当前用户年级
const selectedQuestionCountIndex = ref(1); // 默认10题
const selectedMinutesIndex = ref(3); // 默认3分钟
const selectedSecondsIndex = ref(0); // 默认0秒

// 监听visible属性变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 延迟打开弹窗，确保DOM已经渲染
    setTimeout(() => {
      mainPopup.value.open();
    }, 50);
  } else {
    mainPopup.value.close();
  }
});

// 获取用户信息来设置默认年级
onMounted(() => {
  // 重新获取用户年级（确保用户数据已加载）
  const userGradeIndex = getUserGrade();
  selectedGradeIndex.value = userGradeIndex;
  console.log('练习设置组件 - 设置默认年级索引:', userGradeIndex, '对应年级:', userGradeIndex + 1);
});

// 打开弹窗方法
const openGradePopup = () => {
  gradePopup.value.open();
};

const openQuestionCountPopup = () => {
  questionCountPopup.value.open();
};

const openMinutesPopup = () => {
  minutesPopup.value.open();
};

const openSecondsPopup = () => {
  secondsPopup.value.open();
};

// 关闭弹窗方法
const closeGradePopup = () => {
  gradePopup.value.close();
};

const closeQuestionCountPopup = () => {
  questionCountPopup.value.close();
};

const closeMinutesPopup = () => {
  minutesPopup.value.close();
};

const closeSecondsPopup = () => {
  secondsPopup.value.close();
};

// 选择器相关方法
const selectGrade = (index) => {
  selectedGradeIndex.value = index;
  closeGradePopup();
};

const selectQuestionCount = (index) => {
  selectedQuestionCountIndex.value = index;
  closeQuestionCountPopup();
};

const selectMinutes = (index) => {
  selectedMinutesIndex.value = index;
  closeMinutesPopup();
};

const selectSeconds = (index) => {
  selectedSecondsIndex.value = index;
  closeSecondsPopup();
};

const closePopup = () => {
  mainPopup.value.close();
  emit('close');
};

const confirmSettings = () => {
  const settings = {
    grade: gradeOptions.value[selectedGradeIndex.value].value,
    questionCount: questionCountOptions.value[selectedQuestionCountIndex.value],
    timeLimit: minutesOptions.value[selectedMinutesIndex.value] * 60 +
               secondsOptions.value[selectedSecondsIndex.value]
  };

  mainPopup.value.close();
  emit('confirm', settings);
};

// 暴露方法给父组件调用
defineExpose({
  selectGrade,
  selectQuestionCount,
  selectMinutes,
  selectSeconds
});
</script>

<style lang="scss" scoped>
.popup-container {
  background-color: #fff;
  border-radius: 16px;
  width: 90%;
  min-width: 350px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  margin: 0 auto;
  transform: translate(0, 0);
  flex-shrink: 0;
  max-height: 90vh;
  align-self: center;
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
}

.popup-content {
  padding: 20px;
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 25px;

  &:last-child {
    margin-bottom: 0;
  }
}

.setting-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  width: 80px;
  margin-right: 15px;
  flex-shrink: 0;
}

.picker-selector {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background-color: #f8f9fa;
  border-radius: 10px;
  font-size: 15px;
  color: #666;
}

.time-picker-container {
  flex: 1;
  display: flex;
  gap: 15px;
}

.time-picker {
  flex: 1;
  padding: 12px 15px;
  background-color: #f8f9fa;
  border-radius: 10px;
  font-size: 15px;
  color: #666;
  text-align: center;
}

.popup-footer {
  padding: 20px;
  padding-top: 10px;
}

.confirm-btn {
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

// 自定义弹窗样式
.custom-popup {
  background-color: #fff;
  border-radius: 16px;
  width: 90%;
  min-width: 320px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  margin: 0 auto;
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
