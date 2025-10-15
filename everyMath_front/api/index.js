/**
 * EveryMath API 接口文件 - 简化版
 * 只保留用户相关的核心API：登录、注册、更新用户名、更新年级
 */

import { getToken, setToken, clearToken, setUserData, clearUserData, showError, showSuccess } from './utils.js';

// 后端API基础地址
const API_BASE_URL = 'https://qvpsztnvkjxe.sealoshzh.site/api';

// 错误消息配置
const ERROR_MESSAGES = {
  NETWORK_ERROR: '网络连接失败，请检查网络设置',
  TIMEOUT_ERROR: '请求超时，请稍后重试',
  SERVER_ERROR: '服务器错误，请稍后重试',
  UNAUTHORIZED: '登录已过期，请重新登录',
  FORBIDDEN: '没有权限访问该资源',
  NOT_FOUND: '请求的资源不存在',
  VALIDATION_ERROR: '请求参数错误',
  UNKNOWN_ERROR: '未知错误，请稍后重试'
};

/**
 * 通用请求函数
 * @param {string} url - 请求地址
 * @param {object} options - 请求配置
 * @returns {Promise} 请求结果
 */
const request = async (url, options = {}) => {
  try {
    const token = getToken();
    const response = await uni.request({
      url: `${API_BASE_URL}${url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.header
      },
      timeout: 10000,
      ...options
    });

    if (response.statusCode === 200) {
      const { data } = response;
      if (data.code === 0) {
        return data;
      } else {
        throw new Error(data.message || ERROR_MESSAGES.UNKNOWN_ERROR);
      }
    } else {
      const errorMessage = getErrorMessage(response.statusCode);
      throw new Error(errorMessage);
    }
  } catch (error) {
    console.error('API请求失败:', error);
    if (error.errMsg && error.errMsg.includes('timeout')) {
      throw new Error(ERROR_MESSAGES.TIMEOUT_ERROR);
    } else if (error.errMsg && error.errMsg.includes('fail')) {
      throw new Error(ERROR_MESSAGES.NETWORK_ERROR);
    } else {
      throw error;
    }
  }
};

/**
 * 根据状态码获取错误消息
 * @param {number} statusCode - HTTP状态码
 * @returns {string} 错误消息
 */
const getErrorMessage = (statusCode) => {
  switch (statusCode) {
    case 400: return ERROR_MESSAGES.VALIDATION_ERROR;
    case 401: return ERROR_MESSAGES.UNAUTHORIZED;
    case 403: return ERROR_MESSAGES.FORBIDDEN;
    case 404: return ERROR_MESSAGES.NOT_FOUND;
    case 500: return ERROR_MESSAGES.SERVER_ERROR;
    default: return ERROR_MESSAGES.UNKNOWN_ERROR;
  }
};

// ==================== 用户相关API ====================

/**
 * 用户注册
 * @param {object} userData - 用户注册数据
 * @returns {Promise} 注册结果
 */
export const registerUser = (userData) => {
  return request('/users/register', {
    method: 'POST',
    data: userData
  });
};

/**
 * 用户登录
 * @param {object} loginData - 登录数据
 * @returns {Promise} 登录结果
 */
export const loginUser = (loginData) => {
  return request('/users/login', {
    method: 'POST',
    data: loginData
  });
};


/**
 * 更新用户名
 * @param {string} userId - 用户ID
 * @param {string} newUsername - 新用户名
 * @returns {Promise} 更新结果
 */
export const updateUsername = (userId, newUsername) => {
  return request(`/users/username/${userId}`, {
    method: 'PUT',
    data: {
      newUsername: newUsername
    }
  });
};

/**
 * 更新用户年级
 * @param {string} userId - 用户ID
 * @param {number} newGrade - 新年级
 * @returns {Promise} 更新结果
 */
export const updateUserGrade = (userId, newGrade) => {
  return request(`/users/grade/${userId}`, {
    method: 'PUT',
    data: {
      newGrade: newGrade
    }
  });
};


// ==================== 练习记录相关API ====================

/**
 * 保存练习记录
 * @param {object} practiceData - 练习记录数据
 * @returns {Promise} 保存结果
 */
export const savePracticeRecord = (practiceData) => {
  return request('/practice-records/save', {
    method: 'POST',
    data: practiceData
  });
};

/**
 * 查询用户所有练习记录
 * @param {number} userId - 用户ID
 * @returns {Promise} 练习记录列表
 */
export const getPracticeRecords = (userId) => {
  return request(`/practice-records/user/${userId}`, {
    method: 'GET'
  });
};

// ==================== 错题记录相关API ====================

/**
 * 保存错题记录
 * @param {object} mistakeData - 错题记录数据
 * @returns {Promise} 保存结果
 */
export const saveMistakeRecord = (mistakeData) => {
  return request('/mistake-records', {
    method: 'POST',
    data: mistakeData
  });
};


/**
 * 查询用户所有错题记录
 * @param {number} userId - 用户ID
 * @returns {Promise} 错题记录列表
 */
export const getMistakeRecords = (userId) => {
  return request(`/mistake-records/user/${userId}`, {
    method: 'GET'
  });
};

/**
 * 更新错题纠正状态
 * @param {number} id - 错题记录ID
 * @param {string} corrected - 纠正状态（0-未纠正，1-已纠正）
 * @returns {Promise} 更新结果
 */
export const updateMistakeCorrectedStatus = (id, corrected) => {
  return request(`/mistake-records/${id}/corrected?corrected=${corrected}`, {
    method: 'PUT'
  });
};

/**
 * 删除错题记录
 * @param {number} id - 错题记录ID
 * @returns {Promise} 删除结果
 */
export const deleteMistakeRecord = (id) => {
  return request(`/mistake-records/${id}`, {
    method: 'DELETE'
  });
};

// ==================== 统计数据相关API ====================

/**
 * 获取用户统计概览数据
 * @param {number} userId - 用户ID
 * @returns {Promise} 统计概览数据
 */
export const getStatisticsOverview = (userId) => {
  return request(`/statistics/overview/${userId}`, {
    method: 'GET'
  });
};

/**
 * 获取用户各年级练题数量统计
 * @param {number} userId - 用户ID
 * @returns {Promise} 各年级练题数量统计
 */
export const getGradeStatistics = (userId) => {
  return request(`/statistics/grade/${userId}`, {
    method: 'GET'
  });
};

/**
 * 获取用户错题完成率统计
 * @param {number} userId - 用户ID
 * @returns {Promise} 错题完成率统计
 */
export const getMistakeCompletion = (userId) => {
  return request(`/statistics/mistake-completion/${userId}`, {
    method: 'GET'
  });
};

/**
 * 获取用户近7日做题数量统计
 * @param {number} userId - 用户ID
 * @returns {Promise} 近7日每日做题数量统计
 */
export const getDailyStatistics = (userId) => {
  return request(`/statistics/daily/${userId}`, {
    method: 'GET'
  });
};

// ==================== 习题集相关接口 ====================

/**
 * 获取用户的习题集列表
 * @param {number} userId - 用户ID
 * @returns {Promise} 习题集列表
 */
export const getQuestionSets = (userId) => {
  return request(`/question-sets/user/${userId}`, {
    method: 'GET'
  });
};

/**
 * 删除习题集
 * @param {number} id - 习题集ID
 * @returns {Promise} 删除结果
 */
export const deleteQuestionSet = (id) => {
  return request(`/question-sets/${id}`, {
    method: 'DELETE'
  });
};

/**
 * 获取习题集详情
 * @param {number} id - 习题集ID
 * @returns {Promise} 习题集详情
 */
export const getQuestionSetDetails = (id) => {
  return request(`/question-sets/${id}/details`, {
    method: 'GET'
  });
};

/**
 * 根据习题集ID获取题目列表
 * @param {number} questionSetId - 习题集ID
 * @returns {Promise} 题目列表
 */
export const getQuestionsByQuestionSetId = (questionSetId) => {
  return request(`/questions/question-set/${questionSetId}`, {
    method: 'GET'
  });
};


/**
 * 上传文件（通用文件上传接口）
 * @param {string} filePath - 文件路径
 * @param {number} userId - 用户ID
 * @returns {Promise} 上传结果
 */
export const uploadFile = (filePath, userId) => {
	console.log("uploadFile接口发生调用",filePath, userId);
	
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${API_BASE_URL}/question-sets/upload-file`,
      filePath: filePath,
      name: 'file',
      formData: {
        userId: userId
      },
      header: {
        'Authorization': `Bearer ${getToken()}`
      },
      success: (uploadFileRes) => {
        try {
          const data = JSON.parse(uploadFileRes.data);
          if (data.code === 0) {
            resolve(data);
          } else {
            reject(new Error(data.message || '上传失败'));
          }
        } catch (error) {
          reject(new Error('响应解析失败'));
        }
      },
      fail: (error) => {
        reject(new Error('文件上传失败'));
      }
    });
  });
};

