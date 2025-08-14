package com.pro01.myblog.service.impl;

import com.pro01.myblog.dto.LikeResponse;
import com.pro01.myblog.mapper.ArticleLikeMapper;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.service.ArticleLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private String detailKey(Long articleId) { return "article:detail:" + articleId; }

    @Transactional
    @Override
    public LikeResponse likeArticle(Long userId, Long articleId) {
        int inserted = articleLikeMapper.insertLike(userId, articleId);
        boolean likedNow = inserted > 0;
        if (likedNow) {
            articleMapper.increaseLikeCount(articleId);
            stringRedisTemplate.delete(detailKey(articleId));
        }
        long likeCount = articleMapper.getLikeCount(articleId); // 直接查最新值
        return new LikeResponse(likeCount, true);
    }

    @Transactional
    @Override
    public LikeResponse unlikeArticle(Long userId, Long articleId) {
        int deleted = articleLikeMapper.deleteLike(userId, articleId);
        boolean unlikedNow = deleted > 0;
        if (unlikedNow) {
            articleMapper.decreaseLikeCount(articleId);
            stringRedisTemplate.delete(detailKey(articleId));
        }
        long likeCount = articleMapper.getLikeCount(articleId);
        return new LikeResponse(likeCount, false);
    }
}
