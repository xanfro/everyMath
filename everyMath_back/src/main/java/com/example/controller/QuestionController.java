package com.example.controller;

import com.example.pojo.common.Result;
import com.example.pojo.entity.Question;
import com.example.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 根据习题集ID获取题目列表
     * @param questionSetId 习题集ID
     * @return 题目列表
     */
    @GetMapping("/question-set/{questionSetId}")
    public Result<List<Question>> getQuestionsByQuestionSetId(@PathVariable Integer questionSetId) {
        try {
            List<Question> questions = questionService.getQuestionsByQuestionSetId(questionSetId);
            return Result.success("查询题目列表成功", questions);
        } catch (Exception e) {
            return Result.error("查询题目列表失败: " + e.getMessage());
        }
    }
}

