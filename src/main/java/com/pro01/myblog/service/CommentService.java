package com.pro01.myblog.service;

import com.pro01.myblog.dto.CommentCreateDTO;
import com.pro01.myblog.dto.CommentItemDTO;
import com.pro01.myblog.pojo.PageResult;

public interface CommentService {
    // 发表评论
    Long createComment(Long userId, Long articleId, CommentCreateDTO dto);

    // 查看评论列表（正序+倒序）
    PageResult<CommentItemDTO> pageTopLevelAsc(Long articleId, Integer page, Integer size);
    PageResult<CommentItemDTO> pageTopLevelDesc(Long articleId, Integer page, Integer size);

    // 查看子评论列表（正序+倒序）
    PageResult<CommentItemDTO> pageRepliesAsc(Long parentCommentId, Integer page, Integer size);
    PageResult<CommentItemDTO> pageRepliesDesc(Long parentCommentId, Integer page, Integer size);

    // 删除评论（用户自己删除）
    void deleteOwnComment(Long userId, Long commentId);
    // 删除评论（管理员删除）
    void adminDeleteComment(Long commentId);
}