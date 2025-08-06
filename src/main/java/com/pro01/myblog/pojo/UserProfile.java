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
public class UserProfile {
    private Long userId;
    private Integer articleCount;
    private Integer commentCount;
    private Integer fanCount;
    private Integer followCount;
    private Integer level;
    private Long experience;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}