package com.example.pojo.dto;

import lombok.Data;

/**
 * 错题记录数据传输对象
 * 用于接收前端提交的错题记录数据
 */
@Data
public class MistakeRecordDTO {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 题目内容
     */
    private String question;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 是否已纠正：1-已纠正，0-未纠正
     */
    private String corrected;
}