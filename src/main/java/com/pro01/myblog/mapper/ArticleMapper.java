package com.pro01.myblog.mapper;

import com.pro01.myblog.dto.ArticleHotDTO;
import com.pro01.myblog.dto.ArticleListDTO;
import com.pro01.myblog.dto.ArticleRecommendDTO;
import com.pro01.myblog.pojo.Article;
import com.pro01.myblog.pojo.ArticleViewPair;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {

    // 发布文章
    @Insert("INSERT INTO articles (user_id, title, content, summary, category, cover_url, status, create_time, update_time) " +
            "VALUES (#{userId}, #{title}, #{content}, #{summary}, #{category}, #{coverUrl}, 'PUBLISHED', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertArticle(Article article);

    // 修改文章
    int updateArticleById(@Param("id") Long id,
                           @Param("title") String title,
                           @Param("content") String content,
                           @Param("summary") String summary,
                           @Param("category") String category,
                           @Param("coverUrl") String coverUrl);

    @Select("""
    SELECT id, user_id, title, content, summary, category, cover_url,
           IFNULL(view_count, 0) AS view_count,
           IFNULL(like_count, 0) AS like_count,
           IFNULL(comment_count, 0) AS comment_count,
           status, create_time, update_time
    FROM articles
    WHERE id = #{id}
""")
    Article findByIdForUpdate(@Param("id") Long id);

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

    // 更新文章浏览量(新)
    int updateViewCountBatchCase(@Param("list") List<ArticleViewPair> list);

    // 更新文章浏览量(已废弃)
    int updateViewCount(@Param("id") Long id, @Param("viewCount") Long viewCount);
    int updateViewCountBatch(@Param("list") List<ArticleViewPair> list);

    // 获取点赞数量
    @Select("SELECT IFNULL(like_count,0) FROM articles WHERE id = #{id}")
    Long getLikeCount(@Param("id") Long articleId);

    // 查看文章列表
    @Select("SELECT a.id, a.title, a.summary, a.category, a.cover_url, " +
            "a.view_count, a.like_count, a.comment_count, a.create_time, a.is_top, " +  // ✅ 添加 a.is_top
            "u.nickname, u.avatar " +
            "FROM articles a " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE a.status = 'PUBLISHED' " +
            "ORDER BY a.is_top DESC, a.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ArticleListDTO> findArticles(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED'")
    long countArticles();

    // 查看非置顶文章列表
    @Select("SELECT a.id, a.title, a.summary, a.category, a.cover_url, " +
            "a.view_count, a.like_count, a.comment_count, a.create_time, a.is_top, u.nickname, u.avatar " +
            "FROM articles a " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE a.status = 'PUBLISHED' AND a.is_top = FALSE " +
            "ORDER BY a.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ArticleListDTO> findNormalArticles(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED' AND is_top = FALSE")
    long countNormalArticles();

    // 查看置顶文章列表
    @Select("SELECT a.id, a.title, a.summary, a.category, a.cover_url, " +
            "a.view_count, a.like_count, a.comment_count, a.create_time, a.is_top, u.nickname, u.avatar " +
            "FROM articles a " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE a.status = 'PUBLISHED' AND a.is_top = TRUE " +
            "ORDER BY a.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ArticleListDTO> findTopArticles(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED' AND is_top = TRUE")
    long countTopArticles();

    // 热门文章榜单
    List<ArticleHotDTO> findHotArticlesByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED'")
    List<Article> findAllPublished();

    // 删除文章
    @Update("UPDATE articles SET status = 'DELETED' WHERE id = #{id}")
    int softDeleteArticle(@Param("id") Long id);

    @Select("SELECT * FROM articles WHERE id = #{id} AND status = 'PUBLISHED'")
    Article findPublishedById(@Param("id") Long id);

    // 推荐/取消推荐文章
    @Update("UPDATE articles SET is_recommend = TRUE WHERE id = #{id}")
    int recommendArticle(@Param("id") Long id);

    @Update("UPDATE articles SET is_recommend = FALSE WHERE id = #{id}")
    int cancelRecommendArticle(@Param("id") Long id);

    @Select("SELECT id FROM articles WHERE id = #{id} AND status = 'PUBLISHED'")
    Long checkArticleExists(@Param("id") Long id);

    // 推荐列表
    @Select("""
    SELECT 
        a.id, a.title, a.summary, a.category, a.cover_url,
        u.nickname, u.avatar,
        a.view_count, a.like_count, a.comment_count,
        a.create_time
    FROM articles a
    JOIN users u ON a.user_id = u.id
    WHERE a.is_recommend = TRUE AND a.status = 'PUBLISHED'
    ORDER BY a.create_time DESC
    LIMIT #{offset}, #{limit}
""")
    List<ArticleRecommendDTO> findRecommendedArticles(@Param("offset") int offset, @Param("limit") int limit);

    @Select("""
    SELECT COUNT(*) 
    FROM articles 
    WHERE is_recommend = TRUE AND status = 'PUBLISHED'
""")
    long countRecommendedArticles();

    // 置顶/取消置顶文章
    @Update("UPDATE articles SET is_top = #{isTop} WHERE id = #{id}")
    void updateTopStatus(@Param("id") Long id, @Param("isTop") boolean isTop);

    // 累加点赞数
    @Update("UPDATE articles SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(@Param("id") Long articleId);

    @Update("UPDATE articles SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decreaseLikeCount(@Param("id") Long articleId);

}
