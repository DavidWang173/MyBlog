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

    // 草稿列表
    long countByUser(@Param("userId") Long userId);

    List<ArticleDraft> selectPageByUser(@Param("userId") Long userId,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);

    // 模糊查询草稿（分页）
    long countByUserAndLike(@Param("userId") Long userId,
                            @Param("like") String like);

    List<ArticleDraft> selectPageByUserAndLike(@Param("userId") Long userId,
                                               @Param("like") String like,
                                               @Param("limit") int limit,
                                               @Param("offset") int offset);

    // 查看草稿详情
    ArticleDraft selectByIdAndUser(@Param("id") Long id,
                                   @Param("userId") Long userId);

    // 最新草稿弹窗
    ArticleDraft selectLatestCandidateByUser(@Param("userId") Long userId);

    // 拒绝调出草稿
    int dismissByIdAndUser(@Param("id") Long id,
                           @Param("userId") Long userId);

    // 软删除草稿
    int softDeleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}