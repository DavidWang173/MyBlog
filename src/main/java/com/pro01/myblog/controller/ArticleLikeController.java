package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleLikeService;
import com.pro01.myblog.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article/like")
public class ArticleLikeController {

    @Autowired
    private ArticleLikeService articleLikeService;

    @LoginRequired
    @PostMapping("/{articleId}")
    public Result<?> like(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        articleLikeService.likeArticle(userId, articleId);
        return Result.success();
    }

    @LoginRequired
    @DeleteMapping("/{articleId}")
    public Result<?> unlike(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        articleLikeService.unlikeArticle(userId, articleId);
        return Result.success();
    }
}
