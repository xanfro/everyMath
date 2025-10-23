package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 错题记录实体类
 * 对应数据库表：mistake_records
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("mistake_records")
public class MistakeRecord {

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
     * 题目内容
     */
    @TableField("question")
    private String question;

    /**
     * 正确答案
     */
    @TableField("correct_answer")
    private String correctAnswer;

    /**
     * 是否已纠正：1-已纠正，0-未纠正
     */
    @TableField("corrected")
    private String corrected;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
