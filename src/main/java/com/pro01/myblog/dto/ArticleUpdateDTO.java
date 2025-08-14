package com.pro01.myblog.dto;

import lombok.Data;

@Data
public class ArticleUpdateDTO {
    private String title;
    private String content;
    private String summary;
    private String category;   // 'TECH','LIFE','MUSIC','MOVIE','NOTE','FRIENDS'
    private String coverUrl;
}