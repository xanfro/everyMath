package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.PracticeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 练习记录Mapper接口
 */
@Mapper
public interface PracticeRecordMapper extends BaseMapper<PracticeRecord> {
    // 继承BaseMapper，自动获得基础的CRUD方法
    // 如需自定义SQL，可在此添加方法
}
