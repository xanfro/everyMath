package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mapper.PracticeRecordMapper;
import com.example.pojo.dto.PracticeRecordDTO;
import com.example.pojo.entity.PracticeRecord;
import com.example.service.PracticeRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习记录服务实现类
 */
@Service
public class PracticeRecordServiceImpl implements PracticeRecordService {

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Override
    public void savePracticeRecord(PracticeRecordDTO practiceRecordDTO) {
        // 创建练习记录实体对象
        PracticeRecord practiceRecord = new PracticeRecord();
        
        // 复制DTO属性到实体对象
        BeanUtils.copyProperties(practiceRecordDTO, practiceRecord);
        
        // 将数值类型转换为字符串（按数据库设计）
        practiceRecord.setQuestionCount(String.valueOf(practiceRecordDTO.getQuestionCount()));
        practiceRecord.setCorrectCount(String.valueOf(practiceRecordDTO.getCorrectCount()));
        practiceRecord.setWrongCount(String.valueOf(practiceRecordDTO.getWrongCount()));
        practiceRecord.setCorrectRate(String.format("%.2f", practiceRecordDTO.getCorrectRate()));
        practiceRecord.setTimeSpent(String.valueOf(practiceRecordDTO.getTimeSpent()));
        
        // 处理时间限制（可选字段）
        if (practiceRecordDTO.getTimeLimit() != null) {
            practiceRecord.setTimeLimit(String.valueOf(practiceRecordDTO.getTimeLimit()));
        }
        
        // 设置创建时间（如果没有自动填充的话）
        practiceRecord.setCreatedTime(LocalDateTime.now());
        
        // 保存到数据库
        practiceRecordMapper.insert(practiceRecord);
    }

    @Override
    public List<PracticeRecord> getPracticeRecordsByUserId(Integer userId) {
        // 创建查询条件
        QueryWrapper<PracticeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("created_time"); // 按创建时间倒序排列，最新的在前面
        
        // 查询并返回结果
        return practiceRecordMapper.selectList(queryWrapper);
    }
}
