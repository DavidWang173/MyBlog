package com.pro01.myblog.service.impl;

import com.pro01.myblog.mapper.ArticleLikeMapper;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.service.ArticleLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    // 点赞
    @Override
    public void likeArticle(Long userId, Long articleId) {
        // 插入点赞记录（主键防重复）
        int inserted = articleLikeMapper.insertLike(userId, articleId);
        if (inserted > 0) {
            articleMapper.increaseLikeCount(articleId);
        }
    }

    // 取消点赞
    @Override
    public void unlikeArticle(Long userId, Long articleId) {
        int deleted = articleLikeMapper.deleteLike(userId, articleId);
        if (deleted > 0) {
            articleMapper.decreaseLikeCount(articleId);
        }
    }
}
