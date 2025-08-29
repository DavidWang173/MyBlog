package com.pro01.myblog.controller;

import com.pro01.myblog.service.CommentService;
import com.pro01.myblog.utils.RequestUtil;
import com.pro01.myblog.pojo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/comments/{commentId}")
    public Result<Void> adminDelete(@PathVariable Long commentId, HttpServletRequest request) {
        if (!"ADMIN".equalsIgnoreCase(RequestUtil.getRole(request))) {
            return Result.error("无权限");
        }
        commentService.adminDeleteComment(commentId);
        return Result.success();
    }
}
