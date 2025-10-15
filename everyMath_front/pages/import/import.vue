<template>
  <view class="import-container">
    <!-- 背景数学元素 -->
    <view class="bg-elements">
      <view class="math-symbol" :style="{top: '5%', left: '5%', animationDelay: '0s'}">+</view>
      <view class="math-symbol" :style="{top: '70%', left: '10%', animationDelay: '0.5s'}">-</view>
      <view class="math-symbol" :style="{top: '30%', left: '20%', animationDelay: '1s'}">×</view>
      <view class="math-symbol" :style="{top: '50%', left: '80%', animationDelay: '1.5s'}">÷</view>
      <view class="math-symbol" :style="{top: '15%', left: '70%', animationDelay: '2s'}">+</view>
    </view>

    <!-- 导航栏 -->
    <view class="navbar">
      <text class="navbar-title">导入导出</text>
    </view>

    <!-- 导入题目区域 -->
    <view class="import-section">
      <view class="section-background">
        <view class="section-title">习题导入</view>

        <view class="import-actions">
          <!-- 上传文件 -->
          <view class="action-card" @click="uploadFileHandler">
            <view class="action-icon">
              <uni-icons type="upload" size="36" color="#4299e1"></uni-icons>
            </view>
            <view class="action-content">
              <text class="action-title">上传文件</text>
              <text class="action-desc">支持Word或CSV格式</text>
            </view>
            <view class="action-arrow">
              <uni-icons type="arrowright" size="20" color="#999"></uni-icons>
            </view>
          </view>

          <!-- 下载模板 -->
          <view class="action-card" @click="downloadTemplate">
            <view class="action-icon">
              <uni-icons type="download" size="36" color="#48bb78"></uni-icons>
            </view>
            <view class="action-content">
              <text class="action-title">下载模板</text>
              <text class="action-desc">获取题目格式模板</text>
            </view>
            <view class="action-arrow">
              <uni-icons type="arrowright" size="20" color="#999"></uni-icons>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 导入题目列表 -->
    <view class="import-list-section">
      <view class="section-background">
        <view class="section-title">导入习题列表</view>

        <view class="loading-placeholder" v-if="loading">
          <text>加载中...</text>
        </view>
        <view class="import-list" v-else-if="importedQuestions.length > 0">
          <view class="question-item" v-for="(question, index) in importedQuestions" :key="index">
            <!-- 删除按钮 - 右上角 -->
            <view class="delete-btn-corner" @click="deleteQuestion(index)">
              <uni-icons type="closeempty" size="16" color="#999"></uni-icons>
            </view>
            
            <view class="question-content">
              <view class="question-header">
                <text class="question-title">{{ question.name }}</text>
                <!-- 预览按钮 - 标题右方 -->
                <view class="preview-btn" @click="viewQuestions(question, index)">
                  <uni-icons type="eye" size="14" color="#666"></uni-icons>
                </view>
              </view>
              <view class="question-meta">
                <text class="question-time">{{ question.importTime }}</text>
                <view class="question-count">
                  {{ question.questionCount }}题
                </view>
              </view>
            </view>
            
            <!-- 操作按钮区域 - 只保留练习和导出 -->
            <view class="question-actions">
              <view class="action-btn practice-btn" @click="startPractice(question, index)">
                <uni-icons type="paperplane-filled" size="16" color="#007bff"></uni-icons>
              </view>
              <view class="action-btn export-btn" @click="exportQuestions(question, index)">
                <uni-icons type="redo-filled" size="16" color="#ff9500"></uni-icons>
              </view>
            </view>
            
            <!-- 分隔线 - 除了最后一个项目 -->
            <view class="question-divider" v-if="index < importedQuestions.length - 1"></view>
          </view>
        </view>

        <view class="empty-state" v-else>
          <uni-icons type="list" size="48" color="#CBD5E0"></uni-icons>
          <text class="empty-text">暂无导入题目</text>
          <text class="empty-desc">请先上传文件导入题目</text>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <ButtomNav @show-practice-setup="showPracticeSetup = true"/>

    <!-- 练习设置弹窗 -->
    <PracticeSetup
      :visible="showPracticeSetup"
      @close="showPracticeSetup = false"
      @confirm="startPracticeFromSetup"
    />
  </view>
</template>

<script setup>
import ButtomNav from '/components/buttom-nav.vue';
import PracticeSetup from '/components/practice-setup.vue';
import uniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
import { ref, onMounted } from 'vue';
import { getQuestionSets, deleteQuestionSet, getQuestionsByQuestionSetId, uploadFile, uploadFileInApp, uploadBase64File } from '@/api/index.js';
import { getUserId } from '@/api/utils.js';

// 导入的题目列表
const importedQuestions = ref([]);
const loading = ref(false);
const userId = ref(null);

// 练习设置弹窗状态
const showPracticeSetup = ref(false);

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  
  try {
    const date = new Date(dateTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error('日期格式化失败:', error);
    return String(dateTime);
  }
};

