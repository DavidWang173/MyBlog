package com.pro01.myblog.controller;

import com.pro01.myblog.dto.ArticlePublishDTO;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleService;
import com.pro01.myblog.utils.TokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // 上传封面
    @PostMapping("/cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        String url = articleService.uploadCover(file);
        return Result.success(url);
    }

    // 发布文章
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody ArticlePublishDTO dto, HttpServletRequest request) {
        Long userId = TokenUtil.getUserId(request);
        articleService.publishArticle(userId, dto);
        return Result.success();
    }
}