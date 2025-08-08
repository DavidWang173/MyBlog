package com.pro01.myblog.controller;

import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleService;
import jakarta.annotation.Resource;
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
}