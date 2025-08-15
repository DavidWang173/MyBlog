package com.pro01.myblog.service.impl;

import com.pro01.myblog.config.CoverProperties;
import com.pro01.myblog.dto.*;
import com.pro01.myblog.mapper.*;
import com.pro01.myblog.pojo.Article;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.pojo.User;
import com.pro01.myblog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.pro01.myblog.exception.UnauthorizedException;

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

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    private static final Set<String> VALID_CATEGORIES = Set.of("TECH", "LIFE", "MUSIC", "MOVIE", "NOTE", "FRIENDS");

    private String detailKey(Long id) { return "article:detail:" + id; }

    private static final String HOT_ZSET_KEY  = "article:hot";
    private static final String HOT_LIST_KEY  = "article:hot:list:top10"; // 最终列表缓存
    private static final long   HOT_LIST_TTL_SECONDS = 60;

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
//    @Override
//    public void publishArticle(Long userId, ArticlePublishDTO dto) {
//        String summary = dto.getSummary();
//        if (summary == null || summary.trim().isEmpty()) {
//            summary = dto.getContent().length() > 30
//                    ? dto.getContent().substring(0, 30)
//                    : dto.getContent();
//        }
//
//        Article article = new Article();
//        article.setUserId(userId);
//        article.setTitle(dto.getTitle());
//        article.setContent(dto.getContent());
//        article.setSummary(summary);
//        article.setCategory(dto.getCategory());
//        article.setCoverUrl(dto.getCoverUrl()); // 可为 null
//        article.setStatus("PUBLISHED");
//        article.setCreateTime(LocalDateTime.now());
//        article.setUpdateTime(LocalDateTime.now());
//
//        articleMapper.insertArticle(article);
//
//        // TODO: 之后这里可以调用 elasticService.indexArticle(article);
//    }
    @Override
    @Transactional
    public void publishArticle(Long userId, ArticlePublishDTO dto) {
        // 1) 摘要兜底
        String summary = dto.getSummary();
        if (summary == null || summary.trim().isEmpty()) {
            String content = dto.getContent() == null ? "" : dto.getContent();
            summary = content.length() > 30 ? content.substring(0, 30) : content;
        }

        // 2) 校验/归一化
        String category = dto.getCategory();
        if (category == null || !(category.equals("TECH") || category.equals("LIFE") || category.equals("NOTE"))) {
            throw new IllegalArgumentException("非法的category");
        }

        // 3) 插入文章
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(summary);
        article.setCategory(category);
        article.setCoverUrl(dto.getCoverUrl());
        article.setStatus("PUBLISHED");
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.insertArticle(article); // 回填 article.id

        // 4) 处理标签（可空）
        List<String> tagNames = dto.getTags();
        if (tagNames == null || tagNames.isEmpty()) return;

        // 去重、去空白
        tagNames = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(5) // 最多5个
                .toList();

        if (tagNames.isEmpty()) return;

        // 只允许系统内置标签；查出其ID
        List<Long> tagIds = tagMapper.findTagIdsByNames(tagNames);
        if (tagIds.isEmpty()) {
            // 全都不是系统标签，直接返回（或抛错，看你需求）
            return;
        }

        // 批量绑定（整新增，发布场景无“先删后插”）
        articleTagMapper.insertBatch(article.getId(), tagIds);
    }

    // 修改文章
    @Transactional
    @Override
    public void updateArticle(Long articleId, Long userId, ArticleUpdateDTO dto) {
        // 1) 查原文（不限定状态）
        Article origin = articleMapper.findByIdForUpdate(articleId);
        if (origin == null || "DELETED".equalsIgnoreCase(origin.getStatus())) {
            throw new IllegalArgumentException("文章不存在或已被删除");
        }
        if (!origin.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权修改此文章");
        }

        // 2) 规范化输入
        String inTitle   = trimToNull(dto.getTitle());
        String inContent = trimToNull(dto.getContent());
        String inSummary = dto.getSummary(); // 允许为空，后面处理
        String inCategory= trimToNull(dto.getCategory());
        String inCover   = dto.getCoverUrl(); // 保留原语义：null=不传；""=清空；"nochange"=保持

        // 3) 目标值：为空就继承原值
        String title   = defaultIfNull(inTitle, origin.getTitle());
        String content = defaultIfNull(inContent, origin.getContent());

        // summary：传了就用；没传或空就从 content 截取
        String summary = inSummary;
        if (summary == null || summary.trim().isEmpty()) {
            summary = content != null && content.length() > 120 ? content.substring(0, 120) : content;
        }

        // category：若传值则校验
        String category = defaultIfNull(inCategory, origin.getCategory());
        if (inCategory != null && !VALID_CATEGORIES.contains(inCategory)) {
            throw new IllegalArgumentException("非法的文章分类");
        }

        // coverUrl：遵循你的三态逻辑
        String coverUrl = origin.getCoverUrl();
        if (inCover != null) {
            if ("nochange".equalsIgnoreCase(inCover)) {
                // 保持原值
            } else if (inCover.trim().isEmpty()) {
                coverUrl = null; // 清空
            } else {
                coverUrl = inCover; // 新值
            }
        }

        // 4) 执行更新
        int updated = articleMapper.updateArticleById(articleId, title, content, summary, category, coverUrl);
        if (updated <= 0) {
            throw new RuntimeException("更新失败，请重试");
        }

        // ===== 5) 标签三态 =====
        List<String> inTags = dto.getTags(); // null / [] / ["Java","Redis",...]
        if (inTags != null) {
            if (inTags.isEmpty()) {
                // 清空
                articleTagMapper.deleteByArticleId(articleId);
            } else {
                // 替换：去空白、去重、限量（≤5），仅系统标签
                List<String> cleaned = inTags.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .limit(5)
                        .collect(Collectors.toList());

                if (cleaned.isEmpty()) {
                    articleTagMapper.deleteByArticleId(articleId);
                } else {
                    List<Long> tagIds = tagMapper.findSystemTagIdsByNames(cleaned);
                    if (tagIds.size() != cleaned.size()) {
                        // 有非法/不存在的标签名，策略二选一：
                        // 1) 严格：直接报错
                        throw new IllegalArgumentException("存在非法或非系统标签，请检查");
                        // 2) 宽松：过滤非法的（如需则改成过滤后再写库）
                    }
                    // 整替：先删后插
                    articleTagMapper.deleteByArticleId(articleId);
                    articleTagMapper.insertBatchForUpdate(articleId, tagIds);
                }
            }
        }

        // 6) 删缓存（最小集）
        stringRedisTemplate.delete(detailKey(articleId));

        stringRedisTemplate.delete("article:hot:list:top10");
        // 如有其他相关缓存（比如列表、分类页、热门榜），这里可以视情况追加删除：
        // stringRedisTemplate.delete("article:list:latest"); // 示例
        // stringRedisTemplate.opsForZSet().remove("article:hot:zset", articleId.toString());
    }

    // —— 小工具 —— //
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String defaultIfNull(String val, String fallback) {
        return val != null ? val : fallback;
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

        // ⭐ 新增：查标签名列表
        List<String> tagNames = tagMapper.findNamesByArticleId(articleId);
        dto.setTags(tagNames); // 允许为空列表

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
        copy.setTags(src.getTags()); // ⭐ 新增：把标签也写入缓存
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
        // 1) 先读最终列表缓存（超短缓存）
        Object cached = redisTemplate.opsForValue().get(HOT_LIST_KEY);
        if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ArticleHotDTO) {
            @SuppressWarnings("unchecked")
            List<ArticleHotDTO> hit = (List<ArticleHotDTO>) list;
            return hit;
        }

        // 2) 未命中：按 ZSET 排名取前 10 个 id
        Set<String> idSet = stringRedisTemplate.opsForZSet().reverseRange(HOT_ZSET_KEY, 0, 9);
        if (idSet == null || idSet.isEmpty()) {
            // 回写一个空列表也可（看你喜好）；这里直接返回空
            return Collections.emptyList();
        }

        // 3) 回源 DB 拉卡片
        List<Long> ids = idSet.stream().map(Long::valueOf).toList();
        List<ArticleHotDTO> rows = articleMapper.findHotArticlesByIds(ids);

        // 4) 按 ZSET 顺序重排
        Map<Long, ArticleHotDTO> map = rows.stream()
                .collect(Collectors.toMap(ArticleHotDTO::getId, a -> a));
        List<ArticleHotDTO> result = ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5) 写入 60s 超短缓存
        if (!result.isEmpty()) {
            redisTemplate.opsForValue().set(HOT_LIST_KEY, result, HOT_LIST_TTL_SECONDS, TimeUnit.SECONDS);
        } else {
            // 可选：缓存空列表 20-30s 防穿透（练手项目可忽略）
            // redisTemplate.opsForValue().set(HOT_LIST_KEY, Collections.emptyList(), 30, TimeUnit.SECONDS);
        }

        return result;
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