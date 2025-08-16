package com.pro01.myblog.service.impl;

import com.pro01.myblog.dto.ArticleListDTO;
import com.pro01.myblog.mapper.ArticleFavoriteMapper;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.pojo.Article;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.service.ArticleFavoriteService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleFavoriteServiceImpl implements ArticleFavoriteService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleFavoriteMapper articleFavoriteMapper;

    // 收藏
    @Transactional
    @Override
    public void addFavorite(Long userId, Long articleId) {
        // 1) 校验文章存在且为已发布
        Article article = articleMapper.findById(articleId);
        if (article == null || !"PUBLISHED".equals(article.getStatus())) {
            throw new IllegalArgumentException("文章不存在或未发布，无法收藏");
        }

        // 2) 幂等收藏：新增或恢复
        // INSERT ... ON DUPLICATE KEY UPDATE is_deleted = false
        int affected = articleFavoriteMapper.insertOrRecover(userId, articleId);
        // affected >= 1 即视为成功（插入或恢复）
        // 不需要返回 data
    }

    // 取消收藏
    @Transactional
    @Override
    public void cancelFavorite(Long userId, Long articleId) {
        // 软删除：将 is_deleted 置为 true；若本来就是 true 或不存在，受影响行数为 0，幂等返回即可
        articleFavoriteMapper.softDelete(userId, articleId);

        // 如果你后续引入了 Redis 做状态缓存（fav:user:{uid} 为 Set），可同步移除：
        // String key = "fav:user:" + userId;
        // stringRedisTemplate.opsForSet().remove(key, String.valueOf(articleId));
    }

    // 查看收藏列表
    @Override
    public PageResult<ArticleListDTO> listUserFavorites(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleListDTO> records = articleFavoriteMapper.findUserFavorites(userId, offset, pageSize);
        long total = articleFavoriteMapper.countUserFavorites(userId);
        return PageResult.of(total, records, page, pageSize);
    }
}