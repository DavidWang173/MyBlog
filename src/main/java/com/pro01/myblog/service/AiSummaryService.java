package com.pro01.myblog.service;

public interface AiSummaryService {
    String generateAndSave(Long articleId, Long userId);
}