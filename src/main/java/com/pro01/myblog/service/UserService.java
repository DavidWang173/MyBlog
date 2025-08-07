package com.pro01.myblog.service;

import com.pro01.myblog.dto.*;

public interface UserService {
    // 用户注册
    void register(UserRegisterDTO dto);

    // 用户登录
    UserLoginResponse login(UserLoginDTO dto);

    // 修改个人信息
    void updateUserInfo(Long userId, UserUpdateDTO dto);

    // 查看个人信息
    UserInfoDTO getUserInfo(Long userId);
}