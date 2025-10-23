package com.example.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 年级
     */
    private String grade;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
