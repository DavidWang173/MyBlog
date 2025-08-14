package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.dto.*;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleService;
import com.pro01.myblog.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // 上传封面
    @LoginRequired
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
    @LoginRequired
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody ArticlePublishDTO dto, HttpServletRequest request) {
//        Long userId = TokenUtil.getUserId(request);
        Long userId = RequestUtil.getUserId(request);
        articleService.publishArticle(userId, dto);
        return Result.success();
    }

    // 修改文章
    @LoginRequired
    @PutMapping("/update/{articleId}")
    public Result<Void> updateArticle(@PathVariable Long articleId,
                                      @RequestBody ArticleUpdateDTO dto,
                                      HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        articleService.updateArticle(articleId, userId, dto);
        return Result.success();
    }

    // 查看文章详情
    @GetMapping("/{articleId}")
    public Result<ArticleDetailDTO> getArticleDetail(@PathVariable Long articleId,
                                                     HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request); // 未登录返回 null，自己已有工具就用自己的
        ArticleDetailDTO dto = articleService.getArticleDetail(articleId, userId);
        return Result.success(dto);
    }

    // 查看文章列表(全部)
    @GetMapping("/list")
    public Result<PageResult<ArticleListDTO>> list(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ArticleListDTO> result = articleService.getArticleList(page, pageSize);
        return Result.success(result);
    }

    // 查看非置顶文章列表
    @GetMapping("/list/normal")
    public Result<PageResult<ArticleListDTO>> listNormalArticles(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ArticleListDTO> result = articleService.getNormalArticleList(page, pageSize);
        return Result.success(result);
    }

    // 查看置顶文章列表
    @GetMapping("/list/top")
    public Result<PageResult<ArticleListDTO>> listTopArticles(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "3") int pageSize) {
        PageResult<ArticleListDTO> result = articleService.getTopArticleList(page, pageSize);
        return Result.success(result);
    }

    // 热门文章榜单
    @GetMapping("/popular")
    public Result<List<ArticleHotDTO>> getHotArticles() {
        return Result.success(articleService.getHotArticles());
    }

    // 用户删除文章
    @LoginRequired
    @DeleteMapping("/{articleId}")
    public Result<?> deleteMyArticle(@PathVariable("articleId") Long id, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        boolean success = articleService.deleteByUser(id, userId);
        return success ? Result.success() : Result.error("删除失败：无权限或文章不存在");
    }

    // 推荐列表
    @GetMapping("/recommend")
    public Result<PageResult<ArticleRecommendDTO>> getRecommendedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ArticleRecommendDTO> result = articleService.getRecommendedArticles(page, pageSize);
        return Result.success(result);
    }
}