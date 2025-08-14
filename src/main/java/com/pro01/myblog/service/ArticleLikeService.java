package com.pro01.myblog.service;

import com.pro01.myblog.dto.LikeResponse;

public interface ArticleLikeService {

    // 点赞/取消点赞
    LikeResponse likeArticle(Long userId, Long articleId);
    LikeResponse unlikeArticle(Long userId, Long articleId);

}
