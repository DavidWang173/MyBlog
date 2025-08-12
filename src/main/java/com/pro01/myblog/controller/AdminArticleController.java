package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.AdminRequired;
import com.pro01.myblog.pojo.Result;
import com.pro01.myblog.service.ArticleService;
import com.pro01.myblog.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/article")
public class AdminArticleController {

    @Autowired
    private ArticleService articleService;

    @AdminRequired
    @DeleteMapping("/{id}")
    public Result<?> deleteArticleByAdmin(@PathVariable("id") Long id, HttpServletRequest request) {
        String role = RequestUtil.getRole(request);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return Result.error("无权限");
        }
        boolean success = articleService.deleteByAdmin(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}