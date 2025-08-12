package com.pro01.myblog.service.impl;

import com.pro01.myblog.config.CoverProperties;
import com.pro01.myblog.dto.ArticleDetailDTO;
import com.pro01.myblog.dto.ArticleHotDTO;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    // 发布文章
    @Override
    public void publishArticle(Long userId, ArticlePublishDTO dto) {
        String summary = dto.getSummary();
        if (summary == null || summary.trim().isEmpty()) {
            summary = dto.getContent().length() > 30
                    ? dto.getContent().substring(0, 30)
                    : dto.getContent();
        }

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

    // 查看文章详情
//    @Override
//    public ArticleDetailDTO getArticleDetail(Long articleId) {
//        String redisKey = "article:detail:" + articleId;
//
//        // 增加 Redis 浏览量计数
//        stringRedisTemplate.opsForValue().increment("article:view:" + articleId);
//
//        // 查缓存
//        Object cached = redisTemplate.opsForValue().get(redisKey);
//        if (cached != null && cached instanceof ArticleDetailDTO dto) {
//            // 获取 Redis 中实时浏览量
//            String redisView = stringRedisTemplate.opsForValue().get("article:view:" + articleId);
//            if (redisView != null) {
//                dto.setViewCount(Long.parseLong(redisView));
//            }
//            return dto;
//        }
//
//        // 查数据库
//        Article article = articleMapper.findById(articleId);
//        if (article == null) {
//            throw new IllegalArgumentException("文章不存在");
//        }
//
//        User author = userMapper.findSimpleUserById(article.getUserId());
//        if (author == null) {
//            throw new IllegalArgumentException("作者不存在");
//        }
//
//        ArticleDetailDTO dto = new ArticleDetailDTO();
//        dto.setId(article.getId());
//        dto.setTitle(article.getTitle());
//        dto.setContent(article.getContent());
//        dto.setSummary(article.getSummary());
//        dto.setCategory(article.getCategory());
//        dto.setCoverUrl(article.getCoverUrl());
//        dto.setNickname(author.getNickname());
//        dto.setAvatar(author.getAvatar());
//
//        // 从 Redis 获取实时浏览量（否则用数据库的）
//        String redisView = stringRedisTemplate.opsForValue().get("article:view:" + articleId);
//        dto.setViewCount(redisView != null ? Long.parseLong(redisView) : article.getViewCount());
//        dto.setLikeCount(article.getLikeCount() != null ? article.getLikeCount() : 0L);
//        dto.setCommentCount(article.getCommentCount() != null ? article.getCommentCount() : 0L);
//
//        dto.setCreateTime(article.getCreateTime());
//
//        // 写缓存（不包含 viewCount，viewCount 由 Redis 单独维护）
//        redisTemplate.opsForValue().set(redisKey, dto, 30, TimeUnit.MINUTES);
//
//        return dto;
//    }
    @Override
    public ArticleDetailDTO getArticleDetail(Long articleId) {
        String redisKey = "article:detail:" + articleId;

        // 增加 Redis 浏览量计数（增量部分）
        stringRedisTemplate.opsForValue().increment("article:view:" + articleId);

        // 查数据库（即使缓存命中也需要数据库的持久 view_count）
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        User author = userMapper.findSimpleUserById(article.getUserId());
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }

        // 构建 DTO
        ArticleDetailDTO dto = new ArticleDetailDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setSummary(article.getSummary());
        dto.setCategory(article.getCategory());
        dto.setCoverUrl(article.getCoverUrl());
        dto.setNickname(author.getNickname());
        dto.setAvatar(author.getAvatar());
        dto.setCreateTime(article.getCreateTime());

        // 计算 viewCount：数据库值 + Redis 增量
        Long dbView = article.getViewCount() != null ? article.getViewCount() : 0L;
        String redisView = stringRedisTemplate.opsForValue().get("article:view:" + articleId);
        Long redisViewLong = redisView != null ? Long.parseLong(redisView) : 0L;
        dto.setViewCount(dbView + redisViewLong);

        dto.setLikeCount(article.getLikeCount() != null ? article.getLikeCount() : 0L);
        dto.setCommentCount(article.getCommentCount() != null ? article.getCommentCount() : 0L);

        // 写缓存（不包含 viewCount，viewCount 是动态计算的）
        redisTemplate.opsForValue().set(redisKey, dto, 30, TimeUnit.MINUTES);

        return dto;
    }

    // 查看文章列表
    @Override
    public PageResult<ArticleListDTO> getArticleList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleListDTO> dtoList = articleMapper.findArticles(offset, pageSize);
        long total = articleMapper.countArticles();

        return PageResult.of(total, dtoList, page, pageSize);
    }

    // 热门文章榜单
    @Override
    public List<ArticleHotDTO> getHotArticles() {
        Set<String> idSet = stringRedisTemplate.opsForZSet()
                .reverseRange("article:hot", 0, 9);

        if (idSet == null || idSet.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = idSet.stream().map(Long::valueOf).collect(Collectors.toList());
        List<ArticleHotDTO> articles = articleMapper.findHotArticlesByIds(ids);

        // 保证返回顺序一致
        Map<Long, ArticleHotDTO> map = articles.stream()
                .collect(Collectors.toMap(ArticleHotDTO::getId, a -> a));

        return ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 删除文章
    @Override
    public boolean deleteByUser(Long articleId, Long userId) {
        Article article = articleMapper.findPublishedById(articleId);
        if (article == null || !article.getUserId().equals(userId)) {
            return false;
        }
        int rows = articleMapper.softDeleteArticle(articleId);
        clearArticleCache(articleId);
        return rows > 0;
    }

    @Override
    public boolean deleteByAdmin(Long articleId) {
        Article article = articleMapper.findPublishedById(articleId);
        if (article == null) {
            return false;
        }
        int rows = articleMapper.softDeleteArticle(articleId);
        clearArticleCache(articleId);
        return rows > 0;
    }

    private void clearArticleCache(Long articleId) {
        stringRedisTemplate.delete("article:view:" + articleId);
        redisTemplate.delete("article:detail:" + articleId);
        stringRedisTemplate.opsForZSet().remove("article:hot", String.valueOf(articleId));
    }

    // 推荐/取消推荐文章
    @Override
    public boolean recommendArticle(Long id) {
        if (articleMapper.checkArticleExists(id) == null) return false;
        return articleMapper.recommendArticle(id) > 0;
    }

    @Override
    public boolean cancelRecommendArticle(Long id) {
        if (articleMapper.checkArticleExists(id) == null) return false;
        return articleMapper.cancelRecommendArticle(id) > 0;
    }

    // 推荐列表
    @Override
    public PageResult<ArticleListDTO> getRecommendedArticles(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleListDTO> records = articleMapper.findRecommendedArticles(offset, pageSize);
        long total = articleMapper.countRecommendedArticles();
        return PageResult.of(total, records, page, pageSize);
    }
}