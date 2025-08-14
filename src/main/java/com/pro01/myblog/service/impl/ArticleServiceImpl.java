package com.pro01.myblog.service.impl;

import com.pro01.myblog.config.CoverProperties;
import com.pro01.myblog.dto.*;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.mapper.UserMapper;
import com.pro01.myblog.pojo.Article;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.pojo.User;
import com.pro01.myblog.service.ArticleService;
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
// ServiceImpl 里的核心逻辑
    @Override
    public ArticleDetailDTO getArticleDetail(Long articleId) {
        final String detailKey = "article:detail:" + articleId;     // 文章详情缓存（不含 viewCount）
        final String viewKey   = "article:view:" + articleId;        // 浏览量总数（仅此保存总量，不设 TTL）

        // 1) 先查详情缓存（不含 viewCount）
        Object cached = redisTemplate.opsForValue().get(detailKey);
        if (cached instanceof ArticleDetailDTO dto) {
            // 2) 初始化浏览量（仅首次：将 DB 基数放到 Redis；若已存在则不变）
            initViewCountIfAbsent(articleId, viewKey);

            // 3) 浏览量自增，直接用 INCR 的返回值作为最新总量
            Long total = stringRedisTemplate.opsForValue().increment(viewKey);
            dto.setViewCount(total != null ? total : 0L);

            return dto; // 命中缓存不查库
        }

        // 4) 未命中缓存：查询数据库（只查一次文章 + 作者）
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        User author = userMapper.findSimpleUserById(article.getUserId());
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }

        // 5) 组装 DTO（不把 viewCount 固化进缓存）
        ArticleDetailDTO dto = new ArticleDetailDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setSummary(article.getSummary());
        dto.setCategory(article.getCategory());
        dto.setCoverUrl(article.getCoverUrl());
        dto.setNickname(author.getNickname());
        dto.setAvatar(author.getAvatar());
        dto.setLikeCount(article.getLikeCount() != null ? article.getLikeCount() : 0L);
        dto.setCommentCount(article.getCommentCount() != null ? article.getCommentCount() : 0L);
        dto.setCreateTime(article.getCreateTime());

        // 6) 缓存详情（不含 viewCount），只缓存 30 分钟即可
        redisTemplate.opsForValue().set(detailKey, dto, 30, TimeUnit.MINUTES);

        // 7) 初始化浏览量总数（SETNX，用 DB 基数做起点）
        initViewCountIfAbsent(articleId, viewKey);

        // 8) 浏览量 +1，并把 INCR 的返回值作为最新总量回填
        Long total = stringRedisTemplate.opsForValue().increment(viewKey);
        dto.setViewCount(total != null ? total : 0L);

        return dto;
    }

    /**
     * 如果浏览量 key 不存在，则用数据库基数初始化一次（SETNX 语义）。
     * 注意：只做“缺失补齐”，不覆盖已有值。
     */
    private void initViewCountIfAbsent(Long articleId, String viewKey) {
        Boolean exists = stringRedisTemplate.hasKey(viewKey);
        if (Boolean.TRUE.equals(exists)) {
            return;
        }
        // 读一次 DB 的基数（用已有的 mapper），仅在 key 不存在时执行
        Article base = articleMapper.findById(articleId);
        long baseView = (base != null && base.getViewCount() != null) ? base.getViewCount() : 0L;
        // setIfAbsent = SETNX
        stringRedisTemplate.opsForValue().setIfAbsent(viewKey, String.valueOf(baseView));
    }

    // 查看文章列表
    @Override
    public PageResult<ArticleListDTO> getArticleList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleListDTO> dtoList = articleMapper.findArticles(offset, pageSize);
        long total = articleMapper.countArticles();

        return PageResult.of(total, dtoList, page, pageSize);
    }

    // 查看非置顶文章列表
    @Override
    public PageResult<ArticleListDTO> getNormalArticleList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleListDTO> dtoList = articleMapper.findNormalArticles(offset, pageSize);
        long total = articleMapper.countNormalArticles();

        return PageResult.of(total, dtoList, page, pageSize);
    }

    // 查看置顶文章列表
    @Override
    public PageResult<ArticleListDTO> getTopArticleList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleListDTO> dtoList = articleMapper.findTopArticles(offset, pageSize);
        long total = articleMapper.countTopArticles();

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
    public PageResult<ArticleRecommendDTO> getRecommendedArticles(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ArticleRecommendDTO> records = articleMapper.findRecommendedArticles(offset, pageSize);
        long total = articleMapper.countRecommendedArticles();
        return PageResult.of(total, records, page, pageSize);
    }

    // 置顶/取消置顶文章
    @Override
    public void updateTopStatus(Long articleId, boolean isTop) {
        Article article = articleMapper.findById(articleId);
        if (article == null || article.getStatus().equals("DELETED")) {
            throw new IllegalArgumentException("文章不存在或已删除");
        }
        articleMapper.updateTopStatus(articleId, isTop);
    }
}