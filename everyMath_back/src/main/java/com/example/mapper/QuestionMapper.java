package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目表 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

}
