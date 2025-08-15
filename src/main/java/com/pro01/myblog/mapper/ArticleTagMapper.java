package com.pro01.myblog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper {
    void insertBatch(@Param("articleId") Long articleId, @Param("tagIds") List<Long> tagIds);
}