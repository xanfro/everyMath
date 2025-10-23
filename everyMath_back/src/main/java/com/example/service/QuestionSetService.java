package com.example.service;

import com.example.pojo.dto.QuestionSetImportDTO;
import com.example.pojo.entity.QuestionSet;
import java.util.List;

/**
 * 习题集服务接口
 */
public interface QuestionSetService {

    /**
     * 根据用户ID获取习题集列表
     * @param userId 用户ID
     * @return 习题集列表
     */
    List<QuestionSet> getQuestionSetsByUserId(Integer userId);

    /**
     * 删除习题集（同时删除关联的题目）
     * @param id 习题集ID
     */
    void deleteQuestionSet(Integer id);

    /**
     * 根据ID获取习题集详情（包含题目列表）
     * @param id 习题集ID
     * @return 习题集详情
     */
    QuestionSet getQuestionSetWithQuestions(Integer id);

    /**
     * 保存习题集
     * @param questionSet 习题集对象
     * @return 保存后的习题集
     */
    QuestionSet saveQuestionSet(QuestionSet questionSet);

    /**
     * 导入CSV习题集
     * @param importDTO 导入数据
     * @return 保存后的习题集
     */
    QuestionSet importCsvQuestionSet(QuestionSetImportDTO importDTO);
}
