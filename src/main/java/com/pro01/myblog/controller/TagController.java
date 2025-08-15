package com.pro01.myblog.controller;

import com.pro01.myblog.dto.TagSuggestDTO;
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
    public Result<List<TagSuggestDTO>> suggest(@RequestParam String q,
                                               @RequestParam(required = false) Integer limit) {
        List<TagSuggestDTO> list = tagService.suggest(q, limit);
        return Result.success(list);
    }
}