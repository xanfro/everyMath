# EveryMath API 接口文档

## 概述

本目录包含了EveryMath数学练习应用的前端API接口文件，用于与后端进行数据交互。

**重要更新：**
- 使用uni.request进行网络请求，适配Uni-app环境
- 采用RESTful API规范，URL结构清晰规范
- 所有数字字段使用VARCHAR存储，提高数据灵活性
- 移除用户类型字段，只支持学生角色
- 使用逻辑外键，不使用物理外键
- 新增前端特定API，直接对应页面功能需求
- 统一的错误处理和token管理

## 文件结构

```
api/
├── index.js      # 主要API接口函数（包含配置和接口）
├── utils.js      # API工具函数
└── README.md     # 使用说明文档
```

## 主要文件说明

### 1. index.js - 主要API接口

包含所有与后端交互的API函数，按功能模块分类：

#### 用户相关API
- `registerUser(userData)` - 用户注册
- `loginUser(loginData)` - 用户登录
- `getUserInfo(userId)` - 获取用户信息
- `updateUserInfo(userId, userData)` - 更新用户信息

#### 习题集相关API
- `getQuestionSets(userId)` - 获取用户的习题集列表
- `createQuestionSet(questionSetData)` - 创建习题集
- `getQuestionSetDetail(questionSetId)` - 获取习题集详情
- `deleteQuestionSet(questionSetId)` - 删除习题集

#### 题目相关API
- `getQuestions(questionSetId)` - 获取习题集的题目列表
- `createQuestions(questionsData)` - 批量创建题目
- `getQuestionDetail(questionId)` - 获取题目详情

#### 练习记录相关API
- `createPracticeRecord(practiceData)` - 创建练习记录
- `getPracticeRecords(userId, params)` - 获取用户的练习记录列表
- `getPracticeRecordDetail(recordId)` - 获取练习记录详情
- `updatePracticeRecord(recordId, updateData)` - 更新练习记录

#### 练习详情相关API
- `createPracticeDetails(detailsData)` - 批量创建练习详情
- `getPracticeDetails(practiceRecordId)` - 获取练习的详细答题记录

#### 每日统计相关API
- `getDailyStats(userId, date)` - 获取用户的每日统计
- `getDailyStatsList(userId, params)` - 获取用户的每日统计列表
- `upsertDailyStats(statsData)` - 创建或更新每日统计

#### 错题记录相关API
- `getMistakeRecords(userId, params)` - 获取用户的错题列表
- `createMistakeRecord(mistakeData)` - 创建错题记录
- `updateMistakeRecord(mistakeId, updateData)` - 更新错题记录
- `deleteMistakeRecord(mistakeId)` - 删除错题记录

#### 学习分析相关API
- `getLearningAnalytics(userId, date)` - 获取用户的学习分析数据
- `getLearningAnalyticsList(userId, params)` - 获取用户的学习分析列表
- `upsertLearningAnalytics(analyticsData)` - 创建或更新学习分析数据

#### 文件上传相关API
- `uploadFile(filePath, fileType)` - 上传文件（CSV/Word）

#### 统计相关API
- `getUserStatsOverview(userId)` - 获取用户统计概览
- `getRecentPracticeStats(userId)` - 获取近7日练习情况
- `getPracticeTypeDistribution(userId)` - 获取练习类型分布
- `getOperatorCorrectRate(userId)` - 获取运算符正确率统计

#### 前端特定API
- `getTodayStats(userId, date)` - 获取用户今日统计数据（首页统计板块）
- `getRecentPracticeRecords(userId, limit)` - 获取用户最近练习记录（首页显示）
- `getMistakesForReview(userId, params)` - 获取用户错题列表（错题复习页面）
- `markMistakeAsCorrected(mistakeId)` - 标记错题为已纠正
- `getAnalysisData(userId, date)` - 获取学习分析数据（分析页面）
- `getRecent7DaysStats(userId)` - 获取近7日练习情况（分析页面图表）
- `getPracticeTypeStats(userId)` - 获取练习类型分布（分析页面图表）
- `getOperatorStats(userId)` - 获取运算符正确率统计（分析页面图表）
- `logoutUser()` - 用户退出登录

