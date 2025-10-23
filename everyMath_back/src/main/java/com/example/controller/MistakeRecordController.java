package com.example.controller;

import com.example.pojo.common.Result;
import com.example.pojo.dto.MistakeRecordDTO;
import com.example.pojo.entity.MistakeRecord;
import com.example.service.MistakeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错题记录控制器
 */
@RestController
@RequestMapping("/api/mistake-records")
public class MistakeRecordController {

    @Autowired
    private MistakeRecordService mistakeRecordService;

    /**
     * 插入错题记录
     * @param mistakeRecordDTO 错题记录数据传输对象
     * @return 插入结果
     */
    @PostMapping
    public Result<String> saveMistakeRecord(@RequestBody MistakeRecordDTO mistakeRecordDTO) {
        try {
            mistakeRecordService.saveMistakeRecord(mistakeRecordDTO);
            return Result.success("错题记录保存成功");
        } catch (Exception e) {
            return Result.error("错题记录保存失败: " + e.getMessage());
        }
    }


    /**
     * 查询用户所有错题记录
     * @param userId 用户ID
     * @return 错题记录列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<MistakeRecord>> getMistakeRecordsByUserId(@PathVariable Integer userId) {
        try {
            List<MistakeRecord> records = mistakeRecordService.getMistakeRecordsByUserId(userId);
            return Result.success("查询错题记录成功", records);
        } catch (Exception e) {
            return Result.error("查询错题记录失败: " + e.getMessage());
        }
    }

    /**
     * 更新错题纠正状态
     * @param id 错题记录ID
     * @param corrected 纠正状态（0-未纠正，1-已纠正）
     * @return 更新结果
     */
    @PutMapping("/{id}/corrected")
    public Result<String> updateCorrectedStatus(@PathVariable Integer id, @RequestParam String corrected) {
        try {
            mistakeRecordService.updateCorrectedStatus(id, corrected);
            return Result.success("错题状态更新成功");
        } catch (Exception e) {
            return Result.error("错题状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除错题记录
     * @param id 错题记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteMistakeRecord(@PathVariable Integer id) {
        try {
            mistakeRecordService.deleteMistakeRecord(id);
            return Result.success("错题记录删除成功");
        } catch (Exception e) {
            return Result.error("错题记录删除失败: " + e.getMessage());
        }
    }
}
