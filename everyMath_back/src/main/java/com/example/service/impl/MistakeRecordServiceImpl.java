package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.mapper.MistakeRecordMapper;
import com.example.pojo.dto.MistakeRecordDTO;
import com.example.pojo.entity.MistakeRecord;
import com.example.service.MistakeRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题记录服务实现类
 */
@Service
public class MistakeRecordServiceImpl implements MistakeRecordService {

    @Autowired
    private MistakeRecordMapper mistakeRecordMapper;

    @Override
    public void saveMistakeRecord(MistakeRecordDTO mistakeRecordDTO) {
        // 创建错题记录实体对象
        MistakeRecord mistakeRecord = new MistakeRecord();
        
        // 复制DTO属性到实体对象
        BeanUtils.copyProperties(mistakeRecordDTO, mistakeRecord);
        
        // 设置创建时间和更新时间
        mistakeRecord.setCreatedTime(LocalDateTime.now());
        mistakeRecord.setUpdatedTime(LocalDateTime.now());
        
        // 保存到数据库
        mistakeRecordMapper.insert(mistakeRecord);
    }


    @Override
    public List<MistakeRecord> getMistakeRecordsByUserId(Integer userId) {
        // 创建查询条件
        QueryWrapper<MistakeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("created_time"); // 按创建时间倒序排列，最新的在前面
        
        // 查询并返回结果
        return mistakeRecordMapper.selectList(queryWrapper);
    }

    @Override
    public void updateCorrectedStatus(Integer id, String corrected) {
        // 创建更新条件
        UpdateWrapper<MistakeRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                    .set("corrected", corrected)
                    .set("updated_time", LocalDateTime.now());
        
        // 执行更新
        mistakeRecordMapper.update(null, updateWrapper);
    }

    @Override
    public void deleteMistakeRecord(Integer id) {
        // 根据ID删除错题记录
        mistakeRecordMapper.deleteById(id);
    }
}
