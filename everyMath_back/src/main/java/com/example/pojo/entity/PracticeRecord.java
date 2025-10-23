package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 练习记录实体类
 * 对应数据库表：practice_records
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("practice_records")
public class PracticeRecord {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID，逻辑外键关联users表
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 练习年级
     */
    @TableField("grade")
    private String grade;

    /**
     * 题目总数
     */
    @TableField("question_count")
    private String questionCount;

    /**
     * 正确题目数
     */
    @TableField("correct_count")
    private String correctCount;

    /**
     * 错误题目数
     */
    @TableField("wrong_count")
    private String wrongCount;

    /**
     * 正确率
     */
    @TableField("correct_rate")
    private String correctRate;

    /**
     * 时间限制（秒）
     */
    @TableField("time_limit")
    private String timeLimit;

    /**
     * 实际用时（秒）
     */
    @TableField("time_spent")
    private String timeSpent;

    /**
     * 开始时间
     */
    @TableField(value = "start_time", fill = FieldFill.INSERT)
    private LocalDateTime startTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
