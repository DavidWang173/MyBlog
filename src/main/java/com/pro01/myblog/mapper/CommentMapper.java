package com.pro01.myblog.mapper;

import com.pro01.myblog.dto.CommentItemDTO;
import com.pro01.myblog.pojo.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 发表评论
    @Insert("""
        INSERT INTO comments (article_id, user_id, content, parent_id, is_deleted, is_pinned, create_time, update_time)
        VALUES (#{articleId}, #{userId}, #{content}, #{parentId}, #{isDeleted}, #{isPinned}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    // 仅用于父评论校验（最小字段集）
    @Select("""
        SELECT id, article_id AS articleId, is_deleted AS isDeleted
        FROM comments
        WHERE id = #{id}
        """)
    Comment findBasicById(@Param("id") Long id);

    // 查看评论列表（正序+倒序）
    @Select("""
        SELECT COUNT(*)
        FROM comments c
        WHERE c.article_id = #{articleId}
          AND c.is_deleted = FALSE
          AND c.parent_id IS NULL
        """)
    long countTopLevel(@Param("articleId") Long articleId);

    /** 顶层：置顶在前 + 时间正序，带回复数 */
    @Select("""
        SELECT
          c.id,
          c.user_id AS userId,
          u.nickname,
          u.avatar,
          c.content,
          c.is_pinned AS isPinned,
          c.create_time AS createTime,
          (SELECT COUNT(*) FROM comments r
             WHERE r.parent_id = c.id AND r.is_deleted = FALSE
          ) AS replyCount
        FROM comments c
        JOIN users u ON u.id = c.user_id
        WHERE c.article_id = #{articleId}
          AND c.is_deleted = FALSE
          AND c.parent_id IS NULL
        ORDER BY c.is_pinned DESC, c.create_time ASC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<CommentItemDTO> selectTopLevelAsc(@Param("articleId") Long articleId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    /** 顶层：置顶在前 + 时间倒序，带回复数 */
    @Select("""
        SELECT
          c.id,
          c.user_id AS userId,
          u.nickname,
          u.avatar,
          c.content,
          c.is_pinned AS isPinned,
          c.create_time AS createTime,
          (SELECT COUNT(*) FROM comments r
             WHERE r.parent_id = c.id AND r.is_deleted = FALSE
          ) AS replyCount
        FROM comments c
        JOIN users u ON u.id = c.user_id
        WHERE c.article_id = #{articleId}
          AND c.is_deleted = FALSE
          AND c.parent_id IS NULL
        ORDER BY c.is_pinned DESC, c.create_time DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<CommentItemDTO> selectTopLevelDesc(@Param("articleId") Long articleId,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    // 子评论列表（正序+倒序）
    @Select("""
        SELECT COUNT(*)
        FROM comments c
        WHERE c.parent_id = #{parentId}
          AND c.is_deleted = FALSE
        """)
    long countByParent(@Param("parentId") Long parentId);

    @Select("""
        SELECT
          c.id,
          c.user_id AS userId,
          u.nickname,
          u.avatar,
          c.content,
          c.is_pinned AS isPinned,
          c.create_time AS createTime
        FROM comments c
        JOIN users u ON u.id = c.user_id
        WHERE c.parent_id = #{parentId}
          AND c.is_deleted = FALSE
        ORDER BY c.is_pinned DESC, c.create_time ASC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<CommentItemDTO> selectRepliesAsc(@Param("parentId") Long parentId,
                                          @Param("limit") int limit,
                                          @Param("offset") int offset);

    @Select("""
        SELECT
          c.id,
          c.user_id AS userId,
          u.nickname,
          u.avatar,
          c.content,
          c.is_pinned AS isPinned,
          c.create_time AS createTime
        FROM comments c
        JOIN users u ON u.id = c.user_id
        WHERE c.parent_id = #{parentId}
          AND c.is_deleted = FALSE
        ORDER BY c.is_pinned DESC, c.create_time DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<CommentItemDTO> selectRepliesDesc(@Param("parentId") Long parentId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);
}