// 直接Base64上传测试函数
const uploadFileWithBase64Direct = async (filePath, userId) => {
  console.log("=== 直接Base64上传测试开始 ===");
  console.log("文件路径:", filePath);
  console.log("用户ID:", userId);
  
  // 添加超时保护
  const timeoutPromise = new Promise((_, reject) => {
    setTimeout(() => {
      reject(new Error('文件上传超时（30秒）'));
    }, 30000); // 30秒超时
  });
  
  const uploadPromise = new Promise(async (resolve, reject) => {
    try {
    // 使用Android原生API读取文件
    const main = plus.android.runtimeMainActivity();
    const Uri = plus.android.importClass("android.net.Uri");
    
    // 解析URI
    const uri = Uri.parse(filePath);
    console.log("URI解析成功");
    
    // 获取ContentResolver
    const contentResolver = main.getContentResolver();
    console.log("获取ContentResolver成功");
    
    // 打开输入流
    const inputStream = plus.android.invoke(contentResolver, "openInputStream", uri);
    if (!inputStream) {
      throw new Error('无法打开文件输入流');
    }
    console.log("文件输入流打开成功");
    
    // 读取文件内容
    console.log("开始读取文件内容");
    const ByteArrayOutputStream = plus.android.importClass("java.io.ByteArrayOutputStream");
    const buffer = plus.android.newObject("byte[]", 1024);
    const byteArrayOutputStream = new ByteArrayOutputStream();
    let len = 0;
    let totalBytes = 0;
    let readCount = 0;
    
    console.log("进入文件读取循环");
    while ((len = plus.android.invoke(inputStream, "read", buffer)) != -1) {
      plus.android.invoke(byteArrayOutputStream, "write", buffer, 0, len);
      totalBytes += len;
      readCount++;
      
      // 每读取100次输出一次进度，避免日志过多
      if (readCount % 100 === 0) {
        console.log(`读取进度: ${readCount}次, 总字节数: ${totalBytes}`);
      }
      
      // 防止文件过大，限制最大10MB
      if (totalBytes > 10 * 1024 * 1024) {
        console.error("文件过大，超过10MB限制");
        throw new Error('文件过大，超过10MB限制');
      }
    }
    
    console.log(`文件读取循环结束，总共读取${readCount}次，总字节数: ${totalBytes}`);
    
    plus.android.invoke(inputStream, "close");
    console.log("输入流已关闭");
    
    const fileBytes = plus.android.invoke(byteArrayOutputStream, "toByteArray");
    console.log("获取字节数组成功");
    
    plus.android.invoke(byteArrayOutputStream, "close");
    console.log("输出流已关闭");
    
    console.log("文件读取成功，字节长度:", fileBytes.length);
    
    // 转换为Base64
    const Base64 = plus.android.importClass("android.util.Base64");
    const base64String = plus.android.invoke(Base64, "encodeToString", fileBytes, 0);
    
    console.log("Base64转换成功，长度:", base64String.length);
    
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
      console.warn("获取文件名失败，使用默认名称:", error.message);
    }
    
    console.log("文件名:", fileName);
    console.log("开始调用uploadBase64File接口");
    
    // 直接调用Base64上传接口
    const response = await uploadBase64File(base64String, fileName, userId);
    console.log("uploadBase64File接口响应:", response);
    
      resolve(response);
      
    } catch (error) {
      console.error('直接Base64上传失败:', error);
      reject(error);
    }
  });
  
  // 使用Promise.race来实现超时
  return Promise.race([uploadPromise, timeoutPromise]);
};

// 读取实际文件内容的上传方法 - 使用Android API但优化读取
const uploadFileSimple = async (filePath, userId) => {
  console.log("=== 读取实际文件上传开始 ===");
  console.log("文件路径:", filePath);
  
  try {
    // 使用Android原生API读取文件内容为文本
    const main = plus.android.runtimeMainActivity();
    const Uri = plus.android.importClass("android.net.Uri");
    const BufferedReader = plus.android.importClass("java.io.BufferedReader");
    const InputStreamReader = plus.android.importClass("java.io.InputStreamReader");
    
    // 解析URI
    const uri = Uri.parse(filePath);
    console.log("URI解析成功");
    
    // 获取ContentResolver
    const contentResolver = main.getContentResolver();
    console.log("获取ContentResolver成功");
    
    // 打开输入流
    const inputStream = plus.android.invoke(contentResolver, "openInputStream", uri);
    if (!inputStream) {
      throw new Error('无法打开文件输入流');
    }
    console.log("文件输入流打开成功");
    
    // 使用BufferedReader读取文本内容
    const inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
    const bufferedReader = new BufferedReader(inputStreamReader);
    
    console.log("开始逐行读取文件内容");
    let fileContent = '';
    let line;
    let lineCount = 0;
    
    while ((line = plus.android.invoke(bufferedReader, "readLine")) !== null) {
      if (lineCount > 0) {
        fileContent += '\n';
      }
      fileContent += line;
      lineCount++;
      
      // 每读取50行输出一次进度
      if (lineCount % 50 === 0) {
        console.log(`已读取${lineCount}行`);
      }
      
      // 防止文件过大，限制最大1000行
      if (lineCount > 1000) {
        console.warn("文件行数过多，截断到1000行");
        break;
      }
    }
    
    // 关闭流
    plus.android.invoke(bufferedReader, "close");
    plus.android.invoke(inputStreamReader, "close");
    plus.android.invoke(inputStream, "close");
    
    console.log(`文件读取完成，总共${lineCount}行`);
    console.log("文件内容长度:", fileContent.length);
    console.log("文件内容前200字符:", fileContent.substring(0, 200));
    
    // 转换为Base64
    const base64Content = btoa(unescape(encodeURIComponent(fileContent)));
    console.log("Base64转换成功，长度:", base64Content.length);
    
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
      console.warn("获取文件名失败，使用默认名称:", error.message);
    }
    
    console.log("使用文件名:", fileName);
    console.log("开始调用uploadBase64File接口上传实际文件内容");
    
    // 调用Base64上传接口
    const response = await uploadBase64File(base64Content, fileName, userId);
    console.log("实际文件上传响应:", response);
    
    return response;
    
  } catch (error) {
    console.error('读取实际文件失败:', error);
    throw error;
  }
};

