package com.pro01.myblog.dto;

import lombok.Data;

@Data
public class TagItemDTO {
    private Long id;
    private String name;
    private Boolean isSystem; // 以后万一放开用户自建，也能区分
}