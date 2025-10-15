-- EveryMath 数学练习应用数据库脚本
-- 数据库名称: every_math
-- 创建时间: 2024-01-15
-- 更新时间: 2024-12-19
-- 说明: 完整版数据库结构，使用逻辑外键，不使用物理外键
-- 所有数字字段使用VARCHAR存储，提高灵活性

-- 创建数据库
CREATE DATABASE IF NOT EXISTS every_math DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE every_math;

-- 1. 用户表 (users)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    grade VARCHAR(20) NOT NULL COMMENT '年级',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户表';

-- 2. 习题集表 (question_sets)
CREATE TABLE question_sets (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '习题集ID',
    user_id INT NOT NULL COMMENT '用户ID，逻辑外键关联users表',
    name VARCHAR(100) NOT NULL COMMENT '习题集名称',
    import_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
    question_count VARCHAR(10) NOT NULL DEFAULT '0' COMMENT '题目数量',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '习题集表';

-- 3. 题目表 (questions)
CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    question_set_id INT NOT NULL COMMENT '习题集ID，逻辑外键关联question_sets表',
    question VARCHAR(200) NOT NULL COMMENT '题目内容（如：25 + 18）',
    answer VARCHAR(50) NOT NULL COMMENT '正确答案'
) COMMENT '题目表';

