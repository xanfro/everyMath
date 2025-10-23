package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.entity.User;
import com.example.pojo.vo.UserLoginVO;
import com.example.pojo.vo.UserVO;

/**
 * 用户服务接口（简化版 - 只保留登录和注册）
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterDTO 注册信息
     * @return 注册结果
     */
    UserVO register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     * @param userLoginDTO 登录信息
     * @return 登录结果
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 修改用户名
     * @param userId 用户ID
     * @param newUsername 新用户名
     * @return 更新后的用户信息
     */
    UserVO updateUsername(String userId, String newUsername);

    /**
     * 修改用户年级
     * @param userId 用户ID
     * @param newGrade 新年级
     * @return 更新后的用户信息
     */
    UserVO updateUserGrade(String userId, Integer newGrade);
}
