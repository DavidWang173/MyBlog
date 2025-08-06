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
public class UserFollow {
    private Long followerId;
    private Long followingId;
    private LocalDateTime createTime;
}