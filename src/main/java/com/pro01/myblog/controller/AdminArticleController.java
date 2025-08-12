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

    @AdminRequired
    @PostMapping("/recommend/{articleId}")
    public Result<?> recommend(@PathVariable Long articleId, HttpServletRequest request) {
        if (!"ADMIN".equalsIgnoreCase(RequestUtil.getRole(request))) {
            return Result.error("无权限");
        }
        boolean success = articleService.recommendArticle(articleId);
        return success ? Result.success() : Result.error("推荐失败：文章不存在或已删除");
    }

    @AdminRequired
    @PostMapping("/cancel-recommend/{articleId}")
    public Result<?> cancelRecommend(@PathVariable Long articleId, HttpServletRequest request) {
        if (!"ADMIN".equalsIgnoreCase(RequestUtil.getRole(request))) {
            return Result.error("无权限");
        }
        boolean success = articleService.cancelRecommendArticle(articleId);
        return success ? Result.success() : Result.error("取消推荐失败：文章不存在或已删除");
    }
}