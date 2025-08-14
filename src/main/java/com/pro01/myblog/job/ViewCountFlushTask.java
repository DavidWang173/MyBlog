// ViewCountFlushTask.java
package com.pro01.myblog.job;

import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.pojo.ArticleViewPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountFlushTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final ArticleMapper articleMapper;

    private static final String VIEW_KEY_PREFIX = "article:view:";
    private static final String DIRTY_SET_KEY   = "article:view:dirty";

    @Scheduled(cron = "0 */2 * * * ?")
    public void flushToDatabase() {
        long start = System.currentTimeMillis();
        try {
            // 1) 拿脏集合中的 ID
            Set<String> dirtyIds = stringRedisTemplate.opsForSet().members(DIRTY_SET_KEY);
            if (dirtyIds == null || dirtyIds.isEmpty()) return;

            // 2) pipeline 获取浏览量
            List<Object> rawValues = stringRedisTemplate.executePipelined((RedisConnection conn) -> {
                for (String id : dirtyIds) {
                    String key = VIEW_KEY_PREFIX + id;
                    conn.stringCommands().get(key.getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });

            // 3) 组装成 (id, viewCount)
            List<ArticleViewPair> pairs = new ArrayList<>(dirtyIds.size());
            int index = 0;
            for (String idStr : dirtyIds) {
                Object v = rawValues.get(index++);
                if (v == null) continue;
                String sval = (v instanceof byte[]) ? new String((byte[]) v, StandardCharsets.UTF_8) : String.valueOf(v);
                Long id = safeParseLong(idStr);
                Long count = safeParseLong(sval);
                if (id != null && count != null) {
                    pairs.add(new ArticleViewPair(id, count));
                }
            }

            if (pairs.isEmpty()) return;

            // 4) 分批落库
            final int BATCH = 500;
            for (int i = 0; i < pairs.size(); i += BATCH) {
                int end = Math.min(i + BATCH, pairs.size());
                List<ArticleViewPair> sub = pairs.subList(i, end);
                articleMapper.updateViewCountBatchCase(sub);
            }

            // 5) 清空脏集合
            stringRedisTemplate.delete(DIRTY_SET_KEY);

            long cost = System.currentTimeMillis() - start;
            log.info("[ViewFlush] flushed {} keys to DB in {} ms", pairs.size(), cost);
        } catch (Exception e) {
            log.error("[ViewFlush] flush failed", e);
        }
    }

    private Long safeParseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }
}