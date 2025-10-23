package com.example.service;

import com.example.pojo.entity.Question;
import java.util.List;

/**
 * 题目服务接口
 */
public interface QuestionService {

    /**
     * 根据习题集ID获取题目列表
     * @param questionSetId 习题集ID
     * @return 题目列表
     */
    List<Question> getQuestionsByQuestionSetId(Integer questionSetId);

    /**
     * 保存题目
     * @param question 题目对象
     * @return 保存后的题目
     */
    Question saveQuestion(Question question);

    /**
     * 批量保存题目
     * @param questions 题目列表
     */
    void batchSaveQuestions(List<Question> questions);
}

