package com.example.pojo.vo;

import lombok.Data;

/**
 * 日常统计视图对象
 * 用于返回每日的做题数量统计
 */
@Data
public class DailyStatisticsVO {

    /**
     * 日期（格式：MM-dd）
     */
    private String date;

    /**
     * 该日期的做题数量
     */
    private Integer questionCount;

    public DailyStatisticsVO() {}

    public DailyStatisticsVO(String date, Integer questionCount) {
        this.date = date;
        this.questionCount = questionCount;
    }
}
