package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 习题集实体类
 * 对应数据库表：question_sets
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("question_sets")
public class QuestionSet {

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
     * 习题集名称
     */
    @TableField("name")
    private String name;

    /**
     * 导入时间
     */
    @TableField(value = "import_time", fill = FieldFill.INSERT)
    private LocalDateTime importTime;

    /**
     * 题目数量
     */
    @TableField("question_count")
    private String questionCount;

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
