package com.example.controller;

import com.example.pojo.common.Result;
import com.example.pojo.dto.PracticeRecordDTO;
import com.example.pojo.entity.PracticeRecord;
import com.example.service.PracticeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 练习记录控制器
 */
@RestController
@RequestMapping("/api/practice-records")
public class PracticeRecordController {

    @Autowired
    private PracticeRecordService practiceRecordService;

    /**
     * 保存练习记录
     * @param practiceRecordDTO 练习记录数据传输对象
     * @return 保存结果
     */
    @PostMapping("/save")
    public Result<String> savePracticeRecord(@RequestBody PracticeRecordDTO practiceRecordDTO) {
        try {
            practiceRecordService.savePracticeRecord(practiceRecordDTO);
            return Result.success("练习记录保存成功");
        } catch (Exception e) {
            return Result.error("练习记录保存失败: " + e.getMessage());
        }
    }

    /**
     * 查询当前用户所有练习记录
     * @param userId 用户ID
     * @return 练习记录列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<PracticeRecord>> getPracticeRecordsByUserId(@PathVariable Integer userId) {
        try {
            List<PracticeRecord> records = practiceRecordService.getPracticeRecordsByUserId(userId);
            return Result.success("查询练习记录成功", records);
        } catch (Exception e) {
            return Result.error("查询练习记录失败: " + e.getMessage());
        }
    }
}
