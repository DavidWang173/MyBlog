package com.pro01.myblog.controller;

import com.pro01.myblog.dto.UserLoginDTO;
import com.pro01.myblog.dto.UserLoginResponse;
import com.pro01.myblog.dto.UserRegisterDTO;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    // 用户登录
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody UserLoginDTO dto) {
        UserLoginResponse res = userService.login(dto);
        return Result.success(res);
    }
}