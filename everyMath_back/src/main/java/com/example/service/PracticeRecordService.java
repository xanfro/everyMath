package com.example.service;

import com.example.pojo.dto.PracticeRecordDTO;
import com.example.pojo.entity.PracticeRecord;

import java.util.List;

/**
 * 练习记录服务接口
 */
public interface PracticeRecordService {

    /**
     * 保存练习记录
     * @param practiceRecordDTO 练习记录数据传输对象
     */
    void savePracticeRecord(PracticeRecordDTO practiceRecordDTO);

    /**
     * 根据用户ID查询所有练习记录
     * @param userId 用户ID
     * @return 练习记录列表
     */
    List<PracticeRecord> getPracticeRecordsByUserId(Integer userId);
}
