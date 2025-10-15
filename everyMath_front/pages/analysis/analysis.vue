<template>
  <view class="analysis-container">
    <!-- 背景数学元素 -->
    <view class="bg-elements">
      <view class="math-symbol" :style="{top: '5%', left: '5%', animationDelay: '0s'}">∑</view>
      <view class="math-symbol" :style="{top: '70%', left: '10%', animationDelay: '0.5s'}">∫</view>
      <view class="math-symbol" :style="{top: '30%', left: '20%', animationDelay: '1s'}">∞</view>
      <view class="math-symbol" :style="{top: '50%', left: '80%', animationDelay: '1.5s'}">∠</view>
      <view class="math-symbol" :style="{top: '15%', left: '70%', animationDelay: '2s'}">∏</view>
    </view>

    <!-- 页面标题 -->
    <view class="navbar">
      <text class="navbar-title">学习分析</text>
    </view>

    <!-- 统计概览 -->
    <view class="overview-section">
      <view class="overview-card" v-if="!loading">
        <view class="overview-item">
          <text class="overview-value">{{ learningAnalytics.overall_correct_rate }}%</text>
          <text class="overview-label">总体正确率</text>
        </view>
        <view class="overview-item">
          <text class="overview-value">{{ learningAnalytics.total_question_count }}</text>
          <text class="overview-label">总题目数</text>
        </view>
        <view class="overview-item">
          <text class="overview-value">{{ learningAnalytics.total_correct_count }}</text>
          <text class="overview-label">总正确数</text>
        </view>
      </view>
      <view class="loading-placeholder" v-else>
        <text>加载中...</text>
      </view>
    </view>

    <!-- 图表区域 -->
    <view class="charts-section" v-if="!loading">
      

      <!-- 各年级练题数量 - 柱状图 -->
      <view class="chart-card">
        <view class="chart-header">
          <text class="chart-title">各年级练题数量</text>
        </view>
        <qiun-data-charts
          type="column"
          :opts="gradeColumnOpts"
          :chartData="gradeColumnData"
          style="height: 300px;"
        />
      </view>

      <!-- 错题完成率 - 饼图 -->
      <view class="chart-card">
        <view class="chart-header">
          <text class="chart-title">错题完成率</text>
        </view>
        <qiun-data-charts
          type="pie"
          :opts="pieOpts"
          :chartData="pieData"
          style="height: 300px;"
        />
      </view>

      <!-- 近7日做题总数 - 折线图 -->
      <view class="chart-card">
        <view class="chart-header">
          <text class="chart-title">近7日做题总数</text>
        </view>
        <qiun-data-charts
          type="line"
          :opts="dailyQuestionsOpts"
          :chartData="dailyQuestionsData"
          style="height: 300px;"
        />
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
import qiunDataCharts from '@/uni_modules/qiun-data-charts/components/qiun-data-charts/qiun-data-charts.vue'
import { ref, onMounted } from 'vue'
import ButtomNav from '/components/buttom-nav.vue'
import PracticeSetup from '/components/practice-setup.vue'
import { getStatisticsOverview, getGradeStatistics, getMistakeCompletion, getDailyStatistics } from '@/api/index.js'
import { getUserId } from '@/api/utils.js'

// 练习设置弹窗状态
const showPracticeSetup = ref(false);

// 学习分析数据 - 严格按照learning_analytics表结构
const learningAnalytics = ref({
  id: 0,
  user_id: 0,
  analysis_date: '',
  total_practice_count: 0,           // 总练习次数
  total_question_count: 0,           // 总题目数
  total_correct_count: 0,            // 总正确数
  total_wrong_count: 0,              // 总错误数
  overall_correct_rate: 0,           // 总体正确率
  addition_correct_rate: 0,          // 加法正确率
  subtraction_correct_rate: 0,       // 减法正确率
  multiplication_correct_rate: 0,    // 乘法正确率
  division_correct_rate: 0,          // 除法正确率
  mistake_completion_rate: 0,        // 错题完成率
  average_time_per_question: 0,      // 平均每题用时（秒）
  created_time: '',
  updated_time: ''
})

// 加载状态
const loading = ref(false)

// 用户ID
const userId = ref(null)

// 饼图配置和数据
const pieOpts = ref({
  color: ['#48bb78', '#f56565'], // 绿色表示已完成，红色表示未完成
  enableScroll: false,
  touchMove: false,
  dataLabel: true,
  legend: {
    show: true,
    position: 'right',
    lineHeight: 20
  },
  extra: {
    pie: {
      activeOpacity: 0.5,
      activeRadius: 10,
      offsetAngle: 0,
      labelWidth: 15,
      border: true,
      borderWidth: 3,
      borderColor: '#FFFFFF'
    }
  }
})

