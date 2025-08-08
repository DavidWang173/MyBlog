package com.pro01.myblog.dto;

import lombok.Data;

@Data
public class ArticlePublishDTO {
    private String title;
    private String content;
    private String summary;   // 可为空
    private String category;
    private String coverUrl;  // 可为空
}