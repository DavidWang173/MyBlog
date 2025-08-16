package com.pro01.myblog.service;

import com.pro01.myblog.dto.ArticleListDTO;
import com.pro01.myblog.pojo.PageResult;

public interface ArticleFavoriteService {

    // 收藏
    void addFavorite(Long userId, Long articleId);

    // 取消收藏
    void cancelFavorite(Long userId, Long articleId);

    // 查看收藏列表
    PageResult<ArticleListDTO> listUserFavorites(Long userId, int page, int pageSize);
}