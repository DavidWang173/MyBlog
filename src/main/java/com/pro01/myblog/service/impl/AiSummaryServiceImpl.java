package com.pro01.myblog.service.impl;

import com.pro01.myblog.ai.dto.OpenAIChatReq;
import com.pro01.myblog.ai.dto.OpenAIChatResp;
import com.pro01.myblog.config.QwenProperties;
import com.pro01.myblog.mapper.AiArticleSummaryMapper;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.pojo.AiArticleSummary;
import com.pro01.myblog.pojo.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements com.pro01.myblog.service.AiSummaryService {

    private final WebClient qwenWebClient;
    private final QwenProperties qwenProperties;
    private final ArticleMapper articleMapper;
    private final AiArticleSummaryMapper aiArticleSummaryMapper;
    private final StringRedisTemplate stringRedisTemplate;

    // === 可按需调的参数 ===
    private static final Duration SUMMARY_TTL = Duration.ofDays(7);          // 缓存 7 天
    private static final Duration RL_USER_TTL = Duration.ofSeconds(60);      // 用户维度冷却
    private static final Duration RL_ARTICLE_TTL = Duration.ofSeconds(20);   // 文章维度冷却
    private static final Duration GEN_LOCK_TTL = Duration.ofSeconds(30);     // 生成分布式锁
    private static final Duration WAIT_EXISTING_GEN = Duration.ofSeconds(3); // 等待别人生成中的最长时间
    private static final long     WAIT_POLL_MS = 250;                         // 轮询间隔

    @Override
    public String generateAndSave(Long articleId, Long userId) {
        // 0) 查文章是否存在
        Article article = articleMapper.findById(articleId);
        if (article == null) throw new IllegalArgumentException("文章不存在");

        final String cacheKey     = "ai:summary:" + articleId;
        final String rlUserKey    = "rl:ai:sum:user:" + userId + ":" + articleId;
        final String rlArticleKey = "rl:ai:sum:article:" + articleId;
        final String lockKey      = "lock:ai:sum:" + articleId;

        // 1) 先查缓存（命中直接返回）
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isBlank()) return cached;

        // 2) 限流（用户→文章）
        if (!setNxWithTtl(rlUserKey, "1", RL_USER_TTL)) {
            throw new IllegalStateException("操作过于频繁，请稍后再试（用户维度限流）");
        }
        if (!setNxWithTtl(rlArticleKey, "1", RL_ARTICLE_TTL)) {
            // 文章维度被限流：等一等，看看别人是否刚好已生成
            String waitHit = waitForCache(cacheKey, WAIT_EXISTING_GEN, WAIT_POLL_MS);
            if (waitHit != null) return waitHit;
            throw new IllegalStateException("当前生成过于频繁，请稍后再试（文章维度限流）");
        }

        // 3) 分布式锁（防并发重复生成）
        boolean gotLock = setNxWithTtl(lockKey, String.valueOf(userId), GEN_LOCK_TTL);
        if (!gotLock) {
            String waitHit = waitForCache(cacheKey, WAIT_EXISTING_GEN, WAIT_POLL_MS);
            if (waitHit != null) return waitHit;
            throw new IllegalStateException("AI 摘要生成中，请稍后再试");
        }

        try {
            // 锁内再查一次缓存（有人可能刚写入）
            cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) return cached;

            // 4) 强制调用模型重新生成（关键改动：不再先读 DB）
            String summary = callModelToSummarize(article);

            if (summary == null || summary.isBlank()) {
                // 大模型异常/没 Key 等情况：回退到 DB 旧摘要（若有）
                AiArticleSummary dbOld = aiArticleSummaryMapper.findByArticleId(articleId);
                if (dbOld != null && dbOld.getAiSummary() != null && !dbOld.getAiSummary().isBlank()) {
                    // 旧摘要写回缓存（短一点的 TTL），避免页面空白
                    stringRedisTemplate.opsForValue().set(cacheKey, dbOld.getAiSummary(), Duration.ofHours(1));
                    return dbOld.getAiSummary();
                }
                // 再兜底：内容前缀，不落库
                summary = fallback(article.getContent(), 30);
                stringRedisTemplate.opsForValue().set(cacheKey, summary, Duration.ofMinutes(10));
                return summary;
            }

            // 5) 新摘要：落库 + 写缓存
            AiArticleSummary row = new AiArticleSummary();
            row.setArticleId(articleId);
            row.setAiSummary(summary);
            row.setModel(qwenProperties.getModel());
            aiArticleSummaryMapper.upsert(row);

            stringRedisTemplate.opsForValue().set(cacheKey, summary, SUMMARY_TTL);
            return summary;
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    /** 实际调用模型 */
    private String callModelToSummarize(Article article) {
        // APIKey 可能为空（你做了“弱依赖”），这里先判一下，避免 401 污染日志
        // 也可把这个判断挪到 Controller/Service 的更外层统一处理
        // 为空就直接给出友好提示，不走远端调用
        // 你也可以选择抛出业务异常，由全局异常处理返回 code/message
        // 这里返回 null，让外层走 fallback。
        // if (qwenProperties.getApiKey() == null || qwenProperties.getApiKey().isBlank()) return null;

        String system = "你是资深中文技术编辑，请基于用户提供的文章生成简明摘要：不超过120字，客观中立，不带口号或表情。";
        String user = "标题：《" + safe(article.getTitle()) + "》\n正文：\n" + clip(article.getContent(), 4000);

        OpenAIChatReq req = new OpenAIChatReq();
        req.setModel(qwenProperties.getModel());
        req.setMessages(List.of(
                new OpenAIChatReq.Message("system", system),
                new OpenAIChatReq.Message("user", user)
        ));
        req.setTemperature(0.2);
        req.setMax_tokens(256);

        OpenAIChatResp resp = qwenWebClient.post()
                .uri("/chat/completions")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(OpenAIChatResp.class)
                .block();

        return resp == null ? null : resp.firstContent();
    }

    /** Redis SETNX EX 封装 */
    private boolean setNxWithTtl(String key, String val, Duration ttl) {
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, val, ttl);
        return Boolean.TRUE.equals(ok);
    }

    /** 在给定时间内轮询一次缓存，命中则返回 */
    private String waitForCache(String cacheKey, Duration maxWait, long pollMs) {
        long deadline = System.currentTimeMillis() + maxWait.toMillis();
        while (System.currentTimeMillis() < deadline) {
            String v = stringRedisTemplate.opsForValue().get(cacheKey);
            if (v != null && !v.isBlank()) return v;
            try { Thread.sleep(pollMs); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    // ===== 工具方法（原样保留） =====

    private static String clip(String s, int maxChars) {
        if (s == null) return "";
        return s.length() <= maxChars ? s : s.substring(0, maxChars);
    }

    private static String fallback(String s, int n) {
        if (s == null || s.isBlank()) return "（暂无可生成的摘要）";
        String t = s.strip();
        return t.length() <= n ? t : t.substring(0, n);
    }

    private static String safe(String s) {
        if (s == null) return "";
        return new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}