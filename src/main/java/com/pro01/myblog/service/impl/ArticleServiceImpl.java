package com.pro01.myblog.service.impl;

import com.pro01.myblog.config.CoverProperties;
import com.pro01.myblog.dto.ArticleDetailDTO;
import com.pro01.myblog.dto.ArticleListDTO;
import com.pro01.myblog.dto.ArticlePublishDTO;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.mapper.UserMapper;
import com.pro01.myblog.pojo.Article;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.pojo.User;
import com.pro01.myblog.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private CoverProperties coverProperties;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    // 上传头像
    @Override
    public String uploadCover(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;

            String realPath = new File("").getAbsolutePath() + File.separator + coverProperties.getUploadPath() + filename;
            File dest = new File(realPath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            return coverProperties.getAccessUrlPrefix() + filename;
        } catch (Exception e) {
            throw new RuntimeException("文章封面上传失败", e);
        }
    }

    @Override
    public void publishArticle(Long userId, ArticlePublishDTO dto) {
        String summary = dto.getSummary();
        if (summary == null || summary.trim().isEmpty()) {
            summary = dto.getContent().length() > 30
                    ? dto.getContent().substring(0, 30)
                    : dto.getContent();
        }

//        Article article = Article.builder()
//                .userId(userId)
//                .title(dto.getTitle())
//                .content(dto.getContent())
//                .summary(summary)
//                .category(dto.getCategory())
//                .coverUrl(dto.getCoverUrl()) // 可为 null
//                .status("PUBLISHED")
//                .createTime(LocalDateTime.now())
//                .updateTime(LocalDateTime.now())
//                .build();

        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(summary);
        article.setCategory(dto.getCategory());
        article.setCoverUrl(dto.getCoverUrl()); // 可为 null
        article.setStatus("PUBLISHED");
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        articleMapper.insertArticle(article);

        // TODO: 之后这里可以调用 elasticService.indexArticle(article);
    }

    @Override
    public ArticleDetailDTO getArticleDetail(Long articleId) {
        String redisKey = "article:detail:" + articleId;

        // 增加 Redis 浏览量计数
        stringRedisTemplate.opsForValue().increment("article:view:" + articleId);

        // 查缓存
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null && cached instanceof ArticleDetailDTO dto) {
            // 获取 Redis 中实时浏览量
            String redisView = stringRedisTemplate.opsForValue().get("article:view:" + articleId);
            if (redisView != null) {
                dto.setViewCount(Long.parseLong(redisView));
            }
            return dto;
        }

        // 查数据库
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        User author = userMapper.findSimpleUserById(article.getUserId());
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }

        ArticleDetailDTO dto = new ArticleDetailDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setSummary(article.getSummary());
        dto.setCategory(article.getCategory());
        dto.setCoverUrl(article.getCoverUrl());
        dto.setNickname(author.getNickname());
        dto.setAvatar(author.getAvatar());

        // 从 Redis 获取实时浏览量（否则用数据库的）
        String redisView = stringRedisTemplate.opsForValue().get("article:view:" + articleId);
        dto.setViewCount(redisView != null ? Long.parseLong(redisView) : article.getViewCount());

        dto.setCreateTime(article.getCreateTime());

        // 写缓存（不包含 viewCount，viewCount 由 Redis 单独维护）
        redisTemplate.opsForValue().set(redisKey, dto, 30, TimeUnit.MINUTES);

        return dto;
    }

    // 查看文章列表
    @Override
    public PageResult<ArticleListDTO> getArticleList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Article> articles = articleMapper.findArticles(offset, pageSize);
        long total = articleMapper.countArticles();

        List<ArticleListDTO> dtoList = new ArrayList<>();
        for (Article article : articles) {
            ArticleListDTO dto = new ArticleListDTO();
            dto.setId(article.getId());
            dto.setTitle(article.getTitle());
            dto.setSummary(article.getSummary());
            dto.setCategory(article.getCategory());
            dto.setCoverUrl(article.getCoverUrl());
            dto.setNickname(article.getNickname()); // 映射自 JOIN
            dto.setNickname(article.getAvatar());
            dto.setViewCount(article.getViewCount());
            dto.setCreateTime(article.getCreateTime());
            dtoList.add(dto);
        }

        return PageResult.of(total, dtoList, page, pageSize);
    }
}