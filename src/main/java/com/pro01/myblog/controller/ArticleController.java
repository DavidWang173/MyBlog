package com.pro01.myblog.controller;

import com.pro01.myblog.dto.ArticleDetailDTO;
import com.pro01.myblog.dto.ArticlePublishDTO;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleService;
import com.pro01.myblog.utils.RequestUtil;
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
//    @PostMapping("/cover")
//    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
//        String url = articleService.uploadCover(file);
//        return Result.success(url);
//    }
    @PostMapping("/cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        // 1MB = 1024 * 1024 bytes
        long maxSize = 10 * 1024 * 1024; // 限制 10MB
        if (file.getSize() > maxSize) {
            return Result.error("图片大小不能超过 10MB");
        }
        Long userId = RequestUtil.getUserId(request);
        String url = articleService.uploadCover(file);
        return Result.success(url);
    }

    // 发布文章
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody ArticlePublishDTO dto, HttpServletRequest request) {
//        Long userId = TokenUtil.getUserId(request);
        Long userId = RequestUtil.getUserId(request);
        articleService.publishArticle(userId, dto);
        return Result.success();
    }

    // 查看文章详情
    @GetMapping("/{articleId}")
    public Result<ArticleDetailDTO> getArticleDetail(@PathVariable("articleId") Long id) {
        ArticleDetailDTO dto = articleService.getArticleDetail(id);
        return Result.success(dto);
    }
}