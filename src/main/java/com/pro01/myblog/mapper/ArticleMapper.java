package com.pro01.myblog.mapper;

import com.pro01.myblog.pojo.Article;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ArticleMapper {

    @Insert("INSERT INTO articles (user_id, title, content, summary, category, cover_url, status, create_time, update_time) " +
            "VALUES (#{userId}, #{title}, #{content}, #{summary}, #{category}, #{coverUrl}, 'PUBLISHED', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertArticle(Article article);
}
