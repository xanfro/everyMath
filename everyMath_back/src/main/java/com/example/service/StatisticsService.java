package com.example.service;

import com.example.pojo.vo.DailyStatisticsVO;
import com.example.pojo.vo.GradeStatisticsVO;
import com.example.pojo.vo.MistakeCompletionVO;
import com.example.pojo.vo.StatisticsOverviewVO;

import java.util.List;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取用户统计概览数据
     * @param userId 用户ID
     * @return 统计概览数据
     */
    StatisticsOverviewVO getStatisticsOverview(Integer userId);

    /**
     * 获取用户各年级练题数量统计
     * @param userId 用户ID
     * @return 各年级练题数量列表
     */
    List<GradeStatisticsVO> getGradeStatistics(Integer userId);

    /**
     * 获取用户错题完成率统计
     * @param userId 用户ID
     * @return 错题完成率统计
     */
    MistakeCompletionVO getMistakeCompletion(Integer userId);

    /**
     * 获取用户近7日做题数量统计
     * @param userId 用户ID
     * @return 近7日每日做题数量列表
     */
    List<DailyStatisticsVO> getDailyStatistics(Integer userId);
}
