package com.pro01.myblog.service;

import com.pro01.myblog.dto.TagItemAllDTO;
import com.pro01.myblog.dto.TagItemDTO;
import com.pro01.myblog.dto.TagSuggestDTO;
import com.pro01.myblog.pojo.PageResult;

import java.util.List;

public interface TagService {

    // 联想
    List<TagSuggestDTO> suggest(String q, Integer limit);

    // 模糊查询
    PageResult<TagItemDTO> search(String q, Integer page, Integer size);

    // 显示标签列表
    PageResult<TagItemAllDTO> listAllByHot(Integer page, Integer size);
}