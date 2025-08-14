package com.pro01.myblog.utils;

public class KeyUtil {
    public static String articleDetailKey(Long id) { return "article:detail:" + id; }
    public static String articleViewKey(Long id)   { return "article:view:" + id;   }
    public static String articleBaseKey(Long id)   { return "article:view:base:" + id; }
}