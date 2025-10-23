package com.example.service.impl;

import com.example.pojo.dto.QuestionSetImportDTO;
import com.example.service.FileParseService;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件解析服务实现类
 */
@Service
public class FileParseServiceImpl implements FileParseService {

    @Override
    public QuestionSetImportDTO parseFile(MultipartFile file, Integer userId, String fileExtension) throws Exception {
        System.out.println("开始解析文件: " + file.getOriginalFilename() + ", 扩展名: " + fileExtension + ", 用户ID: " + userId);
        switch (fileExtension.toLowerCase()) {
            case "csv":
                return parseCsvFile(file, userId);
            case "doc":
            case "docx":
                return parseWordFile(file, userId);
            default:
                throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
        }
    }

    @Override
    public QuestionSetImportDTO parseCsvFile(MultipartFile file, Integer userId) throws Exception {
        System.out.println("开始解析CSV文件");
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            List<String> lines = new ArrayList<>();
            String line;
            
            // 读取所有行
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
            
            System.out.println("CSV文件行数: " + lines.size());
            
            if (lines.isEmpty()) {
                throw new IllegalArgumentException("CSV文件为空");
            }
            
            // 解析第一行作为标题
            String firstLine = lines.get(0);
            String[] titleParts = firstLine.split(",", -1);
            String title = titleParts.length > 0 ? titleParts[0].trim() : "";
            
            if (title.isEmpty()) {
                title = "CSV题目集_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            }
            
            // 解析题目数据
            List<QuestionSetImportDTO.QuestionImportDTO> questions = new ArrayList<>();
            
            for (int i = 1; i < lines.size(); i++) {
                String questionLine = lines.get(i);
                String[] parts = questionLine.split(",", -1);
                
                if (parts.length >= 2) {
                    String question = parts[0].trim();
                    String answer = parts[1].trim();
                    
                    if (!question.isEmpty() && !answer.isEmpty()) {
                        QuestionSetImportDTO.QuestionImportDTO questionDTO = new QuestionSetImportDTO.QuestionImportDTO();
                        questionDTO.setQuestion(question);
                        questionDTO.setAnswer(answer);
                        questions.add(questionDTO);
                    }
                }
            }
            
            System.out.println("解析到题目数量: " + questions.size());
            
            if (questions.isEmpty()) {
                throw new IllegalArgumentException("CSV文件中没有找到有效的题目数据");
            }
            
            // 构建导入DTO
            QuestionSetImportDTO importDTO = new QuestionSetImportDTO();
            importDTO.setUserId(userId);
            importDTO.setName(title);
            importDTO.setQuestions(questions);
            
            return importDTO;
            
        } catch (IOException e) {
            throw new Exception("读取CSV文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public QuestionSetImportDTO parseWordFile(MultipartFile file, Integer userId) throws Exception {
        System.out.println("开始解析Word文件");
        String content = extractTextFromWord(file);
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Word文档内容为空");
        }
        
        // 按行分割内容
        String[] lines = content.split("\\r?\\n");
        List<String> validLines = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                validLines.add(line);
            }
        }
        
        System.out.println("Word文件行数: " + validLines.size());
        
        if (validLines.isEmpty()) {
            throw new IllegalArgumentException("Word文档中没有找到有效内容");
        }
        
        // 第一行作为标题
        String title = validLines.get(0);
        if (title.isEmpty()) {
            title = "Word题目集_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        }
        
        // 解析题目数据
        List<QuestionSetImportDTO.QuestionImportDTO> questions = new ArrayList<>();
        
        for (int i = 1; i < validLines.size(); i++) {
            String line = validLines.get(i);
            
            // 尝试解析题目格式：支持多种格式
            // 格式1: "1+1=2" 或 "1+1 = 2"
            // 格式2: "1+1,2" (逗号分隔)
            // 格式3: "1 + 1 = 2" (空格分隔)
            
            QuestionSetImportDTO.QuestionImportDTO questionDTO = parseQuestionLine(line);
            if (questionDTO != null) {
                questions.add(questionDTO);
            }
        }
        
        System.out.println("解析到题目数量: " + questions.size());
        
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Word文档中没有找到有效的题目数据");
        }
        
        // 构建导入DTO
        QuestionSetImportDTO importDTO = new QuestionSetImportDTO();
        importDTO.setUserId(userId);
        importDTO.setName(title);
        importDTO.setQuestions(questions);
        
        return importDTO;
    }
    
    /**
     * 从Word文档中提取文本内容
     * @param file Word文档文件
     * @return 提取的文本内容
     * @throws Exception 提取异常
     */
    private String extractTextFromWord(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名为空");
        }
        
        System.out.println("提取Word文档内容: " + filename);
        
        try (InputStream inputStream = file.getInputStream()) {
            if (filename.toLowerCase().endsWith(".docx")) {
                // 处理.docx文件
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                String content = extractor.getText();
                extractor.close();
                document.close();
                return content;
            } else if (filename.toLowerCase().endsWith(".doc")) {
                // 处理.doc文件
                HWPFDocument document = new HWPFDocument(inputStream);
                WordExtractor extractor = new WordExtractor(document);
                String content = extractor.getText();
                extractor.close();
                document.close();
                return content;
            } else {
                throw new IllegalArgumentException("不支持的Word文档格式");
            }
        } catch (IOException e) {
            throw new Exception("读取Word文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析单行题目数据
     * @param line 题目行
     * @return 解析后的题目数据，如果解析失败返回null
     */
    private QuestionSetImportDTO.QuestionImportDTO parseQuestionLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        line = line.trim();
        
        // 格式1: 逗号分隔 "1+1,2"
        if (line.contains(",")) {
            String[] parts = line.split(",", 2);
            if (parts.length == 2) {
                String question = parts[0].trim();
                String answer = parts[1].trim();
                if (!question.isEmpty() && !answer.isEmpty()) {
                    QuestionSetImportDTO.QuestionImportDTO questionDTO = new QuestionSetImportDTO.QuestionImportDTO();
                    questionDTO.setQuestion(question);
                    questionDTO.setAnswer(answer);
                    return questionDTO;
                }
            }
        }
        
        // 格式2: 等号分隔 "1+1=2" 或 "1+1 = 2"
        if (line.contains("=")) {
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String question = parts[0].trim();
                String answer = parts[1].trim();
                if (!question.isEmpty() && !answer.isEmpty()) {
                    QuestionSetImportDTO.QuestionImportDTO questionDTO = new QuestionSetImportDTO.QuestionImportDTO();
                    questionDTO.setQuestion(question);
                    questionDTO.setAnswer(answer);
                    return questionDTO;
                }
            }
        }
        
        // 格式3: 空格分隔，尝试识别数学表达式
        // 例如: "5 + 3 8" -> question: "5 + 3", answer: "8"
        String[] tokens = line.split("\\s+");
        if (tokens.length >= 3) {
            // 寻找最后一个可能是答案的数字
            for (int i = tokens.length - 1; i >= 2; i--) {
                if (isNumeric(tokens[i])) {
                    String question = String.join(" ", java.util.Arrays.copyOfRange(tokens, 0, i));
                    String answer = tokens[i];
                    QuestionSetImportDTO.QuestionImportDTO questionDTO = new QuestionSetImportDTO.QuestionImportDTO();
                    questionDTO.setQuestion(question);
                    questionDTO.setAnswer(answer);
                    return questionDTO;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 检查字符串是否为数字
     * @param str 字符串
     * @return 是否为数字
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}