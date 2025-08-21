package com.pro01.myblog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private Long parentId;
    private Boolean isDeleted; // 是否被删除
    private Boolean isPinned;   // 是否置顶
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}