package com.pro01.myblog.interceptor;

import com.pro01.myblog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("未登录，缺少 Token");
            return false;
        }

        Map<String, Object> claims = JwtUtil.parseToken(token);

        if (claims == null || claims.get("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token 无效或已过期");
            return false;
        }

        // ✅ 将解析出的 userId 和 role 放到 request 作用域
        request.setAttribute("userId", ((Number) claims.get("userId")).longValue());
        request.setAttribute("role", claims.get("role"));

        return true; // 放行
    }
}