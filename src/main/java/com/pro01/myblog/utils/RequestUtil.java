package com.pro01.myblog.utils;

import jakarta.servlet.http.HttpServletRequest;

//public class RequestUtil {
//    public static Long getUserId(HttpServletRequest request) {
//        return (Long) request.getAttribute("userId");
//    }
//
//    public static String getRole(HttpServletRequest request) {
//        return (String) request.getAttribute("role");
//    }
//}
public class RequestUtil {

    public static Long getUserId(HttpServletRequest request) {
        Object v = request.getAttribute("userId");
        if (v == null) return null;
        if (v instanceof Long) return (Long) v;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRole(HttpServletRequest request) {
        Object v = request.getAttribute("role");
        return v == null ? null : v.toString();
    }

    // 便捷方法：判断管理员（大小写不敏感）
//    public static boolean isAdmin(HttpServletRequest request) {
//        String role = getRole(request);
//        return role != null && "ADMIN".equalsIgnoreCase(role);
//    }
}