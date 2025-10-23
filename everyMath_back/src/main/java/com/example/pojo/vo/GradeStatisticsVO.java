package com.example.pojo.vo;

import lombok.Data;

/**
 * 年级统计视图对象
 * 用于返回各年级的练题数量统计
 */
@Data
public class GradeStatisticsVO {

    /**
     * 年级（1-6）
     */
    private String grade;

    /**
     * 该年级的练题数量
     */
    private Integer questionCount;

    public GradeStatisticsVO() {}

    public GradeStatisticsVO(String grade, Integer questionCount) {
        this.grade = grade;
        this.questionCount = questionCount;
    }
}
