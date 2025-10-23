package com.example.pojo.vo;

import lombok.Data;

/**
 * 错题完成率统计视图对象
 * 用于返回错题的完成情况统计
 */
@Data
public class MistakeCompletionVO {

    /**
     * 已完成错题数量
     */
    private Integer completedCount;

    /**
     * 未完成错题数量
     */
    private Integer uncompletedCount;

    /**
     * 总错题数量
     */
    private Integer totalCount;

    /**
     * 完成率（百分比）
     */
    private Double completionRate;

    public MistakeCompletionVO() {}

    public MistakeCompletionVO(Integer completedCount, Integer uncompletedCount, Integer totalCount, Double completionRate) {
        this.completedCount = completedCount;
        this.uncompletedCount = uncompletedCount;
        this.totalCount = totalCount;
        this.completionRate = completionRate;
    }
}