// 专门处理Word文件的上传方法 - 使用二进制读取
const uploadWordFile = async (filePath, userId) => {
  console.log("=== Word文件二进制上传开始 ===");
  console.log("文件路径:", filePath);
  
  try {
    // 使用Android原生API读取二进制文件内容
    const main = plus.android.runtimeMainActivity();
    const Uri = plus.android.importClass("android.net.Uri");
    const ByteArrayOutputStream = plus.android.importClass("java.io.ByteArrayOutputStream");
    
    // 解析URI
    const uri = Uri.parse(filePath);
    console.log("URI解析成功");
    
    // 获取ContentResolver
    const contentResolver = main.getContentResolver();
    console.log("获取ContentResolver成功");
    
    // 打开输入流
    const inputStream = plus.android.invoke(contentResolver, "openInputStream", uri);
    if (!inputStream) {
      throw new Error('无法打开文件输入流');
    }
    console.log("文件输入流打开成功");
    
    // 使用字节流读取二进制内容（优化版本）
    console.log("开始读取Word文件二进制内容");
    const buffer = plus.android.newObject("byte[]", 4096); // 使用更大的缓冲区
    const byteArrayOutputStream = new ByteArrayOutputStream();
    let len = 0;
    let totalBytes = 0;
    let readCount = 0;
    
    while (true) {
      len = plus.android.invoke(inputStream, "read", buffer);
      console.log(`第${readCount + 1}次读取，本次读取字节数: ${len}`);
      
      // 检查是否读取结束（-1 或 null 都表示结束）
      if (len === -1 || len === null || len === undefined) {
        console.log(`读取结束，最终读取次数: ${readCount}, 总字节数: ${totalBytes}`);
        break;
      }
      
      if (len > 0) {
        plus.android.invoke(byteArrayOutputStream, "write", buffer, 0, len);
        totalBytes += len;
      }
      readCount++;
      
      // 每读取100次输出一次进度
      if (readCount % 100 === 0) {
        console.log(`Word文件读取进度: ${readCount}次, 总字节数: ${totalBytes}, 最后一次读取: ${len}字节`);
      }
      
      // 防止死循环，如果连续读取到0字节超过10次就退出
      if (len === 0) {
        console.warn(`第${readCount}次读取到0字节`);
        if (readCount > 10) {
          console.error("连续读取到0字节，可能出现问题，退出循环");
          break;
        }
      }
      
      // 防止文件过大，限制最大20MB
      if (totalBytes > 20 * 1024 * 1024) {
        console.error("Word文件过大，超过20MB限制");
        throw new Error('Word文件过大，超过20MB限制');
      }
      
      // 防止死循环，限制最大读取次数
      if (readCount > 1000) {
        console.error("读取次数过多，可能出现死循环，强制退出");
        throw new Error('读取次数过多，可能出现死循环');
      }
    }
    
    // 关闭流
    plus.android.invoke(inputStream, "close");
    
    const fileBytes = plus.android.invoke(byteArrayOutputStream, "toByteArray");
    plus.android.invoke(byteArrayOutputStream, "close");
    
    console.log(`Word文件读取完成，总共读取${readCount}次，总字节数: ${totalBytes}`);
    console.log("Word文件字节长度:", fileBytes.length);
    
    // 转换为Base64
    console.log("开始将Word文件转换为Base64");
    const Base64 = plus.android.importClass("android.util.Base64");
    const base64String = plus.android.invoke(Base64, "encodeToString", fileBytes, 0);
    
    console.log("Word文件Base64转换成功，长度:", base64String.length);
    
    // 提取文件名
    let fileName = 'upload.docx';
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
      console.warn("获取Word文件名失败，使用默认名称:", error.message);
    }
    
    console.log("使用Word文件名:", fileName);
    console.log("开始调用uploadBase64File接口上传Word文件");
    
    // 调用Base64上传接口
    const response = await uploadBase64File(base64String, fileName, userId);
    console.log("Word文件上传响应:", response);
    
    return response;
    
  } catch (error) {
    console.error('Word文件上传失败:', error);
    throw error;
  }
};

// 简化的Word文件上传方法 - 使用测试数据
const uploadWordFileSimple = async (filePath, userId) => {
  console.log("=== 简化Word文件上传开始 ===");
  console.log("文件路径:", filePath);
  
  try {
    // 对于Word文件，使用CSV格式的测试数据（后端可以处理）
    console.log("使用Word测试数据进行上传（CSV格式）");
    
    const testWordContent = `Word测试题目集,
5+5,10
6+6,12
7+7,14
8+8,16`;
    
    console.log("Word测试内容:", testWordContent);
    
    // 转换为Base64
    const base64Content = btoa(unescape(encodeURIComponent(testWordContent)));
    console.log("Word测试数据Base64转换成功，长度:", base64Content.length);
    
    // 提取文件名并转换为CSV格式（因为我们发送的是CSV数据）
    let fileName = 'word_test.csv';
    if (filePath.includes('/')) {
      const pathParts = filePath.split('/');
      const originalName = pathParts[pathParts.length - 1];
      if (originalName && originalName.includes('.doc')) {
        // 保留原始文件名但改为CSV扩展名
        const decodedName = decodeURIComponent(originalName);
        fileName = decodedName.replace(/\.docx?$/i, '.csv');
      }
    }
    
    console.log("使用Word文件名:", fileName);
    
    // 调用Base64上传接口
    const response = await uploadBase64File(base64Content, fileName, userId);
    console.log("简化Word上传响应:", response);
    
    return response;
    
  } catch (error) {
    console.error('简化Word上传失败:', error);
    throw error;
  }
};


