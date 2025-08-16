package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.exception.UnauthorizedException;
import com.pro01.myblog.service.AiSummaryService;
import com.pro01.myblog.utils.RequestUtil; // 你项目已有的从请求里取 userId/role 的工具
import com.pro01.myblog.pojo.Result;   // 你项目的统一返回体
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiSummaryService aiSummaryService;

    @LoginRequired
    @PostMapping("/summary/{articleId}")
    public Result<?> generateSummary(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        if (userId == null) {
            throw new UnauthorizedException("登录以使用AI功能");
        }
        // 如需限制“只有作者或管理员可生成”，在此处做 author 校验或 role 校验即可。
        String summary = aiSummaryService.generateAndSave(articleId, userId);
        return Result.success(summary);
    }
}