package com.pro01.myblog.mapper;

import com.pro01.myblog.pojo.ArticleDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DraftMapper {

    // 保存草稿
    void insertDraft(ArticleDraft draft);

    int updateDraftByIdAndUser(@Param("id") Long id,
                               @Param("userId") Long userId,
                               @Param("title") String title,
                               @Param("content") String content,
                               @Param("summary") String summary,
                               @Param("category") String category,
                               @Param("coverUrl") String coverUrl,
                               @Param("tagsJson") String tagsJson);
}