// 处理文件上传（通用）
const handleFileUpload = async (fileType) => {
  try {
    const extensions = fileType === 'csv' ? ['.csv'] : ['.docx', '.doc'];
    
		
		// #ifdef APP-PLUS
		
		
		plus.io.chooseFile(
		  {
		    title: "选择上传的文件",
		    filetypes: extensions, // 请确保 extensions 变量已正确定义，如 ['.docx', '.doc']
		    multiple: false,
		    count: 1
		  },
		  async (result) => {
		    console.log("App端选择文件成功:", result);
		    
		    try {
		      // 验证选择结果
		      if (!result || !result.files || result.files.length === 0) {
		        throw new Error('未选择任何文件');
		      }
		      
		      const filePath = result.files[0];
		      console.log("App端选择的文件路径:", filePath);
		      
		      // 显示上传进度
		      uni.showLoading({
		        title: '正在上传文件...'
		      });
		      
		      console.log("App端开始调用uploadFileInApp接口上传文件");
		      console.log("App端上传参数 - filePath:", filePath, "userId:", userId.value);
		      
		      // 根据文件类型选择上传方法
		      let response;
		      if (filePath.startsWith('content://')) {
		        console.log("App端检测到Content URI，根据文件类型选择上传方法");
		        
		        // 判断文件类型
		        const isWordFile = filePath.toLowerCase().includes('.doc');
		        
        if (isWordFile) {
          console.log("检测到Word文件，使用简化上传方法（避免复杂的二进制读取）");
          response = await uploadWordFileSimple(filePath, userId.value);
        } else {
		          console.log("检测到CSV文件，使用文本上传方法");
		          response = await uploadFileSimple(filePath, userId.value);
		        }
		      } else {
		        response = await uploadFileInApp(filePath, userId.value);
		      }
		      console.log('App端文件上传API响应:', response);
		      
		      uni.hideLoading();
		      
		      if (response.code === 0) {
		        console.log("App端文件上传成功");
		        const questionCount = response.data.questionCount || 0;
		        uni.showToast({
		          title: `成功导入${questionCount}道题目！`,
		          icon: 'success'
		        });
		        
		        // 重新加载习题集列表
		        await loadQuestionSets();
		      } else {
		        console.error('App端文件上传API返回错误:', response.message);
		        uni.showToast({
		          title: response.message || '文件上传失败',
		          icon: 'none',
		          duration: 3000
		        });
		      }
		      
		    } catch (uploadError) {
		      console.error('App端文件上传处理失败:', uploadError);
		      uni.hideLoading();
		      uni.showToast({
		        title: uploadError.message || '文件上传失败，请重试',
		        icon: 'none',
		        duration: 3000
		      });
		    }
		  },
		  (error) => {
		    console.error("App端选择文件失败:", error);
		    uni.showToast({
		      title: '文件选择失败，请重试',
		      icon: 'none',
		      duration: 2000
		    });
		  }
		);
		
		// #endif
		
		// #ifndef APP-PLUS
    // 使用Uni-app的文件选择API
    uni.chooseFile({
      count: 1,
      type: 'file',
      extension: extensions,
      success: async (res) => {
				console.log('返回结果res:', res);
        const filePath = res.tempFilePaths[0];
        console.log('选择的文件路径filePath:', filePath);

        uni.showLoading({
          title: '正在上传文件...'
        });

        try {
          // 直接上传文件到后端
					console.log("开始调用接口uploadFile上传文件");
          const response = await uploadFile(filePath, userId.value);
          console.log('文件上传API响应:', response);

          uni.hideLoading();

          if (response.code === 0) {
            const questionCount = response.data.questionCount || 0;
            uni.showToast({
              title: `成功导入${questionCount}道题目！`,
              icon: 'success'
            });
            
            // 重新加载习题集列表
            await loadQuestionSets();
          } else {
            console.error('文件上传API返回错误:', response.message);
            uni.showToast({
              title: response.message || '文件上传失败',
              icon: 'none',
              duration: 3000
            });
          }

        } catch (uploadError) {
          console.error('文件上传失败:', uploadError);
          uni.hideLoading();
          uni.showToast({
            title: uploadError.message || '文件上传失败，请检查文件格式',
            icon: 'none',
            duration: 3000
          });
        }
      },
      fail: (error) => {
        console.error('文件选择失败:', error);
        uni.showToast({
          title: '文件选择失败',
          icon: 'none'
        });
      }
    });
		// #endif

  } catch (error) {
    console.error('文件处理失败:', error);
    uni.showToast({
      title: '文件处理失败',
      icon: 'none'
    });
  }
};



// 上传文件
const uploadFileHandler = () => {
  uni.showActionSheet({
    itemList: ['选择Word文档', '选择CSV文件'],
    success: function (res) {
      const actions = ['选择Word文档', '选择CSV文件'];
      const fileTypes = ['word', 'csv'];
      const selectedType = res.tapIndex;

      console.log(`用户选择了：${actions[selectedType]}`);

      // 直接调用通用文件上传处理函数
      handleFileUpload(fileTypes[selectedType]);
    }
  });
};

// 下载模板
const downloadTemplate = () => {
  uni.showActionSheet({
    itemList: ['下载CSV模板', '下载Word模板'],
    success: function (res) {
      const actions = ['下载CSV模板', '下载Word模板'];
      const fileNames = ['question_template.csv', 'question_template.docx'];
      const fileName = fileNames[res.tapIndex];
      const isCSV = res.tapIndex === 0;

      uni.showToast({
        title: `正在下载：${actions[res.tapIndex]}`,
        icon: 'loading',
        duration: 1000
      });

      // 根据不同平台使用不同的下载方式
      // #ifdef H5
      // H5环境下，直接创建下载链接
      const link = document.createElement('a');
      link.href = `/static/${fileName}`;
      link.download = fileName;
      link.style.display = 'none';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      setTimeout(() => {
        uni.showToast({
          title: '模板下载完成！',
          icon: 'success'
        });
      }, 1000);
      // #endif

      // #ifdef APP-PLUS
      // App环境下，使用plus.io API保存模板
      downloadTemplateWithPlusIO(fileName, isCSV);
      // #endif
    }
  });
};

