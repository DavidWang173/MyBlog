package com.pro01.myblog.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class UserSqlProvider {
    public static String buildUpdateSql(Map<String, Object> params) {
        return new SQL() {{
            UPDATE("users");

            if (params.get("nickname") != null) {
                SET("nickname = #{nickname}");
            }
            if (params.get("signature") != null) {
                SET("signature = #{signature}");
            }

            SET("updated_at = NOW()");
            WHERE("id = #{userId}");
        }}.toString();
    }
}
