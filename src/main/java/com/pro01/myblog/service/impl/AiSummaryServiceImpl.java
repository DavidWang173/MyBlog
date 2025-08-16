package com.pro01.myblog.service.impl;

import com.pro01.myblog.ai.dto.OpenAIChatReq;
import com.pro01.myblog.ai.dto.OpenAIChatResp;
import com.pro01.myblog.config.QwenProperties;
import com.pro01.myblog.mapper.AiArticleSummaryMapper;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.pojo.AiArticleSummary;
import com.pro01.myblog.pojo.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements com.pro01.myblog.service.AiSummaryService {

    private final WebClient qwenWebClient;
    private final QwenProperties qwenProperties;
    private final ArticleMapper articleMapper;
    private final AiArticleSummaryMapper aiArticleSummaryMapper;

    @Override
    public String generateAndSave(Long articleId, Long userId) {
        // 1) 查文章（只要存在就行；你也可以限制 author==userId 或 status==PUBLISHED）
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        // 2) 构造 Prompt（剪裁正文，省 token）
        String system = "你是资深中文技术编辑，请基于用户提供的文章生成简明摘要：不超过120字，客观中立，不带口号或表情。";
        String user = "标题：《" + safe(article.getTitle()) + "》\n正文：\n" + clip(article.getContent(), 4000);

        OpenAIChatReq req = new OpenAIChatReq();
        req.setModel(qwenProperties.getModel());      // 例如 qwen-plus / qwen-flash / qwen-long
        req.setMessages(List.of(
                new OpenAIChatReq.Message("system", system),
                new OpenAIChatReq.Message("user", user)
        ));
        req.setTemperature(0.2);      // 更稳定
        req.setMax_tokens(256);       // 控制输出上限

        // 3) 调用阿里云 OpenAI 兼容接口 /chat/completions
        OpenAIChatResp resp = qwenWebClient.post()
                .uri("/chat/completions")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(OpenAIChatResp.class)
                .block();

        String summary = resp == null ? null : resp.firstContent();
        if (summary == null || summary.isBlank()) {
            // 兜底：取前30字，保证页面能展示（同时不写回 articles.summary）
            summary = fallback(article.getContent(), 30);
        }

        // 4) 落库到 ai_article_summaries（覆盖更新）
        AiArticleSummary row = new AiArticleSummary();
        row.setArticleId(articleId);
        row.setAiSummary(summary);
        row.setModel(qwenProperties.getModel());
        aiArticleSummaryMapper.upsert(row);

        return summary;
    }

    private static String clip(String s, int maxChars) {
        if (s == null) return "";
        // 以字符数裁剪，简单稳妥；如需更准可换 token 估算
        return s.length() <= maxChars ? s : s.substring(0, maxChars);
    }

    private static String fallback(String s, int n) {
        if (s == null || s.isBlank()) return "（暂无可生成的摘要）";
        String t = s.strip();
        return t.length() <= n ? t : t.substring(0, n);
    }

    private static String safe(String s) {
        if (s == null) return "";
        // 防止极端字符破坏 prompt
        return new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}