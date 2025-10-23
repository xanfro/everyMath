package com.example.controller;

import com.example.pojo.common.Result;
import com.example.pojo.dto.UpdateGradeDTO;
import com.example.pojo.dto.UpdateUsernameDTO;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.vo.UserLoginVO;
import com.example.pojo.vo.UserVO;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器（简化版 - 只保留登录和注册）
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        try {
            UserVO userVO = userService.register(userRegisterDTO);
            return Result.success("注册成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            UserLoginVO loginVO = userService.login(userLoginDTO);
            return Result.success("登录成功", loginVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改用户名
     */
    @PutMapping("/username/{userId}")
    public Result<UserVO> updateUsername(@PathVariable String userId, @RequestBody UpdateUsernameDTO updateUsernameDTO) {
        try {
            UserVO userVO = userService.updateUsername(userId, updateUsernameDTO.getNewUsername());
            return Result.success("用户名修改成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改用户年级
     */
    @PutMapping("/grade/{userId}")
    public Result<UserVO> updateUserGrade(@PathVariable String userId, @RequestBody UpdateGradeDTO updateGradeDTO) {
        try {
            UserVO userVO = userService.updateUserGrade(userId, updateGradeDTO.getNewGrade());
            return Result.success("年级修改成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
