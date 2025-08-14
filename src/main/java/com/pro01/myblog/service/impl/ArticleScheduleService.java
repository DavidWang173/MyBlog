package com.pro01.myblog.service.impl;

import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.pojo.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ArticleScheduleService {

    private final ArticleMapper articleMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_KEY = "article:hot";
    private static final String HOT_LIST_KEY = "article:hot:list:top10";
    private static final Duration TTL = Duration.ofMinutes(10);

    @Scheduled(cron = "0 */5 * * * ?")
    public void refreshHotArticles() {
        List<Article> articles = articleMapper.findAllPublished();
        if (articles == null || articles.isEmpty()) {
            return; // 让旧榜依赖 TTL 自然过期
        }

        final String tmpKey = REDIS_KEY + ":tmp";
        stringRedisTemplate.delete(tmpKey);

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var s = stringRedisTemplate.getStringSerializer();
            byte[] tmp = s.serialize(tmpKey);
            for (Article a : articles) {
                double score = computeScore(a);
                connection.zAdd(tmp, score, s.serialize(String.valueOf(a.getId())));
            }
            return null;
        });

        stringRedisTemplate.expire(tmpKey, TTL);

        stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            var s = stringRedisTemplate.getStringSerializer();
            connection.rename(s.serialize(tmpKey), s.serialize(REDIS_KEY));
            return null;
        });

        // ✅ 刷新成功后，把最终列表的超短缓存也删掉
        stringRedisTemplate.delete(HOT_LIST_KEY);
    }

    @PostConstruct
    public void initHotRanking() {
        refreshHotArticles();
    }

    private double computeScore(Article article) {
        long view = article.getViewCount() == null ? 0L : article.getViewCount();
        long like = article.getLikeCount() == null ? 0L : article.getLikeCount();
        long comment = article.getCommentCount() == null ? 0L : article.getCommentCount();
        return view * 1.0 + like * 3.0 + comment * 5.0;
    }
}