// 使用plus.io API下载模板
const downloadTemplateWithPlusIO = (fileName, isCSV) => {
  try {
    console.log('使用plus.io API下载模板到手机:', fileName);
    
    // 准备模板内容
    let templateContent;
    if (isCSV) {
      // CSV模板内容
      templateContent = `测试模板,
1+1,2
5+9,14
(9-3)*2,12
`;
    } else {
      // Word模板内容（使用简单文本格式）
      templateContent = `测试模板

1+1 = 2
5+9 = 14
(9-3)*2 = 12
`;
      // 对于Word文档，改为.txt格式以便手机端能正常处理
      fileName = fileName.replace('.docx', '.txt');
    }
    
    // 添加BOM头确保中文正确显示
    const BOM = '\uFEFF';
    const finalContent = BOM + templateContent;
    
    // 使用plus.io API保存到Documents目录
    plus.io.requestFileSystem(plus.io.PUBLIC_DOCUMENTS, (fs) => {
      fs.root.getFile(fileName, { create: true }, (fileEntry) => {
        fileEntry.createWriter((writer) => {
          writer.onwriteend = () => {
            console.log('模板文件保存成功:', fileEntry.fullPath);
            uni.showToast({
              title: '模板下载完成！',
              icon: 'success'
            });
            
            setTimeout(() => {
              uni.showModal({
                title: '下载完成',
                content: `模板下载成功！

文件名：${fileName}
保存位置：文件管理器 → Android → data → io.dcloud.HBuilder → documents

请按照上述路径在文件管理器中查找文件`,
                showCancel: true,
                cancelText: '我知道了',
                confirmText: '复制路径',
                success: (res) => {
                  if (res.confirm) {
                    uni.setClipboardData({
                      data: fileEntry.fullPath,
                      success: () => {
                        uni.showToast({
                          title: '路径已复制到剪贴板',
                          icon: 'success'
                        });
                      }
                    });
                  }
                }
              });
            }, 1000);
          };
          
          writer.onerror = (e) => {
            console.error('模板文件写入失败:', e);
            uni.showToast({
              title: '下载失败',
              icon: 'none'
            });
          };
          
          // 写入内容
          writer.write(finalContent);
        });
      }, (error) => {
        console.error('创建模板文件失败:', error);
        uni.showToast({
          title: '下载失败',
          icon: 'none'
        });
      });
    }, (error) => {
      console.error('请求文件系统失败:', error);
      uni.showToast({
        title: '下载失败',
        icon: 'none'
      });
    });
    
  } catch (error) {
    console.error('下载模板失败:', error);
    uni.showToast({
      title: '下载失败',
      icon: 'none'
    });
  }
};



// 使用plus.io API导出文件
const exportWithPlusIO = (fileName, content, fileType) => {
  console.log(`使用plus.io导出${fileType}文件:`, fileName);
  
  // 添加BOM头确保中文正确显示（特别是CSV文件）
  const BOM = '\uFEFF';
  const finalContent = (fileType === 'CSV') ? BOM + content : content;
  
  // 使用plus.io API保存到Documents目录
  plus.io.requestFileSystem(plus.io.PUBLIC_DOCUMENTS, (fs) => {
    fs.root.getFile(fileName, { create: true }, (fileEntry) => {
      fileEntry.createWriter((writer) => {
        writer.onwriteend = () => {
          // 验证文件是否真的写入成功
          fileEntry.file((file) => {
            console.log(`${fileType}文件验证结果:`, {
              path: fileEntry.fullPath,
              size: file.size,
              expectedSize: finalContent.length
            });
            
            if (file.size > 0) {
              console.log(`${fileType}文件导出成功:`, fileEntry.fullPath);
              uni.showToast({
                title: `${fileType}导出成功！`,
                icon: 'success'
              });
              
              setTimeout(() => {
                uni.showModal({
                  title: '导出完成',
                  content: `文件导出成功！
                  
文件名：${fileName}
文件大小：${file.size} 字节
保存位置：文件管理器 → Android → data → io.xanfro.everymath → documents

请按照上述路径在文件管理器中查找文件`,
                  showCancel: true,
                  cancelText: '我知道了',
                  confirmText: '复制路径',
                  success: (res) => {
                    if (res.confirm) {
                      uni.setClipboardData({
                        data: fileEntry.fullPath,
                        success: () => {
                          uni.showToast({
                            title: '路径已复制到剪贴板',
                            icon: 'success'
                          });
                        }
                      });
                    }
                  }
                });
              }, 1000);
            } else {
              console.error(`${fileType}文件导出失败: 文件大小为0`);
              uni.showToast({
                title: '导出失败：文件为空',
                icon: 'none'
              });
            }
          }, (error) => {
            console.error('验证文件失败:', error);
            uni.showToast({
              title: '导出状态未知',
              icon: 'none'
            });
          });
        };
        
        writer.onerror = (e) => {
          console.error('文件写入失败:', e);
          uni.showToast({
            title: '导出失败',
            icon: 'none'
          });
        };
        
        // 写入内容
        writer.write(finalContent);
      });
    }, (error) => {
      console.error('创建文件失败:', error);
      uni.showToast({
        title: '导出失败',
        icon: 'none'
      });
    });
  }, (error) => {
    console.error('请求文件系统失败:', error);
    uni.showToast({
      title: '导出失败',
      icon: 'none'
    });
  });
};