/**
 * Base64文件上传接口 - 专为移动端Content URI问题设计
 * @param {string} base64Content - Base64编码的文件内容
 * @param {string} fileName - 文件名
 * @param {number} userId - 用户ID
 * @returns {Promise} 上传结果
 */
export const uploadBase64File = (base64Content, fileName, userId) => {
  console.log("uploadBase64File接口调用", {fileName, userId, contentLength: base64Content?.length});
  
  return request('/question-sets/upload-base64', {
    method: 'POST',
    data: {
      fileContent: base64Content,
      fileName: fileName,
      userId: userId
    }
  });
};

// 简化的App端文件上传函数 - 使用Base64方式
export const uploadFileInApp = (filePath, userId) => {
  console.log("=== App端简化文件上传开始 ===");
  console.log("uploadFileInApp接口调用参数:", {filePath, userId});
	
  return new Promise((resolve, reject) => {

    // 检查是否为Content URI
    if (filePath.startsWith('content://')) {
      console.log("App端检测到Content URI，使用Base64上传方式:", filePath);
      
      try {
        // 使用Android原生API读取文件并转换为Base64
        const main = plus.android.runtimeMainActivity();
        const Uri = plus.android.importClass("android.net.Uri");
        
        // 解析URI
        const uri = Uri.parse(filePath);
        console.log("App端URI解析成功");
        
        // 获取ContentResolver
        const contentResolver = main.getContentResolver();
        console.log("App端获取ContentResolver成功");
        
        // 打开输入流
        const inputStream = plus.android.invoke(contentResolver, "openInputStream", uri);
        if (!inputStream) {
          reject(new Error('无法打开文件输入流'));
          return;
        }
        console.log("App端文件输入流打开成功");
        
        // 读取文件内容
        const ByteArrayOutputStream = plus.android.importClass("java.io.ByteArrayOutputStream");
        const buffer = plus.android.newObject("byte[]", 1024);
        const byteArrayOutputStream = new ByteArrayOutputStream();
        let len = 0;
        
        while ((len = plus.android.invoke(inputStream, "read", buffer)) != -1) {
          plus.android.invoke(byteArrayOutputStream, "write", buffer, 0, len);
        }
        
        plus.android.invoke(inputStream, "close");
        const fileBytes = plus.android.invoke(byteArrayOutputStream, "toByteArray");
        plus.android.invoke(byteArrayOutputStream, "close");
        
        console.log("App端文件读取成功，字节长度:", fileBytes.length);
        
        // 转换为Base64
        const Base64 = plus.android.importClass("android.util.Base64");
        const base64String = plus.android.invoke(Base64, "encodeToString", fileBytes, 0);
        
        console.log("App端Base64转换成功，长度:", base64String.length);
        
        // 提取文件名
        let fileName = 'upload.csv';
        try {
          const cursor = plus.android.invoke(contentResolver, "query", uri, null, null, null, null);
          if (cursor && plus.android.invoke(cursor, "moveToFirst")) {
            const nameIndex = plus.android.invoke(cursor, "getColumnIndex", "_display_name");
            if (nameIndex !== -1) {
              fileName = plus.android.invoke(cursor, "getString", nameIndex);
            }
            plus.android.invoke(cursor, "close");
          }
        } catch (error) {
          console.warn("App端获取文件名失败，使用默认名称:", error.message);
        }
        
        console.log("App端文件名:", fileName);
        console.log("App端开始Base64上传");
        
        // 使用Base64上传接口
        uploadBase64File(base64String, fileName, userId)
          .then(resolve)
          .catch(reject);
        
      } catch (error) {
        console.error('App端Content URI处理失败:', error);
        reject(new Error('Content URI处理失败: ' + error.message));
      }
    } else {
      // 对于非Content URI，使用原来的uni.uploadFile方法
      console.log("App端普通文件路径，使用标准上传:", filePath);
      uploadFile(filePath, userId)
        .then(resolve)
        .catch(reject);
    }
  });
};


// 默认导出所有API函数
export default {
  registerUser,
  loginUser,
  updateUsername,
  updateUserGrade,
  savePracticeRecord,
  getPracticeRecords,
  saveMistakeRecord,
  getMistakeRecords,
  updateMistakeCorrectedStatus,
  deleteMistakeRecord,
  getStatisticsOverview,
  getGradeStatistics,
  getMistakeCompletion,
  getDailyStatistics,
  getQuestionSets,
  deleteQuestionSet,
  getQuestionSetDetails,
  getQuestionsByQuestionSetId,
  uploadFile,
  uploadBase64File,
  uploadFileInApp
};