const pieData = ref({
  series: [
    {
      data: [
        { name: '已完成', value: 68.5 },
        { name: '未完成', value: 31.5 }
      ]
    }
  ]
})

// 近7日做题总数折线图配置和数据
const dailyQuestionsOpts = ref({
  color: ['#4299e1'],
  padding: [15, 15, 0, 15],
  enableScroll: false,
  legend: {
    show: false
  },
  xAxis: {
    disableGrid: true,
    type: 'category',
    boundaryGap: false,
    axisLine: {
      color: '#cccccc'
    },
    axisTick: {
      show: false
    }
  },
  yAxis: {
    gridType: 'dash',
    splitNumber: 5,
    min: 0,
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  extra: {
    line: {
      type: 'straight',
      width: 3,
      activeType: 'hollow'
    }
  }
})

const dailyQuestionsData = ref({
  categories: [],
  series: [
    {
      name: '做题数量',
      data: []
    }
  ]
})

// 混合运算难度分析柱状图配置和数据
const mixedOperationOpts = ref({
  color: ['#4285F4', '#EA4335', '#FBBC05'],
  padding: [15, 15, 0, 15],
  enableScroll: false,
  xAxis: {
    disableGrid: true,
    itemCount: 5
  },
  yAxis: {
    gridType: 'dash',
    dashLength: 2
  },
  legend: {
    show: true,
    position: 'top',
    lineHeight: 25
  },
  extra: {
    column: {
      type: 'group',
      width: 25,
      seriesGap: 2
    }
  }
})

const mixedOperationData = ref({
  categories: ['简单混合', '带括号', '多步运算', '复杂混合', '应用题型'],
  series: [
    {
      name: '正确率',
      data: [92, 78, 65, 58, 72]
    },
    {
      name: '完成率',
      data: [95, 85, 75, 68, 80]
    },
    {
      name: '平均用时(秒)',
      data: [25, 45, 68, 85, 75]
    }
  ]
})

// 柱状图配置和数据
const columnOpts = ref({
  color: ['#4285F4'],
  padding: [15, 15, 0, 15],
  enableScroll: false,
  xAxis: {
    disableGrid: true,
    itemCount: 7
  },
  yAxis: {
    gridType: 'dash',
    dashLength: 2
  },
  extra: {
    column: {
      type: 'group',
      width: 30
    }
  }
})

const columnData = ref({
  categories: ['加法', '减法', '乘法', '除法'],
  series: [
    {
      name: '正确率',
      data: [88.5, 82.3, 79.8, 76.2]
    }
  ]
})

// 各年级练题数量柱状图配置和数据
const gradeColumnOpts = ref({
  color: ['#4285F4'],
  padding: [15, 15, 0, 15],
  enableScroll: false,
  xAxis: {
    disableGrid: true,
    itemCount: 6
  },
  yAxis: {
    gridType: 'dash',
    dashLength: 2
  },
  extra: {
    column: {
      type: 'group',
      width: 40
    }
  }
})

const gradeColumnData = ref({
  categories: ['1年级', '2年级', '3年级', '4年级', '5年级', '6年级'],
  series: [
    {
      name: '练题数量',
      data: [45, 38, 62, 29, 51, 33]
    }
  ]
})

// 学习能力雷达图配置和数据
const radarOpts = ref({
  color: ['#4285F4', '#34A853'],
  padding: [5, 5, 5, 5],
  enableScroll: false,
  legend: {
    show: true,
    position: 'bottom',
    lineHeight: 25
  },
  extra: {
    radar: {
      gridType: 'radar',
      gridColor: '#CCCCCC',
      gridCount: 5,
      opacity: 0.2,
      max: 100
    }
  }
})

const radarData = ref({
  categories: [
    { name: '计算速度', max: 100 },
    { name: '正确率', max: 100 },
    { name: '专注力', max: 100 },
    { name: '理解力', max: 100 },
    { name: '记忆力', max: 100 },
    { name: '逻辑思维', max: 100 }
  ],
  series: [
    {
      name: '当前水平',
      data: [85, 92, 78, 88, 82, 90]
    },
    {
      name: '目标水平',
      data: [95, 98, 90, 95, 90, 95]
    }
  ]
})

// 加载近7日做题数量数据 - 使用真实API
const loadDailyStatistics = async () => {
  try {
    if (!userId.value) {
      console.warn('用户ID未找到，无法加载近7日统计数据');
      return;
    }

    console.log('开始加载用户近7日统计，用户ID:', userId.value);
    const response = await getDailyStatistics(userId.value);
    
    if (response && response.data) {
      const dailyStats = response.data;
      console.log('近7日统计数据:', dailyStats);
      
      // 更新折线图数据
      const categories = [];
      const data = [];
      
      dailyStats.forEach(stat => {
        categories.push(stat.date);
        data.push(stat.questionCount);
      });
      
      dailyQuestionsData.value = {
        categories: categories,
        series: [
          {
            name: '做题数量',
            data: data
          }
        ]
      };
      
      console.log('近7日图表数据更新成功:', dailyQuestionsData.value);
    } else {
      console.log('没有获取到近7日统计数据，使用默认值');
      // 生成默认的近7日数据（全为0）
      const categories = [];
      const data = [];
      
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        categories.push(`${month}-${day}`);
        data.push(0);
      }
      
      dailyQuestionsData.value = {
        categories: categories,
        series: [
          {
            name: '做题数量',
            data: data
          }
        ]
      };
    }
  } catch (error) {
    console.error('加载近7日统计数据失败:', error);
    uni.showToast({
      title: '近7日数据加载失败',
      icon: 'none'
    });
    // 设置默认值以防止页面崩溃
    const categories = [];
    const data = [];
    
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      categories.push(`${month}-${day}`);
      data.push(0);
    }
    
    dailyQuestionsData.value = {
      categories: categories,
      series: [
        {
          name: '做题数量',
          data: data
        }
      ]
    };
  }
};

