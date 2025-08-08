package com.pro01.myblog.utils;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    public static Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    public static String getRole(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }
}