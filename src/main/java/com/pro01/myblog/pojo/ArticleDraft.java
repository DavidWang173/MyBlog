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
public class ArticleDraft {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String summary;
    private String category;
    private String coverUrl;
    private Boolean promptDismissed;
    private Boolean isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime lastEditTime;
}