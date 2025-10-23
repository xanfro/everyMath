package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mapper.QuestionMapper;
import com.example.mapper.QuestionSetMapper;
import com.example.pojo.dto.QuestionSetImportDTO;
import com.example.pojo.entity.Question;
import com.example.pojo.entity.QuestionSet;
import com.example.service.QuestionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 习题集服务实现类
 */
@Service
public class QuestionSetServiceImpl implements QuestionSetService {

    @Autowired
    private QuestionSetMapper questionSetMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<QuestionSet> getQuestionSetsByUserId(Integer userId) {
        QueryWrapper<QuestionSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("created_time"); // 按创建时间倒序排列
        return questionSetMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void deleteQuestionSet(Integer id) {
        // 先删除关联的题目
        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        questionQueryWrapper.eq("question_set_id", id);
        questionMapper.delete(questionQueryWrapper);
        
        // 再删除习题集
        questionSetMapper.deleteById(id);
    }

    @Override
    public QuestionSet getQuestionSetWithQuestions(Integer id) {
        // 获取习题集基本信息
        QuestionSet questionSet = questionSetMapper.selectById(id);
        
        if (questionSet != null) {
            // 获取关联的题目列表
            QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("question_set_id", id);
            List<Question> questions = questionMapper.selectList(queryWrapper);
            
            // 这里可以将题目列表设置到习题集对象中，但需要先在QuestionSet实体中添加questions字段
            // 暂时返回基本信息，题目列表可以通过单独的接口获取
        }
        
        return questionSet;
    }

    @Override
    public QuestionSet saveQuestionSet(QuestionSet questionSet) {
        if (questionSet.getId() == null) {
            // 新增
            questionSet.setCreatedTime(LocalDateTime.now());
            questionSet.setUpdatedTime(LocalDateTime.now());
            questionSetMapper.insert(questionSet);
        } else {
            // 更新
            questionSet.setUpdatedTime(LocalDateTime.now());
            questionSetMapper.updateById(questionSet);
        }
        return questionSet;
    }

    @Override
    @Transactional
    public QuestionSet importCsvQuestionSet(QuestionSetImportDTO importDTO) {
        // 1. 创建习题集
        QuestionSet questionSet = new QuestionSet();
        questionSet.setUserId(importDTO.getUserId());
        questionSet.setName(importDTO.getName());
        questionSet.setQuestionCount(String.valueOf(importDTO.getQuestions().size()));
        questionSet.setImportTime(LocalDateTime.now());
        questionSet.setCreatedTime(LocalDateTime.now());
        questionSet.setUpdatedTime(LocalDateTime.now());
        
        // 保存习题集
        questionSetMapper.insert(questionSet);
        
        // 2. 批量保存题目
        for (QuestionSetImportDTO.QuestionImportDTO questionDTO : importDTO.getQuestions()) {
            Question question = new Question();
            question.setQuestionSetId(questionSet.getId());
            question.setQuestion(questionDTO.getQuestion());
            question.setAnswer(questionDTO.getAnswer());
            
            questionMapper.insert(question);
        }
        
        return questionSet;
    }
}
