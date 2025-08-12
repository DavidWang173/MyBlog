package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.AdminRequired;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleService;
import com.pro01.myblog.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/article")
public class AdminArticleController {

    @Autowired
    private ArticleService articleService;

    // 删除文章
    @AdminRequired
    @DeleteMapping("/{articleId}")
    public Result<?> deleteArticleByAdmin(@PathVariable Long articleId, HttpServletRequest request) {
        String role = RequestUtil.getRole(request);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return Result.error("无权限");
        }
        boolean success = articleService.deleteByAdmin(articleId);
        return success ? Result.success() : Result.error("删除失败");
    }

    // 推荐文章
    @AdminRequired
    @PostMapping("/recommend/{articleId}")
    public Result<?> recommend(@PathVariable Long articleId, HttpServletRequest request) {
        if (!"ADMIN".equalsIgnoreCase(RequestUtil.getRole(request))) {
            return Result.error("无权限");
        }
        boolean success = articleService.recommendArticle(articleId);
        return success ? Result.success() : Result.error("推荐失败：文章不存在或已删除");
    }

    // 取消推荐文章
    @AdminRequired
    @PostMapping("/cancel-recommend/{articleId}")
    public Result<?> cancelRecommend(@PathVariable Long articleId, HttpServletRequest request) {
        if (!"ADMIN".equalsIgnoreCase(RequestUtil.getRole(request))) {
            return Result.error("无权限");
        }
        boolean success = articleService.cancelRecommendArticle(articleId);
        return success ? Result.success() : Result.error("取消推荐失败：文章不存在或已删除");
    }

    // 置顶文章
    @AdminRequired
    @PostMapping("/top/{articleId}")
    public Result<?> setTop(@PathVariable Long articleId, HttpServletRequest request) {
        if (!"ADMIN".equalsIgnoreCase(RequestUtil.getRole(request))) {
            return Result.error("权限不足");
        }
        articleService.updateTopStatus(articleId, true);
        return Result.success("文章已置顶");
    }

    // 取消置顶文章
    @AdminRequired
    @PostMapping("/untop/{articleId}")
    public Result<?> cancelTop(@PathVariable Long articleId, HttpServletRequest request) {
        if (!"ADMIN".equalsIgnoreCase(RequestUtil.getRole(request))) {
            return Result.error("权限不足");
        }
        articleService.updateTopStatus(articleId, false);
        return Result.success("已取消置顶");
    }

}