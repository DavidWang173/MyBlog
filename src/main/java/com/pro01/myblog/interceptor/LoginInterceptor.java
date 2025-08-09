package com.pro01.myblog.interceptor;

import com.pro01.myblog.exception.ForbiddenException;
import com.pro01.myblog.exception.UnauthorizedException;
import com.pro01.myblog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("未登录，缺少 Token");
        }

        Map<String, Object> claims = JwtUtil.parseToken(token);

        if (claims == null || claims.get("userId") == null) {
            throw new UnauthorizedException("Token 无效或已过期");
        }

        // 如果需要权限校验可以在这里判断
         if (!"ADMIN".equals(claims.get("role"))) {
             throw new ForbiddenException("权限不足");
         }

        request.setAttribute("userId", ((Number) claims.get("userId")).longValue());
        request.setAttribute("role", claims.get("role"));

        return true; // 放行
    }
}