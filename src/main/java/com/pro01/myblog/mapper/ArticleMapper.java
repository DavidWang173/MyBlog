package com.pro01.myblog.mapper;

import com.pro01.myblog.pojo.Article;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ArticleMapper {

    // 发布文章
    @Insert("INSERT INTO articles (user_id, title, content, summary, category, cover_url, status, create_time, update_time) " +
            "VALUES (#{userId}, #{title}, #{content}, #{summary}, #{category}, #{coverUrl}, 'PUBLISHED', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertArticle(Article article);

    // 查看文章详情
    @Select("SELECT * FROM articles WHERE id = #{id} AND status = 'PUBLISHED'")
    Article findById(@Param("id") Long id);

    // 记录浏览量
    @Update("UPDATE articles SET view_count = view_count + #{delta} WHERE id = #{id}")
    void updateViewCount(@Param("id") Long id, @Param("delta") Long delta);
}