-- 4. 练习记录表 (practice_records)
CREATE TABLE practice_records (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '练习记录ID',
    user_id INT NOT NULL COMMENT '用户ID，逻辑外键关联users表',
    grade VARCHAR(20) NOT NULL COMMENT '练习年级',
    question_count VARCHAR(10) NOT NULL COMMENT '题目总数',
    correct_count VARCHAR(10) NOT NULL DEFAULT '0' COMMENT '正确题目数',
    wrong_count VARCHAR(10) NOT NULL DEFAULT '0' COMMENT '错误题目数',
    correct_rate VARCHAR(10) NOT NULL DEFAULT '0.00' COMMENT '正确率',
    time_limit VARCHAR(10) COMMENT '时间限制（秒）',
    time_spent VARCHAR(10) NOT NULL COMMENT '实际用时（秒）',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '练习记录表';

-- 7. 错题记录表 (mistake_records)
CREATE TABLE mistake_records (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '错题记录ID',
    user_id INT NOT NULL COMMENT '用户ID，逻辑外键关联users表',
    question VARCHAR(200) NOT NULL COMMENT '题目内容',
    correct_answer VARCHAR(50) NOT NULL COMMENT '正确答案',
    corrected VARCHAR(10) DEFAULT '0' COMMENT '是否已纠正：1-已纠正，0-未纠正',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '错题记录表';

-- ========================================
-- 插入模拟数据
-- ========================================

-- 1. 插入用户数据
INSERT INTO users (username, password, grade) VALUES
('xiaoming', 'e10adc3949ba59abbe56e057f20f883e', '3'),
('xiaohong', 'e10adc3949ba59abbe56e057f20f883e', '2'),
('xiaogang', 'e10adc3949ba59abbe56e057f20f883e', '4'),
('xiaoli', 'e10adc3949ba59abbe56e057f20f883e', '1'),
('xiaowang', 'e10adc3949ba59abbe56e057f20f883e', '5');

-- 2. 插入习题集数据
INSERT INTO question_sets (user_id, name, question_count) VALUES
(1, '三年级加减法练习', '20'),
(1, '乘法口诀表练习', '15'),
(2, '二年级基础运算', '25'),
(3, '四年级混合运算', '30'),
(3, '除法练习题', '18'),
(4, '一年级数数练习', '12'),
(5, '五年级分数运算', '22');

-- 3. 插入题目数据
-- 习题集1：三年级加减法练习（20题）
INSERT INTO questions (question_set_id, question, answer) VALUES
(1, '15 + 8', '23'),
(1, '23 - 9', '14'),
(1, '34 + 17', '51'),
(1, '45 - 18', '27'),
(1, '56 + 24', '80'),
(1, '67 - 29', '38'),
(1, '78 + 15', '93'),
(1, '89 - 34', '55'),
(1, '92 + 8', '100'),
(1, '73 - 25', '48'),
(1, '84 + 16', '100'),
(1, '95 - 37', '58'),
(1, '68 + 22', '90'),
(1, '79 - 31', '48'),
(1, '86 + 14', '100'),
(1, '97 - 28', '69'),
(1, '74 + 19', '93'),
(1, '85 - 36', '49'),
(1, '96 + 4', '100'),
(1, '87 - 29', '58');

-- 习题集2：乘法口诀表练习（15题）
INSERT INTO questions (question_set_id, question, answer) VALUES
(2, '6 × 7', '42'),
(2, '8 × 9', '72'),
(2, '9 × 4', '36'),
(2, '7 × 8', '56'),
(2, '6 × 9', '54'),
(2, '8 × 7', '56'),
(2, '9 × 5', '45'),
(2, '7 × 6', '42'),
(2, '8 × 8', '64'),
(2, '9 × 9', '81'),
(2, '6 × 6', '36'),
(2, '7 × 7', '49'),
(2, '8 × 6', '48'),
(2, '9 × 7', '63'),
(2, '6 × 8', '48');

-- 习题集3：二年级基础运算（25题）
INSERT INTO questions (question_set_id, question, answer) VALUES
(3, '12 + 8', '20'),
(3, '25 - 7', '18'),
(3, '18 + 15', '33'),
(3, '30 - 12', '18'),
(3, '19 + 16', '35'),
(3, '28 - 9', '19'),
(3, '14 + 17', '31'),
(3, '35 - 18', '17'),
(3, '22 + 19', '41'),
(3, '40 - 15', '25'),
(3, '13 + 9', '22'),
(3, '26 - 8', '18'),
(3, '17 + 14', '31'),
(3, '32 - 16', '16'),
(3, '21 + 18', '39'),
(3, '29 - 11', '18'),
(3, '16 + 15', '31'),
(3, '38 - 19', '19'),
(3, '24 + 17', '41'),
(3, '33 - 14', '19'),
(3, '11 + 12', '23'),
(3, '27 - 9', '18'),
(3, '15 + 16', '31'),
(3, '31 - 13', '18'),
(3, '20 + 19', '39');

-- 习题集4：四年级混合运算（30题）
INSERT INTO questions (question_set_id, question, answer) VALUES
(4, '25 + 18 - 12', '31'),
(4, '36 - 15 + 9', '30'),
(4, '48 ÷ 6 + 7', '15'),
(4, '5 × 8 - 12', '28'),
(4, '72 ÷ 9 + 15', '23'),
(4, '6 × 7 - 18', '24'),
(4, '84 ÷ 12 + 9', '16'),
(4, '9 × 4 - 15', '21'),
(4, '63 ÷ 7 + 12', '21'),
(4, '8 × 6 - 24', '24'),
(4, '45 ÷ 5 + 8', '17'),
(4, '7 × 9 - 25', '38'),
(4, '56 ÷ 8 + 13', '20'),
(4, '4 × 9 - 16', '20'),
(4, '81 ÷ 9 + 7', '16'),
(4, '6 × 8 - 19', '29'),
(4, '42 ÷ 6 + 11', '18'),
(4, '5 × 7 - 14', '21'),
(4, '54 ÷ 6 + 9', '18'),
(4, '8 × 5 - 17', '23'),
(4, '35 ÷ 7 + 12', '17'),
(4, '9 × 6 - 23', '31'),
(4, '49 ÷ 7 + 8', '15'),
(4, '7 × 8 - 26', '30'),
(4, '64 ÷ 8 + 6', '14'),
(4, '6 × 9 - 21', '33'),
(4, '36 ÷ 4 + 7', '16'),
(4, '8 × 7 - 28', '28'),
(4, '72 ÷ 8 + 5', '14'),
(4, '9 × 5 - 19', '26');

-- 习题集5：除法练习题（18题）
INSERT INTO questions (question_set_id, question, answer) VALUES
(5, '48 ÷ 8', '6'),
(5, '35 ÷ 5', '7'),
(5, '54 ÷ 9', '6'),
(5, '42 ÷ 6', '7'),
(5, '63 ÷ 7', '9'),
(5, '72 ÷ 8', '9'),
(5, '56 ÷ 7', '8'),
(5, '81 ÷ 9', '9'),
(5, '64 ÷ 8', '8'),
(5, '49 ÷ 7', '7'),
(5, '36 ÷ 6', '6'),
(5, '45 ÷ 5', '9'),
(5, '32 ÷ 8', '4'),
(5, '28 ÷ 7', '4'),
(5, '24 ÷ 6', '4'),
(5, '40 ÷ 8', '5'),
(5, '21 ÷ 7', '3'),
(5, '18 ÷ 6', '3');

-- 习题集6：一年级数数练习（12题）
INSERT INTO questions (question_set_id, question, answer) VALUES
(6, '3 + 2', '5'),
(6, '5 - 1', '4'),
(6, '4 + 3', '7'),
(6, '6 - 2', '4'),
(6, '2 + 4', '6'),
(6, '7 - 3', '4'),
(6, '1 + 5', '6'),
(6, '8 - 2', '6'),
(6, '3 + 4', '7'),
(6, '9 - 4', '5'),
(6, '2 + 6', '8'),
(6, '10 - 3', '7');

-- 习题集7：五年级分数运算（22题）
INSERT INTO questions (question_set_id, question, answer) VALUES
(7, '1/2 + 1/4', '3/4'),
(7, '3/4 - 1/4', '1/2'),
(7, '2/3 + 1/6', '5/6'),
(7, '5/6 - 1/3', '1/2'),
(7, '1/3 + 2/9', '5/9'),
(7, '7/8 - 3/8', '1/2'),
(7, '2/5 + 1/10', '1/2'),
(7, '4/5 - 2/5', '2/5'),
(7, '1/4 + 3/8', '5/8'),
(7, '7/10 - 3/10', '2/5'),
(7, '3/7 + 2/7', '5/7'),
(7, '6/9 - 2/9', '4/9'),
(7, '1/6 + 1/3', '1/2'),
(7, '5/8 - 1/4', '3/8'),
(7, '2/9 + 4/9', '2/3'),
(7, '3/5 - 1/5', '2/5'),
(7, '1/8 + 3/8', '1/2'),
(7, '7/12 - 1/4', '1/3'),
(7, '2/7 + 3/7', '5/7'),
(7, '4/6 - 1/6', '1/2'),
(7, '3/10 + 2/10', '1/2'),
(7, '8/9 - 2/9', '2/3');

-- 4. 插入练习记录数据
INSERT INTO practice_records (user_id, grade, question_count, correct_count, wrong_count, correct_rate, time_limit, time_spent) VALUES
(1, '3', '10', '8', '2', '80.00', '600', '480'),
(1, '3', '15', '12', '3', '80.00', NULL, '720'),
(2, '2', '10', '9', '1', '90.00', '600', '420'),
(3, '4', '20', '16', '4', '80.00', NULL, '900'),
(3, '4', '8', '6', '2', '75.00', NULL, '360'),
(4, '1', '10', '10', '0', '100.00', '600', '300'),
(5, '5', '15', '13', '2', '86.67', NULL, '840'),
(1, '3', '10', '7', '3', '70.00', '600', '580'),
(2, '2', '10', '8', '2', '80.00', '600', '450'),
(3, '4', '10', '9', '1', '90.00', '600', '520'),
(1, '3', '20', '18', '2', '90.00', '1200', '1080'),
(2, '2', '25', '22', '3', '88.00', '1500', '1320'),
(3, '4', '30', '25', '5', '83.33', '1800', '1650'),
(4, '1', '12', '11', '1', '91.67', '720', '600'),
(5, '5', '22', '19', '3', '86.36', '1320', '1200');

-- 5. 插入错题记录数据
INSERT INTO mistake_records (user_id, question, correct_answer, corrected) VALUES
(1, '25 - 18', '7', '0'),
(1, '6 × 8', '48', '0'),
(1, '72 ÷ 9', '8', '1'),
(2, '34 + 29', '63', '0'),
(3, '45 - 27', '18', '1'),
(3, '7 × 9', '63', '0'),
(3, '56 ÷ 7', '8', '1'),
(1, '19 + 26', '45', '0'),
(2, '15 - 8', '7', '1'),
(3, '4 × 6', '24', '0'),
(4, '12 + 8', '20', '0'),
(5, '81 ÷ 9', '9', '1'),
(5, '3/4 + 1/4', '1', '0'),
(1, '67 - 29', '38', '0'),
(2, '28 - 9', '19', '1'),
(3, '48 ÷ 6 + 7', '15', '0'),
(4, '5 - 1', '4', '1'),
(5, '2/3 + 1/6', '5/6', '0'),
(1, '95 - 37', '58', '1'),
(2, '32 - 16', '16', '0');
