package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.dto.LikeResponse;
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
    public Result<LikeResponse> like(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        return Result.success(articleLikeService.likeArticle(userId, articleId));
    }

    @LoginRequired
    @DeleteMapping("/{articleId}")
    public Result<LikeResponse> unlike(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        return Result.success(articleLikeService.unlikeArticle(userId, articleId));
    }
}
