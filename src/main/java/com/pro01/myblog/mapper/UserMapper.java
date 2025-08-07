package com.pro01.myblog.mapper;

import com.pro01.myblog.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    // 登录注册
    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO users (username, password, nickname, avatar, signature, role, register_time, updated_at) " +
            "VALUES (#{username}, #{password}, #{nickname}, #{avatar}, #{signature}, #{role}, NOW(), NOW())")
    void insertUser(User user);

    // 修改个人信息
    @UpdateProvider(type = UserSqlProvider.class, method = "buildUpdateSql")
    void updateUserFields(@Param("userId") Long userId,
                          @Param("nickname") String nickname,
                          @Param("signature") String signature);

    // 查看个人信息
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(@Param("id") Long id);
}