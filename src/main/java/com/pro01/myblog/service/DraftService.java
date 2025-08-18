package com.pro01.myblog.service;

import com.pro01.myblog.dto.DraftDTO;
import com.pro01.myblog.dto.DraftSaveDTO;
import com.pro01.myblog.pojo.ArticleDraft;
import com.pro01.myblog.pojo.PageResult;

public interface DraftService {

    // 保存草稿
    Long saveDraft(Long userId, DraftSaveDTO dto);

    // 草稿列表
    PageResult<DraftDTO> listMyDrafts(Long userId, Integer page, Integer pageSize);

    // 模糊查询草稿（分页）
    PageResult<DraftDTO> searchMyDrafts(Long userId, String keyword, Integer page, Integer pageSize);

    // 查看草稿详情
    DraftDTO getMyDraftById(Long userId, Long draftId);
}