### 2. utils.js - API工具函数

包含通用的API操作和数据处理函数：

#### Token管理
- `getToken()` - 获取存储的token
- `setToken(token)` - 设置存储的token
- `clearToken()` - 清除存储的token

#### 用户界面提示
- `showError(message, type)` - 显示错误提示
- `showSuccess(message)` - 显示成功提示
- `showLoading(title)` - 显示加载提示
- `hideLoading()` - 隐藏加载提示

#### 数据处理
- `formatDate(date, format)` - 格式化日期
- `formatTime(seconds)` - 格式化时间
- `calculateCorrectRate(correctCount, totalCount)` - 计算正确率

## RESTful API规范

### API设计原则

1. **资源导向**：URL表示资源，HTTP方法表示操作
2. **统一接口**：使用标准HTTP方法（GET、POST、PUT、DELETE）
3. **无状态**：每个请求包含所有必要信息
4. **分层系统**：支持缓存、负载均衡等中间层

### URL结构规范

```
基础URL: http://localhost:8080/api

资源结构:
/users                    # 用户资源
/users/{userId}           # 特定用户
/users/{userId}/question-sets  # 用户的习题集
/question-sets            # 习题集资源
/question-sets/{id}       # 特定习题集
/question-sets/{id}/questions  # 习题集的题目
/practice-records         # 练习记录资源
/users/{userId}/practice-records  # 用户的练习记录
/daily-stats              # 每日统计资源
/users/{userId}/daily-stats/today  # 用户今日统计
```

### HTTP方法使用

- **GET**: 获取资源
- **POST**: 创建资源
- **PUT**: 更新资源
- **DELETE**: 删除资源

### 示例

```javascript
// GET /users/1 - 获取用户信息
const user = await api.getUserInfo(1);

// POST /question-sets - 创建习题集
const questionSet = await api.createQuestionSet(data);

// PUT /question-sets/1 - 更新习题集
const updated = await api.updateQuestionSet(1, data);

// DELETE /question-sets/1 - 删除习题集
await api.deleteQuestionSet(1);

// GET /users/1/question-sets - 获取用户的习题集
const userQuestionSets = await api.getQuestionSets(1);
```

## 后端地址配置

### API基础配置

```javascript
// index.js
// 后端API基础地址
const API_BASE_URL = 'http://localhost:8080/api';
```

### 修改后端地址

如需修改后端地址，只需在 `index.js` 文件中修改 `API_BASE_URL` 即可：

```javascript
// 修改为你的后端地址
const API_BASE_URL = 'http://your-backend-server.com/api';
```

## 使用方法

### 1. 导入API函数

```javascript
// 导入所有API函数
import api from '@/api/index.js';

// 或者按需导入
import { loginUser, getUserInfo } from '@/api/index.js';
```

### 2. 导入工具函数

```javascript
// 导入工具函数
import { showError, showSuccess, formatDate } from '@/api/utils.js';
```

### 3. 使用示例

```javascript
// 用户登录
const handleLogin = async () => {
  try {
    showLoading('登录中...');
    const result = await api.loginUser({
      username: 'testuser',
      password: 'password123'
    });
    
    if (result.success) {
      showSuccess('登录成功');
      // 处理登录成功逻辑
    } else {
      showError(result.message);
    }
  } catch (error) {
    showError('登录失败，请重试');
  } finally {
    hideLoading();
  }
};

// 获取用户信息
const loadUserInfo = async (userId) => {
  try {
    const result = await api.getUserInfo(userId);
    if (result.success) {
      return result.data;
    }
  } catch (error) {
    showError('获取用户信息失败');
  }
};

// 获取练习记录
const loadPracticeRecords = async (userId) => {
  try {
    const result = await api.getPracticeRecords(userId, {
      page: 1,
      pageSize: 20
    });
    if (result.success) {
      return result.data;
    }
  } catch (error) {
    showError('获取练习记录失败');
  }
};
```

## 配置说明

### 1. 修改API基础地址

在 `config.js` 中修改 `API_CONFIG.BASE_URL`：

