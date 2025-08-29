package com.pro01.myblog.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro01.myblog.dto.DraftSaveDTO;
import com.pro01.myblog.dto.DraftDTO;
import com.pro01.myblog.exception.UnauthorizedException;
import com.pro01.myblog.mapper.DraftMapper;
import com.pro01.myblog.mapper.TagMapper;
import com.pro01.myblog.pojo.ArticleDraft;
import com.pro01.myblog.pojo.PageResult;
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

    private final ObjectMapper objectMapper; // Spring Boot 默认已有（jackson）

    // 保存草稿
    @Override
    public Long saveDraft(Long userId, DraftSaveDTO dto) {
        if (userId == null) {
            throw new UnauthorizedException("未登录");
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

    // 草稿列表
    @Override
    public PageResult<DraftDTO> listMyDrafts(Long userId, Integer page, Integer size) {
        if (userId == null) throw new UnauthorizedException("未登录");

        int current = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        int offset = (current - 1) * pageSize;

        long total = draftMapper.countByUser(userId);
        if (total == 0) {
            return PageResult.of(0, List.of(), current, pageSize);
        }

        List<ArticleDraft> rows = draftMapper.selectPageByUser(userId, pageSize, offset);
        List<DraftDTO> dtos = rows.stream().map(this::toDTO).collect(Collectors.toList());

        return PageResult.of(total, dtos, current, pageSize);
    }

    private DraftDTO toDTO(ArticleDraft d) {
        DraftDTO dto = new DraftDTO();
        if (d == null) return dto;

        dto.setId(d.getId());
        dto.setUserId(d.getUserId());
        dto.setTitle(d.getTitle());
        dto.setContent(d.getContent());
        dto.setSummary(d.getSummary());
        dto.setCategory(d.getCategory());
        dto.setCoverUrl(d.getCoverUrl());
        dto.setPromptDismissed(Boolean.TRUE.equals(d.getPromptDismissed()));
        dto.setIsDeleted(Boolean.TRUE.equals(d.getIsDeleted()));
        dto.setCreateTime(d.getCreateTime());
        dto.setLastEditTime(d.getLastEditTime());

        // 关键：把 JSON 字符串转为数组
        dto.setTags(parseTags(d.getTagsJson()));
        return dto;
    }

    private List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 容错：发生解析异常时返回空数组，避免把转义后的字符串透给前端
            return Collections.emptyList();
        }
    }

    // 模糊查询草稿（分页）
    @Override
    public PageResult<DraftDTO> searchMyDrafts(Long userId, String q, Integer page, Integer size) {
        if (userId == null) throw new UnauthorizedException("未登录");

        String key = (q == null) ? "" : q.trim();
        if (key.isEmpty()) {
            // 关键字为空时，返回空结果（避免和“列表接口”语义冲突）
            int current = (page == null || page < 1) ? 1 : page;
            int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
            return PageResult.of(0, List.of(), current, pageSize);
        }

        // 转义 % 和 _，并使用 ESCAPE '\\'
        String escaped = key.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String like = "%" + escaped + "%";

        int current = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        int offset = (current - 1) * pageSize;

        long total = draftMapper.countByUserAndLike(userId, like);
        if (total == 0) {
            return PageResult.of(0, List.of(), current, pageSize);
        }

        List<ArticleDraft> rows = draftMapper.selectPageByUserAndLike(userId, like, pageSize, offset);
        List<DraftDTO> dtos = rows.stream().map(this::toDTO).collect(Collectors.toList());
        return PageResult.of(total, dtos, current, pageSize);
    }

    // 查看草稿详情
    @Override
    public DraftDTO getMyDraftById(Long userId, Long draftId) {
        if (userId == null) throw new UnauthorizedException("未登录");
        ArticleDraft d = draftMapper.selectByIdAndUser(draftId, userId);
        if (d == null) throw new IllegalArgumentException("草稿不存在或已被删除");

        DraftDTO dto = new DraftDTO();
        dto.setId(d.getId());
        dto.setUserId(d.getUserId());
        dto.setTitle(d.getTitle());
        dto.setContent(d.getContent());
        dto.setSummary(d.getSummary());
        dto.setCategory(d.getCategory());
        dto.setCoverUrl(d.getCoverUrl());
        dto.setPromptDismissed(Boolean.TRUE.equals(d.getPromptDismissed()));
        dto.setIsDeleted(Boolean.TRUE.equals(d.getIsDeleted()));
        dto.setCreateTime(d.getCreateTime());
        dto.setLastEditTime(d.getLastEditTime());
        dto.setTags(parseTags(d.getTagsJson()));
        return dto;
    }

    // 最新草稿弹窗
    @Override
    public DraftDTO findLatestCandidate(Long userId) {
        if (userId == null) throw new UnauthorizedException("未登录");
        ArticleDraft d = draftMapper.selectLatestCandidateByUser(userId);
        if (d == null) return null;

        DraftDTO dto = new DraftDTO();
        dto.setId(d.getId());
        dto.setUserId(d.getUserId());
        dto.setTitle(d.getTitle());
        dto.setContent(d.getContent());
        dto.setSummary(d.getSummary());
        dto.setCategory(d.getCategory());
        dto.setCoverUrl(d.getCoverUrl());
        dto.setPromptDismissed(Boolean.TRUE.equals(d.getPromptDismissed()));
        dto.setIsDeleted(Boolean.TRUE.equals(d.getIsDeleted()));
        dto.setCreateTime(d.getCreateTime());
        dto.setLastEditTime(d.getLastEditTime());
        dto.setTags(parseTags(d.getTagsJson()));
        return dto;
    }

    // 拒绝调出草稿
    @Override
    public void dismissDraft(Long userId, Long draftId) {
        if (userId == null) throw new UnauthorizedException("未登录");
        // 幂等更新：只有本人、未删除、且当前为 false 才更新为 true
        draftMapper.dismissByIdAndUser(draftId, userId);
        // rows=0 的情况：已删除、非本人、或已是 true —— 都视为幂等成功，不抛错
    }

    // 软删除草稿
    @Override
    public void softDelete(Long userId, Long draftId) {
        if (userId == null) throw new UnauthorizedException("未登录");
        draftMapper.softDeleteByIdAndUser(draftId, userId);
        // 幂等：影响行数为 0 也不抛错（可能已删/不属于当前用户）
    }
}