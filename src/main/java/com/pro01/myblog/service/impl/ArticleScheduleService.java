package com.pro01.myblog.service.impl;

import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.pojo.Article;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleScheduleService {

    private final ArticleMapper articleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String REDIS_KEY = "article:hot";

    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    public void refreshHotArticles() {
        List<Article> articles = articleMapper.findAllPublished();
        if (articles == null || articles.isEmpty()) return;

        stringRedisTemplate.delete(REDIS_KEY);

        for (Article article : articles) {
            long score = article.getViewCount() * 1
                       + article.getLikeCount() * 3
                       + article.getCommentCount() * 5;
            stringRedisTemplate.opsForZSet().add(REDIS_KEY, String.valueOf(article.getId()), score);
        }
    }

    // 项目启动时初始化热榜
    @PostConstruct
    public void initHotRanking() {
        refreshHotArticles();
    }
}
