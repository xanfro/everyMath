package com.example.controller;

import com.example.pojo.common.Result;
import com.example.pojo.dto.QuestionSetImportDTO;
import com.example.pojo.entity.QuestionSet;
import com.example.service.QuestionSetService;
import com.example.service.FileParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 习题集控制器
 */
@RestController
@RequestMapping("/api/question-sets")
public class QuestionSetController {

    @Autowired
    private QuestionSetService questionSetService;
    
    @Autowired
    private FileParseService fileParseService;

    /**
     * 获取用户的习题集列表
     * @param userId 用户ID
     * @return 习题集列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<QuestionSet>> getQuestionSetsByUserId(@PathVariable Integer userId) {
        try {
            List<QuestionSet> questionSets = questionSetService.getQuestionSetsByUserId(userId);
            return Result.success("查询习题集成功", questionSets);
        } catch (Exception e) {
            return Result.error("查询习题集失败: " + e.getMessage());
        }
    }

    /**
     * 删除习题集
     * @param id 习题集ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteQuestionSet(@PathVariable Integer id) {
        try {
            questionSetService.deleteQuestionSet(id);
            return Result.success("删除习题集成功");
        } catch (Exception e) {
            return Result.error("删除习题集失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取习题集详情（包含题目列表）
     * @param id 习题集ID
     * @return 习题集详情
     */
    @GetMapping("/{id}/details")
    public Result<QuestionSet> getQuestionSetDetails(@PathVariable Integer id) {
        try {
            QuestionSet questionSet = questionSetService.getQuestionSetWithQuestions(id);
            if (questionSet != null) {
                return Result.success("查询习题集详情成功", questionSet);
            } else {
                return Result.error("习题集不存在");
            }
        } catch (Exception e) {
            return Result.error("查询习题集详情失败: " + e.getMessage());
        }
    }