// 加载年级统计数据 - 使用真实API
const loadGradeStatistics = async () => {
  try {
    if (!userId.value) {
      console.warn('用户ID未找到，无法加载年级统计数据');
      return;
    }

    console.log('开始加载用户年级统计，用户ID:', userId.value);
    const response = await getGradeStatistics(userId.value);
    
    if (response && response.data) {
      const gradeStats = response.data;
      console.log('年级统计数据:', gradeStats);
      
      // 更新年级柱状图数据
      const categories = [];
      const data = [];
      
      gradeStats.forEach(stat => {
        categories.push(`${stat.grade}年级`);
        data.push(stat.questionCount);
      });
      
      gradeColumnData.value = {
        categories: categories,
        series: [
          {
            name: '练题数量',
            data: data
          }
        ]
      };
      
      console.log('年级图表数据更新成功:', gradeColumnData.value);
    } else {
      console.log('没有获取到年级统计数据，使用默认值');
      // 设置默认值
      gradeColumnData.value = {
        categories: ['1年级', '2年级', '3年级', '4年级', '5年级', '6年级'],
        series: [
          {
            name: '练题数量',
            data: [0, 0, 0, 0, 0, 0]
          }
        ]
      };
    }
  } catch (error) {
    console.error('加载年级统计数据失败:', error);
    uni.showToast({
      title: '年级数据加载失败',
      icon: 'none'
    });
    // 设置默认值以防止页面崩溃
    gradeColumnData.value = {
      categories: ['1年级', '2年级', '3年级', '4年级', '5年级', '6年级'],
      series: [
        {
          name: '练题数量',
          data: [0, 0, 0, 0, 0, 0]
        }
      ]
    };
  }
};

// 加载错题完成率数据 - 使用真实API
const loadMistakeCompletion = async () => {
  try {
    if (!userId.value) {
      console.warn('用户ID未找到，无法加载错题完成率数据');
      return;
    }

    console.log('开始加载用户错题完成率，用户ID:', userId.value);
    const response = await getMistakeCompletion(userId.value);
    
    if (response && response.data) {
      const mistakeData = response.data;
      console.log('错题完成率数据:', mistakeData);
      
      // 更新饼图数据
      const completedRate = mistakeData.completionRate || 0;
      const uncompletedRate = 100 - completedRate;
      
      pieData.value.series[0].data = [
        { name: '已完成', value: completedRate },
        { name: '未完成', value: uncompletedRate }
      ];
      
      // 同时更新learningAnalytics中的错题完成率
      learningAnalytics.value.mistake_completion_rate = completedRate;
      
      console.log('错题完成率图表数据更新成功:', pieData.value);
    } else {
      console.log('没有获取到错题完成率数据，使用默认值');
      // 设置默认值
      pieData.value.series[0].data = [
        { name: '已完成', value: 0 },
        { name: '未完成', value: 100 }
      ];
      learningAnalytics.value.mistake_completion_rate = 0;
    }
  } catch (error) {
    console.error('加载错题完成率数据失败:', error);
    uni.showToast({
      title: '错题数据加载失败',
      icon: 'none'
    });
    // 设置默认值以防止页面崩溃
    pieData.value.series[0].data = [
      { name: '已完成', value: 0 },
      { name: '未完成', value: 100 }
    ];
    learningAnalytics.value.mistake_completion_rate = 0;
  }
};

