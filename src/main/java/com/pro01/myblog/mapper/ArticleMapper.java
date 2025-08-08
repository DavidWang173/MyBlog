package com.pro01.myblog.mapper;

import com.pro01.myblog.dto.ArticleHotDTO;
import com.pro01.myblog.dto.ArticleListDTO;
import com.pro01.myblog.pojo.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {

    // 发布文章
    @Insert("INSERT INTO articles (user_id, title, content, summary, category, cover_url, status, create_time, update_time) " +
            "VALUES (#{userId}, #{title}, #{content}, #{summary}, #{category}, #{coverUrl}, 'PUBLISHED', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertArticle(Article article);

    // 查看文章详情
    @Select("""
    SELECT id, user_id, title, content, summary, category, cover_url,
           is_top, is_recommend,
           IFNULL(view_count, 0) AS view_count,
           IFNULL(like_count, 0) AS like_count,
           IFNULL(comment_count, 0) AS comment_count,
           status, create_time, update_time
    FROM articles
    WHERE id = #{id} AND status = 'PUBLISHED'
""")
    Article findById(@Param("id") Long id);

    // 记录浏览量
    @Update("UPDATE articles SET view_count = view_count + #{delta} WHERE id = #{id}")
    void updateViewCount(@Param("id") Long id, @Param("delta") Long delta);

    // 查看文章列表
    @Select("SELECT a.id, a.title, a.summary, a.category, a.cover_url, " +
            "a.view_count, a.like_count, a.comment_count, a.create_time, u.nickname, u.avatar " +
            "FROM articles a " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE a.status = 'PUBLISHED' " +
            "ORDER BY a.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ArticleListDTO> findArticles(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED'")
    long countArticles();

}
