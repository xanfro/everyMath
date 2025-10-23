package com.example.pojo.dto;

import lombok.Data;
import java.util.List;

/**
 * 习题集导入数据传输对象
 */
@Data
public class QuestionSetImportDTO {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 习题集名称
     */
    private String name;

    /**
     * 题目列表
     */
    private List<QuestionImportDTO> questions;

    /**
     * 题目导入数据传输对象
     */
    @Data
    public static class QuestionImportDTO {
        /**
         * 题目内容
         */
        private String question;

        /**
         * 正确答案
         */
        private String answer;
    }
}