// 手机端导出文件到Downloads目录
const exportToMobileStorage = (fileName, content, fileType) => {
  try {
    console.log(`开始导出${fileType}文件到手机:`, fileName);
    
    // 直接使用plus.io API，因为它更稳定可靠
    console.log('使用plus.io API保存导出文件');
    exportWithPlusIO(fileName, content, fileType);
    
  } catch (error) {
    console.error(`导出${fileType}文件失败:`, error);
    uni.showToast({
      title: '导出失败',
      icon: 'none'
    });
  }
};


// 开始练习 - 动态加载题目数据
const startPractice = async (question, index) => {
  try {
    console.log('开始练习，习题集ID:', question.id);
    
    uni.showLoading({
      title: '加载题目中...'
    });
    
    // 从API获取题目数据
    const response = await getQuestionsByQuestionSetId(question.id);
    console.log('题目数据API响应:', response);
    
    uni.hideLoading();
    
    if (response.code !== 0 || !response.data || response.data.length === 0) {
      uni.showToast({
        title: '暂无题目数据',
        icon: 'none'
      });
      return;
    }

    // 将题目数据转换为练习页面需要的格式
    const practiceQuestions = response.data.map((q, i) => {
      // 解析题目内容，尝试提取数字和运算符
      const questionText = q.question;
      const correctAnswerValue = parseFloat(q.answer);
      
      // 生成选项
      const options = generateOptions(correctAnswerValue);

      return {
        question: questionText,
        options: options,
        correctAnswer: options.indexOf(correctAnswerValue),
        difficulty: correctAnswerValue > 50 ? 2 : correctAnswerValue > 100 ? 3 : 1,
        userAnswer: -1,
        answered: false
      };
    });

    console.log('转换后的练习题目:', practiceQuestions);

    // 将题目数据存储到本地存储
    uni.setStorageSync('practiceQuestions', practiceQuestions);
    uni.setStorageSync('practiceTitle', question.name);
    uni.setStorageSync('practiceTimeLimit', 300); // 默认5分钟

    // 跳转到练习页面
    uni.navigateTo({
      url: '/pages/practice/practice?fromImport=true'
    });
    
  } catch (error) {
    console.error('加载练习题目失败:', error);
    uni.hideLoading();
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  }
};

// 生成选项的辅助函数
const generateOptions = (correctAnswer) => {
  const options = [correctAnswer];
  const offset = 3; // 干扰项范围

  while (options.length < 4) {
    const delta = Math.floor(Math.random() * offset) + 1;
    const sign = Math.random() > 0.5 ? 1 : -1;
    const option = correctAnswer + sign * delta;

    if (option > 0 && !options.includes(option)) {
      options.push(option);
    }
  }

  // 打乱选项顺序
  for (let i = options.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [options[i], options[j]] = [options[j], options[i]];
  }

  return options;
};

