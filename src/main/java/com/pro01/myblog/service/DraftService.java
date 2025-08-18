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
}