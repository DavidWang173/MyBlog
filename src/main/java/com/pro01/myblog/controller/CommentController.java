package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.dto.CommentCreateDTO;
import com.pro01.myblog.dto.CommentItemDTO;
import com.pro01.myblog.pojo.PageResult;
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

    /** 顶层评论：置顶在前 + 按时间正序（最早→最新） */
    @GetMapping("/comment/list-asc/{articleId}")
    public Result<PageResult<CommentItemDTO>> listAsc(@PathVariable Long articleId,
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(commentService.pageTopLevelAsc(articleId, page, pageSize));
    }

    /** 顶层评论：置顶在前 + 按时间倒序（最新→最早） */
    @GetMapping("/comment/list-desc/{articleId}")
    public Result<PageResult<CommentItemDTO>> listDesc(@PathVariable Long articleId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(commentService.pageTopLevelDesc(articleId, page, pageSize));
    }

    /** 子评论：置顶在前 + 时间正序 */
    @GetMapping("/comment/{commentId}/replies/asc")
    public Result<PageResult<CommentItemDTO>> repliesAsc(@PathVariable Long commentId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(commentService.pageRepliesAsc(commentId, page, pageSize));
    }

    /** 子评论：置顶在前 + 时间倒序 */
    @GetMapping("/comment/{commentId}/replies/desc")
    public Result<PageResult<CommentItemDTO>> repliesDesc(@PathVariable Long commentId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(commentService.pageRepliesDesc(commentId, page, pageSize));
    }

    // 用户删除评论
    @LoginRequired
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteOwn(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        commentService.deleteOwnComment(userId, commentId);
        return Result.success();
    }
}