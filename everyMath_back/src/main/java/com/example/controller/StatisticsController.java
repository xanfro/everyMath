package com.example.controller;

import com.example.pojo.common.Result;
import com.example.pojo.vo.DailyStatisticsVO;
import com.example.pojo.vo.GradeStatisticsVO;
import com.example.pojo.vo.MistakeCompletionVO;
import com.example.pojo.vo.StatisticsOverviewVO;
import com.example.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取用户统计概览数据
     * @param userId 用户ID
     * @return 统计概览数据
     */
    @GetMapping("/overview/{userId}")
    public Result<StatisticsOverviewVO> getStatisticsOverview(@PathVariable Integer userId) {
        try {
            StatisticsOverviewVO overview = statisticsService.getStatisticsOverview(userId);
            return Result.success("获取统计概览成功", overview);
        } catch (Exception e) {
            return Result.error("获取统计概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户各年级练题数量统计
     * @param userId 用户ID
     * @return 各年级练题数量统计
     */
    @GetMapping("/grade/{userId}")
    public Result<List<GradeStatisticsVO>> getGradeStatistics(@PathVariable Integer userId) {
        try {
            List<GradeStatisticsVO> gradeStatistics = statisticsService.getGradeStatistics(userId);
            return Result.success("获取年级统计成功", gradeStatistics);
        } catch (Exception e) {
            return Result.error("获取年级统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户错题完成率统计
     * @param userId 用户ID
     * @return 错题完成率统计
     */
    @GetMapping("/mistake-completion/{userId}")
    public Result<MistakeCompletionVO> getMistakeCompletion(@PathVariable Integer userId) {
        try {
            MistakeCompletionVO mistakeCompletion = statisticsService.getMistakeCompletion(userId);
            return Result.success("获取错题完成率统计成功", mistakeCompletion);
        } catch (Exception e) {
            return Result.error("获取错题完成率统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户近7日做题数量统计
     * @param userId 用户ID
     * @return 近7日每日做题数量统计
     */
    @GetMapping("/daily/{userId}")
    public Result<List<DailyStatisticsVO>> getDailyStatistics(@PathVariable Integer userId) {
        try {
            List<DailyStatisticsVO> dailyStatistics = statisticsService.getDailyStatistics(userId);
            return Result.success("获取近7日统计成功", dailyStatistics);
        } catch (Exception e) {
            return Result.error("获取近7日统计失败: " + e.getMessage());
        }
    }
}