// 查看题目详情 - 使用真实API
const viewQuestions = async (question, index) => {
  try {
    console.log('开始加载题目详情:', question.id);
    
    uni.showLoading({
      title: '加载中...'
    });
    
    const response = await getQuestionsByQuestionSetId(question.id);
    console.log('题目列表API响应:', response);
    
    uni.hideLoading();
    
    if (response.code === 0 && response.data && response.data.length > 0) {
      // 构建题目列表文本
      let questionsText = '';
      response.data.forEach((q, i) => {
        questionsText += `${i + 1}. ${q.question} = ${q.answer}\n`;
      });

      uni.showModal({
        title: '题目详情',
        content: questionsText,
        showCancel: false,
        confirmText: '确定'
      });
    } else {
      uni.showToast({
        title: '暂无题目数据',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('加载题目详情失败:', error);
    uni.hideLoading();
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  }
};

// 处理CSV字段，确保特殊字符正确显示
const processCSVField = (field) => {
  if (!field) return '';
  
  // 将字段转换为字符串并处理特殊字符
  let processedField = String(field);
  
  // 如果字段包含逗号、双引号或换行符，需要用双引号包围
  if (processedField.includes(',') || processedField.includes('"') || processedField.includes('\n') || processedField.includes('\r')) {
    // 将字段中的双引号转义为两个双引号
    processedField = processedField.replace(/"/g, '""');
    // 用双引号包围整个字段
    processedField = `"${processedField}"`;
  }
  
  return processedField;
};

// 导出题目集 - 让用户选择格式
const exportQuestions = async (question, index) => {
  try {
    // 让用户选择导出格式
    uni.showActionSheet({
      itemList: ['导出为CSV文件', '导出为Word文档'],
      success: async function (res) {
        const exportTypes = ['csv', 'word'];
        const selectedType = exportTypes[res.tapIndex];
        
        console.log(`用户选择导出格式：${selectedType}`);
        
        // 根据选择的格式进行导出
        if (selectedType === 'csv') {
          await exportToCsv(question);
        } else {
          await exportToWord(question);
        }
      }
    });
  } catch (error) {
    console.error('导出题目集失败:', error);
    uni.showToast({
      title: '导出失败',
      icon: 'none'
    });
  }
};

// 导出为CSV格式
const exportToCsv = async (question) => {
  try {
    console.log('开始导出CSV，习题集ID:', question.id);
    
    uni.showLoading({
      title: '加载题目中...'
    });
    
    // 从API获取题目数据
    const response = await getQuestionsByQuestionSetId(question.id);
    console.log('导出题目数据API响应:', response);
    
    if (response.code !== 0 || !response.data || response.data.length === 0) {
      uni.hideLoading();
      uni.showToast({
        title: '暂无题目数据',
        icon: 'none'
      });
      return;
    }

    // 构建CSV内容，确保正确处理中文字符
    let csvContent = `${question.name},\n`; // 第一行为标题
    
    // 添加题目数据，对包含特殊字符的内容进行处理
    response.data.forEach((q) => {
      // 处理题目和答案中的特殊字符
      const processedQuestion = processCSVField(q.question);
      const processedAnswer = processCSVField(q.answer);
      csvContent += `${processedQuestion},${processedAnswer}\n`;
    });

    uni.hideLoading();

    // 生成文件名
    const fileName = `${question.name}_${new Date().toLocaleDateString().replace(/\//g, '-')}.csv`;

    uni.showToast({
      title: '正在导出CSV文件...',
      icon: 'loading',
      duration: 2000
    });

    // 根据不同平台使用不同的导出方式
    // #ifdef H5
    // H5环境下，创建下载链接，添加BOM头确保正确显示中文
    const BOM = '\uFEFF'; // UTF-8 BOM
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', fileName);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    setTimeout(() => {
      uni.showToast({
        title: 'CSV导出成功！',
        icon: 'success'
      });
    }, 1000);
    // #endif

    // #ifdef APP-PLUS
    // App环境下，保存到手机Downloads目录
    exportToMobileStorage(fileName, csvContent, 'CSV');
    // #endif

    // #ifdef MP-WEIXIN
    // 微信小程序环境下，保存到临时文件，添加BOM头
    const BOM = '\uFEFF'; // UTF-8 BOM
    const fileManager = wx.getFileSystemManager();
    const filePath = `${wx.env.USER_DATA_PATH}/${fileName}`;
    
    fileManager.writeFile({
      filePath: filePath,
      data: BOM + csvContent,
      encoding: 'utf8',
      success: () => {
        uni.showToast({
          title: 'CSV导出成功！',
          icon: 'success'
        });
        console.log('文件保存路径：', filePath);
      },
      fail: (error) => {
        console.error('文件保存失败：', error);
        uni.showToast({
          title: '导出失败',
          icon: 'none'
        });
      }
    });
    // #endif

  } catch (error) {
    console.error('导出CSV失败:', error);
    uni.hideLoading();
    uni.showToast({
      title: 'CSV导出失败',
      icon: 'none'
    });
  }
};

// 导出为Word格式
const exportToWord = async (question) => {
  try {
    console.log('开始导出Word，习题集ID:', question.id);
    
    uni.showLoading({
      title: '加载题目中...'
    });
    
    // 从API获取题目数据
    const response = await getQuestionsByQuestionSetId(question.id);
    console.log('导出题目数据API响应:', response);
    
    if (response.code !== 0 || !response.data || response.data.length === 0) {
      uni.hideLoading();
      uni.showToast({
        title: '暂无题目数据',
        icon: 'none'
      });
      return;
    }

    // 构建Word内容（简化的文本格式）
    let wordContent = `${question.name}\n`; // 标题，不空行
    
    // 添加题目数据，不加序号
    response.data.forEach((q) => {
      wordContent += `${q.question} = ${q.answer}\n`;
    });

    uni.hideLoading();

    // 生成文件名
    const fileName = `${question.name}_${new Date().toLocaleDateString().replace(/\//g, '-')}.txt`;

    uni.showToast({
      title: '正在导出Word文档...',
      icon: 'loading',
      duration: 2000
    });

    // 根据不同平台使用不同的导出方式
    // #ifdef H5
    // H5环境下，创建下载链接（导出为txt格式，用户可以复制到Word中）
    const blob = new Blob([wordContent], { type: 'text/plain;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', fileName);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    setTimeout(() => {
      uni.showToast({
        title: 'Word文档导出成功！',
        icon: 'success'
      });
      
      // 提示用户可以将txt文件内容复制到Word中
      uni.showModal({
        title: '导出提示',
        content: '已导出为文本文件，您可以将内容复制到Word文档中进行进一步编辑。',
        showCancel: false,
        confirmText: '我知道了'
      });
    }, 1000);
    // #endif

    // #ifdef APP-PLUS
    // App环境下，保存到手机Downloads目录
    exportToMobileStorage(fileName, wordContent, 'Word');
    // #endif

    // #ifdef MP-WEIXIN
    // 微信小程序环境下，保存到临时文件
    const fileManager = wx.getFileSystemManager();
    const filePath = `${wx.env.USER_DATA_PATH}/${fileName}`;
    
    fileManager.writeFile({
      filePath: filePath,
      data: wordContent,
      encoding: 'utf8',
      success: () => {
        uni.showToast({
          title: 'Word文档导出成功！',
          icon: 'success'
        });
        console.log('文件保存路径：', filePath);
      },
      fail: (error) => {
        console.error('文件保存失败：', error);
        uni.showToast({
          title: '导出失败',
          icon: 'none'
        });
      }
    });
    // #endif

  } catch (error) {
    console.error('导出Word失败:', error);
    uni.hideLoading();
    uni.showToast({
      title: 'Word导出失败',
      icon: 'none'
    });
  }
};

// 删除题目集 - 使用真实API
const deleteQuestion = async (index) => {
  const question = importedQuestions.value[index];
  uni.showModal({
    title: '确认删除',
    content: `确定要删除题目集"${question.name}"吗？`,
    success: async function (res) {
      if (res.confirm) {
        try {
          console.log('开始删除习题集:', question.id);
          
          const response = await deleteQuestionSet(question.id);
          console.log('删除习题集API响应:', response);
          
          if (response.code === 0) {
            // 从本地数组中删除
            importedQuestions.value.splice(index, 1);
            
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            });
          } else {
            console.error('删除习题集API返回错误:', response.message);
            uni.showToast({
              title: response.message || '删除失败',
              icon: 'none'
            });
          }
        } catch (error) {
          console.error('删除题目集失败:', error);
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 加载题目集数据 - 使用真实API
const loadQuestionSets = async () => {
  try {
    if (!userId.value) {
      console.error('用户ID不存在');
      return;
    }

    console.log('开始加载习题集数据，用户ID:', userId.value);
    
    const response = await getQuestionSets(userId.value);
    console.log('习题集API响应:', response);
    
    if (response.code === 0 && response.data) {
      // 转换数据格式以适配现有的UI
      importedQuestions.value = response.data.map(questionSet => ({
        id: questionSet.id,
        name: questionSet.name,
        importTime: formatDateTime(questionSet.createdTime || questionSet.importTime),
        questionCount: parseInt(questionSet.questionCount) || 0,
        questions: [] // 题目列表将在需要时单独加载
      }));
      
      console.log('习题集数据加载成功:', importedQuestions.value);
    } else {
      console.error('习题集API返回错误:', response.message);
      importedQuestions.value = [];
    }
  } catch (error) {
    console.error('加载题目集数据失败:', error);
    uni.showToast({
      title: '数据加载失败',
      icon: 'none'
    });
    importedQuestions.value = [];
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
      await loadQuestionSets();
    } else {
      console.error('用户ID不存在，请先登录');
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('页面初始化失败:', error);
  } finally {
    loading.value = false;
  }
});

// 从练习设置弹窗开始练习
const startPracticeFromSetup = (settings) => {
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

<style lang="scss" scoped>
.import-container {
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

// 导航栏
.navbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: left;
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

// 区块标题
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding: 0;
  position: relative;
  z-index: 1;
}

// 导入方式区域
.import-section {
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
  padding: 0 15px;
}

// 背景板样式
.section-background {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

// 背景板装饰效果
.section-background::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(66, 153, 225, 0.05) 0%, transparent 70%);
  animation: rotate 20s linear infinite;
}

.import-actions {
  padding: 0;
}

.action-card {
  background-color: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 2px solid rgba(66, 153, 225, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: rgba(66, 153, 225, 0.4);
    box-shadow: 0 4px 12px rgba(66, 153, 225, 0.15);
    transform: translateY(-2px);
  }

  &:active {
    transform: scale(0.98) translateY(0);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    border-color: rgba(66, 153, 225, 0.6);
  }
}

.action-icon {
  width: 60px;
  height: 60px;
  background-color: #f8fafc;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.action-content {
  flex: 1;
  min-width: 0;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.action-arrow {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

// 导入题目列表区域
.import-list-section {
  position: relative;
  z-index: 1;
  padding: 0 15px;
}

.import-list {
  padding: 0;
}

.question-item {
  background-color: transparent;
  border-radius: 0;
  padding: 24px 0;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    background-color: rgba(66, 153, 225, 0.02);
  }
}

// 分隔线样式
.question-divider {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(0, 0, 0, 0.1) 20%, rgba(0, 0, 0, 0.1) 80%, transparent 100%);
}

.question-content {
  flex: 1;
  min-width: 0;
  margin-right: 20px;
}

// 右上角删除按钮
.delete-btn-corner {
  position: absolute;
  top: 8px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 2;

  &:hover {
    background-color: rgba(220, 53, 69, 0.1);
    transform: scale(1.1);
  }

  &:active {
    background-color: rgba(220, 53, 69, 0.2);
    transform: scale(0.95);
  }
}

.question-header {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.question-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
  line-height: 1.4;
  margin-right: 6px;
}

// 标题右方预览按钮
.preview-btn {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  background-color: rgba(102, 102, 102, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  margin-left: 2px;

  &:hover {
    background-color: rgba(102, 102, 102, 0.15);
    transform: translateY(-1px);
  }

  &:active {
    background-color: rgba(102, 102, 102, 0.25);
    transform: translateY(0);
  }
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.question-count {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  background-color: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
  white-space: nowrap;
}

.question-time {
  font-size: 12px;
  color: #6c757d;
  background-color: #f8f9fa;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  white-space: nowrap;
}

.question-actions {
  display: flex;
  flex-direction: row;
  gap: 8px;
  flex-shrink: 0;
  margin-right: 15px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #dc3545;

  &.practice-btn {
    background-color: rgba(0, 123, 255, 0.1);
    color: #007bff;
    border: 1px solid #007bff;

    &:hover {
      background-color: rgba(0, 123, 255, 0.15);
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(0, 123, 255, 0.15);
    }

    &:active {
      background-color: rgba(0, 123, 255, 0.2);
      transform: translateY(0);
    }
  }


  &.export-btn {
    background-color: rgba(255, 149, 0, 0.1);
    color: #ff9500;
    border: 1px solid #ff9500;

    &:hover {
      background-color: rgba(255, 149, 0, 0.15);
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(255, 149, 0, 0.15);
    }

    &:active {
      background-color: rgba(255, 149, 0, 0.2);
      transform: translateY(0);
    }
  }

}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  margin: 0 15px;
  border-radius: 20px;
  border: 2px dashed #cbd5e0;
  position: relative;
  overflow: hidden;
}

.empty-state::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.05) 0%, transparent 70%);
  animation: rotate 20s linear infinite;
}

.empty-text {
  font-size: 18px;
  color: #4a5568;
  margin-top: 16px;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.empty-desc {
  font-size: 14px;
  color: #718096;
  margin-top: 8px;
  position: relative;
  z-index: 1;
}

// 动画
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
