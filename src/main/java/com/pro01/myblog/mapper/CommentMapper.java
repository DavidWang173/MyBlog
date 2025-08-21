package com.pro01.myblog.mapper;

import com.pro01.myblog.pojo.Comment;
import org.apache.ibatis.annotations.*;

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
}