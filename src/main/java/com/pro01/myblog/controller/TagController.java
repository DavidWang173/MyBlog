package com.pro01.myblog.controller;

import com.pro01.myblog.dto.TagItemAllDTO;
import com.pro01.myblog.dto.TagItemDTO;
import com.pro01.myblog.dto.TagSuggestDTO;
import com.pro01.myblog.pojo.PageResult;
import com.pro01.myblog.service.TagService;
import com.pro01.myblog.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    // 联想接口：前缀匹配
    @GetMapping("/tags/suggest")
    public Result<List<TagSuggestDTO>> suggest(@RequestParam String keyword,
                                               @RequestParam(required = false) Integer limit) {
        List<TagSuggestDTO> list = tagService.suggest(keyword, limit);
        return Result.success(list);
    }

    // 模糊包含 + 分页搜索
    @GetMapping("/tags")
    public Result<PageResult<TagItemDTO>> search(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer pageSize) {
        PageResult<TagItemDTO> pr = tagService.search(keyword, page, pageSize);
        return Result.success(pr);
    }

    // 分页显示所有系统标签，按热度降序
    @GetMapping("/tags/all-hot")
    public Result<PageResult<TagItemAllDTO>> listAllByHot(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(tagService.listAllByHot(page, pageSize));
    }
}