package com.pro01.myblog.mapper;

import com.pro01.myblog.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO users (username, password, nickname, avatar, signature, role, register_time, updated_at) " +
            "VALUES (#{username}, #{password}, #{nickname}, #{avatar}, #{signature}, #{role}, NOW(), NOW())")
    void insertUser(User user);
}