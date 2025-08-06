package com.pro01.myblog.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    // JWT 密钥（建议放入配置文件）
    private static final String SECRET_KEY = "myblog_secret";

    // 过期时间（12 小时）
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 12;

    /**
     * 生成 JWT Token
     * @param claims 业务数据（例如用户 ID）
     * @return 生成的 JWT Token
     */
    public static String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    /**
     * 解析 JWT Token，返回业务数据
     * @param token JWT Token
     * @return 解析出的业务数据
     */
    public static Map<String, Object> parseToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token)
                    .getClaim("claims")
                    .asMap();
        } catch (Exception e) {
            return null; // Token 无效或过期
        }
    }
}
