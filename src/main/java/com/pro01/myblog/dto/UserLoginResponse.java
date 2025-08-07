package com.pro01.myblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponse {
    private String token;
    private String nickname;
    private String avatar;
    private String role;
}