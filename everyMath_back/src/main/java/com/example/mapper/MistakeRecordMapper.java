package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.MistakeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错题记录Mapper接口
 */
@Mapper
public interface MistakeRecordMapper extends BaseMapper<MistakeRecord> {
    // 继承BaseMapper，自动获得基础的CRUD方法
    // 如需自定义SQL，可在此添加方法
}
