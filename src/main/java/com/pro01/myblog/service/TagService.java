package com.pro01.myblog.service;

import com.pro01.myblog.dto.TagSuggestDTO;

import java.util.List;

public interface TagService {

    // 联想
    List<TagSuggestDTO> suggest(String q, Integer limit);
}