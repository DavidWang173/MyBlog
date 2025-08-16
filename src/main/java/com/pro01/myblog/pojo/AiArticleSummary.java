package com.pro01.myblog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiArticleSummary {
    private Long articleId;
    private String aiSummary;
    private String model;
    private LocalDateTime updateTime;
}
