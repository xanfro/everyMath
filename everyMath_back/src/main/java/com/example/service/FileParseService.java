package com.example.service;

import com.example.pojo.dto.QuestionSetImportDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件解析服务接口
 */
public interface FileParseService {
    
    /**
     * 解析上传的文件
     * @param file 上传的文件
     * @param userId 用户ID
     * @param fileExtension 文件扩展名
     * @return 解析后的题目集数据
     * @throws Exception 解析异常
     */
    QuestionSetImportDTO parseFile(MultipartFile file, Integer userId, String fileExtension) throws Exception;
    
    /**
     * 解析CSV文件
     * @param file CSV文件
     * @param userId 用户ID
     * @return 解析后的题目集数据
     * @throws Exception 解析异常
     */
    QuestionSetImportDTO parseCsvFile(MultipartFile file, Integer userId) throws Exception;
    
    /**
     * 解析Word文档
     * @param file Word文档
     * @param userId 用户ID
     * @return 解析后的题目集数据
     * @throws Exception 解析异常
     */
    QuestionSetImportDTO parseWordFile(MultipartFile file, Integer userId) throws Exception;
}
