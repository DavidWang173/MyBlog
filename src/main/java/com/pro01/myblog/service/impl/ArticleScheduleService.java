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
    // TTL 兜底：略大于刷新周期，防止应用停机时旧榜长期挂着
    private static final Duration TTL = Duration.ofMinutes(10);

    @Scheduled(cron = "0 */5 * * * ?") // 每 5 分钟第 0 秒刷新
    public void refreshHotArticles() {
        List<Article> articles = articleMapper.findAllPublished();
        if (articles == null || articles.isEmpty()) {
            // 没有数据时不动旧榜：让旧榜继续可用，直到 TTL 过期
            return;
        }

        final String tmpKey = REDIS_KEY + ":tmp";

        // 1) 清理旧的临时键（如果有）
        stringRedisTemplate.delete(tmpKey);

        // 2) pipeline 批量写入临时键 ZSET
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var serializer = stringRedisTemplate.getStringSerializer();
            byte[] tmp = serializer.serialize(tmpKey);

            for (Article a : articles) {
                double score = computeScore(a);
                byte[] member = serializer.serialize(String.valueOf(a.getId()));
                connection.zAdd(tmp, score, member);
            }
            return null;
        });

        // 3) 先给临时键设置 TTL —— RENAME 会保留剩余 TTL 到目标键
        stringRedisTemplate.expire(tmpKey, TTL);

        // 4) 原子替换：RENAME tmp -> 正式键（无空窗、无半成品）
        stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            var serializer = stringRedisTemplate.getStringSerializer();
            byte[] tmp = serializer.serialize(tmpKey);
            byte[] dst = serializer.serialize(REDIS_KEY);
            connection.rename(tmp, dst); // 覆盖已存在的目标键
            return null;
        });
    }

    // 项目启动时初始化热榜
    @PostConstruct
    public void initHotRanking() {
        refreshHotArticles();
    }

    /** 你的打分逻辑：可按需调整/加时间衰减 */
    private double computeScore(Article article) {
        long view = article.getViewCount() == null ? 0L : article.getViewCount();
        long like = article.getLikeCount() == null ? 0L : article.getLikeCount();
        long comment = article.getCommentCount() == null ? 0L : article.getCommentCount();
        return view * 1.0 + like * 3.0 + comment * 5.0;
    }
}