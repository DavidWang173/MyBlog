package com.pro01.myblog.utils;

import jakarta.servlet.http.HttpServletRequest;


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

}