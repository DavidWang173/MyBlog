package com.pro01.myblog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleListDTO {
    private Long id;
    private String title;
    private String summary;
    private String category;
    private String coverUrl;
    private String nickname;
    private Long viewCount;
    private LocalDateTime createTime;
}