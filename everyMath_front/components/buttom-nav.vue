<template>
  <view class="bottom-nav">
    <view
      v-for="(item, index) in currentNavList"
      :key="index"
      @click="handleNavClick(item.path, item.text)"
      :class="['nav-item', { 'center-item': item.text === '练习' }]"
    >
      <view
        v-if="item.text === '练习'"
        class="center-button"
        :style="{ backgroundColor: item.selected ? activeColor : '#4299e1' }"
      >
        <uni-icons
          :type="item.selected ? item.selectedIcon : item.icon"
          size="32"
          color="#fff"
        ></uni-icons>
      </view>
      <view v-else class="normal-item">
        <uni-icons
          :type="item.selected ? item.selectedIcon : item.icon"
          :size="22"
          :color="item.selected ? activeColor : defaultColor"
        ></uni-icons>
        <text
          class="nav-text"
          :style="{ color: item.selected ? activeColor : defaultColor }"
        >
          {{ item.text }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 接收父组件参数
const props = defineProps({
  // 导航项配置
  navList: {
    type: Array,
    default: () => [
      {
        text: '首页',
        icon: 'home',
        selectedIcon: 'home-filled',
        path: '/pages/home/home'
      },
      {
        text: '练习',
        icon: 'compose',
        selectedIcon: 'compose',
        path: '/pages/practice/practice'
      },
      {
        text: '我的',
        icon: 'contact',
        selectedIcon: 'contact-filled',
        path: '/pages/profile/profile'
      }
    ]
  },
  // 选中颜色
  activeColor: {
    type: String,
    default: '#4299e1'
  },
  // 默认颜色
  defaultColor: {
    type: String,
    default: '#999'
  }
});

const emit = defineEmits(['show-practice-setup']);

// 状态管理
const currentNavList = ref([...props.navList]);

// 监听路由变化更新选中状态
const updateNavStatus = () => {
  const currentPagePath = getCurrentPagePath();
  currentNavList.value = currentNavList.value.map(item => ({
    ...item,
    selected: currentPagePath.includes(item.path)
  }));
};

// 获取当前页面路径
const getCurrentPagePath = () => {
  const pages = getCurrentPages();
  if (pages.length > 0) {
    // 适配不同平台的路径格式
    const route = pages[pages.length - 1].route || pages[pages.length - 1].__route__ || '';
    // 处理路径，确保能正确匹配
    if (route) {
      return '/' + route;
    }
  }
  return '';
};

// 导航点击事件
const handleNavClick = (path, text) => {
  // 如果点击的是练习按钮，触发显示设置弹窗事件
  if (text === '练习') {
    emit('show-practice-setup');
    return;
  }

  // 页面跳转
  uni.redirectTo({
    url: path
  });
};

// 页面显示时更新导航状态
const onPageShow = () => {
  // 使用 setTimeout 确保在页面完全加载后再更新状态
  setTimeout(() => {
    updateNavStatus();
  }, 50);
};

// 组件挂载时初始化
onMounted(() => {
  updateNavStatus();

  // 监听页面显示事件
  uni.$on('onShow', onPageShow);
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  uni.$off('onShow', onPageShow);
});
</script>

<style lang="scss" scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 55px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15), 0 -2px 8px rgba(0, 0, 0, 0.08);
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom); // 适配全面屏底部安全区域
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 3px;
  padding: 5px 0;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin: 0 4px;

  &:hover {
    background-color: rgba(66, 153, 225, 0.05);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
    background-color: rgba(66, 153, 225, 0.1);
  }

  .nav-text {
    font-size: 12px;
    font-weight: 500;
    transition: color 0.2s ease;
  }
}

.center-item {
  flex: 1.5; // 中间按钮占用更多空间
  display: flex;
  justify-content: center;
  align-items: center;
}

.center-button {
  width: 68px;
  height: 68px;
  border-radius: 50%; // 完全圆形
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  box-shadow: 
    0 6px 20px rgba(66, 153, 225, 0.4),
    0 2px 8px rgba(66, 153, 225, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateY(-22px);
  position: relative;
  
  // 添加光晕效果
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(66, 153, 225, 0.3), rgba(49, 130, 206, 0.3));
    z-index: -1;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    transform: translateY(-25px) scale(1.05);
    box-shadow: 
      0 8px 25px rgba(66, 153, 225, 0.5),
      0 4px 12px rgba(66, 153, 225, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
      
    &::before {
      opacity: 1;
    }
  }

  &:active {
    transform: translateY(-20px) scale(0.95);
    box-shadow: 
      0 4px 15px rgba(66, 153, 225, 0.4),
      0 2px 6px rgba(66, 153, 225, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }
}


.normal-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;

  .nav-text {
    font-size: 12px;
  }
}
</style>