// 加载统计概览数据 - 使用真实API
const loadStatisticsOverview = async () => {
  try {
    if (!userId.value) {
      console.warn('用户ID未找到，无法加载统计数据');
      return;
    }

    console.log('开始加载用户统计概览，用户ID:', userId.value);
    const response = await getStatisticsOverview(userId.value);
    
    if (response && response.data) {
      // 将API返回的数据映射到learningAnalytics结构
      const overview = response.data;
      learningAnalytics.value = {
        id: 1,
        user_id: userId.value,
        analysis_date: new Date().toISOString().split('T')[0],
        total_practice_count: 0,                                    // 不再使用
        total_question_count: overview.totalQuestionCount || 0,
        total_correct_count: overview.totalCorrectCount || 0,
        total_wrong_count: 0,                                       // 不再使用
        overall_correct_rate: overview.overallCorrectRate || 0,
        addition_correct_rate: 88.5,        // 暂时使用静态数据，后续可扩展
        subtraction_correct_rate: 82.3,     // 暂时使用静态数据，后续可扩展
        multiplication_correct_rate: 79.8,  // 暂时使用静态数据，后续可扩展
        division_correct_rate: 76.2,        // 暂时使用静态数据，后续可扩展
        mistake_completion_rate: 0,          // 由loadMistakeCompletion函数单独加载
        average_time_per_question: 0,       // 不再使用
        created_time: new Date().toISOString(),
        updated_time: new Date().toISOString()
      };
      
      console.log('统计概览数据加载成功:', learningAnalytics.value);
    } else {
      console.log('没有获取到统计数据，使用默认值');
      // 设置默认值
      learningAnalytics.value = {
        id: 0,
        user_id: userId.value,
        analysis_date: new Date().toISOString().split('T')[0],
        total_practice_count: 0,
        total_question_count: 0,
        total_correct_count: 0,
        total_wrong_count: 0,
        overall_correct_rate: 0,
        addition_correct_rate: 0,
        subtraction_correct_rate: 0,
        multiplication_correct_rate: 0,
        division_correct_rate: 0,
        mistake_completion_rate: 0,
        average_time_per_question: 0,
        created_time: new Date().toISOString(),
        updated_time: new Date().toISOString()
      };
    }
    
    // 近7日数据由loadDailyStatistics单独加载
  } catch (error) {
    console.error('加载统计概览数据失败:', error);
    uni.showToast({
      title: '数据加载失败',
      icon: 'none'
    });
    // 设置默认值以防止页面崩溃
    learningAnalytics.value = {
      id: 0,
      user_id: userId.value || 0,
      analysis_date: new Date().toISOString().split('T')[0],
      total_practice_count: 0,
      total_question_count: 0,
      total_correct_count: 0,
      total_wrong_count: 0,
      overall_correct_rate: 0,
      addition_correct_rate: 0,
      subtraction_correct_rate: 0,
      multiplication_correct_rate: 0,
      division_correct_rate: 0,
      mistake_completion_rate: 0,
      average_time_per_question: 0,
      created_time: new Date().toISOString(),
      updated_time: new Date().toISOString()
    };
  }
};

// 更新图表数据
const updateChartData = () => {
  // 更新饼图数据 - 错题完成率
  const completedRate = learningAnalytics.value.mistake_completion_rate || 68.5;
  const uncompletedRate = 100 - completedRate;
  
  pieData.value.series[0].data = [
    { name: '已完成', value: completedRate },
    { name: '未完成', value: uncompletedRate }
  ];
};

// 页面加载完成后初始化
onMounted(async () => {
  loading.value = true;
  try {
    // 获取用户ID
    userId.value = getUserId();
    console.log('获取到用户ID:', userId.value);
    
    if (userId.value) {
      // 并行加载所有统计数据
      await Promise.all([
        loadStatisticsOverview(),
        loadGradeStatistics(),
        loadMistakeCompletion(),
        loadDailyStatistics()
      ]);
    } else {
      console.warn('用户ID未找到，无法加载统计数据');
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
})

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
.analysis-container {
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

.overview-section {
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
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;

  .overview-value {
    font-size: 20px;
    font-weight: bold;
    color: #4285F4;
    margin-bottom: 4px;
  }

  .overview-label {
    font-size: 12px;
    color: #666;
    text-align: center;
  }
}

.charts-section {
  position: relative;
  z-index: 1;
}

.chart-card {
  background-color: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin: 0 15px 20px;
}

.chart-header {
  margin-bottom: 15px;
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
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
</style>
