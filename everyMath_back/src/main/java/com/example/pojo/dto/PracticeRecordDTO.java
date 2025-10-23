package com.example.pojo.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 练习记录数据传输对象
 * 用于接收前端提交的练习记录数据
 */
@Data
public class PracticeRecordDTO {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 练习年级
     */
    private String grade;

    /**
     * 题目总数
     */
    private Integer questionCount;

    /**
     * 正确题目数
     */
    private Integer correctCount;

    /**
     * 错误题目数
     */
    private Integer wrongCount;

    /**
     * 正确率（0-100）
     */
    private Double correctRate;

    /**
     * 时间限制（秒，可选）
     */
    private Integer timeLimit;

    /**
     * 实际用时（秒）
     */
    private Integer timeSpent;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;
}
