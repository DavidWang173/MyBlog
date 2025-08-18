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
}