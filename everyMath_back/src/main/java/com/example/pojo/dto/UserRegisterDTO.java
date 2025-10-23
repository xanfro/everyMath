package com.example.pojo.dto;

import lombok.Data;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 年级
     */
    private String grade;
}
