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
public class ArticleFavorite {
    private Long userId;
    private Long articleId;
    private boolean isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}