    /**
     * Base64文件上传接口 - 专为移动端Content URI问题设计
     * @param request 包含base64文件内容、文件名和用户ID的请求
     * @return 上传结果
     */
    @PostMapping("/upload-base64")
    public Result<Map<String, Object>> uploadBase64File(@RequestBody Map<String, Object> request) {
        System.out.println("收到Base64文件上传请求");
        
        try {
            // 参数验证
            String base64Content = (String) request.get("fileContent");
            String fileName = (String) request.get("fileName");
            Integer userId = (Integer) request.get("userId");
            
            System.out.println("文件名: " + fileName);
            System.out.println("用户ID: " + userId);
            System.out.println("Base64内容长度: " + (base64Content != null ? base64Content.length() : 0));
            
            if (base64Content == null || base64Content.trim().isEmpty()) {
                System.out.println("Base64文件内容为空");
                return Result.error("文件内容不能为空");
            }
            
            if (fileName == null || fileName.trim().isEmpty()) {
                System.out.println("文件名为空");
                return Result.error("文件名不能为空");
            }
            
            if (userId == null) {
                System.out.println("用户ID为空");
                return Result.error("用户ID不能为空");
            }
            
            // 获取文件扩展名
            String fileExtension = getFileExtension(fileName).toLowerCase();
            System.out.println("文件扩展名: " + fileExtension);
            
            // 验证文件类型
            if (!isValidFileType(fileExtension)) {
                System.out.println("不支持的文件类型: " + fileExtension);
                return Result.error("不支持的文件类型，仅支持CSV和Word文档(.csv, .doc, .docx)");
            }
            
            // 解码Base64内容
            System.out.println("开始解码Base64内容");
            byte[] fileBytes;
            try {
                // 移除可能的data URL前缀
                String cleanBase64 = base64Content;
                if (base64Content.contains(",")) {
                    cleanBase64 = base64Content.substring(base64Content.indexOf(",") + 1);
                }
                fileBytes = java.util.Base64.getDecoder().decode(cleanBase64);
                System.out.println("Base64解码成功，文件大小: " + fileBytes.length + " 字节");
            } catch (Exception e) {
                System.err.println("Base64解码失败: " + e.getMessage());
                return Result.error("文件内容格式错误");
            }
            
            // 验证文件大小（限制为10MB）
            if (fileBytes.length > 10 * 1024 * 1024) {
                System.out.println("文件过大: " + fileBytes.length);
                return Result.error("文件大小不能超过10MB");
            }
            
            // 创建临时MultipartFile对象
            System.out.println("创建临时文件对象");
            MultipartFile multipartFile = new Base64MultipartFile(fileBytes, fileName);
            
            // 解析文件内容
            System.out.println("开始解析文件内容");
            QuestionSetImportDTO importDTO = fileParseService.parseFile(multipartFile, userId, fileExtension);
            System.out.println("文件解析完成");
            
            // 导入到数据库
            System.out.println("开始导入数据库");
            QuestionSet questionSet = questionSetService.importCsvQuestionSet(importDTO);
            System.out.println("数据库导入完成，习题集ID: " + questionSet.getId());
            
            // 构建返回结果
            Map<String, Object> result = Map.of(
                "questionSetId", questionSet.getId(),
                "name", questionSet.getName(),
                "questionCount", questionSet.getQuestionCount(),
                "message", "Base64文件上传并解析成功"
            );
            
            System.out.println("Base64文件上传成功，返回结果: " + result);
            return Result.success("文件上传成功", result);
            
        } catch (Exception e) {
            System.err.println("Base64文件上传失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 通用文件上传接口
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 上传结果
     */
    @PostMapping("/upload-file")
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file, 
                                                  @RequestParam("userId") Integer userId) {
        System.out.println("收到文件上传请求");
        System.out.println("文件名: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("文件大小: " + (file != null ? file.getSize() : "null"));
        System.out.println("文件: " + file);
        System.out.println("用户ID: " + userId);
        
        try {
            // 参数验证
            if (file == null || file.isEmpty()) {
                System.out.println("上传文件为空");
                return Result.error("上传文件不能为空");
            }
            
            if (userId == null) {
                System.out.println("用户ID为空");
                return Result.error("用户ID不能为空");
            }
            
            // 获取文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                System.out.println("文件名为空");
                return Result.error("文件名不能为空");
            }
            
            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            System.out.println("文件扩展名: " + fileExtension);
            
            // 验证文件类型
            if (!isValidFileType(fileExtension)) {
                System.out.println("不支持的文件类型: " + fileExtension);
                return Result.error("不支持的文件类型，仅支持CSV和Word文档(.csv, .doc, .docx)");
            }
            
            // 验证文件大小（限制为10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                System.out.println("文件过大: " + file.getSize());
                return Result.error("文件大小不能超过10MB");
            }
            
            // 解析文件内容
            System.out.println("开始解析文件内容");
            QuestionSetImportDTO importDTO = fileParseService.parseFile(file, userId, fileExtension);
            System.out.println("文件解析完成");
            
            // 导入到数据库
            System.out.println("开始导入数据库");
            QuestionSet questionSet = questionSetService.importCsvQuestionSet(importDTO);
            System.out.println("数据库导入完成，习题集ID: " + questionSet.getId());
            
            // 构建返回结果
            Map<String, Object> result = Map.of(
                "questionSetId", questionSet.getId(),
                "name", questionSet.getName(),
                "questionCount", questionSet.getQuestionCount(),
                "message", "文件上传并解析成功"
            );
            
            System.out.println("文件上传成功，返回结果: " + result);
            return Result.success("文件上传成功", result);
            
        } catch (Exception e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace(); // 打印异常堆栈信息以便调试
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 验证文件类型是否支持
     * @param extension 文件扩展名
     * @return 是否支持
     */
    private boolean isValidFileType(String extension) {
        return "csv".equals(extension) || "doc".equals(extension) || "docx".equals(extension);
    }
    
    /**
     * Base64 MultipartFile 实现类
     */
    private static class Base64MultipartFile implements MultipartFile {
        private final byte[] fileContent;
        private final String fileName;
        
        public Base64MultipartFile(byte[] fileContent, String fileName) {
            this.fileContent = fileContent;
            this.fileName = fileName;
        }
        
        @Override
        public String getName() {
            return "file";
        }
        
        @Override
        public String getOriginalFilename() {
            return fileName;
        }
        
        @Override
        public String getContentType() {
            if (fileName.toLowerCase().endsWith(".csv")) {
                return "text/csv";
            } else if (fileName.toLowerCase().endsWith(".doc")) {
                return "application/msword";
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            return "application/octet-stream";
        }
        
        @Override
        public boolean isEmpty() {
            return fileContent == null || fileContent.length == 0;
        }
        
        @Override
        public long getSize() {
            return fileContent.length;
        }
        
        @Override
        public byte[] getBytes() {
            return fileContent;
        }
        
        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(fileContent);
        }
        
        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                fos.write(fileContent);
            }
        }
    }
}