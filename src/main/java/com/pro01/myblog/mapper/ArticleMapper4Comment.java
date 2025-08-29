package com.pro01.myblog.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ArticleMapper4Comment {

    // 发表评论
    @Select("SELECT status FROM articles WHERE id = #{id}")
    String findStatusById(@Param("id") Long id);

    // 评论数+1
    @Update("UPDATE articles SET comment_count = comment_count + 1 WHERE id = #{id}")
    int incCommentCount(@Param("id") Long id);

    // 评论数-1，且不小于0
    @Update("UPDATE articles SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    int decCommentCount(@Param("id") Long articleId);
}