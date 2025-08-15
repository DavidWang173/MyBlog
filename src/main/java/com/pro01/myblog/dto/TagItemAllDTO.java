package com.pro01.myblog.dto;

import lombok.Data;

@Data
public class TagItemAllDTO {
    private Long id;
    private String name;
    private Boolean isSystem;
    private Long usageCount;
}
