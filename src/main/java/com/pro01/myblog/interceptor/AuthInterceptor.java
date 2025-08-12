package com.pro01.myblog.interceptor;

import com.pro01.myblog.annotation.AdminRequired;
import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.exception.ForbiddenException;
import com.pro01.myblog.exception.UnauthorizedException;
import com.pro01.myblog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Map;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 如果不是方法请求（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Method method = ((HandlerMethod) handler).getMethod();

        boolean needLogin = method.isAnnotationPresent(LoginRequired.class);
        boolean needAdmin = method.isAnnotationPresent(AdminRequired.class);

        // 如果不需要任何权限，直接放行
        if (!needLogin && !needAdmin) {
            return true;
        }

        // 获取 Token
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("未登录，缺少 Token");
        }

        // 解析 Token
        Map<String, Object> claims = JwtUtil.parseToken(token);
        if (claims == null || claims.get("userId") == null) {
            throw new UnauthorizedException("Token 无效或已过期");
        }

        // 提取 userId 和 role
        Long userId = ((Number) claims.get("userId")).longValue();
        String role = (String) claims.get("role");

        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        // 如果需要管理员权限但不是 ADMIN，抛异常
        if (needAdmin && !"ADMIN".equals(role)) {
            throw new ForbiddenException("权限不足，仅管理员可访问");
        }

        return true; // 放行
    }
}