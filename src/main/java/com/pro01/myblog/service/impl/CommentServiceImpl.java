package com.pro01.myblog.service.impl;

import com.pro01.myblog.dto.CommentCreateDTO;
import com.pro01.myblog.dto.CommentItemDTO;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.mapper.ArticleMapper4Comment;
import com.pro01.myblog.mapper.CommentMapper;
import com.pro01.myblog.pojo.Comment;
import com.pro01.myblog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper4Comment articleMapper4Comment; // 仅用于校验/计数

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 发表评论
    @Override
    @Transactional
    public Long createComment(Long userId, Long articleId, CommentCreateDTO dto) {
        if (userId == null) throw new IllegalArgumentException("未登录");
        if (articleId == null) throw new IllegalArgumentException("参数错误");

        String key = "comment:rate:" + userId;
        Long cnt = stringRedisTemplate.opsForValue().increment(key);
        if (cnt != null && cnt == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(30));
        }
        if (cnt != null && cnt > 5) {
            throw new IllegalArgumentException("评论太频繁，请稍后再试");
        }

        // 1) 校验文章必须存在且为 PUBLISHED
        String status = articleMapper4Comment.findStatusById(articleId);
        if (status == null || !"PUBLISHED".equals(status)) {
            throw new IllegalArgumentException("文章不存在或未发布");
        }

        // 2) 内容规范化与长度限制
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (!StringUtils.hasText(content) || content.length() > 1000) {
            throw new IllegalArgumentException("评论内容长度需在 1~1000 字之间");
        }

        // 3) 父评论校验（可选）
        Long parentId = dto.getParentId();
        if (parentId != null) {
            Comment parent = commentMapper.findBasicById(parentId);
            if (parent == null || Boolean.TRUE.equals(parent.getIsDeleted())) {
                throw new IllegalArgumentException("父评论不存在或已删除");
            }
            if (!articleId.equals(parent.getArticleId())) {
                throw new IllegalArgumentException("父评论不属于该文章");
            }
        }

        // 4) 入库
        Comment c = new Comment();
        c.setArticleId(articleId);
        c.setUserId(userId);
        c.setContent(content);
        c.setParentId(parentId);
        c.setIsDeleted(false);
        c.setIsPinned(false);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        commentMapper.insert(c); // 回填 id

        // 5) 评论数 +1
        articleMapper4Comment.incCommentCount(articleId);

        // 6) 失效文章详情缓存（让 comment_count 及时）
        stringRedisTemplate.delete("article:detail:" + articleId);

        return c.getId();
    }

    // 查看评论列表（正序+倒序）
    @Override
    public PageResult<CommentItemDTO> pageTopLevelAsc(Long articleId, Integer page, Integer size) {
        int current = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        int offset = (current - 1) * pageSize;

        long total = commentMapper.countTopLevel(articleId);
        List<CommentItemDTO> list = total == 0 ? List.of()
                : commentMapper.selectTopLevelAsc(articleId, pageSize, offset);
        return PageResult.of(total, list, current, pageSize);
    }

    @Override
    public PageResult<CommentItemDTO> pageTopLevelDesc(Long articleId, Integer page, Integer size) {
        int current = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        int offset = (current - 1) * pageSize;

        long total = commentMapper.countTopLevel(articleId);
        List<CommentItemDTO> list = total == 0 ? List.of()
                : commentMapper.selectTopLevelDesc(articleId, pageSize, offset);
        return PageResult.of(total, list, current, pageSize);
    }

    // 子评论列表（正序+倒序）
    @Override
    public PageResult<CommentItemDTO> pageRepliesAsc(Long parentCommentId, Integer page, Integer size) {
        return doPageReplies(parentCommentId, page, size, true);
    }

    @Override
    public PageResult<CommentItemDTO> pageRepliesDesc(Long parentCommentId, Integer page, Integer size) {
        return doPageReplies(parentCommentId, page, size, false);
    }

    private PageResult<CommentItemDTO> doPageReplies(Long parentCommentId, Integer page, Integer size, boolean asc) {
        if (parentCommentId == null || parentCommentId <= 0) {
            throw new IllegalArgumentException("参数错误");
        }
        // 校验父评论存在且未删除
        Comment parent = commentMapper.findBasicById(parentCommentId);
        if (parent == null || Boolean.TRUE.equals(parent.getIsDeleted())) {
            return PageResult.of(0, List.of(), page == null ? 1 : page, size == null ? 20 : size);
        }

        int current = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        int offset = (current - 1) * pageSize;

        long total = commentMapper.countByParent(parentCommentId);
        List<CommentItemDTO> list = List.of();
        if (total > 0) {
            list = asc
                    ? commentMapper.selectRepliesAsc(parentCommentId, pageSize, offset)
                    : commentMapper.selectRepliesDesc(parentCommentId, pageSize, offset);
        }
        return PageResult.of(total, list, current, pageSize);
    }
}