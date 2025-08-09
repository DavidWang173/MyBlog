package com.pro01.myblog.dto;

import lombok.Data;

@Data
public class ArticleHotDTO {
    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private String nickname;
    private String avatar;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
}