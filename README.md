# EveryMath - 天天口算

EveryMath（天天口算）是一个专为小学生设计的数学口算练习应用，包含完整的前后端系统。该应用可以帮助学生提高口算能力，并提供练习记录、错题回顾等功能。

## 目录结构

```
EveryMath/
├── everyMath_back/         # 后端项目（Spring Boot）
├── everyMath_front/        # 前端项目（UniApp）
└── every_math.sql          # 数据库初始化脚本
```

## 技术栈

### 后端技术栈
- Spring Boot 3.5.6
- MyBatis Plus
- MySQL 数据库
- Maven 项目管理
- Java 17

### 前端技术栈
- UniApp（跨平台开发框架）
- Vue 3
- uCharts 图表组件

## 功能特性

1. **用户管理**
   - 用户注册与登录
   - 年级信息管理

2. **题目练习**
   - 多种题型支持（加减法、乘除法、混合运算、分数运算等）
   - 计时练习模式
   - 实时答题反馈

3. **练习记录**
   - 练习历史记录
   - 正确率统计
   - 时间分析

4. **错题回顾**
   - 错题自动收录
   - 错题标记与复习

5. **数据导入**
   - 支持从Word文档导入题目
   - 自定义习题集管理

6. **学习分析**
   - 练习情况可视化图表
   - 学习进度跟踪

## 环境要求

### 后端环境
- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+

### 前端环境
- HBuilderX（推荐）或其他支持UniApp的IDE
- Node.js（可选，用于本地调试）

## 快速开始

### 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE IF NOT EXISTS every_math DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本 [every_math.sql](every_math.sql) 初始化表结构和数据

### 后端服务部署

1. 修改 [application.yml](everyMath_back/src/main/resources/application.yml) 中的数据库连接配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/every_math?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

2. 在 [everyMath_back](everyMath_back) 目录下执行命令构建项目：
```bash
mvn clean package
```

3. 运行后端服务：
```bash
java -jar target/everyMath_back-0.0.1-SNAPSHOT.jar
```

4. 后端服务默认运行在 `http://localhost:8080`

### 前端部署

1. 使用 HBuilderX 导入 [everyMath_front](everyMath_front) 项目

2. 修改 [api/index.js](everyMath_front/api/index.js) 中的后端服务地址：
```javascript
const BASE_URL = 'http://localhost:8080'
```

3. 在 HBuilderX 中运行到目标平台（H5、微信小程序、Android APK等）

## API接口

后端提供RESTful API接口，主要控制器包括：

- [UserController](everyMath_back/src/main/java/com/example/controller/UserController.java) - 用户管理
- [QuestionController](everyMath_back/src/main/java/com/example/controller/QuestionController.java) - 题目相关
- [PracticeRecordController](everyMath_back/src/main/java/com/example/controller/PracticeRecordController.java) - 练习记录
- [MistakeRecordController](everyMath_back/src/main/java/com/example/controller/MistakeRecordController.java) - 错题记录
- [QuestionSetController](everyMath_back/src/main/java/com/example/controller/QuestionSetController.java) - 习题集管理
- [StatisticsController](everyMath_back/src/main/java/com/example/controller/StatisticsController.java) - 统计分析

## 数据库设计

数据库包含以下核心表：

- `users` - 用户表
- `question_sets` - 习题集表
- `questions` - 题目表
- `practice_records` - 练习记录表
- `mistake_records` - 错题记录表

详细结构请参考 [every_math.sql](every_math.sql)

## 部署说明

### 后端部署

可以使用以下任意一种方式运行后端服务：

1. 直接运行Jar包（推荐生产环境）：
```bash
nohup java -jar everyMath_back-0.0.1-SNAPSHOT.jar > backend.log 2>&1 &
```

2. 使用Docker容器化部署（项目包含[Dockerfile](everyMath_back/Dockerfile)）：
```bash
cd everyMath_back
docker build -t everymath-back .
docker run -d -p 8080:8080 everymath-back
```

### 前端部署

UniApp支持编译到多个平台：

1. **H5网页**
   - 在HBuilderX中选择 发行 -> 网站-H5

2. **微信小程序**
   - 在HBuilderX中选择 发行 -> 小程序-微信

3. **Android APK**
   - 在HBuilderX中选择 发行 -> 原生App-云打包

## 开发指南

### 后端开发

1. 使用IDE导入 [everyMath_back](everyMath_back) Maven项目
2. 修改配置文件适应本地环境
3. 启动 [EveryMathBackApplication](everyMath_back/src/main/java/com/example/EveryMathBackApplication.java) 主类

### 前端开发

1. 使用HBuilderX导入 [everyMath_front](everyMath_front) 项目
2. 安装依赖（如果需要本地调试）：
   ```bash
   npm install
   ```
3. 运行开发服务器：
   ```bash
   npm run dev:%PLATFORM%
   ```

## 项目特点

1. **跨平台支持**：基于UniApp开发，一套代码多端运行
2. **灵活部署**：支持传统部署和容器化部署
3. **易于扩展**：模块化设计，便于添加新功能
4. **数据安全**：密码加密存储，防止敏感信息泄露
5. **用户友好**：直观的操作界面，适合小学生使用

## 贡献者

- 项目开发团队

## 许可证

本项目仅供学习交流使用。