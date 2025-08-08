package com.pro01.myblog.controller;

import com.pro01.myblog.dto.*;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.UserService;
import com.pro01.myblog.utils.TokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
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

    // 修改个人信息
    @PostMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody UserUpdateDTO dto,
                                       HttpServletRequest request) {
        Long userId = TokenUtil.getUserId(request);
        userService.updateUserInfo(userId, dto);
        return Result.success();
    }

    // 查看个人信息
    @GetMapping("/info")
    public Result<UserInfoDTO> getUserInfo(HttpServletRequest request) {
        Long userId = TokenUtil.getUserId(request);
        UserInfoDTO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    // 上传头像
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) {
        Long userId = TokenUtil.getUserId(request);
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success(avatarUrl);
    }
}