package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mapper.MistakeRecordMapper;
import com.example.mapper.PracticeRecordMapper;
import com.example.pojo.entity.MistakeRecord;
import com.example.pojo.entity.PracticeRecord;
import com.example.pojo.vo.DailyStatisticsVO;
import com.example.pojo.vo.GradeStatisticsVO;
import com.example.pojo.vo.MistakeCompletionVO;
import com.example.pojo.vo.StatisticsOverviewVO;
import com.example.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Autowired
    private MistakeRecordMapper mistakeRecordMapper;

    @Override
    public StatisticsOverviewVO getStatisticsOverview(Integer userId) {
        StatisticsOverviewVO overview = new StatisticsOverviewVO();

        // 查询用户所有练习记录
        QueryWrapper<PracticeRecord> practiceWrapper = new QueryWrapper<>();
        practiceWrapper.eq("user_id", userId);
        List<PracticeRecord> practiceRecords = practiceRecordMapper.selectList(practiceWrapper);

        if (practiceRecords.isEmpty()) {
            // 如果没有练习记录，返回默认值
            overview.setOverallCorrectRate(0.0);
            overview.setTotalQuestionCount(0);
            overview.setTotalCorrectCount(0);
            return overview;
        }

        // 统计练习数据
        int totalQuestions = 0;
        int totalCorrect = 0;

        for (PracticeRecord record : practiceRecords) {
            totalQuestions += Integer.parseInt(record.getQuestionCount());
            totalCorrect += Integer.parseInt(record.getCorrectCount());
        }

        // 计算总体正确率
        double overallCorrectRate = totalQuestions > 0 ? (double) totalCorrect / totalQuestions * 100 : 0.0;

        // 设置返回数据
        overview.setOverallCorrectRate(Math.round(overallCorrectRate * 100.0) / 100.0); // 保留两位小数
        overview.setTotalQuestionCount(totalQuestions);
        overview.setTotalCorrectCount(totalCorrect);

        return overview;
    }

    @Override
    public List<GradeStatisticsVO> getGradeStatistics(Integer userId) {
        // 查询用户所有练习记录
        QueryWrapper<PracticeRecord> practiceWrapper = new QueryWrapper<>();
        practiceWrapper.eq("user_id", userId);
        List<PracticeRecord> practiceRecords = practiceRecordMapper.selectList(practiceWrapper);

        // 统计各年级的练题数量
        Map<String, Integer> gradeQuestionMap = new HashMap<>();
        
        // 初始化1-6年级的数据为0
        for (int i = 1; i <= 6; i++) {
            gradeQuestionMap.put(String.valueOf(i), 0);
        }

        // 统计各年级的题目数量
        for (PracticeRecord record : practiceRecords) {
            String grade = record.getGrade();
            int questionCount = Integer.parseInt(record.getQuestionCount());
            
            // 累加该年级的题目数量
            gradeQuestionMap.put(grade, gradeQuestionMap.getOrDefault(grade, 0) + questionCount);
        }

        // 转换为VO列表
        List<GradeStatisticsVO> result = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            String grade = String.valueOf(i);
            Integer questionCount = gradeQuestionMap.get(grade);
            result.add(new GradeStatisticsVO(grade, questionCount));
        }

        return result;
    }

    @Override
    public MistakeCompletionVO getMistakeCompletion(Integer userId) {
        // 查询用户所有错题记录
        QueryWrapper<MistakeRecord> mistakeWrapper = new QueryWrapper<>();
        mistakeWrapper.eq("user_id", userId);
        List<MistakeRecord> mistakeRecords = mistakeRecordMapper.selectList(mistakeWrapper);

        if (mistakeRecords.isEmpty()) {
            // 如果没有错题记录，返回默认值
            return new MistakeCompletionVO(0, 0, 0, 0.0);
        }

        // 统计已完成和未完成的错题数量
        int completedCount = 0;
        int uncompletedCount = 0;

        for (MistakeRecord record : mistakeRecords) {
            if ("1".equals(record.getCorrected())) {
                completedCount++;
            } else {
                uncompletedCount++;
            }
        }

        int totalCount = mistakeRecords.size();
        double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0.0;

        // 保留两位小数
        completionRate = Math.round(completionRate * 100.0) / 100.0;

        return new MistakeCompletionVO(completedCount, uncompletedCount, totalCount, completionRate);
    }

    @Override
    public List<DailyStatisticsVO> getDailyStatistics(Integer userId) {
        // 获取近7日的日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 包含今天共7天
        
        // 查询用户在近7日内的练习记录
        QueryWrapper<PracticeRecord> practiceWrapper = new QueryWrapper<>();
        practiceWrapper.eq("user_id", userId)
                      .ge("created_time", startDate.toString() + " 00:00:00")
                      .le("created_time", endDate.toString() + " 23:59:59");
        List<PracticeRecord> practiceRecords = practiceRecordMapper.selectList(practiceWrapper);

        // 初始化近7日的数据映射
        Map<String, Integer> dailyQuestionMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        // 为近7日初始化数据为0
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateKey = date.format(formatter);
            dailyQuestionMap.put(dateKey, 0);
        }

        // 统计每日的题目数量
        for (PracticeRecord record : practiceRecords) {
            // 从created_time中提取日期部分
            String createdTime = record.getCreatedTime().toString();
            if (createdTime != null && createdTime.length() >= 10) {
                try {
                    LocalDate recordDate = LocalDate.parse(createdTime.substring(0, 10));
                    String dateKey = recordDate.format(formatter);
                    
                    // 如果日期在近7日范围内，累加题目数量
                    if (dailyQuestionMap.containsKey(dateKey)) {
                        int questionCount = Integer.parseInt(record.getQuestionCount());
                        dailyQuestionMap.put(dateKey, dailyQuestionMap.get(dateKey) + questionCount);
                    }
                } catch (Exception e) {
                    // 日期解析失败，跳过该记录
                    continue;
                }
            }
        }

        // 转换为VO列表，按日期顺序排列
        List<DailyStatisticsVO> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateKey = date.format(formatter);
            Integer questionCount = dailyQuestionMap.get(dateKey);
            result.add(new DailyStatisticsVO(dateKey, questionCount));
        }

        return result;
    }
}
