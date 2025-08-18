package com.pro01.myblog.dto;

import lombok.Data;
import java.util.List;

@Data
public class DraftSaveDTO {
    private Long id;              // 草稿ID，null 表示新建
    private String title;
    private String content;
    private String summary;
    private String category;      // 可为空：TECH/LIFE/NOTE
    private String coverUrl;
    private List<String> tags;    // 可为 null 或 []
}