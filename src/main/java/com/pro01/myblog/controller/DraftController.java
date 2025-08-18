package com.pro01.myblog.controller;

import com.pro01.myblog.annotation.LoginRequired;
import com.pro01.myblog.dto.DraftDTO;
import com.pro01.myblog.dto.DraftSaveDTO;
import com.pro01.myblog.pojo.ArticleDraft;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.service.DraftService;
import com.pro01.myblog.utils.RequestUtil;
import com.pro01.myblog.pojo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DraftController {

    @Autowired
    private DraftService draftService;

    // 保存草稿
    @LoginRequired
    @PostMapping("/drafts/save")
    public Result<Long> saveDraft(@RequestBody DraftSaveDTO dto,
                                  HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        Long draftId = draftService.saveDraft(userId, dto);
        return Result.success(draftId);
    }

    // 草稿列表
    @GetMapping("/drafts/me")
    public Result<PageResult<DraftDTO>> myDrafts(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "20") Integer pageSize,
                                                 HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        PageResult<DraftDTO> pr = draftService.listMyDrafts(userId, page, pageSize);
        return Result.success(pr);
    }

    // 模糊查询草稿（分页）
    @LoginRequired
    @GetMapping("/drafts/me/search")
    public Result<PageResult<DraftDTO>> searchMyDrafts(@RequestParam String keyword,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer pageSize,
                                                       HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        return Result.success(draftService.searchMyDrafts(userId, keyword, page, pageSize));
    }

    // 查看草稿详情
    @LoginRequired
    @GetMapping("/drafts/{draftId}")
    public Result<DraftDTO> getDraft(@PathVariable Long draftId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        DraftDTO dto = draftService.getMyDraftById(userId, draftId);
        return Result.success(dto);
    }

    /** 发布页弹窗候选：只返回最新且未被拒绝的草稿；没有则返回 null */
    @LoginRequired
    @GetMapping("/drafts/me/latest-candidate")
    public Result<DraftDTO> latestCandidate(HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        DraftDTO dto = draftService.findLatestCandidate(userId);
        return Result.success(dto); // 没有候选时 dto 为 null
    }

    /** 拒绝“调出草稿”，幂等置 prompt_dismissed = true */
    @LoginRequired
    @PostMapping("/drafts/{draftId}/dismiss")
    public Result<Void> dismiss(@PathVariable Long draftId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        draftService.dismissDraft(userId, draftId);
        return Result.success();
    }

    // 软删除草稿
    @LoginRequired
    @DeleteMapping("/drafts/{draftId}")
    public Result<Void> deleteDraft(@PathVariable Long draftId, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        draftService.softDelete(userId, draftId);
        return Result.success();
    }
}