package com.pro01.myblog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//统一响应结果
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // 预定义状态码
    public static class StatusCode {
        public static final int SUCCESS = 200;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int SERVER_ERROR = 500;
        public static final int CUSTOM_ERROR = 600;
    }

    // 成功返回（带数据）
    public static <E> Result<E> success(E data) {
        return new Result<>(StatusCode.SUCCESS, "操作成功", data);
    }

    // 成功返回（无数据）
    public static Result<Void> success() {
        return new Result<>(StatusCode.SUCCESS, "操作成功", null);
    }

    // 失败返回（默认错误）
    public static <E> Result<E> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    // 失败返回（带泛型）
    public static <E> Result<E> error(String message) {
        return new Result<>(StatusCode.CUSTOM_ERROR, message, null);
    }
}
