package com.pro01.myblog.service;

import com.pro01.myblog.dto.CommentCreateDTO;

public interface CommentService {
    // 发表评论
    Long createComment(Long userId, Long articleId, CommentCreateDTO dto);
}