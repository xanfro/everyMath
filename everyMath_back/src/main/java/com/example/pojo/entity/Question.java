package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目实体类
 * 对应数据库表：questions
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("questions")
public class Question {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 习题集ID，逻辑外键关联question_sets表
     */
    @TableField("question_set_id")
    private Integer questionSetId;

    /**
     * 题目内容（如：25 + 18）
     */
    @TableField("question")
    private String question;

    /**
     * 正确答案
     */
    @TableField("answer")
    private String answer;
}
