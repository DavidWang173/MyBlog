package com.pro01.myblog.task;

import com.pro01.myblog.mapper.ArticleMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ScheduledTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ArticleMapper articleMapper;

    @Scheduled(fixedRate = 60 * 1000) // 每1分钟执行一次
    public void flushArticleViewsToDb() {
        Set<String> keys = stringRedisTemplate.keys("article:view:*");
        if (keys != null) {
            for (String key : keys) {
                String idStr = key.replace("article:view:", "");
                Long articleId = Long.parseLong(idStr);
                String viewStr = stringRedisTemplate.opsForValue().get(key);
                if (viewStr != null) {
                    Long viewCount = Long.parseLong(viewStr);
                    articleMapper.updateViewCount(articleId, viewCount);
                    stringRedisTemplate.delete(key);
                }
            }
        }
    }
}