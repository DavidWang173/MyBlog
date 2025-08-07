package com.pro01.myblog.service;

import com.pro01.myblog.dto.UserLoginDTO;
import com.pro01.myblog.dto.UserLoginResponse;
import com.pro01.myblog.dto.UserRegisterDTO;

public interface UserService {
    // 用户注册
    void register(UserRegisterDTO dto);

    // 用户登录
    UserLoginResponse login(UserLoginDTO dto);
}