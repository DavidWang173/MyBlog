package com.pro01.myblog.service.impl;

import com.pro01.myblog.dto.TagSuggestDTO;
import com.pro01.myblog.mapper.TagMapper;
import com.pro01.myblog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    // 联想
    @Override
    public List<TagSuggestDTO> suggest(String q, Integer limit) {
        String key = (q == null) ? "" : q.trim();
        if (!StringUtils.hasText(key)) {
            return List.of();
        }
        int cap = (limit == null) ? 8 : Math.max(1, Math.min(limit, 20));
        return tagMapper.suggestByPrefix(key, cap);
    }
}