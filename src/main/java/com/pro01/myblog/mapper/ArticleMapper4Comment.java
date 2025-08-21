package com.pro01.myblog.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ArticleMapper4Comment {

    @Select("SELECT status FROM articles WHERE id = #{id}")
    String findStatusById(@Param("id") Long id);

    @Update("UPDATE articles SET comment_count = comment_count + 1 WHERE id = #{id}")
    int incCommentCount(@Param("id") Long id);
}