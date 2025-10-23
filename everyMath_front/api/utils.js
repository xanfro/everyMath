/**
 * API 工具函数
 * 用于处理通用的API操作和数据处理
 */

/**
 * 获取存储的token
 * @returns {string} token
 */
export const getToken = () => {
  return uni.getStorageSync('token') || '';
};

/**
 * 设置存储的token
 * @param {string} token - token值
 */
export const setToken = (token) => {
  uni.setStorageSync('token', token);
};

/**
 * 清除存储的token
 */
export const clearToken = () => {
  uni.removeStorageSync('token');
};

/**
 * 获取当前用户信息
 * @returns {object} 用户信息
 */
export const getUserData = () => {
  try {
    const userStr = uni.getStorageSync('userData');
    return userStr ? JSON.parse(userStr) : null;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    return null;
  }
};

/**
 * 设置用户信息
 * @param {object} userData - 用户数据
 */
export const setUserData = (userData) => {
  try {
    uni.setStorageSync('userData', JSON.stringify(userData));
  } catch (error) {
    console.error('保存用户信息失败:', error);
  }
};

/**
 * 清除用户信息
 */
export const clearUserData = () => {
  uni.removeStorageSync('userData');
};

/**
 * 获取当前用户ID
 * @returns {number} 用户ID
 */
export const getUserId = () => {
  const userData = getUserData();
  return userData ? userData.id : null;
};


/**
 * 显示错误提示
 * @param {string} message - 错误消息
 * @param {string} type - 提示类型
 */
export const showError = (message, type = 'none') => {
  uni.showToast({
    title: message,
    icon: type,
    duration: 3000
  });
};

/**
 * 显示成功提示
 * @param {string} message - 成功消息
 */
export const showSuccess = (message) => {
  uni.showToast({
    title: message,
    icon: 'success',
    duration: 2000
  });
};

/**
 * 显示加载提示
 * @param {string} title - 加载提示文本
 */
export const showLoading = (title = '加载中...') => {
  uni.showLoading({
    title: title,
    mask: true
  });
};

/**
 * 隐藏加载提示
 */
export const hideLoading = () => {
  uni.hideLoading();
};

/**
 * 格式化日期
 * @param {Date|string} date - 日期
 * @param {string} format - 格式
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date, format = 'YYYY-MM-DD') => {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
};

/**
 * 格式化时间（秒转分:秒）
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的时间字符串
 */
export const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

/**
 * 计算正确率
 * @param {number} correctCount - 正确数量
 * @param {number} totalCount - 总数量
 * @returns {number} 正确率（百分比）
 */
export const calculateCorrectRate = (correctCount, totalCount) => {
  if (totalCount === 0) return 0;
  return Math.round((correctCount / totalCount) * 100 * 100) / 100; // 保留两位小数
};



export default {
  getToken,
  setToken,
  clearToken,
  getUserData,
  setUserData,
  clearUserData,
  getUserId,
  showError,
  showSuccess,
  showLoading,
  hideLoading,
  formatDate,
  formatTime,
  calculateCorrectRate
};

