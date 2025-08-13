package com.pro01.myblog.service;

public interface ArticleLikeService {

    // 点赞/取消点赞
    void likeArticle(Long userId, Long articleId);
    void unlikeArticle(Long userId, Long articleId);

}
