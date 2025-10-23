package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.QuestionSet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 习题集表 Mapper 接口
 */
@Mapper
public interface QuestionSetMapper extends BaseMapper<QuestionSet> {

}

