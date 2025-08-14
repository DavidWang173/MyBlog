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

    // 每2分钟落库一次，按需调整
    @Scheduled(cron = "0 */2 * * * ?")
    public void flushToDatabase() {
        long start = System.currentTimeMillis();
        try {
            // 1) SCAN 拿到所有 article:view:* 的 key
            List<String> keys = scanKeys(VIEW_KEY_PREFIX + "*");
            if (CollectionUtils.isEmpty(keys)) {
                return;
            }

            // 2) pipeline 一次性取值
            List<Object> rawValues = stringRedisTemplate.executePipelined((RedisConnection conn) -> {
                for (String k : keys) {
                    conn.stringCommands().get(k.getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });

            // 3) 组装成 (id, viewCount)
            List<ArticleViewPair> pairs = new ArrayList<>(keys.size());
            for (int i = 0; i < keys.size(); i++) {
                String k = keys.get(i);
                Object v = rawValues.get(i);
                if (v == null) continue;
                String sval = (v instanceof byte[]) ? new String((byte[]) v, StandardCharsets.UTF_8) : String.valueOf(v);

                Long id = parseArticleId(k);
                Long count = safeParseLong(sval);
                if (id != null && count != null) {
                    pairs.add(new ArticleViewPair(id, count));
                }
            }

            if (pairs.isEmpty()) return;

            // 4) 分批落库（防止 SQL 过长；这里每 500 条一批）
            final int BATCH = 500;
            for (int i = 0; i < pairs.size(); i += BATCH) {
                int end = Math.min(i + BATCH, pairs.size());
                List<ArticleViewPair> sub = pairs.subList(i, end);
                articleMapper.updateViewCountBatchCase(sub);
            }

            long cost = System.currentTimeMillis() - start;
            log.info("[ViewFlush] flushed {} keys to DB in {} ms", pairs.size(), cost);
        } catch (Exception e) {
            log.error("[ViewFlush] flush failed", e);
        }
    }

    // 1) 非 pipeline 的 SCAN（显式使用 RedisCallback，避免 execute 重载二义性）
    private List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            // 注意：Cursor 需要 try-with-resources，并捕获 IOException
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    byte[] item = cursor.next();
                    keys.add(new String(item, StandardCharsets.UTF_8));
                }
            } catch (Exception e) { // IOException/Runtime 都兜
                throw new RuntimeException(e);
            }
            return null;
        });
        return keys;
    }

    private Long parseArticleId(String key) {
        try {
            int idx = key.lastIndexOf(':');
            return Long.parseLong(key.substring(idx + 1));
        } catch (Exception e) {
            return null;
        }
    }

    private Long safeParseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }
}