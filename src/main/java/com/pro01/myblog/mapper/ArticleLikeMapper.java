package com.pro01.myblog.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ArticleLikeMapper {

    // 点赞/取消点赞
    @Insert("INSERT IGNORE INTO article_likes (user_id, article_id) VALUES (#{userId}, #{articleId})")
    int insertLike(@Param("userId") Long userId, @Param("articleId") Long articleId);

    @Delete("DELETE FROM article_likes WHERE user_id = #{userId} AND article_id = #{articleId}")
    int deleteLike(@Param("userId") Long userId, @Param("articleId") Long articleId);


}
