package com.example.pojo.vo;

import lombok.Data;

/**
 * 用户登录响应VO
 */
@Data
public class UserLoginVO {

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";
}
