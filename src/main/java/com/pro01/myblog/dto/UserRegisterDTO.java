package com.pro01.myblog.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String nickname;
    private String captchaCode;
    private String captchaId; // UUID 用于验证码校验
}