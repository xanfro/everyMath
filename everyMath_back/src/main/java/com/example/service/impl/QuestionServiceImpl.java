package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mapper.QuestionMapper;
import com.example.pojo.entity.Question;
import com.example.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目服务实现类
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<Question> getQuestionsByQuestionSetId(Integer questionSetId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("question_set_id", questionSetId);
        return questionMapper.selectList(queryWrapper);
    }

    @Override
    public Question saveQuestion(Question question) {
        if (question.getId() == null) {
            questionMapper.insert(question);
        } else {
            questionMapper.updateById(question);
        }
        return question;
    }

    @Override
    public void batchSaveQuestions(List<Question> questions) {
        for (Question question : questions) {
            questionMapper.insert(question);
        }
    }
}

