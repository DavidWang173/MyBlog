package com.pro01.myblog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createTime;

    // 新增：标签（进入缓存）
    private List<String> tags;

    // 不进缓存，但会出现在 HTTP 响应里
    private Boolean isLiked;
}