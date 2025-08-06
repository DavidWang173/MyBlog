package com.pro01.myblog.utils;

import com.pro01.myblog.exception.ForbiddenException;
import com.pro01.myblog.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;


public class TokenUtil {

    public static Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Map<String, Object> claims = JwtUtil.parseToken(token);

        if (token == null || claims == null || !claims.containsKey("userId")) {
            throw new UnauthorizedException("无效的 Token，请重新登录");
        }
        return ((Number) claims.get("userId")).longValue();
    }

    public static String getRole(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Map<String, Object> claims = JwtUtil.parseToken(token);
        if (claims == null || claims.get("role") == null) {
            throw new ForbiddenException("无效的 Token，请重新登录");
        }
        return (String) claims.get("role");
    }
}

