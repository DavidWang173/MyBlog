package com.pro01.myblog.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
    private String coverUrl;
    private Boolean isTop;
    private Boolean isRecommend;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}