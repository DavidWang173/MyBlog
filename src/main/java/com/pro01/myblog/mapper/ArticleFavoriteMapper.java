package com.pro01.myblog.mapper;

import com.pro01.myblog.dto.ArticleListDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleFavoriteMapper {

    // 收藏
    @Insert("""
        INSERT INTO article_favorites (user_id, article_id, is_deleted)
        VALUES (#{userId}, #{articleId}, FALSE)
        ON DUPLICATE KEY UPDATE
            is_deleted = FALSE,
            update_time = NOW()
    """)
    int insertOrRecover(@Param("userId") Long userId, @Param("articleId") Long articleId);

    // 取消收藏（软删除）
    @Update("""
        UPDATE article_favorites
        SET is_deleted = TRUE, update_time = NOW()
        WHERE user_id = #{userId}
          AND article_id = #{articleId}
          AND is_deleted = FALSE
    """)
    int softDelete(@Param("userId") Long userId, @Param("articleId") Long articleId);

    // 查看收藏列表
    @Select("""
        SELECT 
            a.id                                   AS id,
            a.title                                AS title,
            a.summary                              AS summary,
            a.category                             AS category,
            a.cover_url                            AS coverUrl,
            u.nickname                             AS nickname,
            u.avatar                               AS avatar,
            a.view_count                           AS viewCount,
            a.like_count                           AS likeCount,
            a.comment_count                        AS commentCount,
            a.create_time                          AS createTime
        FROM article_favorites f
        JOIN articles a ON f.article_id = a.id
        JOIN users u ON a.user_id = u.id
        WHERE f.user_id = #{userId}
          AND f.is_deleted = FALSE
          AND a.status = 'PUBLISHED'
        ORDER BY f.update_time DESC
        LIMIT #{offset}, #{limit}
        """)
    List<ArticleListDTO> findUserFavorites(@Param("userId") Long userId,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    @Select("""
        SELECT COUNT(1)
        FROM article_favorites f
        JOIN articles a ON f.article_id = a.id
        WHERE f.user_id = #{userId}
          AND f.is_deleted = FALSE
          AND a.status = 'PUBLISHED'
        """)
    long countUserFavorites(@Param("userId") Long userId);

    // 是否已收藏（查看文章详情时用）
    @Select("""
        SELECT EXISTS(
            SELECT 1 FROM article_favorites
            WHERE user_id = #{userId}
              AND article_id = #{articleId}
              AND is_deleted = FALSE
        )
    """)
    Boolean existsFavorite(@Param("userId") Long userId, @Param("articleId") Long articleId);
}