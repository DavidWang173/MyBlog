package com.pro01.myblog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleRecommendDTO {
    private Long id;
    private String title;
    private String summary;
    private String category;
    private String coverUrl;
    private String nickname;
    private String avatar;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createTime;
}
