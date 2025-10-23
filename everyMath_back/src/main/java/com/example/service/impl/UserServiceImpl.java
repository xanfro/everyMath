package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mapper.UserMapper;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.entity.User;
import com.example.pojo.vo.UserLoginVO;
import com.example.pojo.vo.UserVO;
import com.example.service.UserService;
import com.example.utils.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现类（使用MD5密码加密）
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserVO register(UserRegisterDTO userRegisterDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userRegisterDTO.getUsername());
        if (this.getOne(queryWrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户（使用MD5加密密码）
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(MD5Utils.encrypt(userRegisterDTO.getPassword())); // 使用MD5加密密码
        user.setGrade(userRegisterDTO.getGrade());
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        this.save(user);

        // 转换为VO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 查找用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userLoginDTO.getUsername());
        User user = this.getOne(queryWrapper);
        
        // 使用MD5验证密码
        if (user == null || !MD5Utils.verify(userLoginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成简单的token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 构建响应
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setUser(userVO);
        loginVO.setToken(token);
        
        return loginVO;
    }

    @Override
    public UserVO updateUsername(String userId, String newUsername) {
        // 检查新用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, newUsername);
        queryWrapper.ne(User::getId, userId); // 排除当前用户
        if (this.getOne(queryWrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 查找当前用户
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户名
        user.setUsername(newUsername);
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);

        // 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public UserVO updateUserGrade(String userId, Integer newGrade) {
        // 查找当前用户
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证年级范围
        if (newGrade == null || newGrade < 1 || newGrade > 6) {
            throw new RuntimeException("年级必须在1-6之间");
        }

        //将newGrade转换为字符串
        String newGradeStr = newGrade.toString();

        // 更新用户年级
        user.setGrade(newGradeStr);
        user.setUpdatedTime(LocalDateTime.now());
        this.updateById(user);

        // 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

}
