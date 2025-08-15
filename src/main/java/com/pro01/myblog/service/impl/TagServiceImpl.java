package com.pro01.myblog.service.impl;

import com.pro01.myblog.dto.TagItemAllDTO;
import com.pro01.myblog.dto.TagItemDTO;
import com.pro01.myblog.dto.TagSuggestDTO;
import com.pro01.myblog.mapper.TagMapper;
import com.pro01.myblog.pojo.PageResult;
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

    // 模糊查询
    @Override
    public PageResult<TagItemDTO> search(String q, Integer page, Integer size) {
        String key = (q == null) ? "" : q.trim();
        if (!StringUtils.hasText(key)) {
            // 空关键字：直接返回空结果，避免把“全部标签”都查出来
            return PageResult.of(0, List.of(), 1, size == null ? 20 : size);
        }
        int current = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        int offset = (current - 1) * pageSize;

        String like = "%" + key + "%";
        long total = tagMapper.countByLike(like);
        List<TagItemDTO> records = total == 0 ? List.of()
                : tagMapper.searchByLike(like, pageSize, offset);

        return PageResult.of(total, records, current, pageSize);
    }

    // 查看标签列表
    @Override
    public PageResult<TagItemAllDTO> listAllByHot(Integer page, Integer size) {
        int current = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        int offset = (current - 1) * pageSize;

        long total = tagMapper.countAllSystem();
        List<TagItemAllDTO> records = total == 0 ? List.of()
                : tagMapper.listAllOrderHot(pageSize, offset);

        return PageResult.of(total, records, current, pageSize);
    }
}