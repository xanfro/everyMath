package com.example.service;

import com.example.pojo.dto.MistakeRecordDTO;
import com.example.pojo.entity.MistakeRecord;

import java.util.List;

/**
 * 错题记录服务接口
 */
public interface MistakeRecordService {

    /**
     * 保存错题记录
     * @param mistakeRecordDTO 错题记录数据传输对象
     */
    void saveMistakeRecord(MistakeRecordDTO mistakeRecordDTO);


    /**
     * 根据用户ID查询所有错题记录
     * @param userId 用户ID
     * @return 错题记录列表
     */
    List<MistakeRecord> getMistakeRecordsByUserId(Integer userId);

    /**
     * 更新错题纠正状态
     * @param id 错题记录ID
     * @param corrected 纠正状态
     */
    void updateCorrectedStatus(Integer id, String corrected);

    /**
     * 删除错题记录
     * @param id 错题记录ID
     */
    void deleteMistakeRecord(Integer id);
}
