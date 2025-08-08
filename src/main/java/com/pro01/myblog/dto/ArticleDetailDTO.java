package com.pro01.myblog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String category;
    private String coverUrl;
    private String nickname;      // 作者昵称
    private String avatar;        // 作者头像
    private Long viewCount;
    private LocalDateTime createTime;
}