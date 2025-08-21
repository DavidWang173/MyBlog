package com.pro01.myblog.service;

import com.pro01.myblog.dto.CommentCreateDTO;
import com.pro01.myblog.dto.CommentItemDTO;
import com.pro01.myblog.pojo.PageResult;

public interface CommentService {
    // 发表评论
    Long createComment(Long userId, Long articleId, CommentCreateDTO dto);

    // 查看文章列表（正序+倒序）
    PageResult<CommentItemDTO> pageTopLevelAsc(Long articleId, Integer page, Integer size);
    PageResult<CommentItemDTO> pageTopLevelDesc(Long articleId, Integer page, Integer size);

}