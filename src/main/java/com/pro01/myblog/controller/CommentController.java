package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.dto.CommentCreateDTO;
import com.pro01.myblog.service.CommentService;
import com.pro01.myblog.utils.RequestUtil;
import com.pro01.myblog.pojo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 发表评论
    @LoginRequired
    @PostMapping("/article/{articleId}/comments")
    public Result<Long> create(@PathVariable Long articleId,
                               @RequestBody CommentCreateDTO dto,
                               HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        Long commentId = commentService.createComment(userId, articleId, dto);
        return Result.success(commentId);
    }
}