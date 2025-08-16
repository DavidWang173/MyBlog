package com.pro01.myblog.mapper;

import com.pro01.myblog.pojo.AiArticleSummary;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiArticleSummaryMapper {

    @Select("SELECT article_id AS articleId, ai_summary AS aiSummary, model, update_time AS updateTime " +
            "FROM ai_article_summaries WHERE article_id = #{articleId}")
    AiArticleSummary findByArticleId(@Param("articleId") Long articleId);

    @Insert("INSERT INTO ai_article_summaries (article_id, ai_summary, model) " +
            "VALUES (#{articleId}, #{aiSummary}, #{model}) " +
            "ON DUPLICATE KEY UPDATE ai_summary = VALUES(ai_summary), model = VALUES(model)")
    int upsert(AiArticleSummary s);
}