package com.example.pojo.vo;

import lombok.Data;

/**
 * 统计概览视图对象
 * 用于返回用户学习统计的概览数据
 */
@Data
public class StatisticsOverviewVO {

    /**
     * 总体正确率（百分比）
     */
    private Double overallCorrectRate;

    /**
     * 总题目数
     */
    private Integer totalQuestionCount;

    /**
     * 总正确数
     */
    private Integer totalCorrectCount;
}
