package com.pro01.myblog.service.impl;

import com.pro01.myblog.config.CoverProperties;
import com.pro01.myblog.dto.*;
import com.pro01.myblog.mapper.ArticleLikeMapper;
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

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

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
//        final String detailKey = "article:detail:" + articleId;
//        final String viewKey   = "article:view:" + articleId;
//        final String dirtySet  = "article:view:dirty";
//
//        // 1) 查缓存
//        Object cached = redisTemplate.opsForValue().get(detailKey);
//        if (cached instanceof ArticleDetailDTO dto) {
//            // 只在缓存命中时初始化一次 viewCount（使用 0 作为默认）
//            initViewCountIfAbsent(articleId, viewKey, 0L);
//
//            // 浏览量 +1
//            Long total = stringRedisTemplate.opsForValue().increment(viewKey);
//            if (total != null) dto.setViewCount(total);
//            // 加入脏集合
//            stringRedisTemplate.opsForSet().add(dirtySet, articleId.toString());
//            return dto;
//        }
//
//        // 2) 缓存 miss → 查数据库
//        Article article = articleMapper.findById(articleId);
//        if (article == null) throw new IllegalArgumentException("文章不存在");
//
//        User author = userMapper.findSimpleUserById(article.getUserId());
//        if (author == null) throw new IllegalArgumentException("作者不存在");
//
//        // 3) 组装 DTO（不含 viewCount）
//        ArticleDetailDTO dto = new ArticleDetailDTO();
//        dto.setId(article.getId());
//        dto.setTitle(article.getTitle());
//        dto.setContent(article.getContent());
//        dto.setSummary(article.getSummary());
//        dto.setCategory(article.getCategory());
//        dto.setCoverUrl(article.getCoverUrl());
//        dto.setNickname(author.getNickname());
//        dto.setAvatar(author.getAvatar());
//        dto.setLikeCount(article.getLikeCount() != null ? article.getLikeCount() : 0L);
//        dto.setCommentCount(article.getCommentCount() != null ? article.getCommentCount() : 0L);
//        dto.setCreateTime(article.getCreateTime());
//
//        // 4) 缓存详情（30分钟）
//        redisTemplate.opsForValue().set(detailKey, dto, 30, TimeUnit.MINUTES);
//
//        // 5) 初始化 viewCount（用 DB 值，避免重复查）
//        long baseView = (article.getViewCount() != null) ? article.getViewCount() : 0L;
//        initViewCountIfAbsent(articleId, viewKey, baseView);
//
//        // 6) 浏览量 +1
//        Long total = stringRedisTemplate.opsForValue().increment(viewKey);
//        dto.setViewCount(total != null ? total : baseView + 1);
//
//        // 7) 加入脏集合
//        stringRedisTemplate.opsForSet().add(dirtySet, articleId.toString());
//
//        return dto;
//    }
//
//    /**
//     * 原子初始化 viewCount（SETNX 语义）
//     */
//    private void initViewCountIfAbsent(Long articleId, String viewKey, long baseView) {
//        stringRedisTemplate.opsForValue().setIfAbsent(viewKey, String.valueOf(baseView));
//    }
    @Override
    public ArticleDetailDTO getArticleDetail(Long articleId, Long currentUserId) {
        final String detailKey = "article:detail:" + articleId;
        final String viewKey   = "article:view:" + articleId;
        final String dirtySet  = "article:view:dirty";

        System.out.println("article_detail_currentUserId = {" + currentUserId + "}");

        // 1) 查缓存
        Object cached = redisTemplate.opsForValue().get(detailKey);
        if (cached instanceof ArticleDetailDTO dto) {
            // 浏览量初始化（缺则补）
            initViewCountIfAbsent(articleId, viewKey, 0L);

            // 浏览量 +1
            Long total = stringRedisTemplate.opsForValue().increment(viewKey);
            if (total != null) dto.setViewCount(total);

            // 标记脏集合
            stringRedisTemplate.opsForSet().add(dirtySet, articleId.toString());

            // —— 返回前补 isLiked（不写回缓存）——
            dto.setIsLiked(resolveIsLiked(currentUserId, articleId));
            return dto;
        }

        // 2) 缓存 miss → 查数据库
        Article article = articleMapper.findById(articleId);
        if (article == null) throw new IllegalArgumentException("文章不存在");

        User author = userMapper.findSimpleUserById(article.getUserId());
        if (author == null) throw new IllegalArgumentException("作者不存在");

        // 3) 组装 DTO（含公共字段；isLiked 稍后补）
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

        // 4) 写缓存：用“无 isLiked 的副本”
        cacheDetailWithoutIsLiked(detailKey, dto);

        // 5) 初始化 viewCount（用 DB 值，避免重复查）
        long baseView = (article.getViewCount() != null) ? article.getViewCount() : 0L;
        initViewCountIfAbsent(articleId, viewKey, baseView);

        // 6) 浏览量 +1
        Long total = stringRedisTemplate.opsForValue().increment(viewKey);
        dto.setViewCount(total != null ? total : baseView + 1);

        // 7) 标记脏集合
        stringRedisTemplate.opsForSet().add(dirtySet, articleId.toString());

        // 8) 返回前补 isLiked（不入缓存）
        dto.setIsLiked(resolveIsLiked(currentUserId, articleId));

        return dto;
    }

    /** 原子初始化 viewCount（SETNX 语义） */
    private void initViewCountIfAbsent(Long articleId, String viewKey, long baseView) {
        stringRedisTemplate.opsForValue().setIfAbsent(viewKey, String.valueOf(baseView));
    }

    /** 实时计算 isLiked：未登录恒 false；已登录查关系表 */
    private boolean resolveIsLiked(Long currentUserId, Long articleId) {
        if (currentUserId == null) return false;
        Boolean liked = articleLikeMapper.existsLike(currentUserId, articleId);
        return Boolean.TRUE.equals(liked);
    }

    /** 写缓存时复制一份“无 isLiked”的 DTO */
    private void cacheDetailWithoutIsLiked(String key, ArticleDetailDTO src) {
        ArticleDetailDTO copy = new ArticleDetailDTO();
        copy.setId(src.getId());
        copy.setTitle(src.getTitle());
        copy.setContent(src.getContent());
        copy.setSummary(src.getSummary());
        copy.setCategory(src.getCategory());
        copy.setCoverUrl(src.getCoverUrl());
        copy.setNickname(src.getNickname());
        copy.setAvatar(src.getAvatar());
        copy.setViewCount(src.getViewCount());
        copy.setLikeCount(src.getLikeCount());
        copy.setCommentCount(src.getCommentCount());
        copy.setCreateTime(src.getCreateTime());
        // 不复制 isLiked

        redisTemplate.opsForValue().set(key, copy, 30, TimeUnit.MINUTES);
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