```javascript
export const API_CONFIG = {
  BASE_URL: 'https://your-api-domain.com/api', // 修改为你的API地址
  // ... 其他配置
};
```

### 2. 修改请求超时时间

```javascript
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080/api',
  TIMEOUT: 15000, // 修改超时时间为15秒
  // ... 其他配置
};
```

### 3. 添加自定义请求头

```javascript
export const REQUEST_HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
  'X-Custom-Header': 'custom-value' // 添加自定义请求头
};
```

## 错误处理

API函数会自动处理常见的错误情况：

1. **网络错误** - 显示网络连接失败提示
2. **超时错误** - 显示请求超时提示
3. **认证错误** - 显示登录过期提示
4. **权限错误** - 显示权限不足提示
5. **服务器错误** - 显示服务器错误提示

## 注意事项

1. **Token管理** - 所有需要认证的请求会自动添加Authorization头
2. **错误处理** - 建议在每个API调用中添加try-catch错误处理
3. **加载状态** - 建议在长时间请求时显示加载提示
4. **网络检查** - 在发起请求前可以检查网络状态
5. **重试机制** - 对于网络请求失败的情况，可以使用重试机制

## 扩展说明

如需添加新的API接口：

1. 在 `index.js` 中添加新的API函数
2. 在 `config.js` 中添加相应的端点配置
3. 在 `utils.js` 中添加必要的工具函数
4. 更新本README文档

## 新增功能示例

### 前端特定API使用示例

```javascript
// 获取今日统计数据（首页统计板块）
const loadTodayStats = async (userId) => {
  try {
    const result = await api.getTodayStats(userId);
    if (result.success) {
      return result.data; // 包含 practice_count, question_count, correct_rate, total_time_spent
    }
  } catch (error) {
    showError('获取今日统计失败');
  }
};

// 获取最近练习记录（首页显示）
const loadRecentRecords = async (userId) => {
  try {
    const result = await api.getRecentPracticeRecords(userId, 10);
    if (result.success) {
      return result.data; // 最近10条练习记录
    }
  } catch (error) {
    showError('获取练习记录失败');
  }
};

// 获取错题列表（错题复习页面）
const loadMistakes = async (userId) => {
  try {
    const result = await api.getMistakesForReview(userId);
    if (result.success) {
      return result.data; // 未纠正的错题列表
    }
  } catch (error) {
    showError('获取错题列表失败');
  }
};

// 标记错题为已纠正
const markCorrected = async (mistakeId) => {
  try {
    const result = await api.markMistakeAsCorrected(mistakeId);
    if (result.success) {
      showSuccess('错题已标记为已纠正');
    }
  } catch (error) {
    showError('标记失败');
  }
};

// 获取学习分析数据（分析页面）
const loadAnalysisData = async (userId) => {
  try {
    const result = await api.getAnalysisData(userId);
    if (result.success) {
      return result.data; // 包含各运算符正确率等分析数据
    }
  } catch (error) {
    showError('获取分析数据失败');
  }
};

// 获取近7日练习情况（分析页面图表）
const loadRecent7Days = async (userId) => {
  try {
    const result = await api.getRecent7DaysStats(userId);
    if (result.success) {
      return result.data; // 近7日的题目数量数据
    }
  } catch (error) {
    showError('获取近7日数据失败');
  }
};
```

### 数据类型转换示例

```javascript
// 创建练习记录时，数字字段会自动转换为字符串
const practiceData = {
  userId: 1,
  questionCount: 10,        // 会自动转换为 '10'
  correctCount: 8,          // 会自动转换为 '8'
  correctRate: 80.0,        // 会自动转换为 '80.0'
  easyCount: 3,             // 会自动转换为 '3'
  mediumCount: 5,           // 会自动转换为 '5'
  hardCount: 2              // 会自动转换为 '2'
};

// 使用时可以转换回数字
const questionCount = parseInt(result.data.question_count);
const correctRate = parseFloat(result.data.correct_rate);
```

## 版本信息

- 版本：2.0.0
- 创建时间：2024-01-15
- 最后更新：2024-12-19
- 更新内容：使用uni.request，VARCHAR存储数字，移除用户类型，新增前端特定API

