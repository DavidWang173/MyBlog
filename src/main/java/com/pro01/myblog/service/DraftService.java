package com.pro01.myblog.service;

import com.pro01.myblog.dto.DraftSaveDTO;

public interface DraftService {

    // 保存草稿
    Long saveDraft(Long userId, DraftSaveDTO dto);
}