package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.dto.ArticleListDTO;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleFavoriteService;
import com.pro01.myblog.utils.RequestUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class ArticleFavoriteController {

    @Autowired
    private ArticleFavoriteService articleFavoriteService;

    // 收藏
    @LoginRequired
    @PostMapping("/article/favorite/{articleId}")
    public Result<Void> addFavorite(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        articleFavoriteService.addFavorite(userId, articleId);
        return Result.success();
    }

    // 取消收藏
    @LoginRequired
    @DeleteMapping("/article/favorite/{articleId}")
    public Result<Void> cancelFavorite(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        articleFavoriteService.cancelFavorite(userId, articleId);
        return Result.success(); // 幂等：即使本就未收藏，也返回成功
    }

    // 查看收藏列表
    @LoginRequired
    @GetMapping("/article/favorite/list")
    public Result<PageResult<ArticleListDTO>> listMyFavorites(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        PageResult<ArticleListDTO> result = articleFavoriteService.listUserFavorites(userId, page, pageSize);
        return Result.success(result);
    }
}