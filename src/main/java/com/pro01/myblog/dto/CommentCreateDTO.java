package com.pro01.myblog.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private String content;   // 1~1000
    private Long parentId;    // 可为空；楼中楼
}