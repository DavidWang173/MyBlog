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

        // 放行静态资源 / 非方法请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 放行 CORS 预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Method method = ((HandlerMethod) handler).getMethod();
        boolean needLogin = method.isAnnotationPresent(LoginRequired.class);
        boolean needAdmin = method.isAnnotationPresent(AdminRequired.class);

        // 取 token（支持 Authorization: Bearer xxx，或 query 参数 token）
        String token = extractToken(request);

        // —— 第一步：只要带了 token，就尽量解析并注入 attribute（即使接口不要求登录）——
        Long userId = null;
        String role = null;
        if (StringUtils.hasText(token)) {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            if (claims != null && claims.get("userId") != null) {
                userId = ((Number) claims.get("userId")).longValue();
                Object r = claims.get("role");
                role = (r == null ? null : r.toString());
                request.setAttribute("userId", userId);
                request.setAttribute("role", role);
            }
        }

        // —— 第二步：仅当接口需要权限时，才做强校验 ——
        if (needLogin || needAdmin) {
            if (!StringUtils.hasText(token)) {
                throw new UnauthorizedException("未登录，缺少 Token");
            }
            if (userId == null) {
                throw new UnauthorizedException("Token 无效或已过期");
            }
            if (needAdmin && !"ADMIN".equals(role)) {
                throw new ForbiddenException("权限不足，仅管理员可访问");
            }
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth)) {
            if (auth.startsWith("Bearer ")) {
                return auth.substring(7);
            }
            // 有些客户端直接塞 token，不带 Bearer，也兼容一下
            return auth.trim();
        }
        // 兼容 ?token=xxx 的场景（可选）
        String t = request.getParameter("token");
        return StringUtils.hasText(t) ? t.trim() : null;
    }
}