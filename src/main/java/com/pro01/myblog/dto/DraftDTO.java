package com.pro01.myblog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DraftDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String summary;
    private String category;
    private String coverUrl;

    // 对外返回的标签数组
    private List<String> tags;

    private Boolean promptDismissed;
    private Boolean isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime lastEditTime;
}