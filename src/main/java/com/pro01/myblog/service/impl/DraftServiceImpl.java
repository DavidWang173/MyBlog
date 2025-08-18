package com.pro01.myblog.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro01.myblog.dto.DraftSaveDTO;
import com.pro01.myblog.mapper.DraftMapper;
import com.pro01.myblog.mapper.TagMapper;
import com.pro01.myblog.pojo.ArticleDraft;
import com.pro01.myblog.service.DraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DraftServiceImpl implements DraftService {

    @Autowired
    private DraftMapper draftMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private final ObjectMapper objectMapper; // Spring Boot 默认已有（jackson）

    // 保存草稿
    @Override
    public Long saveDraft(Long userId, DraftSaveDTO dto) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }

        // 1) 处理标签：去空白、去重、限5、只保留系统标签
        List<String> validTags = normalizeAndValidateTags(dto.getTags());

        String tagsJson = null;
        try {
            if (validTags != null && !validTags.isEmpty()) {
                tagsJson = objectMapper.writeValueAsString(validTags);
            } else {
                // 空就存 NULL（也可以存 "[]"，看你偏好）
                tagsJson = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("标签序列化失败", e);
        }

        // 2) 新建 / 更新
        if (dto.getId() == null) {
            // 新建
            ArticleDraft draft = ArticleDraft.builder()
                    .userId(userId)
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .summary(dto.getSummary())
                    .category(dto.getCategory())
                    .coverUrl(dto.getCoverUrl())
                    .tagsJson(tagsJson)
                    .promptDismissed(false)  // 新建时默认可弹
                    .isDeleted(false)
                    .build();
            draftMapper.insertDraft(draft);
            return draft.getId();
        } else {
            // 更新（只允许本人 & 未删除），且重置 prompt_dismissed=false
            int rows = draftMapper.updateDraftByIdAndUser(
                    dto.getId(), userId,
                    dto.getTitle(), dto.getContent(), dto.getSummary(),
                    dto.getCategory(), dto.getCoverUrl(), tagsJson
            );
            if (rows <= 0) {
                throw new IllegalArgumentException("草稿不存在或无权修改");
            }
            return dto.getId();
        }
    }

    private List<String> normalizeAndValidateTags(List<String> tags) {
        if (tags == null) return List.of();
        List<String> cleaned = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
        if (cleaned.isEmpty()) return List.of();

        // 只保留系统标签（返回有效的标签名列表）
        List<String> systemNames = tagMapper.selectValidSystemNames(cleaned);
        // 这里我选择“宽松”：过滤非法的；如果你要严格，可在数量不等时抛错
        return systemNames;
    }
}